package kserial.internal

import kserial.*
import java.lang.reflect.Array

internal object CharArraySerializer : ArraySerializer<CharArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeChar(Array.getChar(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setChar(arr, index, input.readChar())
    }
}