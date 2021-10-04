@file:Suppress("MagicNumber")

import com.android.build.api.dsl.ApplicationBuildType

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "com.appmattus.certificatetransparency.sampleapp"
        minSdk = 21
        targetSdk = 30
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
        kotlinCompilerExtensionVersion = "1.1.0-alpha05"
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
    implementation("androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}")
    implementation("com.google.android.gms:play-services-base:${Versions.Google.playServices}")
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}")
    implementation("com.afollestad.material-dialogs:core:${Versions.materialDialogs}")
    implementation("com.afollestad.material-dialogs:input:${Versions.materialDialogs}")
    implementation("com.pddstudio:highlightjs-android:${Versions.highlightJs}")
    implementation("com.android.volley:volley:${Versions.volley}")
    implementation("com.github.spullara.mustache.java:compiler:${Versions.mustache}")

    implementation("androidx.compose.ui:ui:${Versions.AndroidX.compose}")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:${Versions.AndroidX.compose}")
    // Material Design
    implementation("androidx.compose.material:material:${Versions.AndroidX.compose}")
    // Integration with activities
    implementation("androidx.activity:activity-compose:${Versions.AndroidX.activityCompose}")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.AndroidX.lifecycleViewmodelCompose}")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:${Versions.AndroidX.compose}")
    // Navigation
    implementation("androidx.navigation:navigation-compose:${Versions.AndroidX.navigationCompose}")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.AndroidX.compose}")


    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugar}")
}
