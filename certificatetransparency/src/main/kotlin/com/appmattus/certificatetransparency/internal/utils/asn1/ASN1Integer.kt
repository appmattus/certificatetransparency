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
import java.math.BigInteger
import java.util.logging.Logger

internal class ASN1Integer private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    private val logger = Logger.getLogger("ASN1")

    val value: BigInteger by lazy {
        try {
            if (encoded[0] == 0x00.toByte() || encoded[0] == 0xff.toByte()) {
                logger.warning("Needlessly long format")
            }
            BigInteger(encoded.toList().toByteArray())
        } catch (expected: ArrayIndexOutOfBoundsException) {
            throw IllegalStateException("End of input reached before message was fully decoded", expected)
        }
    }

    override fun toString(): String = "INTEGER $value"

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = ASN1Integer(tag, encoded)
    }
}
