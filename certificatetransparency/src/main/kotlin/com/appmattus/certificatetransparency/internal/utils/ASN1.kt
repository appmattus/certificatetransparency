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

@file:Suppress("MagicNumber", "ComplexCondition", "NestedBlockDepth")

package com.appmattus.certificatetransparency.internal.utils

import java.io.IOException
import java.io.InputStream

internal fun ByteArray.readNestedOctets(count: Int): ByteArray {
    val stream = inputStream()
    var length = 0
    repeat(count) {
        if (stream.read() != 0x04) throw IOException("Not an octet string")
        length = stream.readLength()!!
    }
    val result = ByteArray(length)
    val readLength = stream.read(result, 0, length)
    if (readLength != length) throw IOException("Bytes don't match expected size")
    return result
}

internal fun InputStream.readLength(): Int? {
    val firstByte = read() and 0xff

    return if (firstByte <= 0x7f) {
        firstByte
    } else if (firstByte == 0x80) {
        null
    } else {
        val length = firstByte and 0x7f

        var value = 0
        repeat(length) {
            value = value shl 8
            value += read() and 0xff
        }

        value
    }
}
