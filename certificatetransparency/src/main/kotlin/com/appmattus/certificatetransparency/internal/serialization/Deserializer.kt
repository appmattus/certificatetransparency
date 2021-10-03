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

package com.appmattus.certificatetransparency.internal.serialization

import com.appmattus.certificatetransparency.internal.exceptions.SerializationException
import com.appmattus.certificatetransparency.internal.verifier.model.DigitallySigned
import com.appmattus.certificatetransparency.internal.verifier.model.LogId
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.internal.verifier.model.Version
import java.io.IOException
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.log2

/** Converting binary data to CT structures.  */
internal object Deserializer {

    /**
     * Parses a SignedCertificateTimestamp from binary encoding.
     *
     * @property inputStream byte stream of binary encoding.
     * @return Built SignedCertificateTimestamp
     * @throws SerializationException if the data stream is too short.
     * @throws IOException
     */
    fun parseSctFromBinary(inputStream: InputStream): SignedCertificateTimestamp {
        val version = Version.forNumber(inputStream.readNumber(1 /* single byte */).toInt())
        if (version != Version.V1) {
            throw SerializationException("Unknown version: $version")
        }

        val keyId = inputStream.readFixedLength(CTConstants.KEY_ID_LENGTH)

        val timestamp = inputStream.readNumber(CTConstants.TIMESTAMP_LENGTH)

        val extensions = inputStream.readVariableLength(CTConstants.MAX_EXTENSIONS_LENGTH)

        val signature = parseDigitallySignedFromBinary(inputStream)

        return SignedCertificateTimestamp(
            sctVersion = version,
            id = LogId(keyId),
            timestamp = timestamp,
            extensions = extensions,
            signature = signature
        )
    }

    /**
     * Parses a DigitallySigned from binary encoding.
     *
     * @property inputStream byte stream of binary encoding.
     * @return Built DigitallySigned
     * @throws SerializationException if the data stream is too short.
     * @throws IOException
     */
    private fun parseDigitallySignedFromBinary(inputStream: InputStream): DigitallySigned {
        val hashAlgorithmByte = inputStream.readNumber(1 /* single byte */).toInt()
        val hashAlgorithm = DigitallySigned.HashAlgorithm.forNumber(hashAlgorithmByte)
            ?: throw SerializationException("Unknown hash algorithm: ${hashAlgorithmByte.toString(HEX_RADIX)}")

        val signatureAlgorithmByte = inputStream.readNumber(1 /* single byte */).toInt()
        val signatureAlgorithm = DigitallySigned.SignatureAlgorithm.forNumber(signatureAlgorithmByte)
            ?: throw SerializationException("Unknown signature algorithm: ${signatureAlgorithmByte.toString(HEX_RADIX)}")

        val signature = inputStream.readVariableLength(CTConstants.MAX_SIGNATURE_LENGTH)

        return DigitallySigned(
            hashAlgorithm = hashAlgorithm,
            signatureAlgorithm = signatureAlgorithm,
            signature = signature
        )
    }

    /**
     * Calculates the number of bytes needed to hold the given number: ceil(log2(maxDataLength)) / 8
     *
     * @property maxDataLength the number that needs to be represented as bytes
     * @return Number of bytes needed to represent the given number
     */
    fun bytesForDataLength(maxDataLength: Int): Int {
        return (ceil(log2(maxDataLength.toDouble())) / BITS_IN_BYTE).toInt()
    }

    private const val HEX_RADIX = 16
    private const val BITS_IN_BYTE = 8
}
