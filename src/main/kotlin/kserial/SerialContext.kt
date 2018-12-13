/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.internal.DefaultModule
import kserial.serializers.*
import sun.misc.Unsafe
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

class SerialContext(private val modules: Set<SerializationModule>, private val useUnsafe: Boolean) {
    private val cachedSerializers = mutableMapOf<KClass<*>, Any>()

    private fun getCustomizedSerializer(cls: KClass<*>): Any? {
        for (module in modules) {
            val ser = module.getSerializer(cls)
            if (ser != null) return ser
        }
        return null
    }

    private fun findSerializerByAnnotation(cls: KClass<*>): Any? {
        val annotation = cls.findAnnotation<SerializableWith>() ?: return null
        val serializerCls = annotation.serializerCls
        checkSerializerCls(serializerCls, cls)
        return serializerCls.objectInstance ?: serializerCls.createInstance()
    }

    private fun checkSerializerCls(serializerCls: KClass<*>, cls: KClass<*>) {
        if (!isSerializerOk(cls, serializerCls)) {
            throw SerializationException("Serializer class of SerializableWith annotation must be a serializer of type $cls")
        }
    }

    private fun isSerializerOk(
        cls: KClass<*>,
        serializerCls: KClass<*>
    ): Boolean {
        val expectedSerializerType = Serializer::class.createType(
            listOf(KTypeProjection(INVARIANT, cls.starProjectedType))
        )
        return serializerCls.starProjectedType.isSubtypeOf(expectedSerializerType)
    }

    private fun companionSerializer(cls: KClass<*>): Any? {
        val companion = cls.companionObject ?: return null
        return if (isSerializerOk(cls, companion)) {
            val companionObject = companion.objectInstance
            companionObject as Serializer<*>
        } else null
    }

    private fun serializableSerializer(cls: KClass<*>): Any? =
        if (cls.isSubclassOf(Serializable::class)) SerializableSerializer
        else null

    private fun enumSerializer(cls: KClass<*>): Serializer<*>? =
        if (cls.java.isEnum) EnumSerializer else null

    private fun objectArraySerializer(arrCls: Class<*>): Serializer<*>? {
        return when {
            !arrCls.isArray                  -> null
            arrCls.componentType.isPrimitive -> null
            else                             -> ObjectArraySerializer
        }
    }

    private fun dataClassSerializer(cls: KClass<*>): DataClassSerializer<*>? =
        if (!cls.isData) null
        else DataClassSerializer(cls)

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> getSerializer(cls: KClass<out T>): Any {
        return cachedSerializers.getOrPut(cls) { createSerializer(cls) }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> createInstance(cls: KClass<T>): T {
        val nullary = cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } }
        return if (nullary != null) {
            nullary.isAccessible = true
            nullary.callBy(emptyMap())
        } else {
            if (useUnsafe) {
                unsafe.allocateInstance(cls.java) as T
            } else {
                throw SerializationException("Classes implementing InplaceSerializer must have a nullary constructor")
            }
        }
    }

    private fun createSerializer(cls: KClass<*>): Any {
        return getCustomizedSerializer(cls)
            ?: serializableSerializer(cls)
            ?: enumSerializer(cls)
            ?: objectArraySerializer(cls.java)
            ?: findSerializerByAnnotation(cls)
            ?: companionSerializer(cls)
            ?: dataClassSerializer(cls)
            ?: DefaultSerializer
    }

    class Builder @PublishedApi internal constructor() {
        private val modules = mutableSetOf<SerializationModule>()

        var useUnsafe = false

        init {
            install(DefaultModule)
        }


        fun install(module: SerializationModule) {
            modules.add(module)
        }

        @PublishedApi internal fun build(): SerialContext = SerialContext(modules, useUnsafe)
    }

    companion object {
        private val unsafe by lazy { unsafeField.get(null) as Unsafe }

        private val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").also {
            it.isAccessible = true
        }

        inline fun newInstance(block: Builder.() -> Unit) = Builder().apply(block).build()
    }
}