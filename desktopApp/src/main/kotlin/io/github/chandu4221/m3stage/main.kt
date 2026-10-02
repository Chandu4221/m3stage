package io.github.chandu4221.m3stage

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "m3stage",
    ) {
        App()
    }
}