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

package com.appmattus.certificatetransparency.internal.verifier

import com.appmattus.certificatetransparency.SctVerificationResult
import com.appmattus.certificatetransparency.VerificationResult
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
import java.util.Random

@RunWith(Parameterized::class)
internal class DefaultPolicyTest(
    @Suppress("unused") private val description: String,
    private val start: Date,
    private val end: Date,
    private val sctsRequired: Int
) {

    @Test
    fun fewerSctsThanRequiredReturnsFailure() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and fewer SCTs than required
        val scts = mutableMapOf<String, SctVerificationResult>()
        for (i in 0 until Random().nextInt(sctsRequired)) {
            scts[i.toString()] = SctVerificationResult.Valid
        }
        for (i in 0 until 10) {
            scts[(i + 100).toString()] = SctVerificationResult.Invalid.FailedVerification
        }

        // when we execute the default policy
        val result = DefaultPolicy().policyVerificationResult(certificate, scts) as VerificationResult.Failure.TooFewSctsTrusted

        // then the correct number of SCTs are required
        assertEquals(sctsRequired, result.minSctCount)
    }

    @Test
    fun correctNumberOfSctsReturnsSuccessTrusted() {
        // given a certificate with start and end date specified
        val certificate: X509Certificate = mock()
        whenever(certificate.notBefore).thenReturn(start)
        whenever(certificate.notAfter).thenReturn(end)

        // and correct number of trusted SCTs present
        val scts = mutableMapOf<String, SctVerificationResult>()
        for (i in 0 until sctsRequired) {
            scts[i.toString()] = SctVerificationResult.Valid
        }
        for (i in 0 until 10) {
            scts[(i + 100).toString()] = SctVerificationResult.Invalid.FailedVerification
        }

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
            }.time

        @JvmStatic
        @Parameterized.Parameters(name = "{0} ({1} -> {2})")
        fun data() = arrayOf(
            arrayOf("Cert valid for -14 months (nonsensical), needs 2 SCTs", date(2016, 6, 6, 11, 25, 0, 0), date(2015, 3, 25, 11, 25, 0, 0), 2),
            arrayOf("Cert valid for 14 months, needs 2 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 6, 11, 25, 0, 0), 2),
            arrayOf("Cert valid for exactly 15 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 25, 11, 25, 0, 0), 3),
            arrayOf("Cert valid for over 15 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2016, 6, 27, 11, 25, 0, 0), 3),
            arrayOf("Cert valid for exactly 27 months, needs 3 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2017, 6, 25, 11, 25, 0, 0), 3),
            arrayOf("Cert valid for over 27 months, needs 4 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2017, 6, 28, 11, 25, 0, 0), 4),
            arrayOf("Cert valid for exactly 39 months, needs 4 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2018, 6, 25, 11, 25, 0, 0), 4),
            arrayOf("Cert valid for over 39 months, needs 5 SCTs", date(2015, 3, 25, 11, 25, 0, 0), date(2018, 6, 27, 11, 25, 0, 0), 5)
        )
    }
}
