/*
 * Copyright 2025 Appmattus Limited
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

package com.appmattus.certificatetransparency.sampleapp.examples.trustmanager

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appmattus.certificatetransparency.CTLogger
import com.appmattus.certificatetransparency.VerificationResult
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import com.appmattus.certificatetransparency.removeCertificateTransparencyProvider
import com.appmattus.certificatetransparency.sampleapp.R
import com.appmattus.certificatetransparency.sampleapp.examples.State
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container

class WebViewExampleViewModel(application: Application) : AndroidViewModel(application), ContainerHost<State, Unit> {

    val title: String
        get() = getApplication<Application>().getString(R.string.webview_example)

    override val container: Container<State, Unit> = viewModelScope.container(State()) {
        intent {
            installCertificateTransparencyProvider {
                failOnError = { true }
                logger = defaultLogger
                diskCache = AndroidDiskCache(getApplication())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeCertificateTransparencyProvider()
    }

    fun dismissMessage() = intent {
        reduce {
            state.copy(message = null)
        }
    }

    fun setFailOnError(failOnError: Boolean) = intent {
        reduce {
            state.copy(failOnError = failOnError)
        }
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
}
