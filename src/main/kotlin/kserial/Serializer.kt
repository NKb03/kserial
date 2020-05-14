/**
 * @author Nikolaus Knop
 */

package kserial

/**
 * A serializer is used to serialize and deserialize objects.
 */
interface Serializer<T : Any> {
    /**
     * Write the given [obj] to the specified [output].
     */
    fun serialize(obj: T, output: Output)

    /**
     * Read an object of the given [cls] from the specified [input].
     */
    fun deserialize(cls: Class<T>, input: Input): T
}