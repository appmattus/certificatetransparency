/*
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
 */

package com.babylon.certificatetransparency.internal.logclient.model.network

import com.babylon.certificatetransparency.internal.exceptions.CertificateTransparencyException
import com.babylon.certificatetransparency.internal.logclient.model.LogEntry
import com.babylon.certificatetransparency.internal.logclient.model.ParsedLogEntry
import com.babylon.certificatetransparency.internal.logclient.model.network.GetEntriesResponse.Entry
import com.babylon.certificatetransparency.internal.serialization.Deserializer
import com.babylon.certificatetransparency.internal.utils.Base64
import com.google.gson.annotations.SerializedName

/**
 * Note that this message is not signed -- the retrieved data can be verified by constructing the Merkle Tree Hash corresponding to a
 * retrieved STH.  All leaves MUST be v1.  However, a compliant v1 client MUST NOT construe an unrecognized MerkleLeafType or LogEntryType
 * value as an error.  This means it may be unable to parse some entries, but note that each client can inspect the entries it does recognize
 * as well as verify the integrity of the data by treating unrecognized leaves as opaque input to the tree.
 *
 * @property entries An array of [Entry] objects
 */
internal data class GetEntriesResponse(
    @SerializedName("entries") val entries: List<Entry>
) {
    /**
     * @property leafInput The base64-encoded MerkleTreeLeaf structure.
     * @property extraData The base64-encoded unsigned data pertaining to the log entry.  In the case of an
     * [LogEntry.X509ChainEntry], this is the "certificate_chain".  In the case of a
     * [LogEntry.PreCertificateChainEntry], this is the whole "PreCertificateChainEntry".
     */
    data class Entry(
        @SerializedName("leaf_input") val leafInput: String,
        @SerializedName("extra_data") val extraData: String
    )

    /**
     * Parses CT log's response for "get-entries" into a list of [ParsedLogEntry] objects.
     *
     * @return list of Log's entries.
     */
    fun toParsedLogEntries(): List<ParsedLogEntry> {
        return entries.map {
            val leafInput = try {
                Base64.decode(it.leafInput)
            } catch (@Suppress("TooGenericExceptionCaught") ignored: Exception) {
                throw CertificateTransparencyException("Bad response. The leafInput is invalid.")
            }

            val extraData = try {
                Base64.decode(it.extraData)
            } catch (@Suppress("TooGenericExceptionCaught") ignored: Exception) {
                throw CertificateTransparencyException("Bad response. The extraData is invalid.")
            }

            Deserializer.parseLogEntry(leafInput.inputStream(), extraData.inputStream())
        }
    }
}
