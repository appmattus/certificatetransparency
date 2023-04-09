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

package com.appmattus.certificatetransparency.internal.utils.asn1

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

abstract class ASN1BaseTest {

    private val logMessages = mutableListOf<String>()
    private val handler = object : Handler() {
        override fun publish(record: LogRecord) {
            logMessages.add(record.message)
        }

        override fun flush() = Unit
        override fun close() = Unit
    }

    @Before
    fun setUp() {
        Logger.getLogger("ASN1").addHandler(handler)
    }

    @After
    fun tearDown() {
        Logger.getLogger("ASN1").removeHandler(handler)
    }

    fun assertNoWarnings() {
        assertEquals(0, logMessages.size)
    }

    fun assertWarnings(expectedMessage: String) {
        assertEquals(expectedMessage, logMessages.last())
    }

    fun assertWarnings(vararg expectedMessage: String) {
        assertEquals(expectedMessage.size, logMessages.size)
        expectedMessage.forEachIndexed { index, expected ->
            assertEquals(expected, logMessages[index])
        }
    }
}
