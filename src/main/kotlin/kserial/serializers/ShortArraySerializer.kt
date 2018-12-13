/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal object ShortArraySerializer: ArraySerializer<ShortArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext) {
        output.writeShort(Array.getShort(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext) {
        Array.setShort(arr, index, input.readShort())
    }
}