/*
 * Copyright 2021-2023 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist.parser

import com.appmattus.certificatetransparency.internal.loglist.RawLogListZipFailedLoadingWithException
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogServerSignatureResult
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import java.security.PublicKey

internal class RawLogListToLogListResultTransformer(
    private val publicKey: PublicKey,
    private val logListVerifier: LogListVerifier = LogListVerifier(publicKey),
    private val logListJsonParser: LogListJsonParser = LogListJsonParserV3()
) {
    fun transform(rawLogListResult: RawLogListResult) =
        when (rawLogListResult) {
            is RawLogListResult.Success -> transformSuccess(rawLogListResult)
            is RawLogListResult.Failure -> transformFailure(rawLogListResult)
        }

    private fun transformFailure(rawLogListResult: RawLogListResult.Failure) =
        when (rawLogListResult) {
            is RawLogListZipFailedLoadingWithException ->
                LogListResult.Invalid.LogListZipFailedLoadingWithException(rawLogListResult.exception)
            else -> LogListResult.Invalid.LogListJsonFailedLoading
        }

    private fun transformSuccess(rawLogListResult: RawLogListResult.Success): LogListResult {
        val (logListJson, signature) = rawLogListResult
        return when (val signatureResult = logListVerifier.verify(logListJson, signature)) {
            is LogServerSignatureResult.Valid -> logListJsonParser.parseJson(logListJson.toString(Charsets.UTF_8))
            is LogServerSignatureResult.Invalid -> LogListResult.Invalid.SignatureVerificationFailed(signatureResult)
        }
    }
}
