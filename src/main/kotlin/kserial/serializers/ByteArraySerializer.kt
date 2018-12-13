/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object ByteArraySerializer: ArraySerializer<ByteArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeByte(Array.getByte(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setByte(arr, index, input.readByte())
    }
}