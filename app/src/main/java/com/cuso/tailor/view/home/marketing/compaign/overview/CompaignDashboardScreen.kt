@file:Suppress("unused")
package com.cuso.tailor.view.home.marketing.compaign.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.marketing.CampaignDailyTrendDto
import com.cuso.tailor.model.marketing.CampaignItemDto
import com.cuso.tailor.model.marketing.CampaignStatusBreakdown
import com.cuso.tailor.model.marketing.PlatformPerformanceSummary
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.chartAmber
import com.cuso.tailor.ui.theme.chartEmerald
import com.cuso.tailor.ui.theme.chartIndigo
import com.cuso.tailor.ui.theme.darkPurple
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.MarketingViewModel
import androidx.compose.ui.platform.LocalLocale

@Composable
fun CampaignsOverviewScreen(
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit,
    onViewAllCampaigns: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isLoading by viewModel.isLoading.collectAsState()
    val campaigns by viewModel.campaignsList.collectAsState()
    val dailyTrend by viewModel.dailyTrend.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val activeTrendMetric by viewModel.activeTrendMetric.collectAsState()
    val statusBreakdown by viewModel.statusBreakdown.collectAsState()
    val platformSummaries by viewModel.platformSummaries.collectAsState()

    var rangeExpanded by remember { mutableStateOf(false) }
    val rangeLabels = listOf("Last 30 Days", "Last 7 Days", "Last 90 Days")
    val rangeKeys = listOf("30D", "7D", "90D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = "Campaigns Overview",
            subtitle = "MARKETING PERFORMANCE",
            onClose = onNavigateBack
        )

        if (isLoading && campaigns.isEmpty()) {
            ListSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Time Range Dropdown selector
                item {
                    Spacer(Modifier.height(tokens.extraPadding / 4))
                    Box {
                        Row(
                            modifier = Modifier
                                .height(tokens.fieldHeight)
                                .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                                .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                                .clickable { rangeExpanded = !rangeExpanded }
                                .padding(horizontal = tokens.screenPadding / 2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Range",
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(modifier = Modifier.width(tokens.extraPadding / 2))
                            val currentIdx = rangeKeys.indexOf(selectedRange)
                            Text(
                                text = if (currentIdx >= 0) rangeLabels[currentIdx] else "Last 30 Days",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(tokens.extraPadding / 4))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }

                        FormDropdown(
                            expanded = rangeExpanded,
                            onExpandChange = { rangeExpanded = it },
                            options = rangeLabels,
                            onOptionSelected = { label ->
                                val index = rangeLabels.indexOf(label)
                                if (index >= 0) {
                                    viewModel.onDateRangeSelected(rangeKeys[index])
                                }
                                rangeExpanded = false
                            }
                        )
                    }
                }

                // 1. PERFORMANCE SUMMARY KPI CARDS (2-Column Grid)
                item {
                    PerformanceSummaryKpiSection(campaigns = campaigns)
                }

                // 2. CAMPAIGN PERFORMANCE TREND CHART
                item {
                    CampaignPerformanceTrendSection(
                        activeMetric = activeTrendMetric,
                        onMetricSelected = { viewModel.onTrendMetricSelected(it) },
                        dailyTrend = dailyTrend
                    )
                }

                // 3. STATUS DISTRIBUTION DONUT CARD
                item {
                    StatusDistributionSection(
                        totalCampaigns = campaigns.size,
                        breakdown = statusBreakdown
                    )
                }

                // 4. BY PLATFORM PROGRESS BARS
                item {
                    ByPlatformPerformanceSection(platforms = platformSummaries)
                }

                // 5. TOP PERFORMING CAMPAIGNS LIST
                item {
                    TopPerformingCampaignsSection(
                        campaigns = campaigns,
                        onViewAllClick = onViewAllCampaigns
                    )
                }

                item { Spacer(modifier = Modifier.height(tokens.screenPadding)) }
            }
        }
    }
}

// =========================================================================
// SECTION 1: PERFORMANCE SUMMARY (2-COLUMN CARDS)
// =========================================================================

