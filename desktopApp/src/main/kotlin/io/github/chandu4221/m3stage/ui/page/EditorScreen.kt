package io.github.chandu4221.m3stage.ui.page

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.state.EditorEvent
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.state.rememberCanvasViewportController
import io.github.chandu4221.m3stage.state.rememberShellPanelsController
import io.github.chandu4221.m3stage.ui.molecule.ExportDialog
import io.github.chandu4221.m3stage.ui.template.EditorShellTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Smart Page: The ONLY stateful mediator between EditorStore and the dumb UI template.
 */
@Composable
fun EditorScreen(store: EditorStore) {
    val projectState = store.project.collectAsState()
    val project by projectState
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId
    val lockedNodeIds by store.lockedNodeIds
    val undoStack by store.undoStack.collectAsState()
    val redoStack by store.redoStack.collectAsState()

    val panels = rememberShellPanelsController()
    var searchQuery by remember { mutableStateOf("") }

    val activeScreen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val disallowedKinds by remember {
        derivedStateOf {
            computeDisallowedKinds(
                project = projectState.value,
                activeScreenId = store.activeScreenId.value,
                selectedNodeId = store.selectedNodeId.value
            )
        }
    }

    val viewportController = rememberCanvasViewportController()

    var showExportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Live Code Preview State
    val isCodePreviewOpen = panels.isBottomOpen
    var codePreviewContent by remember { mutableStateOf("") }
    var isGeneratingCode by remember { mutableStateOf(false) }

    // Drag-and-drop palette state (T3.6)
    var draggedComponentKind by remember { mutableStateOf<ComponentKind?>(null) }

    val activeScreenFileName = remember(activeScreen) {
        if (activeScreen != null) {
            "${KotlinIdentifierSanitizer.toPascalCase(activeScreen.name)}.kt"
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
        delay(500) // 500ms debounce
        try {
            val generatedProject = withContext(Dispatchers.Default) {
                store.codeGenerator.generate(currentProject)
            }
            val pascalName = KotlinIdentifierSanitizer.toPascalCase(targetScreen.name)
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
                is EditorEvent.ExportSuccess -> snackbarHostState.showSnackbar("Exported ZIP to: ${event.path}")
                is EditorEvent.ExportFailed -> snackbarHostState.showSnackbar("Export failed: ${event.message}")
                is EditorEvent.LoadFailed -> snackbarHostState.showSnackbar(
                    message = "Failed to load project: ${event.reason}",
                    duration = SnackbarDuration.Long
                )
                is EditorEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Long
                )
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

    val shortcutActions = remember(draggedComponentKind) {
        EditorShortcutActions(
            isPaletteDragging = { draggedComponentKind != null },
            onCancelPaletteDrag = { draggedComponentKind = null }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .editorKeyboardShortcuts(
                store = store,
                panels = panels,
                viewportController = viewportController,
                actions = shortcutActions
            )
    ) {
        LaunchedEffect(maxWidth, maxHeight) {
            panels.updateAvailableSpace(maxWidth.value, maxHeight.value)
        }

        EditorShellTemplate(
            project = project,
            panels = panels,
            isDarkMode = project?.isDarkMode ?: false,
            onToggleDarkMode = { store.toggleDarkMode() },
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
            onAddScreen = {
                val count = (project?.screens?.size ?: 0) + 1
                store.addNewScreen("Screen$count", "/screen$count")
                val newlyAddedScreen = store.project.value?.screens?.lastOrNull()
                val targetX = newlyAddedScreen?.canvasX ?: 0f
                val currentPan = viewportController.viewportState.value.panOffset
                viewportController.panBy(Offset(-targetX - currentPan.x, 0f))
            },
            onExportCode = { showExportDialog = true },
            codePreviewFileName = activeScreenFileName,
            codePreviewContent = codePreviewContent,
            isGeneratingCode = isGeneratingCode,
            isNodeLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
            viewportController = viewportController,
            store = store,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * Computes disallowed component kinds for palette tiles based on active screen and selection.
 */
fun computeDisallowedKinds(
    project: io.github.chandu4221.m3stage.model.Project?,
    activeScreenId: io.github.chandu4221.m3stage.model.ScreenId?,
    selectedNodeId: io.github.chandu4221.m3stage.model.NodeId?
): Set<ComponentKind> {
    val screen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val root = screen?.root ?: return emptySet()
    val targetNode = (selectedNodeId?.let { root.findNode(it) }) ?: root
    return ComponentKind.entries.filterNot { kind ->
        targetNode.findNearestAcceptingAncestor(root, kind) != null
    }.toSet()
}