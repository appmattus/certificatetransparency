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

package com.appmattus.certificatetransparency.internal.utils.asn1

import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class ASN1Time private constructor(
    override val tag: ASN1HeaderTag,
    override val totalLength: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: Instant by lazy {
        @Suppress("MagicNumber")
        val pattern = if (encoded.size == 13) "yyMMddHHmmss'Z'" else "yyyyMMddHHmmss'Z'"
        val formatter = DateTimeFormatter.ofPattern(pattern)

        val time = encoded.toList().toByteArray().decodeToString()

        LocalDateTime.parse(time, formatter).toInstant(ZoneOffset.UTC)
    }

    override fun toString(): String = "TIME $value"

    companion object {
        fun create(tag: ASN1HeaderTag, totalLength: Int, encoded: ByteBuffer) = ASN1Time(tag, totalLength, encoded)
    }
}
