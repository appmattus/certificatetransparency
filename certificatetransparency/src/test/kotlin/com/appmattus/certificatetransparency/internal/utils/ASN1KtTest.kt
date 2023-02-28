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

package com.appmattus.certificatetransparency.internal.utils

import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ASN1KtTest {

    @Test
    fun readOctet() {
        // Given the input

        // When we read the octet
        val result = input.readNestedOctets(1)

        // Then the result matches the expected output
        assertTrue(octet1Expected.contentEquals(result))
    }

    @Test(expected = IOException::class)
    fun readOctetFails() {
        // Given the input does not start with octet marker (0x04)
        val input = byteArrayOf(0x00, 0x00)

        // When we read the octet
        input.readNestedOctets(1)

        // Then an exception is thrown
    }

    @Test
    fun readNestedOctets() {
        // Given the input

        // When we read 2 octets deep
        val result = input.readNestedOctets(2)

        // Then the result matches the expected output
        assertTrue(octet2Expected.contentEquals(result))
    }

    @Test
    fun readLength00() {
        // Given a byte stream representing an octet length
        val input = byteArrayOf(0x00).inputStream()

        // When we call readLength
        val result = input.readLength()

        // Then the result matches the expected output
        assertEquals(0, result)
    }

    @Test
    fun readLength02() {
        // Given a byte stream representing an octet length
        val input = byteArrayOf(0x02).inputStream()

        // When we call readLength
        val result = input.readLength()

        // Then the result matches the expected output
        assertEquals(2, result)
    }

    @Test
    fun readLength7f() {
        // Given a byte stream representing an octet length
        val input = byteArrayOf(0x7f).inputStream()

        // When we call readLength
        val result = input.readLength()

        // Then the result matches the expected output
        assertEquals(127, result)
    }

    @Test
    fun readLength8180() {
        // Given a byte stream representing an octet length
        val input = byteArrayOf(0x81.toByte(), 0x80.toByte()).inputStream()

        // When we call readLength
        val result = input.readLength()

        // Then the result matches the expected output
        assertEquals(128, result)
    }

    @Test
    fun readLength820101() {
        // Given a byte stream representing an octet length
        val input = byteArrayOf(0x82.toByte(), 0x01, 0x01).inputStream()

        // When we call readLength
        val result = input.readLength()

        // Then the result matches the expected output
        assertEquals(257, result)
    }

    companion object {
        // Ignoring as this is test data
        @Suppress("MaxLineLength")
        val input =
            "0481f50481f200f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()

        // Ignoring as this is test data
        @Suppress("MaxLineLength")
        val octet1Expected =
            "0481f200f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()

        // Ignoring as this is test data
        @Suppress("MaxLineLength")
        val octet2Expected =
            "00f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()
    }
}
