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

import com.appmattus.certificatetransparency.logclient.internal.serialization.Deserializer
import com.appmattus.certificatetransparency.logclient.internal.utils.Base64
import com.appmattus.certificatetransparency.logclient.model.LogId
import com.appmattus.certificatetransparency.logclient.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.logclient.model.Version
import com.google.gson.annotations.SerializedName
import java.io.IOException

/**
 * If the "sctVersion" is not v1, then a v1 client may be unable to verify the signature. It MUST NOT construe this as an error. (Note: Log
 * clients don't need to be able to verify this structure; only TLS clients do.  If we were to serve the structure as a binary blob, then we
 * could completely change it without requiring an upgrade to v1 clients.
 *
 * @property sctVersion The version of the SignedCertificateTimestamp structure, in decimal.  A compliant v1 implementation MUST NOT expect
 * this to be 0 (i.e., v1).
 * @property id The log ID, base64 encoded.  Since log clients who request an SCT for inclusion in TLS handshakes are not required to verify
 * it, we do not assume they know the ID of the log.
 * @property timestamp The SCT timestamp, in decimal.
 * @property extensions An opaque type for future expansion.  It is likely that not all participants will need to understand data in this
 * field.  Logs should set this to the empty string.  Clients should decode the base64-encoded data and include it in the SCT.
 * @property signature The SCT signature, base64 encoded.
 */
internal data class AddChainResponse(
    @SerializedName("sct_version") val sctVersion: Int,
    @SerializedName("id") val id: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("extensions") val extensions: String,
    @SerializedName("signature") val signature: String
) {

    /**
     * Parses the CT Log's json response into a SignedCertificateTimestamp.
     *
     * @return SCT filled from the JSON input.
     * @throws IOException
     */
    fun toSignedCertificateTimestamp(): SignedCertificateTimestamp {
        return SignedCertificateTimestamp(
            sctVersion = Version.forNumber(sctVersion),
            id = LogId(Base64.decode(id)),
            timestamp = timestamp,
            extensions = if (extensions.isNotEmpty()) Base64.decode(extensions) else ByteArray(0),
            signature = Deserializer.parseDigitallySignedFromBinary(Base64.decode(signature).inputStream())
        )
    }
}
