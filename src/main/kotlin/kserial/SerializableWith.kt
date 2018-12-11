/**
 *@author Nikolaus Knop
 */

package kserial

import kotlin.reflect.KClass

annotation class SerializableWith(val serializerCls: KClass<Any>)