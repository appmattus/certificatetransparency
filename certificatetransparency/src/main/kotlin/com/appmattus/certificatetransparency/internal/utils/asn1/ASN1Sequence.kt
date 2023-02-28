package com.appmattus.certificatetransparency.internal.utils.asn1

internal data class ASN1Sequence(
    override val tag: Int,
    override val totalLength: Int,
    override val encoded: ByteBuffer,
) : ASN1Object {

    val values: List<ASN1Object> by lazy {
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
        @Suppress("MagicNumber")
        val name = if (tag == 0x30) "SEQUENCE" else "SET"
        return "$name (${values.size} elem)" + values.joinToString(prefix = "\n", separator = "\n") { it.toString() }.prependIndent("  ")
    }

    companion object {
        fun create(tag: Int, totalLength: Int, encoded: ByteBuffer) = ASN1Sequence(tag, totalLength, encoded)
    }
}
