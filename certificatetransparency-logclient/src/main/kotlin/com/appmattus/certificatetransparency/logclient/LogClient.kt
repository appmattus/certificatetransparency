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

package com.appmattus.certificatetransparency.logclient

import com.appmattus.certificatetransparency.logclient.model.MerkleAuditProof
import com.appmattus.certificatetransparency.logclient.model.ParsedLogEntry
import com.appmattus.certificatetransparency.logclient.model.ParsedLogEntryWithProof
import com.appmattus.certificatetransparency.logclient.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.logclient.model.SignedTreeHead
import java.security.cert.Certificate

/**
 * [LogClient] represents a log server which can be used, for example, to ensure a Signed Certificate Timestamp really has been logged
 */
public interface LogClient {

    /**
     * Retrieves Latest Signed Tree Head from the log. The signature of the Signed Tree Head component
     * is not verified.
     *
     * @return latest STH
     */
    public val logSth: SignedTreeHead

    /**
     * Retrieves accepted Root Certificates.
     *
     * @return a list of root certificates.
     */
    public val logRoots: List<Certificate>

    /**
     * Adds a certificate to the log.
     *
     * @property certificatesChain The certificate chain to add.
     * @return SignedCertificateTimestamp if the log added the chain successfully.
     */
    public fun addCertificate(certificatesChain: List<Certificate>): SignedCertificateTimestamp

    /**
     * Retrieve Entries from Log.
     *
     * @property start 0-based index of first entry to retrieve, in decimal.
     * @property end 0-based index of last entry to retrieve, in decimal.
     * @return list of Log's entries.
     */
    public fun getLogEntries(start: Long, end: Long): List<ParsedLogEntry>

    /**
     * Retrieve Merkle Consistency Proof between Two Signed Tree Heads.
     *
     * @property first The tree_size of the first tree, in decimal.
     * @property second The tree_size of the second tree, in decimal.
     * @return A list of base64 decoded Merkle Tree nodes serialized to ByteString objects.
     */
    public fun getSthConsistency(first: Long, second: Long): List<ByteArray>

    /**
     * Retrieve Entry+Merkle Audit Proof from Log.
     *
     * @property leafIndex The index of the desired entry.
     * @property treeSize The tree_size of the tree for which the proof is desired.
     * @return ParsedLog entry object with proof.
     */
    public fun getLogEntryAndProof(leafIndex: Long, treeSize: Long): ParsedLogEntryWithProof

    /**
     * Retrieve Merkle Audit Proof from Log by Merkle Leaf Hash.
     *
     * @property leafHash sha256 hash of MerkleTreeLeaf.
     * @return MerkleAuditProof object.
     */
    public fun getProofByHash(leafHash: ByteArray): MerkleAuditProof

    /**
     * Retrieve Merkle Audit Proof from Log by Merkle Leaf Hash.
     *
     * @property encodedMerkleLeafHash Base64 encoded of sha256 hash of MerkleTreeLeaf.
     * @property treeSize The tree_size of the tree for which the proof is desired. It can be obtained
     * from latest STH.
     * @return MerkleAuditProof object.
     */
    public fun getProofByEncodedHash(encodedMerkleLeafHash: String, treeSize: Long): MerkleAuditProof
}
