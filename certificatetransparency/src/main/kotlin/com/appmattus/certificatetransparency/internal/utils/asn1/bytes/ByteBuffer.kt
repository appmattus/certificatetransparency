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

package com.appmattus.certificatetransparency.internal.utils.asn1.bytes

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
        for (it in 0 until size) {
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

internal fun List<ByteBuffer>.joinToByteBuffer(): ByteBuffer = ByteBufferArray(this)
