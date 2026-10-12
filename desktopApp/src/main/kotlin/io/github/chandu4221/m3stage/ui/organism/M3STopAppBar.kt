package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.chandu4221.m3stage.ui.molecule.M3SLogo
import io.github.chandu4221.m3stage.ui.theme.AppTheme

@Composable
fun M3STopAppBar() {
    TopAppBar(
        title = { },
        navigationIcon = {
            M3SLogo()
        }
    )
}

@Composable
@Preview
fun M3TopAppBarPreview() {
    AppTheme {
        M3STopAppBar()
    }
}