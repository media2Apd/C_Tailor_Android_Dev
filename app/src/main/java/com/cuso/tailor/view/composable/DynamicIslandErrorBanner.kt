//REFERENCES

@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)
package com.cuso.tailor.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.redText
import kotlinx.coroutines.delay
import com.cuso.tailor.R

// ── Pill dimensions (from the design) ──
private val PILL_HEIGHT = 46.dp
private val PILL_RADIUS = 23.dp
private val BADGE_SIZE = 32.dp
private val ICON_SIZE = 18.dp
private val PROGRESS_HEIGHT = 3.dp

// All colors for one toast style (success/error x light/dark)
private data class ToastColors(
    val pillBg: Color,      // Pill background (tinted)
    val pillBorder: Color,  // 1dp border
    val glow: Color,        // Shadow color
    val badge: Color,       // Circle behind the icon + progress line color
    val icon: Color,        // Check / X color
    val text: Color         // Message text
)

private val SuccessLightColors = ToastColors(
    pillBg = Color(0xFFF0FDF4),
    pillBorder = Color(0xFFBBF7D0),
    glow = Color(0xFF16A34A).copy(alpha = 0.18f),
    badge = Color(0xFF16A34A),
    icon = Color(0xFFFFFFFF),
    text = Color(0xFF14532D)
)

private val ErrorLightColors = ToastColors(
    pillBg = Color(0xFFFEF2F2),
    pillBorder = Color(0xFFFECACA),
    glow = Color(0xFFDC2626).copy(alpha = 0.18f),
    badge = Color(0xFFDC2626),
    icon = Color(0xFFFFFFFF),
    text = Color(0xFF7F1D1D)
)

private val SuccessDarkColors = ToastColors(
    pillBg = Color(0xFF0E1F17),
    pillBorder = Color(0xFF1F4D33),
    glow = Color.Black.copy(alpha = 0.6f),
    badge = Color(0xFF22C55E),
    icon = Color(0xFFFFFFFF),
    text = Color(0xFFDCFCE7)
)

private val ErrorDarkColors = ToastColors(
    pillBg = Color(0xFF231416),
    pillBorder = Color(0xFF5B2128),
    glow = Color.Black.copy(alpha = 0.6f),
    badge = Color(0xFFF87171),
    icon = Color(0xFFFFFFFF),
    text = Color(0xFFFEE2E2)
)

