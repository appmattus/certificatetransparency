/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist.deserializer

import com.appmattus.certificatetransparency.internal.loglist.model.v2.State
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 *
 * "state": {
"qualified": {
"timestamp": "2019-06-04T00:00:00Z"
}
},
 */
internal class StateDeserializerTest {

    @Serializable
    internal data class TestObject(@Serializable(with = StateDeserializer::class) @SerialName("state") val state: State)

    @Test
    fun pending() {
        val result = json.decodeFromString(TestObject.serializer(), "{\"state\":{\"pending\":{\"timestamp\":\"2019-06-04T00:00:00Z\"}}}")
        assertIsA<State.Pending>(result.state)
    }

    @Test
    fun qualified() {
        val result = json.decodeFromString(TestObject.serializer(), "{\"state\":{\"qualified\":{\"timestamp\":\"2019-06-04T00:00:00Z\"}}}")
        assertIsA<State.Qualified>(result.state)
    }

    @Test
    fun usable() {
        val result = json.decodeFromString(TestObject.serializer(), "{\"state\":{\"usable\":{\"timestamp\":\"2019-06-04T00:00:00Z\"}}}")
        assertIsA<State.Usable>(result.state)
    }

    @Test
    fun readOnly() {
        val result = json.decodeFromString(
            deserializer = TestObject.serializer(),
            string = "{\"state\":{\"readonly\":{\"timestamp\":\"2019-06-04T00:00:00Z\"," +
                "\"final_tree_head\":{\"tree_size\":0,\"sha256_root_hash\":\"12345678901234567890123456789012345678901234\"}}}}"
        )
        assertIsA<State.ReadOnly>(result.state)
    }

    @Test
    fun retired() {
        val result = json.decodeFromString(TestObject.serializer(), "{\"state\":{\"retired\":{\"timestamp\":\"2019-06-04T00:00:00Z\"}}}")
        assertIsA<State.Retired>(result.state)
    }

    @Test
    fun rejected() {
        val result = json.decodeFromString(TestObject.serializer(), "{\"state\":{\"rejected\":{\"timestamp\":\"2019-06-04T00:00:00Z\"}}}")
        assertIsA<State.Rejected>(result.state)
    }

    companion object {

        val json: Json = Json
    }
}
