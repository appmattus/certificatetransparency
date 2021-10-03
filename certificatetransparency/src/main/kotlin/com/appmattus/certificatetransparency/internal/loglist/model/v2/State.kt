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
import com.appmattus.certificatetransparency.internal.loglist.deserializer.StateDeserializer
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 * @property timestamp The time at which the log entered this state.
 */
@JsonAdapter(StateDeserializer::class)
internal sealed class State {
    abstract val timestamp: Long

    /**
     * An SCT associated with this log server would be treated as untrusted
     */
    data class Pending(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this (any timestamp okay)
     */
    data class Qualified(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this (any timestamp okay)
     */
    data class Usable(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long
    ) : State()

    /**
     * Validate SCT against this if it was issued before the timestamp, otherwise SCT is untrusted
     * @property finalTreeHead The tree head (tree size and root hash) at which the log was frozen.
     */
    data class ReadOnly(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long,
        @SerializedName("final_tree_head") val finalTreeHead: FinalTreeHead
    ) : State()

    /**
     * Validate SCT against this if it was issued before the state timestamp, otherwise SCT is untrusted
     */
    data class Retired(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long
    ) : State()

    /**
     * An SCT associated with this log server would be treated as untrusted
     */
    data class Rejected(
        @JsonAdapter(Rfc3339Deserializer::class) override val timestamp: Long
    ) : State()
}
