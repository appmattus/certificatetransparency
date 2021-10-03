/*
 * Copyright 2021 Appmattus Limited
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

package com.appmattus.certificaterevocation

import java.io.IOException
import java.security.cert.X509Certificate

/**
 * Abstract class providing the results of performing certificate revocation checks
 */
public sealed class RevocationResult {
    /**
     * Abstract class representing certificate revocation checks passed
     */
    public sealed class Success : RevocationResult() {

        /**
         * Certificate revocation checks passed
         */
        public object Trusted : Success() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Success: Certificates not in revocation list"
        }

        /**
         * Insecure connection so no certificate to check revocation
         */
        public object InsecureConnection : Success() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Success: Revocation not enabled for insecure connection"
        }
    }

    /**
     * Abstract class representing certificate revocation checks failed
     */
    public sealed class Failure : RevocationResult() {

        /**
         * Certificate revocation checks failed as no certificates are present
         */
        public object NoCertificates : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: No certificates"
        }

        /**
         * Certificate revocation checks failed as server not trusted
         */
        public data class CertificateRevoked(val certificate: X509Certificate) : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: Certificate is revoked"
        }

        /**
         * Certificate revocation checks failed due to an unknown [IOException]
         * @property ioException The [IOException] that occurred
         */
        public data class UnknownIoException(val ioException: IOException) : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: IOException $ioException"
        }
    }
}
