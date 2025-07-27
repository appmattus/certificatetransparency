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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.model.v3.LogListV3
import com.appmattus.certificatetransparency.internal.loglist.model.v3.State
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.PublicKeyFactory
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogServer
import com.appmattus.certificatetransparency.loglist.PreviousOperator
import kotlinx.serialization.json.Json
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

internal class LogListJsonParserV3 : LogListJsonParser {

    override fun parseJson(logListJson: String): LogListResult {
        return runCatching {
            val logList = json.decodeFromString(LogListV3.serializer(), logListJson)
            buildLogServerList(logList)
        }.getOrElse { e ->
            LogListResult.Invalid.LogListJsonBadFormat(e)
        }
    }

    @Suppress("ReturnCount")
    private fun buildLogServerList(logList: LogListV3): LogListResult {
        return logList.operators.map { operator ->
            val allLogs = operator.logs + operator.tiledLogs.orEmpty()
            // null, PENDING, REJECTED -> An SCT associated with this log server would be treated as untrusted
            allLogs.filterNot { it.state == null || it.state is State.Pending || it.state is State.Rejected }
                .map { log ->
                    val keyBytes = Base64.decode(log.key)

                    // FROZEN, RETIRED -> Validate SCT against this if it was issued before the state timestamp, otherwise SCT is untrusted
                    // QUALIFIED, USABLE -> Validate SCT against this (any timestamp okay)
                    val state = log.state
                    val validUntil = if (state is State.Retired || state is State.ReadOnly) state.timestamp else null

                    val key = try {
                        PublicKeyFactory.fromByteArray(keyBytes)
                    } catch (e: InvalidKeySpecException) {
                        return LogListResult.Invalid.LogServerInvalidKey(e, log.key)
                    } catch (e: NoSuchAlgorithmException) {
                        return LogListResult.Invalid.LogServerInvalidKey(e, log.key)
                    } catch (e: IllegalArgumentException) {
                        return LogListResult.Invalid.LogServerInvalidKey(e, log.key)
                    }

                    LogServer(
                        key = key,
                        validUntil = validUntil,
                        operator = operator.name,
                        previousOperators = log.listOfPreviousOperators?.map { PreviousOperator(it.name, it.endDate) } ?: emptyList()
                    )
                }
        }.flatten().let { LogListResult.Valid.Success(logList.logListTimestamp, it) }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}
