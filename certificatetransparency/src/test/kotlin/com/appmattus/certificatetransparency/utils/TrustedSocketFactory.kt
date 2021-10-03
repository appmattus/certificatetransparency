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

package com.appmattus.certificatetransparency.utils

import java.security.KeyStore
import java.security.cert.Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class TrustedSocketFactory {
    private fun trustManagerForCertificates(certificates: List<Certificate>): X509TrustManager {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)

            certificates.forEachIndexed { i, certificate ->
                setCertificateEntry("$i", certificate)
            }
        }

        val trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore)
        }.trustManagers

        return trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    fun create(certificates: List<Certificate>): SocketConfiguration {
        val trustManager = trustManagerForCertificates(certificates)
        val sslSocketFactory = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }.socketFactory

        return SocketConfiguration(sslSocketFactory, trustManager)
    }
}
