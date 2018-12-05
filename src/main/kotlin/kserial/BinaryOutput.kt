/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

import kserial.SharingMode.Unshared
import kserial.internal.Constants
import kserial.internal.Constants.SHARING
import java.io.*
import java.lang.AssertionError
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

class BinaryOutput(private val out: DataOutput, sharingMode: SharingMode = Unshared) : Output {
    private val cache = sharingMode.createCache()

    override fun writeNull() = runIO("writing null") {
        out.writeByte(Constants.NULL.toInt())
    }

    override fun writeClass(cls: Class<*>) = runIO("writing object start") {
        out.writeByte(Constants.OBJECT_START.toInt())
        out.writeUTF(cls.name)
    }

    override fun writeByte(byte: Byte) = runIO("writing byte") {
        out.writeByte(byte.toInt())
    }

    override fun writeBoolean(boolean: Boolean) = runIO("writing boolean") {
        out.writeBoolean(boolean)
    }

    override fun writeShort(short: Short) = runIO("writing short") {
        out.writeShort(short.toInt())
    }

    override fun writeInt(int: Int) = runIO("writing int") {
        out.writeInt(int)
    }

    override fun writeLong(long: Long) = runIO("writing long") {
        out.writeLong(long)
    }

    override fun writeFloat(float: Float) = runIO("writing float") {
        out.writeFloat(float)
    }

    override fun writeDouble(double: Double) = runIO("writing double") {
        out.writeDouble(double)
    }

    override fun writeString(str: String) = runIO("writing string") {
        out.writeUTF(str)
    }

    override fun writeObject(obj: Any?, context: SerialContext, typed: Boolean) {
        val cachedId = cache?.get(obj)
        when {
            obj == null      -> writeNull()
            cachedId != null -> writeCachedId(cachedId)
            else             -> writeNew(obj, context, typed)
        }
    }

    private fun writeNew(obj: Any, context: SerialContext, typed: Boolean) {
        registerCache(obj)
        implWriteObject(obj, typed, context)
    }

    private fun implWriteObject(obj: Any, typed: Boolean, context: SerialContext) {
        val cls = obj.javaClass
        if (typed) {
            writeString(cls.name)
        }
        val serializer = context.getSerializer(cls.kotlin)
        when (serializer) {
            is InplaceSerializer<*> -> (serializer as InplaceSerializer<Any>).serialize(obj, this, context)
            is Serializer<*>        -> (serializer as Serializer<Any>).serialize(obj, this, context)
            else                    -> throw AssertionError("Object returned by getSerializer is not a serializer")
        }
    }

    private fun registerCache(obj: Any) {
        cache?.run {
            val id = size
            put(obj, id)
            writeByte(SHARING)
            writeInt(id)
        }
    }

    private fun writeCachedId(cachedId: Int) {
        writeByte(Constants.ID)
        writeInt(cachedId)
    }

    override fun close() {
        if (out is AutoCloseable) out.close()
    }

    companion object {
        fun toStream(stream: OutputStream, sharingMode: SharingMode = Unshared) =
            BinaryOutput(DataOutputStream(stream), sharingMode)

        fun toFile(path: Path, sharingMode: SharingMode = Unshared): BinaryOutput {
            val stream = runIO("getting output stream") { Files.newOutputStream(path) }
            return BinaryOutput.toStream(stream, sharingMode)
        }

        val logger: Logger = Logger.getLogger(BinaryOutput::class.java.name)

        private fun logException(ex: IOException) {
            logger.log(Level.SEVERE, ex.message!!, ex)
        }

        private inline fun <T> runIO(context: String, action: () -> T): T {
            try {
                return action()
            } catch (ex: IOException) {
                logException(ex)
                throw SerializationException("IOException while $context", ex)
            }
        }
    }
}