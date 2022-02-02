/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.sampleapp.examples.trustmanager

import android.app.Application
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.sampleapp.R
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class TrustManagerKotlinExampleViewModel(application: Application) : BaseExampleViewModel(application) {

    override val sampleCodeTemplate
        get() = "trustmanager-kotlin.txt"

    override val title
        get() = getApplication<Application>().getString(R.string.trust_manager_kotlin_example)

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private fun createOkHttpClient(
        includeCommonNames: Set<String>,
        excludeCommonNames: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ): OkHttpClient {

        val trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
        }.trustManagers.first { it is X509TrustManager } as X509TrustManager

        val wrappedTrustManager = certificateTransparencyTrustManager(trustManager) {
            excludeCommonNames.forEach {
                -it
            }
            includeCommonNames.forEach {
                +it
            }
            failOnError = isFailOnError
            logger = defaultLogger
            diskCache = AndroidDiskCache(getApplication())
        }

        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(wrappedTrustManager), SecureRandom())

        // Set the interceptor when creating the OkHttp client
        return OkHttpClient.Builder().apply {
            sslSocketFactory(sslContext.socketFactory, trustManager)
        }.build()
    }

    override fun openConnection(
        connectionHost: String,
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        val client = createOkHttpClient(includeHosts, excludeHosts, isFailOnError, defaultLogger)

        val request = Request.Builder().url("https://$connectionHost").build()

        client.newCall(request).enqueue(
            object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    // Failure. Send message to the UI as logger won't catch generic network exceptions
                    sendException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    // Success. Reason will have been sent to the logger
                }
            }
        )
    }
}
