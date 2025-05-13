/*
 * Copyright 2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleaner
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.TestData.TEST_MITMPROXY_ORIGINAL_CHAIN
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class CertificateTransparencyTrustManagerBasicDelegationTest {

    private val certificateChain = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN).toTypedArray()
    private val brokenCertificateChain = certificateChain.drop(1).toTypedArray()

    private val x509TrustManager = mock<X509TrustManager>(extraInterfaces = arrayOf(AndroidTrustManager::class)) {
        on {
            (this as AndroidTrustManager).checkServerTrusted(
                chain = any(),
                authType = any(),
                host = any()
            )
        } doAnswer { it.getArgument<Array<X509Certificate>>(0).toList() }
    }

    private val subject = CertificateTransparencyTrustManagerBasic(
        delegate = x509TrustManager,
        includeHosts = emptySet(),
        excludeHosts = emptySet(),
        certificateChainCleanerFactory = object : CertificateChainCleanerFactory {
            override fun get(trustManager: X509TrustManager): CertificateChainCleaner {
                return object : CertificateChainCleaner {
                    override fun clean(chain: List<X509Certificate>, hostname: String): List<X509Certificate> = chain
                }
            }
        },
        logListService = null,
        logListDataSource = LogListDataSourceTestFactory.logListDataSource,
        policy = null,
        diskCache = null
    )

    @Test
    fun checkClientTrusted() {
        // When we call checkClientTrusted
        subject.checkClientTrusted(emptyArray(), "AUTH")

        // Then the call is delegated
        verify(x509TrustManager).checkClientTrusted(emptyArray(), "AUTH")
    }

    @Test
    fun checkServerTrustedFailure() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH")
        }

        // Then the call is delegated
        verify(x509TrustManager).checkServerTrusted(brokenCertificateChain, "AUTH")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSuccess() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(
            chain = certificateChain,
            authType = "AUTH",
        )

        // Then the call is delegated
        verify(x509TrustManager).checkServerTrusted(
            certificateChain,
            "AUTH"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedFailureApi17() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH", "host")
        }

        // Then the call is delegated
        verify(x509TrustManager as AndroidTrustManager).checkServerTrusted(brokenCertificateChain, "AUTH", "host")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSuccessApi17() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(
            chain = certificateChain,
            authType = "AUTH",
            host = "host"
        )

        // Then the call is delegated
        verify(x509TrustManager as AndroidTrustManager).checkServerTrusted(
            chain = certificateChain,
            authType = "AUTH",
            host = "host"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun acceptedIssuers() {
        // Given delegate returns accepted issuers
        val issuers = arrayOf(mock<X509Certificate>())
        whenever(x509TrustManager.acceptedIssuers).thenReturn(issuers)

        // When we call acceptedIssuers
        val result = subject.acceptedIssuers

        // Then the call is delegated
        verify(x509TrustManager).acceptedIssuers
        // And the result is the same as the delegate
        assertArrayEquals(issuers, result)
    }
}
