import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
    alias(libs.plugins.owaspDependencyCheckPlugin)
    id("com.android.lint")
    alias(libs.plugins.gradleMavenPublishPlugin)
    alias(libs.plugins.dokkaPlugin)
    kotlin("plugin.serialization") version Versions.kotlin
}

apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinX.serialization.get()}")
    testImplementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.get()}")
    testImplementation("com.squareup.retrofit2:retrofit-mock:${libs.versions.retrofit.get()}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${libs.versions.okhttp.get()}")

    testImplementation("org.bouncycastle:bcpkix-jdk15to18:${libs.versions.bouncyCastle.get()}")
    testImplementation("org.bouncycastle:bcprov-jdk15to18:${libs.versions.bouncyCastle.get()}")
    testImplementation("org.bouncycastle:bctls-jdk15to18:${libs.versions.bouncyCastle.get()}")
    // Adding bcutil directly as it's used through bcprov-jdk15to18 but not directly added
    testImplementation("org.bouncycastle:bcutil-jdk15to18:${libs.versions.bouncyCastle.get()}")

    testImplementation("junit:junit:${libs.versions.junit4.get()}")
    testImplementation("org.mockito:mockito-core:${libs.versions.mockito.get()}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${libs.versions.mockitoKotlin.get()}")

    testImplementation("nl.jqno.equalsverifier:equalsverifier:${libs.versions.equalsVerifier.get()}")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencyCheck {
    failBuildOnCVSS = 0f

    suppressionFile = file("cve-suppressions.xml").toString()

    analyzers.assemblyEnabled = false

    skipConfigurations = listOf("lintClassPath", "jacocoAgent", "jacocoAnt", "kotlinCompilerClasspath", "kotlinCompilerPluginClasspath")
}

lint {
    abortOnError = true
    warningsAsErrors = true
}

tasks.getByName("check").dependsOn(tasks.dependencyCheckAnalyze)
tasks.named("check") {
    finalizedBy(rootProject.tasks.named("detekt"))
}
tasks.getByName("check").dependsOn(rootProject.tasks.getByName("markdownlint"))
