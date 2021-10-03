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
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.serialization

import java.io.IOException
import java.io.OutputStream
import kotlin.math.pow

private const val BITS_IN_BYTE = 8

/**
 * Write a numeric value of numBytes bytes, MSB first.
 *
 * @receiver stream to write to.
 * @property value number to write. Must be non-negative.
 * @property numBytes number to bytes to write for the value.
 * @throws IOException
 */
internal fun OutputStream.writeUint(value: Long, numBytes: Int) {
    require(value >= 0)
    @Suppress("MagicNumber")
    require(value < 256.0.pow(numBytes.toDouble())) { "Value $value cannot be stored in $numBytes bytes" }
    var numBytesRemaining = numBytes
    while (numBytesRemaining > 0) {
        // MSB first.
        val shiftBy = (numBytesRemaining - 1) * BITS_IN_BYTE
        @Suppress("MagicNumber")
        val mask = 0xff.toLong() shl shiftBy
        write((value and mask shr shiftBy).toByte().toInt())
        numBytesRemaining--
    }
}

/**
 * Write a variable-length array to the output stream.
 *
 * @receiver stream to write to.
 * @property data data to write.
 * @property maxDataLength Maximal data length. Used for calculating the number of bytes needed to
 * store the length of the data.
 * @throws IOException
 */
internal fun OutputStream.writeVariableLength(data: ByteArray, maxDataLength: Int) {
    require(data.size <= maxDataLength)
    val bytesForDataLength = Deserializer.bytesForDataLength(maxDataLength)
    writeUint(data.size.toLong(), bytesForDataLength)
    write(data, 0, data.size)
}
