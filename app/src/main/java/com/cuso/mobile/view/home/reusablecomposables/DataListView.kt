package com.cuso.mobile.view.home.reusablecomposables

// ═════════════════════════════════════════════════════════════════════════
// 🔁 ONE shared Table + Card system used by ALL list screens:
//    Measurements, SalesOrder, Department, Branch, Designation, Chart of Account.
//
// Instead of every screen defining its own:
//   - HeaderCell composable
//   - Table composable (header row + body rows + widths)
//   - Row composable
//   - Card composable
//   - Action "⋯" menu
//
// ...they now just describe THEIR data as a `List<DataColumn<T>>` and call
// `DataTable(items, columns)` / `DataCard(item, ..., fields = columns)`.
//
// Add a column → header AND every row AND every card update together.
// ═════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.lightGray
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset

// ── One action-menu item (View / Edit / Delete / View Teams ...) ──
data class MenuAction(
    val label: String,
    val icon: ImageVector,
    val tint: Color = Color(0xFF6B7280),
    val textColor: Color = Color(0xFF111827),
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

// ─────────────────────────────────────────────────────────────
// Dynamic building blocks — pass only what a given page needs,
// everything else stays null/empty and simply won't render.
// ─────────────────────────────────────────────────────────────

/**
 * Leading avatar/thumbnail for the card. Give it a `url` (network image via Coil),
 * a `painter` (local drawable/bitmap), or a `vector` (Material icon placeholder) —
 * whichever one a particular page has available.
 */
data class DataCardImage(
    val url: String? = null,
    val painter: Painter? = null,
    val vector: ImageVector? = null,
    val size: Dp = 44.dp,
    val shape: Shape = CircleShape,
    val backgroundColor: Color = Color(0xFFF3F4F6),
    val tint: Color? = null // only used when `vector` is supplied
)

/** One icon + text line in the footer. Add as many as a page needs (or none). */
data class DataCardField(
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val text: String,
    val textColor: Color = Color(0xFF374151),
    val iconTint: Color = Color(0xFF9CA3AF),
    val label: String? = null,
    val asRow: Boolean = false,
    val labelColor: Color = Color(0xFF9CA3AF),
    val labelBackgroundColor: Color? = null
)

/**
 * ✅ SIMPLIFIED: Badge with user customizable colors
 * User can set both text color and background color directly
 */
data class DataCardBadge(
    val text: String,
    val textColor: Color,           // ✅ User sets this
    val backgroundColor: Color,     // ✅ User sets this
    val cornerRadius: Dp = 20.dp   // ✅ User can customize corner radius
)

// 🔁 ONE action trigger, reused by every table row and every card.
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
                            Icon(
                                action.icon,
                                contentDescription = null,
                                tint = action.tint,
                                modifier = Modifier.size(15.dp)
                            )

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
 * Reusable card with user customizable badge colors
 */
@Composable
fun <T> DataCard(
    item: T,
    modifier: Modifier = Modifier, // NEW — lets the caller attach animateItemPlacement()/animateItem()
    image: DataCardImage? = null,
    dateText: String? = null,
    dateIcon: ImageVector = Icons.Default.CalendarMonth,

    // ── TOP BADGE (shown in the top row, next to date — or inline near title) ──
    topBadgeText: String? = null,
    topBadgeTextColor: Color = Color.White,
    topBadgeBgColor: Color = Color(0xFF3B3BF9),
    topBadgeCornerRadius: Dp = 20.dp,
    topBadgeInline: Boolean = false, // true = show next to title instead of top row

    // ── BOTTOM BADGE (shown at the bottom of the card, below footer fields) ──
    bottomBadgeText: String? = null,
    bottomBadgeTextColor: Color = Color.White,
    bottomBadgeBgColor: Color = Color(0xFF3B3BF9),
    bottomBadgeCornerRadius: Dp = 20.dp,

    title: String,
    titleFontWeight: FontWeight = FontWeight.SemiBold,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 18.sp,      // NEW — default unchanged
    titleColor: Color = Color(0xFF111827),                          // NEW — default unchanged
    subtitle: String? = null,
    footerFields: List<DataCardField> = emptyList(),
    footerAsRows: Boolean = false,
    actions: List<MenuAction> = emptyList(),
    onClick: ((T) -> Unit)? = null,
    containerBrush: Brush? = null, // optional gradient background, null = plain white

    // ── EXPAND/COLLAPSE CHEVRON (NEW) — shown at the end of the title row,
    // right before the "⋮" actions menu. Only rendered when showChevron = true,
    // so screens that don't need an accordion (Measurements, SalesOrder, etc.)
    // are completely unaffected.
    showChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onChevronClick: (() -> Unit)? = null,
    trailingText:String?=null
) {
    Card(
        modifier = modifier // CHANGED — was Modifier, now starts from the passed-in modifier
            .fillMaxWidth()
            .let { m -> if (onClick != null) m.clickable { onClick(item) } else m },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (containerBrush != null) Color.Transparent else Color.White
        )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { m -> if (containerBrush != null) m.background(containerBrush) else m }
                .padding(14.dp)
        ) {

            // ── Top row: date (left) + top badge (right) ──
            val showTopBadgeInTopRow = topBadgeText != null && !topBadgeInline
            if (dateText != null || showTopBadgeInTopRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dateText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(dateIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
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

            // ── Main row: optional image + title/subtitle + inline top badge
            //    + chevron + "⋮" menu — all at the end of the row ──
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                if (image != null) {
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
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        Text(
                            title,
                            fontSize = titleFontSize,   // CHANGED
                            fontWeight = titleFontWeight,
                            color = titleColor,          // CHANGED
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (trailingText != null) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                trailingText,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    }
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

                // ── Inline top badge (next to title, at row end) ──
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

                // ── Expand/collapse chevron — rotates 180° when expanded ──
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

                if (actions.isNotEmpty()) {
                    ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                }
            }

            // ── Footer: any number of icon+text rows, fully dynamic per page ──
            if (footerFields.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(if (footerAsRows) 3.dp else 4.dp)) {
                    footerFields.forEach { field ->
                        val renderAsRow = footerAsRows || field.asRow
                        if (renderAsRow) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (field.labelBackgroundColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(field.labelBackgroundColor.copy(alpha = 0.14f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            field.label ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = field.labelColor
                                        )
                                    }
                                } else {
                                    Text(field.label ?: "", fontSize = 13.sp, color = field.labelColor)
                                }
                                Text(
                                    field.text,
                                    fontSize = 13.sp,
                                    color = field.textColor,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when {
                                    field.icon != null -> {
                                        Icon(field.icon, contentDescription = null, tint = field.iconTint, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    field.painter != null -> {
                                        Icon(field.painter, contentDescription = null, tint = field.iconTint, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                    }
                                }
                                Text(field.text, fontSize = 13.sp, color = field.textColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ── Bottom badge (below everything, at the bottom of the card) ──
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
        }
    }
    HorizontalDivider(color = lightGray, thickness = 2.dp)
}

// ─────────────────────────────────────────────────────────────
// ✅ SINGLE HELPER FUNCTION - Simple and clean
// User can customize text color and background color
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

        /**
         * Create a badge with custom colors
         * @param text The badge text
         * @param textColor Color of the text (default: White)
         * @param backgroundColor Background color of the badge (default: Blue)
         * @param cornerRadius Corner radius of the badge (default: 20.dp)
         */
fun createBadge(
    text: String,
    textColor: Color = Color.White,
    backgroundColor: Color = Color(0xFF3B3BF9),
    cornerRadius: Dp = 20.dp
): DataCardBadge {
    return DataCardBadge(
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius
    )
}