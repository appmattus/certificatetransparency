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

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Boolean
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1ObjectIdentifier
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag

internal class Extension private constructor(
    private val sequence: ASN1Sequence
) : ASN1Object {
    override val tag: ASN1HeaderTag = sequence.tag
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
