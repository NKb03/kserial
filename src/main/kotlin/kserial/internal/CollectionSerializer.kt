/**
 *@author Nikolaus Knop
 */

package kserial.internal

import kserial.*

internal object CollectionSerializer : InplaceSerializer<MutableCollection<Any?>> {
    override fun serialize(obj: MutableCollection<Any?>, output: Output, context: SerialContext) {
        output.writeInt(obj.size)
        for (e in obj) {
            output.writeObject(e, context)
        }
    }

    override fun deserialize(obj: MutableCollection<Any?>, input: Input, context: SerialContext) {
        val size = input.readInt()
        repeat(size) {
            val el = input.readObject(context)
            obj.add(el)
        }
    }
}