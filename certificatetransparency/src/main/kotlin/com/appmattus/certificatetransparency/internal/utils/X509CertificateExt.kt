/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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
 *
 * Code derived from https://github.com/google/certificate-transparency-java
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.utils

import com.appmattus.certificatetransparency.internal.serialization.CTConstants
import com.appmattus.certificatetransparency.internal.serialization.Deserializer
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import java.io.IOException
import java.security.cert.X509Certificate

/**
 * @throws IOException
 */
internal fun X509Certificate.signedCertificateTimestamps(): List<SignedCertificateTimestamp> {
    val bytes = getExtensionValue(CTConstants.SCT_CERTIFICATE_OID)
    // Equivalent of (ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(bytes).octets) as DEROctetString).octet
    return parseSctsFromCertExtension(bytes.readNestedOctets(2))
}

/**
 * @throws IOException
 */
private fun parseSctsFromCertExtension(extensionValue: ByteArray): List<SignedCertificateTimestamp> {
    val sctList = mutableListOf<SignedCertificateTimestamp>()
    val bis = extensionValue.inputStream()
    bis.readUint16() // first one is the length of all SCTs concatenated, we don't actually need this
    while (bis.available() > 2) {
        val sctBytes = bis.readOpaque16()
        sctList.add(Deserializer.parseSctFromBinary(sctBytes.inputStream()))
    }
    return sctList.toList()
}
