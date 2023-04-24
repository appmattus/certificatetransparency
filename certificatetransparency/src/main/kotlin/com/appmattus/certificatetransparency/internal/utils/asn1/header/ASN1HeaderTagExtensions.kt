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

import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import java.math.BigInteger

private const val LONG_LIMIT = (Long.MAX_VALUE shr 7) - 0x7fL

@Suppress("MagicNumber", "CyclomaticComplexMethod", "NestedBlockDepth")
internal fun ByteBuffer.tag(): ASN1HeaderTag {
    if (size == 0) error("Zero buffer length")

    val tagClass = when (this[0].toInt() and 0xC0) {
        0x00 -> TagClass.Universal
        0x40 -> TagClass.Application
        0x80 -> TagClass.ContextSpecific
        0xC0 -> TagClass.Private
        else -> error("Unknown tag class")
    }

    val form: TagForm = if ((this[0].toInt() and 0x20) == 0x20) TagForm.Constructed else TagForm.Primitive

    // Find tag number
    val tagNumberMask = this[0].toInt() and 0x1F
    if (tagNumberMask != 0x1F) {
        // Simple case (tag number < 31)
        return ASN1HeaderTag(
            tagClass = tagClass,
            tagForm = form,
            tagNumber = tagNumberMask.toBigInteger(),
            readLength = 1
        )
    } else {
        // Tag number bigger or equal to 31
        var tagNumber = 0L
        var bigTagNumber: BigInteger? = null
        var i = 1

        do {
            if (i >= size) {
                error("End of input reached before message was fully decoded")
            }

            if (tagNumber < LONG_LIMIT) {
                tagNumber = (tagNumber shl 7) + (this[i].toLong() and 0x7FL)
            } else {
                if (bigTagNumber == null) bigTagNumber = tagNumber.toBigInteger()
                bigTagNumber = (bigTagNumber shl 7) + (this[i].toLong() and 0x7F).toBigInteger()
            }
        } while (this[i++].toInt() and 0x80 != 0)

        if (tagClass == TagClass.Universal && form == TagForm.Constructed) {
            when (tagNumber) {
                1L, // Boolean
                2L, // REAL
                5L, // Null
                6L, // OBJECT IDENTIFIER
                9L, // REAL
                13L, // RELATIVE OBJECT IDENTIFIER
                14L, // Time
                23L,
                24L,
                31L,
                33L,
                34L -> error("Constructed encoding used for primitive type")
            }
        }

        return ASN1HeaderTag(
            tagClass = tagClass,
            tagForm = form,
            tagNumber = bigTagNumber ?: tagNumber.toBigInteger(),
            readLength = i
        )
    }
}
