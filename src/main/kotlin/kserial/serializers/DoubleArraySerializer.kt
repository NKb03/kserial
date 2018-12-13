/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object DoubleArraySerializer: ArraySerializer<DoubleArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeDouble(Array.getDouble(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setDouble(arr, index, input.readDouble())
    }
}