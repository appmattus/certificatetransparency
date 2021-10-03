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
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import java.security.cert.Certificate

/**
 * A [SignatureVerifier] verifies a Signed Certificate Timestamp is trusted by a particular log server
 */
internal interface SignatureVerifier {

    /**
     * Verifies the signature of a Signed Certificate Timestamp and certificate. Works for the following cases:
     *   Ordinary X509 certificate sent to the log.
     *   PreCertificate signed by an ordinary CA certificate.
     *   PreCertificate signed by a PreCertificate Signing Cert. In this case the PreCertificate signing certificate must be 2nd on the chain,
     *   the CA cert itself 3rd.
     *
     * @property sct SignedCertificateTimestamp received from the log.
     * @property chain The certificates chain as sent to the log.
     * @return [SctVerificationResult.Valid] if the log's signature over this SCT can be verified, [SctVerificationResult.Invalid] otherwise.
     */
    fun verifySignature(sct: SignedCertificateTimestamp, chain: List<Certificate>): SctVerificationResult
}
