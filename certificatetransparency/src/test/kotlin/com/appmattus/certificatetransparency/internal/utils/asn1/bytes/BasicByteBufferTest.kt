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

package com.appmattus.certificatetransparency.internal.utils.asn1.bytes

import com.appmattus.certificatetransparency.internal.utils.asn1.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BasicByteBufferTest {

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
}
