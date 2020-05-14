/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object DoubleArraySerializer: ArraySerializer<DoubleArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeDouble(Array.getDouble(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setDouble(arr, index, input.readDouble())
    }
}