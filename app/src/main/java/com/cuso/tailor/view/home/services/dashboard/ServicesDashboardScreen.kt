package com.cuso.tailor.view.home.services.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun ServicesDashboardScreen(
    onNavigateToRequests: () -> Unit = {},
    onNavigateToJobCards: () -> Unit = {},
    onNavigateToComplaints: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val charts by viewModel.charts.collectAsStateWithLifecycle()
    val recentRequests by viewModel.recentRequests.collectAsStateWithLifecycle()
    val recentJobCards by viewModel.recentJobCards.collectAsStateWithLifecycle()
    val recentComplaints by viewModel.recentComplaints.collectAsStateWithLifecycle()
    val recentFeedbacks by viewModel.recentFeedbacks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isDashboardLoading.collectAsStateWithLifecycle()

    if (isLoading && analytics == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Primary_background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BluePrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Dynamic 6 KPI Cards from API (2 Cards Per Row Grid)
        item {
            val kpis = analytics?.kpi
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(tokens.screenPadding)
                ) {
                    Text(
                        "Service Dashboard", fontWeight = FontWeight.Medium, fontSize = tokens.bodyMedium, color = title_color
                    )
                }
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Service Orders",
                        item = kpis?.totalOrders,
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        iconBg = cyanBg,
                        lineColor = BluePrimary
                    )
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "In Workshop",
                        item = kpis?.inWorkshop,
                        icon = Icons.Outlined.Sync,
                        iconBg = primary_light,
                        lineColor = chartBlue
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Ready for Delivery",
                        item = kpis?.readyDelivery,
                        icon = Icons.Outlined.CheckCircleOutline,
                        iconBg = yellowBg,
                        lineColor = chartAmber
                    )
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Urgent Priority",
                        item = kpis?.urgentJobs,
                        icon = Icons.AutoMirrored.Outlined.KeyboardReturn,
                        iconBg = redBg,
                        lineColor = redText
                    )
                }

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Open Complaints",
                        item = kpis?.openComplaints,
                        icon = Icons.Outlined.ReportProblem,
                        iconBg = redBg,
                        lineColor = redText
                    )
                    DynamicKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Customer Rating",
                        item = kpis?.customerSatisfaction,
                        icon = Icons.Outlined.StarOutline,
                        iconBg = greenBg,
                        lineColor = greentext
                    )
                }
            }
        }

        // 2. Dynamic Service Requests & Completions Graph (Charts Endpoint)
        item {
            DynamicRequestsCompletionsGraphCard(lineData = charts?.lineChartData.orEmpty())
        }

        // 3. Dynamic Request Distribution Donut Chart & Legend (Charts Endpoint)
        item {
            DynamicRequestDistributionCard(
                pieData = charts?.statusPieChart.orEmpty(),
                totalCount = charts?.totalJobsCount ?: 0
            )
        }

        // 4. Dynamic Recent Service Requests Table (Recent Requests Endpoint)
        item {
            DynamicRecentRequestsTableCard(
                items = recentRequests,
                onViewAll = onNavigateToRequests
            )
        }

        // 5. Dynamic Active Workshop Job Cards Table (Recent Job Cards Endpoint)
        item {
            DynamicJobCardsTableCard(
                items = recentJobCards,
                onViewAll = onNavigateToJobCards
            )
        }

        // 6. Dynamic Complaints & Support Queue Table (Recent Complaints Endpoint)
        item {
            DynamicComplaintsTableCard(
                items = recentComplaints,
                onViewAll = onNavigateToComplaints
            )
        }

        // 7. Dynamic Recent Customer Reviews Table (Recent Feedbacks Endpoint)
        item {
            DynamicCustomerReviewsTableCard(
                items = recentFeedbacks,
                onViewAll = onNavigateToReviews
            )
        }
    }
}

