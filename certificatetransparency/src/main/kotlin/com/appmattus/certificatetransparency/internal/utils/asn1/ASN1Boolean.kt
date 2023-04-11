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
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import java.util.logging.Logger

internal class ASN1Boolean private constructor(
    override val tag: ASN1HeaderTag,
    override val encoded: ByteBuffer
) : ASN1Object {

    private val logger = Logger.getLogger("ASN1")

    init {
        assert(encoded.size >= 1)
        if (encoded.size > 1) {
            logger.warning("Needlessly long format. BOOLEAN value encoded in more then 1 octet")
        }
    }

    val value: Boolean by lazy {
        try {
            encoded.any { it != 0x00.toByte() }
        } catch (expected: ArrayIndexOutOfBoundsException) {
            throw IllegalStateException("End of input reached before message was fully decoded", expected)
        }
    }

    override fun toString(): String = "BOOLEAN $value"

    companion object {
        fun create(tag: ASN1HeaderTag, encoded: ByteBuffer): ASN1Boolean {
            return ASN1Boolean(tag, encoded)
        }
    }
}
