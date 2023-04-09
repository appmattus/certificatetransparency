package com.appmattus.certificatetransparency.internal.utils.asn1

import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Test

internal class ASN1KtTest {

    /**
     * In an earlier implementation this test would fail when reading the extensions because of
     * the header code being incorrect. The test is here to prove the the fix works.
     */
    @Test
    fun testBloomberg() {
        val cert = TestData.loadCertificates("/testdata/bloomberg.pem")[0]
        val asn1 = cert.encoded.toAsn1()
        println(asn1)
    }
}
