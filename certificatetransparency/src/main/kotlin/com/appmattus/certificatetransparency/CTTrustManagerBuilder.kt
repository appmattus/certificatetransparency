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

package com.appmattus.certificatetransparency

import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.appmattus.certificatetransparency.datasource.DataSource
import com.appmattus.certificatetransparency.internal.verifier.CertificateTransparencyTrustManager
import com.appmattus.certificatetransparency.internal.verifier.model.Host
import com.appmattus.certificatetransparency.loglist.LogListResult
import com.appmattus.certificatetransparency.loglist.LogListService
import com.appmattus.certificatetransparency.loglist.LogServer
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.X509TrustManager

/**
 * Builder to create a [X509TrustManager] that will verify a certificate is trusted using certificate transparency
 * @property delegate [X509TrustManager] to delegate to before performing certificate transparency checks
 */
@Suppress("TooManyFunctions")
public class CTTrustManagerBuilder(
    @Suppress("MemberVisibilityCanBePrivate") public val delegate: X509TrustManager
) {
    private var certificateChainCleanerFactory: CertificateChainCleanerFactory? = null
    private var logListService: LogListService? = null
    private var logListDataSource: DataSource<LogListResult>? = null
    private val includeCommonNames = mutableSetOf<Host>()
    private val excludeCommonNames = mutableSetOf<Host>()

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
    public fun setCertificateChainCleanerFactory(certificateChainCleanerFactory: CertificateChainCleanerFactory): CTTrustManagerBuilder =
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
     * A [LogListService] providing log list data from network
     * Default: Log list loaded from https://www.gstatic.com/ct/log_list/v2/log_list.json
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setLogListService(logListService: LogListService): CTTrustManagerBuilder =
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
    public fun setLogListDataSource(logListDataSource: DataSource<LogListResult>): CTTrustManagerBuilder =
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
    public fun setFailOnError(failOnError: Boolean): CTTrustManagerBuilder = apply { this.failOnError = failOnError }

    /**
     * [CTLogger] which will be called with all results
     * Default: none
     */
    @Suppress("unused")
    public fun setLogger(logger: CTLogger): CTTrustManagerBuilder = apply { this.logger = logger }

    /**
     * [CTPolicy] which will verify correct number of SCTs are present
     * Default: [CTPolicy] which follows rules of https://github.com/chromium/ct-policy/blob/master/ct_policy.md
     */
    @Suppress("unused")
    public fun setPolicy(policy: CTPolicy): CTTrustManagerBuilder = apply { this.policy = policy }

    /**
     * [DiskCache] which will cache the log list
     * Default: none
     */
    @Suppress("unused")
    public fun setDiskCache(diskCache: DiskCache): CTTrustManagerBuilder = apply { this.diskCache = diskCache }

    /**
     * Verify certificate transparency for common names that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun includeCommonName(vararg pattern: String): CTTrustManagerBuilder = apply {
        pattern.forEach { includeCommonNames.add(Host(it)) }
    }

    /**
     * Verify certificate transparency for common names that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryPlus() {
        includeCommonName(this)
    }

    /**
     * Verify certificate transparency for common names that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryPlus() {
        forEach { includeCommonName(it) }
    }

    /**
     * Ignore certificate transparency for common names that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun excludeCommonName(vararg pattern: String): CTTrustManagerBuilder = apply {
        pattern.forEach { excludeCommonNames.add(Host(it)) }
    }

    /**
     * Ignore certificate transparency for common names that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryMinus() {
        excludeCommonName(this)
    }

    /**
     * Ignore certificate transparency for common names that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryMinus() {
        forEach { excludeCommonName(it) }
    }

    /**
     * Build the [HostnameVerifier]
     */
    public fun build(): X509TrustManager = CertificateTransparencyTrustManager(
        delegate,
        includeCommonNames.toSet(),
        excludeCommonNames.toSet(),
        certificateChainCleanerFactory,
        logListService,
        logListDataSource,
        policy,
        diskCache,
        failOnError,
        logger
    )
}
