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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.internal.serialization.Deserializer
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.hasEmbeddedSct
import com.appmattus.certificatetransparency.internal.utils.issuerInformation
import com.appmattus.certificatetransparency.internal.utils.signedCertificateTimestamps
import com.appmattus.certificatetransparency.loglist.LogServer
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.TestData.INTERMEDIATE_CA_CERT
import com.appmattus.certificatetransparency.utils.TestData.PRE_CERT_SIGNING_BY_INTERMEDIATE
import com.appmattus.certificatetransparency.utils.TestData.PRE_CERT_SIGNING_CERT
import com.appmattus.certificatetransparency.utils.TestData.ROOT_CA_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_CERT_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_CERT_SCT_RSA
import com.appmattus.certificatetransparency.utils.TestData.TEST_GITHUB_CHAIN
import com.appmattus.certificatetransparency.utils.TestData.TEST_INTERMEDIATE_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_INTERMEDIATE_CERT_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_LOG_KEY
import com.appmattus.certificatetransparency.utils.TestData.TEST_LOG_KEY_DIGICERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_LOG_KEY_PILOT
import com.appmattus.certificatetransparency.utils.TestData.TEST_LOG_KEY_RSA
import com.appmattus.certificatetransparency.utils.TestData.TEST_LOG_KEY_SKYDIVER
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_PRECA_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_SIGNED_BY_INTERMEDIATE
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_SIGNED_BY_INTERMEDIATE_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_SIGNED_BY_PRECA_CERT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_SIGNED_BY_PRECA_INTERMEDIATE
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_CERT_SIGNED_BY_PRECA_INTERMEDIATE_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_SCT
import com.appmattus.certificatetransparency.utils.TestData.TEST_PRE_SCT_RSA
import com.appmattus.certificatetransparency.utils.TestData.loadCertificates
import com.appmattus.certificatetransparency.utils.assertIsA
import com.appmattus.certificatetransparency.utils.readPemFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * This test verifies that the data is correctly serialized for signature comparison, so signature
 * verification is actually effective.
 */
internal class LogSignatureVerifierTest {

