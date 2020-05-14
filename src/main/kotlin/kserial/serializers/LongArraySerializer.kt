/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.Input
import kserial.Output
import java.lang.reflect.Array

internal object LongArraySerializer: ArraySerializer<LongArray>() {
    override fun serializeElement(arr: Any, index: Int, output: Output) {
        output.writeLong(Array.getLong(arr, index))
    }

    override fun deserializeElement(arr: Any, index: Int, input: Input) {
        Array.setLong(arr, index, input.readLong())
    }
}