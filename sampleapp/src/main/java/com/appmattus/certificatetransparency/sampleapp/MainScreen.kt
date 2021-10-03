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

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appmattus.certificatetransparency.sampleapp.compose.ExampleCardItem
import com.appmattus.certificatetransparency.sampleapp.compose.text.HeaderTextItem

@Composable
fun MainScreen(navController: NavController) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
        ) {
            item {
                HeaderTextItem(
                    title = "Certificate Transparency",
                    icon = R.drawable.ic_launcher_foreground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                ExampleCardItem(
                    scaffoldState = scaffoldState,
                    title = "OkHttp",
                    moreInfoUri = Uri.parse("https://square.github.io/okhttp/"),
                    onKotlinClick = { navController.navigate("okhttp/kotlin") },
                    onJavaClick = { navController.navigate("okhttp/java") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                ExampleCardItem(
                    scaffoldState = scaffoldState,
                    title = "HttpURLConnection",
                    moreInfoUri = Uri.parse("https://developer.android.com/reference/java/net/HttpURLConnection"),
                    onKotlinClick = { navController.navigate("httpurlconnection/kotlin") },
                    onJavaClick = { navController.navigate("httpurlconnection/java") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                ExampleCardItem(
                    scaffoldState = scaffoldState,
                    title = "Volley",
                    moreInfoUri = Uri.parse("https://developer.android.com/training/volley/index.html"),
                    onKotlinClick = { navController.navigate("volley/kotlin") },
                    onJavaClick = { navController.navigate("volley/java") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
