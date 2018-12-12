/**
 *@author Nikolaus Knop
 */

package kserial.internal

import kserial.*
import kserial.internal.Impl.writeField
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

internal class DataClassSerializer<T : Any>(private val cls: KClass<T>) : Serializer<T> {
    init {
        require(cls.isData)
    }

    private val primaryConstructor = cls.primaryConstructor!!

    private val parameters = primaryConstructor.parameters

    private val dataFields = parameters.map { param ->
        val propName = param.name!!
        val prop = cls.declaredMemberProperties.find { it.name == propName }
            ?: throw AssertionError("No associated field for data class parameter found")
        prop.javaField ?: throw AssertionError("No javaField for data class property found")
    }

    override fun serialize(obj: T, output: Output, context: SerialContext) {
        for (field in dataFields) {
            writeField(field, obj, output, context)
        }
    }

    override fun deserialize(cls: Class<T>, input: Input, context: SerialContext): T {
        val args = parameters.associate { param ->
            val v = if (param.hasFinalType) {
                val type = param.type.classifier as KClass<*>
                input.readObject(type.java, context)
            } else {
                input.readObject(context)
            }
            param to v
        }
        primaryConstructor.isAccessible = true
        return primaryConstructor.callBy(args)
    }
}