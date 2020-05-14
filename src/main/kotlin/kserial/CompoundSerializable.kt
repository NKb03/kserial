/**
 *@author Nikolaus Knop
 */

package kserial

interface CompoundSerializable : Serializable {
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