package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object IntArraySerializer: ArraySerializer<IntArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        val i = Array.getInt(arr, index)
        output.writeInt(i)
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setInt(arr, index, input.readInt())
    }
}
