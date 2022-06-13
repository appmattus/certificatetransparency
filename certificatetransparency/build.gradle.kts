import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
    id("org.owasp.dependencycheck")
    id("com.android.lint")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization") version Versions.kotlin
}

apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.bouncycastle:bcpkix-jdk15to18:${Versions.bouncyCastle}")
    implementation("org.bouncycastle:bcprov-jdk15to18:${Versions.bouncyCastle}")
    implementation("org.bouncycastle:bctls-jdk15to18:${Versions.bouncyCastle}")
    // Adding bcutil directly as it's used through bcprov-jdk15to18 but not directly added
    implementation("org.bouncycastle:bcutil-jdk15to18:${Versions.bouncyCastle}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

    implementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KotlinX.serialization}")
    testImplementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    testImplementation("com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${Versions.okhttp}")

    testImplementation("junit:junit:${Versions.junit4}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")

    testImplementation("nl.jqno.equalsverifier:equalsverifier:${Versions.equalsVerifier}")
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
