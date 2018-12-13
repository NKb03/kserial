/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

/**
 * This interface implements and input for the [SFTS serialization format](file://D:\Bibliotheken\Aktive Projekte\kserial\docs\sfts.html)
 */
interface Input : AutoCloseable {
    /**
     * Read a byte, that was written with [Output.writeByte]
     */
    fun readByte(): Byte

    /**
     * Read a boolean
     */
    fun readBoolean(): Boolean

    fun readChar(): Char

    fun readShort(): Short

    fun readInt(): Int

    fun readLong(): Long

    fun readFloat(): Float

    fun readDouble(): Double

    fun readString(): String

    fun readObject(context: SerialContext): Any?

    fun <T : Any> readObject(cls: Class<T>, context: SerialContext): T?
}