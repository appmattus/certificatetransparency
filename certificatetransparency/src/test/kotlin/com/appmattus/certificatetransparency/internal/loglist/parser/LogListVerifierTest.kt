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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.LogServerSignatureResult
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature

internal class LogListVerifierTest {

    @Test
    fun `verifies signature`() = runBlocking {
        // given we have a valid json file and signature
        val keyPair = generateKeyPair()
        val signature = calculateSignature(keyPair.private, json.toByteArray())

        // when we ask for data
        val result = LogListVerifier(keyPair.public).verify(json.toByteArray(), signature)

        // then the signature verifies
        require(result is LogServerSignatureResult.Valid)
    }

    @Test
    fun `returns Invalid if signature invalid`() = runBlocking {
        // given we have an invalid signature
        val signature = ByteArray(512) { it.toByte() }

        // when we ask for data
        val result = LogListVerifier().verify(json.toByteArray(), signature)

        // then invalid is returned
        assertIsA<LogServerSignatureResult.Invalid.SignatureFailed>(result)
    }

    @Test
    fun `returns Invalid if signature corrupt`() = runBlocking {
        // given we have an invalid signature
        val signature = ByteArray(32) { it.toByte() }

        // when we ask for data
        val result = LogListVerifier().verify(json.toByteArray(), signature)

        // then invalid is returned
        assertIsA<LogServerSignatureResult.Invalid.SignatureNotValid>(result)
    }

    private fun calculateSignature(privateKey: PrivateKey, data: ByteArray): ByteArray {
        return Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(data)
        }.sign()
    }

    private fun generateKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("RSA").apply {
            initialize(1024)
        }.generateKeyPair()
    }

    companion object {
        private val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()
    }
}
