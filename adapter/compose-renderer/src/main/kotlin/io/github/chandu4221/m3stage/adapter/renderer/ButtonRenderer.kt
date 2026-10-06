package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class ButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val textContent = (node[ComponentCatalog.ButtonProps.TextContent] as? PropVal.Str)?.value ?: "Button"

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = textContent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}