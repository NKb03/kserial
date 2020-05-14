/**
 *@author Nikolaus Knop
 */

package kserial.lens

import kserial.Input
import kserial.Output

class SerialFormat<in T : Any>(lenses: List<MutableLens<T, *, *>>) {
    constructor(vararg lenses: MutableLens<T, *, *>) : this(lenses.asList())

    @Suppress("UNCHECKED_CAST")
    internal val lenses = lenses as List<MutableLens<T, Any?, Any?>>

    internal val serializer = FormatSerializer(this)

    fun serialize(obj: T, output: Output) {
        serializer.serialize(obj, output)
    }

    fun deserialize(obj: T, input: Input) {
        serializer.deserialize(obj, input)
    }
}