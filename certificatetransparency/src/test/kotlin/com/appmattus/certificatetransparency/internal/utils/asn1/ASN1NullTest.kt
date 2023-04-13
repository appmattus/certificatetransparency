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

class ASN1NullTest : ASN1BaseTest() {

    @Test
    fun `tc30 NULL Using of value block with length more than 0 octet`() {
        val asn1 = TestData.file("/testdata/asn1/tc30.ber").readBytes().toAsn1(logger)
        assertEquals("NULL", asn1.toString())
        assertWarnings("Non-zero length of value block for NULL type")
    }

    @Test
    fun `tc31 NULL Unfinished encoding of value block (+ using of value block with length more than 0 octet)`() {
        val throwable = Assert.assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc31.ber").readBytes().toAsn1(logger)
            asn1.toString()
        }

        assertEquals("End of input reached before message was fully decoded", throwable.message)
    }

    @Test
    fun `tc32 NULL Right NULL encoding`() {
        val asn1 = TestData.file("/testdata/asn1/tc32.ber").readBytes().toAsn1(logger)
        assertEquals("NULL", asn1.toString())
        assertNoWarnings()
    }
}
