package com.cuso.tailor.view.composable

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.ui.theme.Primary
import kotlinx.coroutines.delay

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ThreeDotLoading(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = Primary,
    spaceBetween: Dp = 6.dp,
    travelDistance: Dp = 8.dp
) {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 140L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        0.0f at 0 using FastOutSlowInEasing
                        1.0f at 250 using FastOutSlowInEasing
                        0.0f at 500 using FastOutSlowInEasing
                        0.0f at 1000
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEachIndexed { index, animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = (-travelDistance * animatable.value))
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.4f + (animatable.value * 0.6f)))
            )
            if (index < dots.size - 1) {
                Spacer(modifier = Modifier.width(spaceBetween))
            }
        }
    }
}
