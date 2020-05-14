/**
 *@author Nikolaus Knop
 */

package kserial

/**
 * A [Serializable] that is serialized and deserialized *component-wise*.
 */
interface CompoundSerializable : Serializable {
    /**
     * Return the sequence of components that form this object
     */
    fun components(): Sequence<Any>

    override fun serialize(output: Output) {
        for (comp in components()) {
            output.writeUntyped(comp)
        }
    }

    override fun deserialize(input: Input) {
        for (comp in components()) {
            input.readInplace(comp)
        }
    }
}