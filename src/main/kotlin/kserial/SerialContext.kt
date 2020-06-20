/**
 * @author Nikolaus Knop
 */

package kserial

import bundles.Bundle
import bundles.createBundle
import kserial.internal.AdapterSerializerImpl
import kserial.serializers.*
import sun.misc.Unsafe
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

/**
 * The serial context is used to resolve serializers for objects
 */
open class SerialContext private constructor(
    private val useUnsafe: Boolean,
    private val classLoader: ClassLoader,
    private val customSerializers: Map<KClass<*>, Serializer<*>>,
    private val customInPlaceSerializers: Map<KClass<*>, InplaceSerializer<*>>,
    private val customConstructors: Map<KClass<*>, (Bundle, KClass<*>) -> Any>
) {
    private val cachedSerializers = mutableMapOf<KClass<*>, Any>()
    private val cachedConstructors = mutableMapOf<KClass<*>, ((Bundle, KClass<*>) -> Any)?>()

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

    private fun <T : Any> getCustomSerializer(cls: KClass<T>): Any? {
        customSerializers[cls]?.let { return it }
        return getInplaceSerializer(cls)
    }

    private fun <T : Any> getInplaceSerializer(cls: KClass<T>): Any? {
        customInPlaceSerializers[cls]?.let { return it }
        for (superCls in cls.superclasses) {
            getInplaceSerializer(superCls)?.let { return it }
        }
        return null
    }

    private fun dataClassSerializer(cls: KClass<*>): DataClassSerializer<*>? =
        if (!cls.isData) null
        else DataClassSerializer(cls)

    /**
     * Return the appropriate serializer for the given class
     */
    open fun <T : Any> getSerializer(cls: KClass<out T>): Any {
        return cachedSerializers.getOrPut(cls) { createSerializer(cls) }
    }

    private fun getCustomConstructor(cls: KClass<*>): ((Bundle, KClass<*>) -> Any)? {
        customConstructors[cls]?.let { return it }
        for (c in cls.superclasses) {
            getCustomConstructor(c)?.let { return it }
        }
        return null
    }

    /**
     * Return a new instance of the given class either by calling a nullary-constructor
     * or by unsafely allocating a new object if [useUnsafe] is active.
     */
    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> createInstance(cls: KClass<T>, bundle: Bundle = createBundle()): T {
        val constructor = cachedConstructors.getOrPut(cls) {
            val custom = getCustomConstructor(cls)
            val cstr = cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } }
            when {
                custom != null -> custom
                cstr != null   -> { _, _ ->
                    cstr.isAccessible = true
                    cstr.callBy(emptyMap())
                }
                else           -> null
            }
        }
        val obj = if (constructor != null) constructor(bundle, cls) else allocateInstance(cls)
        return obj as T
    }

    private fun allocateInstance(cls: KClass<*>) =
        if (useUnsafe) unsafe.allocateInstance(cls.java)
        else throw SerializationException("Classes with an InplaceSerializer must have a nullary constructor")

    /**
     * Load the class with the given name. By default this just uses the passed [classLoader].
     */
    open fun loadClass(name: String): Class<*> = Class.forName(name, true, classLoader)

    private fun createSerializer(cls: KClass<*>): Any {
        return getCustomSerializer(cls)
            ?: serializableSerializer(cls)
            ?: enumSerializer(cls)
            ?: objectArraySerializer(cls.java)
            ?: findSerializerByAnnotation(cls)
            ?: findAdapterSerializer(cls)
            ?: companionSerializer(cls)
            ?: dataClassSerializer(cls)
            ?: DefaultSerializer
    }

    /**
     * Builder object for serial contexts
     */
    class Builder @PublishedApi internal constructor() {
        /**
         * Controls whether the [Unsafe] object may be used for allocating objects. Defaults to `false`.
         */
        var useUnsafe = false

        /**
         * The class loader used for loading classes. By default this is the context class loader of the current thread.
         */
        var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

        private val customSerializers = mutableMapOf<KClass<*>, Serializer<*>>()

        private val customInPlaceSerializers = mutableMapOf<KClass<*>, InplaceSerializer<*>>()

        private val customConstructors = mutableMapOf<KClass<*>, (Bundle, KClass<*>) -> Any>()

        /**
         * Register the given [serializer] for objects of the given class.
         */
        fun <T : Any> register(cls: KClass<T>, serializer: Serializer<T>) {
            customSerializers[cls] = serializer
        }

        /**
         * Register the given [serializer] for objects of the given class and its subclasses.
         */
        fun <T : Any> register(cls: KClass<T>, serializer: InplaceSerializer<T>) {
            customInPlaceSerializers[cls] = serializer
        }

        /**
         * Register the given [serializer] for objects of the given class.
         */
        inline fun <reified T : Any> register(serializer: Serializer<T>) {
            register(T::class, serializer)
        }

        /**
         * Register the given [serializer] for objects of the given class and its subclasses.
         */
        inline fun <reified T : Any> register(serializer: InplaceSerializer<T>) {
            register(T::class, serializer)
        }

        /**
         * Register the given constructor for the specified and all of its subclasses.
         * The [constructor] takes the class of which an instance is to be created and creates such an instance.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> registerConstructor(cls: KClass<T>, constructor: (Bundle, KClass<out T>) -> T) {
            customConstructors[cls] = constructor as (Bundle, KClass<*>) -> Any
        }

        /**
         * Register the given constructor for the specified and all of its subclasses.
         * The [constructor] takes the class of which an instance is to be created and creates such an instance.
         */
        inline fun <reified T : Any> registerConstructor(noinline constructor: (Bundle, KClass<out T>) -> T) {
            registerConstructor(T::class, constructor)
        }

        init {
            register(BooleanArraySerializer)
            register(ByteArraySerializer)
            register(CharArraySerializer)
            register(ShortArraySerializer)
            register(IntArraySerializer)
            register(LongArraySerializer)
            register(FloatArraySerializer)
            register(DoubleArraySerializer)
            register(DateSerializer)
            register(StringSerializer)
            register(MapSerializer)
        }


        @PublishedApi internal fun build(): SerialContext =
            SerialContext(useUnsafe, classLoader, customSerializers, customInPlaceSerializers, customConstructors)
    }

    companion object {
        private val unsafe by lazy { unsafeField.get(null) as Unsafe }

        private val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").also {
            it.isAccessible = true
        }

        /**
         * Create a new serial context using a [Builder]
         */
        inline fun newInstance(block: Builder.() -> Unit) = Builder().apply(block).build()
    }
}