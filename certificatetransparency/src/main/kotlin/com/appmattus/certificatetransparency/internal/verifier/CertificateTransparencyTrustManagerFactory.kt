/*
 * Copyright 2022-2023 Appmattus Limited
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

import com.appmattus.certificatetransparency.CTTrustManagerBuilder
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import java.security.KeyStore
import java.security.Security
import javax.net.ssl.ManagerFactoryParameters
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.TrustManagerFactorySpi
import javax.net.ssl.X509TrustManager

internal class CertificateTransparencyTrustManagerFactory(
    private val providerName: String,
    private val init: CTTrustManagerBuilder.() -> Unit
) : TrustManagerFactorySpi() {

    private val delegateTrustManagerFactory: TrustManagerFactory by lazy {
        val defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm()

        val allProviders = Security.getProviders("TrustManagerFactory.$defaultAlgorithm").toList()

        val providerName = allProviders[allProviders.indexOfFirst { it.name == providerName } + 1].name

        TrustManagerFactory.getInstance(defaultAlgorithm, providerName)
    }

    private val cachedTrustManager: Array<TrustManager>? by lazy {
        delegateTrustManagerFactory.trustManagers?.map { trustManager ->
            if (trustManager is X509TrustManager) {
                certificateTransparencyTrustManager(trustManager, init)
            } else {
                trustManager
            }
        }?.toTypedArray()
    }

    override fun engineInit(ks: KeyStore?) {
        delegateTrustManagerFactory.init(ks)
    }

    override fun engineInit(spec: ManagerFactoryParameters?) {
        delegateTrustManagerFactory.init(spec)
    }

    override fun engineGetTrustManagers(): Array<TrustManager> = cachedTrustManager ?: emptyArray()
}
