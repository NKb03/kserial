/**
 *@author Nikolaus Knop
 */

package kserial

/**
 * A Serializer that deserializes objects of type [T] by using the nullary constructor and then applying [deserialize]
 */
interface InplaceSerializer<in T : Any> {
    /**
     * Serialize the given [obj] writing to the [output]
     */
    fun serialize(obj: T, output: Output)

    /**
     * Deserialize the given [obj] reading from [input]
     */
    fun deserialize(obj: T, input: Input)
}