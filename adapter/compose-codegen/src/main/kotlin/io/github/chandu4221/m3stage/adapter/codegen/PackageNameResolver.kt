package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.component.ComponentKind

object PackageNameResolver {
    fun resolve(kind: ComponentKind): String = kind.module.packageName
}