/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.loglist

import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.loglist.InMemoryDataSource
import com.appmattus.certificatetransparency.internal.loglist.LogListNetworkDataSource
import com.appmattus.certificatetransparency.internal.loglist.LogListZipNetworkDataSource
import com.appmattus.certificatetransparency.internal.loglist.await
import com.appmattus.certificatetransparency.internal.loglist.parser.RawLogListToLogListResultTransformer
import com.appmattus.certificatetransparency.internal.utils.MaxSizeInterceptor
import okhttp3.CacheControl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

public object LogListDataSourceFactory {

    /**
     * Create a [LogListService] allowing the override of [baseUrl], [okHttpClient] and [networkTimeoutSeconds].
     * Default: baseUrl = https://www.gstatic.com/ct/log_list/v2/
     */
    public fun createLogListService(
        baseUrl: String = "https://www.gstatic.com/ct/log_list/v2/",
        okHttpClient: OkHttpClient? = null,
        networkTimeoutSeconds: Long = 30,
        trustManager: X509TrustManager? = null
    ): LogListService {

        val client = (okHttpClient?.newBuilder() ?: OkHttpClient.Builder()).apply {
            // If a TrustManager is provided then use it. This will be the case when using the Certificate Transparency provider
            trustManager?.let {
                val sslContext: SSLContext
                try {
                    sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, arrayOf(trustManager), SecureRandom())
                } catch (expected: NoSuchAlgorithmException) {
                    throw IllegalStateException("Unable to create an SSLContext")
                } catch (expected: KeyManagementException) {
                    throw IllegalStateException("Unable to create an SSLContext")
                }

                sslSocketFactory(sslContext.socketFactory, trustManager)
            }

            addInterceptor(MaxSizeInterceptor())
            connectTimeout(networkTimeoutSeconds, TimeUnit.SECONDS)
            readTimeout(networkTimeoutSeconds, TimeUnit.SECONDS)
            writeTimeout(networkTimeoutSeconds, TimeUnit.SECONDS)
            cache(null)
        }.build()

        return object : LogListService {
            override suspend fun getLogList() = get("log_list.json", maxSize = 1048576)
            override suspend fun getLogListSignature() = get("log_list.sig", maxSize = 512)
            override suspend fun getLogListZip() = get("log_list.zip", maxSize = 2097152)

            private suspend fun get(pathSegment: String, maxSize: Long): ByteArray {
                val request = Request.Builder()
                    .url(baseUrl.toHttpUrl().newBuilder().addPathSegment(pathSegment).build())
                    .cacheControl(CacheControl.Builder().noCache().noStore().build())
                    .addHeader(MaxSizeInterceptor.HEADER, maxSize.toString())
                    .build()

                return client.newCall(request).await()
            }
        }
    }

    /**
     * Create a [DataSource] of [LogListResult] allowing the override of [LogListService] and [DiskCache]
     */
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

    private class InMemoryCache : InMemoryDataSource<RawLogListResult>() {
        override suspend fun isValid(value: RawLogListResult?) = value is RawLogListResult.Success
    }
}
