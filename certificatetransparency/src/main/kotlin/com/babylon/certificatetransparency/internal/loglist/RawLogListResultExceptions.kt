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
import com.babylon.certificatetransparency.loglist.RawLogListResult

public data class RawLogListJsonFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.json failed to load with ${exception.stringStackTrace()}"
}

public data class RawLogListSigFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.sig failed to load with ${exception.stringStackTrace()}"
}

public data class RawLogListZipFailedLoadingWithException(val exception: Exception) : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip failed to load with ${exception.stringStackTrace()}"
}

public object RawLogListJsonFailedTooBig : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.json is too large"
}

public object RawLogListSigFailedTooBig : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.sig is too large"
}

public object RawLogListZipFailedTooBig : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip is too large"
}

public object RawLogListZipFailedJsonMissing : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip missing log-list.json file"
}

public object RawLogListZipFailedSigMissing : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip missing log-list.sig file"
}

public object RawLogListZipFailedJsonTooBig : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip contains too large log-list.json file"
}

public object RawLogListZipFailedSigTooBig : RawLogListResult.Failure() {
    override fun toString(): String = "log-list.zip contains too large log-list.sig file"
}
