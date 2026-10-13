@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER", "AssignedValueIsNeverRead", "unusedVariable")

package com.cuso.tailor.view.home.sales.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.headerBg
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.SalesViewModel
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

// =============================================================
// ACCENT PALETTE (taken from the reference design)
// =============================================================

private val Indigo = Color(0xFF4F46E5)
private val IndigoTrack = Color(0xFFEEF0F6)
private val Emerald = Color(0xFF10B981)
private val Blue = Color(0xFF3B82F6)
private val Amber = Color(0xFFF59E0B)
private val Red = Color(0xFFEF4444)
private val Purple = Color(0xFFA855F7)

private val PageSubtitle = Color(0xFF94A3B8)

// =============================================================
// UI MODELS (single adapter layer between API models and composables)
// =============================================================

private data class KpiAccent(val bg: Color, val tint: Color, val spark: Color)

private val AccentBlue = KpiAccent(Color(0xFFEAF0FF), Color(0xFF4F6BED), Color(0xFF3B6FE0))
private val AccentGreen = KpiAccent(Color(0xFFE6F8F0), Emerald, Emerald)
private val AccentAmber = KpiAccent(Color(0xFFFFF3E0), Amber, Amber)
private val AccentSky = KpiAccent(Color(0xFFE5F3FD), Color(0xFF0EA5E9), Color(0xFF0EA5E9))
private val AccentRed = KpiAccent(Color(0xFFFDECEF), Red, Red)

private data class KpiUi(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val trend: String,
    val trendUp: Boolean,
    val spark: List<Float>,
    val accent: KpiAccent
)

private data class TrendUi(val label: String, val revenue: Double, val orders: Float)
private data class HealthUi(val label: String, val percent: Float)
private data class SliceUi(val label: String, val amount: Float, val valueText: String, val percentText: String, val color: Color)
private data class CategoryUi(val label: String, val valueText: String, val percent: Float)
private data class OrderRowUi(val id: String, val customer: String, val owner: String, val status: String, val amount: Long)
private data class FollowUpRowUi(val name: String, val stage: String, val priority: String)
private data class DonutSegment(val value: Float, val color: Color)
private data class TableColumn(val header: String, val width: Dp, val alignEnd: Boolean = false)
private data class ChipStyle(val bg: Color, val border: Color, val fg: Color)

// =============================================================
// SCREEN
// =============================================================

