/*
 * Copyright 2021-2025 Appmattus Limited
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

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.tls.HandshakeCertificates
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.net.ssl.SSLPeerUnverifiedException

internal class CertificateTransparencyInterceptorIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        private val networkInterceptor = certificateTransparencyInterceptor {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        private val networkInterceptorAllowFails = certificateTransparencyInterceptor {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }

            failOnError = false
        }

        private val clientCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
            .addInsecureHost(invalidSctDomain)
            .addPlatformTrustedCertificates()
            .build()
    }

    @Test
    fun appmattusAllowed() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addNetworkInterceptor(networkInterceptor)
            .build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun insecureConnectionAllowed() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addNetworkInterceptor(networkInterceptor)
            .build()

        val request = Request.Builder()
            .url("http://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun invalidDisallowedWithException() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addNetworkInterceptor(networkInterceptor)
            .build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenFailsAllowed() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addNetworkInterceptor(networkInterceptorAllowFails)
            .build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        val client =
            OkHttpClient.Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .addNetworkInterceptor(
                    certificateTransparencyInterceptor {
                        -invalidSctDomain

                        logListDataSource {
                            LogListDataSourceTestFactory.realLogListDataSource
                        }
                    }
                )
                .build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun invalidNotAllowedWhenAllHostsIncluded() {
        val client =
            OkHttpClient.Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .addNetworkInterceptor(
                    certificateTransparencyInterceptor {
                        logListDataSource {
                            LogListDataSourceTestFactory.realLogListDataSource
                        }
                    }
                )
                .build()

        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = IllegalStateException::class)
    fun interceptorThrowsException() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addInterceptor(networkInterceptor)
            .build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun failOnErrorDynamicallyChanged() {
        // Given a hostname verifier with dynamic failOnError value
        var dynamicFailOnError = true
        val networkInterceptor = certificateTransparencyInterceptor {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
            setFailOnError { dynamicFailOnError }
        }
        // Given a request with CT enabled
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .addNetworkInterceptor(networkInterceptor)
            .build()
        val request = Request.Builder()
            .url("https://$invalidSctDomain/")
            .build()

        // When failOnError is set to true
        dynamicFailOnError = true
        // Then the request fails
        assertThrows(SSLPeerUnverifiedException::class.java) {
            client.newCall(request).execute()
        }

        // When failOnError is set to false
        dynamicFailOnError = false
        // The the request succeeds with no error
        client.newCall(request).execute()
    }
}