@Composable
private fun PerformanceSummaryKpiSection(
    campaigns: List<CampaignItemDto>
) {
    val tokens = LocalAppTokens.current

    val totalCampaigns = campaigns.size
    val active = campaigns.count { it.status.equals("Active", ignoreCase = true) }
    val scheduled = campaigns.count { it.status.equals("Scheduled", ignoreCase = true) }
    val completed = campaigns.count { it.status.equals("Completed", ignoreCase = true) }
    val pausedOrStopped = campaigns.count {
        it.status.equals("Paused", ignoreCase = true) || it.status.equals("Stopped", ignoreCase = true)
    }
    val totalSpend = campaigns.sumOf { it.spendValue }
    val totalRevenue = campaigns.sumOf { it.revenueValue }
    val averageRoas = if (totalSpend > 0) totalRevenue / totalSpend else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Text(
            text = "PERFORMANCE SUMMARY",
            fontSize = tokens.bodySmall,
            color = Primary,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Total Campaigns", value = totalCampaigns.toString(), caption = "All registered")
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Active", value = active.toString(), caption = "Running now")
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Scheduled", value = scheduled.toString(), caption = "Upcoming")
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Completed", value = completed.toString(), caption = "Finished")
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Paused / Stopped", value = pausedOrStopped.toString(), caption = "Inactive")
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Total Spend", value = "₹${String.format(LocalLocale.current.platformLocale, "%,.2f", totalSpend)}", caption = "Ad investment")
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Marketing Revenue", value = "₹${String.format(LocalLocale.current.platformLocale, "%,.2f", totalRevenue)}", caption = "Attributed")
            KpiMetricBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Average ROAS", value = if (totalSpend > 0) "${String.format(LocalLocale.current.platformLocale, "%.2f", averageRoas)}x" else "—", caption = "Blended return")
        }
    }
}

