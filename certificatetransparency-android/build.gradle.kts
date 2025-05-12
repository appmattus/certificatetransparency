@file:Suppress("MagicNumber")

import com.android.build.api.dsl.AndroidSourceSet

plugins {
    id("com.android.library")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.gradleMavenPublishPlugin)
    alias(libs.plugins.dokkaPlugin)
}

apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")

android {
    namespace = "com.appmattus.certificatetransparency"

    compileSdk = 36

    defaultConfig {
        minSdk = 19

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    kotlinOptions {
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xstring-concat=inline"
        )
    }

    sourceSets {
        getByName<AndroidSourceSet>("main").java.srcDirs("src/main/kotlin")
        getByName<AndroidSourceSet>("test").java.srcDirs("src/test/kotlin")
        getByName<AndroidSourceSet>("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    api(project(":certificatetransparency"))

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
}
