@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.composable

import android.graphics.BlurMaskFilter
import android.graphics.Matrix as AndroidMatrix
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.setFrom
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.tan

// ── Calibrated industrial shimmer brush (kept for utility compatibility) ──
@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color> = listOf(
        Color(0xFFE3E5EB),
        Color(0xFFF1F3F8),
        Color(0xFFE3E5EB)
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// =============================================================
// PAGE LOADER — Tailor SaaS Brand Loader (Exact React Match)
// =============================================================

private const val DIAMOND_D = 126f
private const val DIAMOND_R = 32f

private data class DiamondPieceSpec(
    val key: String,
    val tx: Float,
    val ty: Float,
    val deg: Float,
    val colorTop: Color,
    val colorBottom: Color,
    val delayMs: Int
)

private val LOADER_PIECES = listOf(
    DiamondPieceSpec("blue", 0f, -DIAMOND_D, 0f, Color(0xFF3D93E6), Color(0xFF1757A0), 0),
    DiamondPieceSpec("yellow", -DIAMOND_D, 0f, 270f, Color(0xFFFFCF55), Color(0xFFE79709), 60),
    DiamondPieceSpec("red", DIAMOND_D, 0f, 90f, Color(0xFFFF6259), Color(0xFFC41B22), 120),
    DiamondPieceSpec("green", 0f, DIAMOND_D, 180f, Color(0xFF34A357), Color(0xFF1A5F2E), 180)
)

private fun buildDiamondPiecePath(deg: Float): Path {
    val a = DIAMOND_R / sqrt(2f)
    val arcCenterY = -DIAMOND_D + 2f * a
    val base = Path().apply {
        moveTo(-a, -DIAMOND_D + a)
        arcTo(
            rect = Rect(
                left = -DIAMOND_R,
                top = arcCenterY - DIAMOND_R,
                right = DIAMOND_R,
                bottom = arcCenterY + DIAMOND_R
            ),
            startAngleDegrees = -135f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(DIAMOND_D, 0f)
        lineTo(0f, DIAMOND_D)
        lineTo(-DIAMOND_D, 0f)
        close()
    }
    if (deg == 0f) return base
    val matrix = AndroidMatrix().apply { setRotate(deg) }
    base.asAndroidPath().transform(matrix)
    return base
}

private fun buildScissorHalfPath(): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    // Outer outline
    moveTo(0f, -190f)
    cubicTo(12f, -130f, 20f, -70f, 18f, -22f)
    cubicTo(17f, -6f, 12f, 8f, 10f, 26f)
    cubicTo(9f, 48f, 12f, 64f, 24f, 80f)
    cubicTo(40f, 90f, 46f, 110f, 46f, 132f)
    cubicTo(46f, 160f, 26f, 182f, 0f, 182f)
    cubicTo(-26f, 182f, -46f, 160f, -46f, 132f)
    cubicTo(-46f, 110f, -40f, 90f, -24f, 80f)
    cubicTo(-12f, 64f, -12f, 48f, -12f, 26f)
    cubicTo(-14f, 8f, -22f, -6f, -26f, -30f)
    cubicTo(-28f, -70f, -16f, -130f, 0f, -190f)
    close()
    // Inner finger hole (rx=28, ry=33 centered at (0, 132))
    addOval(Rect(left = -28f, top = 132f - 33f, right = 28f, bottom = 132f + 33f))
}

private fun buildBevelHighlight1(): Path = Path().apply {
    moveTo(1f, -184f)
    cubicTo(12f, -130f, 19f, -70f, 17f, -24f)
}

private fun buildBevelHighlight2(): Path = Path().apply {
    moveTo(-3f, -170f)
    cubicTo(4f, -112f, 6f, -62f, 4f, -24f)
}

private fun buildSparkStarPath(): Path = Path().apply {
    moveTo(0f, -15f)
    cubicTo(1.6f, -4f, 4f, -1.6f, 15f, 0f)
    cubicTo(4f, 1.6f, 1.6f, 4f, 0f, 15f)
    cubicTo(-1.6f, 4f, -4f, 1.6f, -15f, 0f)
    cubicTo(-4f, -1.6f, -1.6f, -4f, 0f, -15f)
    close()
}

private fun lerpFloat(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun evalSegment(
    p: Float,
    startP: Float,
    endP: Float,
    startV: Float,
    endV: Float,
    easing: Easing = LinearEasing
): Float {
    val span = (endP - startP).coerceAtLeast(0.0001f)
    val t = ((p - startP) / span).coerceIn(0f, 1f)
    return lerpFloat(startV, endV, easing.transform(t))
}

private val EasePieceIn = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1.12f)
private val EaseScIn = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1.1f)
private val EaseOpen = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.15f)
private val EaseSnipClose = CubicBezierEasing(0.55f, 0f, 0.9f, 0.5f)
private val EaseSnipOpen1 = CubicBezierEasing(0.2f, 0.8f, 0.3f, 1f)
private val EaseSnipOpen2 = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.25f)
private val EaseLiftUp1 = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)
private val EaseLiftUp2 = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.3f)
private val EaseLiftDown = CubicBezierEasing(0.45f, 0f, 0.25f, 1f)
private val EaseBar = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

