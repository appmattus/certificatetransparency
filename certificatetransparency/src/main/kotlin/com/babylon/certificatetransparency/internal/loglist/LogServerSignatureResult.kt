package com.babylon.certificatetransparency.internal.loglist

import com.babylon.certificatetransparency.internal.utils.stringStackTrace
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException

public sealed class LogServerSignatureResult {
    public object Valid : LogServerSignatureResult() {
        override fun toString(): String = "Valid signature"
    }

    public sealed class Invalid : LogServerSignatureResult() {
        public object SignatureFailed : Invalid() {
            override fun toString(): String = "Invalid signature"
        }

        public data class SignatureNotValid(val exception: SignatureException) : Invalid() {
            override fun toString(): String = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }

        public data class PublicKeyNotValid(val exception: InvalidKeyException) : Invalid() {
            override fun toString(): String = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }

        public data class NoSuchAlgorithm(val exception: NoSuchAlgorithmException) : Invalid() {
            override fun toString(): String = "Invalid signature (public key) with ${exception.stringStackTrace()}"
        }
    }
}
