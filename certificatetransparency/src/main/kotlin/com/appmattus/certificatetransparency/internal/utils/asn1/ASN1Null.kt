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

internal class ASN1Null private constructor(
    override val tag: ASN1HeaderTag,
    override val totalLength: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    private val logger = Logger.getLogger("ASN1")

    init {
        assert(encoded.size >= 0)
        if (encoded.size > 0) {
            logger.warning("Non-zero length of value block for NULL type")
        }
    }

    val value: Unit by lazy {
        try {
            @Suppress("UNUSED_EXPRESSION")
            encoded.forEach { it }
        } catch (expected: ArrayIndexOutOfBoundsException) {
            throw IllegalStateException("End of input reached before message was fully decoded", expected)
        }
    }

    override fun toString(): String = "NULL".also { value }

    companion object {
        fun create(tag: ASN1HeaderTag, totalLength: Int, encoded: ByteBuffer): ASN1Null {
            return ASN1Null(tag, totalLength, encoded)
        }
    }
}
