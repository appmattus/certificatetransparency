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

import java.math.BigInteger

internal data class ASN1HeaderTag(
    val tagClass: TagClass,
    val tagForm: TagForm,
    val tagNumber: BigInteger,
    val blockLength: Int
) {
    constructor(tagClass: TagClass, tagForm: TagForm, tagNumber: Int, blockLength: Int) :
        this(tagClass, tagForm, tagNumber.toBigInteger(), blockLength)

    private val longTagNumber: Long? = if (tagNumber < Long.MAX_VALUE.toBigInteger()) tagNumber.toLong() else null

    internal fun isTagNumber(tagNumber: Int): Boolean {
        return longTagNumber != null && this.longTagNumber == tagNumber.toLong()
    }

    internal fun isTagNumber(tagNumber: BigInteger): Boolean {
        return this.tagNumber == tagNumber
    }

    fun isUniversal(tagNumber: Int): Boolean {
        return this.tagClass == TagClass.Universal && isTagNumber(tagNumber)
    }

    fun isContextSpecific(tagNumber: Int, isConstructed: Boolean = true): Boolean {
        return this.tagClass == TagClass.ContextSpecific && isTagNumber(tagNumber) &&
            (((isConstructed && this.tagForm == TagForm.Constructed) || (!isConstructed && this.tagForm == TagForm.Primitive)))
    }

    val tagBytes: ByteArray
        get() {
            val tagClassByte = when (tagClass) {
                TagClass.Universal -> 0x00
                TagClass.Application -> 0x40
                TagClass.ContextSpecific -> 0x80
                TagClass.Private -> 0xC0
            }

            val tagFormByte = when (tagForm) {
                TagForm.Primitive -> 0x00
                TagForm.Constructed -> 0x20
            }

            return if (longTagNumber != null && longTagNumber <= 30) {
                val firstByte = tagClassByte + tagFormByte + longTagNumber
                byteArrayOf(firstByte.toByte())
            } else if (longTagNumber != null && longTagNumber <= 127) {
                val firstByte = tagClassByte + tagFormByte + 0x1f
                byteArrayOf(firstByte.toByte(), longTagNumber.toByte())
            } else {
                // tag > 127
                val firstByte = tagClassByte + tagFormByte + 0x1f

                var value = tagNumber

                val bytes = mutableListOf<Byte>()

                var isFirst = true
                while (value != BigInteger.ZERO) {
                    bytes.add(((value.toInt() and 0x7f) + (if (isFirst) 0 else 0x80)).toByte())
                    value = value shr 7
                    isFirst = false
                }
                bytes.add(firstByte.toByte())
                bytes.reversed().toByteArray()
            }
        }
}