@Composable
fun SalesDashboardScreen(
    onClose: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToQuotations: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToLeads: () -> Unit = {},
    onNavigateToPipeline: () -> Unit = {},
    viewModel: SalesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Filter state, initialized from the current device date
    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var selectedPeriod by remember { mutableStateOf("this_month") }

    // True while only the Order Status Mix card should show its skeleton (month or year changed)
    var statusOnlyReload by remember { mutableStateOf(false) }
    // Live API observers
    val kpiMetrics by viewModel.kpiMetrics.collectAsStateWithLifecycle()
    val chartsData by viewModel.chartsData.collectAsStateWithLifecycle()
    val executiveAnalytics by viewModel.executiveAnalytics.collectAsStateWithLifecycle()
    val opportunityAnalytics by viewModel.opportunityAnalytics.collectAsStateWithLifecycle()
    val recentOrders by viewModel.recentOrders.collectAsStateWithLifecycle()
    val recentOpportunities by viewModel.recentOpportunities.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingDashboard.collectAsStateWithLifecycle()
    // Clear the card-only flag once the fetch has finished
    LaunchedEffect(isLoading) {
        if (!isLoading) statusOnlyReload = false
    }

    // Every card except Order Status Mix ignores a reload that came from the month or year picker
    val globalLoading = isLoading && !statusOnlyReload
    // Order Status Mix shows its skeleton for any load
    val statusLoading = isLoading || statusOnlyReload
    // Refetch whenever a filter changes
    LaunchedEffect(selectedMonth, selectedYear, selectedPeriod) {
        viewModel.fetchSalesDashboardData(
            month = selectedMonth,
            year = selectedYear,
            period = selectedPeriod
        )
    }

    // Options shown in the top bar dropdown: label to API value
    val periodOptions = listOf(
        "Period" to "period",
        "Today" to "today",
        "This Week" to "this_week",
        "This Month" to "this_month",
        "This Year" to "this_year",
        "Custom" to "custom"
    )
    val periodTitle = periodOptions.firstOrNull { it.second == selectedPeriod }?.first ?: "This Month"
    val rangeLabel = periodRangeLabel(selectedPeriod, selectedMonth, selectedYear)

    // ---------------- API -> UI mapping ----------------

    val cards = kpiMetrics?.kpiCards
    val kpiItems = listOf(
        KpiUi(
            Icons.Default.CurrencyRupee, "Total Revenue",
            cards?.totalRevenue?.formattedValue ?: "₹0",
            cards?.totalRevenue?.trend ?: "0%",
            cards?.totalRevenue?.trendUp ?: true,
            cards?.totalRevenue?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentBlue
        ),
        KpiUi(
            Icons.Default.ShoppingBag, "Sales Orders",
            cards?.salesOrderCount?.formattedValue ?: "0",
            cards?.salesOrderCount?.trend ?: "0%",
            cards?.salesOrderCount?.trendUp ?: true,
            cards?.salesOrderCount?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentGreen
        ),
        KpiUi(
            Icons.Default.Description, "Quotations Sent",
            cards?.quotationsSentCount?.formattedValue ?: "0",
            cards?.quotationsSentCount?.trend ?: "0%",
            cards?.quotationsSentCount?.trendUp ?: true,
            cards?.quotationsSentCount?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentAmber
        ),
        KpiUi(
            Icons.Default.Group, "Active Customers",
            cards?.activeCustomerCount?.formattedValue ?: "0",
            cards?.activeCustomerCount?.trend ?: "0%",
            cards?.activeCustomerCount?.trendUp ?: true,
            cards?.activeCustomerCount?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentSky
        ),
        KpiUi(
            Icons.Default.Schedule, "Pending Payments",
            cards?.pendingReceivables?.formattedValue ?: "₹0",
            cards?.pendingReceivables?.trend ?: "0%",
            cards?.pendingReceivables?.trendUp ?: true,
            cards?.pendingReceivables?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentRed
        ),
        KpiUi(
            Icons.Default.Percent, "Conversion Rate",
            cards?.conversionRate?.formattedValue ?: "0%",
            cards?.conversionRate?.trend ?: "0%",
            cards?.conversionRate?.trendUp ?: true,
            cards?.conversionRate?.sparkline?.map { it.toFloat() }.orEmpty(),
            AccentGreen
        )
    )

    val trendPoints = chartsData?.revenueOrderTrend.orEmpty().map {
        TrendUi(it.name, it.revenue, it.orders.toFloat())
    }

    // Pipeline and target health is derived from the opportunity stage funnel
    val healthRows = opportunityAnalytics?.stageFunnel.orEmpty().map {
        HealthUi(it.stageName, it.probability.toFloat())
    }

    val statusSlices = chartsData?.statusPieChart.orEmpty().map { slice ->
        SliceUi(
            label = slice.name,
            amount = slice.count.toFloat(),
            valueText = formatIndianNumber(slice.count.toLong()),
            percentText = "${slice.percent}%",
            color = parseColorSafely(slice.color, statusFallbackColor(slice.name))
        )
    }
    val totalOrders = chartsData?.totalOrdersInMonth ?: 0

    val categoryRows = executiveAnalytics?.revenueStreams.orEmpty().map { stream ->
        CategoryUi(
            label = stream.label,
            valueText = formatRupeeShort(stream.revenue),
            percent = stream.percent.toFloat()
        )
    }

    val orderRows = recentOrders.map { order ->
        OrderRowUi(
            id = order.orderCode,
            customer = order.customer?.name ?: "—",
            owner = shortName(order.salespersonName),
            status = order.status ?: "Pending",
            amount = order.financials?.grandTotal?.toLong() ?: 0L
        )
    }

    // Top performers and follow-ups come from the live opportunities list
    val followUpRows = recentOpportunities.map { opp ->
        val pct = (opp.stage?.percentage ?: 0)
        FollowUpRowUi(
            name = opp.clientName ?: opp.name,
            stage = opp.stage?.name ?: "",
            priority = when {
                pct >= 70 -> "High"
                pct >= 40 -> "Medium"
                else -> "Low"
            }
        )
    }

    // ---------------- LAYOUT ----------------

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Page header
            // Top bar: title on the left, period dropdown on the right
            DashboardTopBar(
                title = "Sales Dashboard",
                selectedValue = selectedPeriod,
                options = periodOptions,
                onSelected = { value ->
                    // A period change reloads the whole dashboard, so every card shows its skeleton
                    statusOnlyReload = false
                    // "Period" is the placeholder item, so it resets to the default period
                    selectedPeriod = if (value == "period") "this_month" else value
                }
            )

//            // Date range selector
//            DateRangeSelector(
//                label = rangeLabel,
//                options = periodOptions,
//                onSelected = { selectedPeriod = it }
//            )

            // KPI grid, two cards per row
            kpiItems.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { item ->
                        KpiCard(item = item, isLoading = globalLoading, modifier = Modifier.weight(1f))
                    }
                }
            }

            RevenueOrdersTrendCard(
                points = trendPoints,
                activeMonthIndex = selectedMonth - 1,
                year = selectedYear,
                isLoading = globalLoading
            )

            PipelineHealthCard(rows = healthRows, isLoading = globalLoading)

            OrderStatusMixCard(
                slices = statusSlices,
                totalOrders = totalOrders,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChanged = {
                    // Only this card should show its skeleton while the new month loads
                    statusOnlyReload = true
                    selectedMonth = it
                },
                onYearChanged = {
                    statusOnlyReload = true
                    selectedYear = it
                },
                isLoading = statusLoading
            )

            RevenueByCategoryCard(rows = categoryRows, isLoading = globalLoading)

            RecentSalesOrdersCard(rows = orderRows, isLoading = globalLoading, onViewAll = onNavigateToOrders)

            TopPerformersCard(
                rows = followUpRows,
                isLoading = globalLoading,
                onViewAll = onNavigateToPipeline,
                onFollowUp = onNavigateToLeads
            )
        }
    }
}

