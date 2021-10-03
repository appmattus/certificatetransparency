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
import com.appmattus.certificatetransparency.loglist.RawLogListResult

internal data class RawLogListJsonFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString() = "log-list.json failed to load with ${exception.stringStackTrace()}"
}

internal data class RawLogListSigFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString() = "log-list.sig failed to load with ${exception.stringStackTrace()}"
}

internal data class RawLogListZipFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip failed to load with ${exception.stringStackTrace()}"
}

internal object RawLogListJsonFailedTooBig : RawLogListResult.Failure() {
    override fun toString() = "log-list.json is too large"
}

internal object RawLogListSigFailedTooBig : RawLogListResult.Failure() {
    override fun toString() = "log-list.sig is too large"
}

internal object RawLogListZipFailedTooBig : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip is too large"
}

internal object RawLogListZipFailedJsonMissing : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip missing log-list.json file"
}

internal object RawLogListZipFailedSigMissing : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip missing log-list.sig file"
}

internal object RawLogListZipFailedJsonTooBig : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip contains too large log-list.json file"
}

internal object RawLogListZipFailedSigTooBig : RawLogListResult.Failure() {
    override fun toString() = "log-list.zip contains too large log-list.sig file"
}
