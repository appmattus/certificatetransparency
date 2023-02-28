package com.appmattus.certificatetransparency.internal.utils.asn1

import java.math.BigInteger

internal class ASN1ObjectIdentifier private constructor(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: String by lazy {
        encoded.toObjectIdentifierString()
    }

    override fun toString(): String = "OBJECT IDENTIFIER $value"

    companion object {
        private const val LONG_LIMIT = (Long.MAX_VALUE shr 7) - 0x7f

        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer): ASN1ObjectIdentifier =
            ASN1ObjectIdentifier(tag, totalLength, encoded)

        @Suppress("MagicNumber", "NestedBlockDepth")
        private fun ByteBuffer.toObjectIdentifierString(): String {
            val objId = StringBuffer()
            var value: Long = 0
            var bigValue: BigInteger? = null
            var first = true

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
                    } else {
                        bigValue = bigValue.shiftLeft(7)
                    }
                }
            }

            return objId.toString()
        }
    }
}
