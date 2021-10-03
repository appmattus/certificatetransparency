/*
 * Copyright 2021 Appmattus Limited
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

object Versions {

    const val kotlin = "1.5.31"

    const val androidGradlePlugin = "7.0.2"
    const val detektGradlePlugin = "1.18.1"
    const val dokkaPlugin = "1.5.30"
    const val gradleMavenPublishPlugin = "0.18.0"
    const val gradleVersionsPlugin = "0.36.0"
    const val markdownlintGradlePlugin = "0.6.0"
    const val owaspDependencyCheckPlugin = "6.3.2"

    const val bouncyCastle = "1.69"
    const val coroutines = "1.5.2"
    const val groupie = "2.9.0"
    const val highlightJs = "1.5.0"
    const val materialDialogs = "3.3.0"
    const val mustache = "0.8.18" // Cannot update due to use of ThreadLocal which is not desugared
    const val okhttp = "4.9.2"
    const val retrofit = "2.9.0"
    const val gson = "2.8.8"
    const val volley = "1.2.0"

    const val equalsVerifier = "3.7.1"
    const val junit4 = "4.13.2"
    const val mockito = "3.12.4"
    const val mockitoKotlin = "3.2.0"
    const val robolectric = "4.6.1"

    const val desugar = "1.1.5"

    object AndroidX {
        const val appCompat = "1.3.1"
        const val constraintLayout = "2.1.1"
        const val fragment = "1.3.0"
        const val lifecycle = "2.3.1"
        const val navigation = "2.3.5"
        const val compose = "1.0.3"
        const val activityCompose = "1.3.1"
        const val lifecycleViewmodelCompose = "1.0.0-alpha07"
        const val navigationCompose = "2.4.0-alpha06"

        const val testCore = "1.4.0"
        const val testExtJunit = "1.1.3"
        const val testRunner = "1.4.0"
    }

    object Google {
        const val material = "1.4.0"
        const val playServices = "17.6.0"
    }
}
