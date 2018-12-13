package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object IntArraySerializer: ArraySerializer<IntArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        val i = Array.getInt(arr, index)
        output.writeInt(i)
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setInt(arr, index, input.readInt())
    }
}
