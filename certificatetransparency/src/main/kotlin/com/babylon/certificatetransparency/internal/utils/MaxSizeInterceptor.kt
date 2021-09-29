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

package com.babylon.certificatetransparency.internal.utils

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import okio.source

public class MaxSizeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(
            request.newBuilder().apply {
                removeHeader(HEADER)
            }.build()
        )

        val body = response.body

        return request.headers(HEADER).firstOrNull()?.toLongOrNull()?.let { maxSize ->
            response.newBuilder().body(
                LimitedSizeInputStream(body!!.byteStream(), maxSize)
                    .source()
                    .buffer()
                    .asResponseBody(body.contentType(), body.contentLength())
            ).build()
        } ?: response
    }

    public companion object {
        internal const val HEADER = "Max-Size"
    }
}
