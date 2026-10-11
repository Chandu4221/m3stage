package io.github.chandu4221.m3stage.adapter.codegen.resolver

import io.github.chandu4221.m3stage.component.ComposePackage
import io.github.chandu4221.m3stage.component.ComponentKind

object PackageNameResolver {
    fun resolve(kind: ComponentKind): String {
        return when (kind) {
            ComponentKind.Box,
            ComponentKind.Column,
            ComponentKind.Row,
            ComponentKind.Spacer -> ComposePackage.FoundationLayout.packageName

            ComponentKind.Image -> ComposePackage.Foundation.packageName

            else -> ComposePackage.Material3.packageName
        }
    }
}
