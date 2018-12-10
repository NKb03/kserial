/**
 *@author Nikolaus Knop
 */

package kserial

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

open class SerializationModule {
    private val customSerializers = mutableMapOf<KClass<*>, Serializer<*>>()

    private val customInPlaceSerializers = mutableMapOf<KClass<*>, InplaceSerializer<*>>()

    fun <T : Any> register(cls: KClass<T>, serializer: Serializer<T>) {
        customSerializers[cls] = serializer
    }

    fun <T : Any> register(cls: KClass<T>, serializer: InplaceSerializer<T>) {
        customInPlaceSerializers[cls] = serializer
    }

    internal fun <T : Any> getSerializer(cls: KClass<T>): Any? {
        customSerializers[cls]?.let { return it }
        return getInplaceSerializer(cls)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getInplaceSerializer(cls: KClass<T>): Any? {
        val ser = customInPlaceSerializers[cls]
        if (ser != null) return ser
        else {
            for (superCls in cls.superclasses) {
                val superClsSerializer = getInplaceSerializer(superCls)
                if (superClsSerializer != null) return superClsSerializer as InplaceSerializer<T>
            }
        }
        return null
    }

    inline fun <reified T : Any> register(serializer: Serializer<T>) {
        register(T::class, serializer)
    }

    inline fun <reified T : Any> register(serializer: InplaceSerializer<T>) {
        register(T::class, serializer)
    }
}

