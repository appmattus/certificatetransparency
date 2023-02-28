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
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBufferArray
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import java.math.BigInteger

internal interface ASN1Object {
    val tag: Int
    val encoded: ByteBuffer

    @Suppress("MagicNumber")
    private val lengthBytes: ByteArray
        get() = if (encoded.size <= 0x7f) {
            byteArrayOf(encoded.size.toByte())
        } else {
            val bytes = BigInteger.valueOf(encoded.size.toLong()).toByteArray()

            byteArrayOf((0x80 + bytes.size).toByte()) + bytes
        }

    val totalLength: Int
        get() = encoded.size + lengthBytes.size + 1

    val bytes: ByteBuffer
        get() = ByteBufferArray(listOf(byteArrayOf(tag.toByte()).toByteBuffer(), lengthBytes.toByteBuffer(), encoded))
}
