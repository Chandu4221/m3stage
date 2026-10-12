package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Reports the on-screen bounds of a node in artboard coordinate space into [registry].
 */
fun Modifier.nodeBoundsReporter(
    nodeId: NodeId,
    registry: NodeBoundsRegistry,
    artboardOrigin: () -> Offset
): Modifier = this.onGloballyPositioned { coords ->
    if (!coords.isAttached) return@onGloballyPositioned
    val origin = artboardOrigin()
    val topLeftInArtboard = coords.positionInRoot() - origin
    val size = coords.size.toSize()
    registry.report(
        nodeId,
        Rect(
            left = topLeftInArtboard.x,
            top = topLeftInArtboard.y,
            right = topLeftInArtboard.x + size.width,
            bottom = topLeftInArtboard.y + size.height
        )
    )
}
