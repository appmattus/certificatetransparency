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

package com.babylon.certificatetransparency.internal.serialization

import com.babylon.certificatetransparency.internal.exceptions.SerializationException
import com.babylon.certificatetransparency.internal.logclient.model.DigitallySigned
import com.babylon.certificatetransparency.internal.logclient.model.LogEntry
import com.babylon.certificatetransparency.internal.logclient.model.LogEntryType
import com.babylon.certificatetransparency.internal.logclient.model.LogId
import com.babylon.certificatetransparency.internal.logclient.model.MerkleAuditProof
import com.babylon.certificatetransparency.internal.logclient.model.MerkleTreeLeaf
import com.babylon.certificatetransparency.internal.logclient.model.ParsedLogEntry
import com.babylon.certificatetransparency.internal.logclient.model.ParsedLogEntryWithProof
import com.babylon.certificatetransparency.internal.logclient.model.PreCertificate
import com.babylon.certificatetransparency.internal.logclient.model.SignedCertificateTimestamp
import com.babylon.certificatetransparency.internal.logclient.model.SignedEntry
import com.babylon.certificatetransparency.internal.logclient.model.TimestampedEntry
import com.babylon.certificatetransparency.internal.logclient.model.Version
import com.babylon.certificatetransparency.internal.utils.Base64
import java.io.IOException
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.log2

