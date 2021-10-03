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

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PublicKeyExtTest {

    @Test
    fun sha256Hash() {
        // given a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_CERT)[0]

        // when we hash the public key using SHA256
        val hash = Base64.toBase64String(certificate.publicKey.sha256Hash())

        // then the result matches openssl generated using:
        // openssl x509 -in test-cert.pem -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
        assertEquals("Ojz4hdfbFTowDio/KDGC4/pN9dy/EBfIAsnO2yDbKiE=", hash)
    }

    @Test
    fun sha1Hash() {
        // given a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_CERT)[0]

        // when we hash the public key using SHA1
        val hash = Base64.toBase64String(certificate.publicKey.sha1Hash())

        // then the result matches openssl generated using:
        // openssl x509 -in test-cert.pem -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha1 -binary | openssl enc -base64
        assertEquals("3VE6e2DtM3cCmvh1ScSFnSENktA=", hash)
    }
}
