# *sampleapp* module

This module provides a living example of how to use the certificate
transparency library in an Android app.

The following examples are provided:

- [OkHttp](https://square.github.io/okhttp/)
  - Kotlin - [OkHttpKotlinExampleViewModel.kt](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/okhttp/OkHttpKotlinExampleViewModel.kt)
  - Java - [OkHttpJavaExampleViewModel.java](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/okhttp/OkHttpJavaExampleViewModel.java)
- [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection)
  - Kotlin - [HttpURLConnectionKotlinExampleViewModel.kt](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/httpurlconnection/HttpURLConnectionKotlinExampleViewModel.kt)
  - Java - [HttpURLConnectionJavaExampleViewModel.java](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/httpurlconnection/HttpURLConnectionJavaExampleViewModel.java)
- [Volley](https://developer.android.com/training/volley/index.html)
  - Kotlin - [VolleyKotlinExampleViewModel.kt](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/volley/VolleyKotlinExampleViewModel.kt)
  - Java - [VolleyJavaExampleViewModel.java](src/main/java/com/appmattus/certificatetransparency/sampleapp/examples/volley/VolleyJavaExampleViewModel.java)

**Note:** The examples create the certificate transparency interceptor
and hostname verifier on every request. In a real app this should be
saved and reused for all connections.
