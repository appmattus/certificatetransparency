/*
 * Copyright 2021-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.internal.loglist.model.v3

import com.appmattus.certificatetransparency.utils.TestData
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class LogListV3Test {

    @Test
    fun verifyJsonParser() {
        val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()

        val logList = Json.decodeFromString(LogListV3.serializer(), json)

        val google = logList.operators.first { it.name == "Google" }
        val cloudflare = logList.operators.first { it.name == "Cloudflare" }
        val digiCert = logList.operators.first { it.name == "DigiCert" }

        assertEquals(11, digiCert.logs.size)
        assertEquals(10, google.logs.size)
        assertEquals(2, cloudflare.logs.size)

        val argon2022 = google.logs.first { it.description == "Google 'Argon2022' log" }
        assertEquals(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEeIPc6fGmuBg6AJkv/z7NFckmHvf/OqmjchZJ6wm2qN200keRDg352dWpi7CHnSV51BpQYAj1CQY5JuRAwrrDwg==",
            argon2022.key
        )
        assertTrue(argon2022.state is State.Usable)

        val nimbusLog = cloudflare.logs.first { it.description == "Cloudflare 'Nimbus2022' Log" }
        assertEquals(86400, nimbusLog.maximumMergeDelay)
        // Suppressing MobSF warning as false positive, no logging occurs here
        assertEquals(1572549720000, nimbusLog.state?.timestamp) // mobsf-ignore: android_kotlin_logging
    }
}
