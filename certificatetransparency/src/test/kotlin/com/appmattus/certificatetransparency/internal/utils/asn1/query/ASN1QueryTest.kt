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

package com.appmattus.certificatetransparency.internal.utils.asn1.query

import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1
import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ASN1QueryTest {

    @Test
    fun queryFirstOrNullFalse() {
        val result = certsToCheck.first().query {
            seq().firstOrNull { false }
        }

        assertNull(result)
    }

    @Test
    fun queryFirstOid() {
        val result = certsToCheck.first().query {
            seq().first().seq().first().seq().first().oid()
        }

        assertEquals("2.5.4.15", result)
    }

    @Test
    fun queryLastOid() {
        val result = certsToCheck.first().query {
            seq().last().seq().first().seq().first().oid()
        }

        assertEquals(commonNameOid, result)
    }

    @Test
    fun queryOidValue() {
        val result = certsToCheck.map { cert ->
            cert.query {
                seq().firstOrNull { it.seq().first().seq().first().oid() == commonNameOid }?.seq()?.first()?.seq()?.get(1)?.string()
            }
        }

        assertEquals(listOf("github.com", "DigiCert SHA2 Extended Validation Server CA"), result)
    }

    @Test
    fun queryOptionalOidValue() {
        val result = certsToCheck.map { cert ->
            cert.query {
                seq().firstOrNull { it.seq().first().seq().first().oid() == serialNumberOid }?.seq()?.first()?.seq()?.get(1)?.string()
            }
        }

        assertEquals(listOf("5157550", null), result)
    }

    companion object {
        private val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN).map { it.subjectX500Principal.encoded.toAsn1() }

        private const val commonNameOid = "2.5.4.3"
        private const val serialNumberOid = "2.5.4.5"
    }
}
