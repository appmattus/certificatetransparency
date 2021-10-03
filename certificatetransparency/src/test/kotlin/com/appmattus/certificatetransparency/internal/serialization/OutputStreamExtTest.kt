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
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.serialization

import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.verifier.model.DigitallySigned
import com.appmattus.certificatetransparency.internal.verifier.model.LogId
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.internal.verifier.model.Version
import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

/** Test serialization.  */
internal class OutputStreamExtTest {

    @Test
    fun serializeSct() {
        val keyIdBase64 = "3xwuwRUAlFJHqWFoMl3cXHlZ6PfG04j8AC4LvT9012Q="

        val signatureBase64 = "MEUCIGBuEK5cLVobCu1J3Ek39I3nGk6XhOnCCN+/6e9TbPfyAiEAvrKcctfQbWHQa9s4oGlGmqhv4S4Yu3zEVomiwBh+9aU="

        val signature = DigitallySigned(
            hashAlgorithm = DigitallySigned.HashAlgorithm.SHA256,
            signatureAlgorithm = DigitallySigned.SignatureAlgorithm.ECDSA,
            signature = Base64.decode(signatureBase64)
        )

        val sct = SignedCertificateTimestamp(
            sctVersion = Version.V1,
            timestamp = 1365181456089L,
            id = LogId(Base64.decode(keyIdBase64)),
            signature = signature,
            extensions = ByteArray(0)
        )

        val generatedBytes = serializeSctToBinary(sct)
        val readBytes = TestData.file(TestData.TEST_CERT_SCT).readBytes()
        assertArrayEquals(readBytes, generatedBytes)
    }

    private fun serializeSctToBinary(sct: SignedCertificateTimestamp): ByteArray {
        return ByteArrayOutputStream().use {
            it.writeUint(sct.sctVersion.number.toLong(), CTConstants.VERSION_LENGTH)
            it.write(sct.id.keyId)
            it.writeUint(sct.timestamp, CTConstants.TIMESTAMP_LENGTH)
            it.writeVariableLength(sct.extensions, CTConstants.MAX_EXTENSIONS_LENGTH)
            it.writeUint(sct.signature.hashAlgorithm.number.toLong(), HASH_ALG_LENGTH)
            it.writeUint(sct.signature.signatureAlgorithm.number.toLong(), SIGNATURE_ALG_LENGTH)
            it.writeVariableLength(sct.signature.signature, CTConstants.MAX_SIGNATURE_LENGTH)

            it.toByteArray()
        }
    }

    companion object {
        const val HASH_ALG_LENGTH = 1
        const val SIGNATURE_ALG_LENGTH = 1
    }
}
