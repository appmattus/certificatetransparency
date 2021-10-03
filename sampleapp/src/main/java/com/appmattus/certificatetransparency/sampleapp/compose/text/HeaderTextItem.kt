package com.appmattus.certificatetransparency.sampleapp.compose.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
fun HeaderTextItem(title: String, @DrawableRes icon: Int, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
        )

        Text(text = title, style = MaterialTheme.typography.h4, modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
fun PreviewHeaderTextItem() {
    HeaderTextItem(
        title = "Certificate Transparency",
        icon = R.drawable.ic_launcher_foreground
    )
}
