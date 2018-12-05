/**
 *@author Nikolaus Knop
 */

package kserial.lens

import kserial.*

class SerialFormat<in T : Any>(lenses: List<MutableLens<T, *, *>>) {
    constructor(vararg lenses: MutableLens<T, *, *>): this(lenses.asList())

    @Suppress("UNCHECKED_CAST")
    internal val lenses = lenses as List<MutableLens<T, Any?, Any?>>

    internal val serializer = FormatSerializer(this)

    fun serialize(obj: T, output: Output, context: SerialContext) {
        serializer.serialize(obj, output, context)
    }

    fun deserialize(obj: T, input: Input, context: SerialContext) {
        serializer.deserialize(obj, input, context)
    }
}