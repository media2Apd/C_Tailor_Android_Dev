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
import androidx.compose.foundation.rememberScrollState
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
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.FilterDrawer
import com.cuso.mobile.view.home.reusablecomposables.FilterOption
import com.cuso.mobile.view.home.reusablecomposables.FilterSection
import com.cuso.mobile.view.home.reusablecomposables.FilterSectionType
import com.cuso.mobile.view.home.reusablecomposables.SheetValue
import com.cuso.mobile.view.home.reusablecomposables.SmoothBottomSheet
import com.cuso.mobile.view.home.reusablecomposables.blurScrim
import com.cuso.mobile.view.home.reusablecomposables.rememberFilterDrawerState

// ── Design tokens ──
private val ReportPrimary = Color(0xFF4F39F6)
private val LostRed = Color(0xFFEF4444)
private val ConvertedGreen = Color(0xFF16A34A)
private val TitleColor = Color(0xFF111827)
private val MutedColor = Color(0xFF9CA3AF)
private val BorderColor = Color(0xFFF0F0F0)

// ── Data Models ──
private data class ReportStat(
    val label: String,
    val value: String,
    val trendText: String,
    val trendUp: Boolean
)

private data class PerformanceChannel(
    val id: String,
    val name: String,
    val subTitle: String,
    val negativeCount: Int,
    val positiveCount: Int,
    val trailingText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrderReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    var selectedReportType by remember { mutableStateOf("Sales Report") }
    var reportTypeSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var exportSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val filterDrawerState = rememberFilterDrawerState()

    var reportTypeBlur by remember { mutableStateOf(0.dp) }
    var filterBlur by remember { mutableStateOf(0.dp) }
    var exportBlur by remember { mutableStateOf(0.dp) }
    val backgroundBlur = maxOf(reportTypeBlur, filterBlur, exportBlur)

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
                    dropdownValue = "All Salespersons"
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

    val stats = remember(selectedReportType) {
        if (selectedReportType == "Order Reports" || selectedReportType == "Order Report") {
            listOf(
                ReportStat("Total Orders", "245", "+12.5%", true),
                ReportStat("Completed", "840", "+8.2%", true),
                ReportStat("Cancelled", "32", "-2.4%", false),
                ReportStat("Completed Rate", "85.2%", "+5.1%", true)
            )
        } else {
            listOf(
                ReportStat("Total Sales", "₹4,86,900", "+12.5%", true),
                ReportStat("Conversions", "840", "+8.2%", true),
                ReportStat("Lost", "320", "-2.4%", false),
                ReportStat("Conversion Rate", "34.2%", "+5.1%", true)
            )
        }
    }

    val channels = remember(selectedReportType) {
        if (selectedReportType == "Order Reports" || selectedReportType == "Order Report") {
            listOf(
                PerformanceChannel("1", "WhatsApp", "Direct Enquiries", 30, 120, "60 Orders"),
                PerformanceChannel("2", "Walk-In", "Store Visitors", 30, 120, "48 Orders"),
                PerformanceChannel("3", "Instagram", "Social Media", 30, 120, "60 Orders"),
                PerformanceChannel("4", "Referral", "Customer Referrals", 30, 120, "82 Orders"),
                PerformanceChannel("5", "Google Ads", "Paid Marketing", 30, 120, "32 Orders")
            )
        } else {
            listOf(
                PerformanceChannel("1", "WhatsApp", "Direct Enquiries", 30, 120, "₹1,25,000"),
                PerformanceChannel("2", "Walk-In", "Store Visitors", 30, 120, "₹1,25,000"),
                PerformanceChannel("3", "Instagram", "Social Media", 30, 120, "₹65,000"),
                PerformanceChannel("4", "Referral", "Customer Referrals", 30, 120, "₹1,25,000"),
                PerformanceChannel("5", "Google Ads", "Paid Marketing", 30, 120, "₹80,000")
            )
        }
    }

    val isSalesReport = selectedReportType != "Order Reports" && selectedReportType != "Order Report"
    val negativeSuffix = if (isSalesReport) "Lost" else "Cancelled"
    val positiveSuffix = if (isSalesReport) "Converted" else "Completed"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {

                    Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                        Column {
                            TitleBar("Sales & Order Reports", onClose= onClose)

                        }
                    }
                    HorizontalDivider(color = BorderColor)

            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->

            // ── Background content area below TopBar with Blur ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .blurScrim(backgroundBlur)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ScreenBreadcrumb(
                                    listOf("Reports", "Sales & Order Reports"),
                                    onClick = { onBreadCrumbClick() }
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .clickable { exportSheetState = SheetValue.Collapsed }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.FileUpload,
                                    contentDescription = "Export",
                                    tint = ReportPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Export",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ReportPrimary
                                )
                            }
                        }
                    }

                    item {
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                                FormLabel("Report Type")
                            }
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
                                            .background(whiteBg, RoundedCornerShape(10.dp))
                                            .border(
                                                1.dp,
                                                Color(0xFFE5E7EB),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                reportTypeSheetState = SheetValue.Collapsed
                                            }
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            selectedReportType,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TitleColor
                                        )
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            null,
                                            tint = MutedColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(whiteBg, RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                        .clickable { filterDrawerState.open() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = TitleColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            stats.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    row.forEach { stat ->
                                        ReportStatCard(
                                            stat,
                                            Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Performance Breakdown",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = TitleColor
                            )
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MutedColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    items(channels, key = { it.id }) { channel ->
                        DataCard(
                            item = channel,
                            title = "${channel.name} • ${channel.subTitle}",
                            titleFontSize = 14.sp,
                            titleFontWeight = FontWeight.SemiBold,
                            trailingText = null,
                            footerAsRows = true,
                            footerFields = listOf(
                                DataCardField(
                                    text = "${channel.negativeCount} $negativeSuffix",
                                    textColor = LostRed,
                                    asRow = true
                                ),
                                DataCardField(
                                    text = "${channel.positiveCount} $positiveSuffix",
                                    textColor = ConvertedGreen,
                                    asRow = true
                                )
                            ),
                            actions = listOf(
                                MenuAction(
                                    label = "View",
                                    icon = Icons.Default.Visibility,
                                    onClick = { }
                                ),
                                MenuAction(
                                    label = "Export",
                                    icon = Icons.Default.FileUpload,
                                    onClick = { }
                                )
                            ),
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        channel.trailingText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TitleColor
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // ── Smooth Bottom Sheet for Report Type (Stops below TitleBar with topInset = 60.dp) ──
        SmoothBottomSheet(
            state = reportTypeSheetState,
            onStateChange = { reportTypeSheetState = it },
            peekHeight = 480.dp,
            topInset = 60.dp,
            maxBlurRadius = 16.dp,
            maxScrimAlpha = 0.45f,
            scrollableContent = false,
            onBlurScrimChange = { blur, _ -> reportTypeBlur = blur },
            onDismissRequest = { reportTypeSheetState = SheetValue.Hidden }
        ) {
            ReportTypeSelectorContent(
                selected = selectedReportType,
                onSelect = {
                    selectedReportType = it
                    reportTypeSheetState = SheetValue.Hidden
                }
            )
        }

        // ── Smooth Bottom Sheet for Export (Stops below TitleBar with topInset = 60.dp) ──
        SmoothBottomSheet(
            state = exportSheetState,
            onStateChange = { exportSheetState = it },
            peekHeight = 340.dp,
            topInset = 60.dp,
            maxBlurRadius = 16.dp,
            maxScrimAlpha = 0.45f,
            scrollableContent = false,
            onBlurScrimChange = { blur, _ -> exportBlur = blur },
            onDismissRequest = { exportSheetState = SheetValue.Hidden }
        ) {
            ExportReportSheetContent(onDismiss = { exportSheetState = SheetValue.Hidden })
        }
    }

    FilterDrawer(
        state = filterDrawerState,
        title = "Filter Report",
        sections = filterSections,
        onApply = { updated -> filterSections = updated },
        onClearAll = { },
        onBackgroundBlurChange = { blur -> filterBlur = blur }
    )
}

@Composable
private fun ReportStatCard(stat: ReportStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(whiteBg)
            .padding(14.dp)
    ) {
        Text(stat.label, fontSize = 11.sp, color = MutedColor, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(stat.value, fontSize = 12.sp, color = TitleColor, fontWeight = FontWeight.Bold)
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
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (stat.trendUp) ConvertedGreen else LostRed
            )
        }
    }
}

@Composable
private fun ReportTypeSelectorContent(
    selected: String,
    onSelect: (String) -> Unit
) {
    val recent = listOf("Sales Report", "Order Reports", "Customer Report")
    val favorites = listOf("Sales Report", "Order Reports", "Inventory Report")
    val all = listOf("Sales Report", "Order Reports", "Inventory Report", "Purchase Report", "Customer Report", "Delivery Report")

    var favoriteSet by remember { mutableStateOf(setOf("Sales Report", "Order Reports", "Inventory Report")) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Text("RECENT REPORTS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor)
            Spacer(Modifier.height(4.dp))
            recent.filter { it.contains(searchQuery, ignoreCase = true) }.forEach { label ->
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
            favorites.filter { it.contains(searchQuery, ignoreCase = true) }.forEach { label ->
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
            all.filter { it.contains(searchQuery, ignoreCase = true) }.forEach { label ->
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
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onFavoriteToggle)
        )
    }
}

@Composable
private fun ExportReportSheetContent(onDismiss: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
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