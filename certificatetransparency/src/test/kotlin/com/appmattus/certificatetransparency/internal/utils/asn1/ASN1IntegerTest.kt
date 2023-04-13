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

class ASN1IntegerTest : ASN1BaseTest() {

    @Test
    fun `tc18 INTEGER Needlessly long encoding for INTEGER value`() {
        val asn1 = TestData.file("/testdata/asn1/tc18.ber").readBytes().toAsn1(logger)
        assertEquals("INTEGER -4095", asn1.toString())
        assertWarnings("Needlessly long format")
    }

    @Test
    fun `tc19 INTEGER Never-ending encoding for INTEGER type (non-finished encoding)`() {
        val throwable = Assert.assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc19.ber").readBytes().toAsn1(logger)
            asn1.toString()
        }

        assertEquals("End of input reached before message was fully decoded", throwable.message)
    }

    // As this implementation uses BigInteger we are able to successfully pass a big INTEGER so no exception is thrown
    @Test
    fun `tc20 INTEGER Too big INTEGER number encoded`() {
        val asn1 = TestData.file("/testdata/asn1/tc20.ber").readBytes().toAsn1(logger)
        assertEquals("INTEGER -2361182958856022458111", asn1.toString())
        assertNoWarnings()
    }
}
