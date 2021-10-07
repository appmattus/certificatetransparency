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

package com.appmattus.certificatetransparency.sampleapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appmattus.certificatetransparency.sampleapp.examples.httpurlconnection.HttpURLConnectionJavaExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.httpurlconnection.HttpURLConnectionKotlinExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.okhttp.OkHttpJavaExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.okhttp.OkHttpKotlinExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.trustmanager.TrustManagerJavaExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.trustmanager.TrustManagerKotlinExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.volley.VolleyJavaExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.volley.VolleyKotlinExampleViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    MainScreen(navController = navController)
                }
                composable("okhttp/kotlin") {
                    ExampleScreen(viewModel = viewModel<OkHttpKotlinExampleViewModel>())
                }
                composable("okhttp/java") {
                    ExampleScreen(viewModel = viewModel<OkHttpJavaExampleViewModel>())
                }
                composable("httpurlconnection/kotlin") {
                    ExampleScreen(viewModel = viewModel<HttpURLConnectionKotlinExampleViewModel>())
                }
                composable("httpurlconnection/java") {
                    ExampleScreen(viewModel = viewModel<HttpURLConnectionJavaExampleViewModel>())
                }
                composable("volley/kotlin") {
                    ExampleScreen(viewModel = viewModel<VolleyKotlinExampleViewModel>())
                }
                composable("volley/java") {
                    ExampleScreen(viewModel = viewModel<VolleyJavaExampleViewModel>())
                }
                composable("trustmanager/kotlin") {
                    ExampleScreen(viewModel = viewModel<TrustManagerKotlinExampleViewModel>())
                }
                composable("trustmanager/java") {
                    ExampleScreen(viewModel = viewModel<TrustManagerJavaExampleViewModel>())
                }
            }
        }
    }
}
