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
package com.cuso.mobile.view.home.reports


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.FilterDrawer
import com.cuso.mobile.view.home.reusablecomposables.FilterOption
import com.cuso.mobile.view.home.reusablecomposables.FilterSection
import com.cuso.mobile.view.home.reusablecomposables.FilterSectionType
import com.cuso.mobile.view.home.reusablecomposables.rememberFilterDrawerState

// ── Design tokens ──
private val ReportPrimary = Color(0xFF4F39F6)
private val LostRed = Color(0xFFEF4444)
private val ConvertedGreen = Color(0xFF16A34A)
private val TitleColor = Color(0xFF111827)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFF0F0F0)
private val CardBg = Color(0xFFFAFAFB)

// ── Static data models ──
private data class ReportStat(
    val label: String,
    val value: String,
    val trendText: String,
    val trendUp: Boolean
)

// ✅ CHANGED — lost/converted/amount → cancelled/completed/orders (matches DataCard reuse)
private data class PerformanceChannel(
    val name: String,
    val cancelled: Int,
    val completed: Int,
    val orders: Int
)

private enum class ReportListCategory { RECENT, FAVORITE, ALL }

private data class ReportTypeOption(
    val label: String,
    val category: ReportListCategory,
    val isFavorite: Boolean = false
)

// ─────────────────────────────────────────────────────────────
// 📊 SALES & ORDER REPORTS — main screen
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrderReportsScreen(
    onClose: () -> Unit
) {
    var selectedReportType by remember { mutableStateOf("Sales Report") }
    var showReportTypeSelector by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    val filterDrawerState = rememberFilterDrawerState()

    // ✅ Static filter sections — reuses the existing FilterDrawer/FilterSection model
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Date Range",
                    icon = Icons.Filled.CalendarMonth,
                    type = FilterSectionType.CHIP_ROW,
                    isMultiSelect = false,
                    options = listOf(
                        FilterOption("today", "Today"),
                        FilterOption("week", "This Week"),
                        FilterOption("month", "This Month", isSelected = true)
                    )
                ),
                FilterSection(
                    title = "Salesperson",
                    icon = Icons.Filled.Person,
                    type = FilterSectionType.DROPDOWN,
                    options = emptyList(),
                    dropdownValue = ""
                ),
                FilterSection(
                    title = "Source",
                    icon = Icons.Filled.Campaign,
                    type = FilterSectionType.DROPDOWN,
                    options = emptyList(),
                    dropdownValue = "All Sources"
                ),
                FilterSection(
                    title = "Status",
                    icon = Icons.Filled.Flag,
                    type = FilterSectionType.DROPDOWN,
                    options = emptyList(),
                    dropdownValue = "All Status"
                )
            )
        )
    }

    // ✅ Static stats — swap with real API data later
    val stats = remember {
        listOf(
            ReportStat("Total Sales", "₹4,86,900", "+12.5%", true),
            ReportStat("Conversions", "840", "+8.2%", true),
            ReportStat("Lost", "320", "-2.4%", false),
            ReportStat("Conversion Rate", "34.2%", "+5.1%", true)
        )
    }

    // ✅ CHANGED — static performance breakdown rows now use cancelled/completed/orders
    val channels = remember {
        listOf(
            PerformanceChannel("WhatsApp • Direct Enquiries", 30, 120, 60),
            PerformanceChannel("Walk-In • Store Visitors", 30, 120, 48),
            PerformanceChannel("Instagram • Social Media", 30, 120, 60),
            PerformanceChannel("Referral • Customer Referrals", 30, 120, 82),
            PerformanceChannel("Google Ads • Paid Marketing", 30, 120, 32)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sales & Order Reports", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TitleColor)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TitleColor,
                    modifier = Modifier.size(22.dp).clickable(onClick = onClose)
                )
            }
            HorizontalDivider(color = BorderColor)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Breadcrumb + Export ──
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reports", fontSize = 12.sp, color = MutedColor)
                            Icon(Icons.Default.ChevronRight, null, tint = MutedColor, modifier = Modifier.size(14.dp))
                            Text("Sales & Order Reports", fontSize = 12.sp, color = ReportPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showExportSheet = true }
                        ) {
                            Icon(Icons.Default.FileUpload, null, tint = ReportPrimary, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export", fontSize = 13.sp, color = ReportPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // ── Report Type row ──
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                    .clickable { showReportTypeSelector = true }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(selectedReportType, fontSize = 14.sp, color = TitleColor)
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = MutedColor, modifier = Modifier.size(18.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                .clickable { filterDrawerState.open() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TitleColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // ── Stat grid (2x2) ──
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        stats.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { stat -> ReportStatCard(stat, Modifier.weight(1f)) }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── Performance Breakdown header ──
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Performance Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitleColor)
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MutedColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // ✅ CHANGED — now reuses the shared DataCard instead of the
                // local PerformanceBreakdownRow composable (which has been removed).
                items(channels) { channel ->
                    DataCard(
                        item = channel,
                        title = channel.name,
                        titleFontSize = 14.sp,
                        titleFontWeight = FontWeight.SemiBold,
                        trailingText = "${channel.orders} Orders",
                        footerFields = listOf(
                            DataCardField(
                                text = "${channel.cancelled} Cancelled",
                                textColor = LostRed
                            ),
                            DataCardField(
                                text = "${channel.completed} Completed",
                                textColor = ConvertedGreen
                            )
                        ),
                        actions = listOf(
                            MenuAction(
                                label = "View",
                                icon = Icons.Default.Visibility,
                                onClick = {  }
                            ),
                            MenuAction(
                                label = "Export",
                                icon = Icons.Default.FileUpload,
                                onClick = { }
                            )
                        )
                    )
                }
            }
        }

        // ── Report Type selector (Image 2) ──
        if (showReportTypeSelector) {
            ReportTypeSelectorSheet(
                selected = selectedReportType,
                onSelect = {
                    selectedReportType = it
                    showReportTypeSelector = false
                },
                onDismiss = { showReportTypeSelector = false }
            )
        }

        // ── Export sheet (Image 4) ──
        if (showExportSheet) {
            ExportReportSheet(onDismiss = { showExportSheet = false })
        }
    }

    // ── Filter drawer (Image 3) — reuses the existing FilterDrawer component ──
    FilterDrawer(
        state = filterDrawerState,
        title = "Filter Report",
        sections = filterSections,
        onApply = { updated -> filterSections = updated },
        onClearAll = { }
    )
}

