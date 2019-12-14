/**
 *@author Nikolaus Knop
 */

package kserial

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

abstract class SerializerAdapter<out T : Any>(val target: T)

operator fun <T> KProperty0<T>.getValue(receiver: Any?, property: KProperty<*>) = get()

operator fun <T> KMutableProperty0<T>.setValue(receiver: Any?, property: KProperty<*>, value: T) = set(value)

@Suppress("UNCHECKED_CAST")
class ByNameDelegation<T : Any, R> {
    operator fun provideDelegate(
        receiver: SerializerAdapter<T>,
        property: KProperty<*>
    ): ReadWriteProperty<SerializerAdapter<T>, R> {
        val cls = receiver.target::class
        val targetProp = cls.memberProperties.find { it.name == property.name }
        require(targetProp != null) { "No property with name ${property.name} in $cls" }
        require(targetProp is KMutableProperty1) { "Target property $targetProp is not mutable" }
        return Delegate(targetProp as KMutableProperty1<T, R>, receiver.target)
    }

    private class Delegate<T : Any, R>(private val targetProp: KMutableProperty1<T, R>, private val target: T) :
        ReadWriteProperty<SerializerAdapter<T>, R> {
        override fun getValue(thisRef: SerializerAdapter<T>, property: KProperty<*>): R = targetProp.get(target)

        override fun setValue(thisRef: SerializerAdapter<T>, property: KProperty<*>, value: R) {
            targetProp.set(target, value)
        }
    }
}

fun <T : Any, R> named() = ByNameDelegation<T, R>()