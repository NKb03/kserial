/**
 * @author Nikolaus Knop
 */

@file:JvmName("KSerialKt")

package kserial

import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

fun KSerial.createInput(file: File) =
    createInput(FileInputStream(file))

fun KSerial.createOutput(file: File) =
    createOutput(FileOutputStream(file))

fun KSerial.createInput(path: Path) =
    createInput(Files.newInputStream(path))

fun KSerial.createOutput(path: Path) =
    createOutput(Files.newOutputStream(path))

fun KSerial.createInput(url: URL) = createInput(url.openStream())

fun KSerial.createOutput(url: URL) =
    createOutput(url.openConnection().getOutputStream())

fun KSerial.createInput(arr: ByteArray) = createInput(ByteArrayInputStream(arr))
inline fun <reified T : Any> Input.readTyped(context: SerialContext): T? {
    val obj = readObject(context)
    return obj as T?
}

inline fun <reified T : Any> Input.readUntyped(context: SerialContext): T? = readObject(T::class.java, context)