/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object ByteArraySerializer: ArraySerializer<ByteArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeByte(Array.getByte(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setByte(arr, index, input.readByte())
    }
}