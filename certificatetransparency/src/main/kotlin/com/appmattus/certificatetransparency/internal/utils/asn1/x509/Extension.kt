package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Boolean
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1ObjectIdentifier
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer

internal class Extension private constructor(
    private val sequence: ASN1Sequence
) : ASN1Object {
    override val tag: Int = sequence.tag
    override val encoded: ByteBuffer = sequence.encoded

    private val criticalOffset = if (sequence.values[1] is ASN1Boolean) 1 else 0

    val objectIdentifier: String by lazy { (sequence.values[0] as ASN1ObjectIdentifier).value }
    val isCritical: Boolean by lazy { (sequence.values[1] as? ASN1Boolean)?.value ?: false }
    val value: ASN1Object by lazy { sequence.values[criticalOffset + 1] }

    override fun toString(): String {
        return "Extension $objectIdentifier\n  Critical ${if (isCritical) "YES" else "NO"}"
    }

    companion object {
        fun create(sequence: ASN1Sequence) = Extension(sequence)
    }
}
