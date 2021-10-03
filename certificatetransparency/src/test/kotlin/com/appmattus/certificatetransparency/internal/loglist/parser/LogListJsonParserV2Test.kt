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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.LogListJsonBadFormat
import com.appmattus.certificatetransparency.internal.utils.Base64
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.utils.TestData
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

internal class LogListJsonParserV2Test {

    @Test
    fun `parses the json`() = runBlocking {
        // given we have a valid json file

        // when we parse the data
        val result = LogListJsonParserV2().parseJson(json)

        // then 41 items are returned
        require(result is LogListResult.Valid)
        assertEquals(41, result.servers.size)
        assertEquals("Y/Lbzeg7zCzPC3KEJ1drM6SNYXePvXWmOLHHaFRL2I0=", Base64.toBase64String(result.servers[0].id))
    }

    @Test
    fun `returns Invalid if json incomplete`() = runBlocking {
        // given we have an incomplete json file

        // when we parse the data
        val result = LogListJsonParserV2().parseJson(jsonIncomplete)

        // then invalid is returned
        assertIsA<LogListJsonBadFormat>(result)
    }

    @Test
    fun `validUntil null when not frozen or retired`() = runBlocking {
        // given we have a valid json file and signature

        // when we parse the data
        val result = LogListJsonParserV2().parseJson(json)

        // then validUntil is set to the the STH timestamp
        require(result is LogListResult.Valid)
        val logServer = result.servers[1]
        assertNull(logServer.validUntil)
    }

    @Test
    fun `validUntil set from Retired`() = runBlocking {
        // given we have a valid json file and signature

        // when we parse the data
        val result = LogListJsonParserV2().parseJson(json)

        // then validUntil is set to the the STH timestamp
        require(result is LogListResult.Valid)

        val symantecId = Base64.decode("3esdK3oNT6Ygi4GtgWhwfi6OnQHVXIiNPRHEzbbsvsw=")

        val logServer = result.servers.first { it.id.contentEquals(symantecId) }
        assertNotNull(logServer.validUntil)
        assertEquals(1550275200000, logServer.validUntil)
    }

    companion object {
        private val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()
        private val jsonIncomplete = TestData.file(TestData.TEST_LOG_LIST_JSON_INCOMPLETE).readText()
    }
}
