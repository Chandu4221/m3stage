package io.github.chandu4221.m3stage.component

enum class ComponentCategory(val displayName: String) {
    Layouts(displayName = "Layouts"),       // Column, Row, Box, Scaffold
    Surfaces(displayName = "Surfaces"),     // Card, Surface, Divider
    Inputs(displayName = "Inputs"),         // Button, TextField, Checkbox, Switch
    Display(displayName = "Display"),       // Text, Icon, Image
    Navigation(displayName = "Navigation")  // TopAppBar, NavigationBar, NavigationRail
}