// =============================================================
// KPI CARD
// =============================================================

@Composable
private fun KpiCard(item: KpiUi, isLoading: Boolean, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val trendColor = if (item.trendUp) Emerald else Red

    DashboardCard(modifier = modifier.fillMaxHeight(), contentPadding = 14.dp) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(96.dp).shimmerSkeleton())
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBox(icon = item.icon, bg = item.accent.bg, tint = item.accent.tint)
                Sparkline(points = item.spark, color = item.accent.spark)
            }
            Spacer(Modifier.height(14.dp))
            Text(text = item.title, fontSize = tokens.bodySmall, color = headerGrey, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = item.trend.trimStart('+', '-'),
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = trendColor
                )
                Spacer(Modifier.width(5.dp))
                Text(text = "vs last mo", fontSize = tokens.caption, color = iconMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 64.dp, height = 26.dp)) {
        if (points.size < 2) return@Canvas
        val pad = 2.dp.toPx()
        val innerH = size.height - pad * 2
        val min = points.minOrNull() ?: 0f
        val max = points.maxOrNull() ?: 1f
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val step = size.width / (points.size - 1)
        val offsets = points.mapIndexed { i, v ->
            Offset(i * step, pad + innerH * (1f - (v - min) / range))
        }
        drawPath(smoothPath(offsets), color = color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// =============================================================
// REVENUE AND ORDERS TREND
// =============================================================

@Composable
private fun RevenueOrdersTrendCard(
    points: List<TrendUi>,
    activeMonthIndex: Int,
    year: Int,
    isLoading: Boolean
) {
    val tokens = LocalAppTokens.current

    val maxRevenue = points.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
    val maxOrders = points.maxOfOrNull { it.orders }?.coerceAtLeast(1f) ?: 1f
    val (divisor, unit) = when {
        maxRevenue >= 1e7 -> 1e7 to "Cr"
        maxRevenue >= 1e5 -> 1e5 to "L"
        else -> 1e3 to "k"
    }
    val niceMax = niceCeil(maxRevenue / divisor)
    val axisLabels = (0..4).map { formatTick(niceMax * (4 - it) / 4.0) }

    val revenueNorm = points.map { (it.revenue / divisor / niceMax).toFloat().coerceIn(0f, 1f) }
    val ordersNorm = points.map { (it.orders / maxOrders).coerceIn(0f, 1f) }

    val monthShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val activeName = monthShort.getOrNull(activeMonthIndex)
    val activeIdx = points.indexOfFirst { it.label.startsWith(activeName ?: "#", ignoreCase = true) }
    val yy = (year % 100).toString().padStart(2, '0')

    DashboardCard {
        CardHeader(
            title = "Revenue & Orders Trend",
            trailing = {
                PillDropdown(
                    selectedText = "Monthly",
                    options = listOf("Monthly" to "monthly"),
                    onSelected = {}
                )
            }
        )
        SectionDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = Indigo, label = "Revenue (₹ $unit)")
            LegendDot(color = Emerald, label = "Orders")
        }
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(190.dp).shimmerSkeleton())
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                Column(
                    modifier = Modifier.width(40.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    axisLabels.forEach { Text(text = it, fontSize = tokens.label, color = iconMuted) }
                }
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val w = size.width
                    val h = size.height
                    for (i in 0..4) {
                        val y = h * i / 4f
                        drawLine(grey_border, Offset(0f, y), Offset(w, y), 1.dp.toPx())
                    }
                    val n = points.size
                    if (n == 0) return@Canvas
                    val slot = w / n
                    fun toOffsets(values: List<Float>) = values.mapIndexed { i, v ->
                        Offset(slot * (i + 0.5f), h * (1f - v))
                    }
                    val ordersOffsets = toOffsets(ordersNorm)
                    val revenueOffsets = toOffsets(revenueNorm)
                    if (n > 1) {
                        drawPath(smoothPath(ordersOffsets), Emerald, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(smoothPath(revenueOffsets), Indigo, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    if (activeIdx in 0 until n) {
                        drawCircle(Color.White, 6.dp.toPx(), ordersOffsets[activeIdx])
                        drawCircle(Emerald, 4.dp.toPx(), ordersOffsets[activeIdx])
                        drawCircle(Color.White, 6.dp.toPx(), revenueOffsets[activeIdx])
                        drawCircle(Indigo, 4.dp.toPx(), revenueOffsets[activeIdx])
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(start = 40.dp)) {
                points.forEachIndexed { i, p ->
                    val text = if (i == 0 || i == activeIdx) "${p.label} '$yy" else p.label
                    Text(
                        text = text,
                        fontSize = tokens.label,
                        color = headerGrey,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// =============================================================
// PIPELINE AND TARGET HEALTH
// =============================================================

@Composable
private fun PipelineHealthCard(rows: List<HealthUi>, isLoading: Boolean) {
    val tokens = LocalAppTokens.current
    DashboardCard {
        CardHeader(
            title = "Pipeline & Target Health",
            trailing = { Text(text = "STATUS", fontSize = tokens.label, color = iconMuted, letterSpacing = 0.6.sp) }
        )
        SectionDivider()
        if (isLoading) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(5) { Box(modifier = Modifier.fillMaxWidth().height(34.dp).shimmerSkeleton()) }
            }
        } else if (rows.isEmpty()) {
            EmptyText("No pipeline data available")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    val status = healthStatus(row.percent)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.label,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${row.percent.toInt()}%",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            StatusPill(text = status)
                        }
                        ProgressBar(progress = row.percent / 100f, color = Indigo, height = 8.dp)
                    }
                }
            }
        }
    }
}

// =============================================================
// ORDER STATUS MIX
// =============================================================

@Composable
private fun OrderStatusMixCard(
    slices: List<SliceUi>,
    totalOrders: Int,
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChanged: (Int) -> Unit,
    onYearChanged: (Int) -> Unit,
    isLoading: Boolean
) {
    val tokens = LocalAppTokens.current
    DashboardCard {
        CardHeader(title = "Order Status Mix")
        Spacer(Modifier.height(12.dp))
        // Reusable month and year picker, aligned to the right side of the card
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            MonthYearPicker(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChanged = onMonthChanged,
                onYearChanged = onYearChanged
            )
        }
        SectionDivider()
        if (isLoading) {
            // Ring skeleton with two placeholder bars in the center
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutSkeleton()
            }
            Spacer(Modifier.height(20.dp))
            // Legend placeholders keep the card height stable while loading
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(5) { LegendSkeletonRow() }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(segments = slices.map { DonutSegment(it.amount, it.color) }) {
                    Text(
                        text = formatIndianNumber(totalOrders.toLong()),
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(text = "TOTAL ORDERS", fontSize = tokens.label, color = iconMuted)
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                slices.forEach { slice ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(slice.color))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = slice.label,
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = slice.valueText, fontSize = tokens.bodySmall, color = TextPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = slice.percentText,
                            fontSize = tokens.caption,
                            color = iconMuted,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(52.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    segments: List<DonutSegment>,
    diameter: Dp = 150.dp,
    strokeWidth: Dp = 22.dp,
    center: @Composable ColumnScope.() -> Unit
) {
    val progress = remember(segments) { Animatable(0f) }
    LaunchedEffect(segments) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 850, easing = FastOutSlowInEasing))
    }

    Box(modifier = Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val visible = segments.filter { it.value > 0f }
            val total = visible.sumOf { it.value.toDouble() }.toFloat()

            // Empty state: plain track ring
            if (total <= 0f) {
                drawArc(
                    color = IndigoTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokePx)
                )
                return@Canvas
            }

            // Flat caps with a thin gap between segments
            val gap = if (visible.size > 1) 3f else 0f
            var angle = -90f
            visible.forEach { seg ->
                val full = seg.value / total * 360f
                val sweep = ((full - gap) * progress.value).coerceAtLeast(0f)
                if (sweep > 0f) {
                    drawArc(
                        color = seg.color,
                        startAngle = angle + gap / 2f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokePx, cap = StrokeCap.Butt)
                    )
                }
                angle += full
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = center
        )
    }
}

