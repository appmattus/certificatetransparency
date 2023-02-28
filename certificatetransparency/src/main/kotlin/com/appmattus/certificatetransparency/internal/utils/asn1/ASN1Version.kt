package com.appmattus.certificatetransparency.internal.utils.asn1

internal data class ASN1Version(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val version: ASN1Integer? by lazy {
        encoded.toAsn1() as ASN1Integer
    }

    override fun toString(): String = "Version ${version?.value?.toInt()?.plus(1) ?: 1}"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer) = ASN1Version(tag, totalLength, encoded)
    }
}
