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

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.runtime.Composable

@Composable
fun DynamicIslandError(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(durationMillis)
            onDismiss()
        }
    }

    // ✅ CHANGED — plain Box overlay instead of Dialog.
    // This means: no scrim, no touch blocking, and positioning
    // is controlled entirely by the PARENT Box's alignment.
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { -it })
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message ?: "",
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun DynamicIslandSuccess(
    modifier: Modifier,
    message: String?,
    onDismiss: () -> Unit,
    durationMillis: Long = 3000
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(durationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { -it })
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 24.dp, end = 24.dp)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4ADE80),   // green tick
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message ?: "",
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2
            )
        }
    }
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