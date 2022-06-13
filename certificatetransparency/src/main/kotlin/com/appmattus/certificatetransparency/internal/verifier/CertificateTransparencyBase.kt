/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.CTPolicy
import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleaner
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.loglist.LogListJsonFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.loglist.NoLogServers
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.hasEmbeddedSct
import com.appmattus.certificatetransparency.internal.utils.signedCertificateTimestamps
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListDataSourceFactory
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Suppress("LongParameterList")
internal open class CertificateTransparencyBase(
    private val includeHosts: Set<Host> = emptySet(),
    private val excludeHosts: Set<Host> = emptySet(),
    private val certificateChainCleanerFactory: CertificateChainCleanerFactory? = null,
    trustManager: X509TrustManager? = null,
    logListService: LogListService? = null,
    logListDataSource: DataSource<LogListResult>? = null,
    policy: CTPolicy? = null,
    diskCache: DiskCache? = null
) {
    init {
        includeHosts.forEach {
            require(!it.matchAll) { "Certificate transparency is enabled by default on all domain names" }
            require(!excludeHosts.contains(it)) { "Certificate transparency inclusions must not match exclude directly" }
        }

        require(logListDataSource == null || logListService == null) { "LogListService is ignored when overriding logListDataSource" }
        require(logListDataSource == null || diskCache == null) { "DiskCache is ignored when overriding logListDataSource" }
    }

    private val cleaner: CertificateChainCleaner by lazy {
        val localTrustManager = trustManager ?: TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
        }.trustManagers.first { it is X509TrustManager } as X509TrustManager

        certificateChainCleanerFactory?.get(localTrustManager) ?: CertificateChainCleaner.get(localTrustManager)
    }

    private val logListDataSource = logListDataSource ?: LogListDataSourceFactory.createDataSource(
        logListService = logListService ?: LogListDataSourceFactory.createLogListService(trustManager = trustManager),
        diskCache = diskCache
    )

    private val policy = (policy ?: DefaultPolicy())

    fun verifyCertificateTransparency(host: String, certificates: List<Certificate>): VerificationResult {
        return if (!enabledForCertificateTransparency(host)) {
            VerificationResult.Success.DisabledForHost(host)
        } else if (certificates.isEmpty()) {
            VerificationResult.Failure.NoCertificates
        } else {
            val cleanedCerts = cleaner.clean(certificates.filterIsInstance<X509Certificate>(), host)

            if (cleanedCerts.isEmpty()) {
                VerificationResult.Failure.NoCertificates
            } else {
                hasValidSignedCertificateTimestamp(cleanedCerts)
            }
        }
    }

    /**
     * Check if the certificates provided by a server contain Signed Certificate Timestamps
     * from a trusted CT log.
     *
     * @property certificates the certificate chain provided by the server
     * @return [VerificationResult.Success] if the certificates can be trusted, [VerificationResult.Failure] otherwise.
     */
    @Suppress("ReturnCount")
    private fun hasValidSignedCertificateTimestamp(certificates: List<X509Certificate>): VerificationResult {

        val result = try {
            runBlocking {
                logListDataSource.get()
            }
        } catch (expected: Exception) {
            LogListJsonFailedLoadingWithException(expected)
        }

        val verifiers = when (result) {
            is LogListResult.Valid -> result.servers.associateBy({ Base64.toBase64String(it.id) }) { LogSignatureVerifier(it) }
            is LogListResult.Invalid -> return VerificationResult.Failure.LogServersFailed(result)
            null -> return VerificationResult.Failure.LogServersFailed(NoLogServers)
        }

        val leafCertificate = certificates[0]

        if (!leafCertificate.hasEmbeddedSct()) {
            return VerificationResult.Failure.NoScts
        }

        return try {
            val sctResults = leafCertificate.signedCertificateTimestamps()
                .associateBy { Base64.toBase64String(it.id.keyId) }
                .mapValues { (logId, sct) ->
                    verifiers[logId]?.verifySignature(sct, certificates) ?: SctVerificationResult.Invalid.NoTrustedLogServerFound
                }

            policy.policyVerificationResult(leafCertificate, sctResults)
        } catch (e: IOException) {
            VerificationResult.Failure.UnknownIoException(e)
        }
    }

    private fun enabledForCertificateTransparency(host: String) = !excludeHosts.any { it.matches(host) } || includeHosts.any { it.matches(host) }
}
