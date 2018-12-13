package kserial.serializers

import kserial.*

internal object EnumSerializer: Serializer<Enum<*>> {
    override fun serialize(obj: Enum<*>, output: Output, context: SerialContext) {
        output.writeInt(obj.ordinal)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(cls: Class<Enum<*>>, input: Input, context: SerialContext): Enum<*> {
        val values = cls.getDeclaredMethod("values").invoke(null) as Array<Enum<*>>
        val ordinal = input.readInt()
        return values[ordinal]
    }
}