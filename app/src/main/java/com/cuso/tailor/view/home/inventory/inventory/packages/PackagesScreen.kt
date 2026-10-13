package com.cuso.tailor.view.home.inventory.inventory.packages
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.TrendingDown
//import androidx.compose.material.icons.automirrored.filled.TrendingUp
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.CalendarToday
//import androidx.compose.material.icons.filled.LocalShipping
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import com.cuso.tailor.adaptive_screen.LocalAppTokens
//import com.cuso.tailor.model.inventory.PackageItemDto
//import com.cuso.tailor.model.inventory.PackageSummaryDto
//import com.cuso.tailor.ui.theme.*
//import com.cuso.tailor.view.composable.*
//import com.cuso.tailor.viewmodel.InventoryViewModel
//import com.cuso.tailor.viewmodel.PackageUiState
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.TimeZone
//
//@Composable
//fun PackagesScreen(
//    modifier: Modifier = Modifier,
//    viewModel: InventoryViewModel = hiltViewModel(),
//    onNavigateBack: () -> Unit = {},
//    onCreatePackageClick: () -> Unit = {},
//    onPackageItemClick: (PackageItemDto) -> Unit = {}
//) {
//    val tokens = LocalAppTokens.current
//    val uiState by viewModel.packageUiState.collectAsState()
//    val searchQuery by viewModel.packageSearchQuery.collectAsState()
//    val filterDrawerState = rememberFilterDrawerState()
//
//    // Trigger initial data load
//    LaunchedEffect(Unit) {
//        viewModel.fetchPackages()
//    }
//
//    val filterSections = remember {
//        listOf(
//            FilterSection(
//                title = "Status",
//                options = listOf(
//                    FilterOption("not_shipped", "Not Shipped"),
//                    FilterOption("shipped", "Shipped"),
//                    FilterOption("delivered", "Delivered")
//                ),
//                isMultiSelect = false
//            )
//        )
//    }
//
//    FabScaffold(
//        modifier = modifier.fillMaxSize(),
//        fab = FabConfig(
//            label = "Package",
//            icon = Icons.Default.Add,
//            onClick = onCreatePackageClick
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Transparent)
//        ) {
//            // Screen Header Bar
//            TitleBar(
//                title = "Packages",
//                onClose = onNavigateBack
//            )
//
//            // Search and Filter Bar
//            SearchFilterBar(
//                query = searchQuery,
//                onQueryChange = viewModel::onPackageSearchQueryChanged,
//                placeholder = "Search package, customer...",
//                showFilterIcon = true,
//                onFilterClick = { filterDrawerState.open() }
//            )
//
//            // Content Container
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//            ) {
//                when (val state = uiState) {
//                    is PackageUiState.Loading -> {
//                        ListSkeleton()
//                    }
//
//                    // Enterprise Error State Reusable Component
//                    is PackageUiState.Error -> {
//                        AppErrorState(
//                            title = "Failed to load packages",
//                            message = state.message.ifBlank { "Something went wrong while fetching packages. Please check your connection and try again." },
//                            buttonText = "Retry",
//                            onRetry = {
//                                viewModel.fetchPackages()
//                            }
//                        )
//                    }
//
//                    is PackageUiState.Success -> {
//                        val packages = state.items
//                        val summary = state.summary
//
//                        LazyColumn(
//                            modifier = Modifier.fillMaxSize(),
//                            contentPadding = PaddingValues(
//                                horizontal = tokens.screenPadding,
//                                vertical = 8.dp
//                            ),
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            // Top 4 Metric Overview Cards
//                            item {
//                                PackageMetricsSection(summary = summary)
//                            }
//
//                            // Empty State Handling
//                            if (packages.isEmpty()) {
//                                item {
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(top = 40.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = "No packages found",
//                                            fontSize = tokens.bodyMedium,
//                                            fontWeight = FontWeight.Medium,
//                                            color = mutedText
//                                        )
//                                    }
//                                }
//                            } else {
//                                // Package Item List
//                                items(
//                                    items = packages,
//                                    key = { it.id }
//                                ) { pkg ->
//                                    PackageItemCard(
//                                        item = pkg,
//                                        onClick = { onPackageItemClick(pkg) }
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    is PackageUiState.Idle -> Unit
//                }
//            }
//        }
//
//        // Filter Drawer Integration
//        FilterDrawer(
//            state = filterDrawerState,
//            title = "Filters",
//            sections = filterSections,
//            onApply = { updatedSections ->
//                val chosenStatus = updatedSections
//                    .firstOrNull { it.title == "Status" }
//                    ?.options
//                    ?.firstOrNull { it.isSelected }
//                    ?.label
//                viewModel.onPackageStatusFilterChanged(chosenStatus)
//            },
//            onClearAll = {
//                viewModel.onPackageStatusFilterChanged(null)
//            }
//        )
//    }
//}
//
///**
// * 2x2 Grid Section displaying the summary metrics.
// */
//@Composable
//private fun PackageMetricsSection(
//    summary: PackageSummaryDto
//) {
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(10.dp)
//    ) {
//        // Row 1: Total Packages & Not Shipped
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            PackageMetricCard(
//                title = "Total Packages",
//                value = formatNumber(summary.total),
//                trendText = "8.4% vs last mo",
//                isPositive = true,
//                modifier = Modifier.weight(1f)
//            )
//            PackageMetricCard(
//                title = "Not Shipped",
//                value = formatNumber(summary.notShipped),
//                trendText = "12.6% vs last mo",
//                isPositive = true,
//                modifier = Modifier.weight(1f)
//            )
//        }
//
//        // Row 2: Shipped & Delivered
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            PackageMetricCard(
//                title = "Shipped",
//                value = formatNumber(summary.shipped),
//                trendText = "4.3% vs last mo",
//                isPositive = false,
//                modifier = Modifier.weight(1f)
//            )
//            PackageMetricCard(
//                title = "Delivered",
//                value = formatNumber(summary.delivered),
//                trendText = "11.8% vs last mo",
//                isPositive = true,
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}
//
///**
// * Reusable Metric Card with trend indicator.
// */
//@Composable
//private fun PackageMetricCard(
//    title: String,
//    value: String,
//    trendText: String,
//    isPositive: Boolean,
//    modifier: Modifier = Modifier
//) {
//    val tokens = LocalAppTokens.current
//
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f),
//        color = whiteBg,
//        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(14.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Text(
//                text = title,
//                fontSize = tokens.caption,
//                fontWeight = FontWeight.Medium,
//                color = mutedText
//            )
//
//            Text(
//                text = value,
//                fontSize = tokens.h2,
//                fontWeight = FontWeight.Bold,
//                color = TextPrimary
//            )
//
//            Spacer(modifier = Modifier.height(2.dp))
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
//                    contentDescription = null,
//                    tint = if (isPositive) greentext else redText,
//                    modifier = Modifier.size(14.dp)
//                )
//                Spacer(modifier = Modifier.width(3.dp))
//                Text(
//                    text = trendText,
//                    fontSize = 11.sp,
//                    color = if (isPositive) greentext else redText,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
/////**
//// * Individual Status Filter Chip with count badge.
//// */
////@SuppressLint("DefaultLocale")
////@Composable
////private fun StatusChipPill(
////    label: String,
////    count: Int,
////    dotColor: Color,
////    isSelected: Boolean,
////    onClick: () -> Unit
////) {
////    val tokens = LocalAppTokens.current
////
////    Surface(
////        modifier = Modifier
////            .clip(RoundedCornerShape(20.dp))
////            .clickable { onClick() },
////        shape = RoundedCornerShape(20.dp),
////        color = if (isSelected) Primary else whiteBg,
////        border = androidx.compose.foundation.BorderStroke(
////            width = 1.dp,
////            color = if (isSelected) Primary else BorderGray
////        )
////    ) {
////        Row(
////            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
////            verticalAlignment = Alignment.CenterVertically,
////            horizontalArrangement = Arrangement.spacedBy(6.dp)
////        ) {
////            Box(
////                modifier = Modifier
////                    .size(6.dp)
////                    .clip(CircleShape)
////                    .background(if (isSelected) Color.White else dotColor)
////            )
////
////            Text(
////                text = label,
////                fontSize = tokens.caption,
////                fontWeight = FontWeight.Medium,
////                color = if (isSelected) Color.White else TextPrimary
////            )
////
////            // Count Badge
////            Box(
////                modifier = Modifier
////                    .clip(RoundedCornerShape(10.dp))
////                    .background(
////                        if (isSelected) Color.White.copy(alpha = 0.25f)
////                        else modelBg
////                    )
////                    .padding(horizontal = 6.dp, vertical = 2.dp)
////            ) {
////                Text(
////                    text = String.format("%02d", count),
////                    fontSize = 11.sp,
////                    fontWeight = FontWeight.Bold,
////                    color = if (isSelected) Color.White else TextSecondary
////                )
////            }
////        }
////    }
////}
//
///**
// * Package Data Card displaying package number, sales order, and status details.
// */
//@SuppressLint("DefaultLocale")
//@Composable
//private fun PackageItemCard(
//    item: PackageItemDto,
//    onClick: () -> Unit
//) {
//    val tokens = LocalAppTokens.current
//
//    val (statusBg, statusTextColor) = when (item.status?.lowercase()) {
//        "delivered" -> Pair(greenBg, greentext)
//        "shipped" -> Pair(light_blue_border, Primary)
//        else -> Pair(orangeBg, orangeText)
//    }
//
//    val formattedDate = remember(item.createdAt) {
//        formatIsoDate(item.createdAt)
//    }
//
//    val menuActions = remember(item) {
//        listOf(
//            MenuAction(
//                label = "View",
//                textColor = TextPrimary,
//                onClick = onClick
//            ),
//            MenuAction(
//                label = "Delete",
//                textColor = redText,
//                onClick = { /* Action callback */ }
//            )
//        )
//    }
//
//    DataCard(
//        item = item,
//        title = item.customerDisplay,
//        subtitle = "${item.packageNumber ?: "PCK"} • ${item.salesOrderId ?: "SO"}",
//        titleFontWeight = FontWeight.Bold,
//        titleColor = title_color,
//        topBadgeText = "Qty ${String.format("%.2f", item.totalQty)}",
//        topBadgeBgColor = modelBg,
//        topBadgeTextColor = textSubdued,
//        topBadgeShowDot = false,
//        topBadgeInline = true,
//        actions = menuActions,
//        onClick = { onClick() },
//        showDivider = false,
//        content = {
//            Column(modifier = Modifier.fillMaxWidth()) {
//                Spacer(modifier = Modifier.height(6.dp))
//
//                // Footer Row: Carrier / Date and Status Chip
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Date & Logistics Info
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        Icon(
//                            imageVector = if (item.status?.equals("Shipped", ignoreCase = true) == true) {
//                                Icons.Default.LocalShipping
//                            } else {
//                                Icons.Default.CalendarToday
//                            },
//                            contentDescription = null,
//                            tint = mutedText,
//                            modifier = Modifier.size(14.dp)
//                        )
//
//                        Text(
//                            text = formattedDate.ifBlank { "Date unavailable" },
//                            fontSize = tokens.caption,
//                            fontWeight = FontWeight.Medium,
//                            color = TextSecondary
//                        )
//                    }
//
//                    // Status Pill
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = statusBg
//                    ) {
//                        Text(
//                            text = item.status ?: "Not Shipped",
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = statusTextColor,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                        )
//                    }
//                }
//            }
//        }
//    )
//}
//
///**
// * Formats integer values with thousands comma separator.
// */
//@SuppressLint("DefaultLocale")
//private fun formatNumber(number: Int): String {
//    return String.format("%,d", number)
//}
//
///**
// * Formats ISO date string to "dd MMM yyyy".
// */
//private fun formatIsoDate(isoDate: String?): String {
//    if (isoDate.isNullOrBlank()) return ""
//    return try {
//        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
//            timeZone = TimeZone.getTimeZone("UTC")
//        }
//        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//        val parsed = parser.parse(isoDate)
//        if (parsed != null) formatter.format(parsed) else isoDate
//    } catch (_: Exception) {
//        isoDate
//    }
//}