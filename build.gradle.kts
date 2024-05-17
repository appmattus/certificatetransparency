import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}")

        constraints {
            // Need to force versions for dependencyCheck to work
            add("classpath", "com.fasterxml.jackson:jackson-bom:2.17.1")
            add("classpath", "org.apache.commons:commons-lang3:3.14.0")
            add("classpath", "org.apache.commons:commons-text:1.12.0")
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.markdownlintGradlePlugin)
    alias(libs.plugins.gradleMavenPublishPlugin) apply false
    alias(libs.plugins.dokkaPlugin)
    alias(libs.plugins.detektGradlePlugin)
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
                        remoteUrl.set(URI("https://github.com/appmattus/certificatetransparency/blob/main").toURL())
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

    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            allWarningsAsErrors = true
        }
    }

    plugins.withType<KotlinBasePlugin> {
        configure<KotlinProjectExtension> {
            jvmToolchain(libs.versions.java.get().toInt())
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
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detektGradlePlugin.get()}")
}

detekt {
    input = files(subprojects.map { File(it.projectDir, "src") })

    buildUponDefaultConfig = true

    autoCorrect = true

    config = files("gradle/scripts/detekt-config.yml")
}

tasks.maybeCreate("check").dependsOn(tasks.named("detekt"))