// ─────────────────────────────────────────────────────────────
// Stat card (2x2 grid)
// ─────────────────────────────────────────────────────────────
@Composable
private fun ReportStatCard(stat: ReportStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFEDEDF2), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(stat.label, fontSize = 12.sp, color = MutedColor, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(stat.value, fontSize = 19.sp, color = TitleColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (stat.trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (stat.trendUp) ConvertedGreen else LostRed,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                stat.trendText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (stat.trendUp) ConvertedGreen else LostRed
            )
        }
    }
}

// ❌ REMOVED — PerformanceBreakdownRow composable deleted.
// It's fully replaced by the shared DataCard() call above in the `items(channels)` block.

// ─────────────────────────────────────────────────────────────
// Report Type selector (Image 2)
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportTypeSelectorSheet(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val recent = listOf("Sales Report", "Order Report", "Customer Report")
    val favorites = listOf("Sales Report", "Order Report", "Inventory Report")
    val all = listOf("Sales Report", "Order Report", "Inventory Report", "Purchase Report", "Customer Report", "Delivery Report")

    var favoriteSet by remember { mutableStateOf(setOf("Sales Report", "Order Report", "Inventory Report")) }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text("Report Type", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitleColor)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = MutedColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = TitleColor),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) Text("Search reports...", fontSize = 14.sp, color = MutedColor)
                        inner()
                    }
                )
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollStateSafe())) {
                Spacer(Modifier.height(16.dp))
                Text("RECENT REPORTS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
                Spacer(Modifier.height(4.dp))
                recent.forEach { label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(label) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, tint = MutedColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(label, fontSize = 14.sp, color = TitleColor)
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
                Text("⭐ FAVORITE REPORTS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
                Spacer(Modifier.height(4.dp))
                favorites.forEach { label ->
                    ReportTypeRow(
                        label = label,
                        isSelected = label == selected,
                        isFavorite = favoriteSet.contains(label),
                        onClick = { onSelect(label) },
                        onFavoriteToggle = {
                            favoriteSet = if (favoriteSet.contains(label)) favoriteSet - label else favoriteSet + label
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
                Text("ALL REPORTS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
                Spacer(Modifier.height(4.dp))
                all.forEach { label ->
                    ReportTypeRow(
                        label = label,
                        isSelected = label == selected,
                        isFavorite = favoriteSet.contains(label),
                        onClick = { onSelect(label) },
                        onFavoriteToggle = {
                            favoriteSet = if (favoriteSet.contains(label)) favoriteSet - label else favoriteSet + label
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReportTypeRow(
    label: String,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = ReportPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                label,
                fontSize = 14.sp,
                color = if (isSelected) ReportPrimary else TitleColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        Icon(
            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
            modifier = Modifier.size(18.dp).clickable(onClick = onFavoriteToggle)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Export sheet (Image 4)
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportReportSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                "EXPORT REPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TitleColor,
                letterSpacing = 0.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 14.dp)
            )
            Text("Export Report", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TitleColor)
            Text("Choose an export option", fontSize = 12.sp, color = MutedColor)
            Spacer(Modifier.height(12.dp))

            ExportOptionRow(Icons.Default.PictureAsPdf, Color(0xFFEF4444), "Export as PDF") { onDismiss() }
            ExportOptionRow(Icons.Default.GridOn, Color(0xFF16A34A), "Export as Excel") { onDismiss() }
            ExportOptionRow(Icons.Default.Share, Color(0xFF3B82F6), "Share Report") { onDismiss() }
            ExportOptionRow(Icons.Default.Email, Color(0xFF9333EA), "Send by Email") { onDismiss() }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TitleColor, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExportOptionRow(icon: ImageVector, iconColor: Color, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleColor)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MutedColor, modifier = Modifier.size(18.dp))
    }
}

// small local helper (avoids extra import juggling if rememberScrollState clashes)
@Composable
private fun rememberScrollStateSafe() = androidx.compose.foundation.rememberScrollState()