package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.atom.M3SIcon
import io.github.chandu4221.m3stage.ui.theme.AppTheme

@Composable
fun M3SLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        M3SIcon(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(color = MaterialTheme.colorScheme.primary),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("M3Stage", style = MaterialTheme.typography.headlineMedium)
            Text("Visual Builder", style = MaterialTheme.typography.labelLarge)
        }
    }
}


@Preview
@Composable
private fun M3SLogoPreview() {
    AppTheme {
        M3SLogo()
    }
}