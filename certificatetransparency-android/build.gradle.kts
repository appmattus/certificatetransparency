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
    compileSdk = 31

    defaultConfig {
        minSdk = 19
        targetSdk = 31

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-proguard-rules.pro")
        
        // Required when setting minSdkVersion to 20 or lower (from guide of setting up API desugaring)
        multiDexEnabled = true
    }

    compileOptions {
        // Flag to enable support for the new language APIs by D8 API desugaring
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        allWarningsAsErrors = true
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    sourceSets {
        getByName<AndroidSourceSet>("main").java.srcDirs("src/main/kotlin")
        getByName<AndroidSourceSet>("test").java.srcDirs("src/test/kotlin")
        getByName<AndroidSourceSet>("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    api(project(":certificatetransparency"))
    
    //https://github.com/google/desugar_jdk_libs/blob/master/CHANGELOG.md#version-123-2023-03-29
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.3")

    testImplementation("junit:junit:${libs.versions.junit4.get()}")
    testImplementation("org.mockito:mockito-core:${libs.versions.mockito.get()}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${libs.versions.mockitoKotlin.get()}")

    testImplementation("androidx.test:core:${libs.versions.androidX.testCore.get()}")
    testImplementation("androidx.test:runner:${libs.versions.androidX.testRunner.get()}")
    testImplementation("androidx.test.ext:junit:${libs.versions.androidX.testExtJunit.get()}")
    testImplementation("org.robolectric:robolectric:${libs.versions.robolectric.get()}")
}
