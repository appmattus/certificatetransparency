/*
 * Copyright 2023 Appmattus Limited
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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appmattus.certificatetransparency.loglist.RawLogListResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class AndroidDiskCacheTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val ctCacheDir = File(context.cacheDir, "certificate-transparency-android")
    private val jsonFile = File(ctCacheDir, "loglist.json")
    private val sigFile = File(ctCacheDir, "loglist.sig")

    private val diskCache = AndroidDiskCache(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        context.cacheDir.deleteRecursively()
    }

    @After
    fun tearDown() {
        context.cacheDir.deleteRecursively()
    }

    @Test
    fun writesCachedData() {
        runBlocking {
            // Given a disk cache with no existing data

            // When we set data in the cache
            val result = RawLogListResult.Success(logList = Random.nextBytes(10), signature = Random.nextBytes(10))
            diskCache.set(result)

            // Then the data written to disk matches
            assertTrue(result.logList.contentEquals(jsonFile.readBytes()))
            assertTrue(result.signature.contentEquals(sigFile.readBytes()))
        }
    }

    @Test
    fun overridesCachedData() {
        runBlocking {
            // Given pre-cached data
            ctCacheDir.mkdirs()
            jsonFile.apply { createNewFile() }.writeBytes(Random.nextBytes(10))
            sigFile.apply { createNewFile() }.writeBytes(Random.nextBytes(10))

            // When we set data in the cache
            val result = RawLogListResult.Success(logList = Random.nextBytes(10), signature = Random.nextBytes(10))
            diskCache.set(result)

            // Then the data written to disk overrides the existing data
            assertTrue(result.logList.contentEquals(jsonFile.readBytes()))
            assertTrue(result.signature.contentEquals(sigFile.readBytes()))
        }
    }

    @Test
    fun readsCachedData() {
        runBlocking {
            // Given pre-cached data
            ctCacheDir.mkdirs()
            val jsonData = Random.nextBytes(10)
            val sigData = Random.nextBytes(10)
            jsonFile.apply { createNewFile() }.writeBytes(jsonData)
            sigFile.apply { createNewFile() }.writeBytes(sigData)

            // when we read data
            val result = diskCache.get()

            // then the written result matches the original
            assertIsA<RawLogListResult.Success>(result)
            assertTrue(jsonData.contentEquals(result.logList))
            assertTrue(sigData.contentEquals(result.signature))
        }
    }

    @Test
    fun readsNullOnEmptyData() {
        runBlocking {
            // Given no pre-cached data

            // when we read data
            val result = diskCache.get()

            // then the written result matches the original
            assertNull(result)
        }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun <reified T> assertIsA(result: Any?) {
        contract {
            returns() implies (result is T)
        }
        assertTrue("Expected ${T::class.java.name} but actual ${if (result != null) result::class.java.name else "null"}", result is T)
    }
}
