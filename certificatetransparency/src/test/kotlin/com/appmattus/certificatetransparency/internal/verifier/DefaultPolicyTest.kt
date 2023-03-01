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

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.internal.verifier.model.DigitallySigned
import com.appmattus.certificatetransparency.internal.verifier.model.LogId
import com.appmattus.certificatetransparency.internal.verifier.model.SignedCertificateTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID
import kotlin.random.Random

@RunWith(Parameterized::class)
internal class DefaultPolicyTest(
    @Suppress("unused") private val description: String,
    private val start: Date,
    private val end: Date,
    private val oldPolicySctsRequired: Int,
    private val newPolicySctsRequired: Int
) {

    @Test
    fun fewerSctsThanRequiredReturnsFailureOldPolicy() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and fewer SCTs than required
        val scts = buildList {
            // we ensure there is at least one valid SCT so the old policy is selected
            for (i in 0 until Random.nextInt(1, oldPolicySctsRequired)) {
                add(SctVerificationResult.Valid(oldPolicySct))
            }
            for (i in 0 until 10) {
                add(SctVerificationResult.Invalid.FailedVerification)
            }
        }.shuffled().associateBy { UUID.randomUUID().toString() }

        // when we execute the default policy
        val result = DefaultPolicy().policyVerificationResult(certificate, scts) as VerificationResult.Failure.TooFewSctsTrusted

        // then the correct number of SCTs are required
        assertEquals(oldPolicySctsRequired, result.minSctCount)
    }

    @Test
    fun fewerSctsThanRequiredReturnsFailureNewPolicy() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and fewer SCTs than required
        val scts = buildList {
            for (i in 0 until Random.nextInt(0, newPolicySctsRequired)) {
                add(SctVerificationResult.Valid(newPolicySct))
            }
            for (i in 0 until 10) {
                add(SctVerificationResult.Invalid.FailedVerification)
            }
        }.shuffled().associateBy { UUID.randomUUID().toString() }

        // when we execute the default policy
        val result = DefaultPolicy().policyVerificationResult(certificate, scts) as VerificationResult.Failure.TooFewSctsTrusted

        // then the correct number of SCTs are required
        assertEquals(newPolicySctsRequired, result.minSctCount)
    }

    @Test
    fun correctNumberOfSctsReturnsSuccessTrustedOldPolicy() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and correct number of trusted SCTs present
        val scts = buildList {
            for (i in 0 until oldPolicySctsRequired) {
                add(SctVerificationResult.Valid(oldPolicySct))
            }
            for (i in 0 until 10) {
                add(SctVerificationResult.Invalid.FailedVerification)
            }
        }.shuffled().associateBy { UUID.randomUUID().toString() }

        // when we execute the default policy
        val result = DefaultPolicy().policyVerificationResult(certificate, scts)

        // then the policy passes
        assertTrue(result is VerificationResult.Success.Trusted)
    }

    @Test
    fun correctNumberOfSctsReturnsSuccessTrustedNewPolicy() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and correct number of trusted SCTs present
        val scts = buildList {
            for (i in 0 until newPolicySctsRequired) {
                add(SctVerificationResult.Valid(newPolicySct))
            }
            for (i in 0 until 10) {
                add(SctVerificationResult.Invalid.FailedVerification)
            }
        }.shuffled().associateBy { UUID.randomUUID().toString() }

        // when we execute the default policy
        val result = DefaultPolicy().policyVerificationResult(certificate, scts)

        // then the policy passes
        assertTrue(result is VerificationResult.Success.Trusted)
    }

    companion object {

        @Suppress("LongParameterList", "SameParameterValue")
        private fun date(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int, milliseconds: Int): Date =
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, second)
                set(Calendar.MILLISECOND, milliseconds)
                timeZone = TimeZone.getTimeZone("UTC")
            }.time

        @JvmStatic
        @Parameterized.Parameters(name = "{0} ({1} -> {2})")
        fun data() = arrayOf(
            arrayOf(
                "Cert valid for -14 months (nonsensical), needs 2 SCTs",
                date(2016, 6, 6, 11, 25, 0, 0),
                date(2015, 3, 25, 11, 25, 0, 0),
                2,
                2
            ),
            arrayOf("Cert valid for 14 months, needs 2 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 6, 11, 25, 0, 0), 2, 3),
            arrayOf("Cert valid for exactly 15 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 25, 11, 25, 0, 0), 3, 3),
            arrayOf("Cert valid for over 15 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 27, 11, 25, 0, 0), 3, 3),
            arrayOf("Cert valid for exactly 27 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2017, 6, 25, 11, 25, 0, 0), 3, 3),
            arrayOf("Cert valid for over 27 months, needs 4 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2017, 6, 28, 11, 25, 0, 0), 4, 3),
            arrayOf("Cert valid for exactly 39 months, needs 4 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2018, 6, 25, 11, 25, 0, 0), 4, 3),
            arrayOf("Cert valid for over 39 months, needs 5 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2018, 6, 27, 11, 25, 0, 0), 5, 3)
        )

        private val oldPolicySct = SignedCertificateTimestamp(
            id = LogId(byteArrayOf()),
            timestamp = 0,
            extensions = byteArrayOf(),
            signature = DigitallySigned(signature = byteArrayOf())
        )

        private val newPolicySct = SignedCertificateTimestamp(
            id = LogId(byteArrayOf()),
            // 15 April 2022
            timestamp = 1649980800000,
            extensions = byteArrayOf(),
            signature = DigitallySigned(signature = byteArrayOf())
        )
    }
}
