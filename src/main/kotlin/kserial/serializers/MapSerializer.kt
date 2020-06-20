/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.*

internal object MapSerializer : InplaceSerializer<MutableMap<Any?, Any?>> {
    override fun serialize(obj: MutableMap<Any?, Any?>, output: Output) {
        output.writeInt(obj.size)
        for ((k, v) in obj) {
            output.writeObject(k)
            output.writeObject(v)
        }
    }

    override fun deserialize(obj: MutableMap<Any?, Any?>, input: Input) {
        val size = input.readInt()
        repeat(size) {
            val k = input.readObject()
            val v = input.readObject()
            obj[k] = v
        }
    }
}