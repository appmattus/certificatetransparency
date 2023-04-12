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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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
}