// =============================================================================
// 1. Dynamic Metric Card (Uses real sparkline & trend values from API)
// =============================================================================
@Composable
private fun DynamicKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    item: ServiceKpiItem?,
    icon: ImageVector,
    iconBg: Color,
    lineColor: Color
) {
    val tokens = LocalAppTokens.current
    val valueText = item?.value?.toString() ?: "0"
    val trendText = item?.trend ?: "+ 0%"
    val trendUp = item?.trendUp ?: true
    val sparkline = item?.sparkline.orEmpty()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(30.dp).background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = lineColor, modifier = Modifier.size(16.dp))
                }

                // Dynamic Canvas Sparkline sized proportionally for 2-column layout
                Box(
                    modifier = Modifier.width(48.dp).height(14.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val numericValues = sparkline.mapNotNull {
                            when (it) {
                                is Number -> it.toFloat()
                                is String -> it.toFloatOrNull()
                                else -> null
                            }
                        }

                        if (numericValues.size >= 2) {
                            val minVal = numericValues.minOrNull() ?: 0f
                            val maxVal = numericValues.maxOrNull() ?: 1f
                            val range = (maxVal - minVal).coerceAtLeast(1f)
                            val stepX = w / (numericValues.size - 1)

                            val path = Path()
                            numericValues.forEachIndexed { i, v ->
                                val x = i * stepX
                                val y = if (maxVal == minVal) h / 2f else h - ((v - minVal) / range * h)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        } else {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, h / 2f),
                                end = Offset(w, h / 2f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = valueText,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (trendUp) greentext else redText,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = trendText,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = if (trendUp) greentext else redText
                )
                Text(
                    text = "vs Last Month",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =============================================================================
// 2. Dynamic Service Requests & Completions Graph
// =============================================================================
@Composable
private fun DynamicRequestsCompletionsGraphCard(lineData: List<ServiceLineChartItem>) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Service Requests & Completions",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
            }
            Text(
                text = "Requests received vs successfully completed",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )

            // Dynamic Chart Canvas
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    if (lineData.isNotEmpty()) {
                        val maxRequests = lineData.maxOfOrNull { it.requests ?: 0 } ?: 1
                        val maxCompletions = lineData.maxOfOrNull { it.completions ?: 0 } ?: 1
                        val peak = maxOf(maxRequests, maxCompletions).coerceAtLeast(1).toFloat()
                        val stepX = if (lineData.size > 1) w / (lineData.size - 1) else w

                        // Requests curve (BluePrimary)
                        val reqPath = Path()
                        lineData.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val count = (pt.requests ?: 0).toFloat()
                            val y = h - (count / peak * (h - 24f)) - 12f
                            if (i == 0) reqPath.moveTo(x, y) else reqPath.lineTo(x, y)
                        }
                        drawPath(reqPath, color = BluePrimary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                        // Completions curve (chartBlue)
                        val compPath = Path()
                        lineData.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val count = (pt.completions ?: 0).toFloat()
                            val y = h - (count / peak * (h - 24f)) - 12f
                            if (i == 0) compPath.moveTo(x, y) else compPath.lineTo(x, y)
                        }
                        drawPath(compPath, color = chartBlue, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    } else {
                        // Flat line if API returns empty list
                        drawLine(chartBlue, Offset(0f, h - 10f), Offset(w, h - 10f), strokeWidth = 2.dp.toPx())
                    }
                }
            }

            // Month Labels from lineData
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                lineData.forEach {
                    Text(it.name.orEmpty(), fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                }
            }
        }
    }
}

