import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
    }
}

plugins {
    kotlin("jvm") version Versions.kotlin apply false
    id("org.owasp.dependencycheck") version Versions.owaspDependencyCheckPlugin
    id("com.appmattus.markdown") version Versions.markdownlintGradlePlugin
    id("com.vanniktech.maven.publish") version Versions.gradleMavenPublishPlugin apply false
    id("org.jetbrains.dokka") version Versions.dokkaPlugin
    id("io.gitlab.arturbosch.detekt") version Versions.detektGradlePlugin
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // For Groupie in Sample app
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    version = System.getenv("GITHUB_REF")?.substring(10) ?: System.getProperty("GITHUB_REF")?.substring(10) ?: "0.4.1"

    plugins.withType<DokkaPlugin> {
        tasks.withType<DokkaTask>().configureEach {
            dokkaSourceSets {
                configureEach {
                    if (name.startsWith("ios")) {
                        displayName.set("ios")
                    }

                    sourceLink {
                        localDirectory.set(rootDir)
                        remoteUrl.set(java.net.URL("https://github.com/appmattus/certificatetransparency/blob/main"))
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }
    }

    if (project.name !in listOf("sampleapp")) {
        tasks.withType<KotlinCompile>().configureEach {
            if (!name.contains("test", ignoreCase = true)) {
                kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

apply(from = "$rootDir/gradle/scripts/dependencyUpdates.gradle.kts")

markdownlint {
    rules {
        +LineLengthRule(codeBlocks = false)
        +ProperNamesRule { excludes = listOf(".*/NOTICE.md") }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detektGradlePlugin}")
}

tasks.withType<Detekt> {
    jvmTarget = "1.8"
}

detekt {
    input = files(subprojects.map { File(it.projectDir, "src") })

    buildUponDefaultConfig = true

    autoCorrect = true

    config = files("gradle/scripts/detekt-config.yml")
}

tasks.maybeCreate("check").dependsOn(tasks.named("detekt"))
