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

package com.babylon.certificatetransparency.cache

import java.util.Calendar
import java.util.Date

/**
 * A default disk cache expiry policy. The log list expires after 24 hours since it was last saved.
 */
public class DefaultDiskCachePolicy : DiskCachePolicy {

    override fun isExpired(lastWriteDate: Date, currentDate: Date): Boolean {
        val expiryCalendar = Calendar.getInstance().apply {
            time = lastWriteDate
            add(Calendar.DAY_OF_MONTH, 1)
        }

        return currentDate.after(expiryCalendar.time)
    }
}