// =============================================================
// REVENUE BY SALES CATEGORY
// =============================================================

@Composable
private fun RevenueByCategoryCard(rows: List<CategoryUi>, isLoading: Boolean) {
    val tokens = LocalAppTokens.current
    DashboardCard {
        CardHeader(
            title = "Revenue by Sales Category",
            trailing = {
                PillDropdown(
                    selectedText = "By Revenue",
                    options = listOf("By Revenue" to "revenue"),
                    onSelected = {}
                )
            }
        )
        SectionDivider()
        if (isLoading) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(5) { Box(modifier = Modifier.fillMaxWidth().height(28.dp).shimmerSkeleton()) }
            }
        } else if (rows.isEmpty()) {
            EmptyText("No revenue data available")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.label,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = row.valueText, fontSize = tokens.bodySmall, color = TextPrimary)
                            Text(text = "(${formatPercent(row.percent)}%)", fontSize = tokens.caption, color = iconMuted)
                        }
                        ProgressBar(progress = row.percent / 100f, color = Indigo, height = 6.dp)
                    }
                }
            }
        }
    }
}

// =============================================================
// RECENT SALES ORDERS TABLE
// =============================================================

@Composable
private fun RecentSalesOrdersCard(rows: List<OrderRowUi>, isLoading: Boolean, onViewAll: () -> Unit) {
    val tokens = LocalAppTokens.current
    val columns = listOf(
        TableColumn("ORDER ID", 140.dp),
        TableColumn("CUSTOMER", 170.dp),
        TableColumn("OWNER", 100.dp),
        TableColumn("STATUS", 130.dp),
        TableColumn("AMOUNT", 120.dp, alignEnd = true)
    )
    TableCard(
        title = "Recent Sales Orders",
        onViewAll = onViewAll,
        columns = columns,
        isLoading = isLoading,
        rows = rows.map { row ->
            listOf<@Composable () -> Unit>(
                { Text(row.id, fontSize = tokens.bodySmall, color = TextPrimary, maxLines = 1) },
                { Text(row.customer, fontSize = tokens.bodySmall, color = headerGrey, maxLines = 1) },
                { Text(row.owner, fontSize = tokens.bodySmall, color = headerGrey, maxLines = 1) },
                { StatusPill(text = titleCase(row.status)) },
                {
                    Text(
                        text = "₹${formatIndianNumber(row.amount)}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
            )
        }
    )
}

// =============================================================
// TOP PERFORMERS AND FOLLOW-UPS TABLE
// =============================================================

@Composable
private fun TopPerformersCard(
    rows: List<FollowUpRowUi>,
    isLoading: Boolean,
    onViewAll: () -> Unit,
    onFollowUp: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val columns = listOf(
        TableColumn("LEAD / CUSTOMER", 190.dp),
        TableColumn("STAGE", 130.dp),
        TableColumn("PRIORITY", 100.dp),
        TableColumn("ACTION", 130.dp, alignEnd = true)
    )
    TableCard(
        title = "Top Performers & Follow-ups",
        onViewAll = onViewAll,
        columns = columns,
        isLoading = isLoading,
        rows = rows.map { row ->
            listOf<@Composable () -> Unit>(
                { Text(row.name, fontSize = tokens.bodySmall, color = TextPrimary, maxLines = 1) },
                { Text(row.stage, fontSize = tokens.bodySmall, color = headerGrey, maxLines = 1) },
                { StatusPill(text = row.priority) },
                { FollowUpButton(onClick = onFollowUp) }
            )
        }
    )
}

@Composable
private fun FollowUpButton(onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFC7CCF5), RoundedCornerShape(10.dp))
            .background(whiteBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text = "Follow Up", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Indigo, maxLines = 1)
    }
}

