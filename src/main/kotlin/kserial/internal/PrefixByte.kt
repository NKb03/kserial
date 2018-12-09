package kserial.internal

import kotlin.experimental.and
import kotlin.experimental.or


internal object PrefixByte {
    private const val NUL: Byte = 0b1
    private const val REF: Byte = 0b10
    private const val SHARE: Byte = 0b100
    private const val TYPED: Byte = 0b1000
    private const val CLS_SHARE: Byte = 0b10000
    private const val CLS_REF: Byte = 0b1000000

    fun isNull(b: Byte) = b and NUL > 0

    fun isRef(b: Byte) = b and REF > 0

    fun isShare(b: Byte) = b and SHARE > 0

    fun isUntyped(b: Byte) = b and TYPED > 0

    fun isClsShare(b: Byte) = b and CLS_SHARE > 0

    fun isClsRef(b: Byte) = b and CLS_REF > 0

    fun byte(
        nul: Boolean = false,
        ref: Boolean = false,
        share: Boolean = false,
        untyped: Boolean = false,
        clsShare: Boolean = false,
        clsRef: Boolean = false
    ): Byte {
        val n = if (nul) NUL else 0
        val r = if (ref) REF else 0
        val s = if (share) SHARE else 0
        val t = if (untyped) TYPED else 0
        val cs = if (clsShare) CLS_SHARE else 0
        val cr = if (clsRef) CLS_REF else 0
        return n or r or s or t or cs or cr
    }

    val NULL = byte(nul = true)
}
