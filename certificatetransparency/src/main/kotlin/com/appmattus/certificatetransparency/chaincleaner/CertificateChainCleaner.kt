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

package com.appmattus.certificatetransparency.chaincleaner

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Computes the effective certificate chain from the raw array returned by Java's built in TLS APIs.
 * Cleaning a chain returns a list of certificates where the first element is `chain[0]`, each
 * certificate is signed by the certificate that follows, and the last certificate is a trusted CA
 * certificate.
 *
 *
 * Use of the chain cleaner is necessary to omit unexpected certificates that aren't relevant to
 * the TLS handshake and to extract the trusted CA certificate for the benefit of certificate
 * pinning.
 */
public interface CertificateChainCleaner {
    public fun clean(chain: List<X509Certificate>, hostname: String): List<X509Certificate>

    public companion object {
        private val androidCertificateChainCleanerFactory by lazy {
            try {
                Class.forName("com.appmattus.certificatetransparency.chaincleaner.AndroidCertificateChainCleaner\$Factory")
                    .getDeclaredConstructor().newInstance() as CertificateChainCleanerFactory
            } catch (ignored: Exception) {
                null
            }
        }

        public fun get(trustManager: X509TrustManager): CertificateChainCleaner {
            return androidCertificateChainCleanerFactory?.get(trustManager) ?: BasicCertificateChainCleaner(trustManager)
        }
    }
}
