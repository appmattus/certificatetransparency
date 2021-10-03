/*
 * Copyright 2021 Appmattus Limited
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

package com.appmattus.certificaterevocation

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
class BasicAndroidCRLoggerTest {

    @Test
    fun logsInDebugMode() {
        // given a basic logger
        val logger = BasicAndroidCRLogger(true)

        // when we log
        logger.log("a.b.c", RevocationResult.Failure.NoCertificates)

        // then a message is output
        assertEquals("a.b.c Failure: No certificates", logEntries.first().msg)
    }

    @Test
    fun nothingLoggedInReleaseMode() {
        // given a basic logger
        val logger = BasicAndroidCRLogger(false)

        // when we log
        logger.log("a.b.c", RevocationResult.Failure.NoCertificates)

        // then nothing is output
        assertEquals(0, logEntries.size)
    }

    private val logEntries
        get() = ShadowLog.getLogsForTag("CertificateRevocation")
}
