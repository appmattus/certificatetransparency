/*
 * Copyright 2021-2025 Appmattus Limited
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

package com.appmattus.certificaterevocation

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.net.ssl.SSLPeerUnverifiedException

internal class CertificateRevocationHostnameVerifierIntegrationTest {

    companion object {
        val emptyHostnameVerifier = certificateRevocationHostnameVerifier(OkHostnameVerifier)

        val hostnameVerifier = certificateRevocationHostnameVerifier(OkHostnameVerifier) {
            // Root cert for www.appmattus.com
            @Suppress("MaxLineLength")
            addCrl(
                issuerDistinguishedName = "ME8xCzAJBgNVBAYTAlVTMSkwJwYDVQQKEyBJbnRlcm5ldCBTZWN1cml0eSBSZXNlYXJjaCBHcm91cDEVMBMGA1UEAxMMSVNSRyBSb290IFgx",
                serialNumbers = listOf("AIIQz7DSQONZRGPgu2OCiwA=")
            )
        }
    }

    @Test
    fun appmattusAllowed() {
        val client = OkHttpClient.Builder().hostnameVerifier(emptyHostnameVerifier).build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun certificateRejectedWhenRulePresentForCert() {
        val client = OkHttpClient.Builder().hostnameVerifier(hostnameVerifier).build()

        val request = Request.Builder()
            .url("https://www.appmattus.com")
            .build()

        client.newCall(request).execute()
    }

    @Test
    fun failOnErrorDynamicallyChanged() {
        // Given a hostname verifier with dynamic failOnError value
        var dynamicFailOnError = true
        val hostnameVerifier = certificateRevocationHostnameVerifier(OkHostnameVerifier) {
            // Root cert for www.appmattus.com
            @Suppress("MaxLineLength")
            addCrl(
                issuerDistinguishedName = "ME8xCzAJBgNVBAYTAlVTMSkwJwYDVQQKEyBJbnRlcm5ldCBTZWN1cml0eSBSZXNlYXJjaCBHcm91cDEVMBMGA1UEAxMMSVNSRyBSb290IFgx",
                serialNumbers = listOf("AIIQz7DSQONZRGPgu2OCiwA=")
            )
            setFailOnError { dynamicFailOnError }
        }
        // Given a request with CR enabled
        val client = OkHttpClient.Builder().hostnameVerifier(hostnameVerifier).build()
        val request = Request.Builder()
            .url("https://www.appmattus.com")
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
