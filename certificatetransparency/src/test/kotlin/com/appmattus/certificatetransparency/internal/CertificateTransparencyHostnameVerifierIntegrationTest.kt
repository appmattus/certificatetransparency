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

package com.appmattus.certificatetransparency.internal

import com.appmattus.certificatetransparency.certificateTransparencyHostnameVerifier
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import org.junit.Test
import javax.net.ssl.SSLPeerUnverifiedException

internal class CertificateTransparencyHostnameVerifierIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        val hostnameVerifier = certificateTransparencyHostnameVerifier(OkHostnameVerifier) {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }
    }

    @Test
    fun appmattusAllowed() {
        val client = OkHttpClient.Builder().hostnameVerifier(hostnameVerifier).build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun invalidDisallowedWithException() {
        val client = OkHttpClient.Builder().hostnameVerifier(hostnameVerifier).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        val client = OkHttpClient.Builder().hostnameVerifier(
            certificateTransparencyHostnameVerifier(OkHostnameVerifier) {
                -invalidSctDomain

                logListDataSource {
                    LogListDataSourceTestFactory.realLogListDataSource
                }
            }
        ).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }
}
