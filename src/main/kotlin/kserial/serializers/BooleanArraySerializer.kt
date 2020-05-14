/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object BooleanArraySerializer: ArraySerializer<BooleanArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeBoolean(Array.getBoolean(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setBoolean(arr, index, input.readBoolean())
    }
}