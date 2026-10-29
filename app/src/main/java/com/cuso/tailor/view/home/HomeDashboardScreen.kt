@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.model.home.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.HomeSkeleton
import com.cuso.tailor.viewmodel.HomeViewModel
import java.time.LocalTime
import androidx.core.graphics.toColorInt
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.view.composable.FormDropdown
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeDashboardScreen(
    widthSizeClass: WindowWidthSizeClass,
    adminName: String = "Admin",
    viewModel: HomeViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val designTokens = getAdaptiveTokens(widthSizeClass)
    val baseDensity = LocalDensity.current

    // Load branches from BranchViewModel
    val branchState by branchViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    // Branch selection states
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var selectedBranchLabel by remember { mutableStateOf("All Branches") }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    // Period options mapping
    val periodMapping = remember {
        listOf(
            "This Month (MTD)" to "this_month",
            "This Quarter"     to "this_quarter",
            "This Year (YTD)"   to "this_year"
        )
    }
    var selectedPeriodLabel by remember { mutableStateOf(periodMapping[0].first) }
    var selectedPeriodKey by remember { mutableStateOf(periodMapping[0].second) }
    var periodDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-select default/first branch once loaded
    val branchItems = (branchState as? BranchUiState.Success)?.branches.orEmpty()
    LaunchedEffect(branchItems) {
        if (branchItems.isNotEmpty() && selectedBranchId == null) {
            val defaultBranch = branchItems.find { it.isMainBranch } ?: branchItems.first()
            selectedBranchId = defaultBranch.id
            selectedBranchLabel = defaultBranch.name?:""
            viewModel.fetchAllHomeDashboardData(
                branchId = defaultBranch.id,
                period = selectedPeriodKey
            )
        }
    }

    // Collect API states
    val kpis by viewModel.kpis.collectAsState()
    val revenueTrend by viewModel.revenueTrend.collectAsState()
    val marketing by viewModel.marketing.collectAsState()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val stockStatus by viewModel.stockStatus.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val serviceStatus by viewModel.serviceStatus.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val actionTasks by viewModel.actionTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f),
        LocalAppTokens provides designTokens
    ) {
        val tokens = LocalAppTokens.current

        if (isLoading && kpis == null) {
            HomeSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.screenPadding
                ),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {


                // 1. Top Greeting Card
                item {
                    GreetingCard(
                        userName = adminName,
                        newLeadsCount = actionTasks.find { it.module.equals("Sales", ignoreCase = true) }?.count ?: 0,
                        onNavigate = onNavigate
                    )
                }

                // 2. Top Filter Row: Branch & Period Dropdowns
                item {
                    val branchOptions = remember(branchItems) {
                        listOf("All Branches") + branchItems.map { it.name?:"" }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // Branch Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Branch",
                                value = selectedBranchLabel,
                                expanded = branchDropdownExpanded,
                                onExpandChange = { branchDropdownExpanded = it },
                                options = branchOptions,
                                onOptionSelected = { chosen ->
                                    selectedBranchLabel = chosen
                                    selectedBranchId = if (chosen == "All Branches") {
                                        null
                                    } else {
                                        branchItems.find { (it.name ?: "") == chosen }?.id
                                    }
                                    viewModel.fetchAllHomeDashboardData(
                                        branchId = selectedBranchId,
                                        period = selectedPeriodKey
                                    )
                                }
                            )
                        }

                        // Period Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Period",
                                value = selectedPeriodLabel,
                                expanded = periodDropdownExpanded,
                                onExpandChange = { periodDropdownExpanded = it },
                                options = periodMapping.map { it.first },
                                onOptionSelected = { chosenLabel ->
                                    selectedPeriodLabel = chosenLabel
                                    selectedPeriodKey = periodMapping.firstOrNull { it.first == chosenLabel }?.second ?: "this_month"
                                    viewModel.fetchAllHomeDashboardData(
                                        branchId = selectedBranchId,
                                        period = selectedPeriodKey
                                    )
                                }
                            )
                        }
                    }
                }

                // ... Remaining dashboard items (2 to 11) stay untouched ...

                // 2. 6 Metrics Grid
                item {
                    KeyMetricsSection(kpis = kpis)
                }

                // 3. Sales Targets Progress Bar (Animated)
                item {
                    SalesTargetsSection(revenueData = revenueTrend)
                }

                // 4. Order Status Donut Chart (Animated)
                item {
                    orderStatus?.let { data ->
                        DonutChartCard(
                            title = data.title.ifBlank { "Order Status" },
                            subtitle = data.subtitle,
                            centerValue = "${data.totalOrders ?: 0}",
                            centerLabel = "ORDERS",
                            segments = data.segments.map { DonutSegment(it.count, parseColorSafely(it.color)) },
                            legends = data.segments.map { LegendItemData(it.status, "${it.count.toInt()}", parseColorSafely(it.color)) }
                        )
                    }
                }

                // 5. Stock Status Donut Chart (Animated)
                item {
                    stockStatus?.let { data ->
                        DonutChartCard(
                            title = data.title.ifBlank { "Stock Status" },
                            subtitle = data.subtitle,
                            centerValue = "${data.totalSKUs ?: 0}",
                            centerLabel = "SKUS",
                            segments = data.segments.map { DonutSegment(it.count, parseColorSafely(it.color)) },
                            legends = data.segments.map { LegendItemData(it.status, "${it.count.toInt()}", parseColorSafely(it.color)) }
                        )
                    }
                }

                // 6. Today's Attendance Donut Chart (Animated)
                item {
                    attendance?.let { data ->
                        DonutChartCard(
                            title = data.title.ifBlank { "Today's Attendance" },
                            subtitle = data.subtitle,
                            centerValue = "${data.totalEmployees ?: 0}",
                            centerLabel = "EMPLOYEES",
                            segments = data.segments.map { DonutSegment(it.count, parseColorSafely(it.color)) },
                            legends = data.segments.map { LegendItemData(it.status, "${it.count.toInt()}", parseColorSafely(it.color)) }
                        )
                    }
                }

                // 7. Marketing Performance Bars (Animated)
                item {
                    MarketingPerformanceSection(marketingData = marketing)
                }

                // 8. Service Status Donut Chart (Animated)
                item {
                    serviceStatus?.let { data ->
                        DonutChartCard(
                            title = data.title.ifBlank { "Service Status" },
                            subtitle = data.subtitle,
                            centerValue = "${data.totalRequests ?: 0}",
                            centerLabel = "REQUESTS",
                            segments = data.segments.map { DonutSegment(it.count, parseColorSafely(it.color)) },
                            legends = data.segments.map { LegendItemData(it.status, "${it.count.toInt()}", parseColorSafely(it.color)) }
                        )
                    }
                }

                // 9. Top Products Ranking List
                item {
                    TopProductsSection(
                        products = topProducts,
                        onViewDetails = { onNavigate("inventory_items") }
                    )
                }

                // 10. Action Required Task Cards
                item {
                    ActionRequiredSection(
                        actions = actionTasks,
                        onResolveClick = { route -> onNavigate(route) }
                    )
                }

                // 11. Business Health Status Cards
                item {
                    BusinessHealthSection(onViewModule = { onNavigate("module_settings") })
                }

                item {
                    Spacer(Modifier.height(tokens.screenPadding * 2))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. GREETING CARD
// ─────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GreetingCard(
    userName: String,
    newLeadsCount: Int,
    onNavigate: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(Primary)
            .clickable { onNavigate("sales_lead") }
            .padding(tokens.screenPadding)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = whiteBg.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(tokens.iconSize * 4f)
                .offset(x = tokens.extraPadding, y = tokens.extraPadding)
        )

        Column {
            Text(
                text = "$greeting, $userName",
                color = whiteBg,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
            Text(
                text = "You have $newLeadsCount items requiring your attention today.",
                color = whiteBg.copy(alpha = 0.85f),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. KEY METRICS (From KPI API)
// ─────────────────────────────────────────────────────────────
@Composable
private fun KeyMetricsSection(kpis: HomeKpisGroup?) {
    val tokens = LocalAppTokens.current

    val metrics = remember(kpis) {
        listOf(
            MetricItem("Sales Revenue", "${currencySymbol()}${formatCurrency(kpis?.salesRevenue?.amount ?: 0.0)}", kpis?.salesRevenue?.growth ?: "0%", kpis?.salesRevenue?.trendUp),
            MetricItem("Total Orders", "${kpis?.totalOrders?.count ?: 0}", kpis?.totalOrders?.growth ?: "0%", kpis?.totalOrders?.trendUp),
            MetricItem("Net Profit", "${currencySymbol()}${formatCurrency(kpis?.netProfit?.amount ?: 0.0)}", kpis?.netProfit?.growth ?: "0%", kpis?.netProfit?.trendUp),
            MetricItem("Receivables", "${currencySymbol()}${formatCurrency(kpis?.receivables?.amount ?: 0.0)}", kpis?.receivables?.overdueLabel ?: "Outstanding claims", null),
            MetricItem("Inventory Value", "${currencySymbol()}${formatCurrency(kpis?.inventoryValue?.amount ?: 0.0)}", kpis?.inventoryValue?.reorderLabel ?: "Total stock assets", null),
            MetricItem("Delivery Success", kpis?.deliverySuccess?.percentage ?: "100%", kpis?.deliverySuccess?.comparisonLabel ?: "On-time fulfillment", true)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Text(
            text = "Key Metrics",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
        Text(
            text = "Overview of operational performance",
            fontSize = tokens.caption,
            color = iconMuted
        )

        metrics.chunked(tokens.gridColumns).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                rowMetrics.forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(modifier = Modifier.padding(tokens.extraPadding)) {
                            Text(
                                text = item.title,
                                fontSize = tokens.bodySmall,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                            Text(
                                text = item.value,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.isPositive != null) {
                                    Icon(
                                        imageVector = if (item.isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = if (item.isPositive) darkGreenBg else redText,
                                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                                    )
                                }
                                Text(
                                    text = item.subtitle,
                                    fontSize = tokens.caption,
                                    color = when (item.isPositive) {
                                        true -> darkGreenBg
                                        false -> redText
                                        else -> iconMuted
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class MetricItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val isPositive: Boolean? = null
)

// ─────────────────────────────────────────────────────────────
// 3. SALES TARGETS (From Revenue Trends API)
// ─────────────────────────────────────────────────────────────
@Composable
private fun SalesTargetsSection(revenueData: RevenueTrendData?) {
    val tokens = LocalAppTokens.current
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isStarted = true }

    val currentTotal = revenueData?.summary?.totalCurrentRevenue ?: 0.0
    val targetVal = 100000.0 // Baseline target
    val achievedFraction = (currentTotal / targetVal).coerceIn(0.0, 1.0).toFloat()

    val achievedProgress by animateFloatAsState(
        targetValue = if (isStarted) achievedFraction else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "AchievedProgressAnim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Sales Targets",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = "Monthly progress vs goal (${revenueData?.summary?.overallGrowth ?: "+0%"})",
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Achieved Revenue", fontSize = tokens.bodySmall, color = headerGrey)
                Text(
                    text = "${currencySymbol()}${formatCurrency(currentTotal)}",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = darkGreenBg
                )
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
            LinearProgressIndicator(
                progress = { achievedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.extraPadding * 0.6f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f)),
                color = Primary,
                trackColor = sectionBorder
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE ANIMATED DONUT CHART COMPONENT
// ─────────────────────────────────────────────────────────────
private data class DonutSegment(val value: Float, val color: Color)
private data class LegendItemData(val label: String, val count: String, val color: Color)

@Composable
private fun DonutChartCard(
    title: String,
    subtitle: String,
    centerValue: String,
    centerLabel: String,
    segments: List<DonutSegment>,
    legends: List<LegendItemData>
) {
    val tokens = LocalAppTokens.current
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(segments) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(tokens.cardHeight * 1.1f),
                    contentAlignment = Alignment.Center
                ) {
                    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
                    val strokeWidth = tokens.extraPadding * 1.5f

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        segments.forEach { segment ->
                            val fullSweepAngle = (segment.value / total) * 360f
                            val sweepAngle = fullSweepAngle * animatedProgress.value
                            drawArc(
                                color = segment.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                            )
                            startAngle += fullSweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = centerValue,
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = centerLabel,
                            fontSize = tokens.label,
                            color = iconMuted,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.width(tokens.screenPadding))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                ) {
                    legends.forEach { legend ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(tokens.iconSize * 0.45f)
                                        .clip(CircleShape)
                                        .background(legend.color)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                                Text(
                                    text = legend.label,
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }
                            Text(
                                text = legend.count,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 7. MARKETING PERFORMANCE (From Marketing API)
// ─────────────────────────────────────────────────────────────
@Composable
private fun MarketingPerformanceSection(marketingData: HomeMarketingData?) {
    val tokens = LocalAppTokens.current
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isStarted = true }

    val channels = marketingData?.channels.orEmpty()
    val headerStats = marketingData?.headerStats

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Marketing Performance",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = headerStats?.badgeText ?: "ROI 0.0x | 0 Leads Generated",
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            channels.forEach { item ->
                val progress by animateFloatAsState(
                    targetValue = if (isStarted) item.barProgress else 0f,
                    animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                    label = "ChannelProgress"
                )

                Column(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.channel, fontSize = tokens.caption, color = headerGrey)
                        Text(
                            text = "${currencySymbol()}${formatCurrency(item.amount)}",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.extraPadding * 0.5f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.2f)),
                        color = parseColorSafely(item.color),
                        trackColor = grey_border
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 9. TOP PRODUCTS (From Top Products API)
// ─────────────────────────────────────────────────────────────
@Composable
private fun TopProductsSection(
    products: List<LeaderboardProductItem>,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Top Products & Services",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = "Leading catalog items by sales and momentum",
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            products.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = tokens.extraPadding * 0.6f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                        Text(
                            text = "Orders: ${item.orders}   ${currencySymbol()}${formatCurrency(item.revenue)}",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                    Text(
                        text = item.growth,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (item.growth.startsWith("-")) redText else darkGreenBg
                    )
                }
                if (index != products.lastIndex) {
                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewDetails() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View Details →",
                    color = Primary,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 10. ACTION REQUIRED (From Action Required API)
// ─────────────────────────────────────────────────────────────
@Composable
private fun ActionRequiredSection(
    actions: List<HomeActionTaskItem>,
    onResolveClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Text(
            text = "Action Required",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
        Text(
            text = "Important items requiring attention across your business",
            fontSize = tokens.caption,
            color = iconMuted
        )

        actions.forEach { item ->
            val priorityColor = parseColorSafely(item.priorityColor)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding * 0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.module,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                                color = priorityColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = item.priority,
                                    fontSize = tokens.label,
                                    color = priorityColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.5f, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.actionRequired,
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = item.due, fontSize = tokens.label, color = iconMuted)
                        Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                        Text(
                            text = "Resolve",
                            color = Primary,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                val targetRoute = mapModuleToRoute(item.module)
                                onResolveClick(targetRoute)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 11. BUSINESS HEALTH SECTION
// ─────────────────────────────────────────────────────────────
@Composable
private fun BusinessHealthSection(onViewModule: () -> Unit) {
    val tokens = LocalAppTokens.current
    val items = listOf(
        HealthItem("IT", "99.8% System Uptime • 2 Critical Tickets", true),
        HealthItem("Legal", "94% Compliance • 3 Contracts Expiring", false),
        HealthItem("Security", "96% Security Health • 2 Critical Alerts", true),
        HealthItem("HR", "Payroll Ready • 4 Pending Approvals", false),
        HealthItem("Inventory", "Stock Healthy • 18 Reorders", false),
        HealthItem("Logistics", "96.8% Delivery Success • 7 Delays", true)
    )

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Text(
            text = "Business Health",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
        Text(
            text = "Current health across key operational areas",
            fontSize = tokens.caption,
            color = iconMuted
        )

        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding * 0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.subtitle,
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = if (item.isHealthy) greenBg else yellowBg
                    ) {
                        Text(
                            text = if (item.isHealthy) "Healthy" else "Attention",
                            color = if (item.isHealthy) darkGreenBg else yellowText,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.8f, vertical = tokens.extraPadding * 0.3f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.6f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewModule() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "View Module →",
                color = Primary,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class HealthItem(
    val title: String,
    val subtitle: String,
    val isHealthy: Boolean
)

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────
@SuppressLint("DefaultLocale")
private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}

private fun parseColorSafely(hexColor: String): Color {
    return try {
        Color(hexColor.toColorInt())
    } catch (_: Exception) {
        BluePrimary
    }
}

private fun mapModuleToRoute(module: String): String {
    return when (module.lowercase()) {
        "sales" -> "sales_lead"
        "finance" -> "finance_sales_invoices"
        "inventory" -> "inventory_low_stock_alerts"
        "logistics" -> "logistics_delivery"
        "services" -> "services_service_status"
        "hr" -> "hr_leave_approval"
        else -> "module_settings"
    }
}