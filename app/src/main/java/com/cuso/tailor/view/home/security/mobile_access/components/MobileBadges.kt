@file:Suppress("unused", "AssignedValueIsNeverRead", "unusedVariable", "VariableNeverRead")

package com.cuso.tailor.view.home.security.mobile_access.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*

@Composable
fun DeviceStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor, dotColor, label) = when (status.lowercase()) {
        "approved" -> listOf(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF22C55E), "Approved")
        "pending" -> listOf(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFF59E0B), "Pending")
        "blocked" -> listOf(Color(0xFFFEE2E2), Color(0xFFDC2626), Color(0xFFEF4444), "Blocked")
        else -> listOf(light_grey, TextSecondary, mutedText, status)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor as Color)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor as Color)
        )
        Text(
            text = label as String,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = textColor as Color
        )
    }
}

@Composable
fun TrustBadge(status: String) {
    val tokens = LocalAppTokens.current
    val isTrusted = status.equals("trusted", ignoreCase = true)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isTrusted) whiteBg else Color(0xFFFEF3C7))
            .border(1.dp, if (isTrusted) sectionBorder else Color(0xFFFDE68A), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isTrusted) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(12.dp))
        }
        Text(
            text = if (isTrusted) "Trusted" else "Untrusted",
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = if (isTrusted) TextSecondary else Color(0xFFB45309)
        )
    }
}

@Composable
fun SeverityBadge(severity: String, suffix: String = "") {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor) = when (severity.lowercase()) {
        "critical" -> Color(0xFF7F1D1D) to Color.White
        "high" -> Color(0xFFB91C1C) to Color.White
        "medium" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFF1F5F9) to Color(0xFF334155)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (severity + suffix).uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun PlatformChip(platform: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(light_grey)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val icon = when (platform.lowercase()) {
            "ios" -> Icons.Default.PhoneIphone
            "android" -> Icons.Default.PhoneAndroid
            else -> Icons.Default.Language
        }
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
        Text(
            text = platform.ifBlank { "—" },
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
fun EventTypeIcon(type: String) {
    val (icon, tint) = when (type.lowercase()) {
        "blocked_device" -> Icons.Default.Block to redText
        "new_device" -> Icons.Default.Smartphone to Color(0xFF059669)
        "secure_api_failure", "jailbreak_detected" -> Icons.Default.Warning to redText
        else -> Icons.Default.VpnKey to TextSecondary
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
}