/**
 *@author Nikolaus Knop
 */

package kserial.internal

import kserial.*

internal sealed class BoxedPrimitiveSerializer<T : Any>(
    private val write: (Output, T) -> Unit,
    private val read: (Input) -> T
) : Serializer<T> {
    override fun serialize(obj: T, output: Output, context: SerialContext) {
        write(output, obj)
    }

    override fun deserialize(cls: Class<T>, input: Input, context: SerialContext): T = read(input)

    object BYTE : BoxedPrimitiveSerializer<Byte>(Output::writeByte, Input::readByte)
    object BOOLEAN : BoxedPrimitiveSerializer<Boolean>(Output::writeBoolean, Input::readBoolean)
    object SHORT : BoxedPrimitiveSerializer<Short>(Output::writeShort, Input::readShort)
    object INT : BoxedPrimitiveSerializer<Int>(Output::writeInt, Input::readInt)
    object LONG : BoxedPrimitiveSerializer<Long>(Output::writeLong, Input::readLong)
    object FLOAT : BoxedPrimitiveSerializer<Float>(Output::writeFloat, Input::readFloat)
    object DOUBLE : BoxedPrimitiveSerializer<Double>(Output::writeDouble, Input::readDouble)
}