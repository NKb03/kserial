/**
 *@author Nikolaus Knop
 */

package kserial.lens

/**
 * A Lens that maps objects of type [R] to objects of type [T]
 */
abstract class Lens<in R, out T> {
    /**
     * The name of this lens
     */
    abstract val name: String

    /**
     * Gets the value of this lens from the specified [obj]
     */
    abstract fun get(obj: R): T

    /**
     * @return the name of this Lens
     */
    override fun toString(): String = "Lens: $name"

    companion object {
        /**
         * Create a [Lens] with the specified [name] which maps objects with the specified [getter]
         */
        fun <R, T> of(name: String, getter: (R) -> T) = object : Lens<R, T>() {
            override val name: String = name

            override fun get(obj: R): T = getter(obj)
        }
    }
}