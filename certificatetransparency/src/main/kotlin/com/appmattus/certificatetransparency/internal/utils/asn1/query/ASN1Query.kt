package com.appmattus.certificatetransparency.internal.utils.asn1.query

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1ObjectIdentifier
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Sequence

internal data class ASN1Query(private val obj: ASN1Object) {
    fun seq(index: Int): ASN1Query {
        return ASN1Query((obj as ASN1Sequence).values[index])
    }

    fun oid(): String {
        return (obj as ASN1ObjectIdentifier).value
    }
}

internal fun <T : Any?> ASN1Object.query(query: ASN1Query.() -> T): T {
    return ASN1Query(this).query()
}
