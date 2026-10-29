@file:Suppress("unused")

package com.cuso.tailor.view.home.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState

// ── Shared Models ──
data class ReportItem(
    val id: String,
    val title: String,
    val description: String,
    val isFavorite: Boolean = false
)

data class ReportCategory(
    val id: String,
    val title: String,
    val reports: List<ReportItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreenTemplate(
    title: String,
    categories: List<ReportCategory>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search reports by name, module or author...",
    dateRangeOptions: List<String> = listOf(
        "Today",
        "Last 7 Days",
        "Last 30 Days",
        "This Month",
        "This Quarter",
        "This Year"
    ),
    initialSelectedDateRange: String = "Last 30 Days",
    onDateRangeSelected: (String) -> Unit = {},
    filterSections: List<FilterSection> = emptyList(),
    onFilterApply: (List<FilterSection>) -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {},
    onExportExcel: (ReportItem) -> Unit = {},
    onExportPdf: (ReportItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedDateRange by remember { mutableStateOf(initialSelectedDateRange) }
    var dateDropdownExpanded by remember { mutableStateOf(false) }

    val filterDrawerState = rememberFilterDrawerState()
    var filterBlur by remember { mutableStateOf(0.dp) }
    var activeFilterSections by remember { mutableStateOf(filterSections) }

    // Count currently selected filter options to display on badge
    val activeFilterCount = remember(activeFilterSections) {
        activeFilterSections.flatMap { it.options }.count { it.isSelected && it.id != "all" }
    }

    // Favorite report tracking
    var favoriteReportIds by remember(categories) {
        mutableStateOf(
            categories.flatMap { it.reports }
                .filter { it.isFavorite }
                .map { it.id }
                .toSet()
        )
    }

    // Single expanded accordion ID (auto-closes others)
    var expandedCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.id)
    }

    // Filter categories and sub-reports based on search query
    val displayedCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.mapNotNull { category ->
                val categoryMatches = category.title.contains(searchQuery, ignoreCase = true)
                val matchingReports = category.reports.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true)
                }
                if (categoryMatches || matchingReports.isNotEmpty()) {
                    category.copy(reports = matchingReports.ifEmpty { category.reports })
                } else null
            }
        }
    }

    // Auto-open first matching category when searching
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            expandedCategoryId = displayedCategories.firstOrNull()?.id
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Primary_background,
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    Column {
                        TitleBar(title = title, onClose = onClose)
                        HorizontalDivider(color = dividerColor)
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // ── Search & Filter Bar Component ──
                    item {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = searchPlaceholder,
                            showFilterIcon = activeFilterSections.isNotEmpty(),
                            filterCount = activeFilterCount,
                            onFilterClick = { filterDrawerState.open() },
                            accentColor = Primary,
                            borderColor = sectionBorder,
                            height = tokens.fieldHeight
                        )
                    }

