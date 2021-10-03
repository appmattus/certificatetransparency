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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.internal.utils.stringStackTrace
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.security.cert.CertificateParsingException

internal data class UnsupportedSignatureAlgorithm(
    val algorithm: String,
    override val exception: NoSuchAlgorithmException? = null
) : SctVerificationResult.Invalid.FailedWithException() {

    override fun toString() = if (exception != null) {
        "Unsupported signature algorithm $algorithm with: ${exception.stringStackTrace()}"
    } else {
        "Unsupported signature algorithm $algorithm"
    }
}

internal data class LogPublicKeyNotValid(override val exception: InvalidKeyException) : SctVerificationResult.Invalid.FailedWithException() {
    override fun toString() = "Log's public key cannot be used with ${exception.stringStackTrace()}"
}

internal data class SignatureNotValid(override val exception: SignatureException) : SctVerificationResult.Invalid.FailedWithException() {
    override fun toString() =
        "Signature object not properly initialized or signature from SCT is improperly encoded with: ${exception.stringStackTrace()}"
}

internal data class LogIdMismatch(val sctLogId: String, val logServerId: String) : SctVerificationResult.Invalid.Failed() {
    override fun toString() = "Log ID of SCT, $sctLogId, does not match this log's ID, $logServerId"
}

internal object NoIssuer : SctVerificationResult.Invalid.Failed() {
    override fun toString() = "Chain with PreCertificate or Certificate must contain issuer"
}

internal object NoIssuerWithPreCert : SctVerificationResult.Invalid.Failed() {
    override fun toString() = "Chain with PreCertificate signed by PreCertificate Signing Cert must contain issuer"
}

internal data class CertificateEncodingFailed(override val exception: Exception) : SctVerificationResult.Invalid.FailedWithException() {
    override fun toString() = "Certificate could not be encoded with: ${exception.stringStackTrace()}"
}

internal data class CertificateParsingFailed(
    override val exception: CertificateParsingException
) : SctVerificationResult.Invalid.FailedWithException() {
    override fun toString() = "Error parsing cert with: ${exception.stringStackTrace()}"
}

internal data class ASN1ParsingFailed(override val exception: IOException) : SctVerificationResult.Invalid.FailedWithException() {
    override fun toString() = "Error during ASN.1 parsing of certificate with: ${exception.stringStackTrace()}"
}
