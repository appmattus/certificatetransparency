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

package com.appmattus.certificatetransparency.internal.utils.asn1.header

import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import kotlin.random.Random
import kotlin.random.asJavaRandom

class ASN1HeaderTagTest {

    @Test
    fun twoByteEncoding() {
        val tag = ASN1HeaderTag(TagClass.Application, TagForm.Primitive, 31, 0)
        assertEquals("5f1f", tag.tagBytes.toHexString())
    }

    @Test
    fun encodeSmallTagValues() {
        (0..30).forEach { tagValue ->
            val tag = ASN1HeaderTag(TagClass.Universal, TagForm.Primitive, tagValue, 0)

            val bytes = tag.tagBytes
            assertEquals(1, bytes.size)
            assertEquals(tagValue, bytes[0].toInt() and 0xff)

            val newTag = bytes.toByteBuffer().tag()
            assertEquals(tagValue.toBigInteger(), newTag.tagNumber)
            assertTrue(newTag.isTagNumber(tagValue))
            assertTrue(newTag.isTagNumber(tagValue.toBigInteger()))
        }
    }

    @Test
    fun encodeMediumTagValues() {
        (31..127).forEach { tagValue ->
            val tag = ASN1HeaderTag(TagClass.Universal, TagForm.Primitive, tagValue, 0)

            val bytes = tag.tagBytes
            assertEquals(2, bytes.size)

            val newTag = bytes.toByteBuffer().tag()
            assertEquals(tagValue.toBigInteger(), newTag.tagNumber)
            assertTrue(newTag.isTagNumber(tagValue))
            assertTrue(newTag.isTagNumber(tagValue.toBigInteger()))
        }
    }

    @Test
    fun encodeLargeTagValues() {
        (128..16383).forEach { tagValue ->
            val tag = ASN1HeaderTag(TagClass.Universal, TagForm.Primitive, tagValue, 0)

            val bytes = tag.tagBytes
            assertEquals(3, bytes.size)

            val newTag = bytes.toByteBuffer().tag()
            assertEquals(tagValue.toBigInteger(), newTag.tagNumber)
            assertTrue(newTag.isTagNumber(tagValue))
            assertTrue(newTag.isTagNumber(tagValue.toBigInteger()))
        }
    }

    @Test
    fun encodeXXLargeTagValues() {
        repeat(100) {
            // Given a large tagValue
            val tagValue = BigInteger(Random.nextInt(32, 1024), Random.asJavaRandom())
            val tag = ASN1HeaderTag(TagClass.Universal, TagForm.Primitive, tagValue, 0)

            val bytes = tag.tagBytes

            val newTag = bytes.toByteBuffer().tag()
            assertEquals(tagValue, newTag.tagNumber)
            assertTrue(newTag.isTagNumber(tagValue))
        }
    }
}
