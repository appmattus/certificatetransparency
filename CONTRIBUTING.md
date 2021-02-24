# Contributing

When contributing to this repository, please first discuss the change you wish
to make via issue, email, or any other method with the owners of this repository
before making a change.

Please note we have a [code of conduct](code-of-conduct.md), please follow it
in all your interactions with the project.

## Pull request process

1. Ensure any install or build dependencies are removed before the end of the
   layer when doing a build.
1. Update the [README](README.md) with details of changes to the interface,
   useful file locations and parameters.
1. You may merge the Pull Request in once you have the sign-off of one other
   developer, or if you do not have permission to do that, you may request the
   reviewer to merge it for you.

## Building the project

The project can be built locally with the command `./gradlew build` and in
[Android Studio](https://developer.android.com/studio).

If you want to test the impact of any changes in another project, the easiest
way is to use [composite builds](https://docs.gradle.org/current/userguide/composite_builds.html).
In your projects `settings.gradle.kts` add the following to automatically
substitute in the code of the certificatetransparency library:

```kotlin
includeBuild("path-to-root-of-certificatetransparency-project") {
    dependencySubstitution {
        substitute(module("com.appmattus.certificatetransparency:certificatetransparency")).with(project(":certificatetransparency"))
        substitute(module("com.appmattus.certificatetransparency:certificatetransparency-android")).with(project(":certificatetransparency-android"))
    }
}
```
