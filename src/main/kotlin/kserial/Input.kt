/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package kserial

import kotlin.reflect.KClass

interface Input : AutoCloseable {
    fun readByte(): Byte

    fun readBoolean(): Boolean

    fun readShort(): Short

    fun readInt(): Int

    fun readLong(): Long

    fun readFloat(): Float

    fun readDouble(): Double

    fun readString(): String

    fun readObject(context: SerialContext): Any?

    fun readObject(cls: KClass<*>, context: SerialContext): Any?
}