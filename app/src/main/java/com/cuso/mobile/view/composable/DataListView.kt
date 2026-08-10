package com.cuso.mobile.view.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cuso.mobile.adaptive_screen.LocalAppTokens // Global design system tokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.ui.theme.whiteBg

// --- String Utilities ---

/**
 * Converts any string into Title Case (Capitalizes the first letter of each word).
 * Useful for ensuring customer names and titles look consistent.
 */
fun String.toTitleCase(): String {
    if (this.isBlank()) return ""
    return this.trim().lowercase().split("\\s+".toRegex())
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

// --- UI Data Models ---

data class MenuAction(
    val label: String,
    val icon: ImageVector? = null,
    val tint: Color = Color(0xFF6B7280),
    val textColor: Color = Color(0xFF111827),
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

data class DataCardImage(
    val url: String? = null,
    val painter: Painter? = null,
    val vector: ImageVector? = null,
    val size: Dp = 44.dp,
    val shape: Shape = CircleShape,
    val backgroundColor: Color = Color(0xFFF3F4F6),
    val tint: Color? = null,
    val selfContained: Boolean = false
)

data class DataCardField(
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val text: String,
    val textColor: Color = Color(0xFF374151),
    val iconTint: Color = Color(0xFF9CA3AF),
    val label: String? = null,
    val asRow: Boolean = false,
    val labelColor: Color = Color(0xFF9CA3AF),
    val labelBackgroundColor: Color? = null,
    val valueBadge: Boolean = false,
    val valueBadgeBgColor: Color = Color(0xFFFDE7E7),
    val valueBadgeTextColor: Color = Color(0xFFE53935),
    val valueBadgeCornerRadius: Dp = 20.dp,
    val iconBackgroundColor: Color? = null,
    val iconCircleSize: Dp = 24.dp
)

// --- Components ---

/**
 * An adaptive overflow menu (vertical/horizontal dots).
 * Uses tokens to scale font sizes for better accessibility on larger screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionDropdownMenu(
    actions: List<MenuAction>,
    icon: ImageVector = Icons.Default.MoreHoriz
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = icon,
            contentDescription = "Actions",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier
                .size(if (tokens.isTablet) 24.dp else 20.dp)
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(if (tokens.isTablet) 180.dp else 140.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Transparent),
            shape = RoundedCornerShape(10.dp),
            containerColor = Color.Transparent,
            shadowElevation = 0.dp,
            offset = DpOffset((-8).dp, 4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    actions.forEach { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = action.enabled) {
                                    expanded = false
                                    action.onClick()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            action.icon?.let {
                                Icon(it, null, tint = action.tint, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = action.label,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = action.textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * DataCard is a versatile, adaptive container for displaying list items (Leads, Orders, Customers).
 * Standardized for both Phone and Tablet layouts using CompositionLocal tokens.
 */
@Composable
fun <T> DataCard(
    item: T,
    modifier: Modifier = Modifier,
    image: DataCardImage? = null,
    dateText: String? = null,
    dateIcon: ImageVector = Icons.Default.CalendarMonth,
    topBadgeText: String? = null,
    topBadgeTextColor: Color = whiteBg,
    topBadgeBgColor: Color = Color(0xFF3B3BF9),
    topBadgeCornerRadius: Dp = 20.dp,
    topBadgeInline: Boolean = false,
    bottomBadgeText: String? = null,
    bottomBadgeTextColor: Color = whiteBg,
    bottomBadgeBgColor: Color = Color(0xFF3B3BF9),
    bottomBadgeCornerRadius: Dp = 20.dp,
    eyebrowText: String? = null,
    eyebrowColor: Color = Color(0xFF6B7280),
    title: String? = null,
    smalltitle: String? = null,
    percentage: String? =null,
    titleFontWeight: FontWeight = FontWeight.SemiBold,
    titleColor: Color = Color(0xFF111827),
    subtitle: String? = null,
    footerFields: List<DataCardField> = emptyList(),
    footerAsRows: Boolean = false,
    actions: List<MenuAction> = emptyList(),
    onClick: ((T) -> Unit)? = null,
    containerBrush: Brush? = null,
    showChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onChevronClick: (() -> Unit)? = null,
    trailingText: String? = null,
    content: (@Composable () -> Unit)? = null,
    showDateIcon: Boolean = true
) {
    // Access global design tokens for responsive UI
    val tokens = LocalAppTokens.current

    // Formats title to Title Case (e.g., "john doe" -> "John Doe")
    val formattedTitle = remember(title) { title?.toTitleCase() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { m -> if (onClick != null) m.clickable { onClick(item) } else m },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { m -> if (containerBrush != null) m.background(containerBrush) else m }
                // Use screenPadding token (16dp phone / 32dp tablet)
                .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
        ) {
            // --- Header: Metadata & Status Badges ---
            val showTopBadgeInTopRow = topBadgeText != null && !topBadgeInline
            if (dateText != null || showTopBadgeInTopRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (eyebrowText != null) {
                        Text(
                            text = eyebrowText,
                            fontSize = tokens.label,
                            color = eyebrowColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (dateText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (showDateIcon) {
                                Icon(dateIcon, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(dateText, fontSize = tokens.caption, color = Color(0xFF6B7280))
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (showTopBadgeInTopRow) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topBadgeCornerRadius))
                                .background(topBadgeBgColor)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(topBadgeText!!, fontSize = tokens.label, color = topBadgeTextColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // --- Main Content: Identity & Actions ---
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (image != null) {
                    val avatarSize = if (tokens.isTablet) image.size * 1.2f else image.size
                    Box(
                        modifier = Modifier.size(avatarSize).clip(image.shape).background(image.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            image.url != null -> AsyncImage(image.url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            image.painter != null -> Image(image.painter, null, contentScale = ContentScale.Crop, colorFilter = image.tint?.let { ColorFilter.tint(it) }, modifier = Modifier.fillMaxSize())
                            image.vector != null -> Icon(image.vector, null, tint = image.tint ?: Color(0xFF9CA3AF), modifier = Modifier.size(avatarSize * 0.5f))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (formattedTitle != null) {
                            Text(
                                text = formattedTitle,
                                fontSize = tokens.h2, // Adaptive heading
                                fontWeight = titleFontWeight,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (smalltitle != null) {
                            Text(
                                text = smalltitle,
                                fontSize = tokens.bodySmall, // Adaptive heading
                                fontWeight = FontWeight.Normal,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(5.dp))

                        if (percentage != null) {
                            Text(
                                text = percentage,
                                fontSize = tokens.bodySmall, // Adaptive heading
                                fontWeight = FontWeight.Normal,
                                color = Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = tokens.caption, // Adaptive detail text
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Inline badge (optional)
                if (topBadgeText != null && topBadgeInline) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(topBadgeCornerRadius)).background(topBadgeBgColor).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(topBadgeText, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = topBadgeTextColor)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                // Expandable chevron (optional)
                if (showChevron) {
                    val rotation by animateFloatAsState(if (chevronExpanded) 180f else 0f, label = "rotate")
                    IconButton(onClick = { onChevronClick?.invoke() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280), modifier = Modifier.graphicsLayer { rotationZ = rotation })
                    }
                }

                // Action Menu
                if (actions.isNotEmpty()) {
                    ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                }
            }

            // --- Footer: Metrics & Pricing ---
            if (footerFields.isNotEmpty() || trailingText != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        footerFields.forEach { field ->
                            Text(text = field.text, fontSize = tokens.bodySmall, color = field.textColor, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (trailingText != null) {
                        Text(
                            text = trailingText,
                            fontSize = tokens.bodyLarge, // Adaptive highlight font
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            // --- Bottom Section: Custom badges or content ---
            if (bottomBadgeText != null) {
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(bottomBadgeCornerRadius)).background(bottomBadgeBgColor).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(bottomBadgeText, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = bottomBadgeTextColor)
                }
            }

            if (content != null) {
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
    HorizontalDivider(color = lightGray, thickness = 1.dp)
}

data class DataCardStat(
    val label: String,
    val value: String,
    val valueColor: Color = Color(0xFF111827)
)

@Composable
fun DataCardStatsRow(
    stats: List<DataCardStat>,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stats.forEach { stat ->
            Column {
                Text(
                    text = stat.label,
                    fontSize = tokens.caption,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stat.value,
                    fontSize = tokens.bodyMedium,
                    color = stat.valueColor,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DataCardProgressBar(
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFF3F4F6),
    height: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(progressColor)
        )
    }
}

@Composable
fun DataCardCaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF9CA3AF)
) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.caption,
        color = color,
        modifier = modifier
    )
}