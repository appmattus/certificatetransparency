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

import com.appmattus.certificatetransparency.internal.loglist.model.v2.Hostname
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class HostnameDeserializer : KSerializer<Hostname> {

    override val descriptor = PrimitiveSerialDescriptor("Hostname", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = Hostname(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Hostname) = throw IllegalStateException("Serialization not supported")
}
