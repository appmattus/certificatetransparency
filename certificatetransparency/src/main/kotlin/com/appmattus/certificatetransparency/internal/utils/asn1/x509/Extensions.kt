package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1

internal class Extensions private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val extensions: List<ASN1Object> by lazy {
        (encoded.toAsn1() as ASN1Sequence).values.map { Extension.create(it as ASN1Sequence) }
    }

    override fun toString(): String {
        val values = extensions
        return values.joinToString(separator = "\n\n") { it.toString() }
    }

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = Extensions(tag, encoded)
    }
}
