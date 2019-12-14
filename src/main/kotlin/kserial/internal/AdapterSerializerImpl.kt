/**
 *@author Nikolaus Knop
 */

package kserial.internal

import kserial.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class AdapterSerializerImpl(adapterCls: KClass<*>) : InplaceSerializer<Any> {
    private val constr = adapterCls.primaryConstructor ?: error("Adapter class $adapterCls has no primary constructor")

    private val properties =
        adapterCls.memberProperties.filterIsInstance<KMutableProperty1<Any, Any?>>()

    init {
        constr.isAccessible = true
    }

    override fun serialize(obj: Any, output: Output, context: SerialContext) {
        val adapter = constr.call(obj)
        for (prop in properties) {
            val value = prop.get(adapter)
            output.writeObject(value)
        }
    }

    override fun deserialize(obj: Any, input: Input, context: SerialContext) {
        val adapter = constr.call(obj)
        for (prop in properties) {
            val value = input.readObject()
            prop.set(adapter, value)
        }
    }
}