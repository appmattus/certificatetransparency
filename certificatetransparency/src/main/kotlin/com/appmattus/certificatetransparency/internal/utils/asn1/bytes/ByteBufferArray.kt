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

internal class ByteBufferArray(val bytes: List<ByteBuffer>) : ByteBuffer {

    override val size: Int by lazy { bytes.sumOf { it.size } }

    override fun get(index: Int): Byte {
        val pos = position(index)
        return bytes[pos.first][pos.second]
    }

    private fun position(index: Int): Pair<Int, Int> {
        var listIndex = 0
        var totalIndex = 0

        if (bytes.isEmpty()) {
            throw IndexOutOfBoundsException("Index $index out of bounds for length 0")
        }

        while (index >= totalIndex + bytes[listIndex].size) {
            totalIndex += bytes[listIndex].size
            listIndex++

            if (listIndex >= bytes.size) {
                throw IndexOutOfBoundsException("Index $index out of bounds for length $size")
            }
        }

        return Pair(listIndex, index - totalIndex)
    }

    override fun copyOfRange(fromIndex: Int, toIndex: Int): ByteArray {
        return range(fromIndex, toIndex).toList().toByteArray()
    }

    override fun range(fromIndex: Int, toIndex: Int): ByteBuffer {
        var destOffset = 0
        var len = toIndex - fromIndex
        var index = fromIndex

        val newBytes = mutableListOf<ByteBuffer>()

        while (len > 0) {
            val pos = position(index)
            val available = bytes[pos.first].size - pos.second

            if (available >= len) {
                // this array contains all the data we need...
                newBytes.add(bytes[pos.first].range(pos.second, pos.second + len))
                len = 0
                destOffset += len
                index += len
            } else {
                // partial bytes
                newBytes.add(bytes[pos.first].range(pos.second, pos.second + available))
                len -= available
                destOffset += available
                index += available
            }
        }

        return ByteBufferArray(newBytes)
    }
}