private fun evalSnipAngleA(p: Float): Float = when {
    p < 0.12f -> -44f
    p < 0.19f -> evalSegment(p, 0.12f, 0.19f, -44f, -9f, EaseSnipClose)
    p < 0.26f -> evalSegment(p, 0.19f, 0.26f, -9f, -34f, EaseSnipOpen1)
    p < 0.33f -> evalSegment(p, 0.26f, 0.33f, -34f, -9f, EaseSnipClose)
    p < 0.43f -> evalSegment(p, 0.33f, 0.43f, -9f, -44f, EaseSnipOpen2)
    else -> -44f
}

private fun evalThrustY(p: Float): Float = when {
    p < 0.12f -> 0f
    p < 0.19f -> evalSegment(p, 0.12f, 0.19f, 0f, -5f, EaseSnipOpen1)
    p < 0.26f -> evalSegment(p, 0.19f, 0.26f, -5f, 0f, FastOutSlowInEasing)
    p < 0.33f -> evalSegment(p, 0.26f, 0.33f, 0f, -5f, EaseSnipOpen1)
    p < 0.43f -> evalSegment(p, 0.33f, 0.43f, -5f, 0f, FastOutSlowInEasing)
    else -> 0f
}

private data class SparkState(val alpha: Float, val scale: Float, val rotDeg: Float)

private fun evalSpark(p: Float): SparkState = when {
    p < 0.17f -> SparkState(0f, 0f, 0f)
    p < 0.195f -> SparkState(
        alpha = evalSegment(p, 0.17f, 0.195f, 0f, 1f),
        scale = evalSegment(p, 0.17f, 0.195f, 0f, 1.1f),
        rotDeg = evalSegment(p, 0.17f, 0.195f, 0f, 20f)
    )
    p < 0.26f -> SparkState(
        alpha = evalSegment(p, 0.195f, 0.26f, 1f, 0f),
        scale = evalSegment(p, 0.195f, 0.26f, 1.1f, 0f),
        rotDeg = evalSegment(p, 0.195f, 0.26f, 20f, 50f)
    )
    p < 0.31f -> SparkState(0f, 0f, 0f)
    p < 0.335f -> SparkState(
        alpha = evalSegment(p, 0.31f, 0.335f, 0f, 1f),
        scale = evalSegment(p, 0.31f, 0.335f, 0f, 1.35f),
        rotDeg = evalSegment(p, 0.31f, 0.335f, 0f, 25f)
    )
    p < 0.42f -> SparkState(
        alpha = evalSegment(p, 0.335f, 0.42f, 1f, 0f),
        scale = evalSegment(p, 0.335f, 0.42f, 1.35f, 0f),
        rotDeg = evalSegment(p, 0.335f, 0.42f, 25f, 60f)
    )
    else -> SparkState(0f, 0f, 60f)
}

