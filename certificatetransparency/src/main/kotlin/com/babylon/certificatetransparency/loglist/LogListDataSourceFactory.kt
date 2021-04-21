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

package com.babylon.certificatetransparency.loglist

import com.babylon.certificatetransparency.cache.DiskCache
import com.babylon.certificatetransparency.datasource.DataSource
import com.babylon.certificatetransparency.internal.loglist.InMemoryDataSource
import com.babylon.certificatetransparency.internal.loglist.LogListNetworkDataSource
import com.babylon.certificatetransparency.internal.loglist.LogListZipNetworkDataSource
import com.babylon.certificatetransparency.internal.loglist.parser.RawLogListToLogListResultTransformer
import com.babylon.certificatetransparency.internal.utils.MaxSizeInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public object LogListDataSourceFactory {

    public fun createLogListService(
        baseUrl: String = "https://www.gstatic.com/ct/log_list/v2/",
        okHttpClient: OkHttpClient? = null
    ): LogListService {

        val client = (okHttpClient?.newBuilder() ?: OkHttpClient.Builder())
            .addInterceptor(MaxSizeInterceptor())
            .cache(null)
            .build()

        return object : LogListService {
            override suspend fun getLogList() = get("log_list.json", maxSize = 1048576)
            override suspend fun getLogListSignature() = get("log_list.sig", maxSize = 512)
            override suspend fun getLogListZip() = get("log_list.zip", maxSize = 2097152)

            private suspend fun get(pathSegment: String, maxSize: Long): ByteArray {
                val request = Request.Builder()
                    .url(HttpUrl.get(baseUrl).newBuilder().addPathSegment(pathSegment).build())
                    .cacheControl(CacheControl.Builder().noCache().noStore().build())
                    .addHeader(MaxSizeInterceptor.HEADER, maxSize.toString())
                    .build()

                return client.newCall(request).await()
            }
        }
    }

    public fun createDataSource(
        logListService: LogListService = createLogListService(),
        diskCache: DiskCache? = null
    ): DataSource<LogListResult> {

        val transformer = RawLogListToLogListResultTransformer()

        return InMemoryCache()
            .run {
                diskCache?.let(::compose) ?: this
            }
            .compose(LogListZipNetworkDataSource(logListService))
            .compose(LogListNetworkDataSource(logListService))
            .oneWayTransform { transformer.transform(it) }
            .reuseInflight()
    }

    /**
     * Suspend extension that allows suspend [Call] inside coroutine.
     *
     * @return Result of request or throw exception
     */
    private suspend fun Call.await(): ByteArray {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response.body()?.bytes() ?: throw IOException("No data available"))
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        // Don't bother with resuming the continuation if it is already cancelled.
                        if (continuation.isCancelled) return
                        continuation.resumeWithException(e)
                    }
                })

                continuation.invokeOnCancellation {
                    try {
                        cancel()
                    } catch (ex: Throwable) {
                        // Ignore cancel exception
                    }
                }
            }
        }
    }

    private class InMemoryCache : InMemoryDataSource<RawLogListResult>() {
        override suspend fun isValid(value: RawLogListResult?) = value is RawLogListResult.Success
    }
}
