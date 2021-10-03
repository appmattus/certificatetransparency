/*
 * Copyright 2021 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.utils.isTooBigException
import com.appmattus.certificatetransparency.loglist.LogListService
import com.appmattus.certificatetransparency.loglist.RawLogListResult

internal class LogListNetworkDataSource(
    private val logListService: LogListService
) : DataSource<RawLogListResult> {

    @Suppress("ReturnCount")
    override suspend fun get(): RawLogListResult {
        val logListJson = try {
            logListService.getLogList()
        } catch (expected: Exception) {
            return if (expected.isTooBigException()) RawLogListJsonFailedTooBig else RawLogListJsonFailedLoadingWithException(expected)
        }

        val signature = try {
            logListService.getLogListSignature()
        } catch (expected: Exception) {
            return if (expected.isTooBigException()) RawLogListSigFailedTooBig else RawLogListSigFailedLoadingWithException(expected)
        }

        return RawLogListResult.Success(logListJson, signature)
    }

    override suspend fun isValid(value: RawLogListResult?) = value is RawLogListResult.Success

    override suspend fun set(value: RawLogListResult) = Unit
}
