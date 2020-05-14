/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object ShortArraySerializer: ArraySerializer<ShortArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeShort(Array.getShort(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setShort(arr, index, input.readShort())
    }
}