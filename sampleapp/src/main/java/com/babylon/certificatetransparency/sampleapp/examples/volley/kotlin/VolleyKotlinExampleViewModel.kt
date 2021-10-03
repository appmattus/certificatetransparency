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

package com.babylon.certificatetransparency.sampleapp.examples.volley.kotlin

import android.app.Application
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.babylon.certificatetransparency.CTLogger
import com.babylon.certificatetransparency.cache.AndroidDiskCache
import com.babylon.certificatetransparency.certificateTransparencyHostnameVerifier
import com.babylon.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class VolleyKotlinExampleViewModel(application: Application) : BaseExampleViewModel(application) {

    override val sampleCodeTemplate
        get() = "volley-kotlin.txt"

    private fun HttpURLConnection.enableCertificateTransparencyChecks(
        hosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        if (this is HttpsURLConnection) {
            // Create a hostname verifier wrapping the original
            hostnameVerifier = certificateTransparencyHostnameVerifier(hostnameVerifier) {
                hosts.forEach {
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
    private fun createRequestQueue(hosts: Set<String>, isFailOnError: Boolean, defaultLogger: CTLogger): RequestQueue {
        return Volley.newRequestQueue(
            getApplication(),
            object : HurlStack() {
                override fun createConnection(url: URL): HttpURLConnection {
                    return super.createConnection(url).apply {
                        enableCertificateTransparencyChecks(hosts, isFailOnError, defaultLogger)
                    }
                }
            }
        )
    }

    override fun openConnection(
        connectionHost: String,
        hosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        val queue = createRequestQueue(hosts, isFailOnError, defaultLogger)

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
