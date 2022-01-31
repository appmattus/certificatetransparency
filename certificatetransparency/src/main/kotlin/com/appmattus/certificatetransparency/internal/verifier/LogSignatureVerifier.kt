/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.internal.serialization.CTConstants
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.LOG_ENTRY_TYPE_LENGTH
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.MAX_CERTIFICATE_LENGTH
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.MAX_EXTENSIONS_LENGTH
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.TIMESTAMP_LENGTH
import com.appmattus.certificatetransparency.internal.serialization.CTConstants.VERSION_LENGTH
import com.appmattus.certificatetransparency.internal.serialization.writeUint
import com.appmattus.certificatetransparency.internal.serialization.writeVariableLength
import com.appmattus.certificatetransparency.internal.utils.hasEmbeddedSct
import com.appmattus.certificatetransparency.internal.utils.isPreCertificate
import com.appmattus.certificatetransparency.internal.utils.isPreCertificateSigningCert
import com.appmattus.certificatetransparency.internal.utils.issuerInformation
import com.appmattus.certificatetransparency.internal.utils.issuerInformationFromPreCertificate
import com.appmattus.certificatetransparency.internal.verifier.model.IssuerInformation
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.internal.verifier.model.Version
import com.appmattus.certificatetransparency.loglist.LogServer
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.TBSCertificate
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator
import org.bouncycastle.util.encoders.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.Signature
import java.security.SignatureException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate

/**
 * Verifies signatures from a given CT Log.
 *
 * @constructor Creates a new LogSignatureVerifier which is associated with a single log.
 * @property logServer information of the log this verifier is to be associated with.
 */
internal class LogSignatureVerifier(private val logServer: LogServer) : SignatureVerifier {

    @Suppress("ReturnCount", "ComplexMethod")
    override fun verifySignature(sct: SignedCertificateTimestamp, chain: List<Certificate>): SctVerificationResult {

        // If the timestamp is in the future then we have to reject it
        val now = System.currentTimeMillis()
        if (sct.timestamp > now) {
            return SctVerificationResult.Invalid.FutureTimestamp(sct.timestamp, now)
        }

        if (logServer.validUntil != null && sct.timestamp > logServer.validUntil) {
            return SctVerificationResult.Invalid.LogServerUntrusted(sct.timestamp, logServer.validUntil)
        }

        if (!logServer.id.contentEquals(sct.id.keyId)) {
            return LogIdMismatch(Base64.toBase64String(sct.id.keyId), Base64.toBase64String(logServer.id))
        }

        val leafCert = chain[0]
        if (!leafCert.isPreCertificate() && !leafCert.hasEmbeddedSct()) {
            // When verifying final cert without embedded SCTs, we don't need the issuer but can verify directly
            return try {
                val toVerify = serializeSignedSctData(leafCert, sct)
                verifySctSignatureOverBytes(sct, toVerify)
            } catch (e: IOException) {
                CertificateEncodingFailed(e)
            } catch (e: CertificateEncodingException) {
                CertificateEncodingFailed(e)
            }
        }

        if (chain.size < 2) {
            return NoIssuer
        }

        // PreCertificate or final certificate with embedded SCTs, we want the issuerInformation
        val issuerCert = chain[1]

        val isPreCertificateSigningCert = try {
            issuerCert.isPreCertificateSigningCert()
        } catch (e: CertificateParsingException) {
            return CertificateParsingFailed(e)
        }

        val issuerInformation = if (!isPreCertificateSigningCert) {
            // If signed by the real issuing CA
            try {
                issuerCert.issuerInformation()
            } catch (e: NoSuchAlgorithmException) {
                return UnsupportedSignatureAlgorithm("SHA-256", e)
            }
        } else {
            // Must have at least 3 certificates when a pre-certificate is involved
            @Suppress("MagicNumber")
            if (chain.size < 3) {
                return NoIssuerWithPreCert
            }

            try {
                issuerCert.issuerInformationFromPreCertificate(chain[2])
            } catch (e: CertificateEncodingException) {
                return CertificateEncodingFailed(e)
            } catch (e: NoSuchAlgorithmException) {
                return UnsupportedSignatureAlgorithm("SHA-256", e)
            } catch (e: IOException) {
                return ASN1ParsingFailed(e)
            }
        }

        return verifySCTOverPreCertificate(sct, leafCert, issuerInformation)
    }

