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

package com.appmattus.certificatetransparency.logclient.internal

import com.appmattus.certificatetransparency.logclient.internal.exceptions.CertificateTransparencyException
import com.appmattus.certificatetransparency.logclient.internal.utils.Base64
import com.appmattus.certificatetransparency.logclient.model.DigitallySigned
import com.appmattus.certificatetransparency.logclient.model.LogEntry
import com.appmattus.certificatetransparency.logclient.model.SignedCertificateTimestamp
import com.appmattus.certificatetransparency.logclient.model.Version
import com.appmattus.certificatetransparency.utils.CryptoDataLoader
import com.appmattus.certificatetransparency.utils.TestData
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/** Test interaction with the Log http server.  */
class HttpLogClientTest {

    private val mockInterceptor = mock<Interceptor>()

    private val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(mockInterceptor).build()
    private val retrofit = Retrofit.Builder().client(client).addConverterFactory(GsonConverterFactory.create()).baseUrl("http://ctlog/").build()
    private val logClientService: LogClientService = retrofit.create(LogClientService::class.java)

    private fun expectInterceptor(url: String, jsonResponse: String) {
        whenever(mockInterceptor.intercept(argThat { request().url().toString() == url })).then {

            val chain = it.arguments[0] as Interceptor.Chain

            Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), jsonResponse))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("")
                .build()
        }
    }

    @Test
    fun certificatesAreEncoded() {
        val inputCerts = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.TEST_COLLIDING_ROOTS))
        val client = HttpLogClient(logClientService)

        val encoded = client.encodeCertificates(inputCerts)
        assertTrue(encoded.chain.isNotEmpty())
        assertEquals("Expected to have two certificates in the chain", 2, encoded.chain.size.toLong())
        // Make sure the order is reversed.
        for (i in inputCerts.indices) {
            assertEquals(Base64.toBase64String(inputCerts[i].encoded), encoded.chain[i])
        }
    }

    private fun SignedCertificateTimestamp?.verifySctContents() {
        assertEquals(Version.V1, this?.sctVersion)
        assertArrayEquals(LOG_ID, this?.id?.keyId)
        assertEquals(1373015623951L, this?.timestamp)
        assertEquals(DigitallySigned.HashAlgorithm.SHA256, this?.signature?.hashAlgorithm)
        assertEquals(DigitallySigned.SignatureAlgorithm.ECDSA, this?.signature?.signatureAlgorithm)
    }

    @Test
    fun certificateSentToServer() {
        expectInterceptor("http://ctlog/add-chain", JSON_RESPONSE)

        val client = HttpLogClient(logClientService)
        val certs = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.TEST_COLLIDING_ROOTS))
        val res = client.addCertificate(certs)
        assertNotNull("Should have a meaningful SCT", res)

        res.verifySctContents()
    }

    @Test
    fun getLogSth() {
        expectInterceptor("http://ctlog/get-sth", STH_RESPONSE)

        val client = HttpLogClient(logClientService)
        val sth = client.logSth

        assertNotNull(sth)
        assertEquals(1402415255382L, sth.timestamp)
        assertEquals(4301837, sth.treeSize)
        val rootHash = Base64.toBase64String(sth.sha256RootHash)
        assertTrue("jdH9k+/lb9abMz3N8rVmwrw8MWU7v55+nSAXej3hqPg=" == rootHash)
    }

    @Test(expected = CertificateTransparencyException::class)
    fun getLogSthBadResponseTimestamp() {
        expectInterceptor("http://ctlog/get-sth", BAD_STH_RESPONSE_INVALID_TIMESTAMP)

        val client = HttpLogClient(logClientService)
        client.logSth
    }

    @Test(expected = CertificateTransparencyException::class)
    fun getLogSTHBadResponseRootHash() {
        expectInterceptor("http://ctlog/get-sth", BAD_STH_RESPONSE_INVALID_ROOT_HASH)

        val client = HttpLogClient(logClientService)
        client.logSth
    }

    @Test
    fun getRootCerts() {
        expectInterceptor("http://ctlog/get-roots", TestData.file(TestData.TEST_ROOT_CERTS).readText())

        val client = HttpLogClient(logClientService)
        val rootCerts = client.logRoots

        assertNotNull(rootCerts)
        assertEquals(2, rootCerts.size.toLong())
    }

    @Test
    fun getLogEntries() {
        expectInterceptor("http://ctlog/get-entries?start=0&end=0", LOG_ENTRY)

        val client = HttpLogClient(logClientService)
        val testChainCert = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.ROOT_CA_CERT))[0] as X509Certificate
        val testCert = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.TEST_CERT))[0] as X509Certificate
        val entries = client.getLogEntries(0, 0)

        var chainCert: X509Certificate? = null
        var leafCert: X509Certificate? = null
        try {
            val x509Entry = (entries[0].logEntry as LogEntry.X509ChainEntry)

            val leafCertBytes = x509Entry.leafCertificate
            leafCert = CertificateFactory.getInstance("X509")
                .generateCertificate(leafCertBytes?.inputStream()) as X509Certificate

            val chainCertBytes = x509Entry.certificateChain[0]
            chainCert = CertificateFactory.getInstance("X509")
                .generateCertificate(chainCertBytes.inputStream()) as X509Certificate
        } catch (ignored: CertificateException) {
            fail()
        }

        assertTrue(testCert == leafCert)
        assertTrue(testChainCert == chainCert)
    }

    @Test
    fun getLogEntriesCorruptedEntry() {
        expectInterceptor("http://ctlog/get-entries?start=0&end=0", LOG_ENTRY_CORRUPTED_ENTRY)

        val client = HttpLogClient(logClientService)
        try {
            // Must get an actual entry as the list of entries is lazily transformed.
            client.getLogEntries(0, 0)[0]
            fail()
        } catch (expected: CertificateTransparencyException) {
        }
    }

    @Test
    fun getLogEntriesEmptyEntry() {
        expectInterceptor("http://ctlog/get-entries?start=0&end=0", LOG_ENTRY_EMPTY)

        val client = HttpLogClient(logClientService)
        assertTrue(client.getLogEntries(0, 0).isEmpty())
    }

    @Test
    fun getSTHConsistency() {
        expectInterceptor("http://ctlog/get-sth-consistency?first=1&second=3", CONSISTENCY_PROOF)

        val client = HttpLogClient(logClientService)
        val proof = client.getSthConsistency(1, 3)
        assertNotNull(proof)
        assertEquals(2, proof.size.toLong())
    }

    @Test
    fun getSTHConsistencyEmpty() {
        expectInterceptor("http://ctlog/get-sth-consistency?first=1&second=3", CONSISTENCY_PROOF_EMPTY)

        val client = HttpLogClient(logClientService)
        val proof = client.getSthConsistency(1, 3)
        assertNotNull(proof)
        assertTrue(proof.isEmpty())
    }

    @Test
    fun getLogEntrieAndProof() {
        expectInterceptor("http://ctlog/get-entry-and-proof?leaf_index=1&tree_size=2", LOG_ENTRY_AND_PROOF)

        val client = HttpLogClient(logClientService)
        val testChainCert = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.ROOT_CA_CERT))[0] as X509Certificate
        val testCert = CryptoDataLoader.certificatesFromFile(TestData.file(TestData.TEST_CERT))[0] as X509Certificate
        val entry = client.getLogEntryAndProof(1, 2)

        var chainCert: X509Certificate? = null
        var leafCert: X509Certificate? = null
        try {
            val x509Entry = (entry.parsedLogEntry.logEntry as LogEntry.X509ChainEntry)

            val leafCertBytes = x509Entry.leafCertificate
            leafCert = CertificateFactory.getInstance("X509")
                .generateCertificate(leafCertBytes?.inputStream()) as X509Certificate

            val chainCertBytes = x509Entry.certificateChain[0]
            chainCert = CertificateFactory.getInstance("X509")
                .generateCertificate(chainCertBytes.inputStream()) as X509Certificate
        } catch (ignored: CertificateException) {
            fail()
        }

        assertTrue(testCert == leafCert)
        assertTrue(testChainCert == chainCert)
        assertEquals(2, entry.auditProof.pathNodes.size.toLong())
        assertEquals(1, entry.auditProof.leafIndex)
        assertEquals(2, entry.auditProof.treeSize)
    }

    @Test
    fun getLogProofByHash() {
        val merkleLeafHash = "YWhhc2g="
        expectInterceptor("http://ctlog/get-proof-by-hash?tree_size=40183&hash=YWhhc2g%3D", MERKLE_AUDIT_PROOF)
        val client2 = HttpLogClient(logClientService)
        val auditProof = client2.getProofByEncodedHash(merkleLeafHash, 40183)
        assertTrue(auditProof.leafIndex == 198743L)
    }

    internal companion object {
        const val STH_RESPONSE = (
                "{\"timestamp\":1402415255382," +
                        "\"tree_head_signature\":\"BAMARzBFAiBX9fHXbK3Yi+P+bGM8mlL8XFmwZ7fkbhK2GqlnoJkMkQIhANGoUuD+" +
                        "JvjFTRdESfKO5428e1HAQL412Sa5e16D4E3M\"," +
                        "\"sha256_root_hash\":\"jdH9k+\\/lb9abMz3N8rVmwrw8MWU7v55+nSAXej3hqPg=\"," +
                        "\"tree_size\":4301837}")

        const val BAD_STH_RESPONSE_INVALID_TIMESTAMP = (
                "{\"timestamp\":-1," +
                        "\"tree_head_signature\":\"BAMARzBFAiBX9fHXbK3Yi+P+bGM8mlL8XFmwZ7fkbhK2GqlnoJkMkQIhANGoUuD+" +
                        "JvjFTRdESfKO5428e1HAQL412Sa5e16D4E3M\"," +
                        "\"sha256_root_hash\":\"jdH9k+\\/lb9abMz3N8rVmwrw8MWU7v55+nSAXej3hqPg=\"," +
                        "\"tree_size\":0}")

        const val BAD_STH_RESPONSE_INVALID_ROOT_HASH = (
                "{\"timestamp\":1402415255382," +
                        "\"tree_head_signature\":\"BAMARzBFAiBX9fHXbK3Yi+P+bGM8mlL8XFmwZ7fkbhK2GqlnoJkMkQIhANGo" +
                        "UuD+JvjFTRdESfKO5428e1HAQL412Sa5e16D4E3M\"," +
                        "\"sha256_root_hash\":\"jdH9k+\\/lb9abMz3N8r7v55+nSAXej3hqPg=\"," +
                        "\"tree_size\":4301837}")

        const val JSON_RESPONSE = (
                "{\"sct_version\":0,\"id\":\"pLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BA=\"," +
                        "\"timestamp\":1373015623951,\n" +
                        "\"extensions\":\"\",\n" +
                        "\"signature\":\"BAMARjBEAiAggPtKUMFZ4zmNnPhc7As7VR1Dedsdggs9a8pSEHoyGAIgKGsvIPDZg" +
                        "DnxTjGY8fSBwkl15dA0TUqW5ex2HCU7yE8=\"}")

        const val LOG_ENTRY = (
                "{ \"entries\": [ { \"leaf_input\": \"AAAAAAFHz32CRgAAAALO" +
                        "MIICyjCCAjOgAwIBAgIBBjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEkMCIGA1UEChMbQ2VydGlmaWNhdG" +
                        "UgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IFdlbjAeFw0xMjA2MDEwMDAwMDBa" +
                        "Fw0yMjA2MDEwMDAwMDBaMFIxCzAJBgNVBAYTAkdCMSEwHwYDVQQKExhDZXJ0aWZpY2F0ZSBUcmFuc3BhcmVuY3kxDj" +
                        "AMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCx+jeTYRH4" +
                        "eS2iCBw\\/5BklAIUx3H8sZXvZ4d5HBBYLTJ8Z1UraRHBATBxRNBuPH3U43d0o2aykg2n8VkbdzHYX+BaKrltB1DM" +
                        "x\\/KLa38gE1XIIlJBh+e75AspHzojGROAA8G7uzKvcndL2iiLMsJ3Hbg28c1J3ZbGjeoxnYlPcwQIDAQABo4GsMIG" +
                        "pMB0GA1UdDgQWBBRqDZgqO2LES20u9Om7egGqnLeY4jB9BgNVHSMEdjB0gBRfnYgNyHPmVNT4DdjmsMEktEfDVaFZp" +
                        "FcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2F" +
                        "sZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwCQYDVR0TBAIwADANBgkqhkiG9w0BAQUFAAOBgQAXHNhKrEFKmgMPIqrI9" +
                        "oiwgbJwm4SLTlURQGzXB\\/7QKFl6n678Lu4peNYzqqwU7TI1GX2ofg9xuIdfGsnniygXSd3t0Afj7PUGRfjL9mclb" +
                        "NahZHteEyA7uFgt59Zpb2VtHGC5X0Vrf88zhXGQjxxpcn0kxPzNJJKVeVgU0drA5gAA\", " +
                        "\"extra_data\": \"AALXAALUMIIC0DCCAjmgAwIBAgIBADANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEk" +
                        "MCIGA1UEChMbQ2VydGlmaWNhdGUgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IF" +
                        "dlbjAeFw0xMjA2MDEwMDAwMDBaFw0yMjA2MDEwMDAwMDBaMFUxCzAJBgNVBAYTAkdCMSQwIgYDVQQKExtDZXJ0aWZp" +
                        "Y2F0ZSBUcmFuc3BhcmVuY3kgQ0ExDjAMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQ" +
                        "EBAQUAA4GNADCBiQKBgQDVimhTYhCicRmTbneDIRgcKkATxtB7jHbrkVfT0PtLO1FuzsvRyY2RxS90P6tjXVUJnNE" +
                        "6uvMa5UFEJFGnTHgW8iQ8+EjPKDHM5nugSlojgZ88ujfmJNnDvbKZuDnd\\/iYx0ss6hPx7srXFL8\\/BT\\/9Ab1z" +
                        "URmnLsvfP34b7arnRsQIDAQABo4GvMIGsMB0GA1UdDgQWBBRfnYgNyHPmVNT4DdjmsMEktEfDVTB9BgNVHSMEdjB0g" +
                        "BRfnYgNyHPmVNT4DdjmsMEktEfDVaFZpFcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5" +
                        "zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2FsZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwDAYDVR0TBAUwAwEB\\/zANBgk" +
                        "qhkiG9w0BAQUFAAOBgQAGCMxKbWTyIF4UbASydvkrDvqUpdryOvw4BmBtOZDQoeojPUApV2lGOwRmYef6HReZFSCa6" +
                        "i4Kd1F2QRIn18ADB8dHDmFYT9czQiRyf1HWkLxHqd81TbD26yWVXeGJPE3VICskovPkQNJ0tU4b03YmnKliibduyqQ" +
                        "QkOFPOwqULg==\" } ] }")

        @Suppress("MaxLineLength")
        const val MERKLE_AUDIT_PROOF = (
                "{\n" +
                        "\t\"leaf_index\":\"198743\",\n" +
                        "\t\"audit_path\": [\"MqsV9pJwuMVT1myDLQhy/u8y8yfjo2UIt/gULYIm9kw=\", \"iW2pKxYzPjwg1oqVS6bWKlrnkdsAbnYA4InJNUR4i0o=\", \"dmBMn0eQsFaAeXM/g/CE4awVonE9Bh12Eo3FAKngMqI=\", \"LyzJlb/OswwmACipg53qibS/xM36zPTSqFYyX/PQzRQ=\", \"VdnXYMjdTQg/YyDpahigAJvDzmxvXZNs4olmI2aPpSA=\", \"/WueKL8dlZICSRo/FLG9b/rINajZvrvwIYJMobx9x80=\", \"OCkOnssB2ZCE4RcSAnx0IvvZJYoxU+dgb1lm7OzjeIM=\", \"+2mouv9a2nWDISHvAIs+TAZxmZnDs1cOJ523yC29rj8=\", \"eBBZH33vXm/GQFs1HzGQ7nuMyGgnezv8cbAafXcAbSk=\", \"fQBUze8HOeBbS1bQiSe2PUqpuAj9uSHDMuM2qPsNaeo=\", \"mTkshaMzdhPHbmLOkTLd6SRe9AQmIhkBLfK3LbbYcMk=\", \"MvUpXVpfY/YlN8mca6BwZIVTmo4YAIumh2Frra0uybQ=\", \"CoxlxOr0JN7ZULFjBoHDQX129uMdCNSJk2az2e46oFE=\", \"4jCKz6dTAkMkaKy8uG1Y9IInCpL41Y8zBll2RNKXWm0=\", \"Q6iDW9iQXyefHaILfjAFPCjYR0kWca5MmPa8YoXV2zc=\", \"R9L2yHqkEI81e/D0QoDDw+T+ofaYwNAHTV0erxUlabs=\", \"TZI91NbHjlec2Hpoukwq1SOdVbzwEQ1/7IJAT0bB3ZI=\", \"bLlI5tybRk3WF+S2OQHfgwdZuqqJLAcPPWpp0Evj9qw=\", \"D2lTqW1O272aHIydgu9Bthn5MWYzlpWyHzKArIl7OxE=\", \"HWioeGEa+AwSRHCP4JPY/9xH2Z1+ezcwHbVsdko3uU8=\", \"0ydE3XEnELlBoXKgHtbJzyq1xbRNMOJtYJz1sQ4P2ho=\", \"rPCL+hjXx2zPjrkRFnvXlE6JhjKi9RnqyJ5j9X+Udds=\", \"R3TPvrSfIS3wf236bVFMKNAhKSsuScWnbSVcfsajYrs=\", \"0jLIygXEcuV2mAQLq9lluHdA9a5NGD3pMsyCphaPv/8=\", \"P6im7ddV3GvxchmJf16zAYzb6MvybJgOen3nm9Nrszg=\", \"wN/yk40mY/y5m/tdYR4OwVfN3HOqZC22v5jJrsvgnUI=\", \"jvqb4JzKxrbY9aXhbtsa27ufbaPK8U6j9yWKsFDsjJE=\", \"wrOTFjcFrRl1omqgJ9zibZuIz5FLnV0cdMoiONQyB4g=\"]\n" +
                        "}")
        const val LOG_ENTRY_AND_PROOF = (
                "{\"leaf_input\": \"AAAAAAFHz32" +
                        "CRgAAAALO" +
                        "MIICyjCCAjOgAwIBAgIBBjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEkMCIGA1UEChMbQ2VydGlmaWNhdG" +
                        "UgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IFdlbjAeFw0xMjA2MDEwMDAwMDBa" +
                        "Fw0yMjA2MDEwMDAwMDBaMFIxCzAJBgNVBAYTAkdCMSEwHwYDVQQKExhDZXJ0aWZpY2F0ZSBUcmFuc3BhcmVuY3kxDj" +
                        "AMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCx+jeTYRH4" +
                        "eS2iCBw\\/5BklAIUx3H8sZXvZ4d5HBBYLTJ8Z1UraRHBATBxRNBuPH3U43d0o2aykg2n8VkbdzHYX+BaKrltB1DM" +
                        "x\\/KLa38gE1XIIlJBh+e75AspHzojGROAA8G7uzKvcndL2iiLMsJ3Hbg28c1J3ZbGjeoxnYlPcwQIDAQABo4GsMIG" +
                        "pMB0GA1UdDgQWBBRqDZgqO2LES20u9Om7egGqnLeY4jB9BgNVHSMEdjB0gBRfnYgNyHPmVNT4DdjmsMEktEfDVaFZp" +
                        "FcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2F" +
                        "sZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwCQYDVR0TBAIwADANBgkqhkiG9w0BAQUFAAOBgQAXHNhKrEFKmgMPIqrI9" +
                        "oiwgbJwm4SLTlURQGzXB\\/7QKFl6n678Lu4peNYzqqwU7TI1GX2ofg9xuIdfGsnniygXSd3t0Afj7PUGRfjL9mclb" +
                        "NahZHteEyA7uFgt59Zpb2VtHGC5X0Vrf88zhXGQjxxpcn0kxPzNJJKVeVgU0drA5gAA\", " +
                        "\"extra_data\": \"AALXAALUMIIC0DCCAjmgAwIBAgIBADANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEk" +
                        "MCIGA1UEChMbQ2VydGlmaWNhdGUgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IF" +
                        "dlbjAeFw0xMjA2MDEwMDAwMDBaFw0yMjA2MDEwMDAwMDBaMFUxCzAJBgNVBAYTAkdCMSQwIgYDVQQKExtDZXJ0aWZp" +
                        "Y2F0ZSBUcmFuc3BhcmVuY3kgQ0ExDjAMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQ" +
                        "EBAQUAA4GNADCBiQKBgQDVimhTYhCicRmTbneDIRgcKkATxtB7jHbrkVfT0PtLO1FuzsvRyY2RxS90P6tjXVUJnNE" +
                        "6uvMa5UFEJFGnTHgW8iQ8+EjPKDHM5nugSlojgZ88ujfmJNnDvbKZuDnd\\/iYx0ss6hPx7srXFL8\\/BT\\/9Ab1z" +
                        "URmnLsvfP34b7arnRsQIDAQABo4GvMIGsMB0GA1UdDgQWBBRfnYgNyHPmVNT4DdjmsMEktEfDVTB9BgNVHSMEdjB0g" +
                        "BRfnYgNyHPmVNT4DdjmsMEktEfDVaFZpFcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5" +
                        "zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2FsZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwDAYDVR0TBAUwAwEB\\/zANBgk" +
                        "qhkiG9w0BAQUFAAOBgQAGCMxKbWTyIF4UbASydvkrDvqUpdryOvw4BmBtOZDQoeojPUApV2lGOwRmYef6HReZFSCa6" +
                        "i4Kd1F2QRIn18ADB8dHDmFYT9czQiRyf1HWkLxHqd81TbD26yWVXeGJPE3VICskovPkQNJ0tU4b03YmnKliibduyqQ" +
                        "QkOFPOwqULg==\", " +
                        "\"audit_path\":[\"h6Wo6zvO+d293qbd/5bfwMae9eh4jAZULr6i2fLAop4=\"," +
                        "\"6eIbVFV8aYnfVF4/S3JN+DMPqjzBHyEMooN3rIkGbC4=\"] }")

        const val LOG_ENTRY_CORRUPTED_ENTRY = (
                "{ \"entries\": [ { \"leaf_input\": \"AAAAAAFHz32CRgAAAALO" +
                        "MIICyjCCAjOgAwIBAgIBBjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEkMCIGA1UEChMbQ2VydGlmaWNhdG" +
                        "UgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IFdlbjAeFw0xMjA2MDEwMDAwMDBa" +
                        "Fw0yMjA2MDEwMDAwMDBaMFIxCzAJBgNVBAYTAkdCMSEwHwYDVQQKExhDZXJ0aWZpY2F0ZSBUcmFuc3BhcmVuY3kxDj" +
                        "AMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCx+jeTYRH4" +
                        "eS2iCBw\\/5BklAIUx3H8sZXvZ4d5HBBYLTJ8Z1UraRHBATBxRNBuPH3U43d0o2aykg2n8VkbdzHYX+BaKrltB1DM" +
                        "x\\/KLa38gE1XIIlJBh+e75AspHzojGROAA8G7uzKvcndL2iiLMsJ3Hbg28c1J3ZbGjeoxnYlPcwQIDAQABo4GsMIG" +
                        "FcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2F" +
                        "sZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwCQYDVR0TBAIwADANBgkqhkiG9w0BAQUFAAOBgQAXHNhKrEFKmgMPIqrI9" +
                        "oiwgbJwm4SLTlURQGzXB\\/7QKFl6n678Lu4peNYzqqwU7TI1GX2ofg9xuIdfGsnniygXSd3t0Afj7PUGRfjL9mclb" +
                        "NahZHteEyA7uFgt59Zpb2VtHGC5X0Vrf88zhXGQjxxpcn0kxPzNJJKVeVgU0drA5gAA\", " +
                        "\"extra_data\": \"AALXAALUMIIC0DCCAjmgAwIBAgIBADANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJHQjEk" +
                        "MCIGA1UEChMbQ2VydGlmaWNhdGUgVHJhbnNwYXJlbmN5IENBMQ4wDAYDVQQIEwVXYWxlczEQMA4GA1UEBxMHRXJ3IF" +
                        "dlbjAeFw0xMjA2MDEwMDAwMDBaFw0yMjA2MDEwMDAwMDBaMFUxCzAJBgNVBAYTAkdCMSQwIgYDVQQKExtDZXJ0aWZp" +
                        "Y2F0ZSBUcmFuc3BhcmVuY3kgQ0ExDjAMBgNVBAgTBVdhbGVzMRAwDgYDVQQHEwdFcncgV2VuMIGfMA0GCSqGSIb3DQ" +
                        "EBAQUAA4GNADCBiQKBgQDVimhTYhCicRmTbneDIRgcKkATxtB7jHbrkVfT0PtLO1FuzsvRyY2RxS90P6tjXVUJnNE" +
                        "6uvMa5UFEJFGnTHgW8iQ8+EjPKDHM5nugSlojgZ88ujfmJNnDvbKZuDnd\\/iYx0ss6hPx7srXFL8\\/BT\\/9Ab1z" +
                        "URmnLsvfP34b7arnRsQIDAQABo4GvMIGsMB0GA1UdDgQWBBRfnYgNyHPmVNT4DdjmsMEktEfDVTB9BgNVHSMEdjB0g" +
                        "BRfnYgNyHPmVNT4DdjmsMEktEfDVaFZpFcwVTELMAkGA1UEBhMCR0IxJDAiBgNVBAoTG0NlcnRpZmljYXRlIFRyYW5" +
                        "zcGFyZW5jeSBDQTEOMAwGA1UECBMFV2FsZXMxEDAOBgNVBAcTB0VydyBXZW6CAQAwDAYDVR0TBAUwAwEB\\/zANBgk" +
                        "qhkiG9w0BAQUFAAOBgQAGCMxKbWTyIF4UbASydvkrDvqUpdryOvw4BmBtOZDQoeojPUApV2lGOwRmYef6HReZFSCa6" +
                        "i4Kd1F2QRIn18ADB8dHDmFYT9czQiRyf1HWkLxHqd81TbD26yWVXeGJPE3VICskovPkQNJ0tU4b03YmnKliibduyqQ" +
                        "QkOFPOwqULg==\" } ] }")

        const val LOG_ENTRY_EMPTY = "{ \"entries\": []}"

        const val CONSISTENCY_PROOF =
            "{\"consistency\" :[\"wDblrkBlhZ7UqimOaRS18MjqvNyt" + "/Fc2tcy6nWONY84=\",\"/NeD2RVJUnnzreBeKM4fCCWk+KZzG2ctHdm9LLngwJY=\"]}"

        const val CONSISTENCY_PROOF_EMPTY = "{ \"consistency\": []}"

        val LOG_ID: ByteArray = Base64.decode("pLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BA=")
    }
}
