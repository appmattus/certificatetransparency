package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.nio.charset.Charset
import kotlin.random.Random

@RunWith(Enclosed::class)
class Base64Test {

    class Single {
        @Test
        fun testPowerOf2() {
            val orig1024 = Random.nextBytes(1024)
            val orig2048 = Random.nextBytes(2048)
            val orig4096 = Random.nextBytes(4096)
            val orig8192 = Random.nextBytes(8192)

            val enc1024 = Base64.toBase64String(orig1024)
            val enc2048 = Base64.toBase64String(orig2048)
            val enc4096 = Base64.toBase64String(orig4096)
            val enc8192 = Base64.toBase64String(orig8192)

            val dec1024 = Base64.decode(enc1024)
            val dec2048 = Base64.decode(enc2048)
            val dec4096 = Base64.decode(enc4096)
            val dec8192 = Base64.decode(enc8192)

            assertTrue(orig1024.contentEquals(dec1024))
            assertTrue(orig2048.contentEquals(dec2048))
            assertTrue(orig4096.contentEquals(dec4096))
            assertTrue(orig8192.contentEquals(dec8192))
        }

        @Test
        fun testPowerOf2Plus1() {
            val orig1025 = Random.nextBytes(1025)
            val orig2049 = Random.nextBytes(2049)
            val orig4097 = Random.nextBytes(4097)
            val orig8193 = Random.nextBytes(8193)

            val enc1025 = Base64.toBase64String(orig1025)
            val enc2049 = Base64.toBase64String(orig2049)
            val enc4097 = Base64.toBase64String(orig4097)
            val enc8193 = Base64.toBase64String(orig8193)

            val dec1025 = Base64.decode(enc1025)
            val dec2049 = Base64.decode(enc2049)
            val dec4097 = Base64.decode(enc4097)
            val dec8193 = Base64.decode(enc8193)

            assertTrue(orig1025.contentEquals(dec1025))
            assertTrue(orig2049.contentEquals(dec2049))
            assertTrue(orig4097.contentEquals(dec4097))
            assertTrue(orig8193.contentEquals(dec8193))
        }

        @Test
        fun testBinaryDecode() {
            val bytes = byteArrayOf(
                0xff.toByte(), 0xee.toByte(), 0xdd.toByte(),
                0xcc.toByte(), 0xbb.toByte(), 0xaa.toByte(),
                0x99.toByte(), 0x88.toByte(), 0x77.toByte()
            )

            assertEquals("", Base64.toBase64String(bytes.copyOf(0)))
            assertEquals("/w==", Base64.toBase64String(bytes.copyOf(1)))
            assertEquals("/+4=", Base64.toBase64String(bytes.copyOf(2)))
            assertEquals("/+7d", Base64.toBase64String(bytes.copyOf(3)))
            assertEquals("/+7dzA==", Base64.toBase64String(bytes.copyOf(4)))
            assertEquals("/+7dzLs=", Base64.toBase64String(bytes.copyOf(5)))
            assertEquals("/+7dzLuq", Base64.toBase64String(bytes.copyOf(6)))
            assertEquals("/+7dzLuqmQ==", Base64.toBase64String(bytes.copyOf(7)))
            assertEquals("/+7dzLuqmYg=", Base64.toBase64String(bytes.copyOf(8)))
        }

        @Test
        fun encodeDecodeSmall() {
            val input = TestData.file("/testdata/base64/small").readBytes()
            val expected = TestData.file("/testdata/base64/small.64").readBytes().toString(Charset.forName("US-ASCII")).replace("\n", "")

            val enc = Base64.toBase64String(input)
            assertEquals(expected, enc)

            val dec = Base64.decode(enc)
            assertTrue(input.contentEquals(dec))
        }

