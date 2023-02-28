package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import java.math.BigInteger

internal class CertificateSerialNumber private constructor(private val integer: ASN1Integer) : ASN1Object {

    override val tag: Int
        get() = integer.tag

    override val totalLength: Int
        get() = integer.totalLength

    override val encoded: ByteBuffer
        get() = integer.encoded

    val serialNumber: BigInteger by lazy { integer.value }

    override fun toString(): String =
        "Serial Number ${serialNumber.toByteArray().toHexString().uppercase().chunked(2).joinToString(" ")}"

    companion object {
        fun create(sequence: ASN1Integer) = CertificateSerialNumber(sequence)
    }
}
