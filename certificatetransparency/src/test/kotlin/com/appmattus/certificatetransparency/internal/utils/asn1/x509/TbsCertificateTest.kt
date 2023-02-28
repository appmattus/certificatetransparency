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

package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class TbsCertificateTest {
    @Test
    fun bytesDecodes() {
        // Given extensions from a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN).first()

        // When we extract the TbsCertificate
        val tbsCertificate = Certificate.create(certificate.encoded).tbsCertificate

        // Then the bytes match expected
        val expected = org.bouncycastle.asn1.x509.Certificate.getInstance(certificate.encoded).tbsCertificate.encoded.toHexString()
        assertEquals(expected, tbsCertificate.bytes.toHexString())
    }

    @Test
    fun copy() {
        // Given extensions from a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN).first()

        // When we copy the TbsCertificate
        val tbsCertificate = Certificate.create(certificate.encoded).tbsCertificate.copy()

        // Then the bytes match expected
        val expected = org.bouncycastle.asn1.x509.Certificate.getInstance(certificate.encoded).tbsCertificate.encoded.toHexString()
        assertEquals(expected, tbsCertificate.bytes.toHexString())
    }

    @Test
    fun copyReplacingExtensions() {
        // Given extensions from a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN).first()

        // When we copy the TbsCertificate and create a new extension block
        val tbsCertificate = Certificate.create(certificate.encoded).tbsCertificate
        val result = tbsCertificate.copy(extensions = Extensions.create(tbsCertificate.extensions!!.extensions))

        // Then the bytes match expected
        val expected = org.bouncycastle.asn1.x509.Certificate.getInstance(certificate.encoded).tbsCertificate.encoded.toHexString()
        assertEquals(expected, result.bytes.toHexString())
    }
}
