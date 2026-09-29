package com.cuso.tailor.view.composable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// — Reusable dashed/dotted border modifier
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
): Modifier = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f
        )
    )

    when (val outline = shape.createOutline(size, layoutDirection, this)) {
        is Outline.Rectangle -> {
            drawRect(
                color = color,
                style = stroke
            )
        }
        is Outline.Rounded -> {
            drawRoundRect(
                color = color,
                style = stroke,
                cornerRadius = outline.roundRect.topLeftCornerRadius
            )
        }
        is Outline.Generic -> {
            drawPath(
                path = outline.path,
                color = color,
                style = stroke
            )
        }
    }
}
