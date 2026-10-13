package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.ui.canvas.CanvasWorkbench

/**
 * High-level organism entrypoint for the transformable studio canvas.
 * Delegates directly to [CanvasWorkbench] in the two-canvas architecture.
 */
@Composable
fun CanvasPanel(
    screens: List<Screen>,
    activeScreenId: ScreenId?,
    onSelectScreen: (ScreenId?) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId?) -> Unit,
    viewportController: CanvasViewportController,
    lockedNodeIds: Set<NodeId> = emptySet(),
    projectDefaultDevice: DevicePreset = DevicePreset.Default,
    onRenameScreen: (ScreenId, String) -> Unit = { _, _ -> },
    onDeleteScreen: (ScreenId) -> Unit = {},
    onDuplicateScreen: (ScreenId) -> Unit = {},
    onMoveScreen: (ScreenId, Float, Float) -> Unit = { _, _, _ -> },
    onReorderScreens: (Int, Int) -> Unit = { _, _ -> },
    onDeviceChange: (ScreenId, DevicePreset) -> Unit = { _, _ -> },
    onOrientationToggle: (ScreenId) -> Unit = {},
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    CanvasWorkbench(
        screens = screens,
        activeScreenId = activeScreenId,
        onSelectScreen = onSelectScreen,
        selectedNodeId = selectedNodeId,
        onNodeClick = onNodeClick,
        lockedNodeIds = lockedNodeIds,
        viewportController = viewportController,
        projectDefaultDevice = projectDefaultDevice,
        canMoveLeft = { it > 0 },
        canMoveRight = { it < screens.lastIndex },
        onMoveLeft = { onReorderScreens(it, it - 1) },
        onMoveRight = { onReorderScreens(it, it + 1) },
        onRenameScreen = onRenameScreen,
        onDeleteScreen = onDeleteScreen,
        onDuplicateScreen = onDuplicateScreen,
        onMoveScreen = onMoveScreen,
        onDeviceChange = onDeviceChange,
        onOrientationToggle = onOrientationToggle,
        draggedComponentKind = draggedComponentKind,
        onDropOnScreen = onDropOnScreen,
        modifier = modifier
    )
}