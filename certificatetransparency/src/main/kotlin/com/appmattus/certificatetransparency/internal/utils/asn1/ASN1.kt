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
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.header.tag
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Extensions
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Version
import okio.ByteString.Companion.toByteString
import java.util.logging.Logger

internal fun ByteArray.toAsn1(): ASN1Object = toByteBuffer().toAsn1()

internal data class ASN1Header(val tag: ASN1HeaderTag, val headerLength: Int, val dataLength: Int) {
    val totalLength: Int
        get() = headerLength + dataLength
}

@Suppress("MagicNumber")
internal fun ByteBuffer.header(): ASN1Header {
    val tag = tag()

    var offset = tag.blockLength
    if (offset >= size) throw IllegalStateException("No length block encoded")
    var length = this[offset].toInt() and 0xff
    offset++
    if (length == 0xff) throw IllegalStateException("Length block 0xFF is reserved by standard")

    if (length == 0x80) {
        // indefinite length
        // TODO Not currently verified/supported
        length = size - offset
    } else if ((length and 0x80) == 0x80) {
        // longFormUsed
        val numLengthBytes = length and 0x7f

        // TODO Support large length with BigInteger
        if (numLengthBytes > 8) throw IllegalStateException("Too big integer")

        if (numLengthBytes + 2 > size) throw IllegalStateException("End of input reached before message was fully decoded")

        if (this[offset].toInt() and 0xff == 0x0) Logger.getLogger("ASN1").warning("Needlessly long encoded length")

        length = 0
        repeat(numLengthBytes) { index ->
            length = length shl 8
            length += this[offset + index].toInt() and 0xff
        }
        offset += numLengthBytes

        if (length <= 127) Logger.getLogger("ASN1").warning("Unnecessary usage of long length form")
    }

    return ASN1Header(tag, offset, length)
}

@Suppress("MagicNumber")
internal fun ByteBuffer.toAsn1(): ASN1Object {
    val header = header()

    val encoded = this.range(header.headerLength, header.totalLength)

    val tag = header.tag

    return when {
        tag.isUniversal(0x01) -> ASN1Boolean.create(tag, encoded)
        tag.isUniversal(0x02) -> ASN1Integer.create(tag, encoded)
        tag.isUniversal(0x03) -> ASN1BitString.create(tag, encoded)
        tag.isUniversal(0x05) -> ASN1Null.create(tag, encoded)
        tag.isUniversal(0x06) -> ASN1ObjectIdentifier.create(tag, encoded)
        tag.isUniversal(0x0c) -> ASN1PrintableStringUS.create(tag, encoded)
        tag.isUniversal(0x10) || tag.isUniversal(0x11) -> ASN1Sequence.create(tag, encoded)
        tag.isUniversal(0x13) -> ASN1PrintableStringTeletex.create(tag, encoded)
        tag.isUniversal(0x17) -> ASN1Time.create(tag, encoded)
        tag.isContextSpecific(0x00) -> Version.create(tag, encoded)
        tag.isContextSpecific(0x03) -> Extensions.create(tag, encoded)
        else -> ASN1Unspecified.create(tag, encoded)
    }
}

internal fun ByteArray.toHexString(): String = toByteString().hex()

internal fun ByteBuffer.toHexString(): String = toList().toByteArray().toHexString()
