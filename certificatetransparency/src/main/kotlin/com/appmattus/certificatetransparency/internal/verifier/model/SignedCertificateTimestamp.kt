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

package com.appmattus.certificatetransparency.internal.verifier.model

/**
 * A signed certificate timestamp. If the [sctVersion] is not [Version.V1], then a v1 client may be unable to verify the signature.
 *
 * @property sctVersion The version of the [SignedCertificateTimestamp] structure, in decimal.  A compliant v1 implementation MUST NOT expect
 * this to be 0 (i.e., [Version.V1]).
 * @property id The log ID, base64 encoded.  Since log clients who request an SCT for inclusion in TLS handshakes are not required to verify
 * it, we do not assume they know the ID of the log.
 * @property timestamp The SCT timestamp, in decimal. UTC time in milliseconds, since January 1, 1970, 00:00.
 * @property signature The SCT signature, base64 encoded.
 * @property extensions An opaque type for future expansion.  It is likely that not all participants will need to understand data in this
 * field.  Logs should set this to the empty string.  Clients should decode the base64-encoded data and include it in the SCT.
 */
public data class SignedCertificateTimestamp(
    val sctVersion: Version = Version.UNKNOWN_VERSION,
    val id: LogId,
    val timestamp: Long,
    val signature: DigitallySigned,
    val extensions: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignedCertificateTimestamp

        if (sctVersion != other.sctVersion) return false
        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (signature != other.signature) return false
        if (!extensions.contentEquals(other.extensions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sctVersion.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + extensions.contentHashCode()
        return result
    }
}
