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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.loglist.RawLogListResult

internal class ResourcesCache : DataSource<RawLogListResult> {

    override suspend fun get(): RawLogListResult {
        with(this::class.java.classLoader) {
            val logList = getResourceAsStream("log_list.json")?.use { it.readBytes() } ?: return RawLogListResourceFailedJsonMissing
            val signature = getResourceAsStream("log_list.sig")?.use { it.readBytes() } ?: return RawLogListResourceFailedSigMissing

            return RawLogListResult.Success(logList, signature)
        }
    }

    override suspend fun set(value: RawLogListResult) = Unit

    internal object RawLogListResourceFailedJsonMissing : RawLogListResult.Failure() {
        override fun toString() = "Resources missing log-list.json file"
    }

    internal object RawLogListResourceFailedSigMissing : RawLogListResult.Failure() {
        override fun toString() = "Resources missing log-list.sig file"
    }
}
