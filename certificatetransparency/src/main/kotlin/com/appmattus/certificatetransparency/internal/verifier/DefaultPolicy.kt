/*
 * Copyright 2021-2023 Appmattus Limited
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
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Default [CTPolicy] which follows most of the rules of https://github.com/GoogleChrome/CertificateTransparency/blob/master/ct_policy.md
 */
internal class DefaultPolicy : CTPolicy {

    @Suppress("MagicNumber")
    override fun policyVerificationResult(
        leafCertificate: X509Certificate,
        sctResults: Map<String, SctVerificationResult>
    ): VerificationResult {
        val validScts = sctResults.values.filterIsInstance<SctVerificationResult.Valid>()

        // By default we use the 2022 policy when there are no valid SCTs
        val issuanceDate = validScts.minOfOrNull { it.sct.timestamp } ?: Instant.MAX
        val use2022policy = issuanceDate >= policyUpdateDate

        val before = leafCertificate.notBefore.toInstant().atZone(ZoneOffset.UTC)
        val after = leafCertificate.notAfter.toInstant().atZone(ZoneOffset.UTC)

        val minimumValidSignedCertificateTimestamps = if (use2022policy) {
            if (ChronoUnit.DAYS.between(before, after) > 180) 3 else 2
        } else {
            val (lifetimeInMonths, hasPartialMonth) = roundedDownMonthDifference(before, after)

            when {
                lifetimeInMonths > 39 || lifetimeInMonths == 39 && hasPartialMonth -> 5
                lifetimeInMonths > 27 || lifetimeInMonths == 27 && hasPartialMonth -> 4
                lifetimeInMonths >= 15 -> 3
                else -> 2
            }
        }

        return if (validScts.size < minimumValidSignedCertificateTimestamps) {
            VerificationResult.Failure.TooFewSctsTrusted(sctResults, minimumValidSignedCertificateTimestamps)
        } else if (validScts.distinctBy { it.operator }.size < 2) {
            VerificationResult.Failure.TooFewDistinctOperators(sctResults, 2)
        } else {
            VerificationResult.Success.Trusted(sctResults)
        }
    }

    private fun roundedDownMonthDifference(start: ZonedDateTime, expiry: ZonedDateTime): MonthDifference {
        if (expiry < start) {
            return MonthDifference(roundedMonthDifference = 0, hasPartialMonth = false)
        }

        @Suppress("MagicNumber")
        return MonthDifference(
            roundedMonthDifference = ChronoUnit.MONTHS.between(start, expiry).toInt(),
            hasPartialMonth = expiry.dayOfMonth != start.dayOfMonth
        )
    }

    private data class MonthDifference(val roundedMonthDifference: Int, val hasPartialMonth: Boolean)

    companion object {
        // 15 April 2022
        private val policyUpdateDate = Instant.ofEpochMilli(1649980800000)
    }
}
