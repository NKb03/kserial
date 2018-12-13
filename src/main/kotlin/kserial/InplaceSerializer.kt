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
     * @param context the [SerialContext] used to instantiate serializers for nested objects
     */
    fun serialize(obj: T, output: Output, context: SerialContext)

    /**
     * Deserialize the given [obj] reading from [input]
     * @param context the [SerialContext] used to instantiate serializers for nested objects
     */
    fun deserialize(obj: T, input: Input, context: SerialContext)
}