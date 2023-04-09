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

internal class ASN1ObjectIdentifierTest : ASN1BaseTest() {

    @Test
    fun `tc21 OBJECT IDENTIFIER Needlessly long format of SID encoding`() {
        val asn1 = TestData.file("/testdata/asn1/tc21.ber").readBytes().toAsn1()
        assertEquals("OBJECT IDENTIFIER 2.1.1", asn1.toString())
        assertWarnings(
            "Needlessly long format of SID encoding",
            "Needlessly long format of SID encoding"
        )
    }

    // As this implementation uses BigInteger we are able to successfully pass a big INTEGER so no exception is thrown
    @Test
    fun `tc22 OBJECT IDENTIFIER Too big value for SID`() {
        val asn1 = TestData.file("/testdata/asn1/tc22.ber").readBytes().toAsn1()
        assertEquals("OBJECT IDENTIFIER 2.151115727451828646838079.643.2.2.3", asn1.toString())
        assertNoWarnings()
    }

    @Test
    fun `tc23 OBJECT IDENTIFIER Unfinished encoding of SID`() {
        val throwable = Assert.assertThrows(Exception::class.java) {
            val asn1 = TestData.file("/testdata/asn1/tc23.ber").readBytes().toAsn1()
            asn1.toString()
        }

        assertEquals("End of input reached before message was fully decoded", throwable.message)
    }

    @Test
    fun `tc24 OBJECT IDENTIFIER Common encoding of OID`() {
        val asn1 = TestData.file("/testdata/asn1/tc24.ber").readBytes().toAsn1()
        assertEquals("OBJECT IDENTIFIER 2.10000.840.135119.9.2.12301002.12132323.191919.2", asn1.toString())
        assertNoWarnings()
    }
}