//                    // ── Date Range Dropdown Selector ──
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = tokens.screenPadding)
//                        ) {
//                            Surface(
//                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
//                                border = BorderStroke(1.dp, sectionBorder),
//                                color = whiteBg,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(tokens.fieldHeight)
//                                    .clickable { dateDropdownExpanded = true }
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .padding(horizontal = 12.dp),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                ) {
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Outlined.CalendarToday,
//                                            contentDescription = null,
//                                            tint = headerGrey,
//                                            modifier = Modifier.size(tokens.iconSize * 0.9f)
//                                        )
//                                        Text(
//                                            text = selectedDateRange,
//                                            fontSize = tokens.bodySmall,
//                                            fontWeight = FontWeight.Medium,
//                                            color = TextPrimary
//                                        )
//                                    }
//                                    Icon(
//                                        imageVector = Icons.Default.KeyboardArrowDown,
//                                        contentDescription = null,
//                                        tint = headerGrey,
//                                        modifier = Modifier.size(tokens.iconSize)
//                                    )
//                                }
//                            }
//
//                            DropdownMenu(
//                                expanded = dateDropdownExpanded,
//                                onDismissRequest = { dateDropdownExpanded = false },
//                                offset = DpOffset(0.dp, 4.dp),
//                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
//                                modifier = Modifier
//                                    .background(whiteBg)
//                                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
//                            ) {
//                                dateRangeOptions.forEach { range ->
//                                    DropdownMenuItem(
//                                        text = {
//                                            Text(
//                                                text = range,
//                                                fontSize = tokens.bodySmall,
//                                                color = if (range == selectedDateRange) Primary else TextPrimary,
//                                                fontWeight = FontWeight.Medium
//                                            )
//                                        },
//                                        onClick = {
//                                            selectedDateRange = range
//                                            dateDropdownExpanded = false
//                                            onDateRangeSelected(range)
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }

                    // ── Category Report Accordions ──
                    items(displayedCategories, key = { it.id }) { category ->
                        Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                            ReportCard(
                                category = category,
                                isExpanded = expandedCategoryId == category.id,
                                favoriteReportIds = favoriteReportIds,
                                onToggleExpand = {
                                    expandedCategoryId = if (expandedCategoryId == category.id) null else category.id
                                },
                                onToggleFavorite = { reportId ->
                                    favoriteReportIds = if (favoriteReportIds.contains(reportId)) {
                                        favoriteReportIds - reportId
                                    } else {
                                        favoriteReportIds + reportId
                                    }
                                },
                                onReportClick = onReportClick,
                                onExportExcel = onExportExcel,
                                onExportPdf = onExportPdf
                            )
                        }
                    }
                }

                // Filter Drawer
                if (activeFilterSections.isNotEmpty()) {
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filter Reports",
                        sections = activeFilterSections,
                        onApply = { updated ->
                            activeFilterSections = updated
                            onFilterApply(updated)
                        },
                        onClearAll = { },
                        onBackgroundBlurChange = { blur -> filterBlur = blur }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Common Report Card Composable (Single Accordion Item)
// ─────────────────────────────────────────────────────────────

@Composable
fun ReportCard(
    category: ReportCategory,
    isExpanded: Boolean,
    favoriteReportIds: Set<String>,
    onToggleExpand: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onReportClick: (ReportItem) -> Unit,
    modifier: Modifier = Modifier,
    onExportExcel: (ReportItem) -> Unit = {},
    onExportPdf: (ReportItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(tokens.fieldHeight * 0.8f)
//                            .background(
//                                primary_light,
//                                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            painter = painterResource() category.icon,
//                            contentDescription = null,
//                            tint = Primary,
//                            modifier = Modifier.size(tokens.iconSize)
//                        )
//                    }

                    Text(
                        text = category.title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                // Chevron Indicator Icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(badgeGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .rotate(rotationState)
                    )
                }
            }

            // Accordion Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    // Sub-header Row (REPORT NAME / ACTIONS)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeGrey)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REPORT NAME",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                        Text(
                            text = "ACTIONS",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                    }

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    // Report List Rows
                    category.reports.forEachIndexed { index, report ->
                        ReportItemRow(
                            report = report,
                            isFavorite = favoriteReportIds.contains(report.id),
                            onReportClick = onReportClick,
                            onToggleFavorite = { onToggleFavorite(report.id) },
                            onExportExcel = { onExportExcel(report) },
                            onExportPdf = { onExportPdf(report) }
                        )

                        if (index < category.reports.lastIndex) {
                            HorizontalDivider(
                                color = dividerColor,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Common Report Item Row Composable
// ─────────────────────────────────────────────────────────────

@Composable
fun ReportItemRow(
    report: ReportItem,
    isFavorite: Boolean,
    onReportClick: (ReportItem) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    onExportExcel: () -> Unit = {},
    onExportPdf: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onReportClick(report) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Star Icon
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Primary else iconMuted,
                modifier = Modifier
                    .size(tokens.iconSize)
                    .clickable { onToggleFavorite() }
            )

            // Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = report.description,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey,
                    lineHeight = tokens.bodySmall
                )
            }
        }

        // Actions Three-Dot Menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(tokens.iconSize * 1.4f)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions",
                    tint = iconMuted,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                modifier = Modifier
                    .background(whiteBg)
                    .border(
                        1.dp,
                        sectionBorder,
                        RoundedCornerShape(tokens.cardCornerRadius * 0.7f)
                    )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "View Report",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onReportClick(report)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Export as Excel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onExportExcel()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Export as PDF",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onExportPdf()
                    }
                )
            }
        }
    }
}