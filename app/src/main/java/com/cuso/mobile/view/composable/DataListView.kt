@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "VariableNeverRead"
)

package com.cuso.mobile.view.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

// --- String Utilities ---

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
    val asColumn: Boolean = false,
    val labelColor: Color = Color(0xFF9CA3AF),
    val labelBackgroundColor: Color? = null,
    val valueBadge: Boolean = false,
    val valueBadgeBgColor: Color = Color(0xFFFDE7E7),
    val valueBadgeTextColor: Color = Color(0xFFE53935),
    val valueBadgeCornerRadius: Dp = 20.dp,
    val iconBackgroundColor: Color? = null,
    val iconCircleSize: Dp = 24.dp,
    val valueFontWeight: FontWeight = FontWeight.Normal
)

// --- Components ---

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFDCFCE7),
    textColor: Color = Color(0xFF10B981),
    dotColor: Color = textColor,
    cornerRadius: Dp = 20.dp,
    dotSize: Dp = 6.dp,
    showDot: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

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

@Composable
fun <T> DataCard(
    item: T,
    modifier: Modifier = Modifier,
    image: DataCardImage? = null,
    dateText: String? = null,
    dateIcon: ImageVector = Icons.Default.CalendarMonth,
    topBadgeText: String? = null,
    topBadgeTextColor: Color = Color(0xFF10B981),
    topBadgeBgColor: Color = Color(0xFFDCFCE7),
    topBadgeShowDot: Boolean = true,
    topBadgeDotColor: Color = topBadgeTextColor,
    topBadgeCornerRadius: Dp = 20.dp,
    topBadgeInline: Boolean = false,
    bottomBadgeText: String? = null,
    bottomBadgeTextColor: Color = Color(0xFF10B981),
    bottomBadgeBgColor: Color = Color(0xFFDCFCE7),
    bottomBadgeDotColor: Color = bottomBadgeTextColor,
    bottomBadgeCornerRadius: Dp = 20.dp,
    eyebrowText: String? = null,
    eyebrowColor: Color = Color(0xFF111827),
    title: String? = null,
    smalltitle: String? = null,
    percentage: String? = null,
    titleFontWeight: FontWeight = FontWeight.SemiBold,
    titleColor: Color = Color(0xFF111827),
    subtitle: String? = null,
    footerFields: List<DataCardField> = emptyList(),
    footerTags: List<String> = emptyList(),
    footerAsRows: Boolean = false,
    footerAsColumns: Boolean = false,
    actions: List<MenuAction> = emptyList(),
    onClick: ((T) -> Unit)? = null,
    containerBrush: Brush? = null,
    showChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onChevronClick: (() -> Unit)? = null,
    trailingText: String? = null,
    content: (@Composable () -> Unit)? = null,
    showDateIcon: Boolean = true,
    showActionsInHeader: Boolean = false,
    showDivider: Boolean = true
) {
    val tokens = LocalAppTokens.current
    val formattedTitle = remember(title) { title?.toTitleCase() }

    val effectiveCardClick: (() -> Unit)? = remember(onClick, actions, item) {
        when {
            onClick != null -> {
                { onClick(item) }
            }
            actions.isNotEmpty() -> {
                val viewAction = actions.find {
                    it.label.contains("view", ignoreCase = true) ||
                            it.label.contains("detail", ignoreCase = true)
                }
                viewAction?.let { action -> { action.onClick() } }
            }
            else -> null
        }
    }

    val showHeaderRow = (eyebrowText != null || dateText != null || (topBadgeText != null && !topBadgeInline) || (showActionsInHeader && actions.isNotEmpty()))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (effectiveCardClick != null) {
                    Modifier.clickable { effectiveCardClick() }
                } else {
                    Modifier
                }
            ),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { m -> if (containerBrush != null) m.background(containerBrush) else m }
                .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
        ) {
            // --- 1. Top Header Row: Shown when eyebrow / dateText / non-inline badge exists ---
            if (showHeaderRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (eyebrowText != null) {
                        Text(
                            text = eyebrowText,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = eyebrowColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (dateText != null) {
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (topBadgeText != null && !topBadgeInline) {
                            StatusBadge(
                                text = topBadgeText,
                                dotColor = topBadgeDotColor,
                                bgColor = topBadgeBgColor,
                                textColor = topBadgeTextColor,
                                cornerRadius = topBadgeCornerRadius,
                                showDot = topBadgeShowDot
                            )
                        }
                        if (showActionsInHeader && actions.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // --- 2. Main Identity Row: Image + Title/Subtitle + Inline Badge + Actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (image != null) {
                    val avatarSize = if (tokens.isTablet) image.size * 1.2f else image.size
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(image.shape)
                            .background(image.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            image.url != null -> AsyncImage(image.url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            image.painter != null -> Image(image.painter, null, contentScale = ContentScale.Crop, colorFilter = image.tint?.let { ColorFilter.tint(it) }, modifier = Modifier.fillMaxSize())
                            image.vector != null -> Icon(image.vector, null, tint = image.tint ?: Color(0xFF9CA3AF), modifier = Modifier.size(avatarSize * 0.7f))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (formattedTitle != null) {
                        Text(
                            text = formattedTitle,
                            fontSize = tokens.bodyMedium,
                            fontWeight = titleFontWeight,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (subtitle != null) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = subtitle,
                            fontSize = tokens.caption,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Inline badge (e.g. Active with dot) on the same line
                if (topBadgeText != null && topBadgeInline) {
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(
                        text = topBadgeText,
                        dotColor = topBadgeDotColor,
                        bgColor = topBadgeBgColor,
                        textColor = topBadgeTextColor,
                        cornerRadius = topBadgeCornerRadius,
                        showDot = topBadgeShowDot
                    )
                }

                // Action Menu on the same line
                if (actions.isNotEmpty() && !showActionsInHeader) {
                    Spacer(Modifier.width(6.dp))
                    ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                }

                if (showChevron) {
                    val rotation by animateFloatAsState(if (chevronExpanded) 180f else 0f, label = "rotate")
                    IconButton(onClick = { onChevronClick?.invoke() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280), modifier = Modifier.graphicsLayer { rotationZ = rotation })
                    }
                }
            }

            // --- 3. Restored Multi-mode Footer System ---
            if (footerFields.isNotEmpty() || footerTags.isNotEmpty() || trailingText != null) {
                Spacer(Modifier.height(10.dp))

                val columnFields = footerFields.filter { it.asColumn || footerAsColumns }
                val rowFields = footerFields.filter { (it.asRow || footerAsRows) && !it.asColumn && !footerAsColumns }
                val plainFields = footerFields.filter { !it.asRow && !it.asColumn && !footerAsRows && !footerAsColumns }

                // Mode 1: Column format (Left & Right vertical stacks)
                if (columnFields.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        columnFields.forEachIndexed { index, field ->
                            val alignment = if (index == columnFields.lastIndex && columnFields.size > 1) {
                                Alignment.End
                            } else {
                                Alignment.Start
                            }
                            Column(
                                horizontalAlignment = alignment,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (field.label != null) {
                                    Text(
                                        text = field.label,
                                        fontSize = 11.sp,
                                        color = field.labelColor
                                    )
                                }
                                Text(
                                    text = field.text,
                                    fontSize = tokens.bodyMedium,
                                    color = field.textColor,
                                    fontWeight = field.valueFontWeight
                                )
                            }
                        }
                    }
                }

                // Mode 2: Row format (Horizontal Label-on-Left, Value-on-Right lines for Customer/Invoice screens)
                if (rowFields.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = if (columnFields.isNotEmpty()) 8.dp else 0.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowFields.forEach { field ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (field.label != null) {
                                    Text(
                                        text = field.label,
                                        fontSize = tokens.bodySmall,
                                        color = field.labelColor
                                    )
                                }
                                Text(
                                    text = field.text,
                                    fontSize = tokens.bodySmall,
                                    color = field.textColor,
                                    fontWeight = field.valueFontWeight
                                )
                            }
                        }
                    }
                }

                // Mode 3: Plain fields (Left side list) + Footer Tags / Trailing Text on Right
                if (plainFields.isNotEmpty() || footerTags.isNotEmpty() || trailingText != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (rowFields.isNotEmpty() || columnFields.isNotEmpty()) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            plainFields.forEach { field ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (field.icon != null) {
                                        Icon(
                                            imageVector = field.icon,
                                            contentDescription = null,
                                            tint = field.iconTint,
                                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = field.text,
                                        fontSize = tokens.bodySmall,
                                        color = field.textColor,
                                        fontWeight = field.valueFontWeight
                                    )
                                }
                            }
                        }

                        if (footerTags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                footerTags.forEachIndexed { index, tag ->
                                    val isFirstPill = index == 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isFirstPill) Color(0xFFE0F2FE) else Color(0xFFF1F5F9))
                                            .padding(horizontal = 10.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isFirstPill) Color(0xFF0284C7) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        } else if (trailingText != null) {
                            Text(
                                text = trailingText,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            }

            if (content != null) {
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }

    if (showDivider) {
        HorizontalDivider(color = BorderGray, thickness = 1.dp)
    }
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