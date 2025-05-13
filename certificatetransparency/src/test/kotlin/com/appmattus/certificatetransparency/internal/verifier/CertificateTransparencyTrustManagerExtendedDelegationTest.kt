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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.InetSocketAddress
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

class CertificateTransparencyTrustManagerExtendedDelegationTest {

    private val mockSocketAddress = mock<InetSocketAddress> { on { hostName } doReturn "host" }
    private val mockSocket = mock<Socket> {
        on { remoteSocketAddress } doReturn mockSocketAddress
    }

    private val mockSslEngine = mock<SSLEngine> {
        on { peerHost } doReturn "host"
    }

    private val certificateChain = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN).toTypedArray()
    private val brokenCertificateChain = certificateChain.drop(1).toTypedArray()

    // Called through reflection by X509TrustManagerExtensions on Android
    interface AndroidTrustManager {
        // Added in API level 17
        fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, host: String): List<X509Certificate>

        // Added in API level 36
        fun checkServerTrusted(
            chain: Array<out X509Certificate>,
            ocspData: ByteArray?,
            tlsSctData: ByteArray?,
            authType: String,
            host: String
        ): List<X509Certificate>

        // Added in API level 28
        fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean

        // Added in API level 21
        fun isUserAddedCertificate(cert: X509Certificate): Boolean
    }

    private val x509ExtendedTrustManager = mock<X509ExtendedTrustManager>(extraInterfaces = arrayOf(AndroidTrustManager::class)) {
        on {
            (this as AndroidTrustManager).checkServerTrusted(
                chain = any(),
                authType = any(),
                host = any()
            )
        } doAnswer { it.getArgument<Array<X509Certificate>>(0).toList() }

        on {
            (this as AndroidTrustManager).checkServerTrusted(
                chain = any(),
                ocspData = anyOrNull(),
                tlsSctData = anyOrNull(),
                authType = any(),
                host = any()
            )
        } doAnswer { it.getArgument<Array<X509Certificate>>(0).toList() }
    }

    private val subject = CertificateTransparencyTrustManagerExtended(
        delegate = x509ExtendedTrustManager,
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
        verify(x509ExtendedTrustManager).checkClientTrusted(emptyArray(), "AUTH")
    }

    @Test
    fun checkClientTrustedSocket() {
        // When we call checkClientTrusted
        subject.checkClientTrusted(emptyArray(), "AUTH", mock<Socket>())

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkClientTrusted(eq(emptyArray()), eq("AUTH"), any<Socket>())
    }

    @Test
    fun checkClientTrustedSslEngine() {
        // When we call checkClientTrusted
        subject.checkClientTrusted(emptyArray(), "AUTH", mock<SSLEngine>())

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkClientTrusted(eq(emptyArray()), eq("AUTH"), any<SSLEngine>())
    }

    @Test
    fun isSameTrustConfiguration() {
        // When we call isSameTrustConfiguration
        subject.isSameTrustConfiguration("host1", "host2")

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).isSameTrustConfiguration("host1", "host2")
    }

    @Test
    fun isUserAddedCertificate() {
        // When we call isUserAddedCertificate
        subject.isUserAddedCertificate(mock())

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).isUserAddedCertificate(any())
    }

    @Test
    fun checkServerTrustedFailure() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH")
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(brokenCertificateChain, "AUTH")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSuccess() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(certificateChain, "AUTH")

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(certificateChain, "AUTH")
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedSocketFailure() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH", mockSocket)
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(brokenCertificateChain, "AUTH", mockSocket)
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSocketSuccess() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(certificateChain, "AUTH", mockSocket)

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(certificateChain, "AUTH", mockSocket)
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedSslEngineFailure() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH", mockSslEngine)
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(brokenCertificateChain, "AUTH", mockSslEngine)
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSslEngineSuccess() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(certificateChain, "AUTH", mockSslEngine)

        // Then the call is delegated
        verify(x509ExtendedTrustManager).checkServerTrusted(certificateChain, "AUTH", mockSslEngine)
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedFailureApi17() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, "AUTH", "host")
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(brokenCertificateChain, "AUTH", "host")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedFailureApi36() {
        // When we call checkServerTrusted with a broken certificate chain
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(brokenCertificateChain, null, null, "AUTH", "host")
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(brokenCertificateChain, null, null, "AUTH", "host")
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
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(
            chain = certificateChain,
            authType = "AUTH",
            host = "host"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedSuccessApi36() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(
            chain = certificateChain,
            ocspData = null,
            tlsSctData = null,
            authType = "AUTH",
            host = "host"
        )

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(
            chain = certificateChain,
            ocspData = null,
            tlsSctData = null,
            authType = "AUTH",
            host = "host"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }
}
