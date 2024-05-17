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

    implementation(libs.kotlinx.coroutines)
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
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okhttp.tls)
    testImplementation(libs.retrofit.core)
    testImplementation(libs.retrofit.mock)
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencyCheck {
    failBuildOnCVSS = 0f

    suppressionFile = file("cve-suppressions.xml").toString()

    analyzers.dartEnabled = false
    analyzers.pyDistributionEnabled = false
    analyzers.pyPackageEnabled = false
    analyzers.rubygemsEnabled = false
    analyzers.nuspecEnabled = false
    analyzers.nugetconfEnabled = false
    analyzers.assemblyEnabled = false
    analyzers.msbuildEnabled = false
    analyzers.cmakeEnabled = false
    analyzers.autoconfEnabled = false
    analyzers.composerEnabled = false
    analyzers.cpanEnabled = false
    analyzers.nodeEnabled = false
    analyzers.cocoapodsEnabled = false
    // analyzers.carthageEnabled = false
    analyzers.swiftEnabled = false
    analyzers.swiftPackageResolvedEnabled = false
    analyzers.bundleAuditEnabled = false
    analyzers.golangDepEnabled = false
    analyzers.golangModEnabled = false

    analyzers.nodeAudit.enabled = false
    analyzers.retirejs.enabled = false
    analyzers.ossIndex.enabled = false

    nvd.apiKey = System.getenv("NVD_API_KEY") ?: System.getProperty("NVD_API_KEY") ?: ""

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
