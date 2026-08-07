package com.cuso.mobile.view.composable

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
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg
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
    val endPadding: Dp = 10.dp,
    val bottomPadding: Dp = 50.dp,
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
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable BoxScope.() -> Unit
) {
    //  container size — needed to clamp drag within screen bounds
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    //  fab's own size — needed to clamp so it doesn't go off-edge
    var fabSize by remember { mutableStateOf(IntSize.Zero) }

    //  drag offset from the FAB's default bottom-end position.
    // null until the user actually drags it once — until then it renders
    // at the normal align(BottomEnd) + padding position.
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
                //  once dragged, position is driven purely by the tracked offset
                Modifier.offset {
                    IntOffset(
                        dragOffset!!.x.roundToInt(),
                        dragOffset!!.y.roundToInt()
                    )
                }
            } else {
                //   default resting spot — same as before
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = fab.endPadding, bottom = fab.bottomPadding)
            }

            Button(
                onClick = fab.onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = fabModifier
                    .onGloballyPositioned { fabSize = it.size }
                    .let { m ->
                        if (fab.draggable) {
                            m.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        //  first drag — seed dragOffset from current visual
                                        // position (bottom-end) so it doesn't jump
                                        if (dragOffset == null) {
                                            dragOffset = Offset(
                                                x = (containerSize.width - fabSize.width - fab.endPadding.value.dp.value).let {
                                                    containerSize.width.toFloat() - fabSize.width - with(
                                                        this
                                                    ) { fab.endPadding.toPx() }
                                                },
                                                y = containerSize.height.toFloat() - fabSize.height - with(
                                                    this
                                                ) { fab.bottomPadding.toPx() }
                                            )
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