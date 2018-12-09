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

    fun createOutput(stream: OutputStream, vararg options: SerializationOption): Output

    object Binary : IOFactory {
        override fun createInput(stream: InputStream): BinaryInput = BinaryInput.fromStream(stream)

        override fun createOutput(stream: OutputStream, vararg options: SerializationOption): Output {
            val sharingMode = getSharingMode(options)
            val shareClsNames = isShareClassNames(options)
            return BinaryOutput.toStream(stream, sharingMode, shareClsNames)
        }
    }

    companion object {
        private fun getSharingMode(options: Array<out SerializationOption>): SharingMode {
            val modes = options.filterIsInstance<SerializationOption.Sharing>()
            require(modes.size <= 1) { "multiple sharing modes supplied" }
            return modes.firstOrNull()?.mode ?: Unshared
        }

        private fun isShareClassNames(options: Array<out SerializationOption>): Boolean = ShareClassNames in options
    }
}

fun IOFactory.createInput(file: File) =
    createInput(FileInputStream(file))

fun IOFactory.createOutput(file: File, vararg options: SerializationOption) =
    createOutput(FileOutputStream(file), *options)

fun IOFactory.createInput(path: Path) =
    createInput(Files.newInputStream(path))

fun IOFactory.createOutput(path: Path, vararg options: SerializationOption) =
    createOutput(Files.newOutputStream(path), *options)

fun IOFactory.createInput(url: URL) = createInput(url.openStream())

fun IOFactory.createOutput(url: URL, vararg options: SerializationOption) =
    createOutput(url.openConnection().getOutputStream(), *options)

fun IOFactory.createInput(arr: ByteArray) = createInput(ByteArrayInputStream(arr))