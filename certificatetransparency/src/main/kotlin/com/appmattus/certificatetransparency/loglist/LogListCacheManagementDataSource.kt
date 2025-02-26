/*
 * Copyright 2023-2024 Appmattus Limited
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
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.loglist.InMemoryCache
import com.appmattus.certificatetransparency.internal.loglist.LogListZipNetworkDataSource
import com.appmattus.certificatetransparency.internal.loglist.ResourcesCache
import com.appmattus.certificatetransparency.internal.loglist.parser.RawLogListToLogListResultTransformer
import java.security.PublicKey

@Suppress("LongParameterList")
internal class LogListCacheManagementDataSource constructor(
    private val inMemoryCache: InMemoryCache,
    private val diskCache: DiskCache?,
    private val resourcesCache: ResourcesCache,
    private val networkCache: LogListZipNetworkDataSource,
    private val publicKey: PublicKey,
    private val transformer: RawLogListToLogListResultTransformer = RawLogListToLogListResultTransformer(publicKey),
    private val now: () -> Long
) : DataSource<LogListResult> {

    @Suppress("ReturnCount")
    override suspend fun get(): LogListResult {
        // See if we have up-to-date data in memory and return that if applicable
        val memory = inMemoryCache.get()
        val memoryResult = memory?.takeIfValid(transformer)
        memoryResult?.let { logListResult ->
            // Return the memory data if it is 1 day old or less
            if (logListResult.timestamp + ONE_DAY >= now()) {
                return logListResult
            }
        }

        // No up-to-date data in memory so check the disk cache if one is available
        val disk = diskCache?.get()
        val diskResult = disk?.takeIfValid(transformer)
        diskResult?.let { logListResult ->
            // Return the disk data if it is 1 day old or less
            if (logListResult.timestamp + ONE_DAY >= now()) {
                // We have valid data on disk so set the data into the memory cache
                inMemoryCache.set(disk)
                return logListResult
            }
        }

        // No up-to-date data in memory or disk so check the resources cache and return that if applicable
        val resources = resourcesCache.get()
        val resourcesResult = resources.takeIfValid(transformer)
        resourcesResult?.let { logListResult ->
            // Return the resources data if it is 1 day old or less
            if (logListResult.timestamp + ONE_DAY >= now()) {
                // We have valid data on resources so set the data into the memory and disk cache
                inMemoryCache.set(resources)
                diskCache?.set(resources)
                return logListResult
            }
        }

        // As a fallback for network failures use the latest of the on device caches (we may not even have a disk cache)
        val fallbackResult = listOfNotNull(diskResult, memoryResult, resourcesResult).maxByOrNull { it.timestamp }

        // Either there is no data cached in memory and disk or the data we have is older than 1 day old
        // Query network for more up-to-date data
        val network = networkCache.get()
        val networkResult = transformer.transform(network)

        return if (networkResult is LogListResult.Valid) {
            if (fallbackResult != null && networkResult.timestamp < fallbackResult.timestamp) {
                // If the network response timestamp is older than our fallback data return the fallback and note network is returning stale data
                // Potentially network is compromised and a replay attack is occurring
                LogListResult.Valid.StaleNetworkUsingCachedData(
                    fallbackResult.timestamp,
                    fallbackResult.servers,
                    networkResult
                )
            } else if (networkResult.timestamp + SEVENTY_DAYS >= now()) {
                // Network data is less than 70 days old so use it and cache locally
                inMemoryCache.set(network)
                diskCache?.set(network)

                if (networkResult.timestamp + FOURTEEN_DAYS >= now()) {
                    // Network data is as expected (14 days old or less) and the timestamp is the same or newer than the fallback data
                    networkResult
                } else {
                    // If the data is older than 14 days there's potentially a network issue so note this
                    LogListResult.Valid.StaleNetworkUsingNetworkData(networkResult.timestamp, networkResult.servers)
                }
            } else {
                // Network returning data older than 70 days
                // Potentially network is compromised and a replay attack is occurring
                LogListResult.Invalid.LogListStaleNetwork(networkResult)
            }
        } else if (fallbackResult == null) {
            // Unfortunately with no fallback data and no network response we don't know if CT checks are still valid or not
            // Fail hard and disallow any network connections
            networkResult
        } else if (fallbackResult.timestamp + SEVENTY_DAYS >= now()) {
            // Fallback is 70 days old or less so use it
            fallbackResult
        } else {
            // Fallback is older than 70 days so disable CT checks - this could happen in the scenario the network no longer responds
            LogListResult.DisableChecks(fallbackResult.timestamp, networkResult)
        }
    }

    override suspend fun set(value: LogListResult) = Unit

    private fun RawLogListResult?.takeIfValid(transformer: RawLogListToLogListResultTransformer): LogListResult.Valid? {
        if (this is RawLogListResult.Success) {
            val logListResult = transformer.transform(this)
            if (logListResult is LogListResult.Valid) {
                return logListResult
            }
        }
        return null
    }

    companion object {
        private const val ONE_DAY = 86400000
        private const val FOURTEEN_DAYS = 1209600000
        private const val SEVENTY_DAYS = 6048000000
    }
}
