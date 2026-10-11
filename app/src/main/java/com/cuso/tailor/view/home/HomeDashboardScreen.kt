package com.cuso.tailor.view.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.ui.theme.*
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeDashboardScreen(
    widthSizeClass: WindowWidthSizeClass,
    adminName: String = "Admin",
    onNavigate: (String) -> Unit = {}
) {
    val designTokens = getAdaptiveTokens(widthSizeClass)
    val baseDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(density = baseDensity.density, fontScale = 1f),
        LocalAppTokens provides designTokens
    ) {
        val tokens = LocalAppTokens.current

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
                    newLeadsCount = 12,
                    onNavigate = onNavigate
                )
            }

            // 2. 6 Metrics Grid
            item {
                KeyMetricsSection()
            }

            // 3. Sales Targets Progress Bar (Animated)
            item {
                SalesTargetsSection()
            }

            // 4. Order Status Donut Chart (Animated)
            item {
                OrderStatusSection()
            }

            // 5. Stock Status Donut Chart (Animated)
            item {
                StockStatusSection()
            }

            // 6. Today's Attendance Donut Chart (Animated)
            item {
                TodayAttendanceSection()
            }

            // 7. Marketing Performance Bars (Animated)
            item {
                MarketingPerformanceSection()
            }

            // 8. Service Status Donut Chart (Animated)
            item {
                ServiceStatusSection()
            }

            // 9. Top Products Ranking List
            item {
                TopProductsSection(onViewDetails = { onNavigate("inventory_items") })
            }

            // 10. Action Required Task Cards
            item {
                ActionRequiredSection(onResolveClick = { route -> onNavigate(route) })
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
                text = "You have $newLeadsCount new leads to review today.",
                color = whiteBg.copy(alpha = 0.85f),
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. KEY METRICS (6 Grid Cards)
// ─────────────────────────────────────────────────────────────
private data class MetricItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val isPositive: Boolean? = null
)

