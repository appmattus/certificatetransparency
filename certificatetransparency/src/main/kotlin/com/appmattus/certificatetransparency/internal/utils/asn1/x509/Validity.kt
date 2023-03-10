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

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Time
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
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
        notValidBefore.value.atZone(ZoneId.systemDefault()).format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
        )
        }\nNot Valid After ${
        notValidAfter.value.atZone(ZoneId.systemDefault()).format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
        )
        }"

    companion object {
        fun create(sequence: ASN1Sequence): Validity = Validity(sequence)
    }
}
