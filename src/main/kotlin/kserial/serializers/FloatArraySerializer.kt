/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object FloatArraySerializer: ArraySerializer<FloatArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeFloat(Array.getFloat(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setFloat(arr, index, input.readFloat())
    }
}