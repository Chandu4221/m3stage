package io.github.chandu4221.m3stage.ui.canvas

import androidx.compose.ui.input.pointer.PointerIcon
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import java.awt.Cursor

/**
 * Predefined system cursor overrides that take precedence over the base tool cursor.
 */
enum class CanvasCursorOverride {
    None,
    Panning,
    ClickableHover,
    Wait,
    ResizeNorth,
    ResizeSouth,
    ResizeEast,
    ResizeWest,
    ResizeNorthEast,
    ResizeNorthWest,
    ResizeSouthEast,
    ResizeSouthWest
}

/**
 * Single source of truth for mapping studio tools and interaction overrides
 * to standard system-provided cursors via [java.awt.Cursor.getPredefinedCursor].
 *
 * Guarantees:
 * - Zero external assets or downloaded files.
 * - Reuses cached JVM/AWT singleton instances.
 * - Strict precedence: Overrides (panning, handles) > Hovered Elements > Active Tool.
 */
object ToolCursorMapper {

    private val moveCursor by lazy {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
    }
    private val defaultCursor = PointerIcon.Default
    private val handCursor by lazy {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    }
    private val crosshairCursor by lazy {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
    }
    private val textCursor by lazy {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
    }
    private val waitCursor by lazy {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
    }

    // Directional resize cursors
    private val nResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)) }
    private val sResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)) }
    private val eResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) }
    private val wResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)) }
    private val neResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)) }
    private val nwResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)) }
    private val seResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)) }
    private val swResizeCursor by lazy { PointerIcon(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)) }

    /**
     * Resolves the effective system cursor icon for the canvas.
     */
    fun resolveCursor(
        activeTool: CanvasPointerTool,
        override: CanvasCursorOverride = CanvasCursorOverride.None
    ): PointerIcon {
        return when (override) {
            CanvasCursorOverride.Panning -> moveCursor
            CanvasCursorOverride.ClickableHover -> handCursor
            CanvasCursorOverride.Wait -> waitCursor
            CanvasCursorOverride.ResizeNorth -> nResizeCursor
            CanvasCursorOverride.ResizeSouth -> sResizeCursor
            CanvasCursorOverride.ResizeEast -> eResizeCursor
            CanvasCursorOverride.ResizeWest -> wResizeCursor
            CanvasCursorOverride.ResizeNorthEast -> neResizeCursor
            CanvasCursorOverride.ResizeNorthWest -> nwResizeCursor
            CanvasCursorOverride.ResizeSouthEast -> seResizeCursor
            CanvasCursorOverride.ResizeSouthWest -> swResizeCursor
            CanvasCursorOverride.None -> toolCursor(activeTool)
        }
    }

    private fun toolCursor(tool: CanvasPointerTool): PointerIcon {
        return when (tool) {
            CanvasPointerTool.Pan -> moveCursor
            CanvasPointerTool.Select -> defaultCursor
        }
    }
}
