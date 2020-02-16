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
fun KSerial.createInput(file: File, context: SerialContext) =
    createInput(FileInputStream(file), context)

/**
 * Create an [Output] that writes to the specified [file]
 */
fun KSerial.createOutput(file: File, context: SerialContext) =
    createOutput(FileOutputStream(file), context)

/**
 * Create an [Input] that reads from the specified [path]
 */
fun KSerial.createInput(path: Path, context: SerialContext) =
    createInput(Files.newInputStream(path), context)

/**
 * Create an [Output] that writes to the specified [path]
 */
fun KSerial.createOutput(path: Path, context: SerialContext) =
    createOutput(Files.newOutputStream(path), context)

/**
 * Create an [Input] that reads from the specified [url]
 */
fun KSerial.createInput(url: URL, context: SerialContext) = createInput(url.openStream(), context)

/**
 * Create an [Output] that writes to the specified [url]
 */
fun KSerial.createOutput(url: URL, context: SerialContext) =
    createOutput(url.openConnection().getOutputStream(), context)

/**
 * Create an [Input] that reads from the specified byte-[arr]
 */
fun KSerial.createInput(arr: ByteArray, context: SerialContext) = createInput(ByteArrayInputStream(arr), context)

/**
 * Read a "typed-written" object and safely cast it to [T]
 */
inline fun <reified T> Input.readTyped(): T {
    val obj = readObject()
    return obj as T
}

/**
 * Read a "untyped-written" object of type [T]
 */
inline fun <reified T : Any> Input.readUntyped(): T? = readObject(T::class.java)

inline fun <reified T> readObject(path: Path): T {
    val serial = KSerial.newInstance()
    val context = SerialContext.newInstance {
        classLoader = T::class.java.classLoader
        useUnsafe = true
    }
    val input = serial.createInput(path, context)
    return input.readObject() as T
}

inline fun <reified T> writeObject(path: Path, obj: T) {
    val serial = KSerial.newInstance()
    val context = SerialContext.newInstance {
        classLoader = T::class.java.classLoader
        useUnsafe = true
    }
    val output = serial.createOutput(path, context)
    output.writeObject(obj)
}