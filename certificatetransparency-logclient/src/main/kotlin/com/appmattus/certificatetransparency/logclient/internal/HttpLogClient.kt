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
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.logclient.internal

import com.appmattus.certificatetransparency.logclient.LogClient
import com.appmattus.certificatetransparency.logclient.internal.exceptions.CertificateTransparencyException
import com.appmattus.certificatetransparency.logclient.internal.network.AddChainRequest
import com.appmattus.certificatetransparency.logclient.internal.serialization.Deserializer
import com.appmattus.certificatetransparency.logclient.internal.utils.Base64
import com.appmattus.certificatetransparency.logclient.internal.utils.isPreCertificate
import com.appmattus.certificatetransparency.logclient.internal.utils.isPreCertificateSigningCert
import com.appmattus.certificatetransparency.logclient.model.MerkleAuditProof
import com.appmattus.certificatetransparency.logclient.model.ParsedLogEntry
import com.appmattus.certificatetransparency.logclient.model.ParsedLogEntryWithProof
import com.appmattus.certificatetransparency.logclient.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.logclient.model.SignedTreeHead
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException

/** A CT HTTP client. Abstracts away the json encoding necessary for the server.
 *
 * @constructor New HttpLogClient.
 * @property ctService CtService pointing to CT Log's full URL, e.g. "http://ct.googleapis.com/pilot/ct/v1/"
 */
internal class HttpLogClient(private val ctService: LogClientService) : LogClient {

    /**
     * Retrieves Latest Signed Tree Head from the log. The signature of the Signed Tree Head component
     * is not verified.
     *
     * @return latest STH
     */
    override val logSth: SignedTreeHead by lazy {
        ctService.getSth().execute().body()!!.toSignedTreeHead()
    }

    /**
     * Retrieves accepted Root Certificates.
     *
     * @return a list of root certificates.
     */
    override val logRoots: List<Certificate> by lazy {
        ctService.getRoots().execute().body()!!.toRootCertificates()
    }

    /**
     * JSON-encodes the list of certificates into a JSON object.
     *
     * @property certs Certificates to encode.
     * @return A JSON object with one field, "chain", holding a JSON array of base64-encoded certs.
     */
    internal fun encodeCertificates(certs: List<Certificate>): AddChainRequest {
        try {
            return AddChainRequest(
                certs.map {
                    Base64.toBase64String(it.encoded)
                }
            )
        } catch (e: CertificateEncodingException) {
            throw CertificateTransparencyException("Error encoding certificate", e)
        }
    }

    /**
     * Adds a certificate to the log.
     *
     * @property certificatesChain The certificate chain to add.
     * @return SignedCertificateTimestamp if the log added the chain successfully.
     */
    override fun addCertificate(certificatesChain: List<Certificate>): SignedCertificateTimestamp {
        require(certificatesChain.isNotEmpty()) { "Must have at least one certificate to submit." }

        val isPreCertificate = certificatesChain[0].isPreCertificate()
        if (isPreCertificate && certificatesChain[1].isPreCertificateSigningCert()) {
            // Must have a chain of at least 3 certificates when pre-certificates are involved
            @Suppress("MagicNumber")
            require(certificatesChain.size >= 3) {
                "When signing a PreCertificate with a PreCertificate Signing Cert, the issuer certificate must follow."
            }
        }

        return addCertificate(certificatesChain, isPreCertificate)
    }

    private fun addCertificate(certificatesChain: List<Certificate>, isPreCertificate: Boolean): SignedCertificateTimestamp {
        val jsonPayload = encodeCertificates(certificatesChain)

        val call = if (isPreCertificate) ctService.addPreChain(jsonPayload) else ctService.addChain(jsonPayload)

        return call.execute().body()!!.toSignedCertificateTimestamp()
    }

    /**
     * Retrieve Entries from Log.
     *
     * @property start 0-based index of first entry to retrieve, in decimal.
     * @property end 0-based index of last entry to retrieve, in decimal.
     * @return list of Log's entries.
     */
    override fun getLogEntries(start: Long, end: Long): List<ParsedLogEntry> {
        require(start in 0..end)

        return ctService.getEntries(start, end).execute().body()!!.toParsedLogEntries()
    }

    /**
     * Retrieve Merkle Consistency Proof between Two Signed Tree Heads.
     *
     * @property first The tree_size of the first tree, in decimal.
     * @property second The tree_size of the second tree, in decimal.
     * @return A list of base64 decoded Merkle Tree nodes serialized to ByteString objects.
     */
    override fun getSthConsistency(first: Long, second: Long): List<ByteArray> {
        require(first in 0..second)

        return ctService.getSthConsistency(first, second).execute().body()!!.toMerkleTreeNodes()
    }

    /**
     * Retrieve Entry+Merkle Audit Proof from Log.
     *
     * @property leafIndex The index of the desired entry.
     * @property treeSize The tree_size of the tree for which the proof is desired.
     * @return ParsedLog entry object with proof.
     */
    override fun getLogEntryAndProof(leafIndex: Long, treeSize: Long): ParsedLogEntryWithProof {
        require(leafIndex in 0..treeSize)

        val response = ctService.getEntryAndProof(leafIndex, treeSize).execute().body()!!

        val logEntry = Deserializer.parseLogEntry(
            Base64.decode(response.leafInput).inputStream(),
            Base64.decode(response.extraData).inputStream()
        )

        return Deserializer.parseLogEntryWithProof(logEntry, response.auditPath, leafIndex, treeSize)
    }

    /**
     * Retrieve Merkle Audit Proof from Log by Merkle Leaf Hash.
     *
     * @property leafHash sha256 hash of MerkleTreeLeaf.
     * @return MerkleAuditProof object.
     */
    override fun getProofByHash(leafHash: ByteArray): MerkleAuditProof {
        require(leafHash.isNotEmpty())
        val encodedMerkleLeafHash = Base64.toBase64String(leafHash)
        val sth = logSth
        return getProofByEncodedHash(encodedMerkleLeafHash, sth.treeSize)
    }

    /**
     * Retrieve Merkle Audit Proof from Log by Merkle Leaf Hash.
     *
     * @property encodedMerkleLeafHash Base64 encoded of sha256 hash of MerkleTreeLeaf.
     * @property treeSize The tree_size of the tree for which the proof is desired. It can be obtained
     * from latest STH.
     * @return MerkleAuditProof object.
     */
    override fun getProofByEncodedHash(encodedMerkleLeafHash: String, treeSize: Long): MerkleAuditProof {
        require(encodedMerkleLeafHash.isNotEmpty())
        val response = ctService.getProofByHash(treeSize, encodedMerkleLeafHash).execute().body()!!
        return Deserializer.parseAuditProof(response.auditPath, response.leafIndex, treeSize)
    }
}
