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

package com.appmattus.certificatetransparency.sampleapp.item

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.appmattus.certificatetransparency.sampleapp.R
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "LongMethod")
@Composable
fun ExampleCardItem(
    scaffoldState: ScaffoldState,
    title: String,
    moreInfoUri: Uri?,
    onKotlinClick: () -> Unit,
    onJavaClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(shape = RoundedCornerShape(2.dp), modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                if (moreInfoUri != null) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                launchUri(
                                    context = context,
                                    scaffoldState = scaffoldState,
                                    uri = moreInfoUri
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.open_in_new),
                            contentDescription = null,
                            Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(R.string.more_info))
                    }
                }
            }
            Row {
                OutlinedButton(onClick = onKotlinClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.kotlin),
                        contentDescription = null,
                        Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.kotlin))
                }
                if (onJavaClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onJavaClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.java),
                            contentDescription = null,
                            Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(R.string.java))
                    }
                }
            }
        }
    }
}

private suspend fun launchUri(context: Context, scaffoldState: ScaffoldState, uri: Uri) {
    try {
        val myIntent = Intent(Intent.ACTION_VIEW, uri)
        ContextCompat.startActivity(context, myIntent, Bundle())
    } catch (ignored: ActivityNotFoundException) {
        scaffoldState.snackbarHostState.showSnackbar("Unable to open external link")
    }
}

@Preview
@Composable
fun PreviewExampleCardItem() {
    ExampleCardItem(
        scaffoldState = rememberScaffoldState(),
        title = "OkHttp",
        moreInfoUri = "https://example.com/".toUri(),
        onKotlinClick = {},
        onJavaClick = {}
    )
}
