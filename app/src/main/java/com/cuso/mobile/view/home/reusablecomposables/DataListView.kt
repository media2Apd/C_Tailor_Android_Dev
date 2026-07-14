package com.cuso.mobile.view.home.reusablecomposables

// ═════════════════════════════════════════════════════════════════════════
// 🔁 ONE shared Table + Card system used by ALL list screens:
//    Measurements, SalesOrder, Department, Branch, Designation.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    subtitle: String? = null,
    footerFields: List<DataCardField> = emptyList(),
    footerAsRows: Boolean = false,
    actions: List<MenuAction> = emptyList(),
    onClick: ((T) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { m -> if (onClick != null) m.clickable { onClick(item) } else m },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {

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

            // ── Main row: optional image + title/subtitle + inline top badge + "⋮" menu ──
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
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
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

                // ── Inline top badge (next to title) ──
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

// ─────────────────────────────────────────────────────────────
// Usage Examples - How to use in your screens
// ─────────────────────────────────────────────────────────────

/*
// ============================================================
// EXAMPLE 1: Simple usage with custom colors
// ============================================================

DataCard(
    item = order,
    badge = createBadge(
        text = "Confirmed",
        textColor = Color(0xFF0AB83E),
        backgroundColor = Color(0xFFDBFCE7)
    ),
    title = order.customerName,
    subtitle = order.orderNumber,
    // ...
)

// ============================================================
// EXAMPLE 2: Using with status mapping (most common)
// ============================================================

val badge = when (order.status.lowercase()) {
    "confirmed" -> createBadge(
        text = "Confirmed",
        textColor = Color(0xFF0AB83E),
        backgroundColor = Color(0xFFDBFCE7)
    )
    "unpaid" -> createBadge(
        text = "Unpaid",
        textColor = Color(0xFFC10007),
        backgroundColor = Color(0xFFFEE2E2)
    )
    "paid" -> createBadge(
        text = "Paid",
        textColor = Color(0xFF0AB83E),
        backgroundColor = Color(0xFFDBFCE7)
    )
    "pending" -> createBadge(
        text = "Pending",
        textColor = Color(0xFF92400E),
        backgroundColor = Color(0xFFFEF3C7)
    )
    "cancelled" -> createBadge(
        text = "Cancelled",
        textColor = Color(0xFF4B5563),
        backgroundColor = Color(0xFFF3F4F6)
    )
    "completed" -> createBadge(
        text = "Completed",
        textColor = Color(0xFF065F46),
        backgroundColor = Color(0xFFD1FAE5)
    )
    "draft" -> createBadge(
        text = "Draft",
        textColor = Color(0xFFD97706),
        backgroundColor = Color(0xFFFFFBEB)
    )
    "active" -> createBadge(
        text = "Active",
        textColor = Color(0xFF1E40AF),
        backgroundColor = Color(0xFFDBEAFE)
    )
    "inactive" -> createBadge(
        text = "Inactive",
        textColor = Color(0xFF991B1B),
        backgroundColor = Color(0xFFFEE2E2)
    )
    else -> createBadge(
        text = order.status,
        textColor = Color(0xFF1F2937),
        backgroundColor = Color(0xFFE5E7EB)
    )
}

DataCard(
    item = order,
    badge = badge,
    title = order.customerName,
    subtitle = order.orderNumber,
    // ...
)

// ============================================================
// EXAMPLE 3: With custom corner radius
// ============================================================

val badge = createBadge(
    text = "Completed",
    textColor = Color(0xFF065F46),
    backgroundColor = Color(0xFFD1FAE5),
    cornerRadius = 8.dp  // Square corners
)

// ============================================================
// EXAMPLE 4: Inline badge next to title
// ============================================================

DataCard(
    item = order,
    badge = createBadge(
        text = "New",
        textColor = Color(0xFF1E40AF),
        backgroundColor = Color(0xFFDBEAFE)
    ),
    badgeInline = true,  // Badge appears next to title
    title = order.customerName,
    // ...
)

// ============================================================
// EXAMPLE 5: Footer badge with custom colors
// ============================================================

DataCard(
    item = order,
    footerFields = listOf(
        DataCardField(
            asRow = true,
            label = "Unpaid",
            labelColor = Color(0xFFC10007),
            labelBackgroundColor = Color(0xFFFEE2E2),
            text = "Total: ₹480\n₹480 Due",
            textColor = Color(0xFF111827)
        )
    ),
    // ...
)

// ============================================================
// EXAMPLE 6: Lead status badge
// ============================================================

val leadBadge = when (lead.status) {
    "converted" -> createBadge(
        text = "Converted",
        textColor = Color(0xFF0AB83E),
        backgroundColor = Color(0xFFDBFCE7)
    )
    "new" -> createBadge(
        text = "New Enquiry",
        textColor = Color(0xFF1E40AF),
        backgroundColor = Color(0xFFDBEAFE)
    )
    "follow-up" -> createBadge(
        text = "Follow-up",
        textColor = Color(0xFF991B1B),
        backgroundColor = Color(0xFFFEE2E2)
    )
    else -> createBadge(
        text = lead.status,
        textColor = Color(0xFF1F2937),
        backgroundColor = Color(0xFFE5E7EB)
    )
}

DataCard(
    item = lead,
    badge = leadBadge,
    title = lead.person.name,
    // ...
)
*/