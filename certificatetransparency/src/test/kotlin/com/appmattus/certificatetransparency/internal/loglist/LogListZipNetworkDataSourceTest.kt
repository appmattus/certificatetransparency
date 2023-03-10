/*
 * Copyright 2021-2023 Appmattus Limited
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

import com.appmattus.certificatetransparency.internal.utils.ByteArrayConverterFactory
import com.appmattus.certificatetransparency.internal.utils.MaxSizeInterceptor
import com.appmattus.certificatetransparency.loglist.LogListService
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import javax.net.ssl.SSLPeerUnverifiedException

internal class LogListZipNetworkDataSourceTest {

    private val mockInterceptor = mock<Interceptor>()

    private val client: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(MaxSizeInterceptor()).addInterceptor(mockInterceptor).build()
    private val retrofit = Retrofit.Builder().client(client).baseUrl("http://ctlog/").addConverterFactory(
        ByteArrayConverterFactory()
    ).build()
    private val logListService: LogListService = retrofit.create(TestLogListService::class.java)

    private fun expectInterceptorHttpNotFound(url: String) {
        whenever(mockInterceptor.intercept(argThat { request().url.toString() == url })).then {
            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(ByteArray(0).toResponseBody("application/octet-stream".toMediaType()))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(404)
                .message("")
                .build()
        }
    }

    private fun expectInterceptorSSLException(url: String) {
        whenever(mockInterceptor.intercept(argThat { request().url.toString() == url })).then {
            throw SSLPeerUnverifiedException("Mock throwing exception")
        }
    }

    private fun expectInterceptor(
        @Suppress("SameParameterValue") url: String,
        @Suppress("SameParameterValue") byteResponse: ByteArray
    ) {
        whenever(mockInterceptor.intercept(argThat { request().url.toString() == url })).then {
            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(byteResponse.toResponseBody("application/octet-stream".toMediaType()))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("")
                .build()
        }
    }

    @Test
    fun `verifies signature`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipValid)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then the log list is returned
        assertIsA<RawLogListResult.Success>(result)
    }

    @Test
    fun `returns Invalid when log_list zip too big`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipTooBig)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then the log list is returned
        assertIsA<RawLogListZipFailedTooBig>(result)
    }

    @Test
    fun `returns Invalid when log_list zip not found`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptorHttpNotFound("http://ctlog/log_list.zip")

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list zip sig missing`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipSigMissing)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedSigMissing>(result)
    }

    @Test
    fun `returns Invalid when log_list zip json missing`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipJsonMissing)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedJsonMissing>(result)
    }

    @Test
    fun `returns Invalid when log_list zip sig too big`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipSigTooBig)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedSigTooBig>(result)
    }

    @Test
    fun `returns Invalid when log_list zip json too big`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.zip", zipJsonTooBig)

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedJsonTooBig>(result)
    }

    @Test
    fun `returns Invalid when log_list zip has SslException`() = runBlocking {
        // given we have a valid signature and an exception when accessing the log list
        expectInterceptorSSLException("http://ctlog/log_list.zip")

        // when we ask for data
        val result = LogListZipNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListZipFailedLoadingWithException>(result)
    }

    companion object {
        private val zipValid = TestData.file(TestData.TEST_LOG_LIST_ZIP).readBytes()
        private val zipTooBig = TestData.file(TestData.TEST_LOG_LIST_ZIP_TOO_BIG).readBytes()
        private val zipJsonMissing = TestData.file(TestData.TEST_LOG_LIST_ZIP_JSON_MISSING).readBytes()
        private val zipSigMissing = TestData.file(TestData.TEST_LOG_LIST_ZIP_SIG_MISSING).readBytes()
        private val zipJsonTooBig = TestData.file(TestData.TEST_LOG_LIST_ZIP_JSON_TOO_BIG).readBytes()
        private val zipSigTooBig = TestData.file(TestData.TEST_LOG_LIST_ZIP_SIG_TOO_BIG).readBytes()
    }
}
