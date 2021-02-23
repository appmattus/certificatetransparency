@file:Suppress("MagicNumber")

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    api(project(":certificatetransparency"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.5.13")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

    testImplementation("androidx.test:core:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("org.robolectric:robolectric:4.4")
}
