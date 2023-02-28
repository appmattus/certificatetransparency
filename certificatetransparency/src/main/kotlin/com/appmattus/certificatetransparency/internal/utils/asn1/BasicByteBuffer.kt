package com.appmattus.certificatetransparency.internal.utils.asn1

internal data class BasicByteBuffer(
    private val byteBuffer: ByteBuffer,
    private val startIndex: Int = 0,
    private val endIndex: Int = byteBuffer.size
) : ByteBuffer {

    override val size: Int
        get() = endIndex - startIndex

    override fun get(index: Int): Byte {
        return byteBuffer[index + startIndex]
    }

    override fun copyOfRange(fromIndex: Int, toIndex: Int): ByteArray {
        if (toIndex > size) throw IndexOutOfBoundsException("toIndex: $toIndex, size: $size")
        if (toIndex - fromIndex < 0) throw IllegalArgumentException("$fromIndex > $toIndex")

        return byteBuffer.copyOfRange(fromIndex + startIndex, toIndex + startIndex)
    }

    override fun range(fromIndex: Int, toIndex: Int): ByteBuffer {
        if (toIndex > size) throw IndexOutOfBoundsException("toIndex: $toIndex, size: $size")
        if (toIndex - fromIndex < 0) throw IllegalArgumentException("$fromIndex > $toIndex")

        return BasicByteBuffer(byteBuffer, fromIndex + startIndex, toIndex + startIndex)
    }
}
