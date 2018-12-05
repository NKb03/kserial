package kserial.internal

import kserial.*
import java.lang.reflect.Array

internal object ObjectArraySerializer: ArraySerializer<kotlin.Array<Any?>>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        val o = Array.get(arr, index)
        output.writeObject(o, context)
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.set(arr, index, input.readObject(context))
    }
}