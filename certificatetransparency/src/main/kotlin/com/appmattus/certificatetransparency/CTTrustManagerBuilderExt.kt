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

package com.appmattus.certificatetransparency

import javax.net.ssl.X509TrustManager

/**
 * DSL to create a [X509TrustManager] that will verify a certificate is trusted using certificate transparency
 * @property delegate [X509TrustManager] to delegate to before performing certificate transparency checks
 * @property init Block to execute as a [CTTrustManagerBuilder]
 */
@JvmSynthetic
public fun certificateTransparencyTrustManager(
    delegate: X509TrustManager,
    init: CTTrustManagerBuilder.() -> Unit = {}
): X509TrustManager = CTTrustManagerBuilder(delegate)
    .apply(init)
    .build()
