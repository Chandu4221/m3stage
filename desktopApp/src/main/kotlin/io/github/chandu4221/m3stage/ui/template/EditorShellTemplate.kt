package io.github.chandu4221.m3stage.ui.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.state.CodeGenUiState
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.state.ShellPanelsController
import io.github.chandu4221.m3stage.ui.atom.PanelResizeHandle
import io.github.chandu4221.m3stage.ui.atom.PanelResizeSide
import io.github.chandu4221.m3stage.ui.organism.CodePreviewPanel
import io.github.chandu4221.m3stage.ui.organism.FloatingCanvasStudio
import io.github.chandu4221.m3stage.ui.organism.InspectorPanel
import io.github.chandu4221.m3stage.ui.organism.LayersTreeDrawer
import io.github.chandu4221.m3stage.ui.organism.PartsDrawer
import io.github.chandu4221.m3stage.ui.organism.StudioNavRail
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import io.github.chandu4221.m3stage.ui.organism.ThemeDrawer

/**
 * Dumb UI template for the editor shell layout.
 *
 * Sizing of panels is scope-isolated to [panels] ([ShellPanelsController]), ensuring that resizing
 * any panel only invalidates the panel's own Box and handle, never recomposing the central canvas
 * or parent screen. Content is preserved when collapsed using zero-sized clipping.
 */
@Composable
fun EditorShellTemplate(
    project: Project?,
    panels: ShellPanelsController,
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
    onAddScreen: () -> Unit,
    onExportCode: () -> Unit,
    // Code preview state
    codeGenState: kotlinx.coroutines.flow.StateFlow<CodeGenUiState>,
    // Inspector & Tree Actions
    isNodeLocked: Boolean,
    viewportController: CanvasViewportController,
    store: EditorStore,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Far-Left Studio Navigation Rail
        StudioNavRail(
            activeTab = panels.activeRailTab,
            onTabSelected = { panels.selectRailTab(it) }
        )

        // 2. Expandable & Resizable Left Drawer (Content-Preserving)
        val isLeftCollapsed = panels.left.collapsed || panels.activeRailTab == null
        Box(
            modifier = if (isLeftCollapsed) {
                Modifier.size(0.dp).clipToBounds()
            } else {
                Modifier
                    .width(panels.left.sizeDp.dp)
                    .fillMaxHeight()
                    .clipToBounds()
            }
        ) {
            val currentTab = panels.activeRailTab ?: panels.lastActiveRailTab
            val drawerModifier = Modifier.fillMaxSize()

            when (currentTab) {
                StudioRailTab.Layers -> {
                    val activeScreen = screens.firstOrNull { it.id == activeScreenId }
                    LayersTreeDrawer(
                        activeScreen = activeScreen,
                        selectedNodeId = selectedNodeId,
                        lockedNodeIds = lockedNodeIds,
                        onSelectNode = onNodeClick,
                        onToggleVisibility = { store.toggleVisibility(it) },
                        onToggleLock = { store.toggleLock(it) },
                        onDeleteNode = { store.removeNode(it) },
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
                        onUpdateProjectMetadata = { name, pkg -> store.updateProjectMetadata(name, pkg) },
                        disallowedKinds = disallowedKinds,
                        onComponentDragStart = onComponentDragStart,
                        onComponentDragEnd = onComponentDragEnd,
                        modifier = drawerModifier
                    )
                }
            }
        }

        // Left Splitter
        if (!isLeftCollapsed) {
            PanelResizeHandle(
                slot = panels.left,
                side = PanelResizeSide.Left
            )
        }

        // 3. Central Canvas & Bottom Code Preview
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
                onUndo = { store.undo() },
                onRedo = { store.redo() },
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
                isCodePreviewOpen = panels.isBottomOpen,
                onToggleCodePreview = { panels.toggleBottom() },
                draggedComponentKind = draggedComponentKind,
                onDropOnScreen = onDropOnScreen,
                isLeftPanelOpen = !isLeftCollapsed,
                onToggleLeftPanel = { panels.toggleLeft() },
                isRightPanelOpen = !panels.right.collapsed,
                onToggleRightPanel = { panels.toggleRight() },
                isFullWidth = panels.isFullWidth,
                onToggleFullWidth = { panels.toggleFullWidth() }
            )

            // Content-Preserving Bottom Code Preview
            val isBottomCollapsed = panels.bottom.collapsed
            Box(
                modifier = if (isBottomCollapsed) {
                    Modifier.size(0.dp).clipToBounds()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clipToBounds()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panels.bottom.sizeDp.dp)
                ) {
                    PanelResizeHandle(
                        slot = panels.bottom,
                        side = PanelResizeSide.Bottom
                    )

                    val previewState by codeGenState.collectAsState()
                    val (fileName, code, isGenerating) = when (val state = previewState) {
                        is CodeGenUiState.Idle -> Triple("Screen.kt", "", false)
                        is CodeGenUiState.Generating -> Triple(state.fileName ?: "Screen.kt", state.content ?: "", true)
                        is CodeGenUiState.Ready -> Triple(state.fileName, state.content, false)
                        is CodeGenUiState.Error -> Triple("Screen.kt", "// Generation error: ${state.message}", false)
                    }

                    CodePreviewPanel(
                        fileName = fileName,
                        code = code,
                        isGenerating = isGenerating,
                        onClose = { panels.toggleBottom() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        // 4. Right Inspector Surface & Splitter
        if (!panels.right.collapsed) {
            PanelResizeHandle(
                slot = panels.right,
                side = PanelResizeSide.Right
            )
        }

        Box(
            modifier = if (panels.right.collapsed) {
                Modifier.size(0.dp).clipToBounds()
            } else {
                Modifier
                    .width(panels.right.sizeDp.dp)
                    .fillMaxHeight()
                    .clipToBounds()
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
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