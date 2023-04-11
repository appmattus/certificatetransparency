package com.appmattus.certificatetransparency.internal.utils.asn1

import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.header.TagClass
import com.appmattus.certificatetransparency.internal.utils.asn1.header.TagForm
import org.junit.Assert.assertEquals
import org.junit.Test

class ASN1ObjectTest {

    @Test
    fun verifyLength0() {
        // Given an object of length 0
        val obj = createASN1Object(0)

        // Then we expect header to be size 2
        assertEquals(2 + 0, obj.totalLength)
        // And a single byte of 0x00 for size
        assertEquals("a000", obj.bytes.toHexString())
    }

    @Test
    fun verifyLength1() {
        // Given an object of length 1
        val obj = createASN1Object(1)

        // Then we expect header to be size 2
        assertEquals(2 + 1, obj.totalLength)
        // And a single byte of 0x01 for size
        assertEquals("a00100", obj.bytes.toHexString())
    }

    @Test
    fun verifyLength0x7f() {
        // Given an object of length 1
        val obj = createASN1Object(0x7f)

        // Then we expect header to be size 2
        assertEquals(2 + 0x7f, obj.totalLength)
        // And a single byte of 0x7f for size
        assertEquals("a07f00", obj.bytes.range(0, 3).toHexString())
    }

    @Test
    fun verifyLength0x80() {
        // Given an object of length 1
        val obj = createASN1Object(0x80)

        // Then we expect header to be size 3
        assertEquals(3 + 0x80, obj.totalLength)
        // And a single byte of 0x7f for size
        assertEquals("a08180", obj.bytes.range(0, 3).toHexString())
    }

    @Test
    fun verifyLength159() {
        // Given an object of length 159
        val obj = createASN1Object(159)

        // Then we expect header to be size 3
        assertEquals(3 + 159, obj.totalLength)
        assertEquals("a0819f", obj.bytes.range(0, 3).toHexString())
    }

    @Test
    fun verifyLength256() {
        // Given an object of length 159
        val obj = createASN1Object(256)

        // Then we expect header to be size 4
        assertEquals(4 + 256, obj.totalLength)
        assertEquals("a0820100", obj.bytes.range(0, 4).toHexString())
    }

    private fun createASN1Object(length: Int) = object : ASN1Object {
        override val tag: ASN1HeaderTag
            get() = ASN1HeaderTag(TagClass.ContextSpecific, TagForm.Constructed, 0x00, 1)

        override val encoded: ByteBuffer
            get() = ByteArray(length).toByteBuffer()
    }
}
