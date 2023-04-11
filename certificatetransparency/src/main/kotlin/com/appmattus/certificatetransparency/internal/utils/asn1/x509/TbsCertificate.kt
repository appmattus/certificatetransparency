/*
 * Copyright 2023 Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.header.TagClass
import com.appmattus.certificatetransparency.internal.utils.asn1.header.TagForm

@Suppress("MagicNumber")
internal class TbsCertificate private constructor(private val sequence: ASN1Sequence) : ASN1Object {

    override val tag: ASN1HeaderTag = sequence.tag
    override val encoded: ByteBuffer = sequence.encoded

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
        get() = sequence.values.firstOrNull {
            it.tag.tagClass == TagClass.ContextSpecific && it.tag.tagForm == TagForm.Constructed && it.tag.tagNumber == 1.toBigInteger()
        }

    // UniqueIdentifier 0xa2 (optional)
    val subjectUniqueIdentifier: ASN1Object?
        get() = sequence.values.firstOrNull {
            it.tag.tagClass == TagClass.ContextSpecific && it.tag.tagForm == TagForm.Constructed && it.tag.tagNumber == 2.toBigInteger()
        }

    // Extensions 0xa3 (optional)
    val extensions: Extensions? by lazy {
        sequence.values.firstOrNull {
            it.tag.tagClass == TagClass.ContextSpecific && it.tag.tagForm == TagForm.Constructed && it.tag.tagNumber == 3.toBigInteger()
        } as? Extensions
    }

    override fun toString(): String {
        return "TbsCertificate" +
            "\n  version=${version?.value ?: 0}," +
            "\n  serialNumber=$serialNumber" +
            "\n  subjectUniqueIdentifier=$subjectUniqueIdentifier" +
            "\n  extensions=$extensions"
    }

    fun copy(
        version: Version? = this.version,
        issuer: ASN1Sequence = this.issuer,
        extensions: Extensions? = this.extensions
    ): TbsCertificate {
        val values = buildList {
            version?.let { add(it) }
            add(serialNumber)
            add(signature)
            add(issuer)
            add(validity)
            add(subject)
            add(subjectPublicKeyInfo)
            issuerUniqueIdentifier?.let { add(it) }
            subjectUniqueIdentifier?.let { add(it) }
            extensions?.let { add(it) }
        }

        return create(
            ASN1Sequence.create(
                tag = ASN1HeaderTag(TagClass.Universal, TagForm.Constructed, 0x10, 1),
                values = values
            )
        )
    }

    companion object {
        fun create(sequence: ASN1Sequence): TbsCertificate = TbsCertificate(sequence)
    }
}
