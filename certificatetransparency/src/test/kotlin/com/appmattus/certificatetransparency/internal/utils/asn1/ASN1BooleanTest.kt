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

import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class ASN1BooleanTest : ASN1BaseTest() {

    @Test
    fun `tc25 BOOLEAN Length of value block is more than 1 + encoding of FALSE value`() {
        val asn1 = TestData.file("/testdata/asn1/tc25.ber").readBytes().toAsn1(logger)
        assertEquals("BOOLEAN false", asn1.toString())
        assertWarnings("Needlessly long format. BOOLEAN value encoded in more then 1 octet")
    }

    @Test
    fun `tc26 BOOLEAN Length of value block is more than 1 + encoding of TRUE value`() {
        val asn1 = TestData.file("/testdata/asn1/tc26.ber").readBytes().toAsn1(logger)
        assertEquals("BOOLEAN true", asn1.toString())
        assertWarnings("Needlessly long format. BOOLEAN value encoded in more then 1 octet")
    }

    @Test
    fun `tc27 BOOLEAN Absence of value block`() {
        val throwable = Assert.assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc27.ber").readBytes().toAsn1(logger)
            asn1.toString()
        }

        assertEquals("End of input reached before message was fully decoded", throwable.message)
    }

    @Test
    fun `tc28 BOOLEAN Right encoding for TRUE value`() {
        val asn1 = TestData.file("/testdata/asn1/tc28.ber").readBytes().toAsn1(logger)
        assertEquals("BOOLEAN true", asn1.toString())
        assertNoWarnings()
    }

    @Test
    fun `tc29 BOOLEAN Right encoding for FALSE value`() {
        val asn1 = TestData.file("/testdata/asn1/tc29.ber").readBytes().toAsn1(logger)
        assertEquals("BOOLEAN false", asn1.toString())
        assertNoWarnings()
    }
}
