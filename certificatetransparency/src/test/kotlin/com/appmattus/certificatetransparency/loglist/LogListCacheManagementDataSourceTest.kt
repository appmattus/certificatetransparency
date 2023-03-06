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

package com.appmattus.certificatetransparency.loglist

import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.internal.loglist.InMemoryCache
import com.appmattus.certificatetransparency.internal.loglist.LogListZipNetworkDataSource
import com.appmattus.certificatetransparency.internal.loglist.parser.RawLogListToLogListResultTransformer
import com.appmattus.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

internal class LogListCacheManagementDataSourceTest {

    private val memoryRawResult = mock<RawLogListResult.Success>()
    private val memoryCacheMock = mock<InMemoryCache> {
        onBlocking { get() } doAnswer { memoryRawResult }
    }

    private val diskRawResult = mock<RawLogListResult.Success>()
    private val diskCacheMock = mock<DiskCache> {
        onBlocking { get() } doAnswer { diskRawResult }
    }

    private val networkRawResult = mock<RawLogListResult.Success>()
    private val networkCacheMock = mock<LogListZipNetworkDataSource> {
        onBlocking { get() } doAnswer { networkRawResult }
    }

    private val logListTransformerMock = mock<RawLogListToLogListResultTransformer> {
        on { transform(memoryRawResult) } doReturn LogListResult.Invalid.NoLogServers
        on { transform(diskRawResult) } doReturn LogListResult.Invalid.NoLogServers
        on { transform(networkRawResult) } doReturn LogListResult.Invalid.NoLogServers
    }

    private var now: Instant = defaultLogListTimestamp
    private val dataSource = LogListCacheManagementDataSource(memoryCacheMock, diskCacheMock, networkCacheMock, logListTransformerMock) { now }

    @Test
    fun `returns success when network returns data 14 days old or less and memory and disk cache empty`() {
        runBlocking {
            // Given no data in memory or disk cache and the network returns successfully
            givenNetworkResult(LogListResult.Valid.Success(defaultLogListTimestamp, emptyList()))

            // and the time now is 14 days (inclusive) old or less of the log list
            now = defaultLogListTimestamp + Duration.ofMillis(Random.nextLong(FOURTEEN_DAYS_IN_MILLISECONDS + 1))

            // When we get data
            val result = dataSource.get()

            // The the network data is returned
            assertIsA<LogListResult.Valid.Success>(result)
            // And stored in memory and disk cache
            verify(memoryCacheMock, times(1)).set(any())
            verify(diskCacheMock, times(1)).set(any())
        }
    }

    @Test
    fun `returns success stale network when network returns data between 14 and 70 days old and memory and disk cache empty`() {
        runBlocking {
            // Given no data in memory or disk cache and the network returns successfully
            givenNetworkResult(LogListResult.Valid.Success(defaultLogListTimestamp, emptyList()))
            // and the time now is between 14 days (exclusive) and 70 days (inclusive) old of the log list
            now =
                defaultLogListTimestamp + Duration.ofMillis(Random.nextLong(FOURTEEN_DAYS_IN_MILLISECONDS + 1, SEVENTY_DAYS_IN_MILLISECONDS + 1))

            // When we get data
            val result = dataSource.get()

            // The the network data is returned
            assertIsA<LogListResult.Valid.StaleNetworkUsingNetworkData>(result)
            // And stored in memory and disk cache
            verify(memoryCacheMock, times(1)).set(any())
            verify(diskCacheMock, times(1)).set(any())
        }
    }

    @Test
    fun `returns failure stale network when network returns data more than 70 days old and memory and disk cache empty`() {
        runBlocking {
            // Given no data in memory or disk cache and network returns successfully
            givenNetworkResult(LogListResult.Valid.Success(defaultLogListTimestamp, emptyList()))
            // and the time now is more than 70 days (exclusive) old of the log list
            now = defaultLogListTimestamp + Duration.ofMillis(SEVENTY_DAYS_IN_MILLISECONDS + 1)

            // When we get data
            val result = dataSource.get()

            // The the network data is returned
            assertIsA<LogListResult.Invalid.LogListStaleNetwork>(result)
            // And no data stored in memory and disk cache
            verify(memoryCacheMock, never()).set(any())
            verify(diskCacheMock, never()).set(any())
        }
    }

    @Test
    fun `returns success when memory cache returns data 1 day old or less`() {
        runBlocking {
            // Given data in memory
            givenMemoryResult(LogListResult.Valid.Success(defaultLogListTimestamp, emptyList()))
            // and the time now is 1 day (inclusive) old or less of the log list
            now = defaultLogListTimestamp + Duration.ofMillis(Random.nextLong(ONE_DAY_IN_MILLISECONDS + 1))

            // When we get data
            val result = dataSource.get()

            // The the memory data is returned
            assertIsA<LogListResult.Valid.Success>(result)
            // And disk and network cache not called
            verify(diskCacheMock, never()).get()
            verify(networkCacheMock, never()).get()
        }
    }

    @Test
    fun `returns success when memory cache empty and disk returns data 1 day old or less`() {
        runBlocking {
            // Given no data in memory cache and disk cache returns successfully
            givenDiskResult(LogListResult.Valid.Success(defaultLogListTimestamp, emptyList()))
            // and the time now is 1 day (inclusive) old or less of the log list
            now = defaultLogListTimestamp + Duration.ofMillis(Random.nextLong(ONE_DAY_IN_MILLISECONDS + 1))

            // When we get data
            val result = dataSource.get()

            // The the disk data is returned
            assertIsA<LogListResult.Valid.Success>(result)
            // And stored in memory cache
            verify(memoryCacheMock, times(1)).set(any())
            // And network is not called
            verify(networkCacheMock, never()).get()
        }
    }

