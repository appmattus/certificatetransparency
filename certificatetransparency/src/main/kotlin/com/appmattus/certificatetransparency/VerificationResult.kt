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
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency

import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogServer
import java.io.IOException

/**
 * Abstract class providing the results of performing certificate transparency checks
 */
public sealed class VerificationResult {
    /**
     * Abstract class representing certificate transparency checks passed
     */
    public sealed class Success : VerificationResult() {

        /**
         * Certificate transparency checks passed as [host] is not being verified
         * @property host The host certificate transparency is not enabled for
         */
        public data class DisabledForHost(val host: String) : Success() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Success: SCT not enabled for $host"
        }

        /**
         * Certificate transparency checks passed with the provided [scts]
         * @property scts Map of logIds to [SctVerificationResult] showing the results of checking each Signed Certificate Timestamp
         */
        public data class Trusted(val scts: Map<String, SctVerificationResult>) : Success() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String =
                "Success: SCT trusted logs ${scts.forDisplay()}"
        }

        public data class InsecureConnection(val host: String) : Success() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Success: SCT not enabled for insecure connection to $host"
        }
    }

    /**
     * Abstract class representing certificate transparency checks failed
     */
    public sealed class Failure : VerificationResult() {

        /**
         * Certificate transparency checks failed as no certificates are present
         */
        public object NoCertificates : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: No certificates"
        }

        /**
         * Certificate transparency checks failed as couldn't load list of [LogServer]. This can occur if there are network problems loading
         * the log-list.json or log-list.sig file along with issues with the signature
         */
        public data class LogServersFailed(val logListResult: LogListResult.Invalid) : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: Unable to load log servers with $logListResult"
        }

        /**
         * Certificate transparency checks failed as no Signed Certificate Timestamps have been found in the X.509 extensions. This can occur
         * if your server relies on providing SCTs through TLS extensions or OCSP stapling instead.
         */
        public object NoScts : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: This certificate does not have any Signed Certificate Timestamps in it."
        }

        /**
         * Certificate transparency checks failed as there are not enough valid Signed Certificate Timestamps
         * @property scts Map of logIds to [SctVerificationResult] stating which SCTs passed or failed checks
         * @property minSctCount The number of valid SCTs required for trust to be established
         */
        public data class TooFewSctsTrusted(val scts: Map<String, SctVerificationResult>, val minSctCount: Int) : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String {
                val trustedScts = scts.count { it.value is SctVerificationResult.Valid }
                return "Failure: Too few trusted SCTs, required $minSctCount, found $trustedScts in ${scts.forDisplay()}"
            }
        }

        /**
         * Certificate transparency checks failed due to an unknown [IOException]
         * @property ioException The [IOException] that occurred
         */
        public data class UnknownIoException(val ioException: IOException) : Failure() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "Failure: IOException $ioException"
        }
    }

    public companion object {
        private fun Map<String, SctVerificationResult>.forDisplay() = map { "${it.key}:${it.value}" }
    }
}
