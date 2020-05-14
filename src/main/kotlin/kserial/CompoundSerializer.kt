/**
 *@author Nikolaus Knop
 */

package kserial

abstract class CompoundSerializer<T : Any> : InplaceSerializer<T> {
    abstract fun <T> components(obj: T): Sequence<Any>

    override fun serialize(obj: T, output: Output) {
        for (comp in components(obj)) {
            output.writeUntyped(comp)
        }
    }

    override fun deserialize(obj: T, input: Input) {
        for (comp in components(obj)) {
            input.readInplace(obj)
        }
    }
}