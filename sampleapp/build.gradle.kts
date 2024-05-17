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
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
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

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodelcompose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.google.material)
    implementation(libs.google.playservices)
    implementation(libs.highlightjs)
    implementation(libs.jmustache)
    implementation(libs.orbit.compose)
    implementation(libs.retrofit.core)
    implementation(libs.volley)

    coreLibraryDesugaring(libs.desugar)
}
