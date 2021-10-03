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

package com.babylon.certificatetransparency.cache

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.babylon.certificatetransparency.loglist.RawLogListResult
import java.io.File
import java.io.IOException
import java.util.Date

/**
 * A default log list and signature cache implementation for Android.
 *
 * The private storage directory of the application is used to store the log list and its signature.
 * The last write date is stored alongside in shared preferences in order to track cache expiry.
 */
public class AndroidDiskCache @JvmOverloads constructor(
    context: Context,
    private val diskCachePolicy: DiskCachePolicy = DefaultDiskCachePolicy()
) : DiskCache {
    private val cacheDirPath = "${context.cacheDir.path}/certificate-transparency-android"
    private val prefs = context.applicationContext.getSharedPreferences("certificate-transparency", MODE_PRIVATE)

    override suspend fun get(): RawLogListResult? {
        return try {

            val jsonFile = File(cacheDirPath, LOG_LIST_FILE)
            val sigFile = File(cacheDirPath, SIG_FILE)
            val logList = jsonFile.readBytes()
            val signature = sigFile.readBytes()

            val result = RawLogListResult.Success(logList, signature)

            if (isValid(result)) {
                result
            } else {
                prefs.edit().clear().apply()

                jsonFile.delete()
                sigFile.delete()

                null
            }
        } catch (ignored: IOException) {
            null
        }
    }

    override suspend fun set(value: RawLogListResult) {
        if (value is RawLogListResult.Success) {
            try {
                File(cacheDirPath).mkdirs()

                File(cacheDirPath, LOG_LIST_FILE).writeBytes(value.logList)
                File(cacheDirPath, SIG_FILE).writeBytes(value.signature)

                prefs.edit()
                    .putLong(PREF_KEY_LAST_WRITE, System.currentTimeMillis())
                    .apply()
            } catch (ignored: IOException) {
                // non fatal
            }
        }
    }

    override suspend fun isValid(value: RawLogListResult?): Boolean {
        return value is RawLogListResult.Success &&
                !diskCachePolicy.isExpired(
                    lastWriteDate = Date(prefs.getLong(PREF_KEY_LAST_WRITE, System.currentTimeMillis())),
                    currentDate = Date()
                )
    }

    public companion object {
        private const val LOG_LIST_FILE = "loglist.json"
        private const val SIG_FILE = "loglist.sig"
        private const val PREF_KEY_LAST_WRITE = "last_write"
    }
}
