package com.appmattus.certificatetransparency.internal.utils.asn1

import java.math.BigInteger

internal interface ASN1Object {
    val tag: Int
    val encoded: ByteBuffer

    @Suppress("MagicNumber")
    val lengthBytes: ByteArray
        get() = if (encoded.size <= 0x7f) {
            byteArrayOf(encoded.size.toByte())
        } else {
            val bytes = BigInteger.valueOf(encoded.size.toLong()).toByteArray()

            byteArrayOf((0x80 + bytes.size).toByte()) + bytes
        }

    val totalLength: Int
        get() = encoded.size + lengthBytes.size + 1
}
