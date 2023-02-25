package com.appmattus.certificatetransparency.internal.utils

import java.io.IOException
import java.io.InputStream
import java.math.BigInteger

private const val LONG_LIMIT = (Long.MAX_VALUE shr 7) - 0x7f

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

internal fun ByteArray.readSequence(): ByteArray {
    val stream = inputStream()

    if (stream.read() != 0x30) throw IOException("Not a sequency")
    val length: Int = stream.readLength()!!

    val result = ByteArray(length)
    val readLength = stream.read(result, 0, length)
    if (readLength != length) throw IOException("Bytes don't match expected size")
    return result
}

internal fun ByteArray.readObjectIdentifier(): String {
    val stream = inputStream()
    val tag = stream.read()
    if (tag != 0x06) {
        throw IllegalStateException("Unexpected tag $tag")
    }
    val length = stream.readLength()!!
    val bytes = ByteArray(length)
    stream.read(bytes)

    return bytes.toObjectIdentifierString()
}

private fun ByteArray.toObjectIdentifierString(): String {
    val objId = StringBuffer()
    var value: Long = 0
    var bigValue: BigInteger? = null
    var first = true

    for (i in indices) {
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
