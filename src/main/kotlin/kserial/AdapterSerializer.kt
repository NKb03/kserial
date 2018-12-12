/**
 *@author Nikolaus Knop
 */

package kserial

import kserial.internal.forceClsType
import kserial.internal.isTypeFinal
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

abstract class AdapterSerializer<in T : Any>(private val type: KType) : InplaceSerializer<T> {
    constructor(cls: KClass<T>) : this(cls.starProjectedType)

    override fun serialize(obj: T, output: Output, context: SerialContext) {
        for (p in adapterProperties) {
            val v = p.get(this, obj)
            output.writeObject(v, context, p.isTypeFinal)
        }
    }

    private val adapterProperties
            : Collection<KMutableProperty2<AdapterSerializer<T>, T, Any?>>
            by lazy { findAdapterProperties() }

    private fun findAdapterProperties(): Collection<KMutableProperty2<AdapterSerializer<T>, T, Any?>> {
        val allExtensionProperties = this::class.memberExtensionProperties
        return allExtensionProperties.asSequence()
            .sortedBy { it.name }
            .filterIsInstance<KMutableProperty2<AdapterSerializer<T>, T, Any?>>()
            .filter { it.findAnnotation<KTransient>() == null }
            .filter { it.extensionReceiverParameter!!.type.isSubtypeOf(type) }
            .toList().also { props ->
                props.forEach { p ->
                    p.isAccessible = true
                }
            }
    }

    override fun deserialize(obj: T, input: Input, context: SerialContext) {
        for (p in adapterProperties) {
            val v =
                if (p.isTypeFinal) input.readObject(p.forceClsType(), context)
                else input.readObject(context)
            p.set(this, obj, v)
        }
    }
}