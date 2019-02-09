/**
 * @author Nikolaus Knop
 */

package kserial

import kserial.SharingMode.Unshared
import kserial.internal.BinaryInput
import kserial.internal.BinaryOutput
import java.io.*

/**
 * Root API-class of the kserial-api
 */
class KSerial private constructor(
    private val shareClsNames: Boolean,
    private val sharingMode: SharingMode
) {
    /**
     * Create an [Input] reading from the specified [stream]
     */
    fun createInput(stream: InputStream, context: SerialContext): Input =
        BinaryInput(DataInputStream(stream), context)

    /**
     * Create an [Output] writing to the specified [stream]
     */
    fun createOutput(stream: OutputStream, context: SerialContext): Output {
        return BinaryOutput(DataOutputStream(stream), sharingMode, shareClsNames, context)
    }

    /**
     * The builder for [KSerial]
     */
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
        /**
         * Build a [KSerial] instance
         */
        inline fun newInstance(block: Builder.() -> Unit = {}): KSerial = Builder().apply(block).build()
    }
}