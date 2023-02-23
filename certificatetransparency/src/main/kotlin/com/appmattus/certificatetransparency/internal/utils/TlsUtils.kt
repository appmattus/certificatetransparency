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

package com.appmattus.certificatetransparency.internal.utils

import java.io.EOFException
import java.io.InputStream

internal fun InputStream.readUint16(): Int {
    val i1: Int = read()
    val i2: Int = read()
    if (i2 < 0) {
        throw EOFException()
    }
    return i1 shl 8 or i2
}

internal fun InputStream.readOpaque16(): ByteArray {
    val length = readUint16()
    return readFully(length)
}

private fun InputStream.readFully(length: Int): ByteArray {
    if (length < 1) return byteArrayOf()

    val buf = ByteArray(length)
    if (length != read(buf, 0, buf.size)) throw EOFException()

    return buf
}