    /**
     * Verifies the CT Log's signature over the SCT and the PreCertificate, or a final certificate.
     *
     * @property sct SignedCertificateTimestamp received from the log.
     * @property certificate the PreCertificate sent to the log for addition, or the final certificate
     * with the embedded SCTs.
     * @property issuerInfo Information on the issuer which will (or did) ultimately sign this
     * PreCertificate. If the PreCertificate was signed using by a PreCertificate Signing Cert,
     * the issuerInfo contains data on the final CA certificate used for signing.
     * @return true if the SCT verifies, false otherwise.
     */
    internal fun verifySCTOverPreCertificate(
        sct: SignedCertificateTimestamp,
        certificate: X509Certificate,
        issuerInfo: IssuerInformation
    ): SctVerificationResult {
        return try {
            val preCertificateTBS = createTbsForVerification(certificate, issuerInfo)
            val toVerify = serializeSignedSctDataForPreCertificate(preCertificateTBS.encoded, issuerInfo.keyHash, sct)
            verifySctSignatureOverBytes(sct, toVerify)
        } catch (e: IOException) {
            CertificateEncodingFailed(e)
        } catch (e: CertificateException) {
            CertificateEncodingFailed(e)
        }
    }

    /**
     * @throws CertificateException Certificate error
     * @throws IOException Error deleting extension
     */
    private fun createTbsForVerification(preCertificate: X509Certificate, issuerInformation: IssuerInformation): TBSCertificate {
        @Suppress("MagicNumber")
        require(preCertificate.version >= 3)
        // We have to use bouncycastle's certificate parsing code because Java's X509 certificate
        // parsing discards the order of the extensions. The signature from SCT we're verifying
        // is over the TBSCertificate in its original form, including the order of the extensions.
        // Get the list of extensions, in its original order, minus the poison extension.
        return ASN1InputStream(preCertificate.encoded).use { aIn ->
            val parsedPreCertificate = org.bouncycastle.asn1.x509.Certificate.getInstance(aIn.readObject())
            // Make sure that we have the X509AuthorityKeyIdentifier of the real issuer if:
            // The PreCertificate has this extension, AND:
            // The PreCertificate was signed by a PreCertificate signing cert.
            if (parsedPreCertificate.hasX509AuthorityKeyIdentifier() && issuerInformation.issuedByPreCertificateSigningCert) {
                require(issuerInformation.x509authorityKeyIdentifier != null)
            }

            val orderedExtensions = getExtensionsWithoutPoisonAndSct(
                parsedPreCertificate.tbsCertificate.extensions,
                issuerInformation.x509authorityKeyIdentifier
            )

            V3TBSCertificateGenerator().apply {
                val tbsPart = parsedPreCertificate.tbsCertificate
                // Copy certificate.
                // Version 3 is implied by the generator.
                setSerialNumber(tbsPart.serialNumber)
                setSignature(tbsPart.signature)
                setIssuer(issuerInformation.name ?: tbsPart.issuer)
                setStartDate(tbsPart.startDate)
                setEndDate(tbsPart.endDate)
                setSubject(tbsPart.subject)
                setSubjectPublicKeyInfo(tbsPart.subjectPublicKeyInfo)
                setIssuerUniqueID(tbsPart.issuerUniqueId as DERBitString?)
                setSubjectUniqueID(tbsPart.subjectUniqueId as DERBitString?)
                setExtensions(Extensions(orderedExtensions.toTypedArray()))
            }.generateTBSCertificate()
        }
    }

