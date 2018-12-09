/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import kserial.SharingMode.Unshared
import kserial.internal.PrefixByte.NULL
import kserial.internal.PrefixByte.byte
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

class BinaryOutput(
    private val out: DataOutput,
    sharingMode: SharingMode = Unshared,
    private val shareClassNames: Boolean = true
) : Output {
    private val cache = sharingMode.createCache()

    private val clsNameCache = HashMap<String, Int>()

    override fun writeNull() = runIO("writing null") {
        writeByte(NULL)
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

    override fun writeObject(obj: Any?, context: SerialContext, untyped: Boolean) {
        val cachedId = cache?.get(obj)
        when {
            obj == null      -> writeNull()
            cachedId != null -> writeObjectRef(cachedId)
            else             -> writeObjectNotNull(obj, untyped, context)
        }
    }

    private fun writeObjectNotNull(
        obj: Any,
        untyped: Boolean,
        context: SerialContext
    ) {
        val name = obj.javaClass.name
        val clsId = clsNameCache[name]
        writePrefix(untyped, clsId)
        shareObject(obj)
        if (!untyped) writeClass(clsId, name)
        implWriteObject(obj, context)
    }

    private fun writePrefix(untyped: Boolean, clsId: Int?) {
        val clsRef = clsId != null
        val prefix = byte(
            share = cache != null,
            untyped = untyped,
            clsShare = shareClassNames && !clsRef,
            clsRef = clsRef
        )
        writeByte(prefix)
    }

    private fun writeClass(clsId: Int?, name: String) {
        if (clsId != null) writeInt(clsId)
        else {
            writeString(name)
            shareClass(name)
        }
    }

    private fun shareClass(name: String) {
        if (shareClassNames) {
            val id = clsNameCache.size
            clsNameCache[name] = id
        }
    }

    private fun shareObject(obj: Any) {
        cache?.run {
            val id = size
            writeInt(id)
            cache[obj] = id
        }
    }

    private fun writeObjectRef(cachedId: Int) {
        writeByte(byte(ref = true))
        writeInt(cachedId)
    }

    private fun implWriteObject(obj: Any, context: SerialContext) {
        val serializer = context.getSerializer(obj::class)
        when (serializer) {
            is InplaceSerializer<*> -> (serializer as InplaceSerializer<Any>).serialize(obj, this, context)
            is Serializer<*>        -> (serializer as Serializer<Any>).serialize(obj, this, context)
            else                    -> throw AssertionError("Object returned by getSerializer is not a serializer")
        }
    }

    override fun close() {
        if (out is AutoCloseable) out.close()
    }

    companion object {
        fun toStream(stream: OutputStream, sharingMode: SharingMode = Unshared, shareClassNames: Boolean = true) =
            BinaryOutput(DataOutputStream(stream), sharingMode, shareClassNames)

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