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

package com.appmattus.certificatetransparency.sampleapp.examples.volley

import android.app.Application
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.certificateTransparencyHostnameVerifier
import com.appmattus.certificatetransparency.sampleapp.R
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class VolleyKotlinExampleViewModel(application: Application) : BaseExampleViewModel(application) {
    override val sampleCodeTemplate
        get() = "volley-kotlin.txt"

    override val title
        get() = getApplication<Application>().getString(R.string.volley_kotlin_example)

    private fun HttpURLConnection.enableCertificateTransparencyChecks(
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        if (this is HttpsURLConnection) {
            // Create a hostname verifier wrapping the original
            hostnameVerifier = certificateTransparencyHostnameVerifier(hostnameVerifier) {
                excludeHosts.forEach {
                    -it
                }
                includeHosts.forEach {
                    +it
                }
                failOnError = isFailOnError
                logger = defaultLogger
                diskCache = AndroidDiskCache(getApplication())
            }
        }
    }

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private fun createRequestQueue(
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ): RequestQueue {
        return Volley.newRequestQueue(
            getApplication(),
            object : HurlStack() {
                override fun createConnection(url: URL): HttpURLConnection {
                    return super.createConnection(url).apply {
                        enableCertificateTransparencyChecks(includeHosts, excludeHosts, isFailOnError, defaultLogger)
                    }
                }
            }
        )
    }

    override fun openConnection(
        connectionHost: String,
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        val queue = createRequestQueue(includeHosts, excludeHosts, isFailOnError, defaultLogger)

        val request = StringRequest(
            Request.Method.GET,
            "https://$connectionHost",
            { response ->
                // Success. Reason will have been sent to the logger
                println(response)
            },
            { error ->
                // Failure. Send message to the UI as logger won't catch generic network exceptions
                sendException(error)
            }
        )

        // Explicitly disable cache so we always call the interceptor and thus see the certificate transparency results
        request.setShouldCache(false)

        // Add the request to the RequestQueue.
        queue.add(request)
    }
}
