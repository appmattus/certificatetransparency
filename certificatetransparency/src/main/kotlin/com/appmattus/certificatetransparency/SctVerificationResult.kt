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

/**
 * Abstract class providing the results of verifying a Signed Certificate Timestamp
 */
public sealed class SctVerificationResult {
    /**
     * Signed Certificate Timestamp checks passed
     */
    public object Valid : SctVerificationResult() {
        /**
         * Returns a string representation of the object.
         */
        override fun toString(): String = "Valid SCT"
    }

    /**
     * Abstract class representing Signed Certificate Timestamp checks failed
     */
    public sealed class Invalid : SctVerificationResult() {
        /**
         * Signed Certificate Timestamp checks failed as the signature could not be verified
         */
        public object FailedVerification : Invalid() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "SCT signature failed verification"
        }

        /**
         * Signed Certificate Timestamp checks failed as there was no log server we trust in the log-list.json
         */
        public object NoTrustedLogServerFound : Invalid() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "No trusted log server found for SCT"
        }

        /**
         * Signed Certificate Timestamp checks failed as the [timestamp] of the SCT is in the future
         * @property timestamp The timestamp of the SCT
         * @property now The time now
         */
        public data class FutureTimestamp(val timestamp: Long, val now: Long) : Invalid() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "SCT timestamp, $timestamp, is in the future, current timestamp is $now."
        }

        /**
         * Signed Certificate Timestamp checks failed as the log server is no longer trusted
         * @property timestamp The timestamp of the SCT
         * @property logServerValidUntil The time the log server was valid till
         */
        public data class LogServerUntrusted(val timestamp: Long, val logServerValidUntil: Long) : Invalid() {
            /**
             * Returns a string representation of the object.
             */
            override fun toString(): String = "SCT timestamp, $timestamp, is greater than the log server validity, $logServerValidUntil."
        }

        /**
         * Signed Certificate Timestamp checks failed for an unspecified reason
         */
        public open class Failed : Invalid()

        /**
         * Signed Certificate Timestamp checks failed as an [exception] was detected
         */
        public abstract class FailedWithException : Invalid() {
            /**
             * The [exception] that occurred
             */
            public abstract val exception: Exception?
        }
    }
}
