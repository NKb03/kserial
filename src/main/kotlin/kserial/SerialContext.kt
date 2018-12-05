/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.internal.*
import sun.misc.Unsafe
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.full.*

class SerialContext {
    var useUnsafe = false

    private val modules = mutableSetOf<SerializationModule>()

    private val cachedSerializers = mutableMapOf<KClass<*>, Any>()

    init {
        install(DefaultModule)
    }

    private fun <T : Any> getCustomizedSerializer(cls: KClass<out T>): Any? {
        for (module in modules) {
            val ser = module.getSerializer(cls)
            if (ser != null) return ser
        }
        return null
    }

    private fun <T : Any> findSerializerByAnnotation(cls: KClass<out T>): Serializer<*>? {
        val annotation = cls.findAnnotation<SerializableWith>() ?: return null
        val serializerCls = annotation.serializerCls
        checkSerializerCls(serializerCls, cls)
        val instance = serializerCls.objectInstance ?: serializerCls.createInstance()
        return instance as Serializer<*>
    }

    private fun <T : Any> checkSerializerCls(serializerCls: KClass<*>, cls: KClass<out T>) {
        if (!isSerializerOk(cls, serializerCls)) {
            throw SerializationException("Serializer class of SerializableWith annotation must be a serializer of type $cls")
        }
    }

    private fun <T : Any> isSerializerOk(
        cls: KClass<out T>,
        serializerCls: KClass<*>
    ): Boolean {
        val expectedSerializerType = Serializer::class.createType(
            listOf(KTypeProjection(INVARIANT, cls.starProjectedType))
        )
        return serializerCls.starProjectedType.isSubtypeOf(expectedSerializerType)
    }

    private fun <T : Any> companionSerializer(cls: KClass<out T>): Any? {
        val companion = cls.companionObject ?: return null
        return if (isSerializerOk(cls, companion)) {
            val companionObject = companion.objectInstance
            companionObject as Serializer<*>
        } else null
    }

    private fun <T : Any> serializableSerializer(cls: KClass<out T>): Any? =
        if (cls.isSubclassOf(Serializable::class)) SerializableSerializer
        else null

    private fun <T : Any> enumSerializer(cls: KClass<out T>): Serializer<*>? =
        if (cls.java.isEnum) EnumSerializer else null

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> getSerializer(cls: KClass<out T>): Any {
        return cachedSerializers.getOrPut(cls) { createSerializer(cls) }
    }

    internal fun <T : Any> createInstance(cls: Class<T>): T {
        val nullaryConstructor = try {
            cls.getDeclaredConstructor().also { it.isAccessible = true }
        } catch (noNullaryConstructor: NoSuchMethodException) {
            @Suppress("UNCHECKED_CAST")
            if (useUnsafe) {
                return unsafe.allocateInstance(cls) as T
            } else {
                throw SerializationException("Classes implementing InplaceSerializer must have a nullary constructor")
            }
        }
        return nullaryConstructor.newInstance()
    }

    private fun <T : Any> createSerializer(cls: KClass<out T>): Any {
        return serializableSerializer(cls)
            ?: enumSerializer(cls)
            ?: objectArraySerializer(cls.java)
            ?: findSerializerByAnnotation(cls)
            ?: companionSerializer(cls)
            ?: getCustomizedSerializer(cls)
            ?: DefaultSerializer
    }

    private fun <T : Any> objectArraySerializer(arrCls: Class<out T>): Serializer<*>? {
        return when {
            !arrCls.isArray                  -> null
            arrCls.componentType.isPrimitive -> null
            else                             -> ObjectArraySerializer
        }
    }

    fun install(module: SerializationModule) {
        modules.add(module)
    }

    companion object {
        private val unsafe by lazy { unsafeField.get(null) as Unsafe }

        private val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").also {
            it.isAccessible = true
        }
    }
}