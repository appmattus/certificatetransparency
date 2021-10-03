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

package com.appmattus.certificatetransparency.datasource

import com.appmattus.certificatetransparency.internal.loglist.InMemoryDataSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicInteger

internal class DataSourceReuseInflightTest {

    private val cache = spy(InMemoryDataSource<Any>())

    private lateinit var reuseInflightCache: DataSource<Any>

    @Before
    fun before() {
        reuseInflightCache = cache.reuseInflight()
    }

    // get
    @Test
    fun `single call to get returns the value`() = runBlocking {
        // given value available in first cache only
        whenever(cache.get()).then { "value" }

        // when we get the value
        val result = reuseInflightCache.get()

        // then we return the value
        verify(cache).get()
        assertEquals("value", result)
    }

    @Test
    fun `execute get only once whilst a call is in flight`() = runBlocking {
        val count = AtomicInteger(0)

        // given value available in first cache only
        whenever(cache.get()).then {
            runBlocking(coroutineContext) {
                delay(100)
            }

            count.getAndIncrement()
        }

        // when we get the same key 5 times
        val jobs = arrayListOf<Deferred<Any?>>()
        repeat(5) {
            jobs.add(async { reuseInflightCache.get() })
        }
        jobs.forEach { it.await() }

        // then get is only called once
        assertEquals(1, count.get())
    }

    @Test
    fun `execute get twice if a call is made once the first one has finished`() = runBlocking {
        val count = AtomicInteger(0)

        // given value available in first cache only
        whenever(cache.get()).then {
            runBlocking(coroutineContext) {
                delay(100)
            }

            count.getAndIncrement()
        }

        reuseInflightCache.get()

        // we yield here as the map that stores the reuse may not have been cleared yet
        delay(100)

        // when we get the same key 5 times
        val jobs = arrayListOf<Deferred<Any?>>()
        repeat(5) {
            jobs.add(async { reuseInflightCache.get() })
        }
        jobs.forEach { it.await() }

        // then get is only called once
        assertEquals(2, count.get())
    }

    @Test(expected = TestException::class)
    fun `propogate exception on get`() = runBlocking {
        // given value available in first cache only
        whenever(cache.get()).then { throw TestException() }

        // when we get the value
        reuseInflightCache.get()

        // then we throw an exception
        Unit
    }

    // set
    @Test
    fun `call set from cache`() = runBlocking {
        // given value available in first cache only
        whenever(cache.set("value")).then { "value" }

        // when we get the value
        reuseInflightCache.set("value")

        // then we return the value
        verify(cache).set("value")
    }

    @Test(expected = TestException::class)
    fun `propagate exception on set`() = runBlocking {
        // given value available in first cache only
        whenever(cache.set("value")).then { throw TestException() }

        // when we get the value
        reuseInflightCache.set("value")

        // then we throw an exception
    }

    class TestException : Exception()
}
