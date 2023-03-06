/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Derived from https://github.com/googleapis/google-http-java-client/blob/dev/google-http-client/src/main/java/com/google/api/client/util/DateTime.java
 *
 * Modified 2018 by Babylon Partners Limited
 * Modified 2021-2023 by Appmattus Limited
 */

package com.appmattus.certificatetransparency.internal.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.math.pow
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/** Regular expression for parsing RFC3339 date/times.  */
private val Rfc3339Pattern = Regex(
    // yyyy-MM-dd
    "^(\\d{4})-(\\d{2})-(\\d{2})" +
        // 'T'HH:mm:ss.milliseconds
        "([Tt](\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?)?" +
        // 'Z' or time zone shift HH:mm following '+' or '-'
        "([Zz]|([+-])(\\d{2}):(\\d{2}))?"
)

/**
 * Parses an RFC3339 date/time value.
 *
 * Upgrade warning: in prior version 1.17, this method required milliseconds to be exactly 3 digits (if included), and did not throw an
 * exception for all types of invalid input values, but starting in version 1.18, the parsing done by this method has become more strict
 * to enforce that only valid RFC3339 strings are entered, and if not, it throws a [NumberFormatException]. Also, in accordance with the
 * RFC3339 standard, any number of milliseconds digits is now allowed.
 *
 * For the date-only case, the time zone is ignored and the hourOfDay, minute, second, and
 * millisecond parameters are set to zero.
 *
 * @receiver Date/time string in RFC3339 format
 * @throws NumberFormatException if `str` doesn't match the RFC3339 standard format; an
 * exception is thrown if `str` doesn't match `Rfc3339Pattern` or if it contains a time zone shift but no time.
 */
// Magic numbers accepted as very much linked to the pattern
@Suppress("MagicNumber")
@OptIn(ExperimentalTime::class)
internal fun String.toRfc3339Instant(): Instant {
    val results = Rfc3339Pattern.matchEntire(this) ?: throw NumberFormatException("Invalid RFC3339 date/time format: $this")

    val localDate = LocalDate(
        year = results.groupValues[1].toInt(), // yyyy
        monthNumber = results.groupValues[2].toInt(), // MM
        dayOfMonth = results.groupValues[3].toInt() // dd
    )
    val isTimeGiven = results.groupValues[4].isNotEmpty() // 'T'HH:mm:ss.milliseconds
    val tzShiftRegexGroup = results.groupValues[9] // 'Z', or time zone shift HH:mm following '+'/'-'
    val isTzShiftGiven = tzShiftRegexGroup.isNotEmpty()

    if (isTzShiftGiven && !isTimeGiven) {
        throw NumberFormatException("Invalid RFC33339 date/time format, cannot specify time zone shift without specifying time: $this")
    }

    val localTime = if (isTimeGiven) {
        LocalTime(
            hour = results.groupValues[5].toInt(), // HH
            minute = results.groupValues[6].toInt(), // mm
            second = results.groupValues[7].toInt(), // ss
            nanosecond = results.groupValues[8].ifEmpty { ".000" }.substring(1).let {
                // The number of digits after the dot may not be 3. Need to renormalize.
                val fractionDigits = it.length - 3
                (it.toDouble() / 10.0.pow(fractionDigits.toDouble())).toInt() * 1_000_000
            }
        )
    } else {
        LocalTime(0, 0, 0)
    }

    val tzShift = if (isTzShiftGiven && tzShiftRegexGroup[0].uppercaseChar() != 'Z') {
        // time zone shift HH
        (results.groupValues[11].toInt() * 60 + results.groupValues[12].toInt()) *
            // time zone shift + or -
            (if (results.groupValues[10][0] == '-') -1 else 1)
    } else {
        0
    }

    // e.g. if 1 hour ahead of UTC, subtract an hour to get UTC time
    return localDate.atTime(localTime).toInstant(TimeZone.UTC) - tzShift.minutes
}
