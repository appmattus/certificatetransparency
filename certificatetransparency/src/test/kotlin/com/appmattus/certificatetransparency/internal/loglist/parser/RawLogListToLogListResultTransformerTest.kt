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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.LogListJsonBadFormat
import com.appmattus.certificatetransparency.internal.loglist.LogListJsonFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.loglist.LogListSigFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.loglist.RawLogListJsonFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.loglist.RawLogListSigFailedLoadingWithException
import com.appmattus.certificatetransparency.internal.loglist.SignatureVerificationFailed
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import javax.net.ssl.SSLException

internal class RawLogListToLogListResultTransformerTest {

    @Test
    fun `verifies signature`() = runBlocking {
        // given we have a valid json file and signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(RawLogListResult.Success(json.toByteArray(), sig))

        // then 41 items are returned
        require(result is LogListResult.Valid)
        assertEquals(41, result.servers.size)
        assertEquals("Y/Lbzeg7zCzPC3KEJ1drM6SNYXePvXWmOLHHaFRL2I0=", Base64.toBase64String(result.servers[0].id))
    }

    @Test
    fun `returns Invalid if json incomplete`() = runBlocking {
        // given we have an invalid json file and valid signature
        val keyPair = generateKeyPair()
        val signature = calculateSignature(keyPair.private, jsonIncomplete.toByteArray())

        // when we ask for data
        val result = RawLogListToLogListResultTransformer(
            logListVerifier = LogListVerifier(keyPair.public)
        ).transform(
            RawLogListResult.Success(
                jsonIncomplete.toByteArray(),
                signature
            )
        )

        // then invalid is returned
        assertIsA<LogListJsonBadFormat>(result)
    }

    @Test
    fun `returns Invalid if signature invalid`() = runBlocking {
        // given we have a valid json file and na invalid signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListResult.Success(
                json.toByteArray(),
                ByteArray(512)
            )
        )

        // then invalid is returned
        assertIsA<SignatureVerificationFailed>(result)
    }

    @Test
    fun `returns Invalid if signature corrupt`() = runBlocking {
        // given we have a valid json file and signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListResult.Success(
                json.toByteArray(),
                ByteArray(32)
            )
        )

        // then invalid is returned
        assertIsA<SignatureVerificationFailed>(result)
    }

    @Test
    fun `returns Invalid when log_list json not found`() = runBlocking {
        // given we have a valid json file and signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListJsonFailedLoadingWithException(IOException("bogo"))
        )

        // then invalid is returned
        assertIsA<LogListJsonFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list sig not found`() = runBlocking {
        // given we have a valid json file and signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListSigFailedLoadingWithException(IOException("bogo"))
        )

        // then invalid is returned
        assertIsA<LogListSigFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list json has SslException`() = runBlocking {
        // given we have a valid signature and an exception when accessing the log list

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListJsonFailedLoadingWithException(SSLException("bogo"))
        )

        // then invalid is returned
        assertIsA<LogListJsonFailedLoadingWithException>(result)
    }

    @Test
    fun `returns Invalid when log_list sig has SslException`() = runBlocking {
        // given we have a valid json file and an exception when accessing the signature

        // when we ask for data
        val result = RawLogListToLogListResultTransformer().transform(
            RawLogListSigFailedLoadingWithException(SSLException("bogo"))
        )

        // then invalid is returned
        assertIsA<LogListSigFailedLoadingWithException>(result)
    }

    @Test
    fun `validUntil null when not disqualified or no FinalTreeHead`() = runBlocking {
        // given we have a valid json file and signature
        val keyPair = generateKeyPair()
        val signature = calculateSignature(keyPair.private, jsonValidUntil.toByteArray())

        // when we ask for data
        val result = RawLogListToLogListResultTransformer(
            logListVerifier = LogListVerifier(keyPair.public)
        ).transform(
            RawLogListResult.Success(
                jsonValidUntil.toByteArray(),
                signature
            )
        )

        // then validUntil is set to the the STH timestamp
        require(result is LogListResult.Valid)
        val logServer = result.servers[0]
        assertNull(logServer.validUntil)
    }

    @Test
    fun `validUntil set from disqualified`() = runBlocking {
        // given we have a valid json file and signature
        val keyPair = generateKeyPair()
        val signature = calculateSignature(keyPair.private, jsonValidUntil.toByteArray())

        // when we ask for data
        val result = RawLogListToLogListResultTransformer(
            logListVerifier = LogListVerifier(keyPair.public)
        ).transform(
            RawLogListResult.Success(
                jsonValidUntil.toByteArray(),
                signature
            )
        )

        // then validUntil is set to the timestamp
        require(result is LogListResult.Valid)
        val logServer = result.servers[3]
        assertNotNull(logServer.validUntil)
        assertEquals(1550275200000, logServer.validUntil)
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
        private val sig = TestData.file(TestData.TEST_LOG_LIST_SIG).readBytes()

        private val jsonIncomplete = TestData.file(TestData.TEST_LOG_LIST_JSON_INCOMPLETE).readText()
        private val jsonValidUntil = TestData.file(TestData.TEST_LOG_LIST_JSON_VALID_UNTIL).readText()
    }
}
