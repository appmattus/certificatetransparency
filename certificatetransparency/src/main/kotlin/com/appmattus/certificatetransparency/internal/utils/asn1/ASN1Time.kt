package com.appmattus.certificatetransparency.internal.utils.asn1

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class ASN1Time private constructor(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: Instant by lazy {
        @Suppress("MagicNumber")
        val pattern = if (encoded.size == 13) "yyMMddHHmmss'Z'" else "yyyyMMddHHmmss'Z'"
        val formatter = DateTimeFormatter.ofPattern(pattern)

        val time = encoded.toList().toByteArray().decodeToString()

        LocalDateTime.parse(time, formatter).toInstant(ZoneOffset.UTC)
    }

    override fun toString(): String = "TIME $value"

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer) = ASN1Time(tag, totalLength, encoded)
    }
}
