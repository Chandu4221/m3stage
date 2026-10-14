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
import io.github.chandu4221.m3stage.ui.organism.ThemeDrawer
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

@Composable
fun EditorShellTemplate(
    project: Project?,
    // Rail state
    activeRailTab: StudioRailTab,
    onRailTabSelected: (StudioRailTab) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
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
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Far-Left Studio Navigation Rail
        StudioNavRail(
            activeTab = activeRailTab,
            onTabSelected = onRailTabSelected
        )

        // 2. Expandable Drawer
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
                    onDeleteNode = onDeleteNode
                )
            }
            StudioRailTab.Theme -> {
                ThemeDrawer(
                    currentSeedColor = project?.seedColor ?: 0xFF6750A4L,
                    isDarkMode = isDarkMode,
                    onSelectSeedColor = { store.updateSeedColor(it) },
                    onToggleDarkMode = onToggleDarkMode
                )
            }
            else -> {
                PartsDrawer(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onComponentSelected = onComponentSelected,
                    disallowedKinds = disallowedKinds,
                    onComponentDragStart = onComponentDragStart,
                    onComponentDragEnd = onComponentDragEnd
                )
            }
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
                onDropOnScreen = onDropOnScreen
            )

            if (isCodePreviewOpen) {
                io.github.chandu4221.m3stage.ui.organism.CodePreviewPanel(
                    fileName = codePreviewFileName,
                    code = codePreviewContent,
                    isGenerating = isGeneratingCode,
                    onClose = onToggleCodePreview,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                )
            }
        }

        // 4. Right Inspector Surface
        Surface(
            modifier = Modifier
                .width(320.dp)
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