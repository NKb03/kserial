/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.*

internal object SerializableSerializer: InplaceSerializer<Serializable> {
    override fun deserialize(obj: Serializable, input: Input) {
        obj.deserialize(input)
    }

    override fun serialize(obj: Serializable, output: Output) {
        obj.serialize(output)
    }
}