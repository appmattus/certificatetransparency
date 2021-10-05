## Retrofit
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions



## OkHttp3

-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.Conscrypt$*
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# From https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier


## Kotlinx Serialization

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.appmattus.**$$serializer { *; }
-keepclassmembers class com.appmattus.** {
    *** Companion;
}
-keepclasseswithmembers class com.appmattus.** {
    kotlinx.serialization.KSerializer serializer(...);
}


## Bouncycastle
-dontwarn javax.naming.**


## Ensure network models are not obfuscated
# See https://github.com/babylonhealth/certificate-transparency-android/issues/38
-keep class com.appmattus.certificatetransparency.internal.loglist.model.v2.* { *; }

# Ensure chain cleaner classes are kept as they're loaded through reflection
-keep class com.appmattus.certificatetransparency.chaincleaner.* { *; }


# Specifically for ProGuard (not needed for R8)
-dontwarn module-info
-dontnote module-info
-dontwarn kotlinx.coroutines.debug.AgentPremain
-dontwarn kotlinx.coroutines.debug.AgentPremain$DebugProbesTransformer
-dontwarn org.bouncycastle.jce.provider.OcspCache
-dontwarn org.bouncycastle.jce.provider.ProvOcspRevocationChecker
-dontwarn org.bouncycastle.jsse.util.CustomSSLSocketFactory
