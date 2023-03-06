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

import com.appmattus.certificatetransparency.internal.utils.stringStackTrace
import kotlinx.serialization.SerializationException
import java.time.Instant

public sealed interface LogListResult {
    /**
     * Interface representing log list loading successful
     */
    public sealed interface Valid : LogListResult {
        public val timestamp: Instant
        public val servers: List<LogServer>

        public data class Success(override val timestamp: Instant, override val servers: List<LogServer>) : Valid

        /**
         * Network is returning stale data so this denotes we are returning locally cached data to reduce the chance of replay attacks
         */
        public data class StaleNetworkUsingCachedData(
            override val timestamp: Instant,
            override val servers: List<LogServer>,
            val networkResult: Valid
        ) : Valid

        /**
         * Network is returning stale data so this denotes there is potentially a network issue
         */
        public data class StaleNetworkUsingNetworkData(
            override val timestamp: Instant,
            override val servers: List<LogServer>
        ) : Valid
    }

    /**
     * Class representing log list stale data
     */
    public data class DisableChecks(val timestamp: Instant, val networkResult: LogListResult) : LogListResult

    /**
     * Interface representing log list loading failed
     */
    public sealed interface Invalid : LogListResult {
        public data class SignatureVerificationFailed(val signatureResult: LogServerSignatureResult.Invalid) : Invalid

        public object NoLogServers : Invalid {
            override fun toString(): String = "log-list.json contains no log servers"
        }

        public object LogListJsonFailedLoading : Invalid {
            override fun toString(): String = "log-list.json failed to load"
        }

        public data class LogListZipFailedLoadingWithException(val exception: Exception) : Invalid {
            override fun toString(): String = "log-list.zip failed to load with ${exception.stringStackTrace()}"
        }

        public data class LogListJsonBadFormat(val exception: SerializationException) : Invalid {
            override fun toString(): String = "log-list.json badly formatted with ${exception.stringStackTrace()}"
        }

        public data class LogServerInvalidKey(val exception: Exception, val key: String) : Invalid {
            override fun toString(): String = "Public key for log server $key cannot be used with ${exception.stringStackTrace()}"
        }

        public data class LogListStaleNetwork(val networkResult: LogListResult) : Invalid {
            override fun toString(): String = "log-list.json from server is older than 70 days old"
        }
    }
}
