@file:Suppress("MagicNumber")

import com.android.build.api.dsl.AndroidSourceSet

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 19
        targetSdk = 31

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName<AndroidSourceSet>("main").java.srcDirs("src/main/kotlin")
        getByName<AndroidSourceSet>("test").java.srcDirs("src/test/kotlin")
        getByName<AndroidSourceSet>("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

    api(project(":certificatetransparency"))

    testImplementation("junit:junit:${Versions.junit4}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")

    testImplementation("androidx.test:core:${Versions.AndroidX.testCore}")
    testImplementation("androidx.test:runner:${Versions.AndroidX.testRunner}")
    testImplementation("androidx.test.ext:junit:${Versions.AndroidX.testExtJunit}")
    testImplementation("org.robolectric:robolectric:${Versions.robolectric}")
}