    /** Returns a LogSignatureVerifier for the test log with an EC key  */
    private val verifier by lazy {
        val logInfo = LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY))
        LogSignatureVerifier(logInfo)
    }

    /** Returns a LogSignatureVerifier for the test log with an RSA key  */
    private val verifierRSA by lazy {
        val logInfo = LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY_RSA))
        LogSignatureVerifier(logInfo)
    }

    /** Returns a Map of LogInfos with all log keys to verify the Github certificate  */
    private val logInfosGitHub by lazy {
        listOf(
            LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY_PILOT)),
            LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY_SKYDIVER)),
            LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY_DIGICERT))
        ).associateBy { Base64.toBase64String(it.id) }
    }

    @Test
    fun signatureVerifies() {
        val certs = loadCertificates(TEST_CERT)
        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_CERT_SCT).inputStream())
        assertIsA<SctVerificationResult.Valid>(verifier.verifySignature(sct, certs))
    }

    @Test
    fun signatureVerifiesRSA() {
        val certs = loadCertificates(TEST_CERT)
        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_CERT_SCT_RSA).inputStream())
        assertIsA<SctVerificationResult.Valid>(verifierRSA.verifySignature(sct, certs))
    }

    @Test
    fun signatureOnPreCertificateVerifies() {
        val preCertificatesList = loadCertificates(TEST_PRE_CERT)
        assertEquals(1, preCertificatesList.size.toLong())
        val preCertificate = preCertificatesList[0]

        val caList = loadCertificates(ROOT_CA_CERT)
        assertEquals(1, caList.size.toLong())
        val signerCert = caList[0]

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_SCT).inputStream())

        val verifier = verifier
        assertIsA<SctVerificationResult.Valid>(
            "Expected signature to verify OK",
            verifier.verifySCTOverPreCertificate(sct, preCertificate, signerCert.issuerInformation())
        )
    }

    @Test
    fun signatureOnPreCertificateVerifiesRSA() {
        val preCertificatesList = loadCertificates(TEST_PRE_CERT)
        assertEquals(1, preCertificatesList.size.toLong())
        val preCertificate = preCertificatesList[0]

        val caList = loadCertificates(ROOT_CA_CERT)
        assertEquals(1, caList.size.toLong())
        val signerCert = caList[0]

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_SCT_RSA).inputStream())

        val verifier = verifierRSA
        assertIsA<SctVerificationResult.Valid>(
            "Expected signature to verify OK",
            verifier.verifySCTOverPreCertificate(sct, preCertificate, signerCert.issuerInformation())
        )
    }

    /** Tests for the public verifySignature method taking a chain of certificates.  */
    @Test
    fun signatureOnRegularCertChainVerifies() {
        // Flow:
        // test-cert.pem -> ca-cert.pem
        val certs = loadCertificates(TEST_CERT)
        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_CERT_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>(verifier.verifySignature(sct, certs))
    }

    @Test
    fun signatureOnCertSignedByIntermediateVerifies() {
        // Flow:
        // test-intermediate-cert.pem -> intermediate-cert.pem -> ca-cert.pem
        val certsChain = listOf(TEST_INTERMEDIATE_CERT, INTERMEDIATE_CA_CERT, ROOT_CA_CERT).flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_INTERMEDIATE_CERT_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>(verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun signatureOnPreCertificateCertsChainVerifies() {
        // Flow:
        // test-embedded-pre-cert.pem -> ca-cert.pem
        val certsChain = listOf(TEST_PRE_CERT, ROOT_CA_CERT).flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>(verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun signatureOnPreCertificateSignedByPreCertificateSigningCertVerifies() {
        // Flow:
        // test-embedded-with-preca-pre-cert.pem -> ca-pre-cert.pem -> ca-cert.pem
        val certsChain = listOf(TEST_PRE_CERT_SIGNED_BY_PRECA_CERT, PRE_CERT_SIGNING_CERT, ROOT_CA_CERT).flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_CERT_PRECA_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>("Expected PreCertificate to verify OK", verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun signatureOnPreCertificateSignedByIntermediateVerifies() {
        // Flow:
        // test-embedded-with-intermediate-pre-cert.pem -> intermediate-cert.pem -> ca-cert.pem
        val certsChain = listOf(TEST_PRE_CERT_SIGNED_BY_INTERMEDIATE, INTERMEDIATE_CA_CERT, ROOT_CA_CERT).flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_CERT_SIGNED_BY_INTERMEDIATE_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>("Expected PreCertificate to verify OK", verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun signatureOnPreCertificateSignedByPreCertSigningCertSignedByIntermediateVerifies() {
        // Flow:
        // test-embedded-with-intermediate-preca-pre-cert.pem -> intermediate-pre-cert.pem
        //   -> intermediate-cert.pem -> ca-cert.pem
        val certsChain = listOf(TEST_PRE_CERT_SIGNED_BY_PRECA_INTERMEDIATE, PRE_CERT_SIGNING_BY_INTERMEDIATE, INTERMEDIATE_CA_CERT, ROOT_CA_CERT)
            .flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_CERT_SIGNED_BY_PRECA_INTERMEDIATE_SCT).inputStream())

        assertIsA<SctVerificationResult.Valid>("Expected PreCertificate to verify OK", verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun returnNoIssuerWithPreCertWhenChainWithPreCertificateSignedByPreCertificateSigningCertMissingIssuer() {
        val certsChain = listOf(TEST_PRE_CERT_SIGNED_BY_PRECA_CERT, PRE_CERT_SIGNING_CERT).flatMap(::loadCertificates)

        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_PRE_CERT_PRECA_SCT).inputStream())

        assertIsA<NoIssuerWithPreCert>(verifier.verifySignature(sct, certsChain))
    }

    @Test
    fun signatureOnEmbeddedSCTsInFinalCertificateVerifies() {
        // Flow:
        // github-chain.txt contains leaf certificate signed by issuing CA.
        // Leafcert contains three embedded SCTs, we verify them all
        val certsChain = loadCertificates(TEST_GITHUB_CHAIN)

        // the leaf cert is the first one in this test data
        val leafcert = certsChain[0]
        val issuerCert = certsChain[1]
        assertTrue("The test certificate does have embedded SCTs", leafcert.hasEmbeddedSct())

        val scts = leafcert.signedCertificateTimestamps()
        assertEquals("Expected 3 SCTs in the test certificate", 3, scts.size.toLong())

        for (sct in scts) {
            val id = Base64.toBase64String(sct.id.keyId)
            val logInfo = logInfosGitHub[id]
            val verifier = LogSignatureVerifier(logInfo!!)

            assertIsA<SctVerificationResult.Valid>(
                "Expected signature to verify OK",
                verifier.verifySCTOverPreCertificate(sct, leafcert, issuerCert.issuerInformation())
            )
            assertIsA<SctVerificationResult.Valid>("Expected PreCertificate to verify OK", verifier.verifySignature(sct, certsChain))
        }
    }

    @Test
    fun returnInvalidFutureTimestampWhenSctTimestampInFuture() {
        // given we have an SCT with a future timestamp
        val certs = loadCertificates(TEST_CERT)
        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_CERT_SCT).inputStream())
        val futureSct = sct.copy(timestamp = System.currentTimeMillis() + 10000)

        // when the signature is verified
        assertIsA<SctVerificationResult.Invalid.FutureTimestamp>(verifier.verifySignature(futureSct, certs))
    }

    @Test
    fun signatureInvalidWhenLogServerNoLongerTrusted() {
        // given we have an SCT
        val certs = loadCertificates(TEST_CERT)
        val sct = Deserializer.parseSctFromBinary(TestData.file(TEST_CERT_SCT).inputStream())

        // when we have a log server which is no longer valid
        val logInfo = LogServer.fromKeyFile(TestData.fileName(TEST_LOG_KEY))
        val verifier = LogSignatureVerifier(logInfo.copy(validUntil = sct.timestamp - 10000))

        // then the signature is rejected
        assertIsA<SctVerificationResult.Invalid.LogServerUntrusted>(verifier.verifySignature(sct, certs))
    }

    /**
     * Creates a LogInfo instance from the Log's public key file. Supports both EC and RSA keys.
     *
     * @property pemKeyFilePath Path of the log's public key file.
     * @return new LogInfo instance.
     */
    private fun LogServer.Companion.fromKeyFile(pemKeyFilePath: String): LogServer {
        val logPublicKey = File(pemKeyFilePath).readPemFile()
        return LogServer(logPublicKey)
    }
}
