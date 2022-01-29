# Using Certificate Transparency with Retrofit

With Retrofit built on top of OkHttp, configuring it for certificate
transparency is as simple as setting up an `OkHttpClient` as shown in
[Using Certificate Transparency with OkHttp](okhttp.md) supplying that to your
`Retrofit.Builder`.

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://appmattus.com")
    .client(okHttpClient)
    .build()
```
