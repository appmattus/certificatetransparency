package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1

internal class Version private constructor(
    override val tag: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val version: Int by lazy { (encoded.toAsn1() as ASN1Integer).value.toInt() + 1 }

    override fun toString(): String = "Version $version"

    companion object {
        fun create(tag: Int, encoded: ByteBuffer) = Version(tag, encoded)
    }
}
