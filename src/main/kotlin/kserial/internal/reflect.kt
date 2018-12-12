package kserial.internal

import kotlin.reflect.*

internal val KClass<*>.isPrimitive get() = javaPrimitiveType != null

internal val Any.isPrimitiveValue get() = this::class.isPrimitive


internal fun KProperty<*>.forceClsType() =
    (returnType.classifier as KClass<*>).java

internal val KProperty<*>.isTypeFinal
    get() = (returnType.classifier as? KClass<*>)?.isFinal == true

internal val KParameter.hasFinalType: Boolean
    get() {
        val cls = type.classifier as? KClass<*> ?: return false
        return cls.isFinal
    }