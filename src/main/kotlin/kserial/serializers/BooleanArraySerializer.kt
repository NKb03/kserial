/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object BooleanArraySerializer: ArraySerializer<BooleanArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeBoolean(Array.getBoolean(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setBoolean(arr, index, input.readBoolean())
    }
}