/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

import kserial.internal.Constants.ID
import kserial.internal.Constants.NULL
import kserial.internal.Constants.OBJECT_START
import kserial.internal.Constants.SHARING
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.ConsoleHandler
import java.util.logging.Level.SEVERE
import java.util.logging.Logger
import kotlin.reflect.KClass

class BinaryInput(private val input: DataInput) : Input {
    private val cache = HashMap<Int, Any>()

    fun readClass(): Class<Any> {
        val name = runIO("reading class name") { readString() }
        try {
            return Class.forName(name) as Class<Any>
        } catch (cnf: ClassNotFoundException) {
            throw SerializationException("Could not find serialized class '$name'", cnf)
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
        val next = readByte()
        return when (next) {
            NULL         -> null
            OBJECT_START -> readObjectImpl(context, readClass())
            SHARING      -> {
                val id = readInt()
                val cls = readClass()
                sharingReadObject(context, cls, id)
            }
            ID           -> readShared()
            else         -> throw SerializationException("Unexpected byte $next")
        }
    }

    override fun readObject(cls: KClass<*>, context: SerialContext): Any? {
        val next = readByte()
        val jCls = cls.java as Class<Any>
        return when (next) {
            NULL         -> null
            OBJECT_START -> readObjectImpl(context, jCls)
            SHARING      -> sharingReadObject(context, jCls, readInt())
            ID           -> readShared()
            else         -> throw SerializationException("Unexpected byte $next")
        }
    }

    private fun sharingReadObject(context: SerialContext, cls: Class<Any>, id: Int): Any {
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
            else                    -> throw AssertionError("Object returned by getSerializer is not a serializer")
        }
    }

    private fun readShared(): Any {
        val id = readInt()
        return cache[id]!!
    }

    private fun readObjectImpl(context: SerialContext, cls: Class<Any>): Any {
        val serializer = context.getSerializer(cls.kotlin)
        return when (serializer) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(cls)
                val cast = serializer as InplaceSerializer<Any>
                cast.deserialize(obj, this, context)
                obj
            }
            is Serializer<*>        -> (serializer as Serializer<Any>).deserialize(cls, this, context)
            else                    -> AssertionError("Object returned by getSerializer is not a serializer")
        }
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