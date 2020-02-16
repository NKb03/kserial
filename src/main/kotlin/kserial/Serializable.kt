/**
 * @author Nikolaus Knop
 */

package kserial

/**
 * An object that can be serialized and deserialized.
 */
interface Serializable {
    /**
     * Write this object to given [output].
     */
    fun serialize(output: Output, context: SerialContext)

    /**
     * Read properties of this object from the given [input].
     */
    fun deserialize(input: Input, context: SerialContext)
}