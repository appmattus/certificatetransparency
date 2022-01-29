# Using Certificate Transparency with X509TrustManager

If you are dealing with non-HTTPS connections you can also use the library to
verify the certificates by wrapping a X509TrustManager and using that:

```kotlin
val wrappedTrustManager = certificateTransparencyTrustManager(originalTrustManager)
```

You can also specify exclusions and inclusions to disable certificate
transparency checks. As a TrustManager is not provided with the host used in a
request the inclusion and exclusion rules are based on the common name from the
leaf certificate presented.

```kotlin
val wrappedTrustManager = certificateTransparencyTrustManager(originalTrustManager) {
    // Exclude any subdomain but not "appmattus.com" with no subdomain
    -"*.appmattus.com"

    // Exclude specified domain
    -"example.com"

    // Override the exclusion by include a specific subdomain
    +"allowed.appmattus.com"
}
```
