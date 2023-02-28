package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1BitString
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1
import com.appmattus.certificatetransparency.internal.utils.asn1.toByteBuffer

internal class Certificate private constructor(private val sequence: ASN1Sequence) {

    val tbsCertificate: TbsCertificate
        get() = TbsCertificate.create(sequence.values[0] as ASN1Sequence)

    val signatureAlgorithm: ASN1Object
        get() = sequence.values[1]

    val signatureValue: ASN1BitString
        get() = sequence.values[2] as ASN1BitString

    override fun toString(): String {
        val subject = tbsCertificate.subject.toString().prependIndent("    ")
        val issuer = tbsCertificate.issuer.toString().prependIndent("    ")

        return "Certificate" +
            "\n  Subject Name" +
            "\n$subject" +
            "\n\n  Issuer Name" +
            "\n$issuer" +
            "\n\n${tbsCertificate.serialNumber.toString().prependIndent("  ")}" +
            "\n${tbsCertificate.version.toString().prependIndent("  ")}" +
            "\n\n${tbsCertificate.validity.toString().prependIndent("  ")}" +
            "\n\n  Signature ${signatureValue.encoded.size - 1} bytes"
    }

    companion object {
        fun create(byteArray: ByteArray): Certificate =
            Certificate(byteArray.toByteBuffer().toAsn1() as ASN1Sequence)
    }
}
