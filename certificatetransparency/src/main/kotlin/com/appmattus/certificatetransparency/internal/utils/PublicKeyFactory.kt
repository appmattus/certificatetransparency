/*
 * Copyright 2021-2023 Appmattus Limited
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
 */

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.internal.utils.asn1.query.query
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

internal object PublicKeyFactory {

    /**
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws IllegalArgumentException Unsupported key type
     */
    fun fromByteArray(bytes: ByteArray): PublicKey {
        val keyFactory = KeyFactory.getInstance(determineKeyAlgorithm(bytes))
        return keyFactory.generatePublic(X509EncodedKeySpec(bytes))
    }

    fun fromPemString(keyText: String): PublicKey {
        // Equivalent of val pemContent = PemReader(StringReader(keyText)).readPemObject().content
        val start = keyText.indexOf(publicKeyStart)
        val end = keyText.indexOf(publicKeyEnd)
        require(start >= 0 && end >= 0) { "Missing public key entry in PEM file" }
        val pemContent = Base64.decode(
            keyText.substring(start + publicKeyStart.length, end).replace("\\s+".toRegex(), "")
        )

        return fromByteArray(pemContent)
    }

    /**
     * Parses the beginning of a key, and determines the key algorithm (RSA or EC) based on the OID
     */
    fun determineKeyAlgorithm(keyBytes: ByteArray): String {
        /* Equivalent of the following code
        val seq = ASN1Sequence.getInstance(keyBytes)
        val seq1 = seq.objects.nextElement() as DLSequence
        return when (val oid = seq1.objects.nextElement() as ASN1ObjectIdentifier) {
            PKCSObjectIdentifiers.rsaEncryption -> "RSA"
            X9ObjectIdentifiers.id_ecPublicKey -> "EC"
            else -> throw IllegalArgumentException("Unsupported key type $oid")
        }*/
        return when (val oid = keyBytes.toAsn1().query { seq().first().seq().first().oid() }) {
            "1.2.840.113549.1.1.1" -> "RSA"
            "1.2.840.10045.2.1" -> "EC"
            "1.2.840.10040.4.1" -> "DSA"
            "1.2.840.113549.1.3.1" -> "DH"
            else -> throw IllegalArgumentException("Unsupported key type $oid")
        }
    }

    private const val publicKeyStart = "-----BEGIN PUBLIC KEY-----"
    private const val publicKeyEnd = "-----END PUBLIC KEY-----"
}
