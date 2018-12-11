/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import kserial.SharingMode.Unshared
import kserial.internal.PrefixByte.BYTE_T
import kserial.internal.PrefixByte.CHAR_T
import kserial.internal.PrefixByte.DOUBLE_T
import kserial.internal.PrefixByte.FALSE
import kserial.internal.PrefixByte.FLOAT_T
import kserial.internal.PrefixByte.INT_T
import kserial.internal.PrefixByte.LONG_T
import kserial.internal.PrefixByte.NULL
import kserial.internal.PrefixByte.SHORT_T
import kserial.internal.PrefixByte.STRING_T
import kserial.internal.PrefixByte.TRUE
import kserial.internal.PrefixByte.prefixByte
import java.io.DataOutput
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

internal class BinaryOutput(
    private val out: DataOutput,
    sharingMode: SharingMode = Unshared,
    private val shareClassNames: Boolean = true
) : Output {
    private val cache = sharingMode.createCache()

    private val clsNameCache = HashMap<String, Int>()

    override fun writeNull() = runIO("writing null") {
        out.writeByte(NULL)
    }

    override fun writeByte(byte: Byte) = runIO("writing byte") {
        out.writeByte(BYTE_T)
        out.writeByte(byte.toInt())
    }

    override fun writeBoolean(boolean: Boolean) = runIO("writing boolean") {
        if (boolean) out.writeByte(TRUE)
        else out.writeByte(FALSE)
    }

    override fun writeChar(char: Char) {
        out.writeByte(CHAR_T)
        out.writeChar(char.toInt())
    }

    override fun writeShort(short: Short) = runIO("writing short") {
        out.writeByte(SHORT_T)
        out.writeShort(short.toInt())
    }

    override fun writeInt(int: Int) = runIO("writing int") {
        out.writeByte(INT_T)
        out.writeInt(int)
    }

    override fun writeLong(long: Long) = runIO("writing long") {
        out.writeByte(LONG_T)
        out.writeLong(long)
    }

    override fun writeFloat(float: Float) = runIO("writing float") {
        out.writeByte(FLOAT_T)
        out.writeFloat(float)
    }

    override fun writeDouble(double: Double) = runIO("writing double") {
        out.writeByte(DOUBLE_T)
        out.writeDouble(double)
    }

    override fun writeString(str: String) = runIO("writing string") {
        out.writeByte(STRING_T)
        out.writeUTF(str)
    }

    override fun writeObject(obj: Any?, context: SerialContext, untyped: Boolean) {
        val cachedId = cache?.get(obj)
        when {
            obj == null      -> writeNull()
            obj.isPrimitive  -> writePrimitive(obj)
            cachedId != null -> writeObjectRef(cachedId)
            else             -> writeObjectNotNull(obj, untyped, context)
        }
    }

    private fun writePrimitive(obj: Any) {
        when (obj) {
            is Byte   -> writeByte(obj)
            true      -> out.writeByte(TRUE)
            false     -> out.writeByte(FALSE)
            is Char   -> writeChar(obj)
            is Short  -> writeShort(obj)
            is Int    -> writeInt(obj)
            is Long   -> writeLong(obj)
            is Float  -> writeFloat(obj)
            is Double -> writeDouble(obj)
            is String -> writeString(obj)
            else      -> throw AssertionError("Non primitive object passed to writePrimitive")
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
        val prefix = prefixByte(
            share = cache != null,
            untyped = untyped,
            clsShare = shareClassNames && clsId == null,
            clsRef = clsId != null
        )
        out.writeByte(prefix)
    }

    private fun writeClass(clsId: Int?, name: String) {
        if (clsId != null) out.writeInt(clsId)
        else {
            out.writeUTF(name)
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
            out.writeInt(id)
            cache[obj] = id
        }
    }

    private fun writeObjectRef(cachedId: Int) {
        out.writeByte(prefixByte(ref = true))
        out.writeInt(cachedId)
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