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
    artboardOrigin: () -> Offset,
    zoomProvider: () -> Float = { 1f }
): Modifier = this.onGloballyPositioned { coords ->
    if (!coords.isAttached) return@onGloballyPositioned
    // Ancestor graphicsLayer (parent canvas zoom) scales positionInRoot(); artboardOrigin
    // subtraction cancels pan but NOT zoom, so divide the local delta back to unscaled px.
    // IMPORTANT INVARIANT: this correction assumes the ONLY graphicsLayer between this node
    // and the artboard origin box is the parent-canvas zoom/pan layer. If any scaled layer
    // is ever introduced between them (e.g., a scaled preview surface), this reporter must
    // be updated accordingly.
    val zoom = zoomProvider().coerceAtLeast(0.01f)
    val topLeftInArtboard = (coords.positionInRoot() - artboardOrigin()) / zoom
    val size = coords.size.toSize() // layout size is NOT layer-scaled — do not divide
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
