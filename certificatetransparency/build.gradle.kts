import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
    id("org.owasp.dependencycheck")
    id("com.android.lint")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.bouncycastle:bcpkix-jdk15to18:1.66")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.66")
    implementation("org.bouncycastle:bctls-jdk15to18:1.66")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    testImplementation("com.squareup.retrofit2:retrofit-mock:2.9.0")

    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.5.13")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.4.3")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
    }
}

kotlin {
    explicitApi()
}

dependencyCheck {
    failBuildOnCVSS = 0f

    suppressionFile = file("cve-suppressions.xml").toString()

    analyzers.assemblyEnabled = false

    skipConfigurations = listOf("lintClassPath", "jacocoAgent", "jacocoAnt", "kotlinCompilerClasspath", "kotlinCompilerPluginClasspath")
}

lintOptions {
    isAbortOnError = true
    isWarningsAsErrors = true
}

tasks.getByName("check").dependsOn(tasks.dependencyCheckAnalyze)
tasks.named("check") {
    finalizedBy(rootProject.tasks.named("detekt"))
}
tasks.getByName("check").dependsOn(rootProject.tasks.getByName("markdownlint"))
