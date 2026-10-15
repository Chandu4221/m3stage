package io.github.chandu4221.m3stage.model

import kotlin.test.Test
import kotlin.test.assertEquals

class WindowSizeClassTest {

    @Test
    fun testWindowSizeClassResolverBoundaryMath() {
        // Compact < 600dp
        assertEquals(WindowSizeClass.Compact, WindowSizeClassResolver.fromWidthDp(0))
        assertEquals(WindowSizeClass.Compact, WindowSizeClassResolver.fromWidthDp(360))
        assertEquals(WindowSizeClass.Compact, WindowSizeClassResolver.fromWidthDp(412))
        assertEquals(WindowSizeClass.Compact, WindowSizeClassResolver.fromWidthDp(599))

        // Medium: 600dp <= width < 840dp
        assertEquals(WindowSizeClass.Medium, WindowSizeClassResolver.fromWidthDp(600))
        assertEquals(WindowSizeClass.Medium, WindowSizeClassResolver.fromWidthDp(601))
        assertEquals(WindowSizeClass.Medium, WindowSizeClassResolver.fromWidthDp(700))
        assertEquals(WindowSizeClass.Medium, WindowSizeClassResolver.fromWidthDp(800))
        assertEquals(WindowSizeClass.Medium, WindowSizeClassResolver.fromWidthDp(839))

        // Expanded: width >= 840dp
        assertEquals(WindowSizeClass.Expanded, WindowSizeClassResolver.fromWidthDp(840))
        assertEquals(WindowSizeClass.Expanded, WindowSizeClassResolver.fromWidthDp(841))
        assertEquals(WindowSizeClass.Expanded, WindowSizeClassResolver.fromWidthDp(1280))
        assertEquals(WindowSizeClass.Expanded, WindowSizeClassResolver.fromWidthDp(1920))
    }

    @Test
    fun testPortraitAndLandscapePresetsMapToExpectedClasses() {
        val rootNode = DesignNode(id = NodeId("root"), kind = io.github.chandu4221.m3stage.component.ComponentKind.Box)

        // Pixel 8 Portrait: 412x892 -> width = 412 -> Compact
        val pixel8Portrait = Screen(
            id = ScreenId("s1"),
            name = "Pixel8Screen",
            route = "/s1",
            root = rootNode,
            device = DevicePreset.Pixel8,
            orientation = DeviceOrientation.Portrait
        )
        val p8PortW = pixel8Portrait.resolveWidthDp(DevicePreset.Pixel8)
        assertEquals(412, p8PortW)
        assertEquals(WindowSizeClass.Compact, pixel8Portrait.effectiveWindowSizeClass(p8PortW))

        // Pixel 8 Landscape: 892x412 -> width = 892 -> Expanded
        val pixel8Landscape = pixel8Portrait.copy(orientation = DeviceOrientation.Landscape)
        val p8LandW = pixel8Landscape.resolveWidthDp(DevicePreset.Pixel8)
        assertEquals(892, p8LandW)
        assertEquals(WindowSizeClass.Expanded, pixel8Landscape.effectiveWindowSizeClass(p8LandW))

        // Generic Tablet 7" Portrait: 600x960 -> width = 600 -> Medium
        val genericTabletPortrait = Screen(
            id = ScreenId("s2"),
            name = "TabletScreen",
            route = "/s2",
            root = rootNode,
            device = DevicePreset.GenericTablet,
            orientation = DeviceOrientation.Portrait
        )
        val tabPortW = genericTabletPortrait.resolveWidthDp(DevicePreset.Pixel8)
        assertEquals(600, tabPortW)
        assertEquals(WindowSizeClass.Medium, genericTabletPortrait.effectiveWindowSizeClass(tabPortW))

        // Pixel Tablet Landscape: 1280x800 -> width = 1280 -> Expanded
        val pixelTabletPortrait = Screen(
            id = ScreenId("s3"),
            name = "PixelTabletScreen",
            route = "/s3",
            root = rootNode,
            device = DevicePreset.PixelTablet,
            orientation = DeviceOrientation.Portrait
        )
        val ptPortW = pixelTabletPortrait.resolveWidthDp(DevicePreset.Pixel8)
        assertEquals(1280, ptPortW)
        assertEquals(WindowSizeClass.Expanded, pixelTabletPortrait.effectiveWindowSizeClass(ptPortW))
    }
}