private fun evalBlueLiftY(p: Float): Float = when {
    p < 0.18f -> 0f
    p < 0.27f -> evalSegment(p, 0.18f, 0.27f, 0f, -7f, EaseLiftUp1)
    p < 0.33f -> -7f
    p < 0.46f -> evalSegment(p, 0.33f, 0.46f, -7f, -17f, EaseLiftUp2)
    p < 0.76f -> -17f
    p < 0.98f -> evalSegment(p, 0.76f, 0.98f, -17f, 0f, EaseLiftDown)
    else -> 0f
}

private fun evalSweepX(p: Float): Float = when {
    p < 0.52f -> -520f
    p < 0.78f -> evalSegment(p, 0.52f, 0.78f, -520f, 420f, FastOutSlowInEasing)
    else -> 420f
}

@Composable
fun PageLoader(
    modifier: Modifier = Modifier,
    label: String = "Loading",
    messages: List<String> = emptyList(),
    size: Dp = 128.dp,
    intervalMs: Long = 1900L,
    fullScreen: Boolean = false,
    backgroundColor: Color = Color.White,
    startImmediately: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Cycling messages
    var msgIndex by remember(messages) { mutableIntStateOf(0) }
    LaunchedEffect(messages, intervalMs) {
        msgIndex = 0
        if (messages.size >= 2) {
            while (true) {
                delay(intervalMs)
                msgIndex = (msgIndex + 1) % messages.size
            }
        }
    }

    // Entrance & loop timelines
    val initialProgress = if (startImmediately) 1f else 0f
    val pieceProgress = remember(startImmediately) {
        List(LOADER_PIECES.size) { Animatable(initialProgress) }
    }
    val scInProgress = remember(startImmediately) { Animatable(initialProgress) }
    val openProgress = remember(startImmediately) { Animatable(initialProgress) }
    val glowAlpha = remember(startImmediately) { Animatable(if (startImmediately) 0.20f else 0f) }
    val floorAlpha = remember(startImmediately) { Animatable(initialProgress) }
    val loopProgress = remember { Animatable(0f) }
    var loopStarted by remember(startImmediately) { mutableStateOf(startImmediately) }

    LaunchedEffect(startImmediately) {
        if (startImmediately) {
            // Start scissor snip and shimmer animation immediately on the first frame
            loopStarted = true
            loopProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            LOADER_PIECES.forEachIndexed { i, spec ->
                launch {
                    if (spec.delayMs > 0) delay(spec.delayMs.toLong())
                    pieceProgress[i].animateTo(1f, tween(600, easing = EasePieceIn))
                }
            }
            launch {
                delay(300L)
                glowAlpha.animateTo(0.20f, tween(1200, easing = FastOutSlowInEasing))
            }
            launch {
                delay(300L)
                floorAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            }
            launch {
                delay(450L)
                scInProgress.animateTo(1f, tween(700, easing = EaseScIn))
            }
            launch {
                delay(550L)
                openProgress.animateTo(1f, tween(700, easing = EaseOpen))
            }
            launch {
                delay(1400L)
                loopStarted = true
                loopProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }
    }

    // Continuous glow spin, bar slide, dots pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pl_continuous")
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pl_glow_spin"
    )
    val barProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = EaseBar),
            repeatMode = RepeatMode.Restart
        ),
        label = "pl_bar"
    )
    val dotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pl_dots"
    )

    // Pointer tilt + tap pop + ripple
    var targetPx by remember { mutableStateOf(0f) }
    var targetPy by remember { mutableStateOf(0f) }
    val px by animateFloatAsState(
        targetValue = targetPx,
        animationSpec = tween(400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
        label = "pl_px"
    )
    val py by animateFloatAsState(
        targetValue = targetPy,
        animationSpec = tween(400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)),
        label = "pl_py"
    )
    val popScale = remember { Animatable(1f) }
    val rippleAnim = remember { Animatable(1f) }
    var rippleActive by remember { mutableStateOf(false) }

    fun triggerPop() {
        scope.launch {
            popScale.snapTo(1f)
            popScale.animateTo(1.07f, tween(196, easing = CubicBezierEasing(0.3f, 1.5f, 0.5f, 1f)))
            popScale.animateTo(1f, tween(364, easing = CubicBezierEasing(0.3f, 1.5f, 0.5f, 1f)))
        }
        scope.launch {
            rippleActive = true
            rippleAnim.snapTo(0f)
            rippleAnim.animateTo(1f, tween(950, easing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)))
            rippleActive = false
        }
    }

    // Prebuilt SVG paths & brushes
    val piecePaths = remember { LOADER_PIECES.map { buildDiamondPiecePath(it.deg) } }
    val clipCombinedPath = remember {
        Path().apply {
            LOADER_PIECES.forEachIndexed { idx, p ->
                val shifted = Path().apply {
                    addPath(piecePaths[idx], Offset(p.tx, p.ty))
                }
                addPath(shifted)
            }
        }
    }
    val scissorHalfPath = remember { buildScissorHalfPath() }
    val bevel1 = remember { buildBevelHighlight1() }
    val bevel2 = remember { buildBevelHighlight2() }
    val sparkPath = remember { buildSparkStarPath() }
    val skewMatrix = remember {
        val m = AndroidMatrix().apply {
            setSkew(tan(-20.0 * PI / 180.0).toFloat(), 0f)
        }
        Matrix().apply { setFrom(m) }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val containerModifier = if (fullScreen) {
        modifier
            .fillMaxSize()
            .defaultMinSize(minHeight = 480.dp)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    } else {
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 320.dp)
    }

    val stageSizePx = with(density) { size.toPx() }
    val contentColor = if (fullScreen && backgroundColor == Color.White) {
        Color(0xFF0F172A)
    } else {
        LocalContentColor.current
    }
    Column(
        modifier = containerModifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { targetPx = 0f; targetPy = 0f },
                    onDragCancel = { targetPx = 0f; targetPy = 0f }
                ) { change, _ ->
                    val nx = ((change.position.x / this.size.width.coerceAtLeast(1)) * 2f - 1f).coerceIn(-1f, 1f)
                    val ny = ((change.position.y / this.size.height.coerceAtLeast(1)) * 2f - 1f).coerceIn(-1f, 1f)
                    targetPx = nx
                    targetPy = ny
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Stage ──
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            triggerPop()
                            tryAwaitRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Glow + floor shadow behind tilted SVG
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // .pl-glow (inset -16%, blurred conic gradient)
                val ga = glowAlpha.value
                if (ga > 0f) {
                    val glowRadius = w * 0.66f
                    val glowCenter = Offset(w / 2f, h / 2f)
                    withTransform({
                        rotate(glowRotation, pivot = glowCenter)
                    }) {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                alpha = ga
                                asFrameworkPaint().apply {
                                    isAntiAlias = true
                                    maskFilter = BlurMaskFilter(
                                        (w * 0.24f).coerceAtLeast(1f),
                                        BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                            }
                            val brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF1A66B0),
                                    Color(0xFFF9B018),
                                    Color(0xFFE62729),
                                    Color(0xFF247A3C),
                                    Color(0xFF1A66B0)
                                ),
                                center = glowCenter
                            )
                            brush.applyTo(Size(glowRadius * 2f, glowRadius * 2f), paint, ga)
                            canvas.drawCircle(glowCenter, glowRadius, paint)
                        }
                    }
                }

                // .pl-floor (left/right 20%, bottom -9%, height 9%)
                val fa = floorAlpha.value
                if (fa > 0f) {
                    val left = w * 0.20f
                    val right = w * 0.80f
                    val top = h * 1.00f
                    val floorH = h * 0.09f
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            alpha = fa
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                maskFilter = BlurMaskFilter(
                                    (w * 0.055f).coerceAtLeast(1f),
                                    BlurMaskFilter.Blur.NORMAL
                                )
                            }
                        }
                        val ovalCenter = Offset((left + right) / 2f, top + floorH / 2f)
                        val rx = (right - left) / 2f
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0x4D0F172A), Color.Transparent),
                            center = ovalCenter,
                            radius = rx.coerceAtLeast(1f)
                        )
                        brush.applyTo(Size(rx * 2f, floorH), paint, fa)
                        canvas.drawOval(left, top, right, top + floorH, paint)
                    }
                }
            }

            // .pl-tilt + .pl-pop + SVG Canvas (viewBox 0 0 512 512)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        cameraDistance = 820f
                        rotationX = py * -10f
                        rotationY = px * 10f
                        scaleX = popScale.value
                        scaleY = popScale.value
                    }
            ) {
                val viewScale = this.size.width / 512f
                val lp = if (loopStarted) loopProgress.value else 0f
                val liftY = if (loopStarted) evalBlueLiftY(lp) else 0f
                val sweepX = if (loopStarted) evalSweepX(lp) else -520f

                withTransform({
                    scale(viewScale, viewScale, pivot = Offset.Zero)
                }) {
                    // ---------- 1. Four-diamond mark ----------
                    withTransform({
                        translate(256f, 256f)
                    }) {
                        // Drop shadow filter (#ps: dy=6, stdDeviation=6, #0b1220 @ 0.26)
                        LOADER_PIECES.forEachIndexed { idx, piece ->
                            val prog = pieceProgress[idx].value
                            if (prog > 0.001f) {
                                val currTx = lerpFloat(piece.tx * 0.5f, piece.tx, prog)
                                val currTy = lerpFloat(piece.ty * 0.5f, piece.ty, prog)
                                val currScale = lerpFloat(0.4f, 1f, prog)
                                val extraY = if (piece.key == "blue") liftY else 0f
                                val path = piecePaths[idx]
                                val bounds = path.getBounds()

                                withTransform({
                                    translate(currTx, currTy + extraY)
                                    scale(currScale, currScale, pivot = Offset.Zero)
                                }) {
                                    // Shadow
                                    drawPathWithBlurShadow(
                                        path = path,
                                        dx = 0f,
                                        dy = 6f,
                                        blur = 8f,
                                        shadowColor = Color(0xFF0B1220).copy(alpha = 0.26f * prog)
                                    )

                                    // Piece gradient
                                    drawPath(
                                        path = path,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(piece.colorTop, piece.colorBottom),
                                            startY = bounds.top,
                                            endY = bounds.bottom
                                        ),
                                        alpha = prog
                                    )

                                    // Piece sheen
                                    drawPath(
                                        path = path,
                                        brush = Brush.linearGradient(
                                            0.0f to Color.White.copy(alpha = 0.26f),
                                            0.55f to Color.White.copy(alpha = 0.04f),
                                            1.0f to Color.Black.copy(alpha = 0.06f),
                                            start = Offset(bounds.left, bounds.top),
                                            end = Offset(bounds.right, bounds.bottom)
                                        ),
                                        alpha = prog
                                    )
                                }
                            }
                        }

                        // Shimmer sweep clipped to all 4 diamonds (once assembled)
                        if (loopStarted) {
                            clipPath(clipCombinedPath) {
                                withTransform({
                                    translate(sweepX, 0f)
                                    transform(skewMatrix)
                                }) {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            0.0f to Color.White.copy(alpha = 0f),
                                            0.5f to Color.White.copy(alpha = 0.6f),
                                            1.0f to Color.White.copy(alpha = 0f),
                                            startX = 0f,
                                            endX = 90f
                                        ),
                                        topLeft = Offset(0f, -360f),
                                        size = Size(90f, 720f)
                                    )
                                }
                            }
                        }
                    }

                    // ---------- 2. Scissors ----------
                    val scIn = scInProgress.value
                    if (scIn > 0.001f) {
                        val scParX = (px * 9f) / viewScale.coerceAtLeast(0.01f)
                        val scParY = (py * 7f) / viewScale.coerceAtLeast(0.01f)
                        val scInY = lerpFloat(36f, 0f, scIn)
                        val scInScale = lerpFloat(0.82f, 1f, scIn)
                        val scInRot = lerpFloat(-8f, 0f, scIn)
                        val thrustY = if (loopStarted) evalThrustY(lp) else 0f

                        val bladeAngleA = if (loopStarted) {
                            evalSnipAngleA(lp)
                        } else {
                            lerpFloat(-5f, -44f, openProgress.value)
                        }
                        val bladeAngleB = -bladeAngleA

                        withTransform({
                            translate(257f + scParX, 276f + scParY + scInY + thrustY)
                            scale(scInScale, scInScale, pivot = Offset.Zero)
                            rotate(scInRot, pivot = Offset.Zero)
                        }) {
                            // Blade B (mirrored)
                            withTransform({
                                rotate(bladeAngleB, pivot = Offset.Zero)
                                scale(-1f, 1f, pivot = Offset.Zero)
                            }) {
                                drawScissorHalf(scissorHalfPath, bevel1, bevel2, scIn)
                            }

                            // Blade A
                            withTransform({
                                rotate(bladeAngleA, pivot = Offset.Zero)
                            }) {
                                drawScissorHalf(scissorHalfPath, bevel1, bevel2, scIn)
                            }

                            // Center screw
                            drawCircle(
                                color = Color(0xFF0B1220).copy(alpha = 0.35f * scIn),
                                radius = 10f,
                                center = Offset(0f, 3f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, Color(0xFFAAB3C1)),
                                    center = Offset(-3f, -4f),
                                    radius = 16f
                                ),
                                radius = 10f,
                                center = Offset.Zero,
                                alpha = scIn
                            )
                            drawCircle(
                                color = Color(0xFF1B2230),
                                radius = 10f,
                                center = Offset.Zero,
                                alpha = scIn,
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = Color(0xFF68738A),
                                radius = 3.4f,
                                center = Offset.Zero,
                                alpha = scIn
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = 1.6f,
                                center = Offset(-2.6f, -3f),
                                alpha = scIn
                            )

                            // Glint sparks at (0, -168)
                            if (loopStarted) {
                                val spark = evalSpark(lp)
                                if (spark.alpha > 0.005f && spark.scale > 0.005f) {
                                    withTransform({
                                        translate(0f, -168f)
                                        rotate(spark.rotDeg, pivot = Offset.Zero)
                                        scale(spark.scale, spark.scale, pivot = Offset.Zero)
                                    }) {
                                        drawPath(
                                            path = sparkPath,
                                            color = Color.White,
                                            alpha = spark.alpha
                                        )
                                        withTransform({
                                            translate(24f, -16f)
                                            scale(0.5f, 0.5f, pivot = Offset.Zero)
                                        }) {
                                            drawPath(
                                                path = sparkPath,
                                                color = Color.White,
                                                alpha = spark.alpha
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // .pl-ripple on tap
            if (rippleActive) {
                val t = rippleAnim.value
                val rScale = lerpFloat(0.7f, 1.55f, t)
                val rAlpha = lerpFloat(0.9f, 0f, t)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.88f)
                        .graphicsLayer {
                            scaleX = rScale
                            scaleY = rScale
                            alpha = rAlpha
                        }
                        .border(
                            width = 6.dp,
                            color = Color(0x24F9B018),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0x8C1A66B0),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ── Text block (.pl-text) ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.01.em,
                lineHeight = 20.8.sp,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (messages.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.alpha(0.62f)
                    ) {
                        AnimatedContent(
                            targetState = messages[msgIndex],
                            transitionSpec = {
                                (fadeIn(tween(550, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))) +
                                        slideInVertically(
                                            tween(550, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
                                        ) { it / 2 })
                                    .togetherWith(fadeOut(tween(150)))
                            },
                            label = "pl_msg"
                        ) { msg ->
                            Text(
                                text = msg,
                                color = contentColor,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(3) { dotIdx ->
                                val shifted = (dotPhase - dotIdx * 0.125f + 1f) % 1f
                                val dotAlpha = when {
                                    shifted < 0.4f -> lerpFloat(0.2f, 0.9f, shifted / 0.4f)
                                    else -> lerpFloat(0.9f, 0.2f, (shifted - 0.4f) / 0.6f)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .alpha(dotAlpha)
                                        .background(contentColor, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // .pl-bar (132dp x 3dp gradient sweep)
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(132.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .clipToBounds()
                    .background(contentColor.copy(alpha = 0.12f))
            ) {
                val thumbWidth = 132.dp * 0.44f
                val offsetDp = thumbWidth * lerpFloat(-1f, 2.3f, barProgress)
                Box(
                    modifier = Modifier
                        .offset(x = offsetDp)
                        .width(thumbWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1A66B0),
                                    Color(0xFFF9B018),
                                    Color(0xFFE62729),
                                    Color(0xFF247A3C)
                                )
                            )
                        )
                )
            }
        }
    }
}

private fun DrawScope.drawPathWithBlurShadow(
    path: Path,
    dx: Float,
    dy: Float,
    blur: Float,
    shadowColor: Color
) {
    withTransform({
        translate(dx, dy)
    }) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = shadowColor
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blur.coerceAtLeast(0.5f), BlurMaskFilter.Blur.NORMAL)
                }
            }
            canvas.drawPath(path, paint)
        }
    }
}

private fun DrawScope.drawScissorHalf(
    outlinePath: Path,
    bevel1: Path,
    bevel2: Path,
    alpha: Float
) {
    // Drop shadow (#sh: dy=7, stdDeviation=5.5, #0b1220 @ 0.38)
    drawPathWithBlurShadow(
        path = outlinePath,
        dx = 0f,
        dy = 7f,
        blur = 7.5f,
        shadowColor = Color(0xFF0B1220).copy(alpha = 0.38f * alpha)
    )

    // Finger hole subtle inner reflection
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFDFE4EC)),
            center = Offset(0f, 132f - 6.8f),
            radius = 47.6f
        ),
        topLeft = Offset(-29f, 132f - 34f),
        size = Size(58f, 68f),
        alpha = alpha
    )

    // Steel blade + handle gradient (x1=-46 -> x2=46)
    drawPath(
        path = outlinePath,
        brush = Brush.horizontalGradient(
            0.0f to Color(0xFFA9B2C0),
            0.3f to Color(0xFFEEF2F7),
            0.55f to Color.White,
            0.8f to Color(0xFFDDE2EA),
            1.0f to Color(0xFFA5AEBD),
            startX = -46f,
            endX = 46f
        ),
        alpha = alpha
    )

    // Blade outline stroke
    drawPath(
        path = outlinePath,
        color = Color(0xFF1B2230),
        alpha = alpha,
        style = Stroke(width = 2.2f, join = StrokeJoin.Round)
    )

    // Bevel + cutting-edge highlights
    drawPath(
        path = bevel1,
        color = Color.White.copy(alpha = 0.95f),
        alpha = alpha,
        style = Stroke(width = 1.6f, cap = StrokeCap.Round)
    )
    drawPath(
        path = bevel2,
        color = Color(0xFF6F7A8D).copy(alpha = 0.32f),
        alpha = alpha,
        style = Stroke(width = 1f, cap = StrokeCap.Round)
    )

    // Ring rims
    drawOval(
        color = Color(0xFF1B2230).copy(alpha = 0.5f),
        topLeft = Offset(-28f, 132f - 33f),
        size = Size(56f, 66f),
        alpha = alpha,
        style = Stroke(width = 1.4f)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(-38f, 132f - 44f),
        size = Size(76f, 88f),
        alpha = alpha,
        style = Stroke(width = 1.6f)
    )
}

