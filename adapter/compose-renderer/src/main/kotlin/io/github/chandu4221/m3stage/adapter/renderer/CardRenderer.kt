package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class CardRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val elevation = (node[ComponentCatalog.CardProps.Elevation] as? PropVal.DpVal)?.value ?: 1f

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}