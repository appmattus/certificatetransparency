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
        val result = input.readOctet()

        // Then the result matches the expected output
        assertTrue(octet1Expected.contentEquals(result))
    }

    @Test(expected = IOException::class)
    fun readOctetFails() {
        // Given the input does not start with octet marker (0x04)
        val input = byteArrayOf(0x00, 0x00)

        // When we read the octet
        input.readOctet()

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

    @Test
    fun readOid() {
        val raw = byteArrayOf(0x06, 0x09, 0x2a, 0x86.toByte(), 0x48, 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x01)

        assertEquals("1.2.840.113549.1.1.1", raw.readObjectIdentifier())
    }

    @Test
    fun determineKeyAlgorithm() {
        val input = byteArrayOf(
            0x30, 0x81.toByte(), 0x9f.toByte(), 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86.toByte(), 0x48, 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01,
            0x01, 0x01, 0x05, 0x00, 0x03, 0x81.toByte(), 0x8d.toByte(), 0x00, 0x30, 0x81.toByte(), 0x89.toByte(), 0x02, 0x81.toByte(),
            0x81.toByte(), 0x00, 0x8f.toByte(), 0xe2.toByte(), 0x41, 0x2a, 0x08, 0xe8.toByte(), 0x51, 0xa8.toByte(), 0x8c.toByte(),
            0xb3.toByte(), 0xe8.toByte(), 0x53, 0xe7.toByte(), 0xd5.toByte(), 0x49, 0x50, 0xb3.toByte(), 0x27, 0x8a.toByte(), 0x2b,
            0xcb.toByte(), 0xea.toByte(), 0xb5.toByte(), 0x42, 0x73, 0xea.toByte(), 0x02, 0x57, 0xcc.toByte(), 0x65, 0x33, 0xee.toByte(),
            0x88.toByte(), 0x20, 0x61, 0xa1.toByte(), 0x17, 0x56, 0xc1.toByte(), 0x24, 0x18, 0xe3.toByte(), 0xa8.toByte(), 0x08, 0xd3.toByte(),
            0xbe.toByte(), 0xd9.toByte(), 0x31, 0xf3.toByte(), 0x37, 0x0b, 0x94.toByte(), 0xb8.toByte(), 0xcc.toByte(), 0x43, 0x08, 0x0b, 0x70,
            0x24, 0xf7.toByte(), 0x9c.toByte(), 0xb1.toByte(), 0x8d.toByte(), 0x5d, 0xd6.toByte(), 0x6d, 0x82.toByte(), 0xd0.toByte(), 0x54,
            0x09, 0x84.toByte(), 0xf8.toByte(), 0x9f.toByte(), 0x97.toByte(), 0x01, 0x75, 0x05, 0x9c.toByte(), 0x89.toByte(), 0xd4.toByte(),
            0xd5.toByte(), 0xc9.toByte(), 0x1e, 0xc9.toByte(), 0x13, 0xd7.toByte(), 0x2a, 0x6b, 0x30, 0x91.toByte(), 0x19, 0xd6.toByte(),
            0xd4.toByte(), 0x42, 0xe0.toByte(), 0xc4.toByte(), 0x9d.toByte(), 0x7c, 0x92.toByte(), 0x71, 0xe1.toByte(), 0xb2.toByte(), 0x2f,
            0x5c, 0x8d.toByte(), 0xee.toByte(), 0xf0.toByte(), 0xf1.toByte(), 0x17, 0x1e, 0xd2.toByte(), 0x5f, 0x31, 0x5b, 0xb1.toByte(),
            0x9c.toByte(), 0xbc.toByte(), 0x20, 0x55, 0xbf.toByte(), 0x3a, 0x37, 0x42, 0x45, 0x75, 0xdc.toByte(), 0x90.toByte(), 0x65, 0x02,
            0x03, 0x01, 0x00, 0x01
        )
        println(input.size)

        assertEquals("RSA", PublicKeyFactory.determineKeyAlgorithm(input))
    }

    companion object {
        val input =
            "0481f50481f200f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()
        val octet1Expected =
            "0481f200f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()
        val octet2Expected =
            "00f00076007a328c54d8b72db620ea38e0521ee98416703213854d3bd22bc13a57a352eb520000018601f0b3da0000040300473045022100e123654e2150d7015277bbbc44c561c03c1c485f1253d62e9a37624fe5700fc6022042b665af33881c7c477f87a290e5f14383d0a685d162c58671e0e359d7d2acc7007600b73efb24df9c4dba75f239c5ba58f46c5dfc42cf7a9f35c49e1d098125edb4990000018601f0b3ef0000040300473045022015c4bb6c60faa3b3e05df61d9583c4a9887144c82e2d9a0bccaca63d72c163de022100858e890b8301acbf3a63e1890e954d07672ba195a0d598cd390225cd9970fc76".decodeHex()
                .toByteArray()
    }
}
