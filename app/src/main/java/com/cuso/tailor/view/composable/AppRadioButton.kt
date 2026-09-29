package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.whiteBg

/**
 * Custom Circular Radio Button matching standard UI specs:
 * - Unselected: Clean grey outer circle, no inner dot.
 * - Selected: Solid primary border ring, white buffer space, and centered primary circle dot.
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    selectedColor: Color = Primary,
    unselectedBorderColor: Color = BorderGray,
    borderWidth: Dp = 1.5.dp,
    enabled: Boolean = true
) {
    AppRadioButtonWithDot(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        size = size,
        selectedColor = selectedColor,
        unselectedBorderColor = unselectedBorderColor,
        borderWidth = borderWidth,
        enabled = enabled
    )
}

/**
 * Explicit inner-dot implementation matching the provided circular ratio design.
 */
@Composable
fun AppRadioButtonWithDot(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    dotSize: Dp = (size.value * 0.55f).dp, // Scales inner dot proportionally to ~55% of the outer size
    selectedColor: Color = Primary,
    unselectedBorderColor: Color = BorderGray,
    borderWidth: Dp = 1.5.dp,
    selectedBorderWidth: Dp = 2.dp, // Outer active ring thickness matching image
    enabled: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(whiteBg)
            .border(
                width = if (selected) selectedBorderWidth else borderWidth,
                color = if (selected) selectedColor else unselectedBorderColor,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled && onClick != null,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick?.invoke()
            }
    ) {
        // Render inner dot strictly when selected
        if (selected) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(selectedColor)
            )
        }
    }
}