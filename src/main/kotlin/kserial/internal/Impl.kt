package kserial.internal

import kserial.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@Suppress("UNCHECKED_CAST")
internal object Impl {
    fun writeField(
        f: Field,
        obj: Any,
        output: Output
    ) {
        try {
            f.isAccessible = true
            val v = f.get(obj)
            val untyped = Modifier.isFinal(f.type.modifiers)
            output.writeObject(v, untyped)
        } catch (t: Throwable) {
            throw SerializationException("Exception while writing $f", t)
        }
    }

    fun readField(
        f: Field,
        input: Input,
        obj: Any
    ) {
        try {
            f.isAccessible = true
            val v = if (Modifier.isFinal(f.type.modifiers)) {
                input.readObject(f.type)
            } else {
                input.readObject()
            }
            f.set(obj, v)
        } catch (t: Throwable) {
            throw SerializationException("Exception while reading $f", t)
        }
    }
}