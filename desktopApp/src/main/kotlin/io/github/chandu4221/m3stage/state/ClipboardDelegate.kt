package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.ScreenId

interface ClipboardState {
    val clip: State<DesignNode?>
    val sourceScreenId: State<ScreenId?>
    fun setClip(node: DesignNode?, sourceScreenId: ScreenId?)
    fun clear()
}

class ClipboardDelegate : ClipboardState {
    private val _clip: MutableState<DesignNode?> = mutableStateOf(null)
    override val clip: State<DesignNode?> = _clip

    private val _sourceScreenId: MutableState<ScreenId?> = mutableStateOf(null)
    override val sourceScreenId: State<ScreenId?> = _sourceScreenId

    override fun setClip(node: DesignNode?, sourceScreenId: ScreenId?) {
        _clip.value = node
        _sourceScreenId.value = sourceScreenId
    }

    override fun clear() {
        _clip.value = null
        _sourceScreenId.value = null
    }
}