@Composable
private fun DynamicIslandBase(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long,
    isError: Boolean
) {
    val tokens = LocalAppTokens.current

    // Follows the app's current theme (same detection as the date/time pickers)
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colors = when {
        isError && isDarkTheme -> ErrorDarkColors
        isError -> ErrorLightColors
        isDarkTheme -> SuccessDarkColors
        else -> SuccessLightColors
    }

    val dropY = remember { Animatable(-1f) }
    // Remaining-time loader: 1f = full line, 0f = empty
    val progress = remember { Animatable(1f) }
    var isPresent by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            isPresent = true
            showText = false
            progress.snapTo(1f)
            dropY.snapTo(-1f)

            // Step 1: Drop down animation
            dropY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

            // Step 2: Expand and reveal text
            showText = true

            // Stay visible for durationMillis while the loader line runs down to 0
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = durationMillis.toInt(), easing = LinearEasing)
            )

            // Step 3: Shrink and hide text
            showText = false
            delay(220)

            // Step 4: Retract up
            dropY.animateTo(
                targetValue = -1f,
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            )

            isPresent = false
            onDismiss()
        }
    }

    if (isPresent) {
        // This outer Box ALWAYS fills the entire screen and anchors its child
        // to TopCenter itself, regardless of where the caller placed this
        // composable in their layout tree, so every screen gets the same
        // consistent top position.
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = modifier
                    .statusBarsPadding()
                    .padding(top = 3.dp)
                    // Cap how wide the pill can grow on tablets/large screens
                    .widthIn(max = 380.dp)
                    // Side padding so the pill never touches screen edges
                    .padding(horizontal = tokens.screenPadding)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = dropY.value * 20.dp.toPx()
                        alpha = 1f - (-dropY.value)
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                val pillShape = RoundedCornerShape(PILL_RADIUS)

                // Pill: tinted background + border + colored shadow.
                // clip() makes the loader line follow the pill's rounded bottom edge.
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = pillShape,
                            ambientColor = colors.glow,
                            spotColor = colors.glow
                        )
                        .clip(pillShape)
                        .background(colors.pillBg)
                        .border(1.dp, colors.pillBorder, pillShape)
                ) {
                    Row(
                        modifier = Modifier
                            // Wraps its content height, so 2-3 line messages make
                            // the pill taller instead of clipping the text.
                            .defaultMinSize(minHeight = PILL_HEIGHT)
                            .padding(horizontal = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon badge (check / X)
                        Box(
                            modifier = Modifier
                                .size(BADGE_SIZE)
                                .clip(CircleShape)
                                .background(colors.badge),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = if (isError) painterResource(R.drawable.ic_close_circle) else painterResource(R.drawable.ic_tick),
                                contentDescription = null,
                                tint = colors.icon,
                                modifier = Modifier.size(ICON_SIZE)
                            )
                        }

                        AnimatedVisibility(
                            visible = showText,
                            enter = fadeIn(tween(200, delayMillis = 100)) +
                                    expandHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        expandFrom = Alignment.Start
                                    ),
                            exit = shrinkHorizontally(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                shrinkTowards = Alignment.Start
                            ) + fadeOut(tween(120))
                        ) {
                            Text(
                                text = message ?: "",
                                color = colors.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                // Up to 3 lines so long validation errors fully show
                                maxLines = 3,
                                // 10dp gap after the badge; 13dp + 7dp row padding = 20dp right padding
                                modifier = Modifier.padding(
                                    start = 10.dp,
                                    end = 13.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                            )
                        }
                    }

                    // Loader line: thin bar at the bottom of the pill that
                    // shrinks from full width to 0 over durationMillis.
                    // matchParentSize so the bar never changes the pill's own size.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                if (!showText) return@drawBehind
                                val barHeight = PROGRESS_HEIGHT.toPx()
                                drawRect(
                                    color = colors.badge.copy(alpha = 0.6f),
                                    topLeft = Offset(0f, size.height - barHeight),
                                    size = Size(size.width * progress.value, barHeight)
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicIslandError(
    modifier: Modifier = Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    DynamicIslandBase(modifier, message, onDismiss, durationMillis, isError = true)
}

@Composable
fun DynamicIslandSuccess(
    modifier: Modifier = Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    DynamicIslandBase(modifier, message, onDismiss, durationMillis, isError = false)
}

// ── Error Mapper retained as per original structure ──
object ErrorMapper {
    private val errorMap = mapOf(
        "email already exists" to "This email is already registered.",
        "invalid credentials" to "Incorrect email or password.",
        "user not found" to "We couldn't find an account with that email.",
        "network error" to "Please check your internet connection.",
        "branch limit exceeded" to "You've reached your branch limit. Upgrade your plan.",
        "unauthorized" to "Your session has expired. Please log in again.",
        "validation failed" to "Please check the highlighted fields.",
        "server error" to "Something went wrong on our end. Please try again."
    )

    fun map(rawMessage: String?): String {
        if (rawMessage.isNullOrBlank()) return "Something went wrong. Please try again."
        val key = errorMap.keys.firstOrNull { rawMessage.contains(it, ignoreCase = true) }
        // Key match aagalana, raw API message ah nera kaatanum
        // (idhu already user-readable, "Invalid OTP. 2 attempt(s) remaining."
        // maadhiri), generic fallback illama.
        return key?.let { errorMap[it] } ?: rawMessage
    }

    fun fieldFor(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        return when {
            rawMessage.contains("email", ignoreCase = true) -> "email"
            rawMessage.contains("password", ignoreCase = true) -> "password"
            rawMessage.contains("phone", ignoreCase = true) || rawMessage.contains("mobile", ignoreCase = true) -> "mobile"
            rawMessage.contains("branch", ignoreCase = true) -> "branch"
            rawMessage.contains("name", ignoreCase = true) -> "name"
            else -> null
        }
    }
}

@Composable
fun ErrorFieldWrapper(
    isError: Boolean,
    errorMessage: String? = null,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    Column {
        Box(
            modifier = Modifier
                .then(
                    if (isError) Modifier.border(1.5.dp, redText, RoundedCornerShape(8.dp))
                    else Modifier
                )
        ) {
            content()
        }
        AnimatedVisibility(visible = isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = redText,
                fontSize = tokens.label,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

// ── Helper functions retained as per original structure ──
fun fieldFor(rawMessage: String?): String? {
    if (rawMessage.isNullOrBlank()) return null
    return when {
        rawMessage.contains("email", ignoreCase = true) -> "email"
        rawMessage.contains("mobile", ignoreCase = true) || rawMessage.contains("phone", ignoreCase = true) -> "mobile"
        rawMessage.contains("name", ignoreCase = true) -> "name"
        rawMessage.contains("address", ignoreCase = true) -> "address"
        rawMessage.contains("city", ignoreCase = true) -> "city"
        rawMessage.contains("zone", ignoreCase = true) || rawMessage.contains("area", ignoreCase = true) -> "areaZone"
        else -> null
    }
}

fun resolveAccordionToOpen(errorField: String?, sectionFieldMap: Map<String, List<String>>): String? {
    if (errorField == null) return null
    return sectionFieldMap.entries.firstOrNull { (_, fields) -> errorField in fields }?.key
}