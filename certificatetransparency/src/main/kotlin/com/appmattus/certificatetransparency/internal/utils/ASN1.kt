@file:Suppress("MagicNumber", "ComplexCondition", "NestedBlockDepth")

package com.appmattus.certificatetransparency.internal.utils

import java.io.IOException
import java.io.InputStream

internal fun ByteArray.readOctet(): ByteArray {
    return readNestedOctets(1)
}

internal fun ByteArray.readNestedOctets(count: Int): ByteArray {
    val stream = inputStream()
    var length = 0
    repeat(count) {
        if (stream.read() != 0x04) throw IOException("Not an octet string")
        length = stream.readLength()!!
    }
    val result = ByteArray(length)
    val readLength = stream.read(result, 0, length)
    if (readLength != length) throw IOException("Bytes don't match expected size")
    return result
}

internal fun InputStream.readLength(): Int? {
    val firstByte = read() and 0xff

    return if (firstByte <= 0x7f) {
        firstByte
    } else if (firstByte == 0x80) {
        null
    } else {
        val length = firstByte and 0x7f

        var value = 0
        repeat(length) {
            value = value shl 8
            value += read() and 0xff
        }

        value
    }
}
