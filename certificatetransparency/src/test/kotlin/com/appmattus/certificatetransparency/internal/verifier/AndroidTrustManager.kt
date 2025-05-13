package com.appmattus.certificatetransparency.internal.verifier

import java.security.cert.X509Certificate

/**
 * Interface used to mimic calls made through reflection by X509TrustManagerExtensions on Android
 */
interface AndroidTrustManager {
    // Added in API level 17
    fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, host: String): List<X509Certificate>

    // Added in API level 36
    fun checkServerTrusted(
        chain: Array<out X509Certificate>,
        ocspData: ByteArray?,
        tlsSctData: ByteArray?,
        authType: String,
        host: String
    ): List<X509Certificate>

    // Added in API level 28
    fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean
}
