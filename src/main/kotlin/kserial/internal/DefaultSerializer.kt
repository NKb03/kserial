@file:Suppress("UNCHECKED_CAST")

package kserial.internal

import kserial.*
import kserial.internal.Impl.readField
import kserial.internal.Impl.writeField
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object DefaultSerializer : InplaceSerializer<Any> {
    override fun serialize(obj: Any, output: Output, context: SerialContext) {
        val cls = obj.javaClass
        writeFields(context, cls, obj, output)
    }

    private fun writeFields(context: SerialContext, cls: Class<Any>, obj: Any, output: Output) {
        val fields = getFields(cls)
        for (f in fields) {
            writeField(f, obj, output, context)
        }
    }

    private fun getFields(cls: Class<Any>): List<Field> =
        cls.declaredFields
            .filter { f ->
                !Modifier.isStatic(f.modifiers) && !f.isAnnotationPresent(KTransient::class.java)
            }

    override fun deserialize(obj: Any, input: Input, context: SerialContext) {
        val cls = obj.javaClass
        val fields = getFields(cls)
        for (f in fields) {
            readField(f, input, context, obj)
        }
    }
}
