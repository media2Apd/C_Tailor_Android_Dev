package com.cuso.mobile.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode

//   New Modifier.Node based Indication — no ripple, no highlight, draws nothing extra
object NoRippleIndication : IndicationNodeFactory {

    private class NoRippleIndicationNode : Modifier.Node(), DelegatableNode

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return NoRippleIndicationNode()
    }

    override fun equals(other: Any?): Boolean = other is NoRippleIndication
    override fun hashCode(): Int = -1
}

//   Wrapper composable — call this once at app root
@Composable
fun NoRippleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalIndication provides NoRippleIndication,
        content = content
    )
}