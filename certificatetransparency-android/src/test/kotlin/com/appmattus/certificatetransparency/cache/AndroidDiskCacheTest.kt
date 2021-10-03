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

package com.appmattus.certificatetransparency.cache

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AndroidDiskCacheTest {

    @Test
    fun cachedLogListRetrievedWithinExpiryDate() = runBlocking {
        val result = RawLogListResult.Success(
            "bogo".toByteArray(),
            ByteArray(10) {
                it.toByte()
            }
        )

        // given a disk cache with a non-expiring cache policy
        val diskCache = AndroidDiskCache(
            ApplicationProvider.getApplicationContext(),
            givenDiskCachePolicy(false)
        )

        // when I write the result
        diskCache.set(result)

        // and I read the result back
        val actual = diskCache.get()

        // then the retrieved result matches the original
        assertEquals(result, actual)
    }

    @Test
    fun cachedLogListNotRetrievedOverExpiryDate() = runBlocking {
        val result = RawLogListResult.Success(
            "bogo".toByteArray(),
            ByteArray(10) {
                it.toByte()
            }
        )

        // given a disk cache with an always-expired cache policy
        val diskCache = AndroidDiskCache(
            ApplicationProvider.getApplicationContext(),
            givenDiskCachePolicy(true)
        )

        // when I write the result
        diskCache.set(result)

        // and I read the result back
        val actual = diskCache.get()

        // then the retrieved result is null
        assertEquals(null, actual)
    }

    private fun givenDiskCachePolicy(expiring: Boolean) = object : DiskCachePolicy {
        override fun isExpired(lastWriteDate: Date, currentDate: Date): Boolean {
            return expiring
        }
    }
}
