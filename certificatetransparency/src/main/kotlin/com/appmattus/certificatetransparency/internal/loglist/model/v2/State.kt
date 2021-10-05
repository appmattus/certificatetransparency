/*
 * Copyright 2021 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist.model.v2

import com.appmattus.certificatetransparency.internal.loglist.deserializer.Rfc3339Deserializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property timestamp The time at which the log entered this state.
 */
@Serializable
internal sealed class State {
    abstract val timestamp: Long

    /**
     * An SCT associated with this log server would be treated as untrusted
     */
    @Serializable
    @SerialName("pending")
    data class Pending(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this (any timestamp okay)
     */
    @Serializable
    @SerialName("qualified")
    data class Qualified(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this (any timestamp okay)
     */
    @SerialName("usable")
    @Serializable
    data class Usable(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this if it was issued before the timestamp, otherwise SCT is untrusted
     * @property finalTreeHead The tree head (tree size and root hash) at which the log was frozen.
     */
    @Serializable
    @SerialName("readonly")
    data class ReadOnly(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long,
        @SerialName("final_tree_head") val finalTreeHead: FinalTreeHead
    ) : State()

    /**
     * Validate SCT against this if it was issued before the state timestamp, otherwise SCT is untrusted
     */
    @Serializable
    @SerialName("retired")
    data class Retired(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long
    ) : State()

    /**
     * An SCT associated with this log server would be treated as untrusted
     */
    @Serializable
    @SerialName("rejected")
    data class Rejected(
        @Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") override val timestamp: Long
    ) : State()
}
