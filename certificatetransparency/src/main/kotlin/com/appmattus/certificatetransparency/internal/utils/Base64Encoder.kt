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
 *
 * Code derived from https://gist.github.com/MarkusKramer/4db02c9983c76efc6aa56cf0bdc75a5b
 */

package com.appmattus.certificatetransparency.internal.utils

import kotlin.math.min

/**
 * This class implements an encoder for encoding byte data using
 * the Base64 encoding scheme as specified in RFC 4648 and RFC 2045.
 *
 *
 *  Instances of [Base64Encoder] class are safe for use by
 * multiple concurrent threads.
 *
 *
 *  Unless otherwise noted, passing a `null` argument to
 * a method of this class will cause a
 * [NullPointerException][java.lang.NullPointerException] to
 * be thrown.
 *
 * @see Base64Decoder
 */
@Suppress("MagicNumber")
internal class Base64Encoder {
    private fun outLength(sourceLength: Int): Int = 4 * ((sourceLength + 2) / 3)

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the [Base64] encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param src
     * the byte array to encode
     * @return A newly-allocated byte array containing the resulting encoded bytes.
     */
    fun encode(src: ByteArray): ByteArray {
        val len = outLength(src.size) // dst array size
        val dst = ByteArray(len)
        val ret = encode0(src, dst)
        return if (ret != dst.size) dst.copyOf(ret) else dst
    }

    private fun encodeBlock(src: ByteArray, sp: Int, sl: Int, dst: ByteArray, dp: Int) {
        var sp0 = sp
        var dp0 = dp
        while (sp0 < sl) {
            val bits: Int = src[sp0++].toInt() and 0xff shl 16 or (src[sp0++].toInt() and 0xff shl 8) or (src[sp0++].toInt() and 0xff)
            dst[dp0++] = toBase64[bits ushr 18 and 0x3f]
            dst[dp0++] = toBase64[bits ushr 12 and 0x3f]
            dst[dp0++] = toBase64[bits ushr 6 and 0x3f]
            dst[dp0++] = toBase64[bits and 0x3f]
        }
    }

    private fun encode0(src: ByteArray, dst: ByteArray): Int {
        val base64 = toBase64
        var sp = 0
        val slen = src.size / 3 * 3
        var dp = 0
        while (sp < slen) {
            val sl0: Int = min(sp + slen, slen)
            encodeBlock(src, sp, sl0, dst, dp)
            val dlen = (sl0 - sp) / 3 * 4
            dp += dlen
            sp = sl0
        }
        if (sp < src.size) { // 1 or 2 leftover bytes
            val b0: Int = src[sp++].toInt() and 0xff
            dst[dp++] = base64[b0 shr 2]
            if (sp == src.size) {
                dst[dp++] = base64[b0 shl 4 and 0x3f]
                dst[dp++] = '='.code.toByte()
                dst[dp++] = '='.code.toByte()
            } else {
                val b1: Int = src[sp].toInt() and 0xff
                dst[dp++] = base64[b0 shl 4 and 0x3f or (b1 shr 4)]
                dst[dp++] = base64[b1 shl 2 and 0x3f]
                dst[dp++] = '='.code.toByte()
            }
        }
        return dp
    }

    companion object {
        /**
         * This array is a lookup table that translates 6-bit positive integer index values into their "Base64 Alphabet" equivalents as
         * specified in "Table 1: The Base64 Alphabet" of RFC 2045 (and RFC 4648).
         */
        val toBase64 = charArrayOf(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        ).map { it.code.toByte() }.toByteArray()
    }
}
