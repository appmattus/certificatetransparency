package com.appmattus.certificatetransparency.sampleapp.item

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appmattus.certificatetransparency.sampleapp.R

@Composable
fun OutlineButtonItem(title: String, @DrawableRes icon: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            Modifier.padding(end = 8.dp)
        )
        Text(text = title)
    }
}

@Composable
@Preview
fun PreviewOutlineButtonItem() {
    OutlineButtonItem("Include Host", R.drawable.plus, {})
}
