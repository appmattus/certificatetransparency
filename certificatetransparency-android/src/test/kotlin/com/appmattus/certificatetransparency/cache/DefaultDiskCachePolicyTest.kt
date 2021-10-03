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

package com.appmattus.certificatetransparency.cache

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DefaultDiskCachePolicyTest {

    @Test
    fun dateDifferenceBelow24HrsReturnsFalse() {
        val now = Date()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 23)
            add(Calendar.MINUTE, 55)
        }
        val offset = calendar.time

        // given a default disk cache policy
        val diskCachePolicy = DefaultDiskCachePolicy()

        // when I check the cache expiry
        val result = diskCachePolicy.isExpired(now, offset)

        // then the result is false
        assertFalse(result)
    }

    @Test
    fun dateDifferenceAbove24HrsReturnsTrue() {
        val now = Date()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 24)
            add(Calendar.MINUTE, 5)
        }
        val offset = calendar.time

        // given a default disk cache policy
        val diskCachePolicy = DefaultDiskCachePolicy()

        // when I check the cache expiry
        val result = diskCachePolicy.isExpired(now, offset)

        // then the result is true
        assertTrue(result)
    }
}
