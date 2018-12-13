/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object FloatArraySerializer: ArraySerializer<FloatArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeFloat(Array.getFloat(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setFloat(arr, index, input.readFloat())
    }
}