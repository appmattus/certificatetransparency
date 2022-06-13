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

package com.appmattus.certificatetransparency.internal

import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class CertificateTransparencyTrustManagerIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        private val originalTrustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
        }.trustManagers.first { it is X509TrustManager } as X509TrustManager

        private val trustManager = certificateTransparencyTrustManager(originalTrustManager) {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        private val trustManagerAllowFails = certificateTransparencyTrustManager(originalTrustManager) {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }

            failOnError = false
        }

        private fun X509TrustManager.createSocketFactory(): SSLSocketFactory {
            return SSLContext.getInstance("SSL").apply {
                init(null, arrayOf<TrustManager>(this@createSocketFactory), SecureRandom())
            }.socketFactory
        }
    }

    @Test
    fun appmattusAllowed() {
        val client = OkHttpClient.Builder().sslSocketFactory(trustManager.createSocketFactory(), trustManager).build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun insecureConnectionAllowed() {
        val client = OkHttpClient.Builder().sslSocketFactory(trustManager.createSocketFactory(), trustManager).build()

        val request = Request.Builder()
            .url("http://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithException() {
        val client = OkHttpClient.Builder().sslSocketFactory(trustManager.createSocketFactory(), trustManager).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenFailsAllowed() {
        val client = OkHttpClient.Builder().sslSocketFactory(trustManagerAllowFails.createSocketFactory(), trustManagerAllowFails).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        val trustManager = certificateTransparencyTrustManager(originalTrustManager) {
            -invalidSctDomain

            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        val client = OkHttpClient.Builder().sslSocketFactory(trustManager.createSocketFactory(), trustManager).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidNotAllowedWhenAllHostsIncluded() {
        val trustManager = certificateTransparencyTrustManager(originalTrustManager) {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        val client = OkHttpClient.Builder().sslSocketFactory(trustManager.createSocketFactory(), trustManager).build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }
}
