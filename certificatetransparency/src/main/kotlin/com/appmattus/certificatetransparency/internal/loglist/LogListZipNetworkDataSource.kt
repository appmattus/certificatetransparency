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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.utils.LimitedSizeInputStream
import com.appmattus.certificatetransparency.internal.utils.isTooBigException
import com.appmattus.certificatetransparency.loglist.LogListService
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import java.io.File
import java.util.zip.ZipInputStream

internal class LogListZipNetworkDataSource(
    private val logListService: LogListService
) : DataSource<RawLogListResult> {

    override suspend fun get(): RawLogListResult = when (val logListZip = wrap(RawLogListZipFailedTooBig) { logListService.getLogListZip() }) {
        is Data.Valid -> readZip(logListZip.bytes)
        is Data.Invalid -> logListZip.error
    }

    private suspend fun readZip(logListZip: ByteArray): RawLogListResult {
        var logListJson: Data? = null
        var signature: Data? = null

        ZipInputStream(logListZip.inputStream()).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }.filter { !it.isDirectory }.forEach {
                when (File(it.name).name) {
                    "log_list.json" -> {
                        logListJson = wrap(RawLogListZipFailedJsonTooBig) {
                            LimitedSizeInputStream(zipInputStream, LOG_LIST_JSON_MAX_SIZE).readBytes()
                        }
                    }
                    "log_list.sig" -> {
                        signature = wrap(RawLogListZipFailedSigTooBig) {
                            LimitedSizeInputStream(zipInputStream, LOG_LIST_SIG_MAX_SIZE).readBytes()
                        }
                    }
                }

                zipInputStream.closeEntry()
            }
        }

        return when {
            logListJson == null -> RawLogListZipFailedJsonMissing
            signature == null -> RawLogListZipFailedSigMissing
            logListJson is Data.Invalid -> (logListJson as Data.Invalid).error
            signature is Data.Invalid -> (signature as Data.Invalid).error

            else -> RawLogListResult.Success((logListJson as Data.Valid).bytes, (signature as Data.Valid).bytes)
        }
    }

    override suspend fun isValid(value: RawLogListResult?) = value is RawLogListResult.Success

    override suspend fun set(value: RawLogListResult) = Unit

    private suspend fun wrap(tooBig: RawLogListResult, lambda: suspend () -> ByteArray): Data = try {
        Data.Valid(lambda())
    } catch (expected: Exception) {
        if (expected.isTooBigException()) {
            Data.Invalid(tooBig)
        } else {
            Data.Invalid(RawLogListZipFailedLoadingWithException(expected))
        }
    }

    private sealed interface Data {
        class Valid(val bytes: ByteArray) : Data
        class Invalid(val error: RawLogListResult) : Data
    }

    companion object {
        // Constants also in LogListService

        // 1 MB
        private const val LOG_LIST_JSON_MAX_SIZE = 1048576L

        // 512 bytes
        private const val LOG_LIST_SIG_MAX_SIZE = 512L
    }
}
