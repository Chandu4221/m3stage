package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Draggable vertical splitter divider separating studio panels from the canvas.
 * Delegates to [PanelResizeHandle] for unified gesture handling, keyboard accessibility,
 * and double-click reset.
 */
@Composable
fun PanelSplitter(
    onDragDelta: (Float) -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    side: PanelResizeSide = PanelResizeSide.Left
) {
    val density = LocalDensity.current
    PanelResizeHandle(
        side = side,
        onDragStart = {},
        onDragDelta = { deltaDp ->
            val deltaPx = with(density) { deltaDp.toPx() }
            onDragDelta(deltaPx)
        },
        onDragEnd = {},
        onDoubleClick = onDoubleClick,
        modifier = modifier
    )
}
