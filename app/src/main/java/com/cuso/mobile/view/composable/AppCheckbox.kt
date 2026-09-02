package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.iconMuted
import com.cuso.mobile.ui.theme.whiteBg

// Reusable rounded square checkbox component
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    checkedColor: Color = Primary,
    uncheckedBorderColor: Color = iconMuted,
    checkmarkColor: Color = whiteBg,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) checkedColor else whiteBg)
            .border(
                width = if (checked) 0.dp else 1.5.dp,
                color = if (checked) Color.Transparent else uncheckedBorderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled && onCheckedChange != null) {
                onCheckedChange?.invoke(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = checkmarkColor,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}