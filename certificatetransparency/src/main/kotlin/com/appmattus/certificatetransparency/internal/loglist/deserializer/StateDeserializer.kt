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

package com.appmattus.certificatetransparency.internal.loglist.deserializer

import com.appmattus.certificatetransparency.internal.loglist.model.v2.State
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

internal class StateDeserializer : JsonTransformingSerializer<State>(State.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {
            val key = element.keys.first()
            val entry = element[key] as JsonObject
            return JsonObject(entry + ("type" to JsonPrimitive(key)))
        }

        return element
    }
}
