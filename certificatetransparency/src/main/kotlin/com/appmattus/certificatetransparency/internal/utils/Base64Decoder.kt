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

/**
 * This class implements a decoder for decoding byte data using the
 * Base64 encoding scheme as specified in RFC 4648 and RFC 2045.
 *
 *
 *  The Base64 padding character `'='` is accepted and
 * interpreted as the end of the encoded byte data, but is not
 * required. So if the final unit of the encoded byte data only has
 * two or three Base64 characters (without the corresponding padding
 * character(s) padded), they are decoded as if followed by padding
 * character(s). If there is a padding character present in the
 * final unit, the correct number of padding character(s) must be
 * present, otherwise `IllegalArgumentException` (
 * `IOException` when reading from a Base64 stream) is thrown
 * during decoding.
 *
 *
 *  Instances of [Base64Decoder] class are safe for use by
 * multiple concurrent threads.
 *
 *
 *  Unless otherwise noted, passing a `null` argument to
 * a method of this class will cause a
 * [NullPointerException][java.lang.NullPointerException] to
 * be thrown.
 *
 * @see Base64Encoder
 */
@Suppress(
    "MagicNumber", "ThrowsCount", "LoopWithTooManyJumpStatements", "LongMethod", "ComplexMethod", "NestedBlockDepth",
    "ComplexCondition"
)
internal class Base64Decoder {
    /**
     * Decodes all bytes from the input byte array using the [Base64]
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param src
     * the byte array to decode
     *
     * @return A newly-allocated byte array containing the decoded bytes.
     *
     * @throws IllegalArgumentException
     * if `src` is not in valid Base64 scheme
     */
    fun decode(src: ByteArray): ByteArray {
        var dst = ByteArray(outLength(src))
        val ret = decode0(src, src.size, dst)
        if (ret != dst.size) {
            dst = dst.copyOf(ret)
        }
        return dst
    }

    private fun outLength(src: ByteArray): Int {
        val sp = 0
        var paddings = 0
        val len = src.size - sp
        if (len == 0) return 0
        if (len < 2) {
            throw IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes")
        }
        if (src[src.size - 1].toInt().toChar() == '=') {
            paddings++
            if (src[src.size - 2].toInt().toChar() == '=') paddings++
        }
        if (paddings == 0 && len and 0x3 != 0) paddings = 4 - (len and 0x3)
        return 3 * ((len + 3) / 4) - paddings
    }

    private fun decode0(src: ByteArray, sl: Int, dst: ByteArray): Int {
        var sp = 0
        val base64 = fromBase64
        var dp = 0
        var bits = 0
        var shiftTo = 18 // pos of first byte of 4-byte atom
        while (sp < sl) {
            if (shiftTo == 18 && sp + 4 < sl) { // fast path
                val sl0 = sp + (sl - sp and 3.inv())
                while (sp < sl0) {
                    val b1 = base64[src[sp++].toInt() and 0xff]
                    val b2 = base64[src[sp++].toInt() and 0xff]
                    val b3 = base64[src[sp++].toInt() and 0xff]
                    val b4 = base64[src[sp++].toInt() and 0xff]
                    if (b1 or b2 or b3 or b4 < 0) { // non base64 byte
                        sp -= 4
                        break
                    }
                    val bits0 = b1 shl 18 or (b2 shl 12) or (b3 shl 6) or b4
                    dst[dp++] = (bits0 shr 16).toByte()
                    dst[dp++] = (bits0 shr 8).toByte()
                    dst[dp++] = bits0.toByte()
                }
                if (sp >= sl) break
            }
            var b: Int = src[sp++].toInt() and 0xff
            if (base64[b].also { b = it } < 0) {
                if (b == -2) {
                    // padding byte '='
                    // =     shiftto==18 unnecessary padding
                    // x=    shiftto==12 a dangling single x
                    // x     to be handled together with non-padding case
                    // xx=   shiftto==6&&sp==sl missing last =
                    // xx=y  shiftto==6 last is not =
                    if ((shiftTo == 6 && (sp == sl || src[sp++].toInt().toChar() != '=') || shiftTo == 18)) {
                        throw IllegalArgumentException("Input byte array has wrong 4-byte ending unit")
                    }
                    break
                }
                throw IllegalArgumentException("Illegal base64 character " + src[sp - 1].toInt().toString(16))
            }
            bits = bits or (b shl shiftTo)
            shiftTo -= 6
            if (shiftTo < 0) {
                dst[dp++] = (bits shr 16).toByte()
                dst[dp++] = (bits shr 8).toByte()
                dst[dp++] = bits.toByte()
                shiftTo = 18
                bits = 0
            }
        }
        // reached end of byte array or hit padding '=' characters.
        when (shiftTo) {
            6 -> {
                dst[dp++] = (bits shr 16).toByte()
            }
            0 -> {
                dst[dp++] = (bits shr 16).toByte()
                dst[dp++] = (bits shr 8).toByte()
            }
            else -> if (shiftTo == 12) {
                // dangling single "x", incorrectly encoded.
                throw IllegalArgumentException("Last unit does not have enough valid bits")
            }
        }
        // anything left is invalid, if is not MIME. if MIME, ignore all non-base64 character
        while (sp < sl) {
            throw IllegalArgumentException("Input byte array has incorrect ending byte at $sp")
        }
        return dp
    }

    companion object {
        /**
         * Lookup table for decoding unicode characters drawn from the
         * "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into
         * their 6-bit positive integer equivalents.  Characters that
         * are not in the Base64 alphabet but fall within the bounds of
         * the array are encoded to -1.
         *
         */
        internal val fromBase64 = IntArray(256) { -1 }

        init {
            Base64Encoder.toBase64.forEachIndexed { index, byte -> fromBase64[byte.toInt() and 0xff] = index }
            fromBase64['='.code] = -2
        }
    }
}
