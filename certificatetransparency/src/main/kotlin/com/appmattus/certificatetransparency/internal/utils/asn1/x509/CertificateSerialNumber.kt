package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import org.bouncycastle.util.encoders.Hex

internal class CertificateSerialNumber private constructor(val serialNumber: ASN1Integer) : ASN1Object {

    override val tag: Int
        get() = serialNumber.tag

    override val totalLength: Int
        get() = serialNumber.totalLength

    override val encoded: ByteBuffer
        get() = serialNumber.encoded

    override fun toString(): String =
        "Serial Number ${serialNumber.value.toByteArray().toHexString().uppercase().chunked(2).joinToString(" ")}"

    companion object {
        fun create(sequence: ASN1Integer): CertificateSerialNumber = CertificateSerialNumber(sequence)

        private fun ByteArray.toHexString(): String = Hex.toHexString(this)
    }
}
