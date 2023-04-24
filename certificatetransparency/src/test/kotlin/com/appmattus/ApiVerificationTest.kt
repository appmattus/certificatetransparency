/*
 * Copyright 2023 Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appmattus

import io.github.classgraph.ClassGraph
import org.junit.Assert.fail
import org.junit.Test

class ApiVerificationTest {

    @Test
    fun androidSafeClasses() {
        val scanResult = ClassGraph()
            .enableAllInfo()
            .enableExternalClasses()
            .acceptPackages("com.appmattus")
            .enableInterClassDependencies()
            .scan()

        scanResult.allClasses.asSequence()
            .filter { it.packageName.startsWith("com.appmattus") }
            .filter { it.classpathElementFile.name != "test" }
            .mapNotNull { classInfo ->
                classInfo.classDependencies
                    .asSequence()
                    .filterNot { androidAllowedPackages.contains(it.packageName) }
                    .filterNot { it.packageName.startsWith("com.appmattus") }
                    .filterNot { it.packageName.startsWith("kotlin") }
                    .filterNot { it.packageName.startsWith("okhttp3") }
                    .filterNot { it.packageName.startsWith("okio") }
                    .filter { it.name != "org.jetbrains.annotations.NotNull" }
                    .filter { it.name != "org.jetbrains.annotations.Nullable" }
                    .map { it.name }
                    .toList()
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString("\n  ", prefix = "${classInfo.name}\n  ")
            }
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.let {
                fail("Unsupported classes found:\n\n${it.joinToString("\n")}\n\n")
            }
    }

    private val androidAllowedPackages = listOf(
        "android",
        "android.accessibilityservice",
        "android.accounts",
        "android.adservices",
        "android.adservices.adid",
        "android.adservices.adselection",
        "android.adservices.appsetid",
        "android.adservices.common",
        "android.adservices.customaudience",
        "android.adservices.exceptions",
        "android.adservices.measurement",
        "android.adservices.topics",
        "android.animation",
        "android.annotation",
        "android.app",
        "android.app.admin",
        "android.app.appsearch",
        "android.app.appsearch.exceptions",
        "android.app.appsearch.observer",
        "android.app.appsearch.util",
        "android.app.assist",
        "android.app.backup",
        "android.app.blob",
        "android.app.job",
        "android.app.people",
        "android.app.role",
        "android.app.sdksandbox",
        "android.app.sdksandbox.sdkprovider",
        "android.app.slice",
        "android.app.usage",
        "android.appwidget",
        "android.bluetooth",
        "android.bluetooth.le",
        "android.companion",
        "android.companion.virtual",
        "android.content",
        "android.content.om",
        "android.content.pm",
        "android.content.pm.verify.domain",
        "android.content.res",
        "android.content.res.loader",
        "android.credentials",
        "android.database",
        "android.database.sqlite",
        "android.devicelock",
        "android.drm",
        "android.gesture",
        "android.graphics",
        "android.graphics.drawable",
        "android.graphics.drawable.shapes",
        "android.graphics.fonts",
        "android.graphics.pdf",
        "android.graphics.text",
        "android.hardware",
        "android.hardware.biometrics",
        "android.hardware.camera2",
        "android.hardware.camera2.params",
        "android.hardware.display",
        "android.hardware.fingerprint",
        "android.hardware.input",
        "android.hardware.lights",
        "android.hardware.usb",
        "android.health.connect",
        "android.health.connect.changelog",
        "android.health.connect.datatypes",
        "android.health.connect.datatypes.units",
        "android.icu.lang",
        "android.icu.math",
        "android.icu.number",
        "android.icu.text",
        "android.icu.util",
        "android.inputmethodservice",
        "android.location",
        "android.location.altitude",
        "android.location.provider",
        "android.media",
        "android.media.audiofx",
        "android.media.browse",
        "android.media.effect",
        "android.media.metrics",
        "android.media.midi",
        "android.media.projection",
        "android.media.session",
        "android.media.tv",
        "android.media.tv.interactive",
        "android.mtp",
        "android.net",
        "android.net.eap",
        "android.net.http",
        "android.net.ipsec.ike",
        "android.net.ipsec.ike.exceptions",
        "android.net.nsd",
        "android.net.rtp",
        "android.net.sip",
        "android.net.ssl",
        "android.net.vcn",
        "android.net.wifi",
        "android.net.wifi.aware",
        "android.net.wifi.hotspot2",
        "android.net.wifi.hotspot2.omadm",
        "android.net.wifi.hotspot2.pps",
        "android.net.wifi.p2p",
        "android.net.wifi.p2p.nsd",
        "android.net.wifi.rtt",
        "android.nfc",
        "android.nfc.cardemulation",
        "android.nfc.tech",
        "android.opengl",
        "android.os",
        "android.os.ext",
        "android.os.health",
        "android.os.storage",
        "android.os.strictmode",
        "android.preference",
        "android.print",
        "android.print.pdf",
        "android.printservice",
        "android.provider",
        "android.renderscript",
        "android.sax",
        "android.se.omapi",
        "android.security",
        "android.security.identity",
        "android.security.keystore",
        "android.service.assist.classification",
        "android.service.autofill",
        "android.service.carrier",
        "android.service.chooser",
        "android.service.controls",
        "android.service.controls.actions",
        "android.service.controls.templates",
        "android.service.credentials",
        "android.service.dreams",
        "android.service.media",
        "android.service.notification",
        "android.service.quickaccesswallet",
        "android.service.quicksettings",
        "android.service.restrictions",
        "android.service.textservice",
        "android.service.voice",
        "android.service.vr",
        "android.service.wallpaper",
        "android.speech",
        "android.speech.tts",
        "android.system",
        "android.telecom",
        "android.telephony",
        "android.telephony.cdma",
        "android.telephony.data",
        "android.telephony.emergency",
        "android.telephony.euicc",
        "android.telephony.gsm",
        "android.telephony.ims",
        "android.telephony.ims.feature",
        "android.telephony.ims.stub",
        "android.telephony.mbms",
        "android.test",
        "android.test.mock",
        "android.test.suitebuilder",
        "android.test.suitebuilder.annotation",
        "android.text",
        "android.text.format",
        "android.text.method",
        "android.text.style",
        "android.text.util",
        "android.transition",
        "android.util",
        "android.util.proto",
        "android.view",
        "android.view.accessibility",
        "android.view.animation",
        "android.view.autofill",
        "android.view.contentcapture",
        "android.view.displayhash",
        "android.view.inputmethod",
        "android.view.inspector",
        "android.view.textclassifier",
        "android.view.textservice",
        "android.view.translation",
        "android.webkit",
        "android.widget",
        "android.widget.inline",
        "android.window",
        "dalvik.annotation",
        "dalvik.annotation.optimization",
        "dalvik.bytecode",
        "dalvik.system",
        "java.awt.font",
        "java.beans",
        "java.io",
        "java.lang",
        "java.lang.annotation",
        "java.lang.invoke",
        "java.lang.ref",
        "java.lang.reflect",
        "java.math",
        "java.net",
        "java.nio",
        "java.nio.channels",
        "java.nio.channels.spi",
        "java.nio.charset",
        "java.nio.charset.spi",
        "java.nio.file",
        "java.nio.file.attribute",
        "java.nio.file.spi",
        "java.security",
        "java.security.acl",
        "java.security.cert",
        "java.security.interfaces",
        "java.security.spec",
        "java.sql",
        "java.text",
        "java.time",
        "java.time.chrono",
        "java.time.format",
        "java.time.temporal",
        "java.time.zone",
        "java.util",
        "java.util.concurrent",
        "java.util.concurrent.atomic",
        "java.util.concurrent.locks",
        "java.util.function",
        "java.util.jar",
        "java.util.logging",
        "java.util.prefs",
        "java.util.regex",
        "java.util.stream",
        "java.util.zip",
        "javax.annotation.processing",
        "javax.crypto",
        "javax.crypto.interfaces",
        "javax.crypto.spec",
        "javax.microedition.khronos.egl",
        "javax.microedition.khronos.opengles",
        "javax.net",
        "javax.net.ssl",
        "javax.security.auth",
        "javax.security.auth.callback",
        "javax.security.auth.login",
        "javax.security.auth.x500",
        "javax.security.cert",
        "javax.sql",
        "javax.xml",
        "javax.xml.datatype",
        "javax.xml.namespace",
        "javax.xml.parsers",
        "javax.xml.transform",
        "javax.xml.transform.dom",
        "javax.xml.transform.sax",
        "javax.xml.transform.stream",
        "javax.xml.validation",
        "javax.xml.xpath",
        "junit.framework",
        "junit.runner",
        "org.apache.http.conn",
        "org.apache.http.conn.scheme",
        "org.apache.http.conn.ssl",
        "org.apache.http.params",
        "org.json",
        "org.w3c.dom",
        "org.w3c.dom.ls",
        "org.xml.sax",
        "org.xml.sax.ext",
        "org.xml.sax.helpers",
        "org.xmlpull.v1",
        "org.xmlpull.v1.sax2"
    )
}
