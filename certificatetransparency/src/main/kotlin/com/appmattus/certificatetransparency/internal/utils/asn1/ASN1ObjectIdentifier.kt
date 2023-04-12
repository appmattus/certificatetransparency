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
import java.math.BigInteger
import java.util.logging.Logger

internal class ASN1ObjectIdentifier private constructor(
    override val tag: ASN1HeaderTag,
    override val encoded: ByteBuffer
) : ASN1Object() {

    private val logger = Logger.getLogger("ASN1")

    val value: String by lazy {
        try {
            encoded.toObjectIdentifierString()
        } catch (expected: ArrayIndexOutOfBoundsException) {
            throw IllegalStateException("End of input reached before message was fully decoded", expected)
        }
    }

    @Suppress("MagicNumber", "NestedBlockDepth")
    private fun ByteBuffer.toObjectIdentifierString(): String {
        val objId = StringBuilder()
        var value: Long = 0
        var bigValue: BigInteger? = null
        var first = true

        checkSidEncoding(0)

        for (i in 0 until size) {
            val b: Int = this[i].toInt() and 0xff
            if (value <= LONG_LIMIT) {
                value += (b and 0x7F).toLong()
                if (b and 0x80 == 0) {
                    if (first) {
                        if (value < 40) {
                            objId.append('0')
                        } else if (value < 80) {
                            objId.append('1')
                            value -= 40
                        } else {
                            objId.append('2')
                            value -= 80
                        }
                        first = false
                    }
                    objId.append('.')
                    objId.append(value)
                    value = 0

                    checkSidEncoding(i + 1)
                } else {
                    value = value shl 7
                }
            } else {
                if (bigValue == null) {
                    bigValue = BigInteger.valueOf(value)
                }
                bigValue = bigValue!!.or(BigInteger.valueOf((b and 0x7F).toLong()))
                if (b and 0x80 == 0) {
                    if (first) {
                        objId.append('2')
                        bigValue = bigValue.subtract(BigInteger.valueOf(80))
                        first = false
                    }
                    objId.append('.')
                    objId.append(bigValue)
                    bigValue = null
                    value = 0

                    checkSidEncoding(i + 1)
                } else {
                    bigValue = bigValue.shiftLeft(7)
                }
            }
        }

        return objId.toString()
    }

    @Suppress("MagicNumber")
    private fun ByteBuffer.checkSidEncoding(i: Int) {
        if (i + 1 < size && this[i].toInt() and 0xff == 0x80 && this[i + 1].toInt() and 0xff == 0x80) {
            logger.warning("Needlessly long format of SID encoding")
        }
    }

    override fun toString(): String = "OBJECT IDENTIFIER $value"

    companion object {
        private const val LONG_LIMIT = (Long.MAX_VALUE shr 7) - 0x7f

        fun create(tag: ASN1HeaderTag, encoded: ByteBuffer) = ASN1ObjectIdentifier(tag, encoded)
    }
}
