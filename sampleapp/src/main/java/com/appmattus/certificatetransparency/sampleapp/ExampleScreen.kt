/*
 * Copyright 2021-2025 Appmattus Limited
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import com.appmattus.certificatetransparency.sampleapp.examples.State
import com.appmattus.certificatetransparency.sampleapp.item.CheckboxItem
import com.appmattus.certificatetransparency.sampleapp.item.CodeViewItem
import com.appmattus.certificatetransparency.sampleapp.item.OutlineButtonItem
import com.appmattus.certificatetransparency.sampleapp.item.RemovableItem
import com.appmattus.certificatetransparency.sampleapp.item.text.BodyTextItem
import com.appmattus.certificatetransparency.sampleapp.item.text.HeaderTextItem
import com.appmattus.certificatetransparency.sampleapp.item.text.SubHeaderTextItem
import com.pddstudio.highlightjs.models.Language
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Suppress("LongMethod")
fun ExampleScreen(viewModel: BaseExampleViewModel) {
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

    val showConnectionDialog = remember { mutableStateOf(false) }
    TestConnectionDialog(showConnectionDialog) { viewModel.openConnection(it) }

    val showIncludeHostDialog = remember { mutableStateOf(false) }
    IncludeHostDialog(showIncludeHostDialog) { viewModel.includeHost(it) }

    val showExcludeHostDialog = remember { mutableStateOf(false) }
    ExcludeHostDialog(showExcludeHostDialog) { viewModel.excludeHost(it) }

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

            configurationSection(
                viewModel = viewModel,
                state = state,
                showIncludeHostDialog = showIncludeHostDialog,
                showExcludeHostDialog = showExcludeHostDialog
            )
            sampleCodeSection(state = state)

            item {
                Button(
                    onClick = { showConnectionDialog.value = true },
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

private fun LazyListScope.configurationSection(
    viewModel: BaseExampleViewModel,
    state: State?,
    showIncludeHostDialog: MutableState<Boolean>,
    showExcludeHostDialog: MutableState<Boolean>
) {
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
    item { Spacer(modifier = Modifier.height(8.dp)) }
    items(state?.excludeHosts?.toList() ?: emptyList()) { host ->
        RemovableItem(
            title = "-\"$host\"",
            onRemoveClick = { viewModel.removeExcludeHost(host) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
    items(state?.includeHosts?.toList() ?: emptyList()) { host ->
        RemovableItem(
            title = "+\"$host\"",
            onRemoveClick = { viewModel.removeIncludeHost(host) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
    item { Spacer(modifier = Modifier.height(8.dp)) }
    item {
        OutlineButtonItem(
            title = stringResource(R.string.exclude_host),
            icon = R.drawable.minus,
            onClick = { showExcludeHostDialog.value = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    item {
        OutlineButtonItem(
            title = stringResource(R.string.include_host),
            icon = R.drawable.plus,
            onClick = { showIncludeHostDialog.value = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    // Fail on error
    item {
        BodyTextItem(
            title = stringResource(R.string.fail_on_error_description),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    item {
        CheckboxItem(
            title = stringResource(R.string.fail_on_error),
            checked = state?.failOnError ?: true,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) { viewModel.setFailOnError(it) }
    }
}

private fun LazyListScope.sampleCodeSection(state: State?) {
    item {
        SubHeaderTextItem(
            title = stringResource(R.string.sample_code),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    item {
        CodeViewItem(
            language = Language.JAVA,
            sourceCode = state?.sampleCode,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun IncludeHostDialog(showIncludeHostDialog: MutableState<Boolean>, onInclude: (String) -> Unit) {
    if (showIncludeHostDialog.value) {
        val text = remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showIncludeHostDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    showIncludeHostDialog.value = false
                    onInclude(text.value)
                }) { Text(stringResource(R.string.include_host_dialog_include)) }
            },
            dismissButton = {
                Button(onClick = {
                    showIncludeHostDialog.value = false
                }) { Text(stringResource(R.string.include_host_dialog_cancel)) }
            },
            title = { Text(stringResource(R.string.include_host_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.include_host_dialog_message),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = text.value,
                        onValueChange = { text.value = it }
                    )
                }
            }
        )
    }
}

@Composable
private fun ExcludeHostDialog(showExcludeHostDialog: MutableState<Boolean>, onExclude: (String) -> Unit) {
    if (showExcludeHostDialog.value) {
        val text = remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showExcludeHostDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    showExcludeHostDialog.value = false
                    onExclude(text.value)
                }) { Text(stringResource(R.string.exclude_host_dialog_exclude)) }
            },
            dismissButton = {
                Button(onClick = {
                    showExcludeHostDialog.value = false
                }) { Text(stringResource(R.string.exclude_host_dialog_cancel)) }
            },
            title = { Text(stringResource(R.string.exclude_host_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.exclude_host_dialog_message),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = text.value,
                        onValueChange = { text.value = it }
                    )
                }
            }
        )
    }
}

@Composable
private fun TestConnectionDialog(showConnectionDialog: MutableState<Boolean>, onConnect: (String) -> Unit) {
    if (showConnectionDialog.value) {
        val text = remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showConnectionDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    showConnectionDialog.value = false
                    onConnect(text.value)
                }) { Text(stringResource(R.string.test_connection_dialog_connect)) }
            },
            dismissButton = {
                Button(onClick = {
                    showConnectionDialog.value = false
                }) { Text(stringResource(R.string.test_connection_dialog_cancel)) }
            },
            title = { Text(stringResource(R.string.test_connection_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.test_connection_dialog_message),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = text.value,
                        onValueChange = { text.value = it }
                    )
                }
            }
        )
    }
}
