/*
 * Copyright 2021 Appmattus Limited
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class HostTest {

    @Test
    fun identicalPatternsEqual() {
        val host1 = Host("example.com")
        val host2 = Host("example.com")

        assertEquals(host1, host2)
        assertEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun identicalWildcardPatternsEqual() {
        val host1 = Host("*.example.com")
        val host2 = Host("*.example.com")

        assertEquals(host1, host2)
        assertEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun canonicalPatternsEqual() {
        val host1 = Host("example.com")
        val host2 = Host("eXaMpLe.CoM")

        assertEquals(host1, host2)
        assertEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun subdomainsDoNotEqual() {
        val host1 = Host("sub1.example.com")
        val host2 = Host("sub2.example.com")

        assertNotEquals(host1, host2)
        assertNotEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun baseAndSubDoNotEqual() {
        val host1 = Host("example.com")
        val host2 = Host("sub.example.com")

        assertNotEquals(host1, host2)
        assertNotEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun baseAndWildcardDoNotEqual() {
        val host1 = Host("example.com")
        val host2 = Host("*.example.com")

        assertNotEquals(host1, host2)
        assertNotEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun subAndWildcardDoNotEqual() {
        val host1 = Host("sub.example.com")
        val host2 = Host("*.example.com")

        assertNotEquals(host1, host2)
        assertNotEquals(host1.hashCode(), host2.hashCode())
    }

    @Test
    fun baseDomainMatches() {
        val host = Host("example.com")

        assertTrue(host.matches("example.com"))
    }

    @Test
    fun subdomainMatches() {
        val host = Host("sub.example.com")

        assertTrue(host.matches("sub.example.com"))
    }

    @Test
    fun subdomainMatchesCanonical() {
        val host = Host("SuB.eXaMpLe.CoM")

        assertTrue(host.matches("sub.example.com"))
    }

    @Test
    fun differentSubdomainsDoNotMatch() {
        val host = Host("sub1.example.com")

        assertFalse(host.matches("sub2.example.com"))
    }

    @Test
    fun wildcardMatchesSubdomain() {
        val host = Host("*.example.com")

        assertTrue(host.matches("sub.example.com"))
    }

    @Test
    fun wildcardDoesNotMatchBase() {
        val host = Host("*.example.com")

        assertFalse(host.matches("example.com"))
    }

    @Test
    fun baseDomainMatchesCanonical() {
        val host = Host("eXaMpLe.CoM")

        assertTrue(host.matches("example.com"))
    }

    @Test
    fun baseDoesNotMatchSubdomain() {
        val host = Host("example.com")

        assertFalse(host.matches("sub.example.com"))
    }

    @Test
    fun allBaseDomainMatches() {
        val host = Host("*.*")

        assertTrue(host.matches("example.com"))
    }

    @Test
    fun allSubdomainMatches() {
        val host = Host("*.*")

        assertTrue(host.matches("sub.example.com"))
    }

    @Test
    fun allNestedSubdomainMatches() {
        val host = Host("*.*")

        assertTrue(host.matches("aa.bb.cc.com"))
    }

    @Test
    fun allEmptyMatches() {
        val host = Host("*.*")

        assertTrue(host.matches(""))
    }
}
