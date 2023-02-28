package com.appmattus.certificatetransparency.internal.utils.asn1

internal class ASN1PrintableString private constructor(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val value: String by lazy {
        encoded.copyOfRange(0, encoded.size).decodeToString()
    }

    override fun toString(): String = "PRINTABLE STRING $value"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer): ASN1PrintableString =
            ASN1PrintableString(tag, totalLength, encoded)
    }
}
