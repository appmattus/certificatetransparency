package com.appmattus.certificaterevocation

import com.appmattus.certificatetransparency.utils.TestData
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * DO NOT USE IN REAL CODE
 * This is an OkHttpClient that trusts all certificates as the certs for revoked.badssl.com have expired
 */
fun trustAllOkHttpClient(builder: OkHttpClient.Builder.() -> Unit): OkHttpClient {
    val trustAllCerts = object : X509TrustManager {
        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit

        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> =
            TestData.loadCertificates(TestData.REVOKED_BADSSL_INTERMEDIATE_CA_CERT).toTypedArray()
    }

    val trustAllHostnameVerifier = HostnameVerifier { _, _ -> true }

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(trustAllCerts as TrustManager), SecureRandom())

    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts)
        .hostnameVerifier(trustAllHostnameVerifier)
        .apply(builder)
        .build()
}
