package com.cuso.tailor.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.cuso.tailor.ui.theme.*
import kotlin.math.roundToInt

/**
 * Common configuration for FabScaffold.
 * Added 'menuActions' to support attached dropdown menus (e.g. Add Tax Rate, Add Tax Group).
 */
data class FabConfig(
    val label: String,
    val icon: ImageVector = Icons.Default.KeyboardArrowDown,
    val onClick: () -> Unit = {},
    val menuActions: List<MenuAction> = emptyList(), // Attached dropdown items
    val alignment: Alignment = Alignment.CenterEnd,
    val startPadding: Dp = 0.dp,
    val topPadding: Dp = 0.dp,
    val endPadding: Dp = 12.dp,
    val bottomPadding: Dp = 0.dp,
    val draggable: Boolean = true
)

@Composable
fun FabScaffold(
    modifier: Modifier = Modifier,
    fab: FabConfig?,
    fabVisible: Boolean = true,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
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
                Modifier.offset {
                    IntOffset(
                        dragOffset!!.x.roundToInt(),
                        dragOffset!!.y.roundToInt()
                    )
                }
            } else {
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
                var isMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    Button(
                        onClick = {
                            if (fab.menuActions.isNotEmpty()) {
                                isMenuExpanded = !isMenuExpanded
                            } else {
                                fab.onClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
                            Icon(
                                imageVector = if (fab.menuActions.isNotEmpty() && isMenuExpanded) Icons.Default.KeyboardArrowUp else fab.icon,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Attached dropdown popup directly positioned with the button
                    if (fab.menuActions.isNotEmpty()) {
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            shape = RoundedCornerShape(10.dp),
                            containerColor = whiteBg,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .width(150.dp)
                                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                        ) {
                            fab.menuActions.forEach { action ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = action.label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = action.textColor
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    onClick = {
                                        isMenuExpanded = false
                                        action.onClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}