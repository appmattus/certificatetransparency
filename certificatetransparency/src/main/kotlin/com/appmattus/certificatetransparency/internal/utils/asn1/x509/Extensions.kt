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

package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.joinToByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1

internal class Extensions private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val extensions: List<Extension> by lazy { (encoded.toAsn1() as ASN1Sequence).values.map { Extension.create(it as ASN1Sequence) } }

    override fun toString(): String {
        val values = extensions
        return values.joinToString(separator = "\n\n") { it.toString() }
    }

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = Extensions(tag, encoded)

        fun create(extensions: List<Extension>): Extensions {
            val encoded = extensions.map { it.bytes }.joinToByteBuffer()
            return Extensions(0xa3, ASN1Sequence(0x30, encoded).bytes)
        }
    }
}
