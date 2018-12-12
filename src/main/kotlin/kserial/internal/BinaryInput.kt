/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import kserial.internal.PrefixByte.BYTE_T
import kserial.internal.PrefixByte.CHAR_T
import kserial.internal.PrefixByte.DOUBLE_T
import kserial.internal.PrefixByte.FALSE
import kserial.internal.PrefixByte.FLOAT_T
import kserial.internal.PrefixByte.INT_T
import kserial.internal.PrefixByte.LONG_T
import kserial.internal.PrefixByte.SHORT_T
import kserial.internal.PrefixByte.STRING_T
import kserial.internal.PrefixByte.TRUE
import kserial.internal.PrefixByte.isClsRef
import kserial.internal.PrefixByte.isClsShare
import kserial.internal.PrefixByte.isNull
import kserial.internal.PrefixByte.isRef
import kserial.internal.PrefixByte.isShare
import kserial.internal.PrefixByte.isUntyped
import java.io.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level.SEVERE
import java.util.logging.Logger
import kotlin.reflect.KClass

internal class BinaryInput(private val input: DataInput) : Input {
    private val cache = HashMap<Int, Any>()

    private val clsNameCache = HashMap<Int, String>()

    private fun readClass(prefix: Int): Class<Any> {
        val name = readClassName(prefix)
        try {
            return Class.forName(name) as Class<Any>
        } catch (cnf: ClassNotFoundException) {
            throw SerializationException("Could not find serialized class '$name'", cnf)
        }
    }

    private fun readClassName(prefix: Int): String =
        if (isClsRef(prefix)) clsNameCache[input.readInt()]!!
        else input.readUTF().also {
            if (isClsShare(prefix)) {
                val id = clsNameCache.size
                clsNameCache[id] = it
            }
        }

    private fun requireNextType(expectedByte: Int) {
        val next = input.readByte().toInt()
        if (next != expectedByte) {
            val actualName = PrefixByte.getTypeName(next)
            val expectedName = PrefixByte.getTypeName(expectedByte)
            throw SerializationException("Expected a $expectedName but got a $actualName")
        }
    }

    override fun readByte(): Byte = runIO("reading byte") {
        requireNextType(BYTE_T)
        input.readByte()
    }

    override fun readBoolean(): Boolean = runIO("reading boolean") {
        val b = input.readByte().toInt()
        return when (b) {
            TRUE  -> true
            FALSE -> false
            else  -> {
                val actualType = PrefixByte.getTypeName(b)
                throw SerializationException("Expected boolean but got a $actualType")
            }
        }
    }

    override fun readChar(): Char = runIO("reading char") {
        requireNextType(CHAR_T)
        input.readChar()
    }

    override fun readShort(): Short = runIO("reading short") {
        requireNextType(SHORT_T)
        input.readShort()
    }

    override fun readInt(): Int = runIO("reading int") {
        requireNextType(INT_T)
        input.readInt()
    }

    override fun readLong(): Long = runIO("reading long") {
        requireNextType(LONG_T)
        input.readLong()
    }

    override fun readFloat(): Float = runIO("reading float") {
        requireNextType(FLOAT_T)
        input.readFloat()
    }

    override fun readDouble(): Double = runIO("reading double") {
        requireNextType(DOUBLE_T)
        input.readDouble()
    }

    override fun readString(): String = runIO("reading string") {
        requireNextType(STRING_T)
        input.readUTF()
    }

    override fun readObject(context: SerialContext): Any? {
        val prefix = input.readByte().toInt()
        if (prefix < 0) return readPrimitive(prefix)
        if (isUntyped(prefix)) {
            throw SerializationException("Cannot read untyped object without class specified")
        }
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> readObjectShared(context, prefix)
            else            -> readObjectUnshared(context, prefix)
        }
    }

    override fun <T : Any> readObject(cls: Class<T>, context: SerialContext): T? {
        val o = readObjectImpl(cls, context)
        return o as T?
    }

    private fun <T : Any> readObjectImpl(
        cls: Class<T>,
        context: SerialContext
    ): Any? {
        val kt = cls.kotlin
        if (kt.isPrimitive) return readPrimitive(kt)
        val prefix = input.readByte().toInt()
        val anyCls = cls as Class<Any>
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> {
                val id = input.readInt()
                readObjectShared(context, anyCls, id)
            }
            else            -> readObjectUnshared(context, anyCls)
        }
    }

    private fun readPrimitive(type: KClass<*>): Any = when (type) {
        Byte::class    -> readByte()
        Boolean::class -> readBoolean()
        Char::class    -> readChar()
        Short::class   -> readShort()
        Int::class     -> readInt()
        Long::class    -> readLong()
        Float::class   -> readFloat()
        Double::class  -> readDouble()
        String::class  -> readString()
        else           -> throw AssertionError("Non primitive class passed to readPrimitive")
    }

    private fun readPrimitive(type: Int): Any = when (type) {
        BYTE_T   -> input.readByte()
        TRUE     -> true
        FALSE    -> false
        CHAR_T   -> input.readChar()
        SHORT_T  -> input.readShort()
        INT_T    -> input.readInt()
        LONG_T   -> input.readLong()
        FLOAT_T  -> input.readFloat()
        DOUBLE_T -> input.readDouble()
        STRING_T -> input.readUTF()
        else     -> throw SerializationException("Expected primitive type prefix but got $type")
    }

    private fun readObjectUnshared(ctx: SerialContext, prefix: Int): Any {
        val cls = readClass(prefix)
        return readObjectUnshared(ctx, cls)
    }

    private fun readObjectShared(ctx: SerialContext, prefix: Int): Any {
        val id = input.readInt()
        val cls = readClass(prefix)
        return readObjectShared(ctx, cls, id)
    }

    private fun readRef(): Any {
        val id = input.readInt()
        return cache[id] ?: throw SerializationException("Did not find object ref with id $id")
    }

    private fun readObjectShared(context: SerialContext, cls: Class<Any>, id: Int): Any {
        val kt = cls.kotlin
        val ser = context.getSerializer(kt)
        return when (ser) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(kt)
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
        val kt = cls.kotlin
        val serializer = context.getSerializer(kt)
        return when (serializer) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(kt)
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