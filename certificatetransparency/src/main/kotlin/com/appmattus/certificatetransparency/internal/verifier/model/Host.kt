/*
 * Copyright 2021-2025 Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

package com.appmattus.certificatetransparency.internal.verifier.model

import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * @property pattern A hostname like `example.com` or a pattern like `*.example.com`.
 */
internal data class Host(
    private val pattern: String
) {
    /**
     * The canonical hostname, i.e. `EXAMPLE.com` becomes `example.com`.
     */
    private val canonicalHostname: String

    val startsWithWildcard = pattern.startsWith(WILDCARD)

    val matchAll = pattern == "*.*"

    init {
        this.canonicalHostname = if (startsWithWildcard) {
            ("http://" + pattern.substring(WILDCARD.length)).toHttpUrl().host
        } else {
            "http://$pattern".toHttpUrl().host
        }
    }

    fun matches(hostname: String): Boolean {
        if (startsWithWildcard) {
            val firstDot = hostname.indexOf('.')
            return matchAll || hostname.length - firstDot - 1 == canonicalHostname.length && hostname.regionMatches(
                firstDot + 1,
                canonicalHostname,
                0,
                canonicalHostname.length,
                ignoreCase = false
            )
        }

        return hostname == canonicalHostname
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return (other is Host && canonicalHostname == other.canonicalHostname && startsWithWildcard == other.startsWithWildcard)
    }

    override fun hashCode(): Int {
        return arrayOf<Any>(canonicalHostname, startsWithWildcard).contentHashCode()
    }

    companion object {

        private const val WILDCARD = "*."
    }
}
