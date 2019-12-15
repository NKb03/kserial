/**
 *@author Nikolaus Knop
 */

package kserial

interface CompoundSerializable : Serializable {
    fun components(): Sequence<Any>

    override fun serialize(output: Output, context: SerialContext) {
        for (comp in components()) {
            output.writeUntyped(comp)
        }
    }

    override fun deserialize(input: Input, context: SerialContext) {
        for (comp in components()) {
            input.readInplace(comp)
        }
    }
}