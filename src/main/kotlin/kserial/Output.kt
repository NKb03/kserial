/**
 * @author Nikolaus Knop
 */

package kserial

interface Output: AutoCloseable {
    fun writeNull()

    fun writeClass(cls: Class<*>)

    fun writeByte(byte: Byte)

    fun writeBoolean(boolean: Boolean)

    fun writeShort(short: Short)

    fun writeInt(int: Int)

    fun writeLong(long: Long)

    fun writeFloat(float: Float)

    fun writeDouble(double: Double)

    fun writeString(str: String)

    fun writeObject(obj: Any?, context: SerialContext, typed: Boolean = true)
}

