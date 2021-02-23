import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
    }
}

plugins {
    kotlin("jvm") version Versions.kotlin apply false
    id("org.owasp.dependencycheck") version Versions.owaspDependencyCheckPlugin
    id("com.appmattus.markdown") version Versions.markdownlintGradlePlugin
    id("com.vanniktech.maven.publish") version Versions.gradleMavenPublishPlugin apply false
    id("org.jetbrains.dokka") version Versions.dokkaPlugin
}

allprojects {
    repositories {
        google()
        jcenter()
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
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

apply(from = "$rootDir/gradle/scripts/detekt.gradle.kts")
apply(from = "$rootDir/gradle/scripts/dependencyUpdates.gradle.kts")

markdownlint {
    rules {
        +LineLengthRule(codeBlocks = false)
        +ProperNamesRule { excludes = listOf(".*/NOTICE.md") }
    }
}
