/*
 * Copyright 2024-2025 Appmattus Limited
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

import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.CTPolicy
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.utils.asn1.query.query
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import okhttp3.internal.peerName
import java.lang.reflect.Method
import java.net.Socket
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

@Suppress("LongParameterList", "CustomX509TrustManager", "NewApi", "TooManyFunctions")
internal class CertificateTransparencyTrustManagerExtended(
    private val delegate: X509TrustManager,
    includeHosts: Set<Host>,
    excludeHosts: Set<Host>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory?,
    logListService: LogListService?,
    logListDataSource: DataSource<LogListResult>?,
    policy: CTPolicy?,
    diskCache: DiskCache?,
    private val failOnError: () -> Boolean = { true },
    private val logger: CTLogger? = null
) : X509ExtendedTrustManager(), CertificateTransparencyTrustManager {
    private val ctBase = CertificateTransparencyBase(
        includeHosts = includeHosts,
        excludeHosts = excludeHosts,
        certificateChainCleanerFactory = certificateChainCleanerFactory,
        trustManager = delegate,
        logListService = logListService,
        logListDataSource = logListDataSource,
        policy = policy,
        diskCache = diskCache
    )

    private val checkServerTrustedMethodApi17: Method? = try {
        delegate::class.java.getDeclaredMethod(
            "checkServerTrusted",
            Array<X509Certificate>::class.java,
            String::class.java,
            String::class.java
        )
    } catch (ignored: NoSuchMethodException) {
        null
    }

    private val checkServerTrustedMethodApi36: Method? = try {
        delegate::class.java.getDeclaredMethod(
            "checkServerTrusted",
            Array<X509Certificate>::class.java,
            ByteArray::class.java,
            ByteArray::class.java,
            String::class.java,
            String::class.java
        )
    } catch (ignored: NoSuchMethodException) {
        null
    }

    private val isSameTrustConfigurationMethod: Method? = try {
        delegate::class.java.getDeclaredMethod("isSameTrustConfiguration", String::class.java, String::class.java)
    } catch (ignored: NoSuchMethodException) {
        null
    }

    override fun verifyCertificateTransparency(host: String, certificates: List<Certificate>): VerificationResult =
        ctBase.verifyCertificateTransparency(host, certificates)

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) =
        delegate.checkClientTrusted(chain, authType)

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String, socket: Socket) =
        if (delegate is X509ExtendedTrustManager) delegate.checkClientTrusted(chain, authType, socket) else Unit

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine) {
        if (delegate is X509ExtendedTrustManager) delegate.checkClientTrusted(chain, authType, engine) else Unit
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, socket: Socket) {
        if (delegate is X509ExtendedTrustManager) delegate.checkServerTrusted(chain, authType, socket)

        val commonName = socket.peerName()

        val result = verifyCertificateTransparency(commonName, chain.toList())

        logger?.log(commonName, result)

        if (result is VerificationResult.Failure && failOnError()) {
            throw CertificateException("Certificate transparency failed")
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine) {
        if (delegate is X509ExtendedTrustManager) delegate.checkServerTrusted(chain, authType, engine)

        val commonName = engine.peerHost

        val result = verifyCertificateTransparency(commonName, chain.toList())

        logger?.log(commonName, result)

        if (result is VerificationResult.Failure && failOnError()) {
            throw CertificateException("Certificate transparency failed")
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        delegate.checkServerTrusted(chain, authType)

        val leafCertificate = chain.first()

        val commonName = leafCertificate.subjectX500Principal.encoded.toAsn1().query {
            seq().firstOrNull { it.seq().first().seq().first().oid() == "2.5.4.3" }?.seq()?.first()?.seq()?.get(1)?.string()
        } ?: throw CertificateException("No commonName found in certificate subjectDN")

        val result = verifyCertificateTransparency(commonName, chain.toList())

        logger?.log(commonName, result)

        if (result is VerificationResult.Failure && failOnError()) {
            throw CertificateException("Certificate transparency failed")
        }
    }

    // Called through reflection by X509TrustManagerExtensions on Android
    // Added in API level 17
    @Suppress("unused")
    fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, host: String): List<X509Certificate> {
        @Suppress("UNCHECKED_CAST")
        val certs = checkServerTrustedMethodApi17!!.invoke(delegate, chain, authType, host) as List<X509Certificate>

        val result = verifyCertificateTransparency(host, certs.toList())

        logger?.log(host, result)

        if (result is VerificationResult.Failure && failOnError()) {
            throw CertificateException("Certificate transparency failed")
        }

        return certs
    }

    // Called through reflection by X509TrustManagerExtensions on Android
    // Added in API level 36
    @Suppress("unused")
    fun checkServerTrusted(
        chain: Array<out X509Certificate>,
        ocspData: ByteArray?,
        tlsSctData: ByteArray?,
        authType: String,
        host: String
    ): List<X509Certificate> {
        @Suppress("UNCHECKED_CAST")
        val certs = checkServerTrustedMethodApi36!!.invoke(delegate, chain, ocspData, tlsSctData, authType, host) as List<X509Certificate>

        val result = verifyCertificateTransparency(host, certs.toList())

        logger?.log(host, result)

        if (result is VerificationResult.Failure && failOnError()) {
            throw CertificateException("Certificate transparency failed")
        }

        return certs
    }

    // Called through reflection by X509TrustManagerExtensions on Android
    // Added in API level 28
    @Suppress("unused")
    fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean {
        return isSameTrustConfigurationMethod!!.invoke(delegate, hostname1, hostname2) as Boolean
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers
}
