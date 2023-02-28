/*
 * Copyright 2023 Appmattus Limited
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
 */

package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import com.appmattus.certificatetransparency.utils.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsTest {

    val expected =
        "a38202be308202ba301f0603551d2304183016801459a4660652a07b95923ca394072796745bf93dd0301d0603551d0e04160414069268eedf349b7ff39bddb5d17f" +
            "64afc1165a8f305d0603551d11045630548211626162796c6f6e6865616c74682e636f6d82132a2e626162796c6f6e6865616c74682e636f6d8213626162796c" +
            "6f6e706172746e6572732e636f6d82152a2e626162796c6f6e706172746e6572732e636f6d300e0603551d0f0101ff0404030205a0301d0603551d2504163014" +
            "06082b0601050507030106082b06010505070302303b0603551d1f043430323030a02ea02c862a687474703a2f2f63726c2e73636131622e616d617a6f6e7472" +
            "7573742e636f6d2f73636131622e63726c30200603551d2004193017300b06096086480186fd6c01023008060667810c010201307506082b0601050507010104" +
            "693067302d06082b060105050730018621687474703a2f2f6f6373702e73636131622e616d617a6f6e74727573742e636f6d303606082b06010505073002862a" +
            "687474703a2f2f6372742e73636131622e616d617a6f6e74727573742e636f6d2f73636131622e637274300c0603551d130101ff0402300030820104060a2b06" +
            "010401d6790204020481f50481f200f0007600bbd9dfbc1f8a71b593942397aa927b473857950aab52e81a909664368e1ed18500000163ef3b50df0000040300" +
            "473045022100e77827b4ae839ebe913b0606063aa3b79752e2d58ce99c3f4edc48c4ebe86d6f02205de7e178c85fbff0eba8138500f8ee217b626c5e97081442" +
            "03f31163651d08ae0076008775bfe7597cf88c43995fbdf36eff568d475636ff4ab560c1b4eaff5ea0830f00000163ef3b50d4000004030047304502205acd62" +
            "3fb6f9a4567b614012de5ba3680fe874d8ea836d9f639e2c90d014a674022100f161adf2f6623fe87753eea410ef063178724cb4fea76cdd5e1451e47f1df32f"

    @Test
    fun bytesDecodes() {
        // Given extensions from a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN).first()
        val originalExtensions = Certificate.create(certificate.encoded).tbsCertificate.extensions!!

        // Then the bytes match expected
        assertEquals(expected, originalExtensions.bytes.toHexString())
    }

    @Test
    fun copy() {
        // Given extensions from a certificate
        val certificate = TestData.loadCertificates(TestData.TEST_MITMPROXY_ORIGINAL_CHAIN).first()
        val originalExtensions = Certificate.create(certificate.encoded).tbsCertificate.extensions!!

        // When we copy the existing extensions
        val result = Extensions.create(originalExtensions.values)

        // Then the bytes match expected
        assertEquals(expected, result.bytes.toHexString())
    }
}
