package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer

@Suppress("MagicNumber")
internal class TbsCertificate private constructor(private val sequence: ASN1Sequence) : ASN1Object {

    private val versionOffset = if (sequence.values[0] is Version) 1 else 0

    // Version
    val version: Version?
        get() = sequence.values[0] as? Version

    // CertificateSerialNumber
    val serialNumber: CertificateSerialNumber
        get() = CertificateSerialNumber.create(sequence.values[versionOffset] as ASN1Integer)

    // AlgorithmIdentifier
    val signature: ASN1Sequence
        get() = sequence.values[versionOffset + 1] as ASN1Sequence

    // Name
    val issuer: ASN1Sequence
        get() = sequence.values[versionOffset + 2] as ASN1Sequence

    val validity: Validity
        get() = Validity.create(sequence.values[versionOffset + 3] as ASN1Sequence)

    // Name
    val subject: ASN1Sequence
        get() = sequence.values[versionOffset + 4] as ASN1Sequence

    // SubjectPublicKeyInfo
    val subjectPublicKeyInfo: ASN1Sequence
        get() = sequence.values[versionOffset + 5] as ASN1Sequence

    // UniqueIdentifier 0xa1 (optional)
    val issuerUniqueIdentifier: ASN1Object?
        get() = sequence.values.firstOrNull { it.tag == 0xa1 }

    // UniqueIdentifier 0xa2 (optional)
    val subjectUniqueIdentifier: ASN1Object?
        get() = sequence.values.firstOrNull { it.tag == 0xa2 }

    // Extensions 0xa3 (optional)
    val extensions: Extensions?
        get() = sequence.values.firstOrNull { it.tag == 0xa3 } as Extensions?

    override val tag: Int
        get() = sequence.tag

    override val totalLength: Int
        get() = sequence.totalLength

    override val encoded: ByteBuffer
        get() = sequence.encoded

    override fun toString(): String {
        return "TbsCertificate" +
            "\n  version=${version?.version ?: 0}," +
            "\n  serialNumber=$serialNumber" +
            "\n  subjectUniqueIdentifier=$subjectUniqueIdentifier" +
            "\n  extensions=$extensions"
    }

    // optional a2
    // optional a3

    companion object {
        fun create(sequence: ASN1Sequence): TbsCertificate = TbsCertificate(sequence)
    }
}
