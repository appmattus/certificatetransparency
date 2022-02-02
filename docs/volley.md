# Using Certificate Transparency with Volley

Overriding the *HostnameVerifier* can be achieved by overriding
`createConnection` when creating the `RequestQueue`:

```kotlin
val requestQueue = Volley.newRequestQueue(applicationContext, object : HurlStack() {
    override fun createConnection(url: URL): HttpURLConnection {
        val connection = super.createConnection(url)
        if (connection is HttpsURLConnection) {
            connection.hostnameVerifier = certificateTransparencyHostnameVerifier(connection.hostnameVerifier)
        }
        return connection
    }
})
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
