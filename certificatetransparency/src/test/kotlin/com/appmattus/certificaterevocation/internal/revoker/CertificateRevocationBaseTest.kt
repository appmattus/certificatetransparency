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
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificaterevocation.internal.revoker

import com.appmattus.certificaterevocation.RevocationResult
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleaner
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.assertIsA
import org.junit.Test
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class CertificateRevocationBaseTest {

    @Test
    fun chainAllowedWhenNoRevocationsInPlace() {
        val ctb = CertificateRevocationBase()

        val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        assertIsA<RevocationResult.Success.Trusted>(ctb.verifyCertificateRevocation("www.github.com", certsToCheck))
    }

    @Test
    fun chainAllowedWhenRevocationDoesNotMatch() {
        val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        val randomCert = TestData.loadCertificates(TestData.ROOT_CA_CERT)[0]
        val ctb = CertificateRevocationBase(
            crlSet = setOf(CrlItem(randomCert.issuerX500Principal, listOf(randomCert.serialNumber)))
        )

        assertIsA<RevocationResult.Success.Trusted>(ctb.verifyCertificateRevocation("www.github.com", certsToCheck))
    }

    @Test
    fun chainRejectedWhenFirstCertMatches() {
        val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        val ctb = CertificateRevocationBase(
            crlSet = setOf(CrlItem(certsToCheck[0].issuerX500Principal, listOf(certsToCheck[0].serialNumber)))
        )

        assertIsA<RevocationResult.Failure.CertificateRevoked>(ctb.verifyCertificateRevocation("www.github.com", certsToCheck))
    }

    @Test
    fun noCertificatesDisallowed() {
        val ctb = CertificateRevocationBase()

        assertIsA<RevocationResult.Failure.NoCertificates>(ctb.verifyCertificateRevocation("www.github.com", emptyList()))
    }

    @Test
    fun chainRejectedWhenLastCertMatches() {
        val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        val ctb = CertificateRevocationBase(
            crlSet = setOf(CrlItem(certsToCheck[1].issuerX500Principal, listOf(certsToCheck[1].serialNumber)))
        )

        assertIsA<RevocationResult.Failure.CertificateRevoked>(ctb.verifyCertificateRevocation("www.github.com", certsToCheck))
    }

    @Test
    fun emptyCleanedCertificateChainFailsWithNoCertificates() {
        val certsToCheck = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        val ctb = CertificateRevocationBase(
            crlSet = setOf(CrlItem(certsToCheck[0].issuerX500Principal, listOf(certsToCheck[0].serialNumber))),
            certificateChainCleanerFactory = EmptyCertificateChainCleanerFactory()
        )

        assertIsA<RevocationResult.Failure.NoCertificates>(ctb.verifyCertificateRevocation("www.github.com", certsToCheck))
    }

    class EmptyCertificateChainCleanerFactory : CertificateChainCleanerFactory {
        override fun get(trustManager: X509TrustManager): CertificateChainCleaner {
            return object : CertificateChainCleaner {
                override fun clean(chain: List<X509Certificate>, hostname: String) = emptyList<X509Certificate>()
            }
        }
    }
}
