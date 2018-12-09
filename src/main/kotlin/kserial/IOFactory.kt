/**
 * @author Nikolaus Knop
 */

package kserial

import java.io.*
import java.nio.file.*

interface IOFactory {
    fun createInput(stream: InputStream): Input

    fun createOutput(stream: OutputStream): Output

    object Binary : IOFactory {
        override fun createInput(stream: InputStream): BinaryInput = BinaryInput.fromStream(stream)

        override fun createOutput(stream: OutputStream): Output = BinaryOutput.toStream(stream)
    }
}

fun IOFactory.createInput(file: File) = createInput(FileInputStream(file))

fun IOFactory.createOutput(file: File) = createOutput(FileOutputStream(file))

fun IOFactory.createInput(path: Path) = createInput(Files.newInputStream(path))

fun IOFactory.createOutput(path: Path) = createOutput(Files.newOutputStream(path))