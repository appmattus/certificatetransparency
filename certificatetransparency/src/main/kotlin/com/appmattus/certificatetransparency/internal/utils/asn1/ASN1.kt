package com.appmattus.certificatetransparency.internal.utils.asn1

internal fun ByteArray.toAsn1(): ASN1Object = toByteBuffer().toAsn1()

@Suppress("MagicNumber")
internal fun ByteBuffer.toAsn1(): ASN1Object {
    val tag = this[0].toInt() and 0xff
    var length = this[1].toInt() and 0xff
    var offset = 2
    if (length >= 0x7f) {
        val numLengthBytes = length and 0x7f
        length = 0
        repeat(numLengthBytes) { index ->
            length = length shl 8
            length += this[offset + index].toInt() and 0xff
        }
        offset += numLengthBytes
    }

    val encoded = this.range(offset, offset + length)

    val totalLength = offset + length

    return when (tag) {
        0x02 -> ASN1Integer.create(tag, totalLength, encoded)
        0x03 -> ASN1BitString.create(tag, totalLength, encoded)
        0x05 -> ASN1Null.create(tag, totalLength, encoded)
        0x06 -> ASN1ObjectIdentifier.create(tag, totalLength, encoded)
        0x13 -> ASN1PrintableString.create(tag, totalLength, encoded)
        0x17 -> ASN1Time.create(tag, totalLength, encoded)
        0x30, 0x31 -> ASN1Sequence.create(tag, totalLength, encoded)
        0xa0 -> ASN1Version.create(tag, totalLength, encoded)
        0xa3 -> ASN1Extensions.create(tag, totalLength, encoded)
        else -> ASN1Unspecified.create(tag, totalLength, encoded)
    }
}
