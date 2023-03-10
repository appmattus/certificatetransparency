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
        require(toIndex <= size) { "toIndex: $toIndex, size: $size" }
        require(toIndex - fromIndex >= 0) { "$fromIndex > $toIndex" }

        return byteBuffer.copyOfRange(fromIndex + startIndex, toIndex + startIndex)
    }

    override fun range(fromIndex: Int, toIndex: Int): ByteBuffer {
        require(toIndex <= size) { "toIndex: $toIndex, size: $size" }
        require(toIndex - fromIndex >= 0) { "$fromIndex > $toIndex" }

        return BasicByteBuffer(byteBuffer, fromIndex + startIndex, toIndex + startIndex)
    }
}
