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
