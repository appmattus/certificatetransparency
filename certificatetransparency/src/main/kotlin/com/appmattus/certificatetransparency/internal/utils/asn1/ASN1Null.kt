package com.appmattus.certificatetransparency.internal.utils.asn1

internal class ASN1Null private constructor(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    override fun toString(): String = "NULL"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer): ASN1Null {
            assert(encoded.size == 0)
            return ASN1Null(tag, totalLength, encoded)
        }
    }
}
