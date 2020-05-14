/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.internal.AdapterSerializerImpl
import kserial.internal.DefaultModule
import kserial.serializers.*
import sun.misc.Unsafe
import kotlin.reflect.*
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

open class SerialContext(
    private val modules: Set<SerializationModule> = setOf(DefaultModule),
    private val useUnsafe: Boolean = false,
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) {
    private val cachedSerializers = mutableMapOf<KClass<*>, Any>()
    private val cachedConstructors = mutableMapOf<KClass<*>, KFunction<*>?>()

    private fun getCustomizedSerializer(cls: KClass<*>): Any? {
        for (module in modules) {
            val ser = module.getSerializer(cls)
            if (ser != null) return ser
        }
        return null
    }

    private inline fun <reified A : Annotation> findAnnotationInHierarchy(cls: KClass<*>): A? {
        cls.findAnnotation<A>()?.let { return it }
        for (st in cls.allSuperclasses) {
            cls.findAnnotation<A>()?.let { return it }
        }
        return null
    }

    private fun findSerializerByAnnotation(cls: KClass<*>): Any? {
        val annotation = findAnnotationInHierarchy<SerializableWith>(cls) ?: return null
        val serializerCls = annotation.serializerCls
        checkSerializerCls(serializerCls, cls)
        return serializerCls.objectInstance ?: serializerCls.createInstance()
    }

    private fun findAdapterSerializer(cls: KClass<*>): Any? {
        val ann = findAnnotationInHierarchy<UseAdapter>(cls) ?: return null
        val adapter = ann.adapterCls
        return AdapterSerializerImpl(adapter)
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
    open fun <T : Any> getSerializer(cls: KClass<out T>): Any {
        return cachedSerializers.getOrPut(cls) { createSerializer(cls) }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> createInstance(cls: KClass<T>): T {
        val nullary = cachedConstructors.getOrPut(cls) {
            cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } }
        }
        return if (nullary != null) {
            nullary.isAccessible = true
            nullary.callBy(emptyMap()) as T
        } else {
            if (useUnsafe) {
                unsafe.allocateInstance(cls.java) as T
            } else {
                throw SerializationException("Classes with an InplaceSerializer must have a nullary constructor")
            }
        }
    }

    open fun loadClass(name: String): Class<*> = Class.forName(name, true, classLoader)

    private fun createSerializer(cls: KClass<*>): Any {
        return getCustomizedSerializer(cls)
            ?: serializableSerializer(cls)
            ?: enumSerializer(cls)
            ?: objectArraySerializer(cls.java)
            ?: findSerializerByAnnotation(cls)
            ?: findAdapterSerializer(cls)
            ?: companionSerializer(cls)
            ?: dataClassSerializer(cls)
            ?: DefaultSerializer
    }

    class Builder @PublishedApi internal constructor() {
        private val modules = mutableSetOf<SerializationModule>()

        var useUnsafe = false

        var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

        init {
            install(DefaultModule)
        }


        fun install(module: SerializationModule) {
            modules.add(module)
        }

        @PublishedApi internal fun build(): SerialContext = SerialContext(modules, useUnsafe, classLoader)
    }

    companion object {
        val unsafe by lazy { unsafeField.get(null) as Unsafe }

        private val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").also {
            it.isAccessible = true
        }

        inline fun newInstance(block: Builder.() -> Unit) = Builder().apply(block).build()
    }
}