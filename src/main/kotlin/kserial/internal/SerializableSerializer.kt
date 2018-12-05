/**
 *@author Nikolaus Knop
 */

package kserial.internal

import kserial.*

internal object SerializableSerializer: InplaceSerializer<Serializable> {
    override fun deserialize(obj: Serializable, input: Input, context: SerialContext) {
        obj.deserialize(input, context)
    }

    override fun serialize(obj: Serializable, output: Output, context: SerialContext) {
        obj.serialize(output, context)
    }
}