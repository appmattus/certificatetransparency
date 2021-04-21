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

package com.babylon.certificatetransparency.internal.verifier

import com.babylon.certificatetransparency.CTLogger
import com.babylon.certificatetransparency.CTPolicy
import com.babylon.certificatetransparency.VerificationResult
import com.babylon.certificatetransparency.cache.DiskCache
import com.babylon.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.babylon.certificatetransparency.datasource.DataSource
import com.babylon.certificatetransparency.internal.verifier.model.Host
import com.babylon.certificatetransparency.loglist.LogListResult
import com.babylon.certificatetransparency.loglist.LogListService
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

@Suppress("LongParameterList")
internal class CertificateTransparencyHostnameVerifier(
    private val delegate: HostnameVerifier,
    includeHosts: Set<Host>,
    excludeHosts: Set<Host>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory?,
    trustManager: X509TrustManager?,
    logListService: LogListService?,
    logListDataSource: DataSource<LogListResult>?,
    policy: CTPolicy?,
    diskCache: DiskCache?,
    private val failOnError: Boolean = true,
    private val logger: CTLogger? = null
) : CertificateTransparencyBase(
    includeHosts,
    excludeHosts,
    certificateChainCleanerFactory,
    trustManager,
    logListService,
    logListDataSource,
    policy,
    diskCache
), HostnameVerifier {

    override fun verify(host: String, sslSession: SSLSession): Boolean {
        if (!delegate.verify(host, sslSession)) {
            return false
        }

        val result = verifyCertificateTransparency(host, sslSession.peerCertificates.toList())

        logger?.log(host, result)

        return !(result is VerificationResult.Failure && failOnError)
    }
}
