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

package com.appmattus.certificatetransparency

import android.util.Log

/**
 * Basic logger which outputs the host name and certificate transparency results.
 *
 * **NOTE:** This data could be considered sensitive data. Please ensure you review your usage.
 */
public class BasicAndroidCTLogger(private val isDebugMode: Boolean) : CTLogger {
    override fun log(host: String, result: VerificationResult) {
        if (isDebugMode) {
            // Suppressing MobSF warning as note added to documentation
            Log.i("CertificateTransparency", "$host $result") // mobsf-ignore: android_kotlin_logging
        }
    }
}
