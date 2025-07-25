/*
 * Copyright 2021-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.CTPolicy
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager

@Suppress("LongParameterList")
internal class CertificateTransparencyInterceptor(
    includeHosts: Set<Host>,
    excludeHosts: Set<Host>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory?,
    trustManager: X509TrustManager?,
    logListService: LogListService?,
    logListDataSource: DataSource<LogListResult>?,
    policy: CTPolicy?,
    diskCache: DiskCache? = null,
    private val failOnError: (VerificationResult.Failure) -> Boolean = { true },
    private val logger: CTLogger? = null
) : Interceptor, CertificateTransparencyBase(
    includeHosts,
    excludeHosts,
    certificateChainCleanerFactory,
    trustManager,
    logListService,
    logListDataSource,
    policy,
    diskCache
) {

    override fun intercept(chain: Interceptor.Chain): Response {
        val host = chain.request().url.host
        val connection = chain.connection() ?: error(
            "No connection found. Verify interceptor is added using addNetworkInterceptor"
        )

        val certs = connection.handshake()?.peerCertificates ?: emptyList()

        val result = if (connection.socket() is SSLSocket) {
            verifyCertificateTransparency(host, certs)
        } else {
            VerificationResult.Success.InsecureConnection(host)
        }

        logger?.log(host, result)

        if (result is VerificationResult.Failure && failOnError(result)) {
            throw SSLPeerUnverifiedException("Certificate transparency failed")
        }

        return chain.proceed(chain.request())
    }
}
