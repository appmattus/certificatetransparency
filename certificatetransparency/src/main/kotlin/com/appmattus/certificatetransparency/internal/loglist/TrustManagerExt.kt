/*
 * Copyright 2023 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyProvider
import java.security.KeyStore
import java.security.Security
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Returns the first [TrustManagerFactory] that is NOT provided by the Certificate Transparency library
 * This helps ensure we don't end up in infinite loops
 */
internal fun defaultTrustManagerFactory(): TrustManagerFactory {
    val defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm()

    val providerName = Security.getProviders("TrustManagerFactory.$defaultAlgorithm").first { provider ->
        provider::class.java != CertificateTransparencyProvider::class.java
    }.name

    return TrustManagerFactory.getInstance(defaultAlgorithm, providerName)
}

/**
 * Returns the first [X509TrustManager] that is NOT provided by the Certificate Transparency library
 * This helps ensure we don't end up in infinite loops
 */
internal fun defaultTrustManager(): X509TrustManager {
    return defaultTrustManagerFactory().apply {
        init(null as KeyStore?)
    }.trustManagers.first { it is X509TrustManager } as X509TrustManager
}
