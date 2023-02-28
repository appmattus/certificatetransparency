package com.appmattus.certificatetransparency.internal.utils.asn1

internal class ASN1Boolean private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: Boolean by lazy { encoded[0] != 0x00.toByte() }

    override fun toString(): String = "BOOLEAN $value"

    companion object {
        fun create(tag: Int, encoded: ByteBuffer): ASN1Boolean {
            assert(encoded.size == 1)
            return ASN1Boolean(tag, encoded)
        }
    }
}
