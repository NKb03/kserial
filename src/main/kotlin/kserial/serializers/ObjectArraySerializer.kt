package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object ObjectArraySerializer: ArraySerializer<kotlin.Array<Any?>>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        val o = Array.get(arr, index)
        output.writeObject(o)
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.set(arr, index, input.readObject())
    }
}