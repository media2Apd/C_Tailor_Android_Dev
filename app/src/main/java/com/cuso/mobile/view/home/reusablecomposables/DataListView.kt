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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.R

// ── One column = one piece of data + how it looks. Reused by header,
//    table cell AND card field (label above value). ──
data class DataColumn<T>(
    val key: String,
    val label: String,
    val width: Dp,
    val headerBold: Boolean = false,
    val cellAlignment: Alignment = Alignment.CenterStart,
    // Override for special headers (e.g. a "select all" checkbox instead of
    // plain text). Falls back to the default text header when null.
    val headerContent: (@Composable () -> Unit)? = null,
    val cellContent: @Composable (T) -> Unit
)

// ── One action-menu item (View / Edit / Delete / View Teams ...) ──
data class MenuAction(
    val label: String,
    val icon: ImageVector,
    val tint: Color = Color(0xFF6B7280),
    val textColor: Color = Color(0xFF111827),
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

// 🔁 ONE action trigger, reused by every table row and every card.
// Tapping the "⋯" opens a bottom sheet listing all actions
// (title "Actions" + close button, then a full-width row per action).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionDropdownMenu(
    actions: List<MenuAction>,
    icon: ImageVector = Icons.Default.MoreHoriz,
    sheetTitle: String = "Actions"
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Icon(
        icon,
        contentDescription = "Actions",
        tint = Color(0xFF9CA3AF),
        modifier = Modifier
            .size(20.dp)
            .clickable { expanded = true }
    )

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Header: title + close ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sheetTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Red, CircleShape)
                            .clickable { expanded = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Action rows ──
                actions.forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = action.enabled) {
                                expanded = false
                                action.onClick()
                            }
                            .alpha(if (action.enabled) 1f else 0.4f)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            action.icon,
                            contentDescription = null,
                            tint = action.tint,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            action.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = action.textColor
                        )
                    }
                    if (index != actions.lastIndex) HorizontalDivider(color = Color(0xFFF0F0F0))
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// 🔁 ONE header cell — used for every table, every screen.
@Composable
private fun DataTableHeaderCell(text: String, width: Dp, bold: Boolean = false) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
        color = if (bold) Color(0xFF111827) else Color(0xFF6B7280)
    )
}

// 🔁 ONE table — header row + scrollable body, built purely from `columns`.
// Any screen's "XyzTable" becomes a 3-line wrapper around this.


// 🔁 ONE card shell — header (leading + title / trailing), optional middle
// row, then N-per-row "label above value" fields built from `fields`
// (the SAME DataColumn list used for the table's cells).
// 🔁 ONE card shell — matches the "date + badge / bold title + menu / subtitle / footer" look.
// Reused by every list screen (Leads, Measurements, SalesOrder, Department, Branch, Designation).
@Composable
fun <T> DataCard(
    item: T,
    dateText: String? = null,
    dateIcon: Painter?=null,
    badgeText: String,
    badgeColor: Color,
    title: String,
    subtitle: String? = null,
    footerIcon: ImageVector? = null,
    footerText: String? = null,
    actions: List<MenuAction> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {

            // ── Top row: date (left) + status badge (right) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dateText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val painter = dateIcon ?: painterResource(id = R.drawable.calendar1)
                        // ↑ simpler: default null-nu vachikonalam
                        if (dateIcon != null) {
                            Icon(dateIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                        } else {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(dateText, fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.14f))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Text(badgeText, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Title row: bold name (left) + "⋮" menu (right) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

            }
            Row(Modifier.fillMaxWidth()) {
                // ── Subtitle line ──
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    if (actions.isNotEmpty()) {
                        ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = actions)
                    }
                }
            }


            // ── Footer row: icon + value ──
            if (footerText != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(footerText, fontSize = 13.sp, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    HorizontalDivider(color= lightGray, thickness = 2.dp)
}