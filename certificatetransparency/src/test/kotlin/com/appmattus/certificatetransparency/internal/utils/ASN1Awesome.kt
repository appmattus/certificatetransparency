package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1
import com.appmattus.certificatetransparency.internal.utils.asn1.toByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.x509.Certificate
import com.appmattus.certificatetransparency.utils.TestData
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ASN1Awesome {

    @Test
    fun byteBuffer() {
        var randomBytes = Random.nextBytes(200)
        var runningByteBuffer = randomBytes.toByteBuffer()

        repeat(30) {
            val startIndex = min(Random.nextInt(10), randomBytes.size)
            val endIndex = max(randomBytes.size - min(Random.nextInt(10), randomBytes.size), startIndex)

            val newByteBuffer = randomBytes.toByteBuffer().range(startIndex, endIndex)
            randomBytes = randomBytes.copyOfRange(startIndex, endIndex)
            runningByteBuffer = runningByteBuffer.range(startIndex, endIndex)

            assertEquals(randomBytes.toHexString(), runningByteBuffer.toHexString())
            assertEquals(randomBytes.toHexString(), newByteBuffer.toHexString())
        }
    }

    @Test
    fun test() {
        val certsToCheck = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN)

        certsToCheck.forEach {
            println(it.encoded.toHexString())
            // val result = it.encoded.toByteBuffer().parseASN1()
            // println(result.toString())

            println(Certificate.create(it.encoded))
        }
    }

    @Test
    fun v1() {
        val cert = TestData.loadCertificates(TestData.TEST_V1_CERT).first()
        println(Certificate.create(cert.encoded))
    }

    @Test
    fun simple() {
        val bytes =
            "308187020100301306072A8648CE3D020106082A8648CE3D030107046D306B0201010420898C9DA36EDA3463BD0C151678610F0FCA0D8B52D90EA7F13E642589" +
                "E2C18D54A14403420004213D5258BC6C69C3E2139675EA3928A409FCFFEF39ACC8E0C82A24AE78C37EDE98FD89C0E00E74C997BB0A716CA9E0DCA673DBB9" +
                "B3FA72962255C9DEBCD218CA".decodeHex()

        println(bytes.toByteArray().toByteBuffer().toAsn1().toString())
    }

    @Test
    fun chatGpt() {
        val data = byteArrayOf(0x30, 0x0d, 0x02, 0x01, 0x01, 0x31, 0x08, 0x30, 0x06, 0x02, 0x01, 0x02, 0x02, 0x01, 0x03)
        val asn1Object = data.toByteBuffer().toAsn1()

        println(asn1Object.toString())
    }

    @Test
    fun length() {
        val value = "82010F003082010A".decodeHex().toByteArray()

        val length = value.inputStream().readLength()

        println(length)
    }

    companion object {
        private fun ByteArray.toHexString(): String = toByteString().hex()

        private fun ByteBuffer.toHexString(): String = toList().toByteArray().toHexString()
    }
}
