package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Time
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class Validity private constructor(private val sequence: ASN1Sequence) : ASN1Object {

    val notValidBefore: ASN1Time by lazy { sequence.values[0] as ASN1Time }

    val notValidAfter: ASN1Time by lazy { sequence.values[1] as ASN1Time }

    override val tag: Int
        get() = sequence.tag

    override val encoded: ByteBuffer
        get() = sequence.encoded

    override fun toString(): String =
        "Not Valid Before ${
            notValidBefore.value.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL))
        }\nNot Valid After ${
            notValidAfter.value.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL))
        }"

    companion object {
        fun create(sequence: ASN1Sequence): Validity = Validity(sequence)
    }
}
