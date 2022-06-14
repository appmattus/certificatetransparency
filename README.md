# Certificate transparency for Android and JVM

[![CI status](https://github.com/appmattus/certificatetransparency/workflows/CI/badge.svg)](https://github.com/appmattus/certificatetransparency/actions)
[![codecov](https://codecov.io/gh/appmattus/certificatetransparency/branch/main/graph/badge.svg)](https://codecov.io/gh/appmattus/certificatetransparency)
[![Maven Central](https://img.shields.io/maven-central/v/com.appmattus.certificatetransparency/certificatetransparency)](https://search.maven.org/search?q=g:com.appmattus.certificatetransparency)

To protect our apps from man-in-the-middle attacks one of the first things that
usually springs to mind is certificate pinning. However, the issues of
certificate pinning are numerous. Firstly deciding on a reliable set of keys to
pin against is tough. Once you made that decision if your expectations don't
match reality your users suffer from not being able to access your app or
website. Smashing Magazine learnt about this the hard way in late 2016 when they
blocked users access for up to a year because of a mismatch between the pins and
the certificates. On mobile fixing an invalid pin means pushing out a new
version of an app which can still take a while to reach every user.

So with certificate pinning falling out of favour, what should you do? The new
kid in town is **[certificate transparency](docs/what-is-certificate-transparency.md)**.

## Security

We are open about the security of our library and provide a threat model in the
[source code](ThreatDragonModels/), created using
[OWASP Threat Dragon](https://threatdragon.org). If you feel there is something
we have missed please reach out so we can keep this up to date.

## Getting started

[![Maven Central](https://img.shields.io/maven-central/v/com.appmattus.certificatetransparency/certificatetransparency)](https://search.maven.org/search?q=g:com.appmattus.certificatetransparency)

For Android modules include the `android` dependency in your build.gradle file
which ensures the necessary ProGuard rules are present:

```kotlin
implementation("com.appmattus.certificatetransparency:certificatetransparency-android:<latest-version>")
```

For Java library modules include the dependency as follows:

```kotlin
implementation("com.appmattus.certificatetransparency:certificatetransparency:<latest-version>")
```

On Android it is recommended to configure certificate transparency through the
provided [Java Security Provider](https://docs.oracle.com/javase/8/docs/api/java/security/Provider.html)
at app startup, which can be configured through
`installCertificateTransparencyProvider`. The advantage of this setup is it
should work across all network types including WebViews with no additional
setup.

> :warning: Android's WebViews only allow you to override GET network requests
> through overriding the *shouldInterceptRequest* method. This means the only
> reliable way to implement certificate transparency in WebViews is to use the
> Java Security Provider documented here.

```kotlin
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        installCertificateTransparencyProvider {
            // Setup a logger
            logger = BasicAndroidCTLogger(BuildConfig.DEBUG)

            // Exclude any subdomain but not "appmattus.com" with no subdomain
            -"*.appmattus.com"

            // Exclude specified domain
            -"example.com"

            // Override the exclusion by including a specific subdomain
            +"allowed.appmattus.com"
        }
    }
}
```

Take a look at the [advanced configuration](docs/advanced-configuration.md) for
documentation on all the available options.

> :warning: Using the Java Security Provider may not work on all JVMs so if you
> are not on Android you are recommended to use one of the alternatives
> documented below.

Certificate transparency can also be setup in specific network connections,
instructions are available for:

- [OkHttp](docs/okhttp.md)
- [Retrofit](docs/retrofit.md)
- [HttpURLConnection](docs/httpurlconnection.md)
- [Volley](docs/volley.md)
- [X509TrustManager](docs/x509trustmanager.md) If you are dealing with non-HTTPS
  connections you can also use the library to verify the certificates by
  wrapping a X509TrustManager and using that.

Currently, there is no support in the library for [Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.1.x/).

## Certificate revocation

Unfortunately in Android there is no built-in support for certificate
revocation, which means you're basically on your own. This is an incredibly hard
to solve problem and it is worth reading [revocation is broken](https://scotthelme.co.uk/revocation-is-broken/)
for more background. Needless to say I would argue that revocation is flawed
along with the broken implementations in mobile and web browsers.

For our purposes we've added `certificateRevocationInterceptor` to this library:

```kotlin
certificateRevocationInterceptor {
    addCrl(
        issuerDistinguishedName = "ME0xCzAJBgNVBAYTAlVTMRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxJzAlBgNVBAMTHkRpZ2lDZXJ0IFNIQTIgU2VjdXJlIFNlcnZlciBDQQ==",
        serialNumbers = listOf("Aa8e+91erglSMgsk/mtVaA==", "A3G1iob2zpw+y3v0L5II/A==")
    )
}
```

It is worth highlighting that the list of revoked certificates would need to be
built into the app and so would require pushing out an app update should you
want to add a revocation in. This does mean there's a small window for any
attacks using a revoked certificate.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of
conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available,
see the [tags on this repository](https://github.com/appmattus/certificatetransparency/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[LICENSE.md](LICENSE.md) file for details
