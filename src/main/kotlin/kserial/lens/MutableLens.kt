/**
 * @author Nikolaus Knop
 */

package kserial.lens

abstract class MutableLens<in R, T, in S : T> : Lens<R, T>() {
    abstract fun set(obj: R, value: S)

    companion object {
        fun <R, T, S : T> of(name: String, getter: (R) -> T, setter: (R, S) -> Unit) = object : MutableLens<R, T, S>() {
            override val name: String = name

            override fun set(obj: R, value: S) {
                setter(obj, value)
            }

            override fun get(obj: R): T = getter(obj)
        }
    }
}