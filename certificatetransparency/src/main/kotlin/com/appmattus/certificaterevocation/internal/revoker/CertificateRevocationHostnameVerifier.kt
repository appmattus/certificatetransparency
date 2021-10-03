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

package com.appmattus.certificaterevocation.internal.revoker

import com.appmattus.certificaterevocation.CRLogger
import com.appmattus.certificaterevocation.RevocationResult
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

internal class CertificateRevocationHostnameVerifier(
    private val delegate: HostnameVerifier,
    crlSet: Set<CrlItem>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory? = null,
    trustManager: X509TrustManager?,
    private val failOnError: Boolean = true,
    private val logger: CRLogger? = null
) : CertificateRevocationBase(crlSet, certificateChainCleanerFactory, trustManager), HostnameVerifier {

    override fun verify(host: String, sslSession: SSLSession): Boolean {
        if (!delegate.verify(host, sslSession)) {
            return false
        }

        val result = verifyCertificateRevocation(host, sslSession.peerCertificates.toList())

        logger?.log(host, result)

        return !(result is RevocationResult.Failure && failOnError)
    }
}
