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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.internal.utils.stringStackTrace
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException

internal sealed class LogServerSignatureResult {
    object Valid : LogServerSignatureResult() {
        override fun toString() = "Valid signature"
    }

    sealed class Invalid : LogServerSignatureResult() {
        object SignatureFailed : Invalid() {
            override fun toString() = "Invalid signature"
        }

        data class SignatureNotValid(val exception: SignatureException) : Invalid() {
            override fun toString() = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }

        data class PublicKeyNotValid(val exception: InvalidKeyException) : Invalid() {
            override fun toString() = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }

        data class NoSuchAlgorithm(val exception: NoSuchAlgorithmException) : Invalid() {
            override fun toString() = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }
    }
}