/** Converting binary data to CT structures.  */
//
@Suppress("TooManyFunctions")
internal object Deserializer {
    private const val TIMESTAMPED_ENTRY_LEAF_TYPE = 0

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
    fun parseDigitallySignedFromBinary(inputStream: InputStream): DigitallySigned {
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
     * Parses an entry retrieved from Log and it's audit proof.
     *
     * @property entry ParsedLogEntry instance.
     * @property proof An array of base64-encoded Merkle Tree nodes proving the inclusion of the chosen
     * certificate.
     * @property leafIndex The index of the desired entry.
     * @property treeSize The tree size of the tree for which the proof is desired.
     * @return [ParsedLogEntryWithProof]
     */
    fun parseLogEntryWithProof(entry: ParsedLogEntry, proof: List<String>, leafIndex: Long, treeSize: Long): ParsedLogEntryWithProof {
        return ParsedLogEntryWithProof(
            entry,
            parseAuditProof(proof, leafIndex, treeSize)
        )
    }

    /**
     * Parses the audit proof retrieved from Log.
     *
     * @property proof An array of base64-encoded Merkle Tree nodes proving the inclusion of the chosen
     * certificate.
     * @property leafIndex The index of the desired entry.
     * @property treeSize The tree size of the tree for which the proof is desired.
     * @return [MerkleAuditProof]
     */
    fun parseAuditProof(proof: List<String>, leafIndex: Long, treeSize: Long) =
        MerkleAuditProof(
            Version.V1,
            treeSize,
            leafIndex,
            proof.map(Base64::decode)
        )

    /**
     * Parses an entry retrieved from Log.
     *
     * @property merkleTreeLeaf MerkleTreeLeaf structure, byte stream of binary encoding.
     * @property extraData extra data, byte stream of binary encoding.
     * @return [ParsedLogEntry]
     */
    fun parseLogEntry(merkleTreeLeaf: InputStream, extraData: InputStream): ParsedLogEntry {
        val treeLeaf = parseMerkleTreeLeaf(merkleTreeLeaf)

        val logEntry = when (treeLeaf.timestampedEntry.signedEntry) {
            is SignedEntry.X509 -> {
                parseX509ChainEntry(extraData, treeLeaf.timestampedEntry.signedEntry.x509)
            }
            is SignedEntry.PreCertificate -> {
                parsePreCertificateChainEntry(extraData, treeLeaf.timestampedEntry.signedEntry.preCertificate)
            }
        }

        return ParsedLogEntry(treeLeaf, logEntry)
    }

    /**
     * Parses a [MerkleTreeLeaf] from binary encoding.
     *
     * @property inputStream byte stream of binary encoding.
     * @return Built [MerkleTreeLeaf].
     * @throws SerializationException if the data stream is too short.
     */
    private fun parseMerkleTreeLeaf(inputStream: InputStream): MerkleTreeLeaf {
        val version = inputStream.readNumber(CTConstants.VERSION_LENGTH).toInt()
        if (version != Version.V1.number) {
            throw SerializationException("Unknown version: $version")
        }

        val leafType = inputStream.readNumber(1).toInt()
        if (leafType != TIMESTAMPED_ENTRY_LEAF_TYPE) {
            throw SerializationException("Unknown entry type: $leafType")
        }

        return MerkleTreeLeaf(
            Version.forNumber(
                version
            ),
            parseTimestampedEntry(inputStream)
        )
    }

    /**
     * Parses a [TimestampedEntry] from binary encoding.
     *
     * @property inputStream byte stream of binary encoding.
     * @return Built [TimestampedEntry].
     * @throws SerializationException if the data stream is too short.
     */
    private fun parseTimestampedEntry(inputStream: InputStream): TimestampedEntry {
        val timestamp = inputStream.readNumber(CTConstants.TIMESTAMP_LENGTH)

        val entryType = inputStream.readNumber(CTConstants.LOG_ENTRY_TYPE_LENGTH).toInt()
        val logEntryType = LogEntryType.forNumber(entryType)

        val signedEntry = when (logEntryType) {
            LogEntryType.X509_ENTRY -> {
                val length = inputStream.readNumber(THREE_BYTES).toInt()
                SignedEntry.X509(inputStream.readFixedLength(length))
            }
            LogEntryType.PRE_CERTIFICATE_ENTRY -> {
                val issuerKeyHash = inputStream.readFixedLength(THIRTY_TWO_BYTES)

                val length = inputStream.readNumber(2).toInt()
                val tbsCertificate = inputStream.readFixedLength(length)

                SignedEntry.PreCertificate(
                    PreCertificate(
                        issuerKeyHash = issuerKeyHash,
                        tbsCertificate = tbsCertificate
                    )
                )
            }
            else -> throw SerializationException("Unknown entry type: $entryType")
        }

        return TimestampedEntry(
            timestamp = timestamp,
            signedEntry = signedEntry
        )
    }

    /**
     * Parses X509ChainEntry structure.
     *
     * @property inputStream X509ChainEntry structure, byte stream of binary encoding.
     * @property x509Cert leaf certificate.
     * @throws SerializationException if an I/O error occurs.
     * @return [LogEntry.X509ChainEntry] object.
     */
    private fun parseX509ChainEntry(inputStream: InputStream, x509Cert: ByteArray?): LogEntry.X509ChainEntry {
        val certificateChain = mutableListOf<ByteArray>()

        try {
            if (inputStream.readNumber(THREE_BYTES) != inputStream.available().toLong()) {
                throw SerializationException("Extra data corrupted.")
            }
            while (inputStream.available() > 0) {
                val length = inputStream.readNumber(THREE_BYTES).toInt()
                certificateChain.add(inputStream.readFixedLength(length))
            }
        } catch (expected: IOException) {
            throw SerializationException("Cannot parse xChainEntry. ${expected.localizedMessage}")
        }

        return LogEntry.X509ChainEntry(
            leafCertificate = x509Cert,
            certificateChain = certificateChain.toList()
        )
    }

    /**
     * Parses PreCertificateChainEntry structure.
     *
     * @property inputStream PreCertificateChainEntry structure, byte stream of binary encoding.
     * @property preCertificate Pre-certificate.
     * @return [LogEntry.PreCertificateChainEntry] object.
     */
    private fun parsePreCertificateChainEntry(inputStream: InputStream, preCertificate: PreCertificate): LogEntry.PreCertificateChainEntry {
        val preCertificateChain = mutableListOf<ByteArray>()

        try {
            if (inputStream.readNumber(THREE_BYTES) != inputStream.available().toLong()) {
                throw SerializationException("Extra data corrupted.")
            }
            while (inputStream.available() > 0) {
                val length = inputStream.readNumber(THREE_BYTES).toInt()
                preCertificateChain.add(inputStream.readFixedLength(length))
            }
        } catch (expected: IOException) {
            throw SerializationException("Cannot parse PrecertEntryChain.${expected.localizedMessage}")
        }

        return LogEntry.PreCertificateChainEntry(
            preCertificate = preCertificate,
            preCertificateChain = preCertificateChain.toList()
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
    private const val THREE_BYTES = 3
    private const val THIRTY_TWO_BYTES = 32
    private const val BITS_IN_BYTE = 8
}
