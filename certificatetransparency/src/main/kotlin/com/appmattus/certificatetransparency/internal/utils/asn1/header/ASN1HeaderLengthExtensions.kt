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

package com.appmattus.certificatetransparency.internal.utils.asn1.header

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Logger
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer

@Suppress("MagicNumber")
internal fun ByteBuffer.length(tag: ASN1HeaderTag, logger: ASN1Logger): ASN1HeaderLength {
    var offset = tag.readLength

    if (offset >= size) error("No length block encoded")
    var length = this[offset].toInt() and 0xff
    offset++
    if (length == 0xff) error("Length block 0xFF is reserved by standard")

    if (length == 0x80) {
        // indefinite length
        // X509 certificates are encoded with ASN.1 DER which does not allow indefinite-length encodings
        error("Indefinite length encoding not supported")
        // length = size - offset
    } else if ((length and 0x80) == 0x80) {
        // long form used
        val numLengthBytes = length and 0x7f

        // We don't support values larger than an Int as we cannot store such large data in a ByteArray
        if (numLengthBytes > 8) error("Too big integer")

        if (numLengthBytes + 1 > size) error("End of input reached before message was fully decoded")

        if (this[offset].toInt() and 0xff == 0x0) logger.warning("ASN1HeaderLength", "Needlessly long encoded length")

        length = 0
        repeat(numLengthBytes) { index ->
            length = length shl 8
            length += this[offset + index].toInt() and 0xff
        }
        offset += numLengthBytes

        if (length <= 127) logger.warning("ASN1HeaderLength", "Unnecessary usage of long length form")
    }

    return ASN1HeaderLength(length, offset)
}
