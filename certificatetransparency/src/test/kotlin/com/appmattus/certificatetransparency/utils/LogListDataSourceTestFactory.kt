/*
 * Copyright 2021-2024 Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

package com.appmattus.certificatetransparency.utils

import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.loglist.model.v3.LogListV3
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.PublicKeyFactory
import com.appmattus.certificatetransparency.loglist.LogListDataSourceFactory
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogServer
import kotlinx.serialization.json.Json

internal object LogListDataSourceTestFactory {

    val realLogListDataSource: DataSource<LogListResult> by lazy {
        LogListDataSourceFactory.createDataSource()
    }

    val logListDataSource: DataSource<LogListResult> by lazy {
        // Collection of CT logs that are trusted from https://www.gstatic.com/ct/log_list/v3/log_list.json
        val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()

        val list = Json.decodeFromString(LogListV3.serializer(), json).operators.map { operator ->
            operator.logs.map {
                LogServer(
                    PublicKeyFactory.fromByteArray(Base64.decode(it.key)),
                    operator = operator.name,
                    previousOperators = emptyList()
                )
            }
        }.flatten().let { LogListResult.Valid.Success(System.currentTimeMillis(), it) }

        object : DataSource<LogListResult> {
            override suspend fun get() = list

            override suspend fun set(value: LogListResult) = Unit
        }
    }

    val emptySource: DataSource<LogListResult> by lazy {
        object : DataSource<LogListResult> {
            override suspend fun get() = LogListResult.Valid.Success(System.currentTimeMillis(), emptyList())

            override suspend fun set(value: LogListResult) = Unit
        }
    }

    val nullSource: DataSource<LogListResult> by lazy {
        object : DataSource<LogListResult> {
            override suspend fun get(): LogListResult? = null

            override suspend fun set(value: LogListResult) = Unit
        }
    }
}
