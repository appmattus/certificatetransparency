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
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.joinToByteBuffer

internal data class ASN1Sequence(
    override val tag: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val values: List<ASN1Object> by lazy {
        // Treating SETS the same as sequence
        val subObjects = mutableListOf<ASN1Object>()
        var subOffset = 0
        while (subOffset < encoded.size) {
            val remaining = encoded.range(subOffset, encoded.size)
            val header = remaining.header()

            val range = remaining.range(0, header.totalLength)

            val subObject = range.toAsn1()
            subObjects.add(subObject)
            subOffset += header.totalLength
        }
        subObjects
    }

    override fun toString(): String {
        @Suppress("MagicNumber")
        val name = if (tag == 0x30) "SEQUENCE" else "SET"
        return "$name (${values.size} elem)" + values.joinToString(prefix = "\n", separator = "\n") { it.toString() }.prependIndent("  ")
    }

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = ASN1Sequence(tag, encoded)

        fun create(tag: Int, values: List<ASN1Object>): ASN1Sequence {
            val encoded = values.map {
                it.bytes
            }.joinToByteBuffer()
            return ASN1Sequence(tag, encoded)
        }
    }
}
