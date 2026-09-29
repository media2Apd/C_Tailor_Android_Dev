package com.cuso.tailor.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.whiteBg
import kotlin.math.roundToInt

/**
 * Common config for the fixed bottom-end FAB button used across
 * Branch / Department / Designation / Lead / Customer / Measurements /
 * SalesOrder screens.
 */
data class FabConfig(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val alignment: Alignment = Alignment.CenterEnd,
    val startPadding: Dp = 0.dp,
    val topPadding: Dp = 0.dp,
    val endPadding: Dp = 12.dp,
    val bottomPadding: Dp = 0.dp,
    val draggable: Boolean = true
)

/**
 * Wraps screen content in a Box so a fixed/draggable FAB (and optional SnackbarHost)
 * can float above it — same pattern used in Branch/Department/Designation/
 * Lead/Customer/Measurements/SalesOrder screens.
 *
 * The FAB starts at its usual bottom-end position, but the user can drag it
 * anywhere on screen — position stays stuck there until moved again (it doesn't
 * reset back to bottom-end on recomposition, since offset is remembered).
 */
@Composable
fun FabScaffold(
    modifier: Modifier = Modifier,
    fab: FabConfig?,
    fabVisible: Boolean = true,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Screen container dimensions
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Button dimensions
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    // User drag position
    var dragOffset by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier.onGloballyPositioned { containerSize = it.size }
    ) {
        content()

        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        if (fab != null) {
            val fabModifier = if (dragOffset != null) {
                // Position driven by manual drag gestures
                Modifier.offset {
                    IntOffset(
                        dragOffset!!.x.roundToInt(),
                        dragOffset!!.y.roundToInt()
                    )
                }
            } else {
                // Static resting position based on configured alignment
                Modifier
                    .align(fab.alignment)
                    .padding(
                        start = fab.startPadding,
                        top = fab.topPadding,
                        end = fab.endPadding,
                        bottom = fab.bottomPadding
                    )
            }

            AnimatedVisibility(
                visible = fabVisible,
                modifier = fabModifier,
                enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.8f),
                exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f)
            ) {
                Button(
                    onClick = fab.onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .onGloballyPositioned { fabSize = it.size }
                        .let { m ->
                            if (fab.draggable) {
                                m.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            if (dragOffset == null) {
                                                val startX = containerSize.width.toFloat() - fabSize.width - with(this) { fab.endPadding.toPx() }
                                                val startY = when (fab.alignment) {
                                                    Alignment.CenterEnd, Alignment.Center, Alignment.CenterStart -> {
                                                        (containerSize.height.toFloat() - fabSize.height) / 2f
                                                    }
                                                    Alignment.TopEnd, Alignment.TopCenter, Alignment.TopStart -> {
                                                        with(this) { fab.topPadding.toPx() }
                                                    }
                                                    else -> {
                                                        containerSize.height.toFloat() - fabSize.height - with(this) { fab.bottomPadding.toPx() }
                                                    }
                                                }
                                                dragOffset = Offset(startX, startY)
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val current = dragOffset ?: Offset.Zero
                                            val newX = (current.x + dragAmount.x)
                                                .coerceIn(0f, (containerSize.width - fabSize.width).toFloat().coerceAtLeast(0f))
                                            val newY = (current.y + dragAmount.y)
                                                .coerceIn(0f, (containerSize.height - fabSize.height).toFloat().coerceAtLeast(0f))
                                            dragOffset = Offset(newX, newY)
                                        }
                                    )
                                }
                            } else m
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(fab.label, color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(fab.icon, contentDescription = null, tint = whiteBg, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}