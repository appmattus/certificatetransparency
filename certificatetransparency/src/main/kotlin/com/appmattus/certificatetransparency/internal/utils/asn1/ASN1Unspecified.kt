package com.appmattus.certificatetransparency.internal.utils.asn1

import org.bouncycastle.util.encoders.Hex

internal data class ASN1Unspecified(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    override fun toString(): String = "UNSPECIFIED 0x${byteArrayOf(tag.toByte()).toHexString()}"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer) = ASN1Unspecified(tag, totalLength, encoded)

        private fun ByteArray.toHexString(): String = Hex.toHexString(this)
    }
}
