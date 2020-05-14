/**
 *@author Nikolaus Knop
 */

package kserial.lens

import kserial.*

/**
 * A [Serializer] that uses the specified format [f] to serialize objects of type [T]
 */
open class FormatSerializer<in T : Any>(private val f: SerialFormat<T>) : InplaceSerializer<T> {
    override fun serialize(obj: T, output: Output) {
        for (lens in f.lenses) {
            val v = lens.get(obj)
            output.writeObject(v)
        }
    }

    override fun deserialize(obj: T, input: Input) {
        for (lens in f.lenses) {
            val v = input.readObject()
            lens.set(obj, v)
        }
    }
}