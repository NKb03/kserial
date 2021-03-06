/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import bundles.Bundle
import bundles.createBundle
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

internal class BinaryInput(private val input: DataInput, override val context: SerialContext) : Input {
    private val cache = HashMap<Int, Any>()

    private val clsNameCache = HashMap<Int, String>()

    override val bundle: Bundle = createBundle()

    private fun readClass(prefix: Int): Class<Any> {
        val name = readClassName(prefix)
        try {
            return context.loadClass(name) as Class<Any>
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

    override fun readObject(): Any? {
        val prefix = input.readByte().toInt()
        if (prefix < 0) return readPrimitive(prefix)
        if (isUntyped(prefix)) {
            throw SerializationException("Cannot read untyped object without class specified")
        }
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> readObjectShared(prefix)
            else            -> readObjectUnshared(prefix)
        }
    }

    override fun readInplace(obj: Any) {
        val prefix = input.readByte().toInt()
        if (prefix < 0) throw SerializationException("Cannot read primitive object inplace")
        if (!isUntyped(prefix)) throw SerializationException("readInplace expects untyped object")
        val ser = context.getSerializer(obj::class)
        if (ser !is InplaceSerializer<*>) throw SerializationException("readInplace expects inplace serializer")
        ser as InplaceSerializer<Any>
        ser.deserialize(obj, this)
    }

    override fun <T : Any> readObject(cls: Class<T>): T? {
        val o = readObjectImpl(cls)
        return o as T?
    }

    private fun <T : Any> readObjectImpl(
        cls: Class<T>
    ): Any? {
        if (cls.isPrimitive) return readPrimitive(cls)
        val prefix = input.readByte().toInt()
        val anyCls = cls as Class<Any>
        return when {
            isNull(prefix)  -> null
            isRef(prefix)   -> readRef()
            isShare(prefix) -> {
                val id = input.readInt()
                readObjectShared(anyCls, id)
            }
            else            -> readObjectUnshared(anyCls)
        }
    }

    private fun readPrimitive(type: Class<*>): Any = when (type) {
        Byte::class.java    -> readByte()
        Boolean::class.java -> readBoolean()
        Char::class.java    -> readChar()
        Short::class.java   -> readShort()
        Int::class.java     -> readInt()
        Long::class.java    -> readLong()
        Float::class.java   -> readFloat()
        Double::class.java  -> readDouble()
        String::class.java  -> readString()
        else                -> throw AssertionError("Non primitive class passed to readPrimitive")
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

    private fun readObjectUnshared(prefix: Int): Any {
        val cls = readClass(prefix)
        return readObjectUnshared(cls)
    }

    private fun readObjectShared(prefix: Int): Any {
        val id = input.readInt()
        val cls = readClass(prefix)
        return readObjectShared(cls, id)
    }

    private fun readRef(): Any {
        val id = input.readInt()
        return cache[id] ?: throw SerializationException("Did not find object ref with id $id")
    }

    private fun readObjectShared(cls: Class<Any>, id: Int): Any {
        val kt = cls.kotlin
        return when (val ser = context.getSerializer(kt)) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(kt, bundle)
                cache[id] = obj
                val cast = ser as InplaceSerializer<Any>
                cast.deserialize(obj, this)
                obj
            }
            is Serializer<*>        -> {
                val cast = ser as Serializer<Any>
                val obj = cast.deserialize(cls, this)
                cache[id] = obj
                obj
            }
            else                    -> unexpectedSerializer()
        }
    }

    private fun readObjectUnshared(cls: Class<Any>): Any {
        val kt = cls.kotlin
        return when (val serializer = context.getSerializer(kt)) {
            is InplaceSerializer<*> -> {
                val obj = context.createInstance(kt, bundle)
                val cast = serializer as InplaceSerializer<Any>
                cast.deserialize(obj, this)
                obj
            }
            is Serializer<*>        -> (serializer as Serializer<Any>).deserialize(cls, this)
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
        fun fromStream(stream: InputStream, context: SerialContext) = BinaryInput(DataInputStream(stream), context)

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