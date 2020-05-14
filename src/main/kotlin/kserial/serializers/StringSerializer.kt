/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.*

object StringSerializer : Serializer<String> {
    override fun serialize(obj: String, output: Output) {
        output.writeString(obj)
    }

    override fun deserialize(cls: Class<String>, input: Input): String = input.readString()
}