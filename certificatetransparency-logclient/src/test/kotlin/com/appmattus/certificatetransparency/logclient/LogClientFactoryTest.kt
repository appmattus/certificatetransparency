/*
 * Copyright 2021 Appmattus Limited
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

package com.appmattus.certificatetransparency.logclient

import com.appmattus.certificatetransparency.logclient.LogClientFactory.create
import com.appmattus.certificatetransparency.logclient.model.LogEntry
import org.junit.Ignore
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

public class LogClientFactoryTest {

    // Currently returns: SerializationException: Extra data corrupted
    @Test
    @Ignore("See https://github.com/babylonhealth/certificate-transparency-android/issues/68")
    public fun testIt() {
        val client = create("http://ct.googleapis.com/logs/argon2021/ct/v1/")
        // SignedTreeHead sth = client.getLogSTH();
        val sth = client.logSth
        println(sth)
        val ent = client.getLogEntries(0, 10)
        val logEntry = ent[0]
        val (leafCert) = logEntry.logEntry as LogEntry.X509ChainEntry
        val cert = CertificateFactory.getInstance("X509")
            .generateCertificate(ByteArrayInputStream(leafCert)) as X509Certificate
        println(cert)
    }
}
