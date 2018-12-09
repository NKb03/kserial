/**
 * @author Nikolaus Knop
 */

package kserial

import java.io.InputStream
import java.io.OutputStream

interface IOFactory {
    fun createInput(stream: InputStream)

    fun createOutput(stream: OutputStream)
}