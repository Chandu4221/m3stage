package io.github.chandu4221.m3stage.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.query.canPlace
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.flatten
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportState
import io.github.chandu4221.m3stage.state.EditorEvent
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.molecule.ExportDialog
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import io.github.chandu4221.m3stage.ui.template.EditorShellTemplate

/**
 * Smart Page: The ONLY stateful mediator between EditorStore and the dumb UI template.
 */
@Composable
fun EditorScreen(store: EditorStore) {
    val project by store.project.collectAsState()
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId
    val lockedNodeIds by store.lockedNodeIds
    val undoStack by store.undoStack.collectAsState()
    val redoStack by store.redoStack.collectAsState()
    val isDirty by store.isDirty.collectAsState()

    var activeRailTab by remember { mutableStateOf(StudioRailTab.Parts) }
    var searchQuery by remember { mutableStateOf("") }

    val activeScreen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val selectedNode = activeScreen?.root?.let { root ->
        selectedNodeId?.let { root.findNode(it) }
    }
    val effectiveTargetNode = selectedNode ?: activeScreen?.root
    val disallowedKinds = remember(effectiveTargetNode, activeScreen?.root) {
        val root = activeScreen?.root
        if (effectiveTargetNode == null || root == null) {
            emptySet()
        } else {
            ComponentKind.entries.filterNot { kind ->
                effectiveTargetNode.findNearestAcceptingAncestor(root, kind) != null
            }.toSet()
        }
    }

    val viewportController = io.github.chandu4221.m3stage.state.rememberCanvasViewportController()

    var showExportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Live Code Preview State
    var isCodePreviewOpen by remember { mutableStateOf(false) }
    var codePreviewContent by remember { mutableStateOf("") }
    var isGeneratingCode by remember { mutableStateOf(false) }

    // Drag-and-drop palette state (T3.6)
    var draggedComponentKind by remember { mutableStateOf<ComponentKind?>(null) }

    val activeScreenFileName = remember(activeScreen) {
        if (activeScreen != null) {
            "${io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer.toPascalCase(activeScreen.name)}.kt"
        } else {
            "Screen.kt"
        }
    }

    // Debounced code generation when code preview is open
    LaunchedEffect(isCodePreviewOpen, project, activeScreenId) {
        if (!isCodePreviewOpen) return@LaunchedEffect
        val currentProject = project ?: return@LaunchedEffect
        val currentActiveScreenId = activeScreenId ?: return@LaunchedEffect
        val targetScreen = currentProject.screens.firstOrNull { it.id == currentActiveScreenId } ?: return@LaunchedEffect

        isGeneratingCode = true
        kotlinx.coroutines.delay(500) // 500ms debounce
        try {
            val generatedProject = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                store.codeGenerator.generate(currentProject)
            }
            val pascalName = io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer.toPascalCase(targetScreen.name)
            val matchingFile = generatedProject.files.firstOrNull { it.fileName.startsWith(pascalName) }
                ?: generatedProject.files.firstOrNull()

            if (matchingFile != null) {
                codePreviewContent = matchingFile.content
            }
        } catch (e: Exception) {
            codePreviewContent = "// Generation error: ${e.message}"
        } finally {
            isGeneratingCode = false
        }
    }

    LaunchedEffect(Unit) {
        store.events.collect { event ->
            when (event) {
                is EditorEvent.ExportSuccess -> {
                    snackbarHostState.showSnackbar("Exported ZIP to: ${event.path}")
                }

                is EditorEvent.ExportFailed -> {
                    snackbarHostState.showSnackbar("Export failed: ${event.message}")
                }

                is EditorEvent.LoadFailed -> {
                    snackbarHostState.showSnackbar(
                        message = "Failed to load project: ${event.reason}",
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }

                is EditorEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }

                else -> {}
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            initialPackageName = project?.basePackage ?: "com.example.app",
            screens = project?.screens ?: emptyList(),
            onDismiss = { showExportDialog = false },
            onConfirm = { customPackage ->
                showExportDialog = false
                store.exportCode(customPackage)
            }
        )
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                // 1. Undo / Redo
                if (keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Z) {
                    if (undoStack.isNotEmpty() || redoStack.isNotEmpty()) {
                        store.redo()
                        return@onPreviewKeyEvent true
                    }
                } else if (keyEvent.isCtrlPressed && keyEvent.key == Key.Z) {
                    if (undoStack.isNotEmpty()) {
                        store.undo()
                        return@onPreviewKeyEvent true
                    }
                }

                // 2. Duplicate: Ctrl+D
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.D) {
                    val currentSelected = selectedNodeId
                    if (currentSelected != null) {
                        store.duplicateNode(currentSelected)
                        return@onPreviewKeyEvent true
                    }
                }

                // 2.5. Save: Ctrl+S
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
                    store.saveProject()
                    return@onPreviewKeyEvent true
                }

                // 2.6. Zoom: Ctrl+(+/-/0)
                if (keyEvent.isCtrlPressed) {
                    if (keyEvent.key == Key.Equals || keyEvent.key == Key.Plus || keyEvent.key == Key.NumPadAdd) {
                        viewportController.zoomIn()
                        return@onPreviewKeyEvent true
                    } else if (keyEvent.key == Key.Minus || keyEvent.key == Key.NumPadSubtract) {
                        viewportController.zoomOut()
                        return@onPreviewKeyEvent true
                    } else if (keyEvent.key == Key.Zero || keyEvent.key == Key.NumPad0) {
                        viewportController.resetZoom()
                        return@onPreviewKeyEvent true
                    }
                }

                // 3. Escape: cancel drag if dragging, else pop anchor / deselect
                if (keyEvent.key == Key.Escape) {
                    if (draggedComponentKind != null) {
                        draggedComponentKind = null
                        return@onPreviewKeyEvent true
                    }
                    val root = activeScreen?.root
                    if (root != null) {
                        val treeInfo = root.toTreeInfo()
                        store.popSelectionAnchor(treeInfo, root.id)
                    } else {
                        store.selectNode(null)
                    }
                    return@onPreviewKeyEvent true
                }

                // 4. Delete / Backspace: remove node
                if (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace) {
                    val currentSelected = selectedNodeId
                    if (currentSelected != null) {
                        store.removeNode(currentSelected)
                        return@onPreviewKeyEvent true
                    }
                }

                // 5. Arrow Up / Down in Layers tree
                if (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.DirectionDown) {
                    val screen = activeScreen
                    if (screen != null) {
                        val allNodes = screen.root.flatten().filter { it.isVisible }
                        if (allNodes.isNotEmpty()) {
                            val currentIndex = allNodes.indexOfFirst { it.id == selectedNodeId }
                            val newIndex = if (keyEvent.key == Key.DirectionUp) {
                                if (currentIndex <= 0) allNodes.lastIndex else currentIndex - 1
                            } else {
                                if (currentIndex == -1 || currentIndex >= allNodes.lastIndex) 0 else currentIndex + 1
                            }
                            store.selectNode(allNodes[newIndex].id)
                            return@onPreviewKeyEvent true
                        }
                    }
                }

                // 6. Tool Shortcuts (V: Select, H: Pan)
                if (!keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isMetaPressed) {
                    if (keyEvent.key == Key.V) {
                        viewportController.setActiveTool(CanvasPointerTool.Select)
                        return@onPreviewKeyEvent true
                    } else if (keyEvent.key == Key.H) {
                        viewportController.setActiveTool(CanvasPointerTool.Pan)
                        return@onPreviewKeyEvent true
                    }
                }

                false
            }
    ) {
        EditorShellTemplate(
            project = project,
            // Rail
            activeRailTab = activeRailTab,
            onRailTabSelected = { activeRailTab = it },
            isDarkMode = project?.isDarkMode ?: false,
            onToggleDarkMode = { store.toggleDarkMode() },

            // Parts Drawer
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onComponentSelected = { kind ->
                val active = activeScreen
                if (active == null) {
                    store.notifyUser("No active screen. Create or select a screen first.")
                    return@EditorShellTemplate
                }
                val startNode = (selectedNodeId?.let { active.root.findNode(it) }) ?: active.root
                val targetAncestor = startNode.findNearestAcceptingAncestor(active.root, kind)
                if (targetAncestor != null) {
                    store.addNodeToActiveScreen(targetAncestor.id, kind)
                } else {
                    store.addNodeToActiveScreen(startNode.id, kind)
                }
            },
            disallowedKinds = disallowedKinds,
            draggedComponentKind = draggedComponentKind,
            onComponentDragStart = { kind -> draggedComponentKind = kind },
            onComponentDragEnd = { draggedComponentKind = null },
            onDropOnScreen = { screenId, kind ->
                store.setActiveScreen(screenId)
                val targetScreen = project?.screens?.firstOrNull { it.id == screenId }
                if (targetScreen != null) {
                    val targetNode = (selectedNodeId?.let { targetScreen.root.findNode(it) }) ?: targetScreen.root
                    val ancestor = targetNode.findNearestAcceptingAncestor(targetScreen.root, kind) ?: targetScreen.root
                    store.addNodeToActiveScreen(ancestor.id, kind)
                }
                draggedComponentKind = null
            },

            // Floating Canvas
            screens = project?.screens ?: emptyList(),
            activeScreenId = activeScreenId,
            onSelectScreen = { store.setActiveScreen(it) },
            selectedNodeId = selectedNodeId,
            onNodeClick = { nodeId ->
                store.selectNode(nodeId)
                if (nodeId != null) {
                    val root = activeScreen?.root
                    if (root != null) {
                        val treeInfo = root.toTreeInfo()
                        val parentId = treeInfo[nodeId]?.parent ?: root.id
                        store.setDrillAnchor(parentId)
                    }
                }
            },
            lockedNodeIds = lockedNodeIds,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onUndo = { store.undo() },
            onRedo = { store.redo() },
            onAddScreen = {
                val count = (project?.screens?.size ?: 0) + 1
                store.addNewScreen("Screen$count", "/screen$count")
                // Automatically pan canvas to reveal the newly added screen on the right!
                val newlyAddedScreen = store.project.value?.screens?.lastOrNull()
                val targetX = newlyAddedScreen?.canvasX ?: 0f
                val currentPan = viewportController.viewportState.value.panOffset
                viewportController.panBy(Offset(-targetX - currentPan.x, 0f))
            },
            onExportCode = { showExportDialog = true },
            isCodePreviewOpen = isCodePreviewOpen,
            codePreviewFileName = activeScreenFileName,
            codePreviewContent = codePreviewContent,
            isGeneratingCode = isGeneratingCode,
            onToggleCodePreview = { isCodePreviewOpen = !isCodePreviewOpen },
            // Inspector & Tree Actions
            selectedNode = selectedNode,
            isNodeLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
            onToggleNodeLock = { nodeId -> store.toggleLock(nodeId) },
            onToggleVisibility = { nodeId -> store.toggleVisibility(nodeId) },
            onDeleteNode = { nodeId -> store.removeNode(nodeId) },
            viewportController = viewportController,
            store = store,
        )

        // Add this right here, inside the Box after EditorShellTemplate:
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}