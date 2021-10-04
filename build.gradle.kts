import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
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
    }
}

subprojects {
    version = System.getenv("GITHUB_REF")?.substring(10) ?: System.getProperty("GITHUB_REF")?.substring(10) ?: "unknown"

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

    plugins.withType<KotlinPluginWrapper> {
        configure<KotlinProjectExtension> {
            // for strict mode
            explicitApi()
        }
    }

    plugins.withId("com.android.library") {
        plugins.withType<KotlinAndroidPluginWrapper> {
            configure<KotlinProjectExtension> {
                // for strict mode
                explicitApi()
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
