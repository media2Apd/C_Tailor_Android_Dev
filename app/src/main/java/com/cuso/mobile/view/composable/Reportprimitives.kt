package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.ui.theme.title_border

/**
 * Semantic status colors used by badges and progress bars across every report screen.
 * NOTE: These are report-specific semantic colors (success / warning / critical / neutral).
 * They intentionally live alongside — not inside — AppDesignTokens, since AppDesignTokens
 * only carries structural tokens (spacing, sizing, typography). Swap the hex values below
 * for the equivalents already defined in your project's colors.kt if you'd rather centralize them.
 */
object ReportStatusColors {
    val CriticalBg = Color(0xFFFEE2E2)
    val CriticalText = Color(0xFFDC2626)

    val HealthyBg = Color(0xFFDCFCE7)
    val HealthyText = Color(0xFF16A34A)

    val WarningBg = Color(0xFFFEF3C7)
    val WarningText = Color(0xFFD97706)

    val NeutralBg = Color(0xFFF1E7E7)
    val NeutralText = Color(0xFF8A6D6D)

    val LinkBlue = Color(0xFF3B5BDB)
    val MutedGray = Color(0xFF9CA3AF)
    val DividerGray = title_border
    val TrackGray = Color(0xFFE9E9EC)
}

enum class ReportBadgeType {
    CRITICAL,
    HEALTHY,
    WARNING,
    NEUTRAL
}

/**
 * Small pill-shaped status label, e.g. "Critical", "Healthy", "Low Stock",
 * "Full", "Nearly Full", "Received", "In transit", "Dead Stock".
 */
@Composable
fun ReportStatusBadge(
    text: String,
    type: ReportBadgeType,
    modifier: Modifier = Modifier
) {
    val tokens = com.cuso.mobile.adaptive_screen.LocalAppTokens.current
    val (bg, fg) = when (type) {
        ReportBadgeType.CRITICAL -> ReportStatusColors.CriticalBg to ReportStatusColors.CriticalText
        ReportBadgeType.HEALTHY -> ReportStatusColors.HealthyBg to ReportStatusColors.HealthyText
        ReportBadgeType.WARNING -> ReportStatusColors.WarningBg to ReportStatusColors.WarningText
        ReportBadgeType.NEUTRAL -> ReportStatusColors.NeutralBg to ReportStatusColors.NeutralText
    }

    Box(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

/**
 * Thin horizontal progress track used under each inventory item
 * (red for critical / low stock, green for healthy stock levels, etc).
 */
@Composable
fun ReportProgressBar(
    fraction: Float,
    trackColor: Color = ReportStatusColors.TrackGray,
    fillColor: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 4.dp
) {
    val clamped = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(trackColor, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .background(fillColor, RoundedCornerShape(50))
        )
    }
}

@Composable
fun sectionTitleStyle() = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)