/**
 * @author Nikolaus Knop
 */

package kserial

interface Output : AutoCloseable {
    val context: SerialContext

    fun writeNull()

    fun writeByte(byte: Byte)

    fun writeBoolean(boolean: Boolean)

    fun writeChar(char: Char)

    fun writeShort(short: Short)

    fun writeInt(int: Int)

    fun writeLong(long: Long)

    fun writeFloat(float: Float)

    fun writeDouble(double: Double)

    fun writeString(str: String)

    fun writeObject(obj: Any?, untyped: Boolean = false)
}

