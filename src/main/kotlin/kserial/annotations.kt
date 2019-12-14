/**
 * @author Nikolaus Knop
 */

package kserial

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Target(FIELD)
/**
 * Annotation usable on fields, preventing the default serializer from serializing them
 */
annotation class KTransient

annotation class SerializableWith(val serializerCls: KClass<*>)

@Target(CLASS)
annotation class UseAdapter(val adapterCls: KClass<*>)
