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

package com.appmattus.certificatetransparency.internal.loglist.deserializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class Rfc3339DeserializerTest {

    @Parameterized.Parameter(0)
    lateinit var input: String

    @Parameterized.Parameter(1)
    lateinit var expected: String

    @Serializable
    data class TestObject(@Serializable(with = Rfc3339Deserializer::class) @SerialName("timestamp") val timestamp: Long)

    @Test
    fun test() {
        if (expected == "fail") {
            assertThrows(NumberFormatException::class.java) {
                json.decodeFromString(TestObject.serializer(), "{\"timestamp\":\"$input\"}").timestamp
            }
        } else {
            val result = json.decodeFromString(TestObject.serializer(), "{\"timestamp\":\"$input\"}").timestamp

            assertEquals(expected.toLong(), result)
        }
    }

    companion object {

        val json: Json = Json

        @JvmStatic
        @Parameterized.Parameters(name = "Rfc3339Deserializer({0}) = {1}")
        fun data() = arrayOf(
            // success cases
            arrayOf("2018-04-16T10:04:55Z", "1523873095000"),
            arrayOf("2012-11-06T12:10:44.000-08:00", "1352232644000"),
            arrayOf("2012-11-06T16:10:44.000-04:00", "1352232644000"),
            arrayOf("2012-11-06T17:10:44.000-03:00", "1352232644000"),
            arrayOf("2012-11-06T20:10:44.001Z", "1352232644001"),
            arrayOf("2012-11-06T20:10:44.01Z", "1352232644010"),
            arrayOf("2012-11-06T20:10:44.1Z", "1352232644100"),
            arrayOf("2012-11-06", "1352160000000"),

            // failure cases
            arrayOf("abc", "fail"),
            arrayOf("2013-01-01 09:00:02", "fail"),
            // missing time
            arrayOf("2013-01-01T", "fail"),
            // invalid month
            arrayOf("1937--3-55T12:00:27+00:20", "fail"),
            // can't have time zone shift without time
            arrayOf("2013-01-01Z", "fail")
        )
    }
}
