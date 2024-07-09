/*
 * Copyright 2023-2024 Appmattus Limited
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
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.header.length
import com.appmattus.certificatetransparency.internal.utils.asn1.header.tag
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Extensions
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Version
import okio.ByteString.Companion.toByteString

internal fun ByteArray.toAsn1(logger: ASN1Logger = EmptyLogger): ASN1Object = toByteBuffer().toAsn1(logger)

internal data class ASN1Header(val tag: ASN1HeaderTag, val headerLength: Int, val dataLength: Int) {
    val totalLength: Int
        get() = headerLength + dataLength
}

@Suppress("MagicNumber")
internal fun ByteBuffer.header(logger: ASN1Logger): ASN1Header {
    val tag = tag()

    val headerLength = length(tag, logger)

    return ASN1Header(tag, headerLength.offset, headerLength.length)
}

@Suppress("MagicNumber", "CyclomaticComplexMethod")
internal fun ByteBuffer.toAsn1(logger: ASN1Logger = EmptyLogger): ASN1Object {
    val header = header(logger)

    val encoded = this.range(header.headerLength, header.totalLength)

    val tag = header.tag

    return when {
        tag.isUniversal(0x01) -> ASN1Boolean.create(tag, encoded, logger)
        tag.isUniversal(0x02) -> ASN1Integer.create(tag, encoded, logger)
        tag.isUniversal(0x03) -> ASN1BitString.create(tag, encoded, logger)
        tag.isUniversal(0x05) -> ASN1Null.create(tag, encoded, logger)
        tag.isUniversal(0x06) -> ASN1ObjectIdentifier.create(tag, encoded, logger)
        tag.isUniversal(0x0c) -> ASN1PrintableStringUS.create(tag, encoded, logger)
        tag.isUniversal(0x10) || tag.isUniversal(0x11) -> ASN1Sequence.create(tag, encoded, logger)
        tag.isUniversal(0x13) -> ASN1PrintableStringTeletex.create(tag, encoded, logger)
        tag.isUniversal(0x17) -> UTCTime.create(tag, encoded, logger)
        tag.isUniversal(0x18) -> GeneralizedTime.create(tag, encoded, logger)
        tag.isContextSpecific(0x00) -> Version.create(tag, encoded, logger)
        tag.isContextSpecific(0x03) -> Extensions.create(tag, encoded, logger)
        else -> ASN1Unspecified.create(tag, encoded, logger)
    }
}

@Suppress("MagicNumber")
internal fun ByteArray.readNestedOctets(count: Int, logger: ASN1Logger = EmptyLogger): ByteArray {
    var bytes: ByteBuffer = this.toByteBuffer()
    repeat(count) {
        val asn = bytes.toAsn1(logger)
        if (!asn.tag.isUniversal(0x04)) error("Not an octet string")
        bytes = asn.encoded
    }

    return bytes.copyOfRange(0, bytes.size)
}

internal fun ByteArray.toHexString(): String = toByteString().hex()

internal fun ByteBuffer.toHexString(): String = toList().toByteArray().toHexString()
