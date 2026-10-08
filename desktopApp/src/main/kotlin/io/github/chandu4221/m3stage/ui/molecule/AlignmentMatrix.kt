package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.atom.DotMatrixCell
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

enum class AlignmentPosition {
    TopStart, TopCenter, TopEnd,
    CenterStart, Center, CenterEnd,
    BottomStart, BottomCenter, BottomEnd
}

/**
 * Dumb Molecule: 3x3 interactive matrix for selecting container alignments.
 */
@Composable
fun AlignmentMatrix(
    selectedPosition: AlignmentPosition,
    onPositionSelected: (AlignmentPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf(AlignmentPosition.TopStart, AlignmentPosition.TopCenter, AlignmentPosition.TopEnd),
                listOf(AlignmentPosition.CenterStart, AlignmentPosition.Center, AlignmentPosition.CenterEnd),
                listOf(AlignmentPosition.BottomStart, AlignmentPosition.BottomCenter, AlignmentPosition.BottomEnd)
            )

            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { pos ->
                        DotMatrixCell(
                            isSelected = pos == selectedPosition,
                            onClick = { onPositionSelected(pos) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlignmentMatrixPreview() {
    DualThemePreview {
        var pos by remember { mutableStateOf(AlignmentPosition.Center) }
        AlignmentMatrix(
            selectedPosition = pos,
            onPositionSelected = { pos = it }
        )
    }
}