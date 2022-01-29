# Using Certificate Transparency with OkHttp

The library allows you to create a network interceptor for use with OkHttp where
by default certificate transparency checks are run on all
domains.

```kotlin
val interceptor = certificateTransparencyInterceptor()

val client = OkHttpClient.Builder().apply {
    addNetworkInterceptor(interceptor)
}.build()
```

You can also specify which hosts to disable certificate transparency checks on
through exclusions.

```kotlin
val interceptor = certificateTransparencyInterceptor {
    // Exclude any subdomain but not "appmattus.com" with no subdomain
    -"*.appmattus.com"

    // Exclude specified domain
    -"example.com"

    // Override the exclusion by include a specific subdomain
    +"allowed.appmattus.com"
}
```

In Java, you can create the network interceptor through
[CTInterceptorBuilder](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/CTInterceptorBuilder.kt).
