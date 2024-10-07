/*
 * Copyright 2024 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.utils.asn1

import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class GeneralizedTime private constructor(
    override val tag: ASN1HeaderTag,
    override val encoded: ByteBuffer,
    override val logger: ASN1Logger
) : ASN1Object(), ASN1Time {

    override val value: Date by lazy {
        // RFC5280 specifies:
        // For the purposes of this profile, GeneralizedTime values MUST be
        // expressed in Greenwich Mean Time (Zulu) and MUST include seconds
        // (i.e., times are YYYYMMDDHHMMSSZ), even where the number of seconds
        // is zero.  GeneralizedTime values MUST NOT include fractional seconds.
        // https://datatracker.ietf.org/doc/html/rfc5280#section-4.1.2.5.2
        val time = encoded.toList().toByteArray().decodeToString()
        SimpleDateFormat("yyyyMMddHHmmss'Z'", Locale.ROOT).parse(time)
    }

    override fun toString(): String = "TIME $value"

    companion object {
        fun create(tag: ASN1HeaderTag, encoded: ByteBuffer, logger: ASN1Logger) = GeneralizedTime(tag, encoded, logger)
    }
}
