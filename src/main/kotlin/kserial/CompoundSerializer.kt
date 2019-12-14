/**
 *@author Nikolaus Knop
 */

package kserial

abstract class CompoundSerializer<T : Any> : InplaceSerializer<T> {
    abstract fun <T> components(obj: T): Sequence<Any>

    override fun serialize(obj: T, output: Output, context: SerialContext) {
        for (comp in components(obj)) {
            output.writeObject(comp)
        }
    }

    override fun deserialize(obj: T, input: Input, context: SerialContext) {
        for (comp in components(obj)) {
            input.readInplace(obj)
        }
    }
}