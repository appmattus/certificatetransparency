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
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.internal.utils.stringStackTrace
import com.appmattus.certificatetransparency.loglist.LogListResult
import kotlinx.serialization.SerializationException

internal data class SignatureVerificationFailed(val signatureResult: LogServerSignatureResult.Invalid) : LogListResult.Invalid()

internal object NoLogServers : LogListResult.Invalid() {
    override fun toString() = "log-list.json contains no log servers"
}

internal object LogListJsonFailedLoading : LogListResult.Invalid() {
    override fun toString() = "log-list.json failed to load"
}

internal data class LogListJsonFailedLoadingWithException(val exception: Exception) : LogListResult.Invalid() {
    override fun toString() = "log-list.json failed to load with ${exception.stringStackTrace()}"
}

internal data class LogListSigFailedLoadingWithException(val exception: Exception) : LogListResult.Invalid() {
    override fun toString() = "log-list.sig failed to load with ${exception.stringStackTrace()}"
}

internal data class LogListJsonBadFormat(val exception: SerializationException) : LogListResult.Invalid() {
    override fun toString() = "log-list.json badly formatted with ${exception.stringStackTrace()}"
}

internal data class LogServerInvalidKey(val exception: Exception, val key: String) : LogListResult.Invalid() {
    override fun toString() = "Public key for log server $key cannot be used with ${exception.stringStackTrace()}"
}
