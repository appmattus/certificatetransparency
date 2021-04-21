/*
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
 */

package com.babylon.certificatetransparency.internal

import com.babylon.certificatetransparency.certificateTransparencyInterceptor
import com.babylon.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import javax.net.ssl.SSLPeerUnverifiedException

class CertificateTransparencyInterceptorIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        val networkInterceptor = certificateTransparencyInterceptor {
            +"*.babylonhealth.com"
            +invalidSctDomain

            logListDataSource {
                LogListDataSourceTestFactory.logListDataSource
            }
        }

        val networkInterceptorAllowFails = certificateTransparencyInterceptor {
            +"*.babylonhealth.com"
            +invalidSctDomain

            logListDataSource {
                LogListDataSourceTestFactory.logListDataSource
            }

            failOnError = false
        }
    }

    @Test
    fun babylonHealthAllowed() {
        val client = OkHttpClient.Builder().addNetworkInterceptor(networkInterceptor).build()

        val request = Request.Builder()
            .url("https://www.babylonhealth.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun insecureConnectionAllowed() {
        val client = OkHttpClient.Builder().addNetworkInterceptor(networkInterceptor).build()

        val request = Request.Builder()
            .url("http://www.babylonhealth.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun invalidDisallowedWithException() {
        val client = OkHttpClient.Builder().addNetworkInterceptor(networkInterceptor).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenFailsAllowed() {
        val client = OkHttpClient.Builder().addNetworkInterceptor(networkInterceptorAllowFails).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        val client =
            OkHttpClient.Builder().addNetworkInterceptor(
                certificateTransparencyInterceptor {
                    +"*.babylonhealth.com"

                    logListDataSource {
                        LogListDataSourceTestFactory.logListDataSource
                    }
                }
            ).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun invalidNotAllowedWhenAllHostsIncluded() {
        val client =
            OkHttpClient.Builder().addNetworkInterceptor(
                certificateTransparencyInterceptor {
                    +"*.*"

                    logListDataSource {
                        LogListDataSourceTestFactory.logListDataSource
                    }
                }
            ).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenHostExcludedFromAll() {
        val client =
            OkHttpClient.Builder().addNetworkInterceptor(
                certificateTransparencyInterceptor {
                    +"*.*"
                    -invalidSctDomain

                    logListDataSource {
                        LogListDataSourceTestFactory.logListDataSource
                    }
                }
            ).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = IllegalStateException::class)
    fun interceptorThrowsException() {
        val client = OkHttpClient.Builder().addInterceptor(networkInterceptor).build()

        val request = Request.Builder()
            .url("https://www.babylonhealth.com")
            .build()

        client.newCall(request).execute()
    }
}
