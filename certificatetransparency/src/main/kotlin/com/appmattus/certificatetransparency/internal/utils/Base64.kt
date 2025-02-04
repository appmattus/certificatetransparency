/*
 * Copyright 2023-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal object Base64 {

    @OptIn(ExperimentalEncodingApi::class)
    private val base64WithPadding = Base64.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)

    @OptIn(ExperimentalEncodingApi::class)
    fun decode(data: String): ByteArray = base64WithPadding.decode(data.encodeToByteArray())

    @OptIn(ExperimentalEncodingApi::class)
    fun toBase64String(data: ByteArray): String = base64WithPadding.encode(data)
}
