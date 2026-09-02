package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg

// Reusable custom circular radio button component
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    selectedColor: Color = Primary,
    unselectedBorderColor: Color = grey_border,
    borderWidth: Dp = 1.5.dp,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (selected) selectedColor else whiteBg)
            .border(
                width = if (selected) 0.dp else borderWidth,
                color = if (selected) Color.Transparent else unselectedBorderColor,
                shape = CircleShape
            )
            .clickable(enabled = enabled && onClick != null) {
                onClick?.invoke()
            }
    )
}