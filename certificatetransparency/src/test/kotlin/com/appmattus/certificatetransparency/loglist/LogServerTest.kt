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

package com.appmattus.certificatetransparency.loglist

import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.internal.utils.PublicKeyFactory
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mostly for verifying the log info calculates the log ID correctly.  */
internal class LogServerTest {

    @Test
    fun testCalculatesLogIdCorrectly() {
        val logServer = LogServer(PublicKeyFactory.fromByteArray(PUBLIC_KEY))
        assertTrue(logServer.id.contentEquals(LOG_ID))
    }

    @Test
    fun testCalculatesLogIdCorrectlyRSA() {
        val logServer = LogServer(PublicKeyFactory.fromByteArray(PUBLIC_KEY_RSA))
        assertTrue(logServer.id.contentEquals(LOG_ID_RSA))
    }

    companion object {
        /** EC log key  */
        val PUBLIC_KEY: ByteArray = Base64.decode(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEfahLEimAoz2t01p3uMziiLOl/fHTDM0YDOhBRuiBARsV4UvxG2LdNgoIGLrtCzWE0J5APC2em4JlvR8EEEFMoA=="
        )

        val LOG_ID: ByteArray = Base64.decode("pLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BA=")

        /** RSA log key  */
        val PUBLIC_KEY_RSA: ByteArray = Base64.decode(
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3tyLdYQYM+K+1jGlLUTJ" +
                "lNFTeNJM4LN5ctwAwXDhoKCFJrGAayZaXJsYtKHf+RH2Y6pqbtE4Ln/4HgXXzFQi" +
                "BuyTed/ooAafYkDPQsrg51/DxV4WZG66WzFjbFtBPKVfSnLqmbhRlr99PEY92bDt" +
                "8YUOCfEikqHIDZaieJHQQlIx5yjOYbRnsBT0HDitTuvM1or589k+wnYVyNEtU9Np" +
                "NA+37kBD0SM7LipYCCSrb0zh5yTriNQS/LmdUWE1G5v8VR+acttDl5zPKetocNMg" +
                "7NIa/zvrXizld9DQqt2UiC49KcD9x2shxEgp64K0S0546kU0lKYnY7NimDkVRCOe" +
                "3wIDAQAB"
        )

        val LOG_ID_RSA: ByteArray = Base64.decode("oCQsumIkVhezsKvGJ+spTJIM9H+jy/OdvSGDIX0VsgY=")
    }
}
