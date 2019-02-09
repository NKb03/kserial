/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

/**
 * This interface implements and input for the [SFTS serialization format](file://D:\Bibliotheken\Aktive Projekte\kserial\docs\sfts.html)
 * * Unless otherwise noted all functions of this interface can throw a [SerializationException]
 * * All [java.io.IOException]'s are wrapped in a [SerializationException] before being rethrown
 */
interface Input : AutoCloseable {
    /**
     * Read a byte, that was written with [Output.writeByte]
     */
    fun readByte(): Byte

    /**
     * Read a boolean, that was written with [Output.writeBoolean]
     */
    fun readBoolean(): Boolean

    /**
     * Read a char, that was written with [Output.writeChar]
     */
    fun readChar(): Char

    /**
     * Read a short, that was written with [Output.writeShort]
     */
    fun readShort(): Short

    /**
     * Read a int, that was written with [Output.writeInt]
     */
    fun readInt(): Int

    /**
     * Read a long, that was written with [Output.writeLong]
     */
    fun readLong(): Long

    /**
     * Read a float, that was written with [Output.writeFloat]
     */
    fun readFloat(): Float

    /**
     * Read a double, that was written with [Output.writeDouble]
     */
    fun readDouble(): Double

    /**
     * Read a [String] that was written with [Output.writeString]
     */
    fun readString(): String

    /**
     * Read an object that was written **typed** with [Output.writeObject]
     * * If `null` was written this function returns `null`
     * @param context the [SerialContext] that is used to instantiate serializers
     * @throws SerializationException if an untyped object was written
     */
    fun readObject(): Any?

    /**
     * Read an object that was written **untyped** with [Output.writeObject]
     * * If `null` was written this function returns `null`
     * @param context the [SerialContext] that is used to instantiate serializers
     */
    fun <T : Any> readObject(cls: Class<T>): T?
}