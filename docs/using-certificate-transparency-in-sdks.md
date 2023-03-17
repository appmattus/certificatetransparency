# Using Certificate Transparency in SDKs

To perform Certificate Transparency checks in an SDK, configure using one of the
supported methods.

If using OkHttp, it is recommended to use the interceptor method.

When using the [Java Security Provider](https://docs.oracle.com/javase/8/docs/api/java/security/Provider.html)
method, be cautious as it applies across the app and the SDKs configuration can
conflict with that of the client.

To avoid breaking the client app, exclude checks on all domains and explicitly
enable checks only on domains and subdomains used directly.

Note that only one provider with the same name can be installed at a time, but a
custom name can be provided in the configuration.

```kotlin
installCertificateTransparencyProvider(providerName = "CT-SDK") {
    // Exclude checks on all domains
    -"*.*"

    // Override the exclusion for a specific domain
    +"*.appmattus.com"
    // or subdomain
    +"subdomain.appmattus.com"
}
```
