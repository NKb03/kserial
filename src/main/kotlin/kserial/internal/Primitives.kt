package kserial.internal

import kotlin.reflect.KClass

val KClass<*>.isPrimitive get() = javaPrimitiveType != null

val Any.isPrimitive get() = this::class.isPrimitive