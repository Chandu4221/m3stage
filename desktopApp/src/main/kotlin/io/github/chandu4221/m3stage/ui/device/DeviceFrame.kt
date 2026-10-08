package io.github.chandu4221.m3stage.ui.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DeviceCategory
import io.github.chandu4221.m3stage.model.DevicePreset

enum class DeviceOrientation {
    Portrait,
    Landscape
}

/**
 * Realistic hardware device frame wrapper.
 * When showFrame is true: Renders outer metallic bezel, camera punch-hole, and home indicator.
 * When showFrame is false: Renders a clean bounded artboard with shadow.
 */
@Composable
fun DeviceFrame(
    preset: DevicePreset,
    orientation: DeviceOrientation,
    showFrame: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Determine effective screen dimensions based on orientation
    val screenWidth: Dp = if (orientation == DeviceOrientation.Portrait) preset.widthDp.dp else preset.heightDp.dp
    val screenHeight: Dp = if (orientation == DeviceOrientation.Portrait) preset.heightDp.dp else preset.widthDp.dp
    val cornerRadius: Dp = if (showFrame) preset.cornerRadiusDp.dp else 8.dp

    if (!showFrame || preset.category == DeviceCategory.Desktop) {
        // --- BARE ARTBOARD (Clean Studio Mode) ---
        Surface(
            modifier = modifier
                .size(width = screenWidth, height = screenHeight)
                .shadow(12.dp, RoundedCornerShape(cornerRadius)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    } else {
        // --- REALISTIC HARDWARE BEZEL ---
        val bezelThickness = 12.dp
        val bezelRadius = cornerRadius + bezelThickness
        val bezelColor = MaterialTheme.colorScheme.inverseSurface

        Surface(
            modifier = modifier
                .size(
                    width = screenWidth + (bezelThickness * 2),
                    height = screenHeight + (bezelThickness * 2)
                )
                .shadow(16.dp, RoundedCornerShape(bezelRadius)),
            color = bezelColor,
            shape = RoundedCornerShape(bezelRadius),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bezelThickness)
            ) {
                // The actual phone screen viewport
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()

                        // Camera Punch-Hole Cutout (Top-Center)
                        if (preset.hasCameraNotch && orientation == DeviceOrientation.Portrait) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .size(12.dp)
                                    .background(Color.Black, CircleShape)
                            )
                        }

                        // Bottom Gesture Bar (Home indicator pill)
                        if (preset.category == DeviceCategory.Phone) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                                    .size(width = 72.dp, height = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}