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

import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import com.appmattus.certificatetransparency.removeCertificateTransparencyProvider
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Test
import javax.net.ssl.SSLHandshakeException

internal class CertificateTransparencyProviderIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        private fun installProvider() {
            installCertificateTransparencyProvider {
                logListDataSource {
                    LogListDataSourceTestFactory.realLogListDataSource
                }
            }
        }

        private fun installProviderAllowFails() {
            installCertificateTransparencyProvider {
                logListDataSource {
                    LogListDataSourceTestFactory.realLogListDataSource
                }

                failOnError = false
            }
        }
    }

    @After
    fun removeProvider() {
        removeCertificateTransparencyProvider()
    }

    @Test
    fun appmattusAllowed() {
        installProvider()

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun insecureConnectionAllowed() {
        installProvider()

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("http://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithException() {
        installProvider()

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenFailsAllowed() {
        installProviderAllowFails()

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        installCertificateTransparencyProvider {
            -invalidSctDomain

            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidNotAllowedWhenAllHostsIncluded() {
        installCertificateTransparencyProvider {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }
}
