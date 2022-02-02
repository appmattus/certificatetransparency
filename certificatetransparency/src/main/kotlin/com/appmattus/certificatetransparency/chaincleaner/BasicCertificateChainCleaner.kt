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
 * Code derived from https://github.com/square/okhttp/ and
 * https://github.com/google/conscrypt/
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.chaincleaner

import java.security.cert.X509Certificate
import java.util.ArrayDeque
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.X509TrustManager

/**
 * Certificate chain cleaner that uses [X509TrustManager.getAcceptedIssuers] to build a trusted chain, duplicating what happens during a
 * secure connection handshake.
 */
internal class BasicCertificateChainCleaner(
    trustManager: X509TrustManager
) : CertificateChainCleaner {

    private val subjectToCaCerts = trustManager.acceptedIssuers.groupBy { it.subjectX500Principal }

    /**
     * Returns a cleaned certificate chain from [chain]. An exception is thrown if a valid chain cannot be found, which may happen if a
     * different [X509TrustManager] is used to establish the secure connection.
     */
    @Suppress("LongMethod")
    @Throws(SSLPeerUnverifiedException::class)
    override fun clean(chain: List<X509Certificate>, hostname: String): List<X509Certificate> {
        if (chain.isEmpty()) throw SSLPeerUnverifiedException("Certificate chain is empty")

        val queue = ArrayDeque<X509Certificate>(chain)
        val result = mutableListOf<X509Certificate>()
        result.add(queue.removeFirst())
        var foundTrustedCertificate = false

        repeat(MAX_SIGNERS) {
            val toVerify = result.last()

            // If this cert has been signed by a trusted cert, use that. Add the trusted certificate to
            // the end of the chain unless it's already present. (That would happen if the first
            // certificate in the chain is itself a self-signed and trusted CA certificate.)
            findTrustedCertByIssuerAndSignature(toVerify)?.also { trustedCert ->
                // Add trusted cert to the chain unless it is already there
                if (result.size > 1 || toVerify != trustedCert) {
                    result.add(trustedCert)
                }
                if (trustedCert.isSignedBy(trustedCert)) {
                    // Self-signed cert is a root CA so we are at the end of the chain
                    return result
                }
                foundTrustedCertificate = true
            } ?: let {
                // Find the first certificate in the chain that signs [toVerify]. Usually the next element, but not always
                queue.firstOrNull { signingCert ->
                    toVerify.isSignedBy(signingCert)
                }?.also { signingCert ->
                    queue.remove(signingCert)
                    result.add(signingCert)
                } ?: let {
                    // No matching cert found so the end of the chain is reached. If any cert in the chain is trusted then return the chain
                    if (foundTrustedCertificate) {
                        return result
                    }

                    // No certificate is trusted and we've gone through the entire chain so fail
                    throw SSLPeerUnverifiedException("Failed to find a trusted cert that signed $toVerify")
                }
            }
        }

        throw SSLPeerUnverifiedException("Certificate chain too long: $result")
    }

    private fun findTrustedCertByIssuerAndSignature(cert: X509Certificate): X509Certificate? {
        val issuer = cert.issuerX500Principal
        return subjectToCaCerts[issuer]?.firstOrNull { cert.isSignedBy(it) }
    }

    /** Returns true if [this] is signed by the public key of [signingCert]
     *  @receiver An [X509Certificate]
     **/
    private fun X509Certificate.isSignedBy(signingCert: X509Certificate): Boolean {
        if (issuerX500Principal != signingCert.subjectX500Principal) return false
        return try {
            verify(signingCert.publicKey)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    companion object {
        /** Maximum signers in a chain, using 9 for consistency with OpenSSL */
        private const val MAX_SIGNERS = 9
    }
}
