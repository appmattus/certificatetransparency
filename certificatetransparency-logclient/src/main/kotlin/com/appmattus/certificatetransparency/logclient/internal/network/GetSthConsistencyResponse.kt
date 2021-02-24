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

package com.appmattus.certificatetransparency.logclient.internal.network

import com.appmattus.certificatetransparency.logclient.internal.utils.Base64
import com.google.gson.annotations.SerializedName

/**
 * Note that no signature is required on this data, as it is used to verify an STH, which is signed.
 *
 * @property consistency An array of Merkle Tree nodes, base64 encoded.
 */
internal data class GetSthConsistencyResponse(
    @SerializedName("consistency") val consistency: List<String>
) {

    /**
     * Parses CT log's response for the "get-sth-consistency" request.
     *
     * @return A list of base64 decoded Merkle Tree nodes serialized to ByteString objects.
     */
    fun toMerkleTreeNodes(): List<ByteArray> {
        return consistency.map { Base64.decode(it) }
    }
}
