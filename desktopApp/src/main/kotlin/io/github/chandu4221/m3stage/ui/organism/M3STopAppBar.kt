package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.chandu4221.m3stage.ui.theme.AppTheme

@Composable
fun M3TopAppBar() {
    TopAppBar(
        title = { Text("M3Stage") },
        navigationIcon = {
            Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = "M3Stage"
            )
        }
    )
}

@Composable
@Preview
fun M3TopAppBarPreview() {
    AppTheme {
        M3TopAppBar()
    }
}