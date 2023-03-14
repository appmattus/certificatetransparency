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

package com.appmattus.certificatetransparency

import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyProvider
import com.appmattus.certificatetransparency.internal.verifier.DefaultProviderName
import java.security.Security

/**
 * DSL to install a Java security provider that enables certificate transparency checks
 * @property init Block to execute as a [CTTrustManagerBuilder]
 */
@JvmSynthetic
public fun installCertificateTransparencyProvider(
    providerName: String = DefaultProviderName,
    init: CTTrustManagerBuilder.() -> Unit = {}
) {
    require(Security.getProvider(providerName) == null) { "Cannot register duplicate Security Provider with the name $providerName" }

    val provider = CertificateTransparencyProvider(providerName, init)

    Security.insertProviderAt(provider, 1)
}

@JvmSynthetic
public fun removeCertificateTransparencyProvider(providerName: String = DefaultProviderName) {
    Security.removeProvider(providerName)
}
