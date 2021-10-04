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

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.appmattus.certificatetransparency.sampleapp.compose.CheckboxItem
import com.appmattus.certificatetransparency.sampleapp.compose.CodeViewItem
import com.appmattus.certificatetransparency.sampleapp.compose.RemovableItem
import com.appmattus.certificatetransparency.sampleapp.compose.text.BodyTextItem
import com.appmattus.certificatetransparency.sampleapp.compose.text.HeaderTextItem
import com.appmattus.certificatetransparency.sampleapp.compose.text.SubHeaderTextItem
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.State
import com.pddstudio.highlightjs.models.Language
import kotlinx.coroutines.launch

@Composable
fun ExampleScreen(viewModel: BaseExampleViewModel) {
    val state = viewModel.liveData.observeAsState().value

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    state?.message?.let {
        scope.launch {
            val color = if (it is State.Message.Success) R.color.colorSuccess else R.color.colorFailure

            val result = scaffoldState.snackbarHostState.showSnackbar(it.text, color.toString())
            if (result == SnackbarResult.Dismissed) {
                println("dismissed")
                viewModel.dismissMessage()
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) {
                Snackbar(backgroundColor = colorResource(it.actionLabel!!.toInt()), modifier = Modifier.padding(8.dp)) { Text(it.message) }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                HeaderTextItem(title = viewModel.title, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            item {
                SubHeaderTextItem(
                    title = stringResource(R.string.configuration),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Hosts
            item {
                BodyTextItem(
                    title = "Verify certificate transparency for hosts that match one of the patterns.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(state?.hosts?.toList() ?: emptyList()) { host ->
                RemovableItem(
                    host,
                    onRemoveClick = { viewModel.removeHost(host) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                OutlinedButton(
                    onClick = { showIncludeHostDialog(context, viewModel) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null,
                        Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.include_host))
                }
            }

            // Fail On Error
            item {
                BodyTextItem(
                    title = "Determine if a failure to pass certificate transparency results in the connection being closed. " +
                            "A value of true ensures the connection is closed on errors.\nDefault: true",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                CheckboxItem(
                    title = stringResource(R.string.fail_on_error),
                    checked = state?.failOnError ?: true,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    viewModel.setFailOnError(it)
                }
            }

            item {
                SubHeaderTextItem(
                    title = stringResource(R.string.sample_code),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // CodeViewItem
            item {
                CodeViewItem(
                    language = Language.JAVA,
                    sourceCode = state?.sampleCode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Button(
                    onClick = { showConnectionDialog(context, viewModel) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.test_certificate_transparency))
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@SuppressLint("CheckResult")
private fun showIncludeHostDialog(context: Context, viewModel: BaseExampleViewModel) {
    MaterialDialog(context).show {
        title(R.string.include_host_title)
        message(R.string.include_host_message)
        input { _, text ->
            viewModel.includeHost(text.toString())
        }

        positiveButton(text = "Include")
        negativeButton(text = "Cancel")
    }
}

@SuppressLint("CheckResult")
private fun showConnectionDialog(context: Context, viewModel: BaseExampleViewModel) {
    MaterialDialog(context).show {
        title(text = "Test connection")
        message(text = "Please provide a host to test a connection to. 'https://' will be automatically added")
        input { _, text ->
            viewModel.openConnection(text.toString())
        }

        positiveButton(text = "Connect")
        negativeButton(text = "Cancel")
    }
}
