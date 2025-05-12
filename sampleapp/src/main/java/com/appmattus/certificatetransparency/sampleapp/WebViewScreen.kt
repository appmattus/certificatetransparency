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

package com.appmattus.certificatetransparency.sampleapp

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appmattus.certificatetransparency.sampleapp.examples.State
import com.appmattus.certificatetransparency.sampleapp.examples.trustmanager.WebViewExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.item.text.HeaderTextItem
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Suppress("LongMethod")
fun WebViewScreen(viewModel: WebViewExampleViewModel) {
    val state = viewModel.collectAsState().value

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    state.message?.let {
        scope.launch {
            val color = if (it is State.Message.Success) R.color.colorSuccess else R.color.colorFailure

            val result = scaffoldState.snackbarHostState.showSnackbar(it.text, color.toString())
            if (result == SnackbarResult.Dismissed) {
                viewModel.dismissMessage()
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { snackbarHostState ->
            SnackbarHost(snackbarHostState) {
                Snackbar(backgroundColor = colorResource(it.actionLabel!!.toInt()), modifier = Modifier.padding(8.dp)) {
                    Text(
                        it.message
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxHeight()
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                HeaderTextItem(
                    title = viewModel.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                AndroidView(factory = {
                    WebView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }, update = {
                    it.loadUrl("https://example.com/")
                })
            }
        }
    }
}
