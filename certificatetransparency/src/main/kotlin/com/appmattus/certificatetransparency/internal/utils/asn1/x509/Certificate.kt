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

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1BitString
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Logger
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.EmptyLogger
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1

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
            "\n${tbsCertificate.version?.toString()?.prependIndent("  ") ?: "  Version 1"}" +
            "\n\n${tbsCertificate.validity.toString().prependIndent("  ")}" +
            "\n\n  Signature ${signatureValue.encoded.size - 1} bytes" +
            "\n\n${tbsCertificate.extensions?.toString()?.prependIndent("  ") ?: ""}"
    }

    companion object {
        fun create(byteArray: ByteArray, logger: ASN1Logger = EmptyLogger) = Certificate(byteArray.toByteBuffer().toAsn1(logger) as ASN1Sequence)
    }
}
