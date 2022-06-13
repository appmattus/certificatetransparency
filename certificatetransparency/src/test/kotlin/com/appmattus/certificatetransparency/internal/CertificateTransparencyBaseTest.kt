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

package com.appmattus.certificatetransparency.internal

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleaner
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.loglist.LogListJsonFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.serialization.CTConstants
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyBase
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.TestData.TEST_MITMPROXY_ATTACK_CHAIN
import com.appmattus.certificatetransparency.utils.TestData.TEST_MITMPROXY_ORIGINAL_CHAIN
import com.appmattus.certificatetransparency.utils.TestData.TEST_MITMPROXY_ROOT_CERT
import com.appmattus.certificatetransparency.utils.TrustedSocketFactory
import com.appmattus.certificatetransparency.utils.assertIsA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.X509TrustManager

internal class CertificateTransparencyBaseTest {

    @Test
    fun mitmDisallowedWhenHostChecked() {
        val trustManager = mitmProxyTrustManager()

        val ctb = CertificateTransparencyBase(
            trustManager = trustManager,
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ATTACK_CHAIN)

        assertIsA<VerificationResult.Failure.NoScts>(ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck))
    }

    @Test
    fun mitmAttackAllowedWhenHostNotChecked() {
        val trustManager = mitmProxyTrustManager()

        val ctb = CertificateTransparencyBase(
            excludeHosts = setOf(Host("*.appmattus.com")),
            trustManager = trustManager,
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ATTACK_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck))
    }

    @Test
    fun originalChainAllowedWhenHostChecked() {
        val ctb = CertificateTransparencyBase(
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        val result = ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck)

        require(result is VerificationResult.Success.Trusted)
        assertEquals(2, result.scts.count { it.value is SctVerificationResult.Valid })
    }

    @Test
    fun dataSourceThrowsException() {
        val ctb = CertificateTransparencyBase(
            logListDataSource = object : DataSource<LogListResult> {
                override suspend fun get() = throw InterruptedException()
                override suspend fun set(value: LogListResult) = Unit
            }
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        val result = ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck)
        require(result is VerificationResult.Failure.LogServersFailed)

        val logListResult = result.logListResult
        require(logListResult is LogListJsonFailedLoadingWithException)

        assertTrue(logListResult.exception is InterruptedException)
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun untrustedCertificateThrowsException() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ATTACK_CHAIN)

        ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck)
    }

    @Test
    fun originalChainDisallowedWhenEmptyLogs() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.emptySource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Failure.TooFewSctsTrusted>(ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck))
    }

    @Test
    fun originalChainDisallowedWhenNullLogs() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.nullSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Failure.LogServersFailed>(ctb.verifyCertificateTransparency("www.appmattus.com", certsToCheck))
    }

    @Test
    fun originalChainDisallowedWhenOnlyOneSct() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        val certWithSingleSct = singleSctOnly(certsToCheck.first())

        val filtered = listOf(certWithSingleSct, *certsToCheck.drop(1).toTypedArray())

        assertIsA<VerificationResult.Failure.TooFewSctsTrusted>(ctb.verifyCertificateTransparency("www.appmattus.com", filtered))
    }

    @Test
    fun noCertificatesDisallowed() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.nullSource
        )

        assertIsA<VerificationResult.Failure.NoCertificates>(ctb.verifyCertificateTransparency("www.appmattus.com", emptyList()))
    }

    @Test
    fun includeHostsRuleMatchesSubdomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.random.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("allowed.random.com", certsToCheck))
    }

    @Test
    fun excludeHostsRuleOnlyBlocksSpecifiedSubdomainMatching() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.random.com")),
            excludeHosts = setOf(Host("disallowed.random.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("allowed.random.com", certsToCheck))
    }

    @Test
    fun emptyCleanedCertificateChainFailsWithNoCertificates() {
        val ctb = CertificateTransparencyBase(
            logListDataSource = LogListDataSourceTestFactory.logListDataSource,
            certificateChainCleanerFactory = EmptyCertificateChainCleanerFactory()
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Failure.NoCertificates>(ctb.verifyCertificateTransparency("allowed.random.com", certsToCheck))
    }

    class EmptyCertificateChainCleanerFactory : CertificateChainCleanerFactory {
        override fun get(trustManager: X509TrustManager): CertificateChainCleaner {
            return object : CertificateChainCleaner {
                override fun clean(chain: List<X509Certificate>, hostname: String) = emptyList<X509Certificate>()
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun excludeHostMatchingIncludeNotAllowed() {
        CertificateTransparencyBase(
            includeHosts = setOf(Host("allowed.random.com")),
            excludeHosts = setOf(Host("allowed.random.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )
    }

    private fun mitmProxyTrustManager(): X509TrustManager {
        val rootCerts = TestData.loadCertificates(TEST_MITMPROXY_ROOT_CERT)
        return TrustedSocketFactory().create(rootCerts).trustManager
    }

    private fun singleSctOnly(cert: X509Certificate) = spy(cert).apply {
        whenever(getExtensionValue(CTConstants.SCT_CERTIFICATE_OID)).thenAnswer {
            @Suppress("MaxLineLength")
            Base64.decode("BHwEegB4AHYAu9nfvB+KcbWTlCOXqpJ7RzhXlQqrUugakJZkNo4e0YUAAAFj7ztQ3wAABAMARzBFAiEA53gntK6Dnr6ROwYGBjqjt5dS4tWM6Zw/TtxIxOvobW8CIF3n4XjIX7/w66gThQD47iF7YmxelwgUQgPzEWNlHQiu")
        }
    }

    @Test
    fun defaultVerifiesTransparencyForDomain() {
        val ctb = CertificateTransparencyBase(
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("appmattus.com", certsToCheck))
    }

    @Test
    fun defaultVerifiesTransparencyForSubdomain() {
        val ctb = CertificateTransparencyBase(
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("enabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeDomainIgnoresTransparencyForDomain() {
        val ctb = CertificateTransparencyBase(
            excludeHosts = setOf(Host("appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("appmattus.com", certsToCheck))
    }

    @Test
    fun excludeSpecifiedSubdomainIgnoresTransparencyForSubdomain() {
        val ctb = CertificateTransparencyBase(
            excludeHosts = setOf(Host("disabled.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("disabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllSubdomainsIgnoresTransparencyForSubdomain() {
        val ctb = CertificateTransparencyBase(
            excludeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("disabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllSubdomainsVerifiesTransparencyForDomain() {
        val ctb = CertificateTransparencyBase(
            excludeHosts = setOf(Host("*.appmattus.com")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllSubdomainsAndIncludeSpecifiedSubdomainVerifiesTransparencyForIncludedDomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("enabled.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.appmattus.com")), // don't do ct checks on these domains
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("enabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllSubdomainsAndIncludeSpecifiedSubdomainIgnoresTransparencyForExcludedSubdomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("enabled.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.appmattus.com")), // don't do ct checks on these domains
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("disabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllAndIncludeSpecifiedSubdomainVerifiesTransparencyForIncludedSubdomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("enabled.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.*")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("enabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllAndIncludeSpecifiedSubdomainIgnoresTransparencyForUnspecifiedSubdomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("enabled.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.*")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("disabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllAndIncludeAllSubdomainsVerifiesTransparencyForSubdomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.*")), // don't do ct checks on any domain
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.Trusted>(ctb.verifyCertificateTransparency("enabled.appmattus.com", certsToCheck))
    }

    @Test
    fun excludeAllAndIncludeAllSubdomainsIgnoresTransparencyForDomain() {
        val ctb = CertificateTransparencyBase(
            includeHosts = setOf(Host("*.appmattus.com")), // but do ensure they're done on these
            excludeHosts = setOf(Host("*.*")), // don't do ct checks on any domain
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )

        val certsToCheck = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

        assertIsA<VerificationResult.Success.DisabledForHost>(ctb.verifyCertificateTransparency("appmattus.com", certsToCheck))
    }

    @Test(expected = IllegalArgumentException::class)
    fun includeAllThrowsException() {
        CertificateTransparencyBase(
            includeHosts = setOf(Host("*.*")),
            logListDataSource = LogListDataSourceTestFactory.logListDataSource
        )
    }
}
