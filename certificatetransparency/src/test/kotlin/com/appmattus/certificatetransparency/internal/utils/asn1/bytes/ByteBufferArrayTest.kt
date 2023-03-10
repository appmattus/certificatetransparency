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

import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertEquals
import org.junit.Test

class ByteBufferArrayTest {

    private val byteBuffer =
        listOf("0102030405", "06070809", "0a0b0c0d0e0f").map { it.decodeHex().toByteArray().toByteBuffer() }.joinToByteBuffer()

    @Test
    fun joins() {
        assertEquals("0102030405060708090a0b0c0d0e0f", byteBuffer.toList().toByteArray().toHexString())
    }

    @Test
    fun partialRange() {
        assertEquals("0405060708090a0b0c", byteBuffer.range(3, 12).toList().toByteArray().toHexString())
        assertEquals("0405060708090a0b0c", byteBuffer.copyOfRange(3, 12).toHexString())
    }

    @Test
    fun singleRange() {
        assertEquals("0304", byteBuffer.range(2, 4).toList().toByteArray().toHexString())
        assertEquals("0304", byteBuffer.copyOfRange(2, 4).toHexString())
    }

    @Test
    fun allRange() {
        assertEquals(
            "0102030405060708090a0b0c0d0e0f",
            byteBuffer.range(0, byteBuffer.size).toList().toByteArray().toHexString()
        )
        assertEquals("0102030405060708090a0b0c0d0e0f", byteBuffer.copyOfRange(0, byteBuffer.size).toHexString())
    }

    @Test
    fun emptyRange() {
        assertEquals("", byteBuffer.range(0, 0).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(0, 0).toHexString())

        assertEquals("", byteBuffer.range(3, 3).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(3, 3).toHexString())

        assertEquals("", byteBuffer.range(5, 5).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(5, 5).toHexString())

        assertEquals("", byteBuffer.range(9, 9).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(9, 9).toHexString())

        assertEquals("", byteBuffer.range(11, 11).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(11, 11).toHexString())

        assertEquals("", byteBuffer.range(byteBuffer.size, byteBuffer.size).toList().toByteArray().toHexString())
        assertEquals("", byteBuffer.copyOfRange(byteBuffer.size, byteBuffer.size).toHexString())
    }
}
