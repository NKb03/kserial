/**
 *@author Nikolaus Knop
 */

package kserial.lens

abstract class Lens<in R, out T> {
    abstract val name: String

    abstract fun get(obj: R): T

    override fun toString(): String = "Lens: $name"

    companion object {
        fun <R, T> of(name: String, getter: (R) -> T) = object : Lens<R, T>() {
            override val name: String = name

            override fun get(obj: R): T = getter(obj)
        }
    }
}