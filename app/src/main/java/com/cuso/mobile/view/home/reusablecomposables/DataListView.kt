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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    val label: String? = null   // ✅ NEW — when set + footerAsRows=true, renders "label   value" instead of icon+text
)

data class DataCardBadge(
    val text: String,
    val color: Color
)

// 🔁 ONE action trigger, reused by every table row and every card.
// Tapping the "⋯" opens a bottom sheet listing all actions
// (title "Actions" + close button, then a full-width row per action).
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
 * Fully reusable card — same shell used everywhere, every visual bit (image,
 * date icon, badge, footer rows, actions) is driven by parameters instead of
 * being hardcoded, so each screen just plugs in its own data.
 */
@Composable
fun <T> DataCard(
    item: T,
    image: DataCardImage? = null,
    dateText: String? = null,
    dateIcon: ImageVector = Icons.Default.CalendarMonth,
    badge: DataCardBadge? = null,
    badgeInline: Boolean = false,          // ✅ NEW — default false = old behavior (badge in top row). true = badge sits next to title.
    title: String,
    subtitle: String? = null,
    footerFields: List<DataCardField> = emptyList(),
    footerAsRows: Boolean = false,         // ✅ NEW — default false = old icon+text footer. true = "label   value" rows like image 1.
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

            // ── Top row: date (left) + status badge (right) — both optional ──
            // ── Top row: date (left) + status badge (right) — both optional ──
            val showBadgeInTopRow = badge != null && !badgeInline   // ✅ NEW guard
            if (dateText != null || showBadgeInTopRow) {
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
                    if (showBadgeInTopRow) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badge!!.color.copy(alpha = 0.14f))
                                .padding(horizontal = 20.dp, vertical = 0.dp)
                        ) {
                            Text(badge.text, fontSize = 10.sp, color = badge.color, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Main row: optional image + title/subtitle + "⋮" menu ──
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

                // ✅ NEW — inline badge next to title (only used when badgeInline=true)
                if (badge != null && badgeInline) {
                    Box(
                        modifier = Modifier
                            .background(badge.color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(badge.text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badge.color)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (actions.isNotEmpty()) {
                    ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                }
            }

            // ── Footer: any number of icon+text rows, fully dynamic per page ──
            // ── Footer: any number of icon+text rows, fully dynamic per page ──
            if (footerFields.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(if (footerAsRows) 3.dp else 4.dp)) {
                    footerFields.forEach { field ->
                        if (footerAsRows) {
                            // ✅ NEW — "label        value" row style (image 1 design)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(field.label ?: "", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                                Text(field.text, fontSize = 13.sp, color = field.textColor, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            // existing icon+text style — unchanged for all other pages
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
        }
    }
    HorizontalDivider(color = lightGray, thickness = 2.dp)
}

// ─────────────────────────────────────────────────────────────
// Usage examples — 3 different pages, 3 different field sets,
// same DataCard, nothing hardcoded.
// ─────────────────────────────────────────────────────────────

/*
// 1) Orders page — no image, date + badge + one footer line
DataCard(
    item = order,
    dateText = order.createdOn,
    badge = DataCardBadge(text = order.status, color = Color(0xFF22C55E)),
    title = order.customerName,
    subtitle = order.orderNumber,
    footerFields = listOf(
        DataCardField(icon = Icons.Filled.CurrencyRupee, text = "₹${order.amount}")
    ),
    actions = listOf(
        MenuAction("Edit") { /* ... */ },
        MenuAction("Delete") { /* ... */ }
    ),
    onClick = { navigateToOrder(order.id) }
)

// 2) Customer page — network image (avatar) + two footer lines
DataCard(
    item = customer,
    image = DataCardImage(url = customer.photoUrl, size = 48.dp),
    title = customer.name,
    subtitle = customer.city,
    footerFields = listOf(
        DataCardField(icon = Icons.Filled.LocationOn, text = customer.address),
        DataCardField(icon = Icons.Filled.People, text = customer.phone)
    )
)

// 3) Garment page — local drawable as image, no date/badge/footer at all
DataCard(
    item = garment,
    image = DataCardImage(painter = painterResource(id = R.drawable.garment_placeholder), shape = RoundedCornerShape(8.dp)),
    title = garment.name,
    subtitle = garment.category
)
*/