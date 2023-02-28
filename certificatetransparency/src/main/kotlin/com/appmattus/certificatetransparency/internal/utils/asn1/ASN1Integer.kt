package com.appmattus.certificatetransparency.internal.utils.asn1

import java.math.BigInteger

internal class ASN1Integer private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer
) : ASN1Object {

    val value: BigInteger by lazy {
        BigInteger(encoded.toList().toByteArray())
    }

    override fun toString(): String = "INTEGER $value"

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = ASN1Integer(tag, encoded)
    }
}
