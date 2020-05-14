/**
 * @author Nikolaus Knop
 */

package kserial.serializers

import kserial.*

internal abstract class CollectionSerializer<C : Collection<Any?>, MC : MutableCollection<Any?>> : Serializer<C> {
    protected abstract fun createCollection(size: Int): MC

    override fun serialize(obj: C, output: Output) {
        output.writeInt(obj.size)
        for (e in obj) output.writeObject(e)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(cls: Class<C>, input: Input): C {
        val size = input.readInt()
        val c = createCollection(size)
        for (i in 0 until size) c.add(input.readObject())
        return c as C
    }
}