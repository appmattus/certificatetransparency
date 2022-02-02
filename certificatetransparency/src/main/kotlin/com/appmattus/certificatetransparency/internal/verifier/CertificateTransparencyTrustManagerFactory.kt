/*
 * Copyright 2022 Appmattus Limited
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

import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import java.security.KeyStore
import javax.net.ssl.ManagerFactoryParameters
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactorySpi
import javax.net.ssl.X509TrustManager

internal class CertificateTransparencyTrustManagerFactory : TrustManagerFactorySpi() {
    private var cachedTrustManager: Array<TrustManager>? = null

    override fun engineInit(ks: KeyStore?) {
        CertificateTransparencyTrustManagerFactoryState.delegate?.init(ks)
    }

    override fun engineInit(spec: ManagerFactoryParameters?) {
        CertificateTransparencyTrustManagerFactoryState.delegate?.init(spec)
    }

    override fun engineGetTrustManagers(): Array<TrustManager> {
        if (cachedTrustManager == null) {
            with(CertificateTransparencyTrustManagerFactoryState) {
                cachedTrustManager = delegate?.trustManagers?.map { trustManager ->
                    if (trustManager is X509TrustManager) {
                        certificateTransparencyTrustManager(trustManager, init)
                    } else {
                        trustManager
                    }
                }?.toTypedArray()
            }
        }

        return cachedTrustManager ?: emptyArray()
    }
}
