package kserial.internal

import kserial.SerializationException


internal object PrefixByte {
    private const val NUL = 0b1
    private const val REF = 0b10
    private const val SHARE = 0b100
    private const val UNTYPED = 0b1000
    private const val CLS_SHARE = 0b10000
    private const val CLS_REF = 0b1000000

    fun isNull(b: Int) = b and NUL > 0

    fun isRef(b: Int) = b and REF > 0

    fun isShare(b: Int) = b and SHARE > 0

    fun isUntyped(b: Int) = b and UNTYPED > 0

    fun isClsShare(b: Int) = b and CLS_SHARE > 0

    fun isClsRef(b: Int) = b and CLS_REF > 0

    fun prefixByte(
        nul: Boolean = false,
        ref: Boolean = false,
        share: Boolean = false,
        untyped: Boolean = false,
        clsShare: Boolean = false,
        clsRef: Boolean = false
    ): Int {
        val n = if (nul) NUL else 0
        val r = if (ref) REF else 0
        val s = if (share) SHARE else 0
        val t = if (untyped) UNTYPED else 0
        val cs = if (clsShare) CLS_SHARE else 0
        val cr = if (clsRef) CLS_REF else 0
        return n or r or s or t or cs or cr
    }

    const val NULL = 0b1

    const val BYTE_T = -1

    const val TRUE = -2

    const val FALSE = -3

    const val SHORT_T = -4

    const val CHAR_T = -5

    const val INT_T = -6

    const val LONG_T = -7

    const val FLOAT_T = -8

    const val DOUBLE_T = -9

    const val STRING_T = -10

    private val primitiveTypeNames = mapOf(
        BYTE_T to "byte",
        TRUE to "boolean",
        FALSE to "boolean",
        CHAR_T to "char",
        SHORT_T to "short",
        INT_T to "int",
        LONG_T to "long",
        FLOAT_T to "float",
        DOUBLE_T to "double",
        STRING_T to "string"
    )

    fun getTypeName(typeByte: Int): String {
        return if (typeByte >= 0) "Object"
        else primitiveTypeNames[typeByte]
            ?: throw SerializationException("No type with prefix byte $typeByte")
    }
}
