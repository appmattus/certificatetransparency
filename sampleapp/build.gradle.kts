@file:Suppress("MagicNumber")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.babylon.certificatetransparency.sampleapp"
        minSdkVersion(19)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/atomicfu.kotlin_module")
    }
    buildFeatures {
        viewBinding = true
    }
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation(project(":certificatetransparency-android"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("com.google.android.material:material:${Versions.Google.material}")
    implementation("com.google.android.gms:play-services-base:17.4.0")
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0")
    implementation("android.arch.navigation:navigation-ui-ktx:1.0.0")
    implementation("com.xwray:groupie:${Versions.groupie}")
    implementation("com.xwray:groupie-viewbinding:${Versions.groupie}")
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation("com.pddstudio:highlightjs-android:1.5.0")
    implementation("com.android.volley:volley:1.1.1")
    implementation("com.github.spullara.mustache.java:compiler:0.8.18")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugar}")
}
