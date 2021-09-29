/*
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
 */

package com.babylon.certificatetransparency.internal.loglist

import com.babylon.certificatetransparency.datasource.DataSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@DelicateCoroutinesApi
internal open class InMemoryDataSource<Value : Any> : DataSource<Value> {
    private var cachedValue: Value? = null

    override suspend fun get(): Value? = cachedValue

    override suspend fun set(value: Value) {
        cachedValue = value
    }

    override val coroutineContext = GlobalScope.coroutineContext
}
