/*
 * Copyright 2021-2023 Appmattus Limited
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

import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.CTTrustManagerBuilder
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyProvider
import com.appmattus.certificatetransparency.internal.verifier.DefaultProviderName
import com.appmattus.certificatetransparency.removeCertificateTransparencyProvider
import com.appmattus.certificatetransparency.utils.LogListDataSourceTestFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.Security
import javax.net.ssl.SSLHandshakeException

internal class CertificateTransparencyProviderIntegrationTest {

    companion object {
        private const val invalidSctDomain = "no-sct.badssl.com"

        private val results = mutableListOf<String>()

        private fun installProvider(
            providerName: String = DefaultProviderName,
            init: CTTrustManagerBuilder.() -> Unit = {}
        ) {
            installCertificateTransparencyProvider(providerName) {
                logListDataSource {
                    LogListDataSourceTestFactory.realLogListDataSource
                }
                logger = object : CTLogger {
                    override fun log(host: String, result: VerificationResult) {
                        println("$providerName $host $result")
                        results += "$providerName $host ${result::class.java.simpleName}"
                    }
                }

                init()
            }
        }

        private fun installProviderAllowFails(
            providerName: String = DefaultProviderName,
            init: CTTrustManagerBuilder.() -> Unit = {}
        ) {
            installCertificateTransparencyProvider(providerName) {
                logListDataSource {
                    LogListDataSourceTestFactory.realLogListDataSource
                }
                logger = object : CTLogger {
                    override fun log(host: String, result: VerificationResult) {
                        println("$providerName $host $result")
                        results += "$providerName $host ${result::class.java.simpleName}"
                    }
                }

                failOnError = false

                init()
            }
        }
    }

    @After
    fun removeProvider() {
        Security.getProviders().filterIsInstance<CertificateTransparencyProvider>().forEach {
            Security.removeProvider(it.name)
        }
        results.clear()
    }

    @Test
    fun appmattusAllowed() {
        installProvider()
        makeConnection("https://www.appmattus.com")
    }

    @Test
    fun connectionAllowedWithTwoProvidersInstalled() {
        // Simulate client and server installing CT checks
        installProvider("SDK")
        installProvider("Client")

        makeConnection("https://www.appmattus.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithTwoProvidersInstalled() {
        // Simulate client and server installing CT checks
        installProvider("SDK")
        installProvider("Client")

        makeConnection("https://$invalidSctDomain/")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithTwoProvidersInstalledSdkIgnoringDomain() {
        // Simulate client and server installing CT checks
        installProvider("SDK") { -invalidSctDomain }
        installProvider("Client")

        makeConnection("https://$invalidSctDomain/")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithTwoProvidersInstalledClientIgnoringDomain() {
        // Simulate client and server installing CT checks
        installProvider("SDK")
        installProvider("Client") { -invalidSctDomain }

        makeConnection("https://$invalidSctDomain/")
    }

    @Test
    fun invalidAllowedWithTwoProvidersInstalledAndIgnoringDomain() {
        // Simulate client and server installing CT checks
        installProvider("SDK") { -invalidSctDomain }
        installProvider("Client") { -invalidSctDomain }

        makeConnection("https://$invalidSctDomain/")
    }

    @Test
    fun invalidAllowedWhenProviderUninstalled() {
        // Given we install a provider
        installProvider()

        // When we remove the provider
        removeCertificateTransparencyProvider()
        // And make a connection to an invalid domain
        makeConnection("https://$invalidSctDomain/")

        // Then the connection is allowed and no checks are performed
        assertEquals(0, results.size)
    }

    @Test
    fun invalidAllowedWhenDisallowedProviderUninstalled() {
        // Simulate client and server installing CT checks
        installProvider("SDK")
        installProvider("Client") { -invalidSctDomain }

        // Remove the SDK provider that would otherwise block the connection
        removeCertificateTransparencyProvider("SDK")

        makeConnection("https://$invalidSctDomain/")
    }

    @Test
    fun invalidAllowedWhenSdkExcludesAllExceptSpecificDomains() {
        // Simulate client and server installing CT checks similar to recommendations
        installProvider("Client") {
            -invalidSctDomain
        }
        installProvider("SDK") {
            // Exclude all but a specific domain
            -"*.*"
            +"*.appmattus.com"
        }

        makeConnection("https://$invalidSctDomain/")
        makeConnection("https://www.appmattus.com/")

        assertEquals(4, results.size)
        assertTrue(results.contains("SDK no-sct.badssl.com DisabledForHost"))
        assertTrue(results.contains("Client no-sct.badssl.com DisabledForHost"))
        assertTrue(results.contains("SDK www.appmattus.com Trusted"))
        assertTrue(results.contains("Client www.appmattus.com Trusted"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun installingTwoProvidersWithSameDefaultNameFails() {
        installProvider()
        installProvider()
    }

    @Test(expected = IllegalArgumentException::class)
    fun installingTwoProvidersWithSameConfiguredNameFails() {
        installProvider("CustomName")
        installProvider("CustomName")
    }

    @Test
    fun insecureConnectionAllowed() {
        installProvider()
        makeConnection("http://www.appmattus.com")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidDisallowedWithException() {
        installProvider()
        makeConnection("https://$invalidSctDomain/")
    }

    @Test
    fun invalidAllowedWhenFailsAllowed() {
        installProviderAllowFails()
        makeConnection("https://$invalidSctDomain/")
    }

    @Test
    fun invalidAllowedWhenSctNotChecked() {
        installCertificateTransparencyProvider {
            -invalidSctDomain

            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        makeConnection("https://$invalidSctDomain/")
    }

    @Test(expected = SSLHandshakeException::class)
    fun invalidNotAllowedWhenAllHostsIncluded() {
        installCertificateTransparencyProvider {
            logListDataSource {
                LogListDataSourceTestFactory.realLogListDataSource
            }
        }

        makeConnection("https://$invalidSctDomain/")
    }

    private fun makeConnection(url: String) {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute()
    }
}
