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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

class CertificateTransparencyTrustManagerExtendedTest {

    private val certificateChain = TestData.loadCertificates(TEST_MITMPROXY_ORIGINAL_CHAIN)

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
    fun checkServerTrustedFailureApi17() {
        // When we call checkClientTrusted
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(emptyArray(), "AUTH", "host")
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(emptyArray(), "AUTH", "host")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedFailureApi36() {
        // When we call checkClientTrusted
        val exception = assertThrows(CertificateException::class.java) {
            subject.checkServerTrusted(emptyArray(), null, null, "AUTH", "host")
        }

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(emptyArray(), null, null, "AUTH", "host")
        // And a certificate transparency failure is thrown
        assertEquals("Certificate transparency failed", exception.message)
    }

    @Test
    fun checkServerTrustedSuccessApi17() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(
            chain = certificateChain.toTypedArray(),
            authType = "AUTH",
            host = "host"
        )

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(
            chain = certificateChain.toTypedArray(),
            authType = "AUTH",
            host = "host"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }

    @Test
    fun checkServerTrustedSuccessApi36() {
        // When we call checkServerTrusted with a valid certificate chain
        subject.checkServerTrusted(
            chain = certificateChain.toTypedArray(),
            ocspData = null,
            tlsSctData = null,
            authType = "AUTH",
            host = "host"
        )

        // Then the call is delegated
        verify(x509ExtendedTrustManager as AndroidTrustManager).checkServerTrusted(
            chain = certificateChain.toTypedArray(),
            ocspData = null,
            tlsSctData = null,
            authType = "AUTH",
            host = "host"
        )
        // And no exception is thrown i.e. certificate transparency successful
    }
}
