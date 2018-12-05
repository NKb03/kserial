package kserial.internal

import kserial.*

internal object StringSerializer : Serializer<String> {
    override fun serialize(obj: String, output: Output, context: SerialContext) {
        output.writeString(obj)
    }

    override fun deserialize(cls: Class<String>, input: Input, context: SerialContext): String =
        input.readString()
}