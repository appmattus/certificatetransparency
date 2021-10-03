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

package com.appmattus.certificaterevocation

import com.appmattus.certificaterevocation.internal.revoker.CertificateRevocationInterceptor
import com.appmattus.certificaterevocation.internal.revoker.CrlItem
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.internal.utils.Base64
import okhttp3.Interceptor
import java.math.BigInteger
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.security.auth.x500.X500Principal

/**
 * Builder to create an OkHttp network interceptor that will reject cert chains containing revoked certificates
 */
public class CRInterceptorBuilder {
    private var certificateChainCleanerFactory: CertificateChainCleanerFactory? = null
    private var trustManager: X509TrustManager? = null
    private val crlSet = mutableSetOf<CrlItem>()

    /**
     * Determine if a failure to pass certificate revocation results in the connection being closed. A value of true ensures the connection is
     * closed on errors
     * Default: true
     */
    public var failOnError: Boolean = true
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CRLogger] which will be called with all results
     * Default: none
     */
    public var logger: CRLogger? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setCertificateChainCleanerFactory(certificateChainCleanerFactory: CertificateChainCleanerFactory): CRInterceptorBuilder =
        apply { this.certificateChainCleanerFactory = certificateChainCleanerFactory }

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun certificateChainCleanerFactory(init: () -> CertificateChainCleanerFactory) {
        setCertificateChainCleanerFactory(init())
    }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setTrustManager(trustManager: X509TrustManager): CRInterceptorBuilder =
        apply { this.trustManager = trustManager }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun trustManager(init: () -> X509TrustManager) {
        setTrustManager(init())
    }

    /**
     * Determine if a failure to pass certificate revocation results in the connection being closed. [failOnError] set to true closes the
     * connection on errors
     * Default: true
     */
    @Suppress("unused")
    public fun setFailOnError(failOnError: Boolean): CRInterceptorBuilder = apply { this.failOnError = failOnError }

    /**
     * [CRLogger] which will be called with all results
     * Default: none
     */
    @Suppress("unused")
    public fun setLogger(logger: CRLogger): CRInterceptorBuilder = apply { this.logger = logger }

    /**
     * Verify certificate revocation for certificates that match [issuerDistinguishedName] and [serialNumbers].
     *
     * @property issuerDistinguishedName lower-case host name or wildcard pattern such as `*.example.com`.
     */
    public fun addCrl(issuerDistinguishedName: String, serialNumbers: List<String>): CRInterceptorBuilder = apply {
        val decodedIssuerDistinguishedName = X500Principal(Base64.decode(issuerDistinguishedName))
        val decodedSerialNumbers = serialNumbers.map { BigInteger(Base64.decode(it)) }

        crlSet.add(CrlItem(decodedIssuerDistinguishedName, decodedSerialNumbers))
    }

    /**
     * Build the network [Interceptor]
     */
    public fun build(): Interceptor = CertificateRevocationInterceptor(
        crlSet = crlSet.toSet(),
        certificateChainCleanerFactory = certificateChainCleanerFactory,
        trustManager = trustManager,
        failOnError = failOnError,
        logger = logger
    )
}
