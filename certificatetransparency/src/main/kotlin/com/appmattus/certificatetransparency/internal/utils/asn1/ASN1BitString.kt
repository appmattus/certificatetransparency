package com.appmattus.certificatetransparency.internal.utils.asn1

internal data class ASN1BitString(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    override fun toString(): String = "BIT STRING"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer): ASN1BitString =
            ASN1BitString(tag, totalLength, encoded)
    }
}
