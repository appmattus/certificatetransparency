/*
 * Copyright 2021-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist.model.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name Name of this log operator
 * @property email CT log operator email addresses. The log operator can be contacted using any of these email addresses. (format: email)
 * @property logs Details of Certificate Transparency logs run by this operator.
 * @property tiledLogs Details of Certificate Transparency tiled logs run by this operator.
 */
@Serializable
internal data class Operator(
    @SerialName("name") val name: String,
    @SerialName("email") val email: List<String>,
    @SerialName("logs") val logs: List<Log>,
    // Technically tiled_logs are required but marking as nullable for ease of integration
    @SerialName("tiled_logs") val tiledLogs: List<TiledLog>? = null,
) {
    init {
        require(name.isNotEmpty())
        require(email.isNotEmpty())
    }
}
