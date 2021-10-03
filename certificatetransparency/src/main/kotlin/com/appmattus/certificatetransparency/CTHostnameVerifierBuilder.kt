/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency

import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyHostnameVerifier
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import com.appmattus.certificatetransparency.loglist.LogServer
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Builder to create a [HostnameVerifier] that will verify a host is trusted using certificate
 * transparency
 * @property delegate [HostnameVerifier] to delegate to before performing certificate transparency checks
 */
@Suppress("TooManyFunctions")
public class CTHostnameVerifierBuilder(
    @Suppress("MemberVisibilityCanBePrivate") public val delegate: HostnameVerifier
) {
    private var certificateChainCleanerFactory: CertificateChainCleanerFactory? = null
    private var trustManager: X509TrustManager? = null
    private var logListService: LogListService? = null
    private var logListDataSource: DataSource<LogListResult>? = null
    private val includeHosts = mutableSetOf<Host>()
    private val excludeHosts = mutableSetOf<Host>()

    /**
     * Determine if a failure to pass certificate transparency results in the connection being closed. A value of true ensures the connection is
     * closed on errors
     * Default: true
     */
    public var failOnError: Boolean = true
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CTLogger] which will be called with all results
     * Default: none
     */
    public var logger: CTLogger? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CTPolicy] which will verify correct number of SCTs are present
     * Default: [CTPolicy] which follows rules of https://github.com/chromium/ct-policy/blob/master/ct_policy.md
     */
    public var policy: CTPolicy? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [DiskCache] which will cache the log list
     * Default: none
     */
    public var diskCache: DiskCache? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setCertificateChainCleanerFactory(certificateChainCleanerFactory: CertificateChainCleanerFactory): CTHostnameVerifierBuilder =
        apply { this.certificateChainCleanerFactory = certificateChainCleanerFactory }

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun certificateChainCleanerFactory(init: () -> CertificateChainCleanerFactory) {
        setCertificateChainCleanerFactory(init())
    }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setTrustManager(trustManager: X509TrustManager): CTHostnameVerifierBuilder =
        apply { this.trustManager = trustManager }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun trustManager(init: () -> X509TrustManager) {
        setTrustManager(init())
    }

    /**
     * A [LogListService] providing log list data from network
     * Default: Log list loaded from https://www.gstatic.com/ct/log_list/v2/log_list.json
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setLogListService(logListService: LogListService): CTHostnameVerifierBuilder =
        apply {
            this.logListService = logListService
        }

    /**
     * A [LogListService] providing log list data from network
     * Default: Log list loaded from https://www.gstatic.com/ct/log_list/v2/log_list.json
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun logListService(init: () -> LogListService) {
        setLogListService(init())
    }

    /**
     * A [DataSource] providing a list of [LogServer]
     * Default: In memory cached log list loaded from https://www.gstatic.com/ct/log_list/v2/log_list.json
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setLogListDataSource(logListDataSource: DataSource<LogListResult>): CTHostnameVerifierBuilder =
        apply {
            this.logListDataSource = logListDataSource
        }

    /**
     * A [DataSource] providing a list of [LogServer]
     * Default: In memory cached log list loaded from https://www.gstatic.com/ct/log_list/v2/log_list.json
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun logListDataSource(init: () -> DataSource<LogListResult>) {
        setLogListDataSource(init())
    }

    /**
     * Determine if a failure to pass certificate transparency results in the connection being closed. [failOnError] set to true closes the
     * connection on errors
     * Default: true
     */
    @Suppress("unused")
    public fun setFailOnError(failOnError: Boolean): CTHostnameVerifierBuilder = apply { this.failOnError = failOnError }

    /**
     * [CTLogger] which will be called with all results
     * Default: none
     */
    @Suppress("unused")
    public fun setLogger(logger: CTLogger): CTHostnameVerifierBuilder = apply { this.logger = logger }

    /**
     * [CTPolicy] which will verify correct number of SCTs are present
     * Default: [CTPolicy] which follows rules of https://github.com/chromium/ct-policy/blob/master/ct_policy.md
     */
    @Suppress("unused")
    public fun setPolicy(policy: CTPolicy): CTHostnameVerifierBuilder = apply { this.policy = policy }

    /**
     * [DiskCache] which will cache the log list
     * Default: none
     */
    @Suppress("unused")
    public fun setDiskCache(diskCache: DiskCache): CTHostnameVerifierBuilder = apply { this.diskCache = diskCache }

    /**
     * Verify certificate transparency for hosts that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun includeHost(vararg pattern: String): CTHostnameVerifierBuilder = apply {
        pattern.forEach { includeHosts.add(Host(it)) }
    }

    /**
     * Verify certificate transparency for hosts that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryPlus() {
        includeHost(this)
    }

    /**
     * Verify certificate transparency for hosts that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryPlus() {
        forEach { includeHost(it) }
    }

    /**
     * Ignore certificate transparency for hosts that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun excludeHost(vararg pattern: String): CTHostnameVerifierBuilder = apply {
        pattern.forEach { excludeHosts.add(Host(it)) }
    }

    /**
     * Ignore certificate transparency for hosts that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryMinus() {
        excludeHost(this)
    }

    /**
     * Ignore certificate transparency for hosts that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryMinus() {
        forEach { excludeHost(it) }
    }

    /**
     * Build the [HostnameVerifier]
     */
    public fun build(): HostnameVerifier = CertificateTransparencyHostnameVerifier(
        delegate,
        includeHosts.toSet(),
        excludeHosts.toSet(),
        certificateChainCleanerFactory,
        trustManager,
        logListService,
        logListDataSource,
        policy,
        diskCache,
        failOnError,
        logger
    )
}
