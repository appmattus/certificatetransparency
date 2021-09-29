/*
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
 */

package com.babylon.certificatetransparency.internal.loglist.model.v2

import com.babylon.certificatetransparency.internal.loglist.deserializer.HostnameDeserializer
import com.google.gson.annotations.JsonAdapter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@JsonAdapter(HostnameDeserializer::class)
internal data class Hostname(
    val value: String
) {
    init {
        "http://$value".toHttpUrlOrNull()?.host ?: throw IllegalArgumentException("$value is not a well-formed hostname")
    }
}
