/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.SharingMode.Unshared
import kserial.internal.BinaryInput
import kserial.internal.BinaryOutput
import java.io.*

class KSerial private constructor(
    private val shareClsNames: Boolean,
    private val sharingMode: SharingMode
) {

    fun createInput(stream: InputStream): Input =
        BinaryInput(DataInputStream(stream))

    fun createOutput(stream: OutputStream): Output {
        return BinaryOutput(DataOutputStream(stream), sharingMode, shareClsNames)
    }

    class Builder @PublishedApi internal constructor() {
        /**
         * Specifies whether to share class names, defaults to `false`
         */
        var shareClsNames = false

        /**
         * The sharing mode, defaults to [Unshared]
         */
        var sharingMode: SharingMode = Unshared

        @PublishedApi internal fun build(): KSerial = KSerial(shareClsNames, sharingMode)
    }

    companion object {
        fun newInstance(block: Builder.() -> Unit): KSerial = Builder().apply(block).build()
    }
}