@Composable
private fun KeyMetricsSection() {
    val tokens = LocalAppTokens.current
    val metrics = listOf(
        MetricItem("Sales Revenue", "₹42.8L", "12.4% Today", true),
        MetricItem("Total Orders", "1,284", "8.2% Growth", true),
        MetricItem("Net Profit", "₹8.6L", "6.8% Up", true),
        MetricItem("Receivables", "₹11.4L", "Outstanding claims", null),
        MetricItem("Inventory Value", "₹32.6L", "Total stock assets", null),
        MetricItem("Delivery Success", "96.8%", "1.8% Increase", true)
    )

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
                                if (item.isPositive == true) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropUp,
                                        contentDescription = null,
                                        tint = darkGreenBg,
                                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                                    )
                                }
                                Text(
                                    text = item.subtitle,
                                    fontSize = tokens.caption,
                                    color = if (item.isPositive == true) darkGreenBg else iconMuted,
                                    fontWeight = if (item.isPositive == true) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 3. SALES TARGETS (Animated Progress)
// ─────────────────────────────────────────────────────────────
@Composable
private fun SalesTargetsSection() {
    val tokens = LocalAppTokens.current
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isStarted = true
    }

    val targetProgress by animateFloatAsState(
        targetValue = if (isStarted) 1.0f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "TargetProgressAnim"
    )

    val achievedProgress by animateFloatAsState(
        targetValue = if (isStarted) 0.856f else 0f,
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
                text = "Monthly progress vs goal set",
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Monthly Target", fontSize = tokens.bodySmall, color = headerGrey)
                Text(
                    text = "₹50.0L",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
            LinearProgressIndicator(
                progress = { targetProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.extraPadding * 0.6f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f)),
                color = mutedText.copy(alpha = 0.5f),
                trackColor = sectionBorder
            )

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Achieved (85.6%)", fontSize = tokens.bodySmall, color = headerGrey)
                Text(
                    text = "₹42.8L",
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

    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
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
// 4. ORDER STATUS
// ─────────────────────────────────────────────────────────────
@Composable
private fun OrderStatusSection() {
    DonutChartCard(
        title = "Order Status",
        subtitle = "Fulfillment lifecycle of 1,284 orders",
        centerValue = "1,284",
        centerLabel = "ORDERS",
        segments = listOf(
            DonutSegment(246f, darkPurple),
            DonutSegment(318f, Primary),
            DonutSegment(168f, orangeText),
            DonutSegment(212f, greentext),
            DonutSegment(344f, BluePrimary)
        ),
        legends = listOf(
            LegendItemData("Conf", "246", darkPurple),
            LegendItemData("Proc", "318", Primary),
            LegendItemData("Ready", "168", orangeText),
            LegendItemData("Transit", "212", greentext),
            LegendItemData("Delivered", "344", BluePrimary)
        )
    )
}

// ─────────────────────────────────────────────────────────────
// 5. STOCK STATUS
// ─────────────────────────────────────────────────────────────
@Composable
private fun StockStatusSection() {
    DonutChartCard(
        title = "Stock Status",
        subtitle = "10 Reorders Required",
        centerValue = "8,420",
        centerLabel = "SKUS",
        segments = listOf(
            DonutSegment(6120f, darkGreenBg),
            DonutSegment(1450f, BluePrimary),
            DonutSegment(620f, yellowText),
            DonutSegment(230f, redText)
        ),
        legends = listOf(
            LegendItemData("Available", "6,120", darkGreenBg),
            LegendItemData("Reserved", "1,450", BluePrimary),
            LegendItemData("Low Stock", "620", yellowText),
            LegendItemData("Out of Stock", "230", redText)
        )
    )
}

// ─────────────────────────────────────────────────────────────
// 6. TODAY'S ATTENDANCE
// ─────────────────────────────────────────────────────────────
@Composable
private fun TodayAttendanceSection() {
    DonutChartCard(
        title = "Today's Attendance",
        subtitle = "12 Leave Requests Pending",
        centerValue = "248",
        centerLabel = "EMPLOYEES",
        segments = listOf(
            DonutSegment(214f, darkGreenBg),
            DonutSegment(8f, redText),
            DonutSegment(12f, yellowText),
            DonutSegment(14f, darkPurple)
        ),
        legends = listOf(
            LegendItemData("Present", "214", darkGreenBg),
            LegendItemData("Absent", "8", redText),
            LegendItemData("Late", "12", yellowText),
            LegendItemData("On Leave", "14", darkPurple)
        )
    )
}

// ─────────────────────────────────────────────────────────────
// 7. MARKETING PERFORMANCE (Animated Linear Bars)
// ─────────────────────────────────────────────────────────────
private data class ChannelItem(val name: String, val amount: String, val targetProgress: Float, val color: Color)

@Composable
private fun MarketingPerformanceSection() {
    val tokens = LocalAppTokens.current
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isStarted = true
    }

    val channels = listOf(
        ChannelItem("Paid Ads", "₹18.5L", 0.9f, Primary),
        ChannelItem("Social", "₹12.2L", 0.65f, darkGreenBg),
        ChannelItem("Email", "₹6.4L", 0.35f, yellowText),
        ChannelItem("WhatsApp", "₹4.1L", 0.22f, BluePrimary),
        ChannelItem("Referral", "₹1.6L", 0.1f, iconMuted)
    )

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
                text = "ROI 4.2x | 1,842 Leads Generated",
                fontSize = tokens.caption,
                color = iconMuted
            )
            Spacer(Modifier.height(tokens.extraPadding))

            channels.forEach { item ->
                val progress by animateFloatAsState(
                    targetValue = if (isStarted) item.targetProgress else 0f,
                    animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                    label = "ChannelProgress"
                )

                Column(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name, fontSize = tokens.caption, color = headerGrey)
                        Text(
                            text = item.amount,
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
                        color = item.color,
                        trackColor = grey_border
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 8. SERVICE STATUS
// ─────────────────────────────────────────────────────────────
@Composable
private fun ServiceStatusSection() {
    DonutChartCard(
        title = "Service Status",
        subtitle = "Rating 4.6/5 | 12 SLA Breached",
        centerValue = "186",
        centerLabel = "REQUESTS",
        segments = listOf(
            DonutSegment(24f, redText),
            DonutSegment(48f, yellowText),
            DonutSegment(18f, BluePrimary),
            DonutSegment(12f, darkPurple),
            DonutSegment(84f, darkGreenBg)
        ),
        legends = listOf(
            LegendItemData("Open", "24", redText),
            LegendItemData("In Progress", "48", yellowText),
            LegendItemData("Awaiting", "18", BluePrimary),
            LegendItemData("Refund", "12", darkPurple),
            LegendItemData("Resolved", "84", darkGreenBg)
        )
    )
}

// ─────────────────────────────────────────────────────────────
// 9. TOP PRODUCTS
// ─────────────────────────────────────────────────────────────
private data class ProductRankingItem(
    val title: String,
    val orders: String,
    val revenue: String,
    val change: String
)

@Composable
private fun TopProductsSection(onViewDetails: () -> Unit) {
    val tokens = LocalAppTokens.current
    val products = listOf(
        ProductRankingItem("Wireless Headphones", "Orders: 186", "Revenue: ₹4.8L", "+18.2%"),
        ProductRankingItem("Classic Cotton Shirt", "Orders: 164", "Revenue: ₹3.9L", "+12.4%"),
        ProductRankingItem("Running Shoes", "Orders: 142", "Revenue: ₹3.6L", "+9.8%"),
        ProductRankingItem("Smart Watch", "Orders: 118", "Revenue: ₹3.2L", "+15.6%"),
        ProductRankingItem("Travel Backpack", "Orders: 106", "Revenue: ₹2.7L", "+7.4%")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Top Products",
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
                            text = item.title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                        Text(
                            text = "${item.orders}   ${item.revenue}",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                    Text(
                        text = item.change,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = darkGreenBg
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
// 10. ACTION REQUIRED
// ─────────────────────────────────────────────────────────────
private data class ActionItem(
    val module: String,
    val priority: String,
    val priorityColor: Color,
    val description: String,
    val time: String,
    val route: String
)

@Composable
private fun ActionRequiredSection(onResolveClick: (String) -> Unit) {
    val tokens = LocalAppTokens.current
    val actions = listOf(
        ActionItem("Sales", "High", redText, "14 overdue follow-ups", "Today", "sales_lead"),
        ActionItem("Finance", "High", redText, "₹3.2L overdue receivables", "Today", "finance_sales_invoices"),
        ActionItem("Inventory", "High", redText, "18 SKUs require reorder", "Today", "inventory_low_stock_alerts"),
        ActionItem("Logistics", "Medium", yellowText, "7 delayed deliveries", "Today", "logistics_delivery"),
        ActionItem("Services", "High", redText, "4 SLA-breached complaints", "Today", "services_service_status"),
        ActionItem("HR", "High", redText, "3 payroll approvals pending", "Today", "hr_leave_approval"),
        ActionItem("IT", "Critical", redText, "2 integration failures", "Now", "module_settings"),
        ActionItem("Legal", "Medium", yellowText, "3 contracts expiring", "7 Days", "module_settings"),
        ActionItem("Security", "Critical", redText, "2 critical security alerts", "Now", "module_settings")
    )

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Text(
            text = "Action Required",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
        Text(
            text = "Important items requiring business-wide attention",
            fontSize = tokens.caption,
            color = iconMuted
        )

        actions.forEach { item ->
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
                                color = item.priorityColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = item.priority,
                                    fontSize = tokens.label,
                                    color = item.priorityColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.5f, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.description,
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = item.time, fontSize = tokens.label, color = iconMuted)
                        Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                        Text(
                            text = "Resolve",
                            color = Primary,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onResolveClick(item.route) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 11. BUSINESS HEALTH
// ─────────────────────────────────────────────────────────────
private data class HealthItem(
    val title: String,
    val subtitle: String,
    val isHealthy: Boolean
)

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