        @Test
        fun encodeDecodeMedium() {
            val input = TestData.file("/testdata/base64/medium").readBytes()
            val expected = TestData.file("/testdata/base64/medium.64").readBytes().toString(Charset.forName("US-ASCII")).replace("\n", "")

            val enc = Base64.toBase64String(input)
            assertEquals(expected, enc)

            val dec = Base64.decode(enc)
            assertTrue(input.contentEquals(dec))
        }
    }

    @RunWith(Parameterized::class)
    class Parameterised {
        @Parameterized.Parameter(0)
        lateinit var input: String

        @Parameterized.Parameter(1)
        lateinit var expected: String

        @Test
        fun encode() {
            val enc = Base64.toBase64String(input.toByteArray(Charset.forName("US-ASCII")))
            assertEquals(expected, enc)
        }

        @Test
        fun decodePadding() {
            val dec = Base64.decode(expected).toString(Charset.forName("US-ASCII"))
            assertEquals(input, dec)
        }

        @Test
        fun decodeNoPadding() {
            val decNoPadding = Base64.decode(expected.trimEnd('=')).toString(Charset.forName("US-ASCII"))
            assertEquals(input, decNoPadding)
        }

        companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0} -> {1}")
            fun data() = arrayOf(
                arrayOf("", ""),
                arrayOf("f", "Zg=="),
                arrayOf("fo", "Zm8="),
                arrayOf("foo", "Zm9v"),
                arrayOf("foob", "Zm9vYg=="),
                arrayOf("fooba", "Zm9vYmE="),
                arrayOf("foobar", "Zm9vYmFy"),

                arrayOf("a", "YQ=="),
                arrayOf("ab", "YWI="),
                arrayOf("abc", "YWJj"),
                arrayOf("abcd", "YWJjZA=="),

                arrayOf("A", "QQ=="),
                arrayOf("AB", "QUI="),
                arrayOf("ABC", "QUJD"),
                arrayOf("ABCD", "QUJDRA=="),
                arrayOf("JIHGFEDCBA", "SklIR0ZFRENCQQ=="),

                arrayOf("Hello World", "SGVsbG8gV29ybGQ="),
                arrayOf("Base64 Encode", "QmFzZTY0IEVuY29kZQ=="),
                arrayOf("hello, world", "aGVsbG8sIHdvcmxk"),
                arrayOf("hello, world?!", "aGVsbG8sIHdvcmxkPyE="),
                arrayOf("hello, world.", "aGVsbG8sIHdvcmxkLg==")
            )
        }
    }

    @RunWith(Parameterized::class)
    class DecodeOnly {
        @Parameterized.Parameter(0)
        lateinit var input: String

        @Test
        fun decodeFailure() {
            assertThrows(Exception::class.java) {
                val dec = Base64.decode(input).toString(Charset.forName("US-ASCII"))
                println("Unexpected result: $dec")
            }
        }

        companion object {

            @JvmStatic
            @Parameterized.Parameters(name = "{0} -> {1}")
            fun data() = arrayOf(
                // Equal only
                arrayOf("===="),
                // invalid letters
                arrayOf("4rdHFh\\%2BHYoS8oLdVvbUzEVqB8Lvm7kSPnuwF0AAABYQ\\%3D"),

                // padding 0
                arrayOf("aGVsbG8sIHdvcmxk="),
                arrayOf("aGVsbG8sIHdvcmxk=="),
                arrayOf("aGVsbG8sIHdvcmxk ="),
                arrayOf("aGVsbG8sIHdvcmxk = = "),

                // padding 1
                arrayOf("aGVsbG8sIHdvcmxkPyE=="),
                arrayOf("aGVsbG8sIHdvcmxkPyE =="),
                arrayOf("aGVsbG8sIHdvcmxkPyE = = "),

                // padding 2
                arrayOf("aGVsbG8sIHdvcmxkLg="),
                arrayOf("aGVsbG8sIHdvcmxkLg ="),
                arrayOf("aGVsbG8sIHdvcmxkLg = ")
            )
        }
    }
}
