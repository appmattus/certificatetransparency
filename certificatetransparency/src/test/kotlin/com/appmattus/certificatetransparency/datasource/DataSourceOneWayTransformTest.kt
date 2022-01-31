/*
 * Copyright 2021-2022 Appmattus Limited
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

package com.appmattus.certificatetransparency.datasource

import com.appmattus.certificatetransparency.internal.loglist.InMemoryDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class DataSourceOneWayTransformTest {

    private val cache = spy(InMemoryDataSource<String>())

    private val function: (String) -> Int = mock()

    private lateinit var mappedValuesCache: DataSource<Int>

    @Before
    fun before() {
        mappedValuesCache = cache.oneWayTransform(function)
        reset(cache)
    }

    // get
    @Test
    fun `invokes function`() {
        runBlocking {
            // given we have a cache that returns a string
            whenever(cache.get()).then { "1" }
            whenever(function.invoke(anyString())).then { it.getArgument<String>(0).toInt() }

            // when we get the value
            mappedValuesCache.get()

            // then the main function is invoked but the inverse is not
            verify(function).invoke("1")
        }
    }

    @Test
    fun `map string value in get to int`() = runBlocking {
        // given we have a cache that returns a string
        whenever(cache.get()).then { "1" }
        whenever(function.invoke(anyString())).then { it.getArgument<String>(0).toInt() }

        // when we get the value
        val result = mappedValuesCache.get()

        // then it is converted to an integer
        assertEquals(1, result)
        assertTrue(result is Int)
    }

    @Test(expected = TestException::class)
    fun `throw exception when mapping in function`() = runBlocking {
        // given we have a string and transform throws an exception
        whenever(cache.get()).then { "1" }
        whenever(function.invoke(anyString())).then { throw TestException() }

        // when we get the value from a map with exception throwing functions
        mappedValuesCache.get()

        // then an exception is thrown
        Unit
    }

    @Test(expected = TestException::class)
    fun `throw exception when mapping in get`() = runBlocking {
        // given we throw an exception on get
        whenever(cache.get()).then { throw TestException() }

        // when we get the value from a map
        mappedValuesCache.get()

        // then an exception is thrown
        Unit
    }

    // set
    @Test
    fun `not interact with parent set`() = runBlocking {
        // when we set the value
        mappedValuesCache.set(1)

        // then the parent cache is not called
        verifyNoMoreInteractions(cache)
    }

    @Test
    fun `not interact with transform during set`() = runBlocking {
        // when we set the value
        mappedValuesCache.set(1)

        // then the parent cache is not called
        verifyNoInteractions(function)
    }

    class TestException : Exception()
}
