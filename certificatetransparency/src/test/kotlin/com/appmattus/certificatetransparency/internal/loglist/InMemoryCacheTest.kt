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

package com.appmattus.certificatetransparency.internal.loglist

import com.appmattus.certificatetransparency.loglist.RawLogListResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class InMemoryCacheTest {

    @Test
    fun emptyDataSourceReturnsNull() = runBlocking {
        // given a new data source
        val dataSource = InMemoryCache()

        // when we get the current value
        val result = dataSource.get()

        // then it returns null
        assertNull(result)
    }

    @Test
    fun dataSourceReturnsSetValue() = runBlocking {
        // given a new data source populated with a value
        val dataSource = InMemoryCache()
        dataSource.set(RawLogListResult.Success(byteArrayOf(1), byteArrayOf(2)))

        // when we get the current value
        val result = dataSource.get()

        // then it returns the value
        assertEquals(RawLogListResult.Success(byteArrayOf(1), byteArrayOf(2)), result)
    }

    @Test
    fun dataSourceReturnsLatestSetValue() = runBlocking {
        // given a new data source populated with a value
        val dataSource = InMemoryCache()
        dataSource.set(RawLogListResult.Success(byteArrayOf(1), byteArrayOf(2)))

        // when we set a second value
        dataSource.set(RawLogListResult.Success(byteArrayOf(3), byteArrayOf(4)))

        // then the second value is returned when retrieved
        assertEquals(RawLogListResult.Success(byteArrayOf(3), byteArrayOf(4)), dataSource.get())
    }
}
