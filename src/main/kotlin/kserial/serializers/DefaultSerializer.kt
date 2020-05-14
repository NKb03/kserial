package kserial.serializers

import kserial.*
import kserial.internal.Impl.readField
import kserial.internal.Impl.writeField
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object DefaultSerializer : InplaceSerializer<Any> {
    override fun serialize(obj: Any, output: Output) {
        val cls = obj.javaClass
        writeFields(cls, obj, output)
    }

    private fun writeFields(cls: Class<Any>, obj: Any, output: Output) {
        val fields = getFields(cls)
        for (f in fields) {
            writeField(f, obj, output)
        }
    }

    private fun getFields(cls: Class<Any>): List<Field> {
        val fields = mutableListOf<Field>()
        var c: Class<*>? = cls
        while (c != null) {
            for (f in c.declaredFields) {
                if (!Modifier.isStatic(f.modifiers)) {
                    fields.add(f)
                }
            }
            c = c.superclass
        }
        return fields
    }

    override fun deserialize(obj: Any, input: Input) {
        val cls = obj.javaClass
        val fields = getFields(cls)
        for (f in fields) {
            readField(f, input, obj)
        }
    }
}