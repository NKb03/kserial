/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.SerializationOption.ShareClassNames
import kserial.SharingMode.Unshared
import kserial.internal.BinaryInput
import kserial.internal.BinaryOutput
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

interface IOFactory {
    fun createInput(stream: InputStream): Input

    fun createOutput(stream: OutputStream): Output

    private class Binary(options: Array<out SerializationOption>) : IOFactory {
        private val shareClsNames = isShareClassNames(options)

        private val sharingMode = getSharingMode(options)

        override fun createInput(stream: InputStream): Input = BinaryInput.fromStream(stream)

        override fun createOutput(stream: OutputStream): Output {
            return BinaryOutput(DataOutputStream(stream), sharingMode, shareClsNames)
        }

        override fun toString(): String = "Binary: shareClsNames: $shareClsNames $sharingMode"
    }

    companion object {
        private fun getSharingMode(options: Array<out SerializationOption>): SharingMode {
            val modes = options.filterIsInstance<SerializationOption.Sharing>()
            require(modes.size <= 1) { "multiple sharing modes supplied" }
            return modes.firstOrNull()?.mode ?: Unshared
        }

        private fun isShareClassNames(options: Array<out SerializationOption>): Boolean = ShareClassNames in options

        fun binary(vararg options: SerializationOption): IOFactory = Binary(options)
    }
}

fun IOFactory.createInput(file: File) =
    createInput(FileInputStream(file))

fun IOFactory.createOutput(file: File) =
    createOutput(FileOutputStream(file))

fun IOFactory.createInput(path: Path) =
    createInput(Files.newInputStream(path))

fun IOFactory.createOutput(path: Path) =
    createOutput(Files.newOutputStream(path))

fun IOFactory.createInput(url: URL) = createInput(url.openStream())

fun IOFactory.createOutput(url: URL) =
    createOutput(url.openConnection().getOutputStream())

fun IOFactory.createInput(arr: ByteArray) = createInput(ByteArrayInputStream(arr))