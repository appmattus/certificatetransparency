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

package com.appmattus.certificaterevocation.internal.revoker

import com.appmattus.certificaterevocation.CRLogger
import com.appmattus.certificaterevocation.RevocationResult
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager

internal class CertificateRevocationInterceptor(
    crlSet: Set<CrlItem>,
    certificateChainCleanerFactory: CertificateChainCleanerFactory? = null,
    trustManager: X509TrustManager?,
    private val failOnError: () -> Boolean = { true },
    private val logger: CRLogger? = null
) : CertificateRevocationBase(crlSet, certificateChainCleanerFactory, trustManager), Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val host = chain.request().url.host
        val certs = chain.connection()?.handshake()?.peerCertificates ?: emptyList()

        val result = if (chain.connection()?.socket() is SSLSocket) {
            verifyCertificateRevocation(host, certs)
        } else {
            RevocationResult.Success.InsecureConnection
        }

        logger?.log(host, result)

        if (result is RevocationResult.Failure && failOnError()) {
            throw SSLPeerUnverifiedException("Certificate revocation failed")
        }

        return chain.proceed(chain.request())
    }
}
