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

package com.appmattus.certificatetransparency.internal.utils.asn1.header

import java.math.BigInteger

internal data class ASN1HeaderTag(
    val tagClass: TagClass,
    val tagForm: TagForm,
    val tagNumber: BigInteger,
    val blockLength: Int
) {
    constructor(tagClass: TagClass, tagForm: TagForm, tagNumber: Int, blockLength: Int) :
        this(tagClass, tagForm, tagNumber.toBigInteger(), blockLength)

    private val longTagNumber: Long? = if (tagNumber < Long.MAX_VALUE.toBigInteger()) tagNumber.toLong() else null

    internal fun isTagNumber(tagNumber: Int): Boolean {
        return (longTagNumber != null && this.longTagNumber == tagNumber.toLong()) || (this.tagNumber == tagNumber.toBigInteger())
    }

    fun isUniversal(tagNumber: Int): Boolean {
        return this.tagClass == TagClass.Universal && isTagNumber(tagNumber)
    }

    fun isContextSpecific(tagNumber: Int, isConstructed: Boolean = true): Boolean {
        return this.tagClass == TagClass.ContextSpecific && isTagNumber(tagNumber) &&
            (((isConstructed && this.tagForm == TagForm.Constructed) || (!isConstructed && this.tagForm == TagForm.Primitive)))
    }
}
