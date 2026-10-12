package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.theme.AppTheme

@Composable
fun M3SIcon(
    icon: ImageVector = Icons.Outlined.Widgets,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.padding(all = 8.dp).size(36.dp),
        tint = tint
    )
}

@Composable
@Preview
fun M3SIconPreview() {
    AppTheme {
        Column {
            M3SIcon()
            M3SIcon(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = MaterialTheme.colorScheme.primary),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}