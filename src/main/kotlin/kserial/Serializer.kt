/**
 * @author Nikolaus Knop
 */

package kserial

interface Serializer<T : Any> {
    fun serialize(obj: T, output: Output, context: SerialContext)

    fun deserialize(cls: Class<T>, input: Input, context: SerialContext): T
}