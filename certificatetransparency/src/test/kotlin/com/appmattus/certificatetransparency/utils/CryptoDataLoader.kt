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

package com.appmattus.certificatetransparency.utils

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

/** Class for reading various crypto structures off disk.  */
internal object CryptoDataLoader {
    /**
     * Returns a list of certificates from an input stream of PEM-encoded certs.
     *
     * @property pemStream input stream with PEM bytes
     * @return A list of certificates in the PEM file.
     */
    private fun parseCertificates(pemStream: InputStream): List<Certificate> {
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificates(pemStream).toList()
    }

    /**
     * Parses a PEM-encoded file containing a list of certificates.
     *
     * @property pemCertsFile File to parse.
     * @return A list of certificates from the certificates in the file.
     * @throws FileNotFoundException If the file is not present.
     */
    fun certificatesFromFile(pemCertsFile: File): List<Certificate> {
        return pemCertsFile.inputStream().use {
            parseCertificates(it)
        }
    }
}
