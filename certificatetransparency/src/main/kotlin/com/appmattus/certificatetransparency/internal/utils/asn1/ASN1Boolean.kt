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

internal class ASN1Boolean private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: Boolean by lazy { encoded[0] != 0x00.toByte() }

    override fun toString(): String = "BOOLEAN $value"

    companion object {
        fun create(tag: Int, encoded: ByteBuffer): ASN1Boolean {
            assert(encoded.size == 1)
            return ASN1Boolean(tag, encoded)
        }
    }
}
