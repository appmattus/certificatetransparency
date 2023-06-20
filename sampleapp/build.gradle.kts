@file:Suppress("MagicNumber")

import com.android.build.api.dsl.ApplicationBuildType

plugins {
    id("com.android.application")
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.appmattus.certificatetransparency.sampleapp"

    compileSdk = 33
    defaultConfig {
        applicationId = "com.appmattus.certificatetransparency.sampleapp"
        minSdk = 21
        targetSdk = 33
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
        kotlinCompilerExtensionVersion = libs.versions.androidX.compose.compiler.get()
    }
    packaging {
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
    implementation("com.pddstudio:highlightjs-android:${libs.versions.highlightJs.get()}")
    implementation("com.android.volley:volley:${libs.versions.volley.get()}")
    implementation("com.samskivert:jmustache:1.15")

    implementation("androidx.compose.ui:ui:${libs.versions.androidX.compose.ui.get()}")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:${libs.versions.androidX.compose.ui.get()}")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:${libs.versions.androidX.compose.foundation.get()}")
    // Material Design
    implementation("androidx.compose.material:material:${libs.versions.androidX.compose.material.get()}")
    // Integration with activities
    implementation("androidx.activity:activity-compose:${libs.versions.androidX.activityCompose.get()}")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidX.lifecycleViewmodelCompose.get()}")
    // Integration with observables
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.androidX.lifecycle.get()}")
    // Navigation
    implementation("androidx.navigation:navigation-compose:${libs.versions.androidX.navigationCompose.get()}")
    // MVVM+
    implementation("org.orbit-mvi:orbit-compose:${libs.versions.orbit.get()}")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${libs.versions.desugar.get()}")
}
