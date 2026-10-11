package io.github.chandu4221.m3stage.component

/**
 * Official Material 3 component categories matching https://m3.material.io/components
 * plus foundational Compose layout primitives.
 */
enum class ComponentCategory(val displayName: String) {
    Actions(displayName = "Actions"),               // Buttons, FABs, Segmented Buttons
    Communication(displayName = "Communication"),   // Badges, Progress Indicators, Snackbars
    Containment(displayName = "Containment"),       // Cards, Dialogs, Bottom Sheets, Dividers
    Navigation(displayName = "Navigation"),         // Top App Bar, Navigation Bar, Tabs
    Selection(displayName = "Selection"),           // Checkboxes, Chips, Switches, Sliders
    TextInputs(displayName = "Text Inputs"),       // Text Fields
    Layouts(displayName = "Layouts")                // Column, Row, Box, Scaffold (Compose primitives)
}