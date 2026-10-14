package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Custom Material Symbols for workspace panel layout controls:
 * - LeftPanelOpen / LeftPanelClose
 * - RightPanelOpen / RightPanelClose
 * - WidthFull
 */
object StageIcons {

    val WidthFull: ImageVector = Icons.Default.WidthFull

    val LeftPanelClose: ImageVector
        get() {
            if (_leftPanelClose != null) return _leftPanelClose!!
            _leftPanelClose = materialIcon(name = "StageIcons.LeftPanelClose") {
                materialPath {
                    // Arrow pointing left (collapse left panel)
                    moveTo(16.5f, 8.5f)
                    lineTo(13.0f, 12.0f)
                    lineToRelative(3.5f, 3.5f)
                    lineToRelative(-1.4f, 1.4f)
                    lineTo(10.2f, 12.0f)
                    lineToRelative(4.9f, -4.9f)
                    close()

                    // Outer window frame
                    moveTo(20.0f, 4.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(16.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                    close()

                    // Left panel column
                    moveTo(7.0f, 18.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(12.0f)
                    close()

                    // Canvas area outline
                    moveTo(20.0f, 18.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(11.0f)
                    verticalLineToRelative(12.0f)
                    close()
                }
            }
            return _leftPanelClose!!
        }
    private var _leftPanelClose: ImageVector? = null

    val LeftPanelOpen: ImageVector
        get() {
            if (_leftPanelOpen != null) return _leftPanelOpen!!
            _leftPanelOpen = materialIcon(name = "StageIcons.LeftPanelOpen") {
                materialPath {
                    // Arrow pointing right (open left panel)
                    moveTo(12.5f, 8.5f)
                    lineTo(16.0f, 12.0f)
                    lineToRelative(-3.5f, 3.5f)
                    lineToRelative(1.4f, 1.4f)
                    lineToRelative(4.9f, -4.9f)
                    lineToRelative(-4.9f, -4.9f)
                    close()

                    // Outer window frame
                    moveTo(20.0f, 4.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(16.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                    close()

                    // Left panel column
                    moveTo(7.0f, 18.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(12.0f)
                    close()

                    // Canvas area outline
                    moveTo(20.0f, 18.0f)
                    horizontalLineTo(9.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(11.0f)
                    verticalLineToRelative(12.0f)
                    close()
                }
            }
            return _leftPanelOpen!!
        }
    private var _leftPanelOpen: ImageVector? = null

    val RightPanelClose: ImageVector
        get() {
            if (_rightPanelClose != null) return _rightPanelClose!!
            _rightPanelClose = materialIcon(name = "StageIcons.RightPanelClose") {
                materialPath {
                    // Arrow pointing right (collapse right panel)
                    moveTo(7.5f, 8.5f)
                    lineTo(11.0f, 12.0f)
                    lineToRelative(-3.5f, 3.5f)
                    lineToRelative(1.4f, 1.4f)
                    lineToRelative(4.9f, -4.9f)
                    lineToRelative(-4.9f, -4.9f)
                    close()

                    // Outer window frame
                    moveTo(20.0f, 4.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(16.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                    close()

                    // Canvas area outline
                    moveTo(15.0f, 18.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(11.0f)
                    verticalLineToRelative(12.0f)
                    close()

                    // Right panel column
                    moveTo(20.0f, 18.0f)
                    horizontalLineToRelative(-3.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(12.0f)
                    close()
                }
            }
            return _rightPanelClose!!
        }
    private var _rightPanelClose: ImageVector? = null

    val RightPanelOpen: ImageVector
        get() {
            if (_rightPanelOpen != null) return _rightPanelOpen!!
            _rightPanelOpen = materialIcon(name = "StageIcons.RightPanelOpen") {
                materialPath {
                    // Arrow pointing left (open right panel)
                    moveTo(11.5f, 8.5f)
                    lineTo(8.0f, 12.0f)
                    lineToRelative(3.5f, 3.5f)
                    lineToRelative(-1.4f, 1.4f)
                    lineTo(5.2f, 12.0f)
                    lineToRelative(4.9f, -4.9f)
                    close()

                    // Outer window frame
                    moveTo(20.0f, 4.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(16.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(6.0f)
                    curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                    close()

                    // Canvas area outline
                    moveTo(15.0f, 18.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(11.0f)
                    verticalLineToRelative(12.0f)
                    close()

                    // Right panel column
                    moveTo(20.0f, 18.0f)
                    horizontalLineToRelative(-3.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(12.0f)
                    close()
                }
            }
            return _rightPanelOpen!!
        }
    private var _rightPanelOpen: ImageVector? = null
}
