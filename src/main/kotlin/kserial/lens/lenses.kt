/**
 * @author Nikolaus Knop
 */

package kserial.lens

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun <R, T> KProperty1<R, T>.lens() = object : MutableLens<R, T, T>() {
    init {
        check(javaField != null)
    }

    override val name: String
        get() = this@lens.name

    override fun get(obj: R): T = this@lens.get(obj)

    override fun set(obj: R, value: T) {
        javaField!!.set(obj, value)
    }
}

fun <R, T> KMutableProperty1<R, T>.lens(): MutableLens<R, T, T> = MutableLens.of(name, getter, setter)

infix fun <R, T, V> Lens<R, T>.then(next: Lens<T, V>): Lens<R, V> =
    Lens.of("$name.${next.name}") { r -> next.get(get(r)) }

infix fun <R, T, V> Lens<R, T>.then(next: MutableLens<T, V, V>) = object : MutableLens<R, V, V>() {
    override val name: String = "${this@then.name}.${next.name}"

    override fun set(obj: R, value: V) {
        next.set(this@then.get(obj), value)
    }

    override fun get(obj: R): V = next.get(this@then.get(obj))
}

fun <R, T : Any, V> Lens<R, T?>.safe(next: Lens<T, V>): Lens<R, V?> = Lens.of("$name?.${next.name}") { r ->
    val o = get(r) ?: return@of null
    next.get(o)
}

fun <R, T : Any, V> Lens<R, T?>.safe(next: MutableLens<T, V, V>) = object : MutableLens<R, V?, V>() {
    override val name: String = "${this@safe.name}?.${next.name}"

    override fun get(obj: R): V? {
        val o = this@safe.get(obj) ?: return null
        return next.get(o)
    }

    override fun set(obj: R, value: V) {
        val o = this@safe.get(obj) ?: return
        next.set(o, value)
    }
}