// =============================================================
// TABLE PRIMITIVES
// =============================================================

@Composable
private fun TableCard(
    title: String,
    onViewAll: () -> Unit,
    columns: List<TableColumn>,
    rows: List<List<@Composable () -> Unit>>,
    isLoading: Boolean
) {
    val tokens = LocalAppTokens.current
    DashboardCard(contentPadding = 0.dp) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            CardHeader(
                title = title,
                trailing = {
                    Text(
                        text = "View All",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Indigo,
                        modifier = Modifier.clickable { onViewAll() }
                    )
                }
            )
            SectionDivider()
        }
        when {
            isLoading -> Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(4) { Box(modifier = Modifier.fillMaxWidth().height(28.dp).shimmerSkeleton()) }
            }
            rows.isEmpty() -> Box(modifier = Modifier.padding(16.dp)) { EmptyText("No records found") }
            else -> DashboardTable(columns = columns, rows = rows)
        }
    }
}

@Composable
private fun DashboardTable(columns: List<TableColumn>, rows: List<List<@Composable () -> Unit>>) {
    val tokens = LocalAppTokens.current
    val totalWidth = columns.fold(0.dp) { acc, c -> acc + c.width }

    Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.width(totalWidth).background(headerBg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { col ->
                Box(
                    modifier = Modifier.width(col.width).padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = if (col.alignEnd) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = col.header,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = iconMuted,
                        letterSpacing = 0.6.sp,
                        maxLines = 1
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.width(totalWidth), color = grey_border)

        rows.forEachIndexed { index, rowCells ->
            Row(modifier = Modifier.width(totalWidth), verticalAlignment = Alignment.CenterVertically) {
                rowCells.forEachIndexed { i, cell ->
                    Box(
                        modifier = Modifier.width(columns[i].width).padding(horizontal = 14.dp, vertical = 14.dp),
                        contentAlignment = if (columns[i].alignEnd) Alignment.CenterEnd else Alignment.CenterStart
                    ) { cell() }
                }
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(modifier = Modifier.width(totalWidth), color = grey_border)
            }
        }
    }
}

// =============================================================
// SHARED COMPONENTS
// =============================================================

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
private fun CardHeader(title: String, trailing: (@Composable () -> Unit)? = null) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Indigo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.Info, contentDescription = null, tint = iconMuted, modifier = Modifier.size(15.dp))
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = grey_border)
}

