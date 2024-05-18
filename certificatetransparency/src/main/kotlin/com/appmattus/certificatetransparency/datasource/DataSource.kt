/*
 * Copyright 2021-2024 Appmattus Limited
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
 * Derived from https://github.com/appmattus/layercache/
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.datasource

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A standard cache which stores and retrieves data
 *
 * @param Value The data type this data source accesses
 */
public interface DataSource<Value : Any> {

    /**
     * Return the value associated with this data source or null if not present
     */
    public suspend fun get(): Value?

    /**
     * Save the [value] to this data source
     */
    public suspend fun set(value: Value)

    /**
     * If a get call is already in flight then this ensures the original request is returned
     */
    public fun reuseInflight(): DataSource<Value> {
        return object : DataSource<Value> {
            private var job: Deferred<Value?>? = null

            override suspend fun get(): Value? {
                return coroutineScope {
                    job ?: async { this@DataSource.get() }.apply {
                        job = this
                        invokeOnCompletion { job = null }

                        launch {
                            join()
                        }
                    }
                }.await()
            }

            override suspend fun set(value: Value) = this@DataSource.set(value)
        }
    }
}
