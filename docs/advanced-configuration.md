# Advanced configuration

## Network Interceptor

The network interceptor allows you to configure the following properties:

**Trust Manager** [X509TrustManager](https://docs.oracle.com/javase/6/docs/api/javax/net/ssl/X509TrustManager.html)
used to clean the certificate chain
*Default:* Platform default [X509TrustManager](https://docs.oracle.com/javase/6/docs/api/javax/net/ssl/X509TrustManager.html)
created through [TrustManagerFactory](http://docs.oracle.com/javase/6/docs/api/javax/net/ssl/TrustManagerFactory.html)

**Log List Service**
A [LogListService](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/loglist/LogListService.kt)
providing log_list.json and log_list.sig byte data from the network. Can be used
to override the OkHttpClient by creating through [LogListDataSourceFactory.createLogListService](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/loglist/LogListDataSourceFactory.kt).
*Default:* log_list.json and log_list.sig byte data loaded
from [https://www.gstatic.com/ct/log_list/v2/log_list.json](https://www.gstatic.com/ct/log_list/v2/log_list.json)

**Log List Data Source** A [DataSource](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/datasource/DataSource.kt)
providing a list of [LogServer](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/loglist/LogServer.kt).
*Default:* In memory cached log list loaded
from [https://www.gstatic.com/ct/log_list/v2/log_list.json](https://www.gstatic.com/ct/log_list/v2/log_list.json)

**Policy** [CTPolicy](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/CTPolicy.kt)
which will verify correct number of SCTs are present
*Default:* Policy which follows rules of [Chromium CT Policy](https://github.com/chromium/ct-policy/blob/master/ct_policy.md)

**Fail On Error** Determine if a failure to pass certificate transparency
results in the connection being closed. A value of `true` ensures the connection
is closed on errors
*Default:* true

**Logger** [CTLogger](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/CTLogger.kt)
which will be called with all results. On Android you can use the provided
[BasicAndroidCTLogger](../certificatetransparency-android/src/main/kotlin/com/appmattus/certificatetransparency/BasicAndroidCTLogger.kt)
which logs with the tag `CertificateTransparency` by setting
`logger = BasicAndroidCRLogger(BuildConfig.DEBUG)` using your apps
`BuildConfig`.
*Default:* none

**Hosts** Verify certificate transparency for hosts that match a pattern which
is a lower-case host name or wildcard pattern such as `*.example.com`.

**Certificate Chain Provider Factory** Provide a custom implementation of a
certificate chain cleaner.
*Default:* Platform default factory which resolves
to [AndroidCertificateChainCleaner](../certificatetransparency-android/src/main/kotlin/com/appmattus/certificatetransparency/chaincleaner/AndroidCertificateChainCleaner.kt)
or [BasicCertificateChainCleaner](../certificatetransparency/src/main/kotlin/com/appmattus/certificatetransparency/chaincleaner/BasicCertificateChainCleaner.kt).

## HostnameVerifier

In addition to all of the properties above the hostname verifier ensures you
provide a **delegate** hostname verifier which is used to first verify the
hostname before the certificate transparency checks occur.
