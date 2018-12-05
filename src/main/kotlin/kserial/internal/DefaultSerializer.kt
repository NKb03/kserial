@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object DefaultSerializer : InplaceSerializer<Any> {
    override fun serialize(obj: Any, output: Output, context: SerialContext) {
        val cls = obj.javaClass
        val kCls = cls.kotlin
        require(kCls.objectInstance == null) { "Cannot serialize kotlin singletons" }
        writeFields(context, cls, obj, output)
    }

    private fun writeFields(context: SerialContext, cls: Class<Any>, obj: Any, output: Output) {
        val fields = getFields(cls)
        for (f in fields) {
            writeField(f, obj, output, context)
        }
    }

    private fun writeField(
        f: Field,
        obj: Any,
        output: Output,
        context: SerialContext
    ) {
        try {
            f.isAccessible = true
            if (f.type.isPrimitive) {
                writePrimitiveValue(f, obj, output)
            } else {
                val v = f.get(obj)
                val typed = !Modifier.isFinal(f.type.modifiers)
                output.writeObject(v, context, typed)
            }
        } catch (t: Throwable) {
            throw SerializationException("Exception while writing $f")
        }
    }

    private fun getFields(cls: Class<Any>): List<Field> =
        cls.declaredFields
            .filter { f ->
                !Modifier.isStatic(f.modifiers) && !Modifier.isTransient(f.modifiers)
            }

    private fun writePrimitiveValue(f: Field, obj: Any, output: Output) {
        when (f.type.kotlin) {
            Byte::class    -> output.writeByte(f.getByte(obj))
            Boolean::class -> output.writeBoolean(f.getBoolean(obj))
            Short::class   -> output.writeShort(f.getShort(obj))
            Int::class     -> output.writeInt(f.getInt(obj))
            Long::class    -> output.writeLong(f.getLong(obj))
            Float::class   -> output.writeFloat(f.getFloat(obj))
            Double::class  -> output.writeDouble(f.getDouble(obj))
        }
    }

    override fun deserialize(obj: Any, input: Input, context: SerialContext) {
        val cls = obj.javaClass
        val fields = getFields(cls)
        for (f in fields) {
            readField(f, input, context, obj)
        }
    }

    private fun readField(
        f: Field,
        input: Input,
        context: SerialContext,
        obj: Any
    ) {
        try {
            f.isAccessible = true
            if (f.type.isPrimitive) {
                readPrimitiveField(f, obj, input)
            } else {
                val v = if (Modifier.isFinal(f.type.modifiers)) {
                    input.readObject(f.type.kotlin, context)
                } else {
                    input.readObject(context)
                }
                f.set(obj, v)
            }
        } catch (t: Throwable) {
            throw SerializationException("Exception while reading $f")
        }
    }

    private fun readPrimitiveField(f: Field, obj: Any, input: Input) {
        when (f.type.kotlin) {
            Boolean::class -> f.setBoolean(obj, input.readBoolean())
            Byte::class    -> f.setByte(obj, input.readByte())
            Short::class   -> f.setShort(obj, input.readShort())
            Int::class     -> f.setInt(obj, input.readInt())
            Long::class    -> f.setLong(obj, input.readLong())
            Float::class   -> f.setFloat(obj, input.readFloat())
            Double::class  -> f.setDouble(obj, input.readDouble())
        }
    }

}
