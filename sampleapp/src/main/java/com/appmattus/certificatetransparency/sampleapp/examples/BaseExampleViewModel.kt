/*
 * Copyright 2021-2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.sampleapp.examples

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.samskivert.mustache.Mustache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax
import javax.net.ssl.SSLPeerUnverifiedException

@Suppress("TooManyFunctions")
abstract class BaseExampleViewModel(application: Application) : AndroidViewModel(application), ContainerHost<State, Unit> {

    abstract val sampleCodeTemplate: String

    abstract val title: String

    override val container: Container<State, Unit> = viewModelScope.container(State()) {
        intent {
            updateSourceCode()
        }
    }

    fun includeHost(title: String) = intent {
        reduce {
            if (isValidHost(title)) {
                state.copy(includeHosts = state.includeHosts.toMutableSet().apply { add(title) }.toSet())
            } else {
                state.copy(message = State.Message.Failure("Invalid host"))
            }
        }

        updateSourceCode()
    }

    fun excludeHost(title: String) = intent {
        reduce {
            if (isValidHost(title)) {
                state.copy(excludeHosts = state.excludeHosts.toMutableSet().apply { add(title) }.toSet())
            } else {
                state.copy(message = State.Message.Failure("Invalid host"))
            }
        }

        updateSourceCode()
    }

    fun removeIncludeHost(title: String) = intent {
        reduce {
            state.copy(includeHosts = state.includeHosts.toMutableSet().apply { remove(title) }.toSet())
        }
        updateSourceCode()
    }

    fun removeExcludeHost(title: String) = intent {
        reduce {
            state.copy(excludeHosts = state.excludeHosts.toMutableSet().apply { remove(title) }.toSet())
        }
        updateSourceCode()
    }

    fun dismissMessage() = intent {
        reduce {
            state.copy(message = null)
        }
    }

    private fun generateSourceCode(includeHosts: Set<String>, excludeHosts: Set<String>, failOnError: Boolean): String {
        val scopes = mapOf("includeHosts" to includeHosts, "excludeHosts" to excludeHosts, "failOnError" to failOnError)

        val template = BaseExampleViewModel::class.java.classLoader!!.getResourceAsStream(sampleCodeTemplate).bufferedReader().readText()
        return Mustache.compiler().compile(template).execute(scopes)
    }

    private suspend fun Syntax<State, Unit>.updateSourceCode() {
        val source = generateSourceCode(state.includeHosts, state.excludeHosts, state.failOnError)
        reduce {
            state.copy(sampleCode = source)
        }
    }

    fun setFailOnError(failOnError: Boolean) = intent {
        reduce {
            state.copy(failOnError = failOnError)
        }
        updateSourceCode()
    }

    private val defaultLogger = object : CTLogger {
        override fun log(host: String, result: VerificationResult) {
            intent {
                val message = when (result) {
                    is VerificationResult.Success -> State.Message.Success(result.toString())
                    is VerificationResult.Failure -> State.Message.Failure(result.toString())
                }

                Log.d("CT", "$host -> $result")

                reduce {
                    state.copy(message = message)
                }
            }
        }
    }

    fun sendException(e: Throwable?) = intent {
        if (e?.message != "Certificate transparency failed" && e?.cause !is SSLPeerUnverifiedException) {
            reduce {
                state.copy(message = State.Message.Failure(e?.message ?: e.toString()))
            }
        }
    }

    abstract fun openConnection(
        connectionHost: String,
        includeHosts: Set<String>,
        excludeHosts: Set<String>,
        isFailOnError: Boolean,
        defaultLogger: CTLogger
    )

    fun openConnection(connectionHost: String) = intent {
        try {
            openConnection(connectionHost, state.includeHosts, state.excludeHosts, state.failOnError, defaultLogger)
        } catch (expected: Exception) {
            sendException(expected)
        }
    }

    companion object {
        private const val WILDCARD = "*."

        private fun isValidHost(pattern: CharSequence): Boolean {
            val host = if (pattern.startsWith(WILDCARD)) {
                ("http://" + pattern.substring(WILDCARD.length)).toHttpUrlOrNull()?.host
            } else {
                "http://$pattern".toHttpUrlOrNull()?.host
            }

            return host != null
        }
    }
}
