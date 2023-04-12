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
 */

package com.appmattus.certificatetransparency.internal.utils.asn1.x509

import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Integer
import com.appmattus.certificatetransparency.internal.utils.asn1.ASN1Object
import com.appmattus.certificatetransparency.internal.utils.asn1.bytes.ByteBuffer
import com.appmattus.certificatetransparency.internal.utils.asn1.header.ASN1HeaderTag
import com.appmattus.certificatetransparency.internal.utils.asn1.toAsn1

internal class Version private constructor(
    override val tag: ASN1HeaderTag,
    override val encoded: ByteBuffer,
) : ASN1Object() {

    val value: Int by lazy { (encoded.toAsn1() as ASN1Integer).value.toInt() + 1 }

    override fun toString(): String = "Version $value"

    companion object {
        fun create(tag: ASN1HeaderTag, encoded: ByteBuffer) = Version(tag, encoded)
    }
}
