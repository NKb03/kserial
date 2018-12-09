/**
 * @author Nikolaus Knop
 */

package kserial

import java.io.InputStream
import java.io.OutputStream

interface IOFactory {
    fun createInput(stream: InputStream): Input

    fun createOutput(stream: OutputStream): Output

    object Binary: IOFactory {
        override fun createInput(stream: InputStream): BinaryInput = BinaryInput.fromStream(stream)

        override fun createOutput(stream: OutputStream): Output = BinaryOutput.toStream(stream)
    }
}