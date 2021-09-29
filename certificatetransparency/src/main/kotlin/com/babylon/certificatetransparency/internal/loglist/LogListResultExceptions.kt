/*
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
 */

package com.babylon.certificatetransparency.internal.loglist

import com.babylon.certificatetransparency.internal.utils.stringStackTrace
import com.babylon.certificatetransparency.loglist.LogListResult
import com.google.gson.JsonParseException

public data class SignatureVerificationFailed(val signatureResult: LogServerSignatureResult.Invalid) : LogListResult.Invalid()

public object NoLogServers : LogListResult.Invalid() {
    override fun toString(): String = "log-list.json contains no log servers"
}

public object LogListJsonFailedLoading : LogListResult.Invalid() {
    override fun toString(): String = "log-list.json failed to load"
}

public data class LogListJsonFailedLoadingWithException(val exception: Exception) : LogListResult.Invalid() {
    override fun toString(): String = "log-list.json failed to load with ${exception.stringStackTrace()}"
}

public data class LogListSigFailedLoadingWithException(val exception: Exception) : LogListResult.Invalid() {
    override fun toString(): String = "log-list.sig failed to load with ${exception.stringStackTrace()}"
}

public data class LogListJsonBadFormat(val exception: JsonParseException) : LogListResult.Invalid() {
    override fun toString(): String = "log-list.json badly formatted with ${exception.stringStackTrace()}"
}

public data class LogServerInvalidKey(val exception: Exception, val key: String) : LogListResult.Invalid() {
    override fun toString(): String = "Public key for log server $key cannot be used with ${exception.stringStackTrace()}"
}
