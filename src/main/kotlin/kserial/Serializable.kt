/**
 * @author Nikolaus Knop
 */

package kserial

interface Serializable {
    fun serialize(output: Output, context: SerialContext)

    fun deserialize(input: Input, context: SerialContext)
}