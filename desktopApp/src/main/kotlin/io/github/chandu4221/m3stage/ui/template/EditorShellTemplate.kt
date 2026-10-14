package io.github.chandu4221.m3stage.ui.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.organism.InspectorPanel
import io.github.chandu4221.m3stage.ui.organism.FloatingCanvasStudio
import io.github.chandu4221.m3stage.ui.organism.LayersTreeDrawer
import io.github.chandu4221.m3stage.ui.organism.PartsDrawer
import io.github.chandu4221.m3stage.ui.organism.StudioNavRail
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import io.github.chandu4221.m3stage.ui.atom.PanelResizeHandle
import io.github.chandu4221.m3stage.ui.atom.PanelResizeSide
import io.github.chandu4221.m3stage.ui.atom.PanelSplitter
import io.github.chandu4221.m3stage.ui.organism.ThemeDrawer
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

@Composable
fun EditorShellTemplate(
    project: Project?,
    // Rail state
    activeRailTab: StudioRailTab?,
    onRailTabSelected: (StudioRailTab) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    // Panel collapse & resize state
    isLeftPanelOpen: Boolean = activeRailTab != null,
    onToggleLeftPanel: () -> Unit = {},
    isRightPanelOpen: Boolean = true,
    onToggleRightPanel: () -> Unit = {},
    isFullWidth: Boolean = false,
    onToggleFullWidth: () -> Unit = {},
    leftPanelWidth: Dp = 280.dp,
    onLeftDragStart: () -> Unit = {},
    onLeftDragDelta: (Dp) -> Unit = {},
    onLeftDragEnd: () -> Unit = {},
    onLeftPanelWidthChange: (Dp) -> Unit = {},
    onResetLeftPanelWidth: () -> Unit = {},
    onAdjustLeftWidth: (Dp) -> Unit = {},
    onSnapLeftToMin: () -> Unit = {},
    rightPanelWidth: Dp = 320.dp,
    onRightDragStart: () -> Unit = {},
    onRightDragDelta: (Dp) -> Unit = {},
    onRightDragEnd: () -> Unit = {},
    onRightPanelWidthChange: (Dp) -> Unit = {},
    onResetRightPanelWidth: () -> Unit = {},
    onAdjustRightWidth: (Dp) -> Unit = {},
    onSnapRightToMin: () -> Unit = {},
    // Project metadata state
    onUpdateProjectMetadata: (String, String) -> Unit = { _, _ -> },
    // Drawer state
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onComponentSelected: (ComponentKind) -> Unit,
    disallowedKinds: Set<ComponentKind> = emptySet(),
    draggedComponentKind: ComponentKind? = null,
    onComponentDragStart: (ComponentKind) -> Unit = {},
    onComponentDragEnd: () -> Unit = {},
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    // Canvas state
    screens: List<Screen>,
    activeScreenId: ScreenId?,
    onSelectScreen: (ScreenId?) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId?) -> Unit,
    lockedNodeIds: Set<NodeId>,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAddScreen: () -> Unit,
    onExportCode: () -> Unit,
    // Code preview state
    isCodePreviewOpen: Boolean = false,
    codePreviewFileName: String = "",
    codePreviewContent: String = "",
    isGeneratingCode: Boolean = false,
    onToggleCodePreview: () -> Unit = {},
    bottomPanelHeight: Dp = 280.dp,
    onBottomDragStart: () -> Unit = {},
    onBottomDragDelta: (Dp) -> Unit = {},
    onBottomDragEnd: () -> Unit = {},
    onBottomPanelHeightChange: (Dp) -> Unit = {},
    onResetBottomPanelHeight: () -> Unit = {},
    onAdjustBottomHeight: (Dp) -> Unit = {},
    onSnapBottomToMin: () -> Unit = {},
    // Inspector state
    selectedNode: DesignNode?,
    isNodeLocked: Boolean,
    onToggleNodeLock: (NodeId) -> Unit,
    onToggleVisibility: (NodeId) -> Unit = {},
    onDeleteNode: (NodeId) -> Unit = {},
    viewportController: CanvasViewportController,
    store: EditorStore,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Far-Left Studio Navigation Rail
        StudioNavRail(
            activeTab = activeRailTab,
            onTabSelected = onRailTabSelected
        )

        // 2. Expandable & Resizable Drawer
        if (isLeftPanelOpen && activeRailTab != null) {
            if (leftPanelWidth > 0.dp) {
                val drawerModifier = Modifier
                    .width(leftPanelWidth)
                    .fillMaxHeight()

                when (activeRailTab) {
                    StudioRailTab.Layers -> {
                        val activeScreen = screens.firstOrNull { it.id == activeScreenId }
                        LayersTreeDrawer(
                            activeScreen = activeScreen,
                            selectedNodeId = selectedNodeId,
                            lockedNodeIds = lockedNodeIds,
                            onSelectNode = onNodeClick,
                            onToggleVisibility = onToggleVisibility,
                            onToggleLock = onToggleNodeLock,
                            onDeleteNode = onDeleteNode,
                            modifier = drawerModifier
                        )
                    }
                    StudioRailTab.Theme -> {
                        ThemeDrawer(
                            currentSeedColor = project?.seedColor ?: 0xFF6750A4L,
                            isDarkMode = isDarkMode,
                            onSelectSeedColor = { store.updateSeedColor(it) },
                            onToggleDarkMode = onToggleDarkMode,
                            modifier = drawerModifier
                        )
                    }
                    else -> {
                        PartsDrawer(
                            searchQuery = searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            onComponentSelected = onComponentSelected,
                            projectName = project?.name ?: "Untitled",
                            basePackage = project?.basePackage ?: "com.example.app",
                            onUpdateProjectMetadata = onUpdateProjectMetadata,
                            disallowedKinds = disallowedKinds,
                            onComponentDragStart = onComponentDragStart,
                            onComponentDragEnd = onComponentDragEnd,
                            modifier = drawerModifier
                        )
                    }
                }
            }

            // Left Splitter
            PanelResizeHandle(
                side = PanelResizeSide.Left,
                onDragStart = onLeftDragStart,
                onDragDelta = { deltaDp ->
                    onLeftDragDelta(deltaDp)
                    onLeftPanelWidthChange(leftPanelWidth + deltaDp)
                },
                onDragEnd = onLeftDragEnd,
                onDoubleClick = onResetLeftPanelWidth,
                onAdjustWidth = onAdjustLeftWidth,
                onSnapToMin = onSnapLeftToMin,
                onResetToDefault = onResetLeftPanelWidth
            )
        }

        // 3. Central Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            FloatingCanvasStudio(
                screens = screens,
                activeScreenId = activeScreenId,
                onSelectScreen = onSelectScreen,
                selectedNodeId = selectedNodeId,
                onNodeClick = onNodeClick,
                lockedNodeIds = lockedNodeIds,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onAddScreen = onAddScreen,
                onExportCode = onExportCode,
                viewportController = viewportController,
                projectName = project?.name,
                isDirty = store.isDirty.value,
                onSaveProject = { store.saveProject() },
                onRenameScreen = { id, name -> store.renameScreen(id, name) },
                onDeleteScreen = { store.deleteScreen(it) },
                onDuplicateScreen = { store.duplicateScreen(it) },
                onMoveScreen = { id, x, y -> store.moveScreen(id, x, y) },
                onTidy = { store.tidyScreens() },
                onReorderScreens = { from, to -> store.reorderScreens(from, to) },
                onDeviceChange = { id, device -> store.updateScreenDevice(id, device) },
                onOrientationToggle = { id -> store.toggleScreenOrientation(id) },
                isCodePreviewOpen = isCodePreviewOpen,
                onToggleCodePreview = onToggleCodePreview,
                draggedComponentKind = draggedComponentKind,
                onDropOnScreen = onDropOnScreen,
                isLeftPanelOpen = isLeftPanelOpen,
                onToggleLeftPanel = onToggleLeftPanel,
                isRightPanelOpen = isRightPanelOpen,
                onToggleRightPanel = onToggleRightPanel,
                isFullWidth = isFullWidth,
                onToggleFullWidth = onToggleFullWidth
            )

            if (isCodePreviewOpen && bottomPanelHeight > 0.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                ) {
                    PanelResizeHandle(
                        side = PanelResizeSide.Bottom,
                        onDragStart = onBottomDragStart,
                        onDragDelta = { deltaDp ->
                            onBottomDragDelta(deltaDp)
                            onBottomPanelHeightChange(bottomPanelHeight + deltaDp)
                        },
                        onDragEnd = onBottomDragEnd,
                        onDoubleClick = onResetBottomPanelHeight,
                        onAdjustWidth = onAdjustBottomHeight,
                        onSnapToMin = onSnapBottomToMin,
                        onResetToDefault = onResetBottomPanelHeight
                    )

                    io.github.chandu4221.m3stage.ui.organism.CodePreviewPanel(
                        fileName = codePreviewFileName,
                        code = codePreviewContent,
                        isGenerating = isGeneratingCode,
                        onClose = onToggleCodePreview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bottomPanelHeight)
                    )
                }
            }
        }

        // 4. Right Inspector Surface & Splitter
        if (isRightPanelOpen) {
            // Right Splitter
            PanelResizeHandle(
                side = PanelResizeSide.Right,
                onDragStart = onRightDragStart,
                onDragDelta = { deltaDp ->
                    onRightDragDelta(deltaDp)
                    onRightPanelWidthChange(rightPanelWidth + deltaDp)
                },
                onDragEnd = onRightDragEnd,
                onDoubleClick = onResetRightPanelWidth,
                onAdjustWidth = onAdjustRightWidth,
                onSnapToMin = onSnapRightToMin,
                onResetToDefault = onResetRightPanelWidth
            )

            if (rightPanelWidth > 0.dp) {
                Surface(
                    modifier = Modifier
                        .width(rightPanelWidth)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    InspectorPanel(
                        project = project,
                        activeScreenId = activeScreenId?.value,
                        selectedNodeId = selectedNodeId,
                        isLocked = isNodeLocked,
                        store = store
                    )
                }
            }
        }
    }
}