    private fun getExtensionsWithoutPoisonAndSct(
        extensions: Extensions,
        replacementX509authorityKeyIdentifier: Extension?
    ): List<Extension> {
        // Order is important, which is why a list is used.
        return extensions.extensionOIDs
            // Do nothing - skip copying this extension
            .filterNot { it.id == CTConstants.POISON_EXTENSION_OID }
            // Do nothing - skip copying this extension
            .filterNot { it.id == CTConstants.SCT_CERTIFICATE_OID }
            .map {
                if (it.id == X509_AUTHORITY_KEY_IDENTIFIER && replacementX509authorityKeyIdentifier != null) {
                    // Use the real issuer's authority key identifier, since it's present.
                    replacementX509authorityKeyIdentifier
                } else {
                    // Copy the extension as-is.
                    extensions.getExtension(it)
                }
            }
    }

    @Suppress("ComplexMethod")
    private fun verifySctSignatureOverBytes(sct: SignedCertificateTimestamp, toVerify: ByteArray): SctVerificationResult {
        val sigAlg = when {
            logServer.key.algorithm == "EC" -> "SHA256withECDSA"
            logServer.key.algorithm == "RSA" -> "SHA256withRSA"
            else -> return UnsupportedSignatureAlgorithm(logServer.key.algorithm)
        }

        return try {
            val result = Signature.getInstance(sigAlg).apply {
                initVerify(logServer.key)
                update(toVerify)
            }.verify(sct.signature.signature)

            if (result) SctVerificationResult.Valid else SctVerificationResult.Invalid.FailedVerification
        } catch (e: SignatureException) {
            SignatureNotValid(e)
        } catch (e: InvalidKeyException) {
            LogPublicKeyNotValid(e)
        } catch (e: NoSuchAlgorithmException) {
            UnsupportedSignatureAlgorithm(sigAlg, e)
        }
    }

    private fun org.bouncycastle.asn1.x509.Certificate.hasX509AuthorityKeyIdentifier(): Boolean {
        return tbsCertificate.extensions.getExtension(ASN1ObjectIdentifier(X509_AUTHORITY_KEY_IDENTIFIER)) != null
    }

    /**
     * @throws IOException
     * @throws CertificateEncodingException
     */
    private fun serializeSignedSctData(certificate: Certificate, sct: SignedCertificateTimestamp): ByteArray {
        return ByteArrayOutputStream().use {
            it.serializeCommonSctFields(sct)
            it.writeUint(X509_ENTRY, LOG_ENTRY_TYPE_LENGTH)
            it.writeVariableLength(certificate.encoded, MAX_CERTIFICATE_LENGTH)
            it.writeVariableLength(sct.extensions, MAX_EXTENSIONS_LENGTH)
            it.toByteArray()
        }
    }

    /**
     * @throws IOException
     */
    private fun serializeSignedSctDataForPreCertificate(
        preCertBytes: ByteArray,
        issuerKeyHash: ByteArray,
        sct: SignedCertificateTimestamp
    ): ByteArray {
        return ByteArrayOutputStream().use {
            it.serializeCommonSctFields(sct)
            it.writeUint(PRECERT_ENTRY, LOG_ENTRY_TYPE_LENGTH)
            it.write(issuerKeyHash)
            it.writeVariableLength(preCertBytes, MAX_CERTIFICATE_LENGTH)
            it.writeVariableLength(sct.extensions, MAX_EXTENSIONS_LENGTH)
            it.toByteArray()
        }
    }

    /**
     * @throws IOException
     */
    private fun OutputStream.serializeCommonSctFields(sct: SignedCertificateTimestamp) {
        require(sct.sctVersion == Version.V1) { "Can only serialize SCT v1 for now." }
        writeUint(sct.sctVersion.number.toLong(), VERSION_LENGTH) // ct::V1
        writeUint(0, 1) // ct::CERTIFICATE_TIMESTAMP
        writeUint(sct.timestamp, TIMESTAMP_LENGTH) // Timestamp
    }

    companion object {
        private const val X509_AUTHORITY_KEY_IDENTIFIER = "2.5.29.35"

        private const val X509_ENTRY = 0L
        private const val PRECERT_ENTRY = 1L
    }
}
