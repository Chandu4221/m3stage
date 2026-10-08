package io.github.chandu4221.m3stage.model

enum class DeviceCategory(val displayName: String) {
    Phone("Phone"),
    Foldable("Foldable"),
    Tablet("Tablet"),
    Desktop("Desktop")
}

/**
 * Strongly-typed Device Preset for visual canvas viewports.
 * Canonical dimensions match official Android reference devices and Jetpack Compose Devices catalog.
 */
enum class DevicePreset(
    val id: String,
    val displayName: String,
    val category: DeviceCategory,
    val widthDp: Int,
    val heightDp: Int,
    val cornerRadiusDp: Int = 16,
    val hasCameraNotch: Boolean = true
) {
    // --- PHONES ---
    Pixel8(
        id = "pixel_8",
        displayName = "Pixel 8",
        category = DeviceCategory.Phone,
        widthDp = 412,
        heightDp = 892,
        cornerRadiusDp = 24,
        hasCameraNotch = true
    ),
    Pixel8Pro(
        id = "pixel_8_pro",
        displayName = "Pixel 8 Pro",
        category = DeviceCategory.Phone,
        widthDp = 412,
        heightDp = 915,
        cornerRadiusDp = 28,
        hasCameraNotch = true
    ),
    GalaxyS24(
        id = "galaxy_s24",
        displayName = "Galaxy S24",
        category = DeviceCategory.Phone,
        widthDp = 384,
        heightDp = 832,
        cornerRadiusDp = 18,
        hasCameraNotch = true
    ),
    GenericPhone(
        id = "generic_phone",
        displayName = "Medium Phone",
        category = DeviceCategory.Phone,
        widthDp = 360,
        heightDp = 800,
        cornerRadiusDp = 16,
        hasCameraNotch = false
    ),

    // --- FOLDABLES ---
    PixelFold(
        id = "pixel_fold",
        displayName = "Pixel Fold (Unfolded)",
        category = DeviceCategory.Foldable,
        widthDp = 840,
        heightDp = 700,
        cornerRadiusDp = 18,
        hasCameraNotch = true
    ),

    // --- TABLETS ---
    PixelTablet(
        id = "pixel_tablet",
        displayName = "Pixel Tablet",
        category = DeviceCategory.Tablet,
        widthDp = 1280,
        heightDp = 800,
        cornerRadiusDp = 20,
        hasCameraNotch = true
    ),
    GenericTablet(
        id = "generic_tablet",
        displayName = "Medium Tablet (7\")",
        category = DeviceCategory.Tablet,
        widthDp = 600,
        heightDp = 960,
        cornerRadiusDp = 12,
        hasCameraNotch = false
    ),

    // --- DESKTOP ---
    Desktop(
        id = "desktop",
        displayName = "Desktop (1080p)",
        category = DeviceCategory.Desktop,
        widthDp = 1280,
        heightDp = 720,
        cornerRadiusDp = 0,
        hasCameraNotch = false
    );

    companion object {
        val Default: DevicePreset = Pixel8

        fun fromId(id: String): DevicePreset =
            entries.firstOrNull { it.id == id } ?: Default
    }
}