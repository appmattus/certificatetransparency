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

package com.appmattus.certificatetransparency.internal.loglist.model.v3

import com.appmattus.certificatetransparency.internal.loglist.deserializer.Rfc3339Deserializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * @property name Name of the log operator
 * @property endDate The time at which this operator stopped operating this log.
 */

@Serializable
internal data class PreviousOperator(
    @Serializable(with = Rfc3339Deserializer::class)
    @SerialName("end_time")
    val endDate: Instant,
    @SerialName("name") val name: String
)
