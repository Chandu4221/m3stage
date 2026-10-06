package io.github.chandu4221.m3stage.property

enum class HorizontalArrangementOption(val displayName: String) {
    Start(displayName = "Start"),
    Center(displayName = "Center"),
    End(displayName = "End"),
    SpaceBetween(displayName = "Space Between"),
    SpaceAround(displayName = "Space Around"),
    SpaceEvenly(displayName = "Space Evenly")
}

enum class VerticalArrangementOption(val displayName: String) {
    Top(displayName = "Top"),
    Center(displayName = "Center"),
    Bottom(displayName = "Bottom"),
    SpaceBetween(displayName = "Space Between"),
    SpaceAround(displayName = "Space Around"),
    SpaceEvenly(displayName = "Space Evenly")
}

enum class HorizontalAlignmentOption(val displayName: String) {
    Start(displayName = "Start"),
    CenterHorizontally(displayName = "Center Horizontally"),
    End(displayName = "End")
}

enum class VerticalAlignmentOption(val displayName: String) {
    Top(displayName = "Top"),
    CenterVertically(displayName = "Center Vertically"),
    Bottom(displayName = "Bottom")
}

enum class ContentAlignmentOption(val displayName: String) {
    TopStart(displayName = "Top Start"),
    TopCenter(displayName = "Top Center"),
    TopEnd(displayName = "Top End"),
    CenterStart(displayName = "Center Start"),
    Center(displayName = "Center"),
    CenterEnd(displayName = "Center End"),
    BottomStart(displayName = "Bottom Start"),
    BottomCenter(displayName = "Bottom Center"),
    BottomEnd(displayName = "Bottom End")
}

enum class TextOverflowOption(val displayName: String) {
    Clip(displayName = "Clip"),
    Ellipsis(displayName = "Ellipsis"),
    Visible(displayName = "Visible")
}