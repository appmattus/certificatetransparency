package com.appmattus.certificatetransparency.internal.utils.asn1

internal interface ByteBuffer : Iterable<Byte> {

    val size: Int

    /**
     * Returns the array element at the given [index].  This method can be called using the index operator.
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    operator fun get(index: Int): Byte

    /** Creates an iterator over the elements of the array. */
    override fun iterator(): Iterator<Byte> = iterator {
        (0 until size).forEach {
            yield(get(it))
        }
    }

    fun copyOfRange(fromIndex: Int, toIndex: Int): ByteArray

    fun range(fromIndex: Int, toIndex: Int): ByteBuffer
}

internal fun ByteArray.toByteBuffer() = object : ByteBuffer {
    override val size: Int
        get() = this@toByteBuffer.size

    override fun get(index: Int): Byte = this@toByteBuffer[index]

    override fun copyOfRange(fromIndex: Int, toIndex: Int): ByteArray =
        this@toByteBuffer.copyOfRange(fromIndex, toIndex)

    override fun range(fromIndex: Int, toIndex: Int): ByteBuffer = BasicByteBuffer(this, fromIndex, toIndex)
}
