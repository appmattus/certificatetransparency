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

package com.appmattus.certificatetransparency.loglist

import com.appmattus.certificatetransparency.internal.utils.sha256Hash
import java.security.PublicKey
import java.time.Instant

/**
 * Representation of a log server, usually loaded from log-list.json
 * @property key The log servers [PublicKey]
 * @property validUntil Timestamp denoting when a log server is valid until, or null if it is valid for all time
 */
public data class LogServer(
    val key: PublicKey,
    val validUntil: Instant? = null,
    val operator: String,
    val previousOperators: List<PreviousOperator>
) {
    /**
     * The log servers id. A SHA-256 hash of the log servers [PublicKey]
     */
    val id: ByteArray = key.sha256Hash()

    public fun operatorAt(timestamp: Instant): String {
        previousOperators.sortedBy { it.endDate }.forEach {
            if (timestamp < it.endDate) return it.name
        }
        // Either the log has only ever had one operator, or the timestamp is after
        // the last operator change.
        return operator
    }

    public companion object
}