@Composable
private fun IconBox(icon: ImageVector, bg: Color, tint: Color, boxSize: Dp = 36.dp) {
    Box(
        modifier = Modifier.size(boxSize).clip(RoundedCornerShape(10.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text = label, fontSize = tokens.caption, color = headerGrey)
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color, height: Dp) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(height).clip(CircleShape),
        color = color,
        trackColor = IndigoTrack,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

@Composable
private fun StatusPill(text: String) {
    val tokens = LocalAppTokens.current
    val style = chipStyleFor(text)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(style.bg)
            .border(1.dp, style.border, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = style.fg, maxLines = 1)
    }
}

@Composable
private fun EmptyText(text: String) {
    val tokens = LocalAppTokens.current
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
        Text(text = text, fontSize = tokens.bodySmall, color = iconMuted)
    }
}

@Composable
private fun DateRangeSelector(
    label: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(whiteBg)
                .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = mutedText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = mutedText, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(whiteBg)) {
            options.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text = text, fontSize = tokens.bodySmall, color = TextPrimary) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> PillDropdown(
    selectedText: String,
    options: List<Pair<String, T>>,
    onSelected: (T) -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(light_grey)
                .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedText, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = headerGrey, maxLines = 1)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = mutedText, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(whiteBg)) {
            options.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text = text, fontSize = tokens.bodySmall, color = TextPrimary) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

