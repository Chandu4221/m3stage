package io.github.chandu4221.m3stage.model


/**
 * Categories for organizing components in the UI Palette
 * and determining import paths in Code Generation.
 */
enum class ComponentCategory(val displayName: String) {
    LAYOUTS("Layouts"),          // Column, Row, Box, Scaffold
    SURFACES("Surfaces"),        // Card, Surface, Divider
    INPUTS("Inputs"),            // Button, TextField, Checkbox, Switch
    DISPLAY("Display"),          // Text, Icon, Image
    NAVIGATION("Navigation")     // TopAppBar, NavigationBar, NavigationRail
}