/**
 *@author Nikolaus Knop
 */

package kserial

/**
 * An [InplaceSerializer] that serializes and deserializes objects *component-wise*
 */
abstract class CompoundSerializer<T : Any> : InplaceSerializer<T> {
    /**
     * Return a sequence of components that form the given object.
     */
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