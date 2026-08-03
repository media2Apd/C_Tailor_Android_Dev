package com.cuso.mobile.view.home.reusablecomposables

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
import com.cuso.mobile.ui.theme.lightGray

// ── Action Menu ──
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

data class DataCardBadge(
    val text: String,
    val textColor: Color,
    val backgroundColor: Color,
    val cornerRadius: Dp = 20.dp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionDropdownMenu(
    actions: List<MenuAction>,
    icon: ImageVector = Icons.Default.MoreHoriz
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            icon,
            contentDescription = "Actions",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier
                .size(20.dp)
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Transparent),
            shape = RoundedCornerShape(10.dp),
            containerColor = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            offset = DpOffset((-8).dp, 4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            action.icon?.let {
                                Icon(
                                    it,
                                    contentDescription = null,
                                    tint = action.tint,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Text(
                                text = action.label,
                                fontSize = 13.sp,
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
 * Reusable DataCard with trailingText positioned at bottom right (aligned with Completed row)
 */
@Composable
fun <T> DataCard(
    item: T,
    modifier: Modifier = Modifier,
    image: DataCardImage? = null,
    dateText: String? = null,
    dateIcon: ImageVector = Icons.Default.CalendarMonth,
    topBadgeText: String? = null,
    topBadgeTextColor: Color = Color.White,
    topBadgeBgColor: Color = Color(0xFF3B3BF9),
    topBadgeCornerRadius: Dp = 20.dp,
    topBadgeInline: Boolean = false,
    bottomBadgeText: String? = null,
    bottomBadgeTextColor: Color = Color.White,
    bottomBadgeBgColor: Color = Color(0xFF3B3BF9),
    bottomBadgeCornerRadius: Dp = 20.dp,
    eyebrowText: String? = null,
    eyebrowColor: Color = Color(0xFF6B7280),
    title: String,
    titleFontWeight: FontWeight = FontWeight.Normal,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { m -> if (onClick != null) m.clickable { onClick(item) } else m },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (containerBrush != null) Color.Transparent else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { m -> if (containerBrush != null) m.background(containerBrush) else m }
                .padding(14.dp)
        ) {
            // ── Top row: optional date + top badge ──
            val showTopBadgeInTopRow = topBadgeText != null && !topBadgeInline
            if (dateText != null || showTopBadgeInTopRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (eyebrowText != null) {
                        Text(
                            eyebrowText,
                            fontSize = 10.sp,
                            color = eyebrowColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    if (dateText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (showDateIcon) {
                                Icon(dateIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(dateText, fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (showTopBadgeInTopRow) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topBadgeCornerRadius))
                                .background(topBadgeBgColor)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                topBadgeText,
                                fontSize = 11.sp,
                                color = topBadgeTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Main Header Row: Title on Left, Actions ("⋮") on Right ──
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (image != null) {
                    if (image.selfContained && image.painter != null) {
                        Image(
                            painter = image.painter,
                            contentDescription = null,
                            modifier = Modifier.size(image.size)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(image.size)
                                .clip(image.shape)
                                .background(image.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                image.url != null -> AsyncImage(
                                    model = image.url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                image.painter != null -> Image(
                                    painter = image.painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = image.tint?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) },
                                    modifier = Modifier.fillMaxSize()
                                )
                                image.vector != null -> Icon(
                                    image.vector,
                                    contentDescription = null,
                                    tint = image.tint ?: Color(0xFF9CA3AF),
                                    modifier = Modifier.size(image.size * 0.55f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = titleFontSize,
                        fontWeight = titleFontWeight,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            subtitle,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (topBadgeText != null && topBadgeInline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topBadgeCornerRadius))
                            .background(topBadgeBgColor)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            topBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = topBadgeTextColor
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (showChevron) {
                    val rotation by animateFloatAsState(
                        targetValue = if (chevronExpanded) 180f else 0f,
                        label = "chevronRotation"
                    )
                    IconButton(
                        onClick = { onChevronClick?.invoke() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (chevronExpanded) "Collapse" else "Expand",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                }

                // ── ⋮ Menu icon at top right ──
                if (actions.isNotEmpty()) {
                    ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                }
            }

            // ── Footer: Left side Cancelled/Completed counts, Right side "60 Orders" / "₹1,25,000" ──
            if (footerFields.isNotEmpty() || trailingText != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(if (footerAsRows) 3.dp else 4.dp)) {
                        footerFields.forEach { field ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    field.text,
                                    fontSize = 13.sp,
                                    color = field.textColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // ── 60 Orders / ₹1,25,000 at bottom right ──
                    if (trailingText != null) {
                        Text(
                            text = trailingText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (bottomBadgeText != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomBadgeCornerRadius))
                        .background(bottomBadgeBgColor)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        bottomBadgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = bottomBadgeTextColor
                    )
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