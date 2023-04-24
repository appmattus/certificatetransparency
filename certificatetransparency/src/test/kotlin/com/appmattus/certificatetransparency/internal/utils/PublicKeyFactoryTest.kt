/*
 * Copyright 2023 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.utils.TestData
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PublicKeyFactoryTest {

    @Test
    fun determineKeyAlgorithmRSA() {
        val input =
            "30819f300d06092a864886f70d010101050003818d00308189028181008fe2412a08e851a88cb3e853e7d54950b3278a2bcbeab54273ea0257cc6533ee882061" +
                "a11756c12418e3a808d3bed931f3370b94b8cc43080b7024f79cb18d5dd66d82d0540984f89f970175059c89d4d5c91ec913d72a6b309119d6d442e0c49d" +
                "7c9271e1b22f5c8deef0f1171ed25f315bb19cbc2055bf3a37424575dc90650203010001"
        val bytes = input.decodeHex().toByteArray()
        assertEquals("RSA", PublicKeyFactory.determineKeyAlgorithm(bytes))
    }

    @Test
    fun determineKeyAlgorithmEC() {
        val input =
            "3059301306072a8648ce3d020106082a8648ce3d030107034200047883dce9f1a6b8183a00992fff3ecd15c9261ef7ff3aa9a3721649eb09b6a8ddb4d247910e" +
                "0df9d9d5a98bb0879d2579d41a506008f509063926e440c2bac3c2"
        val bytes = input.decodeHex().toByteArray()
        assertEquals("EC", PublicKeyFactory.determineKeyAlgorithm(bytes))
    }

    @Test
    fun fromPemStringEC() {
        val pem = TestData.file(TestData.TEST_PUBLIC_KEY_EC).readText()
        val key = PublicKeyFactory.fromPemString(pem)
        assertEquals("EC", key.algorithm)
    }

    @Test
    fun fromPemStringRSA() {
        val pem = TestData.file(TestData.TEST_PUBLIC_KEY_RSA).readText()
        val key = PublicKeyFactory.fromPemString(pem)
        assertEquals("RSA", key.algorithm)
    }

    @Test
    fun fromPemStringDSA() {
        val pem = TestData.file(TestData.TEST_PUBLIC_KEY_DSA).readText()
        val key = PublicKeyFactory.fromPemString(pem)
        assertEquals("DSA", key.algorithm)
    }

    @Test
    fun fromPemStringEd25519Unsupported() {
        val pem = TestData.file(TestData.TEST_PUBLIC_KEY_ED25519).readText()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            PublicKeyFactory.fromPemString(pem)
        }
        assertEquals("Unsupported key type 1.3.101.112", exception.message)
    }

    @Test
    fun fromPemStringDH() {
        val pem = TestData.file(TestData.TEST_PUBLIC_KEY_DH).readText()
        val key = PublicKeyFactory.fromPemString(pem)
        assertEquals("DH", key.algorithm)
    }
}
