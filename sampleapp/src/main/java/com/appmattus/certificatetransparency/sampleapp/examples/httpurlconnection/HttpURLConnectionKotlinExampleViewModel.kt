/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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

package com.appmattus.certificatetransparency.sampleapp.examples.httpurlconnection

import android.app.Application
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.certificateTransparencyHostnameVerifier
import com.appmattus.certificatetransparency.sampleapp.R
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HttpURLConnectionKotlinExampleViewModel(application: Application) : BaseExampleViewModel(application) {

    override val sampleCodeTemplate
        get() = "httpurlconnection-kotlin.txt"

    override val title
        get() = getApplication<Application>().getString(R.string.httpurlconnection_kotlin_example)

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

    override fun openConnection(
        connectionHost: String,
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    ) {
        // Quick and dirty way to push the network call onto a background thread, don't do this is a real app
        Thread {
            try {
                val connection = URL("https://$connectionHost").openConnection() as HttpURLConnection

                connection.enableCertificateTransparencyChecks(includeHosts, excludeHosts, isFailOnError, defaultLogger)

                connection.connect()
            } catch (e: IOException) {
                sendException(e)
            }
        }.start()
    }
}
