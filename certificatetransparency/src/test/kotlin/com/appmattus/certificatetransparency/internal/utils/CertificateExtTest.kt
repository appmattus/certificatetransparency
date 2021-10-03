/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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
 *
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.utils.TestData.PRE_CERT_SIGNING_CERT
import com.appmattus.certificatetransparency.utils.TestData.ROOT_CA_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT
import com.appmattus.certificatetransparency.utils.TestData.loadCertificates
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Make sure the correct info about certificates is provided.  */
internal class CertificateExtTest {
    @Test
    fun correctlyIdentifiesPreCertificateSigningCert() {
        val preCertificateSigningCert = loadCertificates(PRE_CERT_SIGNING_CERT)[0]
        val ordinaryCaCert = loadCertificates(ROOT_CA_CERT)[0]

        assertTrue(preCertificateSigningCert.isPreCertificateSigningCert())
        assertFalse(ordinaryCaCert.isPreCertificateSigningCert())
    }

    @Test
    fun correctlyIdentifiesPreCertificates() {
        val regularCert = loadCertificates(TEST_CERT)[0]
        val preCertificate = loadCertificates(TEST_PRE_CERT)[0]

        assertTrue(preCertificate.isPreCertificate())
        assertFalse(regularCert.isPreCertificate())
    }
}
