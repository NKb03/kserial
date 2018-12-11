@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object DefaultSerializer : InplaceSerializer<Any> {
    override fun serialize(obj: Any, output: Output, context: SerialContext) {
        val cls = obj.javaClass
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
            val v = f.get(obj)
            val untyped = Modifier.isFinal(f.type.modifiers)
            output.writeObject(v, context, untyped)
        } catch (t: Throwable) {
            throw SerializationException("Exception while writing $f", t)
        }
    }

    private fun getFields(cls: Class<Any>): List<Field> =
        cls.declaredFields
            .filter { f ->
                !Modifier.isStatic(f.modifiers) && !cls.isAnnotationPresent(KTransient::class.java)
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
            val v = if (Modifier.isFinal(f.type.modifiers)) {
                input.readObject(f.type, context)
            } else {
                input.readObject(context)
            }
            f.set(obj, v)
        } catch (t: Throwable) {
            throw SerializationException("Exception while reading $f", t)
        }
    }
}