// =============================================================================
// 3. Dynamic Request Distribution Donut Chart & Legend
// =============================================================================
@Composable
private fun DynamicRequestDistributionCard(
    pieData: List<ServicePieChartItem>,
    totalCount: Int
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Request Distribution",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
            }
            Text(
                text = "Current service request status breakdown",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )

            // Centered Dynamic Donut Chart calculated from statusPieChart values
            Box(
                modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 18.dp.toPx()
                    val total = totalCount.takeIf { it > 0 } ?: pieData.sumOf { it.value ?: 0 }.coerceAtLeast(1)
                    var startAngle = -90f

                    val activeSlices = pieData.filter { (it.value ?: 0) > 0 }
                    if (activeSlices.isNotEmpty()) {
                        activeSlices.forEach { slice ->
                            val sliceValue = slice.value ?: 0
                            val sweepAngle = (sliceValue.toFloat() / total.toFloat()) * 360f
                            val sliceColor = mapPieSliceColor(slice.name.orEmpty())

                            drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    } else {
                        // Empty outline if all values are 0
                        drawArc(
                            color = dividerColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Butt)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalCount", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("Total Requests", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                }
            }

            // Legend Rows directly from statusPieChart
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pieData.forEach { item ->
                    val dotColor = mapPieSliceColor(item.name.orEmpty())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                            Text(item.name.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Text("${item.value ?: 0} (${item.percent.orEmpty()})", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. Dynamic Recent Service Requests Table
// =============================================================================
@Composable
private fun DynamicRecentRequestsTableCard(
    items: List<RecentServiceRequestItem>,
    onViewAll: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recent Service Requests", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                }
                Text("View All ↗", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.clickable { onViewAll() })
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(620.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(cardBgLight).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("REQUEST ID", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("CUSTOMER", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(140.dp))
                        Text("CATEGORY", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(120.dp))
                        Text("PRIORITY", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(90.dp))
                        Text("STATUS", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                    }

                    HorizontalDivider(color = dividerColor)

                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.serviceRequestCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.customerSnapshot?.name ?: "—", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(140.dp))
                            Text(item.primaryCategory.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(120.dp))
                            // In DynamicRecentRequestsTableCard
                            Box(modifier = Modifier.width(90.dp)) {
                                AppPriorityBadge(priority = item.priority ?: "")
                            }
                            Box(modifier = Modifier.width(130.dp)) {
                                AppServiceStatusBadge(status = item.status ?: "")
                            }
                        }
                        HorizontalDivider(color = cardBgLight)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. Dynamic Active Workshop Job Cards Table
// =============================================================================
@Composable
private fun DynamicJobCardsTableCard(
    items: List<RecentJobCardItem>,
    onViewAll: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Active Workshop Job Cards", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                }
                Text("View All ↗", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.clickable { onViewAll() })
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(620.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(cardBgLight).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("JOB CARD", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("ORDER", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(140.dp))
                        Text("STAGE", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(110.dp))
                        Text("STATUS", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("PROGRESS", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(90.dp))
                    }

                    HorizontalDivider(color = dividerColor)

                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.jobCardCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.salesOrderId?.orderCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(140.dp))
                            Text(item.currentStageName.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(110.dp))
                            // In DynamicJobCardsTableCard
                            Box(modifier = Modifier.width(130.dp)) {
                                AppServiceStatusBadge(status = item.status ?: "Completed")
                            }
                            Text("${item.completedQuantity ?: 0}/${item.totalQuantity ?: 1} pcs", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(90.dp))
                        }
                        HorizontalDivider(color = cardBgLight)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 6. Dynamic Complaints & Support Queue Table
// =============================================================================
@Composable
private fun DynamicComplaintsTableCard(
    items: List<RecentComplaintItem>,
    onViewAll: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Complaints & Support Queue", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                }
                Text("View All ↗", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.clickable { onViewAll() })
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(660.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(cardBgLight).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TICKET", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("CUSTOMER", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("COMPLAINT ISSUE", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(190.dp))
                        Text("SEVERITY", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(90.dp))
                        Text("ACTION", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(80.dp))
                    }

                    HorizontalDivider(color = dividerColor)

                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.complaintCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.customerSnapshot?.name ?: "—", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.complaintTitle.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(190.dp))
                            // In DynamicComplaintsTableCard
                            Box(modifier = Modifier.width(90.dp)) {
                                AppPriorityBadge(priority = item.severity ?: "High")
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = cardBgLight,
                                modifier = Modifier.clickable { /* Handle view action */ }
                            ) {
                                Text("View", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }
                        }
                        HorizontalDivider(color = cardBgLight)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 7. Dynamic Recent Customer Reviews Table
// =============================================================================
@Composable
private fun DynamicCustomerReviewsTableCard(
    items: List<RecentFeedbackItem>,
    onViewAll: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recent Customer Reviews", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                }
                Text("View All ↗", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.clickable { onViewAll() })
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(660.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(cardBgLight).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CODE", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("CLIENT", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("ORDER", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("RATING", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(100.dp))
                        Text("FEEDBACK REVIEW", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(170.dp))
                    }

                    HorizontalDivider(color = dividerColor)

                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.feedbackCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.customerSnapshot?.name ?: "—", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(item.salesOrderId?.orderCode.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                            Text(
                                text = "★".repeat(item.overallRating ?: 5),
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = chartAmber,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(item.reviewTitle.orEmpty(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(170.dp))
                        }
                        HorizontalDivider(color = cardBgLight)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Helper Color Mappers (Theme Palette Only)
// =============================================================================
private fun mapPieSliceColor(name: String): Color = when (name.lowercase()) {
    "completed", "delivered" -> greentext
    "in progress" -> chartBlue
    "in workshop", "received in workshop" -> chartAmber
    "pending approval" -> redText
    "cancelled" -> purpleText
    "ready for delivery" -> chartBlue
    else -> mutedText // Draft or unspecified
}

// =============================================================================
// Reusable Status & Priority Badges (Delegating to shared StatusBadge)
// =============================================================================
@Composable
private fun AppPriorityBadge(priority: String) {
    val (bg, fg) = when (priority.lowercase()) {
        "high", "urgent" -> Pair(redBg, redText)
        "low"            -> Pair(greenBg, greentext)
        else             -> Pair(yellowBg, orangeText)
    }

    StatusBadge(
        text = priority,
        bgColor = bg,
        textColor = fg,
        cornerRadius = 12.dp,
        showDot = false
    )
}

@Composable
private fun AppServiceStatusBadge(status: String) {
    val clean = status.replace("_", " ")

    val (bg, fg) = when (clean.lowercase()) {
        "completed", "delivered"              -> Pair(greenBg, greentext)
        "received in workshop", "in workshop" -> Pair(cyanBg, chartBlue)
        "pending allocation", "pending"       -> Pair(yellowBg, orangeText)
        else                                  -> Pair(primary_light, BluePrimary)
    }

    StatusBadge(
        text = clean,
        bgColor = bg,
        textColor = fg,
        cornerRadius = 12.dp,
        showDot = false
    )
}