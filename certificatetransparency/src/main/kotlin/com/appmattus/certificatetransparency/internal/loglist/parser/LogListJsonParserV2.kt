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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.LogListJsonBadFormat
import com.appmattus.certificatetransparency.internal.loglist.LogServerInvalidKey
import com.appmattus.certificatetransparency.internal.loglist.model.v2.LogListV2
import com.appmattus.certificatetransparency.internal.loglist.model.v2.State
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.PublicKeyFactory
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogServer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

internal class LogListJsonParserV2 : LogListJsonParser {

    override fun parseJson(logListJson: String): LogListResult {
        val logList = try {
            json.decodeFromString(LogListV2.serializer(), logListJson)
        } catch (e: SerializationException) {
            return LogListJsonBadFormat(e)
        }

        return buildLogServerList(logList)
    }

    @Suppress("ReturnCount")
    private fun buildLogServerList(logList: LogListV2): LogListResult {
        return logList.operators
            .flatMap { it.logs }
            // null, PENDING, REJECTED -> An SCT associated with this log server would be treated as untrusted
            .filterNot { it.state == null || it.state is State.Pending || it.state is State.Rejected }
            .map {
                val keyBytes = Base64.decode(it.key)

                // FROZEN, RETIRED -> Validate SCT against this if it was issued before the state timestamp, otherwise SCT is untrusted
                // QUALIFIED, USABLE -> Validate SCT against this (any timestamp okay)
                val validUntil = if (it.state is State.Retired || it.state is State.ReadOnly) it.state.timestamp else null

                val key = try {
                    PublicKeyFactory.fromByteArray(keyBytes)
                } catch (e: InvalidKeySpecException) {
                    return LogServerInvalidKey(e, it.key)
                } catch (e: NoSuchAlgorithmException) {
                    return LogServerInvalidKey(e, it.key)
                } catch (e: IllegalArgumentException) {
                    return LogServerInvalidKey(e, it.key)
                }

                LogServer(key, validUntil)
            }.let(LogListResult::Valid)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}
