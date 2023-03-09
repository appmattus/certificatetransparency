@file:Suppress("MagicNumber")

import com.android.build.api.dsl.ApplicationBuildType

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.appmattus.certificatetransparency.sampleapp"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
    buildTypes {
        getByName<ApplicationBuildType>("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-beta01"
    }
    packagingOptions {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/atomicfu.kotlin_module")
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":certificatetransparency-android"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("androidx.appcompat:appcompat:${libs.versions.androidX.appCompat.get()}")
    implementation("com.google.android.material:material:${libs.versions.google.material.get()}")
    implementation("com.google.android.gms:play-services-base:${libs.versions.google.playServices.get()}")
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.get()}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${libs.versions.androidX.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${libs.versions.androidX.lifecycle.get()}")
    implementation("com.pddstudio:highlightjs-android:${libs.versions.highlightJs.get()}")
    implementation("com.android.volley:volley:${libs.versions.volley.get()}")
    implementation("com.samskivert:jmustache:1.15")

    implementation("androidx.compose.ui:ui:${libs.versions.androidX.compose.get()}")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:${libs.versions.androidX.compose.get()}")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:${libs.versions.androidX.compose.get()}")
    // Material Design
    implementation("androidx.compose.material:material:${libs.versions.androidX.compose.get()}")
    // Integration with activities
    implementation("androidx.activity:activity-compose:${libs.versions.androidX.activityCompose.get()}")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidX.lifecycleViewmodelCompose.get()}")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:${libs.versions.androidX.compose.get()}")
    // Navigation
    implementation("androidx.navigation:navigation-compose:${libs.versions.androidX.navigationCompose.get()}")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${libs.versions.desugar.get()}")
}