// =============================================================
// GLOBAL FULL-SCREEN LOADER CONTROLLER & OVERLAY
// =============================================================

data class AppLoaderUiState(
    val isLoading: Boolean = false,
    val label: String = "Loading",
    val messages: List<String> = listOf("Fetching data", "Processing", "Almost done")
)

object AppLoader {
    private val _state = MutableStateFlow(AppLoaderUiState())
    val state: StateFlow<AppLoaderUiState> = _state.asStateFlow()

    fun show(
        label: String = "Loading",
        messages: List<String> = listOf("Fetching data", "Processing", "Almost done")
    ) {
        _state.value = AppLoaderUiState(isLoading = true, label = label, messages = messages)
    }

    fun hide() {
        _state.value = _state.value.copy(isLoading = false)
    }

    suspend inline fun <T> withLoader(
        label: String = "Loading",
        messages: List<String> = listOf("Fetching data", "Processing", "Almost done"),
        crossinline block: suspend () -> T
    ): T {
        show(label = label, messages = messages)
        return try {
            block()
        } finally {
            hide()
        }
    }
}

@Composable
fun FullScreenLoaderOverlay(
    isLoading: Boolean,
    label: String = "Loading",
    messages: List<String> = listOf("Fetching data", "Syncing", "Almost done"),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(220))
        ) {
            PageLoader(
                label = label,
                messages = messages,
                fullScreen = true,
                backgroundColor = backgroundColor
            )
        }
    }
}

