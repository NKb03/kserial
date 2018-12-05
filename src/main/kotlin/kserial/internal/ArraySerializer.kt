/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import java.lang.reflect.Array

internal abstract class ArraySerializer<Arr: Any> : Serializer<Arr> {
    protected abstract fun serializeElement(arr: Any, index: Int, output: Output, context: SerialContext)

    protected abstract fun deserializeElement(arr: Any, index: Int, input: Input, context: SerialContext)

    override fun serialize(obj: Arr, output: Output, context: SerialContext) {
        val l = Array.getLength(obj)
        output.writeInt(l)
        for (i in 0 until l) {
            serializeElement(obj, i, output, context)
        }
    }

    override fun deserialize(cls: Class<Arr>, input: Input, context: SerialContext): Arr {
        val l = input.readInt()
        val arr = Array.newInstance(cls.componentType, l) as Arr
        for (i in 0 until l) {
            deserializeElement(arr, i, input, context)
        }
        return arr
    }
}