//@Composable
//private fun Modifier.shimmerSkeleton(): Modifier {
//    val transition = rememberInfiniteTransition(label = "shimmer")
//    val translate by transition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1000f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 1100, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "shimmer"
//    )
//    val brush = Brush.linearGradient(
//        colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFF1F5F9)),
//        start = Offset(translate - 400f, translate - 400f),
//        end = Offset(translate, translate)
//    )
//    return this.clip(RoundedCornerShape(8.dp)).background(brush)
//}

// =============================================================
// HELPERS
// =============================================================

private fun parseColorSafely(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(hex.toColorInt())
    } catch (_: Exception) {
        fallback
    }
}

// Fallback colors used when the API does not return a color for a status slice
private fun statusFallbackColor(name: String): Color = when (name.trim().lowercase()) {
    "completed", "delivered" -> Emerald
    "in progress", "in_progress", "processing" -> Blue
    "pending" -> Amber
    "cancelled", "canceled" -> Red
    "returned" -> Purple
    else -> Indigo
}

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        val p0 = points[i - 1]
        val p1 = points[i]
        val cx = (p0.x + p1.x) / 2f
        path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }
    return path
}

// Colors for status, priority and health chips
private fun chipStyleFor(raw: String): ChipStyle {
    val green = ChipStyle(Color(0xFFE7F8F0), Color(0xFFBDEBD4), Color(0xFF0E9F6E))
    val blue = ChipStyle(Color(0xFFEAF1FF), Color(0xFFC9DAFB), Color(0xFF2F6FE4))
    val amber = ChipStyle(Color(0xFFFFF5E0), Color(0xFFFBDDA0), Color(0xFFC77700))
    val red = ChipStyle(Color(0xFFFDECEF), Color(0xFFF8C7CF), Color(0xFFD62839))
    val purple = ChipStyle(Color(0xFFF3EAFE), Color(0xFFDDC6FA), Color(0xFF7E22CE))
    val grey = ChipStyle(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFF475569))

    return when (raw.trim().lowercase().replace("_", " ")) {
        "completed", "delivered", "won", "strong", "low", "accepted" -> green
        "in progress", "on track", "processing", "confirmed" -> blue
        "pending", "attention", "medium", "draft" -> amber
        "cancelled", "canceled", "high", "rejected", "lost" -> red
        "returned" -> purple
        else -> grey
    }
}

// Health label thresholds for the Pipeline & Target Health card
private fun healthStatus(percent: Float): String = when {
    percent >= 88f -> "Strong"
    percent >= 80f -> "On Track"
    else -> "Attention"
}

private fun titleCase(raw: String?): String =
    raw.orEmpty().replace("_", " ").trim().split(" ").filter { it.isNotEmpty() }
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }

// "Anita Sharma" becomes "Anita S."
private fun shortName(raw: String?): String {
    val parts = raw.orEmpty().trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "—"
        parts.size == 1 -> parts[0]
        else -> "${parts[0]} ${parts.last().first().uppercase()}."
    }
}

private fun formatRupeeShort(value: Double): String = when {
    value >= 1e7 -> "₹" + String.format(Locale.US, "%.2f", value / 1e7) + " Cr"
    value >= 1e5 -> "₹" + String.format(Locale.US, "%.2f", value / 1e5) + " L"
    else -> "₹" + formatIndianNumber(value.toLong())
}

private fun formatPercent(value: Float): String = String.format(Locale.US, "%.1f", value)

private fun formatTick(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)

// Rounds a value up to a "nice" axis maximum such as 1, 2, 2.5, 5 or 10 times a power of ten
private fun niceCeil(value: Double): Double {
    if (value <= 0.0) return 1.0
    val base = 10.0.pow(floor(log10(value)))
    val f = value / base
    val nice = when {
        f <= 1.0 -> 1.0
        f <= 2.0 -> 2.0
        f <= 2.5 -> 2.5
        f <= 5.0 -> 5.0
        else -> 10.0
    }
    return nice * base
}

