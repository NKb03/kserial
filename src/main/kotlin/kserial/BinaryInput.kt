/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

import kserial.internal.PrefixByte.isClsRef
import kserial.internal.PrefixByte.isClsShare
import kserial.internal.PrefixByte.isNull
import kserial.internal.PrefixByte.isRef
import kserial.internal.PrefixByte.isShare
import kserial.internal.PrefixByte.isUntyped
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.ConsoleHandler
import java.util.logging.Level.SEVERE
import java.util.logging.Logger

class BinaryInput(private val input: DataInput) : Input {
    private val cache = HashMap<Int, Any>()

    private val clsNameCache = HashMap<Int, String>()

    private fun readClass(prefix: Byte): Class<Any> {
        val name = readClassName(prefix)
        try {
            return Class.forName(name) as Class<Any>
        } catch (cnf: ClassNotFoundException) {
            throw SerializationException("Could not find serialized class '$name'", cnf)
        }
    }

    private fun readClassName(prefix: Byte): String =
        if (isClsRef(prefix)) clsNameCache[readInt()]!!
        else readString().also {
            if (isClsShare(prefix)) {
                val id = clsNameCache.size
                clsNameCache[id] = it
            }
        }

    override fun readByte(): Byte = runIO("reading byte") { input.readByte() }

    override fun readBoolean(): Boolean = runIO("reading boolean") { input.readBoolean() }

    override fun readShort(): Short = runIO("reading short") { input.readShort() }

    override fun readInt(): Int = runIO("reading int") { input.readInt() }

    override fun readLong(): Long = runIO("reading long") { input.readLong() }

    override fun readFloat(): Float = runIO("reading float") { input.readFloat() }

    override fun readDouble(): Double = runIO("reading double") { input.readDouble() }

    override fun readString(): String = runIO("reading string") { input.readUTF() }

    override fun readObject(context: SerialContext): Any? {
        val prefix = readByte()
        if (isUntyped(prefix)) untypedWithoutSpecifiedCls()
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> readObjectShared(context, prefix)
            else            -> readObjectUnshared(context, prefix)
        }
    }

    private fun untypedWithoutSpecifiedCls() {
        throw SerializationException("Cannot read untyped object without class specified")
    }

    override fun readObject(cls: Class<*>, context: SerialContext): Any? {
        val prefix = readByte()
        val anyCls = cls as Class<Any>
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> readObjectShared(context, anyCls, id = readInt())
            else            -> readObjectUnshared(context, anyCls)
        }
    }

    private fun readObjectUnshared(ctx: SerialContext, prefix: Byte): Any {
        val cls = readClass(prefix)
        return readObjectUnshared(ctx, cls)
    }

    private fun readObjectShared(ctx: SerialContext, prefix: Byte): Any {
        val id = readInt()
        val cls = readClass(prefix)
        return readObjectShared(ctx, cls, id)
    }

    private fun readRef(): Any {
        val id = readInt()
        return cache[id] ?: unresolvedRef(id)
    }

    private fun unresolvedRef(id: Int) {
        throw SerializationException("Did not find object ref with id $id")
    }

    private fun readObjectShared(context: SerialContext, cls: Class<Any>, id: Int): Any {
        val ser = context.getSerializer(cls.kotlin)
        return when (ser) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(cls)
                cache[id] = obj
                val cast = ser as InplaceSerializer<Any>
                cast.deserialize(obj, this, context)
                obj
            }
            is Serializer<*>        -> {
                val cast = ser as Serializer<Any>
                val obj = cast.deserialize(cls, this, context)
                cache[id] = obj
                obj
            }
            else                    -> unexpectedSerializer()
        }
    }

    private fun readObjectUnshared(context: SerialContext, cls: Class<Any>): Any {
        val serializer = context.getSerializer(cls.kotlin)
        return when (serializer) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(cls)
                val cast = serializer as InplaceSerializer<Any>
                cast.deserialize(obj, this, context)
                obj
            }
            is Serializer<*>        -> (serializer as Serializer<Any>).deserialize(cls, this, context)
            else                    -> unexpectedSerializer()
        }
    }

    private fun unexpectedSerializer() {
        throw AssertionError("Object returned by getSerializer is not a serializer")
    }

    override fun close() {
        if (input is AutoCloseable) runIO("Closing underlying input") { input.close() }
    }

    companion object {
        fun fromStream(stream: InputStream) = BinaryInput(DataInputStream(stream))

        fun fromByteArray(arr: ByteArray) = fromStream(ByteArrayInputStream(arr))

        fun fromFile(path: Path): BinaryInput {
            val stream = runIO("getting input stream") { Files.newInputStream(path) }
            return fromStream(stream)
        }

        val logger: Logger = Logger.getLogger(BinaryInput::class.java.name)

        init {
            logger.useParentHandlers = false
            logger.addHandler(ConsoleHandler())
        }

        private fun logException(ex: IOException) {
            logger.log(SEVERE, ex.message, ex)
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