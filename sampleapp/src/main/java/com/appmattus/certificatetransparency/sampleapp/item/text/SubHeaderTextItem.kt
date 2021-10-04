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

package com.appmattus.certificatetransparency.sampleapp.item.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appmattus.certificatetransparency.sampleapp.R

@Composable
fun SubHeaderTextItem(title: String, modifier: Modifier = Modifier, @DrawableRes icon: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        icon?.let {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
        }

        Text(text = title, style = MaterialTheme.typography.h6, modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
fun PreviewSubHeaderTextItem() {
    Row {
        SubHeaderTextItem(
            title = "Certificate Transparency",
            icon = R.drawable.ic_launcher_foreground
        )
        Spacer(modifier = Modifier.height(16.dp))
        SubHeaderTextItem(
            title = "OkHttp Kotlin Example"
        )
    }
}
