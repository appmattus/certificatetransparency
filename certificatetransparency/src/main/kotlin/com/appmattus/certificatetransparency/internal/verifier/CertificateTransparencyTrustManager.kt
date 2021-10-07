/*
 * Copyright 2021 Appmattus Limited
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
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.X509TrustManager

@Suppress("LongParameterList", "CustomX509TrustManager")
internal class CertificateTransparencyTrustManager(
    private val delegate: X509TrustManager,
    includeHosts: Set<Host>,
    excludeHosts: Set<Host>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory?,
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
    delegate,
    logListService,
    logListDataSource,
    policy,
    diskCache
), X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = delegate.checkClientTrusted(chain, authType)

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        delegate.checkServerTrusted(chain, authType)

        val leafCertificate = chain.first()

        val commonName = X500Name(leafCertificate.subjectX500Principal.name).getRDNs(BCStyle.CN)[0].first.value.toString()

        // val subjectAlternativeNames = leafCertificate.subjectAlternativeNames.filter { it[0] == 2 }.map { it[1].toString() }
        // val dnsNames = listOf(cn) + subjectAlternativeNames

        // val host = dnsNames.firstOrNull { enabledForCertificateTransparency(it) } ?: cn

        val result = verifyCertificateTransparency(commonName, chain.toList())

        logger?.log(commonName, result)

        if (result is VerificationResult.Failure && failOnError) {
            throw SSLPeerUnverifiedException("Certificate transparency failed")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers
}
