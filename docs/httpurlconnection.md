# Using Certificate Transparency with HttpURLConnection

Firstly if you are still using HttpURLConnection consider upgrading to OkHttp.
The version built into Android, naturally, is a static version so you won't get
any security updates or bug fixes.

To use with HttpURLConnection you wrap the original hostname verifier before
calling connect() on the connection:

```kotlin
val connection = URL("https://www.appmattus.com").openConnection()
if (connection is HttpsURLConnection) {
    connection.hostnameVerifier = certificateTransparencyHostnameVerifier(connection.hostnameVerifier)
}
```

You can also specify which hosts to disable certificate transparency checks on
through exclusions.

```kotlin
connection.hostnameVerifier = certificateTransparencyHostnameVerifier(connection.hostnameVerifier) {
    // Exclude any subdomain but not "appmattus.com" with no subdomain
    -"*.appmattus.com"

    // Exclude specified domain
    -"example.com"

    // Override the exclusion by include a specific subdomain
    +"allowed.appmattus.com"
}
```

In Java, you can create the hostname verifier through
[CTHostnameVerifierBuilder](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/CTHostnameVerifierBuilder.kt).
