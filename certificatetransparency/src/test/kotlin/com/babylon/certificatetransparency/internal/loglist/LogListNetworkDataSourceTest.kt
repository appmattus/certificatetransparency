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

import com.babylon.certificatetransparency.internal.utils.ByteArrayConverterFactory
import com.babylon.certificatetransparency.internal.utils.MaxSizeInterceptor
import com.babylon.certificatetransparency.loglist.LogListService
import com.babylon.certificatetransparency.loglist.RawLogListResult
import com.babylon.certificatetransparency.utils.TestData
import com.babylon.certificatetransparency.utils.assertIsA
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Retrofit
import javax.net.ssl.SSLPeerUnverifiedException

class LogListNetworkDataSourceTest {

    private val mockInterceptor = mock<Interceptor>()

    private val client: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(MaxSizeInterceptor()).addInterceptor(mockInterceptor).build()
    private val retrofit = Retrofit.Builder().client(client).baseUrl("http://ctlog/").addConverterFactory(ByteArrayConverterFactory()).build()
    private val logListService: LogListService = retrofit.create(TestLogListService::class.java)

    private fun expectInterceptor(
        @Suppress("SameParameterValue") url: String,
        @Suppress("SameParameterValue") jsonResponse: String
    ) {
        whenever(mockInterceptor.intercept(argThat { request().url().toString() == url })).then {

            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), jsonResponse))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("")
                .build()
        }
    }

    private fun expectInterceptorHttpNotFound(url: String) {
        whenever(mockInterceptor.intercept(argThat { request().url().toString() == url })).then {

            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/octet-stream"), ByteArray(0)))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(404)
                .message("")
                .build()
        }
    }

    private fun expectInterceptorSSLException(url: String) {
        whenever(mockInterceptor.intercept(argThat { request().url().toString() == url })).then {
            throw SSLPeerUnverifiedException("Mock throwing exception")
        }
    }

    private fun expectInterceptor(
        @Suppress("SameParameterValue") url: String,
        @Suppress("SameParameterValue") byteResponse: ByteArray
    ) {
        whenever(mockInterceptor.intercept(argThat { request().url().toString() == url })).then {

            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/octet-stream"), byteResponse))
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
        expectInterceptor("http://ctlog/log_list.json", json)
        expectInterceptor("http://ctlog/log_list.sig", sig)

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then the log list is returned
        assertIsA<RawLogListResult.Success>(result)
    }

    @Test
    fun `returns Invalid when log_list json not found`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptorHttpNotFound("http://ctlog/log_list.json")
        expectInterceptor("http://ctlog/log_list.sig", sig)

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListJsonFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list sig not found`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.json", json)
        expectInterceptorHttpNotFound("http://ctlog/log_list.sig")

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListSigFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list json has SslException`() = runBlocking {
        // given we have a valid signature and an exception when accessing the log list
        expectInterceptorSSLException("http://ctlog/log_list.json")
        expectInterceptor("http://ctlog/log_list.sig", sig)

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListJsonFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list sig has SslException`() = runBlocking {
        // given we have a valid json file and an exception when accessing the signature
        expectInterceptor("http://ctlog/log_list.json", json)
        expectInterceptorSSLException("http://ctlog/log_list.sig")

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then invalid is returned
        assertIsA<RawLogListSigFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list json too big`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.json", jsonTooBig)
        expectInterceptor("http://ctlog/log_list.sig", sig)

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then the log list is returned
        assertIsA<RawLogListJsonFailedTooBig>(result)
    }

    @Test
    fun `returns Invalid when log_list sig too big`() = runBlocking {
        // given we have a valid json file and signature
        expectInterceptor("http://ctlog/log_list.json", json)
        expectInterceptor("http://ctlog/log_list.sig", sigTooBig)

        // when we ask for data
        val result = LogListNetworkDataSource(logListService).get()

        // then the log list is returned
        assertIsA<RawLogListSigFailedTooBig>(result)
    }

    companion object {
        private val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()
        private val jsonTooBig = TestData.file(TestData.TEST_LOG_LIST_JSON_TOO_BIG).readText()
        private val sig = TestData.file(TestData.TEST_LOG_LIST_SIG).readBytes()
        private val sigTooBig = TestData.file(TestData.TEST_LOG_LIST_SIG_TOO_BIG).readBytes()
    }
}