@Composable
private fun KpiMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    caption: String
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        Text(
            text = value,
            fontSize = tokens.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        Text(
            text = caption,
            fontSize = tokens.label,
            color = mutedText,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =========================================================================
// SECTION 2: CAMPAIGN PERFORMANCE TREND CHART
// =========================================================================

@Composable
private fun CampaignPerformanceTrendSection(
    activeMetric: String,
    onMetricSelected: (String) -> Unit,
    dailyTrend: List<CampaignDailyTrendDto>
) {
    val tokens = LocalAppTokens.current
    val metricTabs = listOf("Spend", "Impressions", "Clicks", "Promotions", "Conversions", "Orders")

    val trendValues = dailyTrend.map { point ->
        when (activeMetric) {
            "Spend" -> point.spend
            "Impressions" -> point.impressions.toDouble()
            "Clicks" -> point.clicks.toDouble()
            "Promotions" -> point.promotions.toDouble()
            "Conversions" -> point.conversions.toDouble()
            "Orders" -> point.orders.toDouble()
            else -> point.spend
        }
    }
    val trendMax = trendValues.maxOrNull()?.takeIf { it > 0.0 } ?: 1000.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Column {
            Text(
                text = "Campaign Performance Trend",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${dailyTrend.size} daily data points",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }

        // Horizontal metric filter tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 3)
        ) {
            items(metricTabs) { tab ->
                val isSelected = tab == activeMetric
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                        .background(if (isSelected) darkPurple else light_grey)
                        .clickable { onMetricSelected(tab) }
                        .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.extraPadding / 3)
                ) {
                    Text(
                        text = tab,
                        fontSize = tokens.caption,
                        color = if (isSelected) whiteBg else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // SVG/Canvas Line Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.cardHeight * 1.3f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepY = size.height / 3
                val strokeEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                for (i in 0..3) {
                    val y = i * stepY
                    drawLine(
                        color = grey_border,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f,
                        pathEffect = strokeEffect
                    )
                }

                if (trendValues.isNotEmpty()) {
                    val stepX = size.width / (trendValues.size - 1).coerceAtLeast(1)
                    val trendPath = Path()

                    trendValues.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = (size.height - (value / trendMax * size.height * 0.8f)).toFloat().coerceIn(0f, size.height)
                        if (index == 0) trendPath.moveTo(x, y) else trendPath.lineTo(x, y)
                    }
                    drawPath(path = trendPath, color = chartIndigo, style = Stroke(width = 3.5f))
                }
            }
        }

        // X-Axis Date Labels
        if (dailyTrend.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelIndices = listOf(0, dailyTrend.size / 2, dailyTrend.lastIndex)
                labelIndices.distinct().forEach { idx ->
                    val point = dailyTrend.getOrNull(idx)
                    if (point != null) {
                        Text(
                            text = point.date.takeLast(5),
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 3: STATUS DISTRIBUTION (DONUT RING + PERCENTAGES)
// =========================================================================

@Composable
private fun StatusDistributionSection(
    totalCampaigns: Int,
    breakdown: List<CampaignStatusBreakdown>
) {
    val tokens = LocalAppTokens.current

    val colorsMap = mapOf(
        "Active" to chartEmerald,
        "Scheduled" to chartAmber,
        "Completed" to chartIndigo,
        "Paused / Stopped" to redText
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Text(
            text = "Status Distribution",
            fontSize = tokens.bodyMedium,
            color = blueText,
            fontWeight = FontWeight.Medium
        )

        // Donut Center
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.cardHeight * 1.2f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(tokens.cardHeight * 0.9f)) {
                val strokeWidth = 26f
                var startAngle = -90f

                breakdown.forEach { item ->
                    val sweep = (item.count.toFloat() / totalCampaigns.coerceAtLeast(1)) * 360f
                    if (sweep > 0f) {
                        drawArc(
                            color = colorsMap[item.status] ?: mutedText,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalCampaigns.toString(),
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Campaigns",
                    fontSize = tokens.label,
                    color = mutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Status List Rows
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 3)) {
            breakdown.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.extraPadding / 2)
                                .clip(CircleShape)
                                .background(colorsMap[item.status] ?: mutedText)
                        )
                        Spacer(modifier = Modifier.width(tokens.extraPadding / 3))
                        Text(
                            text = item.status,
                            fontSize = tokens.caption,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${item.count} (${item.percentage}%)",
                        fontSize = tokens.caption,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =========================================================================
// SECTION 4: BY PLATFORM PROGRESS BARS
// =========================================================================

@Composable
private fun ByPlatformPerformanceSection(
    platforms: List<PlatformPerformanceSummary>
) {
    val tokens = LocalAppTokens.current
    val maxSpend = platforms.maxOfOrNull { it.spend }?.takeIf { it > 0.0 } ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "By Platform",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Spend & Conversions",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }

        if (platforms.isNotEmpty()) {
            platforms.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.platform,
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${item.conversions} conv. • ${String.format(LocalLocale.current.platformLocale, "%.2f", item.roi)}x",
                            fontSize = tokens.caption,
                            color = greentext,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.extraPadding / 2)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius / 4))
                            .background(light_grey)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.spend / maxSpend).toFloat().coerceIn(0.05f, 1f))
                                .height(tokens.extraPadding / 2)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius / 4))
                                .background(chartIndigo)
                        )
                    }

                    Text(
                        text = "Spend: ₹${String.format(LocalLocale.current.platformLocale, "%,.2f", item.spend)}",
                        fontSize = tokens.label,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Text(
                text = "No platform metrics synced",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// =========================================================================
// SECTION 5: TOP PERFORMING CAMPAIGNS (CARDS)
// =========================================================================

@Composable
private fun TopPerformingCampaignsSection(
    campaigns: List<CampaignItemDto>,
    onViewAllClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Performing Campaigns",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.clickable { onViewAllClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View all",
                    fontSize = tokens.caption,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                )
            }
        }

        if (campaigns.isNotEmpty()) {
            campaigns.take(6).forEach { campaign ->
                CampaignCardRowItem(campaign = campaign)
            }
        } else {
            Text(
                text = "No campaigns available",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CampaignCardRowItem(
    campaign: CampaignItemDto
) {
    val tokens = LocalAppTokens.current
    val isActive = campaign.status.equals("Active", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
            .background(badgeGrey)
            .padding(tokens.screenPadding / 2),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 3)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaign.campaignName,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${campaign.platform} • ${campaign.campaignCode}",
                    fontSize = tokens.caption,
                    color = mutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 4))
                    .background(if (isActive) greenBg else yellowBg)
                    .padding(horizontal = tokens.extraPadding / 2, vertical = tokens.extraPadding / 6)
            ) {
                Text(
                    text = campaign.status,
                    fontSize = tokens.label,
                    color = if (isActive) greentext else yellowText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Spend", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                Text(text = "₹${campaign.spendValue}", fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            Column {
                Text(text = "Conversions", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                Text(text = campaign.conversions.toString(), fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            Column {
                Text(text = "ROAS", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                Text(text = campaign.roas, fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
            }
        }
    }
}