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

package com.appmattus.certificatetransparency.logclient.model

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo

/**
 * @property issuerKeyHash [issuerKeyHash] is the SHA-256 hash of the certificate issuer's public key, calculated over the DER encoding of the
 * key represented as [SubjectPublicKeyInfo].  This is needed to bind the issuer to the final certificate.
 * @property tbsCertificate [tbsCertificate] is the DER-encoded TBSCertificate (see [RFC5280](https://tools.ietf.org/html/rfc5280)) component of
 * the Precertificate -- that is, without the signature and the poison extension.  If the Precertificate is not signed with the CA certificate
 * that will issue the final certificate, then the TBSCertificate also has its issuer changed to that of the CA that will issue the final
 * certificate.  Note that it is also possible to reconstruct this TBSCertificate from the final certificate by extracting the TBSCertificate
 * from it and deleting the SCT extension. Also note that since the TBSCertificate contains an AlgorithmIdentifier that must match both the
 * Precertificate signature algorithm and final certificate signature algorithm, they must be signed with the same algorithm and parameters.  If
 * the Precertificate is issued using a Precertificate Signing Certificate and an Authority Key Identifier extension is present in the
 * TBSCertificate, the corresponding extension must also be present in the Precertificate Signing Certificate -- in this case, the
 * TBSCertificate also has its Authority Key Identifier changed to match the final issuer.
 */
public data class PreCertificate(
    public val issuerKeyHash: ByteArray? = null,
    public val tbsCertificate: ByteArray? = null
) {

    @Suppress("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreCertificate

        if (issuerKeyHash != null) {
            if (other.issuerKeyHash == null) return false
            if (!issuerKeyHash.contentEquals(other.issuerKeyHash)) return false
        } else if (other.issuerKeyHash != null) return false
        if (tbsCertificate != null) {
            if (other.tbsCertificate == null) return false
            if (!tbsCertificate.contentEquals(other.tbsCertificate)) return false
        } else if (other.tbsCertificate != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = issuerKeyHash?.contentHashCode() ?: 0
        result = 31 * result + (tbsCertificate?.contentHashCode() ?: 0)
        return result
    }
}
