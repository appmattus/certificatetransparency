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
    implementation("androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}")
    implementation("com.google.android.material:material:${Versions.Google.material}")
    implementation("com.google.android.gms:play-services-base:${Versions.Google.playServices}")
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.AndroidX.navigation}")
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.AndroidX.navigation}")
    implementation("com.github.lisawray.groupie:groupie:${Versions.groupie}")
    implementation("com.github.lisawray.groupie:groupie-viewbinding:${Versions.groupie}")
    implementation("com.afollestad.material-dialogs:core:${Versions.materialDialogs}")
    implementation("com.afollestad.material-dialogs:input:${Versions.materialDialogs}")
    implementation("com.pddstudio:highlightjs-android:${Versions.highlightJs}")
    implementation("com.android.volley:volley:${Versions.volley}")
    implementation("com.github.spullara.mustache.java:compiler:${Versions.mustache}")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugar}")
}
