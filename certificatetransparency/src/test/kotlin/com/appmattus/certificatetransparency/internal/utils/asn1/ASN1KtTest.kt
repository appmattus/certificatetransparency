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

package com.appmattus.certificatetransparency.internal.utils.asn1

import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Certificate
import com.appmattus.certificatetransparency.utils.TestData
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ASN1KtTest : ASN1BaseTest() {

    /**
     * In an earlier implementation this test would fail when reading the extensions because of
     * the header code being incorrect. The test is here to prove the the fix works.
     */
    @Test
    fun testBloomberg() {
        val cert = TestData.loadCertificates("/testdata/bloomberg.pem")[0]
        Certificate.create(cert.encoded).toString()
    }

    // The library passes the tag number to a BigInteger so has no issues with large tag numbers
    @Test
    fun `tc1 COMMON Too big tag number`() {
        val asn1 = TestData.file("/testdata/asn1/tc1.ber").readBytes().toAsn1()
        assertEquals("UNSPECIFIED(1180591620717411303423) 0x40", asn1.toString())
    }

    @Test
    fun `tc2 COMMON Never-ending tag number (non-finished encoding of tag number)`() {
        val throwable = assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc2.ber").readBytes().toAsn1()
            asn1.toString()
        }

        assertEquals("End of input reached before message was fully decoded", throwable.message)
    }

    @Test
    fun `tc3 COMMON Absence of standard length block`() {
        val throwable = assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc3.ber").readBytes().toAsn1()
            asn1.toString()
        }

        assertEquals("No length block encoded", throwable.message)
    }

    @Test
    fun `tc4 COMMON 0xFF value as standard length block`() {
        val throwable = assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc4.ber").readBytes().toAsn1()
            asn1.toString()
        }

        assertEquals("Length block 0xFF is reserved by standard", throwable.message)
    }

    @Test
    fun `tc5 COMMON Unnecessary usage of long length form (length value is less then 127, but long form of length encoding is used)`() {
        val asn1 = TestData.file("/testdata/asn1/tc5.ber").readBytes().toAsn1()
        asn1.toString()
        assertWarnings("Unnecessary usage of long length form")
    }

    @Test
    fun readOctet() {
        // Given the input

        // When we read the octet
        val result = input.readNestedOctets(1)

        // Then the result matches the expected output
        assertTrue(octet1Expected.contentEquals(result))
    }

    @Test(expected = IllegalStateException::class)
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
