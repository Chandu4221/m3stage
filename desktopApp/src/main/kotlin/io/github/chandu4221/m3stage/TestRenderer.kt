package io.github.chandu4221.m3stage

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId


@Preview
@Composable
fun TestRendererPreview() {
    // Create a simple test tree
    val root = DesignNode(
        id = NodeId("root"),
        type = ComponentTypes.Column,
        children = listOf(
            DesignNode(
                id = NodeId("text1"),
                type = ComponentTypes.Text,
                props = mapOf("text" to "Hello World")
            ),
            DesignNode(
                id = NodeId("button1"),
                type = ComponentTypes.Button,
                children = listOf(
                    DesignNode(
                        id = NodeId("text2"),
                        type = ComponentTypes.Text,
                        props = mapOf("text" to "Click Me")
                    )
                )
            )
        )
    )

    // Render the tree
    DesignRenderer.Render(root)
}