@Composable
fun TailorSplashScreen(
    isReady: Boolean,
    onSplashFinished: () -> Unit,
    minDisplayMs: Long = 4200L,
    label: String = "Loading",
    backgroundColor: Color = Color.White,
) {
    val ready by rememberUpdatedState(isReady)
    val finished by rememberUpdatedState(onSplashFinished)
    LaunchedEffect(Unit) {
        delay(minDisplayMs)
        snapshotFlow { ready }.first { it }
        finished()
    }
    PageLoader(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        label = label
    )
}
// =============================================================
// APPLIED FULL-SCREEN TO ALL DASHBOARD & LIST LOADERS
// =============================================================

@Composable
fun DashboardSkeleton() {
    PageLoader(
        label = "Loading dashboard",
        messages = listOf("Fetching metrics", "Preparing overview", "Almost done"),
        fullScreen = true
    )
}

@Composable
fun ListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int? = null
) {
    PageLoader(
        modifier = modifier,
        label = "Loading orders",
        messages = listOf("Fetching", "Sorting", "Almost done"),
        fullScreen = true
    )
}

@Composable
fun LegalDashboardSkeleton() {
    PageLoader(
        label = "Loading legal analytics",
        messages = listOf("Checking compliance", "Calculating risk metrics", "Almost done"),
        fullScreen = true
    )
}

@Composable
fun HomeSkeleton() {
    PageLoader(
        label = "Loading home",
        messages = listOf("Fetching overview", "Syncing targets", "Almost done"),
        fullScreen = true
    )
}

@Composable
fun FinanceDashboardSkeleton() {
    PageLoader(
        label = "Loading finance",
        messages = listOf("Calculating cash flow", "Syncing ledger", "Almost done"),
        fullScreen = true
    )
}

@Composable
fun SalesDashboardSkeleton() {
    PageLoader(
        label = "Loading sales",
        messages = listOf("Fetching pipeline", "Calculating revenue", "Almost done"),
        fullScreen = true
    )
}