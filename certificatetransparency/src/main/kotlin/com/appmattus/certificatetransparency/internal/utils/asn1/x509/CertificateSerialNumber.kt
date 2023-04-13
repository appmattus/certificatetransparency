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
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Logger
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import java.math.BigInteger

internal class CertificateSerialNumber private constructor(private val integer: ASN1Integer) : ASN1Object() {

    override val tag: ASN1HeaderTag
        get() = integer.tag

    override val totalLength: Int
        get() = integer.totalLength

    override val encoded: ByteBuffer
        get() = integer.encoded

    override val logger: ASN1Logger
        get() = integer.logger

    val serialNumber: BigInteger by lazy { integer.value }

    override fun toString(): String =
        "Serial Number ${serialNumber.toByteArray().toHexString().uppercase().chunked(2).joinToString(" ")}"

    companion object {
        fun create(sequence: ASN1Integer) = CertificateSerialNumber(sequence)
    }
}
