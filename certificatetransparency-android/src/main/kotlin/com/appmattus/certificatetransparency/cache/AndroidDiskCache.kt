/*
 * Copyright 2023-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.cache

import android.content.Context
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException

/**
 * A default log list and signature cache implementation for Android.
 *
 * The private storage directory of the application is used to store the log list and its signature.
 * Expiry is managed externally using the timestamp of the log-list.json file.
 */
public class AndroidDiskCache(context: Context) : DiskCache {
    private val cacheDirPath = "${context.cacheDir.path}/certificate-transparency-android"

    override suspend fun get(): RawLogListResult? {
        mutex.withLock {
            return try {
                val jsonFile = File(cacheDirPath, LOG_LIST_FILE)
                if (jsonFile.length() > LOG_LIST_JSON_MAX_SIZE) {
                    return RawLogListCacheFailedJsonTooBig
                }

                val sigFile = File(cacheDirPath, SIG_FILE)
                if (sigFile.length() > LOG_LIST_SIG_MAX_SIZE) {
                    return RawLogListCacheFailedSigTooBig
                }

                val logList = jsonFile.readBytes()
                val signature = sigFile.readBytes()

                RawLogListResult.Success(logList, signature)
            } catch (ignored: IOException) {
                null
            }
        }
    }

    override suspend fun set(value: RawLogListResult) {
        mutex.withLock {
            if (value is RawLogListResult.Success) {
                try {
                    File(cacheDirPath).mkdirs()

                    File(cacheDirPath, LOG_LIST_FILE).writeBytes(value.logList)
                    File(cacheDirPath, SIG_FILE).writeBytes(value.signature)
                } catch (ignored: IOException) {
                    // non fatal
                }
            }
        }
    }

    public companion object {
        private const val LOG_LIST_FILE = "loglist.json"
        private const val SIG_FILE = "loglist.sig"

        /**
         * Ensure only one instance of AndroidDiskCache can read/write at a time
         */
        private val mutex = Mutex()

        // Constants also in LogListZipNetworkDataSource

        // 1 MB
        private const val LOG_LIST_JSON_MAX_SIZE = 1048576L

        // 512 bytes
        private const val LOG_LIST_SIG_MAX_SIZE = 512L
    }
}
