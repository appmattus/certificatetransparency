/*
 * Copyright 2023-2024 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.certificateTransparencyTrustManager
import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.security.cert.CertificateException
import javax.net.ssl.X509TrustManager

internal class CertificateTransparencyTrustManagerBasicTest {

    private val delegateTrustManager = mock<X509TrustManager> {
        doNothing().whenever(it).checkServerTrusted(any(), anyString())
    }

    private val trustManager = spy(certificateTransparencyTrustManager(delegateTrustManager) as CertificateTransparencyTrustManager)

    @Before
    fun setUp() {
        whenever(trustManager.verifyCertificateTransparency(anyString(), anyList())).thenReturn(VerificationResult.Success.Trusted(emptyMap()))

        doNothing().whenever(delegateTrustManager).checkServerTrusted(any(), anyString())
    }

    @Test
    fun commonNamePassedToCertificateTransparencyCheck() {
        val certs = TestData.loadCertificates(TestData.TEST_GITHUB_CHAIN)

        trustManager.checkServerTrusted(certs.toTypedArray(), "AUTH")

        verify(trustManager).verifyCertificateTransparency(eq("github.com"), anyList())
    }

    @Test
    fun noCommonNameThrowsException() {
        val certs = TestData.loadCertificates(TestData.TEST_NO_COMMON_NAME)

        val exception = assertThrows(CertificateException::class.java) {
            trustManager.checkServerTrusted(certs.toTypedArray(), "AUTH")
        }

        assertEquals("No commonName found in certificate subjectDN", exception.message)
    }

    @Test
    fun noSubjectThrowsException() {
        val certs = TestData.loadCertificates(TestData.TEST_NO_SUBJECT)

        val exception = assertThrows(CertificateException::class.java) {
            trustManager.checkServerTrusted(certs.toTypedArray(), "AUTH")
        }

        assertEquals("No commonName found in certificate subjectDN", exception.message)
    }
}
