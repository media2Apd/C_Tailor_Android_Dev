//REFERENCES
// SmoothBottomSheet.kt - Updated version

package com.cuso.mobile.view.composable

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import com.cuso.mobile.ui.theme.blackTitle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.whiteBg
import kotlinx.coroutines.launch

enum class SheetValue { Hidden, Collapsed, Expanded }

//   BLUR is applied ONLY to the background scrim
// The sheet itself remains crystal clear
fun Modifier.blurScrim(radius: Dp): Modifier = this.then(
    if (radius.value > 0f) {
        Modifier.graphicsLayer {
            if (Build.VERSION.SDK_INT >= 31) {
                val px = radius.toPx().coerceAtLeast(0.01f)
                renderEffect = RenderEffect
                    .createBlurEffect(px, px, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
        }
    } else Modifier
)

@Composable
fun SmoothBottomSheet(
    state: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 280.dp,
    topInset: Dp = 0.dp,
    collapsedFraction: Float? = null,
    expandedFraction: Float? = null,
    maxBlurRadius: Dp = 14.dp,
    maxScrimAlpha: Float = 0.35f,
    sheetBackgroundColor: Color = whiteBg,
    collapsedCornerRadius: Dp = 24.dp,
    dragCloseEnabled: Boolean = true,
    scrollableContent: Boolean = true,
    onDismissRequest: () -> Unit = { onStateChange(SheetValue.Hidden) },
    onBlurScrimChange: (blurRadius: Dp, scrimAlpha: Float) -> Unit = { _, _ -> },
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val peekHeightPx = collapsedFraction?.let { containerHeightPx * it }
            ?: with(density) { peekHeight.toPx() }
        val topInsetPx = expandedFraction?.let { containerHeightPx * (1f - it) }
            ?: with(density) { topInset.toPx() }

        val hiddenY = containerHeightPx
        val collapsedY = containerHeightPx - peekHeightPx
        val expandedY = topInsetPx

        val offsetY = remember { Animatable(hiddenY) }

        var blurRadiusDp by remember { mutableFloatStateOf(0f) }
        var scrimAlpha by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(offsetY.value) {
            val fraction = (1f - (offsetY.value / hiddenY)).coerceIn(0f, 1f)
            blurRadiusDp = maxBlurRadius.value * fraction
            scrimAlpha = maxScrimAlpha * fraction
            onBlurScrimChange(blurRadiusDp.dp, scrimAlpha)
        }

        LaunchedEffect(state) {
            val target = when (state) {
                SheetValue.Hidden -> hiddenY
                SheetValue.Collapsed -> collapsedY
                SheetValue.Expanded -> expandedY
            }
            offsetY.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        if (state == SheetValue.Hidden && offsetY.value >= hiddenY) return@BoxWithConstraints

        val cornerFraction = (offsetY.value / collapsedY).coerceIn(0f, 1f)
        val cornerRadius = collapsedCornerRadius * cornerFraction

        val expandRange = (collapsedY - expandedY).let { if (it == 0f) 1f else it }
        val expandFraction = ((collapsedY - offsetY.value) / expandRange).coerceIn(0f, 1f)

        val sheetHeightPx = (containerHeightPx - offsetY.value).coerceAtLeast(0f)
        val sheetHeightDp = with(density) { sheetHeightPx.toDp() }

        // ── Scrim with blur ONLY on the background ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurScrim(blurRadiusDp.dp)  //   BLUR applied only to scrim
                .background(blackTitle.copy(alpha = scrimAlpha))
                .then(
                    if (scrimAlpha > 0.01f) {
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                offsetY.animateTo(hiddenY, tween(300, easing = FastOutSlowInEasing))
                                onDismissRequest()
                            }
                        }
                    } else Modifier
                )
        )

        // ── Sheet itself — NO BLUR, crystal clear ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeightDp)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(sheetBackgroundColor)
                //   No blur here - sheet stays clear
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* consumes clicks */ }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newY = (offsetY.value + delta).coerceIn(expandedY, hiddenY)
                                offsetY.snapTo(newY)
                            }
                        },
                        onDragStopped = { velocity ->
                            scope.launch {
                                val target = resolveDragTarget(
                                    currentY = offsetY.value,
                                    velocity = velocity,
                                    expandedY = expandedY,
                                    collapsedY = collapsedY,
                                    hiddenY = hiddenY,
                                    dragCloseEnabled = dragCloseEnabled
                                )
                                offsetY.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                onStateChange(
                                    when (target) {
                                        expandedY -> SheetValue.Expanded
                                        hiddenY -> SheetValue.Hidden
                                        else -> SheetValue.Collapsed
                                    }
                                )
                                if (target == hiddenY) onDismissRequest()
                            }
                        }
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        scope.launch {
                            val target = if (expandFraction > 0.6f) collapsedY
                            else if (offsetY.value <= (collapsedY + expandedY) / 2f) collapsedY
                            else expandedY
                            offsetY.animateTo(target, tween(350, easing = FastOutSlowInEasing))
                            onStateChange(if (target == expandedY) SheetValue.Expanded else SheetValue.Collapsed)
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dashed handle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer {
                            alpha = 1f - expandFraction
                            val s = 1f - expandFraction * 0.6f
                            scaleX = s
                            scaleY = s
                        }
                ) {
                    DashedHandle()
                }

                // Close "X"
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF111827),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(22.dp)
                        .graphicsLayer {
                            alpha = expandFraction
                            val s = 0.4f + expandFraction * 0.6f
                            scaleX = s
                            scaleY = s
                            rotationZ = (1f - expandFraction) * 90f
                        }
                        .clickable(
                            enabled = expandFraction > 0.5f,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                offsetY.animateTo(hiddenY, tween(300, easing = FastOutSlowInEasing))
                                onStateChange(SheetValue.Hidden)
                                onDismissRequest()
                            }
                        }
                )
            }

            // ── Scrollable content ──
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                if (scrollableContent) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        content()
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        content()
                    }
                }
            }
        }
    }
}

private fun resolveDragTarget(
    currentY: Float,
    velocity: Float,
    expandedY: Float,
    collapsedY: Float,
    hiddenY: Float,
    dragCloseEnabled: Boolean
): Float {
    val flingThreshold = 800f

    if (velocity < -flingThreshold) return expandedY
    if (velocity > flingThreshold) {
        return if (dragCloseEnabled && currentY >= collapsedY - 40f) hiddenY else collapsedY
    }

    return when {
        currentY < collapsedY -> {
            val range = collapsedY - expandedY
            val progress = 1f - (currentY - expandedY) / range
            if (progress > 0.2f) expandedY else collapsedY
        }

        else -> {
            if (!dragCloseEnabled) return collapsedY
            val range = hiddenY - collapsedY
            val progress = (currentY - collapsedY) / range
            if (progress > 0.35f) hiddenY else collapsedY
        }
    }
}

@Composable
private fun DashedHandle() {
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFD1D5DB))
    )
}