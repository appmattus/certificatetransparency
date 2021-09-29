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

package com.babylon.certificatetransparency.internal.loglist

import com.babylon.certificatetransparency.datasource.DataSource
import com.babylon.certificatetransparency.internal.utils.isTooBigException
import com.babylon.certificatetransparency.loglist.LogListService
import com.babylon.certificatetransparency.loglist.RawLogListResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

public class LogListNetworkDataSource(
    private val logListService: LogListService
) : DataSource<RawLogListResult> {

    override val coroutineContext: CoroutineContext = GlobalScope.coroutineContext

    @Suppress("ReturnCount")
    override suspend fun get(): RawLogListResult {
        val logListJob = async { logListService.getLogList() }
        val signatureJob = async { logListService.getLogListSignature() }

        val logListJson = try {
            logListJob.await()
        } catch (expected: Exception) {
            return if (expected.isTooBigException()) RawLogListJsonFailedTooBig else RawLogListJsonFailedLoadingWithException(expected)
        }

        val signature = try {
            signatureJob.await()
        } catch (expected: Exception) {
            return if (expected.isTooBigException()) RawLogListSigFailedTooBig else RawLogListSigFailedLoadingWithException(expected)
        }

        return RawLogListResult.Success(logListJson, signature)
    }

    override suspend fun isValid(value: RawLogListResult?): Boolean = value is RawLogListResult.Success

    override suspend fun set(value: RawLogListResult): Unit = Unit
}
