package kserial.internal

import kserial.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object Impl {
    fun writeField(
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

    fun readField(
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