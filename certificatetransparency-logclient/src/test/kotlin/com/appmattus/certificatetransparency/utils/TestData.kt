/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

import org.junit.Ignore
import java.io.File

/** Constants for tests.  */
@Ignore("Contains constants for tests")
object TestData {
    private const val DATA_ROOT = "/testdata/"

    // Root CA cert.
    const val ROOT_CA_CERT = DATA_ROOT + "ca-cert.pem"

    // Ordinary cert signed by ca-cert, with SCT served separately.
    const val TEST_CERT = DATA_ROOT + "test-cert.pem"

    const val TEST_COLLIDING_ROOTS = DATA_ROOT + "test-colliding-roots.pem"

    const val TEST_ROOT_CERTS = DATA_ROOT + "test-root-certs"

    fun file(name: String): File {
        return File(TestData::class.java.getResource(name)!!.file)
    }
}
