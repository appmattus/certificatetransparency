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
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Extensions
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Version
import okio.ByteString.Companion.toByteString

internal fun ByteArray.toAsn1(): ASN1Object = toByteBuffer().toAsn1()

internal data class ASN1Header(val tag: Int, val headerLength: Int, val dataLength: Int) {
    val totalLength: Int
        get() = headerLength + dataLength
}

@Suppress("MagicNumber")
internal fun ByteBuffer.header(): ASN1Header {
    val tag = this[0].toInt() and 0xff

    var length = this[1].toInt() and 0xff
    var offset = 2
    if (length >= 0x7f) {
        val numLengthBytes = length and 0x7f
        length = 0
        repeat(numLengthBytes) { index ->
            length = length shl 8
            length += this[offset + index].toInt() and 0xff
        }
        offset += numLengthBytes
    }

    return ASN1Header(tag, offset, length)
}

@Suppress("MagicNumber")
internal fun ByteBuffer.toAsn1(): ASN1Object {
    val header = header()

    val encoded = this.range(header.headerLength, header.totalLength)

    val tag = header.tag
    val totalLength = header.totalLength

    return when (tag) {
        0x01 -> ASN1Boolean.create(tag, encoded)
        0x02 -> ASN1Integer.create(tag, encoded)
        0x03 -> ASN1BitString.create(tag, totalLength, encoded)
        0x05 -> ASN1Null.create(tag, totalLength, encoded)
        0x06 -> ASN1ObjectIdentifier.create(tag, totalLength, encoded)
        0x0c -> ASN1PrintableStringUS.create(tag, totalLength, encoded)
        0x13 -> ASN1PrintableStringTeletex.create(tag, totalLength, encoded)
        0x17 -> ASN1Time.create(tag, totalLength, encoded)
        0x30, 0x31 -> ASN1Sequence.create(tag, encoded)
        0xa0 -> Version.create(tag, encoded)
        0xa3 -> Extensions.create(tag, encoded)
        else -> ASN1Unspecified.create(tag, totalLength, encoded)
    }
}

internal fun ByteArray.toHexString(): String = toByteString().hex()

internal fun ByteBuffer.toHexString(): String = toList().toByteArray().toHexString()
