package io.github.chandu4221.m3stage

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "M3 Stage Renderer Test"
    ) {
        MaterialTheme {
            val root = DesignNode(
                id = NodeId("root"),
                type = ComponentTypes.Column,
                children = listOf(
                    DesignNode(
                        id = NodeId("text1"),
                        type = ComponentTypes.Text,
                        props = mapOf("text" to "Welcome to M3 Stage")
                    ),
                    DesignNode(
                        id = NodeId("card1"),
                        type = ComponentTypes.Card,
                        props = mapOf("elevation" to "4"),
                        children = listOf(
                            DesignNode(
                                id = NodeId("text2"),
                                type = ComponentTypes.Text,
                                props = mapOf("text" to "Card Content")
                            )
                        )
                    )
                )
            )

            DesignRenderer.Render(root)
        }
    }
}