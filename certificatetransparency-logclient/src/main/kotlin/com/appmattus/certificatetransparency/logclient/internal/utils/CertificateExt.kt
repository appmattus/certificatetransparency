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

package com.appmattus.certificatetransparency.logclient.internal.utils

import com.appmattus.certificatetransparency.logclient.internal.serialization.CTConstants.POISON_EXTENSION_OID
import com.appmattus.certificatetransparency.logclient.internal.serialization.CTConstants.PRECERTIFICATE_SIGNING_OID
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/** Helper class for finding out all kinds of information about a certificate.  */

/**
 * @throws java.security.cert.CertificateParsingException
 */
@OptIn(ExperimentalContracts::class)
internal fun Certificate.isPreCertificateSigningCert(): Boolean {
    contract {
        returns(true) implies (this@isPreCertificateSigningCert is X509Certificate)
    }
    return this is X509Certificate && extendedKeyUsage?.contains(PRECERTIFICATE_SIGNING_OID) == true
}

@OptIn(ExperimentalContracts::class)
internal fun Certificate.isPreCertificate(): Boolean {
    contract {
        returns(true) implies (this@isPreCertificate is X509Certificate)
    }
    return this is X509Certificate && criticalExtensionOIDs?.contains(POISON_EXTENSION_OID) == true
}