// Builds a label such as "May 1 – May 31, 2026" from the selected period
private fun periodRangeLabel(period: String, month: Int, year: Int): String {
    val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun lastDay(m: Int, y: Int): Int =
        Calendar.getInstance().apply { clear(); set(y, m - 1, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

    return when (period) {
        "last_month" -> {
            val m = if (month == 1) 12 else month - 1
            val y = if (month == 1) year - 1 else year
            "${names[m - 1]} 1 – ${names[m - 1]} ${lastDay(m, y)}, $y"
        }
        "this_quarter" -> {
            val startMonth = ((month - 1) / 3) * 3 + 1
            val endMonth = startMonth + 2
            "${names[startMonth - 1]} 1 – ${names[endMonth - 1]} ${lastDay(endMonth, year)}, $year"
        }
        "this_year" -> "Jan 1 – Dec 31, $year"
        else -> "${names[month - 1]} 1 – ${names[month - 1]} ${lastDay(month, year)}, $year"
    }
}

// Top bar: bold title on the left, period dropdown on the right
@Composable
private fun DashboardTopBar(
    title: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val selectedText = options.firstOrNull { it.second == selectedValue }?.first
        ?: options.first().first

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))

        Box {
            // Trigger: gray pill, gets a light blue border while the menu is open
            Row(
                modifier = Modifier
                    .width(140.dp)
                    .clip(shape)
                    .background(light_grey)
                    .border(1.dp, if (expanded) Color(0xFFBFD6F6) else Color.Transparent, shape)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Menu: the selected item is highlighted with a light blue background
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // Limit the menu to 200dp so the remaining months are reached by scrolling
                modifier = Modifier
                    .background(whiteBg)
                    .heightIn(max = 200.dp)
            ) {
                options.forEach { (label, value) ->
                    val isSelected = value == selectedValue
                    DropdownMenuItem(
                        text = { Text(text = label, fontSize = tokens.bodyMedium, color = TextPrimary) },
                        modifier = Modifier.background(if (isSelected) Color(0xFFEAF2FF) else Color.Transparent),
                        onClick = {
                            onSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Reusable month and year picker. Use it in any card that needs a month and year filter.
@Composable
private fun MonthYearPicker(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChanged: (Int) -> Unit,
    onYearChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    years: List<Int> = defaultYearOptions()
) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    // Make sure the currently selected year is always part of the list
    val yearList = (years + selectedYear).distinct().sorted()

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PickerPill(
            selectedText = monthNames.getOrElse(selectedMonth - 1) { "Jan" },
            selectedValue = selectedMonth,
            options = monthNames.mapIndexed { index, name -> name to (index + 1) },
            onSelected = onMonthChanged,
            modifier = Modifier.width(92.dp)
        )
        PickerPill(
            selectedText = selectedYear.toString(),
            selectedValue = selectedYear,
            options = yearList.map { it.toString() to it },
            onSelected = onYearChanged,
            leadingIcon = Icons.Outlined.CalendarToday,
            modifier = Modifier.width(112.dp)
        )
    }
}

// Generic gray pill with a dropdown menu, used by MonthYearPicker
@Composable
private fun <T> PickerPill(
    selectedText: String,
    selectedValue: T,
    options: List<Pair<String, T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(light_grey)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = mutedText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = selectedText,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(whiteBg)
        ) {
            options.forEach { (label, value) ->
                val isSelected = value == selectedValue
                DropdownMenuItem(
                    text = { Text(text = label, fontSize = tokens.bodySmall, color = TextPrimary) },
                    modifier = Modifier.background(if (isSelected) Color(0xFFEAF2FF) else Color.Transparent),
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Year list for the picker: three years back up to next year
private fun defaultYearOptions(): List<Int> {
    val current = Calendar.getInstance().get(Calendar.YEAR)
    return (current - 3..current + 1).toList()
}

// Shared animated shimmer brush used by every skeleton
@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFF1F5F9)),
        start = Offset(translate - 400f, translate - 400f),
        end = Offset(translate, translate)
    )
}

@Composable
private fun Modifier.shimmerSkeleton(): Modifier =
    this.clip(RoundedCornerShape(8.dp)).background(rememberShimmerBrush())

// Donut skeleton: pale ring with two stacked placeholder bars in the center
@Composable
private fun DonutSkeleton(
    diameter: Dp = 150.dp,
    strokeWidth: Dp = 22.dp
) {
    val brush = rememberShimmerBrush()

    Box(modifier = Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(size.width - strokePx, size.height - strokePx),
                style = Stroke(strokePx)
            )
        }
        // Placeholders for the total value and its label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.width(56.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.width(44.dp).height(10.dp).background(brush, RoundedCornerShape(4.dp)))
        }
    }
}

// One legend row placeholder: dot, label bar, value bar and percent bar
@Composable
private fun LegendSkeletonRow() {
    val brush = rememberShimmerBrush()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(brush, CircleShape))
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.width(90.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.width(30.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.width(40.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
    }
}