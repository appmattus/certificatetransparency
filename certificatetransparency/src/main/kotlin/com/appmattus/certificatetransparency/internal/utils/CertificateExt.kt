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
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.internal.serialization.CTConstants.POISON_EXTENSION_OID
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.PRECERTIFICATE_SIGNING_OID
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.SCT_CERTIFICATE_OID
import com.appmattus.certificatetransparency.internal.verifier.model.IssuerInformation
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/** Helper class for finding out all kinds of information about a certificate.  */

private const val X509_AUTHORITY_KEY_IDENTIFIER = "2.5.29.35"

/**
 * @throws java.security.cert.CertificateParsingException
 */
@OptIn(ExperimentalContracts::class)
internal fun Certificate.isPreCertificateSigningCert(): Boolean {
    contract {
        returns(true) implies (this@isPreCertificateSigningCert is X509Certificate)
    }
    return this is X509Certificate && extendedKeyUsage?.contains(PRECERTIFICATE_SIGNING_OID) == true
}

@OptIn(ExperimentalContracts::class)
internal fun Certificate.isPreCertificate(): Boolean {
    contract {
        returns(true) implies (this@isPreCertificate is X509Certificate)
    }
    return this is X509Certificate && criticalExtensionOIDs?.contains(POISON_EXTENSION_OID) == true
}

@OptIn(ExperimentalContracts::class)
internal fun Certificate.hasEmbeddedSct(): Boolean {
    contract {
        returns(true) implies (this@hasEmbeddedSct is X509Certificate)
    }
    return this is X509Certificate && nonCriticalExtensionOIDs?.contains(SCT_CERTIFICATE_OID) == true
}

// Produces issuer information in case the PreCertificate is signed by a regular CA cert,
// not PreCertificate Signing Cert. In this case, the only thing that's needed is the
// issuer key hash - the Precertificate will already have the right value for the issuer
// name and K509 Authority Key Identifier extension.
internal fun Certificate.issuerInformation(): IssuerInformation {
    return IssuerInformation(keyHash = keyHash(), issuedByPreCertificateSigningCert = false)
}

/**
 * @throws CertificateEncodingException
 * @throws NoSuchAlgorithmException
 * @throws IOException
 */
internal fun Certificate.issuerInformationFromPreCertificate(preCertificate: Certificate): IssuerInformation {
    ASN1InputStream(encoded).use { aIssuerIn ->
        val parsedIssuerCert = org.bouncycastle.asn1.x509.Certificate.getInstance(aIssuerIn.readObject())

        val issuerExtensions = parsedIssuerCert.tbsCertificate.extensions
        val x509authorityKeyIdentifier = issuerExtensions?.getExtension(ASN1ObjectIdentifier(X509_AUTHORITY_KEY_IDENTIFIER))

        return IssuerInformation(parsedIssuerCert.issuer, preCertificate.keyHash(), x509authorityKeyIdentifier, true)
    }
}

private fun Certificate.keyHash() = publicKey.sha256Hash()
