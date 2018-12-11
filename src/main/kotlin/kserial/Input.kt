/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

interface Input : AutoCloseable {
    fun readByte(): Byte

    fun readBoolean(): Boolean

    fun readChar(): Char

    fun readShort(): Short

    fun readInt(): Int

    fun readLong(): Long

    fun readFloat(): Float

    fun readDouble(): Double

    fun readString(): String

    fun readObject(context: SerialContext): Any?

    fun readObject(cls: Class<*>, context: SerialContext): Any?
}