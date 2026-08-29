package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class StatusBadgeVariant {
    DEFAULT,     // Neutral Grey tag (+More, Organization Profile, Sales, Finance)
    SUCCESS,     // Green tag (CONFIGURED, Active, Paid)
    WARNING,     // Orange/Yellow tag (SETUP REQUIRED, Pending)
    PRIMARY      // Purple/Indigo tag (System Administration)
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: StatusBadgeVariant = StatusBadgeVariant.DEFAULT,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    borderColor: Color? = null,
    fontSize: TextUnit = 11.5.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    cornerRadius: Dp = 14.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null
) {
    val (resolvedBg, resolvedText) = when (variant) {
        StatusBadgeVariant.DEFAULT -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
        StatusBadgeVariant.SUCCESS -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        StatusBadgeVariant.WARNING -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        StatusBadgeVariant.PRIMARY -> Color(0xFFEDE9FE) to Color(0xFF6366F1)
    }

    val finalBg = backgroundColor ?: resolvedBg
    val finalText = textColor ?: resolvedText
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape)
                else Modifier
            )
            .background(finalBg)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = finalText,
            fontWeight = fontWeight
        )
    }
}