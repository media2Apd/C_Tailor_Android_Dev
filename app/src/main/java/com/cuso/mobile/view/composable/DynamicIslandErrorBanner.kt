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
package com.cuso.mobile.view.composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Circle diameter = icon size + (2 * padding) → perfect circle when text is hidden
private val CIRCLE_SIZE = 40.dp
private val ICON_SIZE = 18.dp

// ── Theme-aware colors for the Dynamic Island pill ──
private val PillBgLight = Color(0xFF1C1C1E)   // dark pill on light backgrounds (matches iOS style)
private val PillBgDark = Color(0xFFF2F2F7)    // light pill on dark backgrounds
private val PillTextLight = Color(0xFFFFFFFF)
private val PillTextDark = Color(0xFF1C1C1E)

@Composable
private fun DynamicIslandBase(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    val isDarkTheme = isSystemInDarkTheme()
    val pillBgColor = if (isDarkTheme) PillBgDark else PillBgLight
    val pillTextColor = if (isDarkTheme) PillTextDark else PillTextLight

    val dropY = remember { Animatable(-1f) }   // -1f = above screen, 0f = settled
    var isPresent by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            isPresent = true
            showText = false
            dropY.snapTo(-1f)

            // ── STEP 1: round circle drops down from top ──
            dropY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            )

            // ── STEP 3: circle settled → now expand width both sides + reveal text ──
            showText = true

            delay(durationMillis)

            // ── STEP 4 (reverse) part A: text hides, width shrinks back to circle ──
            showText = false
            delay(220)

            // ── STEP 4 (reverse) part B: circle goes back up ──
            dropY.animateTo(
                targetValue = -1f,
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            )

            isPresent = false
            onDismiss()
        }
    }

    if (isPresent) {
        Box(
            modifier = modifier
                .statusBarsPadding()
                .padding(top = 3.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = dropY.value * 20
                        .dp.toPx()
                    alpha = 1f - (-dropY.value)
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                modifier = Modifier
                    .height(CIRCLE_SIZE)
                    .background(pillBgColor, RoundedCornerShape(CIRCLE_SIZE / 2)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── STEP 2: icon perfectly fit inside the circle ──
                Box(
                    modifier = Modifier.size(CIRCLE_SIZE),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(ICON_SIZE)
                    )
                }

                // Text container — expands/shrinks, pushing the pill wider on both sides
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
                        color = pillTextColor,
                        fontSize = 13.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicIslandError(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    DynamicIslandBase(modifier, message, onDismiss, durationMillis, Icons.Default.ErrorOutline, Color(0xFFFF5252))
}

@Composable
fun DynamicIslandSuccess(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    DynamicIslandBase(modifier, message, onDismiss, durationMillis, Icons.Default.CheckCircle, Color(0xFF4ADE80))
}

// Central place to map raw backend error strings to user-friendly messages
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
        return key?.let { errorMap[it] } ?: "Something went wrong. Please try again."
    }

    // Optional: map raw error to which field it belongs to (for red border + accordion open)
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
    Column {
        Box(
            modifier = Modifier
                .then(
                    if (isError) Modifier.border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                    else Modifier
                )
        ) {
            content()
        }
        AnimatedVisibility(visible = isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
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