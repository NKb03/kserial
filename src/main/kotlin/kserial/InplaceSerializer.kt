/**
 *@author Nikolaus Knop
 */

package kserial

interface InplaceSerializer<in T : Any> {
    fun serialize(obj: T, output: Output, context: SerialContext)

    fun deserialize(obj: T, input: Input, context: SerialContext)
}