    @Test
    fun `returns success when memory cache returns data older than 1 day and disk returns data 1 day old or less`() {
        runBlocking {
            // Given data in memory is older than a day
            givenMemoryResult(
                LogListResult.Valid.Success(
                    defaultLogListTimestamp - Duration.ofMillis(ONE_DAY_IN_MILLISECONDS + 1),
                    emptyList()
                )
            )
            // And data in disk is one day or less old
            givenDiskResult(
                LogListResult.Valid.Success(
                    defaultLogListTimestamp - Duration.ofMillis(Random.nextLong(ONE_DAY_IN_MILLISECONDS)),
                    emptyList()
                )
            )

            // When we get data
            val result = dataSource.get()

            // The the disk data is returned
            assertIsA<LogListResult.Valid.Success>(result)
            // And stored in memory cache
            verify(memoryCacheMock, times(1)).set(any())
            // And network is not called
            verify(networkCacheMock, never()).get()
        }
    }

    @Test
    fun `returns success stale network when memory data newer than network data`() {
        runBlocking {
            // Given data in memory older than one day
            val memoryTimestamp = defaultLogListTimestamp - Duration.ofMillis(ONE_DAY_IN_MILLISECONDS + 1)
            givenMemoryResult(LogListResult.Valid.Success(memoryTimestamp, emptyList()))
            // And network data is older than memory data
            givenNetworkResult(
                LogListResult.Valid.Success(
                    memoryTimestamp - Duration.ofMillis(Random.nextLong(1, ONE_DAY_IN_MILLISECONDS)),
                    emptyList()
                )
            )

            // When we get data
            val result = dataSource.get()

            // The the memory data is returned
            assertIsA<LogListResult.Valid.StaleNetworkUsingCachedData>(result)
            assertEquals(memoryTimestamp, result.timestamp)
        }
    }

    @Test
    fun `returns success stale network when disk data newer than network data`() {
        runBlocking {
            // Given data in disk older than one day
            val diskTimestamp = defaultLogListTimestamp - Duration.ofMillis(ONE_DAY_IN_MILLISECONDS + 1)
            givenDiskResult(
                LogListResult.Valid.Success(
                    defaultLogListTimestamp - Duration.ofMillis(ONE_DAY_IN_MILLISECONDS + 1),
                    emptyList()
                )
            )
            // And network data that is older than memory
            givenNetworkResult(
                LogListResult.Valid.Success(
                    diskTimestamp - Duration.ofMillis(Random.nextLong(1, ONE_DAY_IN_MILLISECONDS)),
                    emptyList()
                )
            )

            // When we get data
            val result = dataSource.get()

            // The the disk data is returned
            assertIsA<LogListResult.Valid.StaleNetworkUsingCachedData>(result)
            assertEquals(diskTimestamp, result.timestamp)
        }
    }

    @Test
    fun `returns failure when network fails and theres no fallback data`() {
        runBlocking {
            // Given network failure and no data in memory or disk
            givenNetworkResult(LogListResult.Invalid.LogListZipFailedLoadingWithException(IOException()))

            // When we get data
            val result = dataSource.get()

            // Then network failure is returned
            assertIsA<LogListResult.Invalid.LogListZipFailedLoadingWithException>(result)
        }
    }

    @Test
    fun `returns success when network fails and fallback data is between 1 and 70 days old`() {
        runBlocking {
            // Given network failure and no data in memory or disk
            givenNetworkResult(LogListResult.Invalid.LogListZipFailedLoadingWithException(IOException()))
            // And memory data is between 1 day and 70 days
            val memoryTimestamp =
                defaultLogListTimestamp - Duration.ofMillis(Random.nextLong(ONE_DAY_IN_MILLISECONDS + 1, SEVENTY_DAYS_IN_MILLISECONDS))
            givenMemoryResult(LogListResult.Valid.Success(memoryTimestamp, emptyList()))

            // When we get data
            val result = dataSource.get()

            // Then fallback data is returned
            assertIsA<LogListResult.Valid.Success>(result)
            assertEquals(memoryTimestamp, result.timestamp)
        }
    }

    @Test
    fun `returns disable checks when network fails and fallback data is more than 70 days old`() {
        runBlocking {
            // Given network failure and no data in memory or disk
            givenNetworkResult(LogListResult.Invalid.LogListZipFailedLoadingWithException(IOException()))
            // And memory data is older than 70 days
            val memoryTimestamp = defaultLogListTimestamp - Duration.ofMillis(SEVENTY_DAYS_IN_MILLISECONDS + 1)
            givenMemoryResult(LogListResult.Valid.Success(memoryTimestamp, emptyList()))

            // When we get data
            val result = dataSource.get()

            // Then disable checks is returned
            assertIsA<LogListResult.DisableChecks>(result)
            // And network failure returned in the result
            assertIsA<LogListResult.Invalid.LogListZipFailedLoadingWithException>(result.networkResult)
        }
    }

    private fun givenNetworkResult(result: LogListResult) {
        whenever(logListTransformerMock.transform(networkRawResult)) doReturn result
    }

    private fun givenMemoryResult(result: LogListResult) {
        whenever(logListTransformerMock.transform(memoryRawResult)) doReturn result
    }

    private fun givenDiskResult(result: LogListResult) {
        whenever(logListTransformerMock.transform(diskRawResult)) doReturn result
    }

    companion object {
        private val defaultLogListTimestamp = Instant.ofEpochMilli(1663678537000L)

        private const val ONE_DAY_IN_MILLISECONDS = 86400000L
        private const val FOURTEEN_DAYS_IN_MILLISECONDS = 1209600000L
        private const val SEVENTY_DAYS_IN_MILLISECONDS = 6048000000L
    }
}
