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
 * Modified 2021 by Appmattus Limited
 */

package com.appmattus.certificatetransparency.internal.utils

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.pow

private val GMT = TimeZone.getTimeZone("GMT")

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
internal fun String.toRfc3339Long(): Long {
    val results = Rfc3339Pattern.matchEntire(this) ?: throw NumberFormatException("Invalid RFC3339 date/time format: $this")

    val year = results.groupValues[1].toInt() // yyyy
    val month = results.groupValues[2].toInt() - 1 // MM
    val day = results.groupValues[3].toInt() // dd
    val isTimeGiven = results.groupValues[4].isNotEmpty() // 'T'HH:mm:ss.milliseconds
    val tzShiftRegexGroup = results.groupValues[9] // 'Z', or time zone shift HH:mm following '+'/'-'
    val isTzShiftGiven = tzShiftRegexGroup.isNotEmpty()
    var hourOfDay = 0
    var minute = 0
    var second = 0
    var milliseconds = 0

    if (isTzShiftGiven && !isTimeGiven) {
        throw NumberFormatException("Invalid RFC33339 date/time format, cannot specify time zone shift without specifying time: $this")
    }

    if (isTimeGiven) {
        hourOfDay = results.groupValues[5].toInt() // HH
        minute = results.groupValues[6].toInt() // mm
        second = results.groupValues[7].toInt() // ss
        if (results.groupValues[8].isNotEmpty()) { // contains .milliseconds?
            milliseconds = results.groupValues[8].substring(1).toInt() // milliseconds
            // The number of digits after the dot may not be 3. Need to renormalize.
            val fractionDigits = results.groupValues[8].substring(1).length - 3
            milliseconds = (milliseconds.toDouble() / 10.0.pow(fractionDigits.toDouble())).toInt()
        }
    }
    val dateTime = GregorianCalendar(GMT)
    dateTime.set(year, month, day, hourOfDay, minute, second)
    dateTime.set(Calendar.MILLISECOND, milliseconds)
    var value = dateTime.timeInMillis

    if (isTimeGiven && isTzShiftGiven) {
        @Suppress("EXPERIMENTAL_API_USAGE_ERROR")
        if (tzShiftRegexGroup[0].uppercaseChar() != 'Z') {
            var tzShift = (
                results.groupValues[11].toInt() * 60 + // time zone shift HH
                    results.groupValues[12].toInt()
                ) // time zone shift mm
            if (results.groupValues[10][0] == '-') { // time zone shift + or -
                tzShift = -tzShift
            }
            value -= tzShift * 60000L // e.g. if 1 hour ahead of UTC, subtract an hour to get UTC time
        }
    }

    return value
}
