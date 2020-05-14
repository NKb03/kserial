package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object CharArraySerializer : ArraySerializer<CharArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeChar(Array.getChar(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setChar(arr, index, input.readChar())
    }
}