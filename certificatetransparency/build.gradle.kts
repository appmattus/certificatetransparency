import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    id("com.android.lint")
    alias(libs.plugins.gradleMavenPublishPlugin)
    alias(libs.plugins.dokkaPlugin)
    alias(libs.plugins.kotlin.pluginSerialization)
}

apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okio)

    testImplementation(libs.bouncycastle.bcpkix)
    testImplementation(libs.bouncycastle.bcprov)
    testImplementation(libs.bouncycastle.bctls)
    // Adding bcutil directly as it's used through bcprov-jdk15to18 but not directly added
    testImplementation(libs.bouncycastle.bcutil)

    testImplementation(libs.classgraph)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okhttp.tls)
    testImplementation(libs.retrofit.core)
    testImplementation(libs.retrofit.mock)
}

tasks.withType(KotlinCompile::class.java).all {
    compilerOptions {
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xstring-concat=inline"
        )
    }
}

lint {
    abortOnError = true
    warningsAsErrors = true
}

tasks.named("check") {
    finalizedBy(rootProject.tasks.named("detekt"))
}
tasks.getByName("check").dependsOn(rootProject.tasks.getByName("markdownlint"))

tasks.register<UpdateLogListTask>("updateLogList")
