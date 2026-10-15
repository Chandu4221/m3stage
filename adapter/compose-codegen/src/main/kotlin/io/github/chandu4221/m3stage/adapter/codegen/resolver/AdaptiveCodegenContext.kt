package io.github.chandu4221.m3stage.adapter.codegen.resolver

object AdaptiveCodegenContext {
    private val isAdaptive = ThreadLocal.withInitial { false }
    private val isRailMode = ThreadLocal.withInitial { false }

    fun <T> withAdaptive(adaptive: Boolean, block: () -> T): T {
        val prev = isAdaptive.get()
        isAdaptive.set(adaptive)
        try {
            return block()
        } finally {
            isAdaptive.set(prev)
        }
    }

    val isAdaptiveScreen: Boolean get() = isAdaptive.get()

    fun <T> withRailMode(rail: Boolean, block: () -> T): T {
        val prev = isRailMode.get()
        isRailMode.set(rail)
        try {
            return block()
        } finally {
            isRailMode.set(prev)
        }
    }

    val isRail: Boolean get() = isRailMode.get()
}
