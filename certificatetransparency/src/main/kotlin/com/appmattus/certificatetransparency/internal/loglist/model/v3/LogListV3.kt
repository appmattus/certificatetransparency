/*
 * Copyright 2021-2023 Appmattus Limited
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

import com.appmattus.certificatetransparency.internal.loglist.deserializer.Rfc3339Deserializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * @property logListTimestamp The time at which this version of the log list was published.
 * @property version Version of this log list. The version will change whenever a change is made to any part of this log list.
 * @property operators CT log operators. People/organizations that run Certificate Transparency logs.
 */
@Serializable
internal data class LogListV3(
    @Serializable(with = Rfc3339Deserializer::class) @SerialName("log_list_timestamp") val logListTimestamp: Instant,
    @SerialName("version") val version: String,
    @SerialName("operators") val operators: List<Operator>
)
