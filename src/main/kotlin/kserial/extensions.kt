/**
 * @author Nikolaus Knop
 */

@file:JvmName("KSerialKt")

package kserial

import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

/**
 * Create an [Input] that reads from the specified [file]
 */
fun KSerial.createInput(file: File) =
    createInput(FileInputStream(file))

/**
 * Create an [Output] that writes to the specified [file]
 */
fun KSerial.createOutput(file: File) =
    createOutput(FileOutputStream(file))

/**
 * Create an [Input] that reads from the specified [path]
 */
fun KSerial.createInput(path: Path) =
    createInput(Files.newInputStream(path))

/**
 * Create an [Output] that writes to the specified [path]
 */
fun KSerial.createOutput(path: Path) =
    createOutput(Files.newOutputStream(path))

/**
 * Create an [Input] that reads from the specified [url]
 */
fun KSerial.createInput(url: URL) = createInput(url.openStream())

/**
 * Create an [Output] that writes to the specified [url]
 */
fun KSerial.createOutput(url: URL) =
    createOutput(url.openConnection().getOutputStream())

/**
 * Create an [Input] that reads from the specified byte-[arr]
 */
fun KSerial.createInput(arr: ByteArray) = createInput(ByteArrayInputStream(arr))

/**
 * Read a "typed-written" object and safely cast it to [T]
 */
inline fun <reified T : Any> Input.readTyped(context: SerialContext): T? {
    val obj = readObject(context)
    return obj as T?
}

/**
 * Read a "untyped-written" object of type [T]
 */
inline fun <reified T : Any> Input.readUntyped(context: SerialContext): T? = readObject(T::class.java, context)