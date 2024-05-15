@file:Suppress("MagicNumber")

import com.android.build.api.dsl.ApplicationBuildType

plugins {
    id("com.android.application")
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.appmattus.certificatetransparency.sampleapp"

    compileSdk = 34
    defaultConfig {
        applicationId = "com.appmattus.certificatetransparency.sampleapp"
        minSdk = 21
        targetSdk = 34
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

    implementation(platform(libs.androidx.compose.bom))

    implementation(kotlin("stdlib-jdk8"))
    implementation("androidx.appcompat:appcompat:${libs.versions.androidX.appCompat.get()}")
    implementation("com.google.android.material:material:${libs.versions.google.material.get()}")
    implementation("com.google.android.gms:play-services-base:${libs.versions.google.playServices.get()}")
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.get()}")
    implementation("com.pddstudio:highlightjs-android:${libs.versions.highlightJs.get()}")
    implementation("com.android.volley:volley:${libs.versions.volley.get()}")
    implementation(libs.jmustache)

    implementation(libs.androidx.compose.ui)
    // Tooling support (Previews, etc.)
    implementation(libs.androidx.compose.ui.tooling)
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation(libs.androidx.compose.foundation)
    // Material Design
    implementation(libs.androidx.compose.material)
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
