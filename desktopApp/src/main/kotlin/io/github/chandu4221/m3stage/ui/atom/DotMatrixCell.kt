package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Atom: Single interactive cell for the 3x3 alignment matrix.
 */
@Composable
fun DotMatrixCell(
    isSelected: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentDescription: String = "Alignment cell"
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .semantics { role = Role.RadioButton }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 10.dp else 6.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
        )
    }
}


@Preview
@Composable
private fun DotMatrixCellPreview() {
    DualThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DotMatrixCell(isSelected = true)
            DotMatrixCell(isSelected = false)
        }
    }
}