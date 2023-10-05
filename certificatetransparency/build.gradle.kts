import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    alias(libs.plugins.owaspDependencyCheckPlugin)
    id("com.android.lint")
    alias(libs.plugins.gradleMavenPublishPlugin)
    alias(libs.plugins.dokkaPlugin)
    alias(libs.plugins.kotlin.pluginSerialization)
}

apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("com.squareup.okio:okio:${libs.versions.okio.get()}")
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
    testImplementation("io.github.classgraph:classgraph:${libs.versions.classgraph.get()}")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencyCheck {
    failBuildOnCVSS = 0f

    suppressionFile = file("cve-suppressions.xml").toString()

    analyzers.assemblyEnabled = false
    analyzers.ossIndex.enabled = false

    skipConfigurations = listOf(
        "lintClassPath", "jacocoAgent", "jacocoAnt", "kotlinCompilerClasspath", "kotlinCompilerPluginClasspath",
        "dokkaJavadocPlugin", "dokkaGfmPartialPlugin", "dokkaHtmlPartialPlugin", "dokkaJekyllPartialPlugin", "dokkaJavadocPartialPlugin",
        "dokkaHtmlPlugin", "dokkaGfmPlugin", "dokkaJekyllPlugin", "dokkaGfmRuntime", "dokkaGfmPartialRuntime", "dokkaHtmlRuntime",
        "dokkaJekyllRuntime", "dokkaHtmlPartialRuntime", "dokkaJavadocRuntime", "dokkaJekyllPartialRuntime", "dokkaJavadocPartialRuntime"
    )
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

tasks.register<UpdateLogListTask>("updateLogList")
