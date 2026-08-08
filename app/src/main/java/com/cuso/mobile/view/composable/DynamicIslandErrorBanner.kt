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
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import kotlinx.coroutines.delay

// Circle diameter logic
private val CIRCLE_SIZE = 40.dp
private val ICON_SIZE = 18.dp

// Theme-aware colors
private val PillBgLight = Color(0xFF1C1C1E)
private val PillBgDark = Color(0xFFF2F2F7)
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
    val tokens = LocalAppTokens.current
    val isDarkTheme = isSystemInDarkTheme()
    val pillBgColor = if (isDarkTheme) PillBgDark else PillBgLight
    val pillTextColor = if (isDarkTheme) PillTextDark else PillTextLight

    val dropY = remember { Animatable(-1f) }
    var isPresent by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            isPresent = true
            showText = false
            dropY.snapTo(-1f)

            // Step 1: Drop down animation
            dropY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

            // Step 2: Expand and reveal text
            showText = true

            delay(durationMillis)

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
        Box(
            modifier = modifier
                .statusBarsPadding()
                .padding(top = 3.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = dropY.value * 20.dp.toPx()
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
                        fontSize = tokens.bodySmall, // Applied Adaptive Token
                        maxLines = 2,
                        modifier = Modifier.padding(end = tokens.screenPadding) // Applied Adaptive Token
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
        return key?.let { errorMap[it] } ?: "Something went wrong. Please try again."
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
                fontSize = tokens.label, // Applied Adaptive Token
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