/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.serializers

import kserial.*
import java.lang.reflect.Array

internal abstract class ArraySerializer<Arr: Any> : Serializer<Arr> {
    protected abstract fun serializeElement(arr: Any, index: Int, output: Output)

    protected abstract fun deserializeElement(arr: Any, index: Int, input: Input)

    override fun serialize(obj: Arr, output: Output) {
        val l = Array.getLength(obj)
        output.writeInt(l)
        for (i in 0 until l) {
            serializeElement(obj, i, output)
        }
    }

    override fun deserialize(cls: Class<Arr>, input: Input): Arr {
        val l = input.readInt()
        val arr = Array.newInstance(cls.componentType, l) as Arr
        for (i in 0 until l) {
            deserializeElement(arr, i, input)
        }
        return arr
    }
}