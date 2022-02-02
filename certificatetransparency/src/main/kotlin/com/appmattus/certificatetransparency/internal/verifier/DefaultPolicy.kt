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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.CTPolicy
import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.VerificationResult
import java.security.cert.X509Certificate
import java.util.Calendar

internal class DefaultPolicy : CTPolicy {

    override fun policyVerificationResult(
        leafCertificate: X509Certificate,
        sctResults: Map<String, SctVerificationResult>
    ): VerificationResult {
        val before = Calendar.getInstance().apply {
            time = leafCertificate.notBefore
        }
        val after = Calendar.getInstance().apply {
            time = leafCertificate.notAfter
        }

        val (lifetimeInMonths, hasPartialMonth) = roundedDownMonthDifference(before, after)

        @Suppress("MagicNumber")
        val minimumValidSignedCertificateTimestamps = when {
            lifetimeInMonths > 39 || lifetimeInMonths == 39 && hasPartialMonth -> 5
            lifetimeInMonths > 27 || lifetimeInMonths == 27 && hasPartialMonth -> 4
            lifetimeInMonths >= 15 -> 3
            else -> 2
        }

        return if (sctResults.count { it.value is SctVerificationResult.Valid } < minimumValidSignedCertificateTimestamps) {
            VerificationResult.Failure.TooFewSctsTrusted(sctResults, minimumValidSignedCertificateTimestamps)
        } else {
            VerificationResult.Success.Trusted(sctResults)
        }
    }

    private fun roundedDownMonthDifference(start: Calendar, expiry: Calendar): MonthDifference {
        if (expiry < start) {
            return MonthDifference(roundedMonthDifference = 0, hasPartialMonth = false)
        }

        @Suppress("MagicNumber")
        return MonthDifference(
            roundedMonthDifference = (expiry.year - start.year) * 12 + (expiry.month - start.month) -
                if (expiry.dayOfMonth < start.dayOfMonth) 1 else 0,
            hasPartialMonth = expiry.dayOfMonth != start.dayOfMonth
        )
    }

    private data class MonthDifference(val roundedMonthDifference: Int, val hasPartialMonth: Boolean)

    private val Calendar.year
        get() = get(Calendar.YEAR)

    private val Calendar.month
        get() = get(Calendar.MONTH)

    private val Calendar.dayOfMonth
        get() = get(Calendar.DAY_OF_MONTH)
}
