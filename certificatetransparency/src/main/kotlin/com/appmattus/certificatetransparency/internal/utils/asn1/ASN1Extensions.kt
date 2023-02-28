package com.appmattus.certificatetransparency.internal.utils.asn1

internal data class ASN1Extensions(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val extensions: List<ASN1Object> by lazy {
        // Treating SETS the same as sequence
        val subObjects = mutableListOf<ASN1Object>()
        var subOffset = 0
        while (subOffset < encoded.size) {
            val subObject = encoded.range(subOffset, encoded.size).toAsn1()
            subObjects.add(subObject)
            subOffset += subObject.totalLength
        }
        subObjects
    }

    override fun toString(): String {
        val values = extensions
        return "Extensions (${values.size} elem)" + values.joinToString(prefix = "\n", separator = "\n") { it.toString() }.prependIndent("  ")
    }

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer): ASN1Extensions = ASN1Extensions(tag, totalLength, encoded)
    }
}
