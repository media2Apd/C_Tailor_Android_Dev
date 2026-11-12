@file:Suppress("unused", "UnusedVariable")
package com.cuso.tailor.view.home.marketing.dashboard

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.marketing.CampaignItemDto
import com.cuso.tailor.model.marketing.ChannelOverviewData
import com.cuso.tailor.model.marketing.ConversionFunnelDto
import com.cuso.tailor.model.marketing.CustomerOverviewData
import com.cuso.tailor.model.marketing.GrowthTrendItemDto
import com.cuso.tailor.model.marketing.MarketingDashboardSummaryData
import com.cuso.tailor.model.marketing.SocialOverviewData
import com.cuso.tailor.model.marketing.WebAnalyticsOverviewData
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.chartAmber
import com.cuso.tailor.ui.theme.chartBlue
import com.cuso.tailor.ui.theme.chartCyan
import com.cuso.tailor.ui.theme.chartEmerald
import com.cuso.tailor.ui.theme.chartIndigo
import com.cuso.tailor.ui.theme.chartViolet
import com.cuso.tailor.ui.theme.darkPurple
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.MarketingViewModel

@Composable
fun MarketingDashboardScreen(
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFunnelDetails: () -> Unit = {},
    onNavigateToChannels: () -> Unit = {},
    onNavigateToCampaigns: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isLoading by viewModel.isLoading.collectAsState()
    val summaryData by viewModel.dashboardSummary.collectAsState()
    val channelData by viewModel.channelData.collectAsState()
    val socialOverview by viewModel.socialOverview.collectAsState()
    val webAnalyticsData by viewModel.webAnalyticsData.collectAsState()
    val customerOverview by viewModel.customerOverview.collectAsState()
    val campaignsList by viewModel.campaignsList.collectAsState()

    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val selectedTrendRange by viewModel.selectedTrendRange.collectAsState()

    var timeframeExpanded by remember { mutableStateOf(false) }
    val timeframeLabels = listOf("This Week", "Last Week", "This Month", "Last Month", "This Quarter", "This Year")
    val timeframeKeys = listOf("this_week", "last_week", "this_month", "last_month", "this_quarter", "this_year")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(
            title = "Fashion Marketing",
            subtitle = "ANALYTICS OVERVIEW",
            onClose = onNavigateBack
        )

        if (isLoading && summaryData == null) {
            ListSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Timeframe Filter Button
                item {
                    Spacer(Modifier.height(tokens.extraPadding / 2))
                    Box {
                        Row(
                            modifier = Modifier
                                .height(tokens.fieldHeight)
                                .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                                .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                                .clickable { timeframeExpanded = !timeframeExpanded }
                                .padding(horizontal = tokens.screenPadding / 2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Timeframe",
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(modifier = Modifier.width(tokens.extraPadding / 2))
                            val currentLabelIndex = timeframeKeys.indexOf(selectedTimeframe)
                            Text(
                                text = if (currentLabelIndex >= 0) timeframeLabels[currentLabelIndex] else "This Week",
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
                            expanded = timeframeExpanded,
                            onExpandChange = { timeframeExpanded = it },
                            options = timeframeLabels,
                            onOptionSelected = { selectedLabel ->
                                val index = timeframeLabels.indexOf(selectedLabel)
                                if (index >= 0) {
                                    viewModel.onTimeframeSelected(timeframeKeys[index])
                                }
                                timeframeExpanded = false
                            }
                        )
                    }
                }

                // 1. Primary Metrics (Uniform 2-column cards)
                item {
                    PrimaryMetricCardsSection(summaryData = summaryData)
                }

                // 2. Key Performance Ratios Section
                item {
                    KeyPerformanceRatiosSection(
                        summaryData = summaryData,
                        campaignsList = campaignsList
                    )
                }

                // 3. Marketing Growth Overview Section
                item {
                    MarketingGrowthOverviewSection(
                        activeRange = selectedTrendRange,
                        onRangeSelected = { viewModel.onTrendRangeSelected(it) },
                        trendList = summaryData?.growthTrend ?: emptyList()
                    )
                }

                // 4. Marketing Funnel Section
                item {
                    MarketingFunnelSection(
                        funnelData = summaryData?.conversionFunnel,
                        campaignsList = campaignsList,
                        channelData = channelData,
                        onDetailsClick = onNavigateToFunnelDetails
                    )
                }

                // 5. Marketing Channels Section
                item {
                    MarketingChannelsSection(
                        channelData = channelData,
                        onFilterClick = onNavigateToChannels
                    )
                }

                // 6. Social Media Performance Section
                item {
                    SocialMediaPerformanceSection(socialOverview = socialOverview)
                }

                // 7. Website Performance Scores Section
                item {
                    WebsitePerformanceScoresSection(webData = webAnalyticsData)
                }

                // 8. Customer & Order Growth Section
                item {
                    CustomerOrderGrowthSection(
                        customerData = customerOverview,
                        summaryData = summaryData,
                        campaignsList = campaignsList
                    )
                }

                item { Spacer(modifier = Modifier.height(tokens.screenPadding)) }
            }
        }
    }
}

// =========================================================================
// SECTION 1: PRIMARY METRICS (EQUAL HEIGHT 2-COLUMN GRID)
// =========================================================================

@Composable
private fun PrimaryMetricCardsSection(
    summaryData: MarketingDashboardSummaryData?
) {
    val tokens = LocalAppTokens.current
    val row1 = summaryData?.kpiRibbonRow1

    // LOG CURRENT SYMBOL AND VALUES
    val currentSymbol = currencySymbol()
    android.util.Log.d("MarketingDashboard", ">>> PrimaryMetricCardsSection: currentSymbol = '$currentSymbol'")

    val revenueVal = row1?.marketingRevenue?.value?.takeIf { it.isNotBlank() && it != "--" }?.let { "${currencySymbol()}$it" } ?: "${currencySymbol()}0.0L"
    val spendVal = row1?.marketingSpend?.value?.takeIf { it.isNotBlank() && it != "--" }?.let { "${currencySymbol()}$it" } ?: "${currencySymbol()}0.0L"
    val roasVal = row1?.roas?.value?.takeIf { it.isNotBlank() && it != "--" } ?: "0x"
    val ordersVal = row1?.ordersFromMarketing?.value?.takeIf { it.isNotBlank() && it != "--" } ?: "0"
    val newCustVal = row1?.newCustomers?.value?.takeIf { it.isNotBlank() && it != "--" } ?: "0"
    val convRateVal = row1?.conversionRate?.value?.takeIf { it.isNotBlank() && it != "--" } ?: "0%"

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "Marketing Revenue",
                    value = revenueVal,
                    caption = row1?.marketingRevenue?.subtitle ?: "Attributed",
                    sparklineColor = chartIndigo
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "Marketing Spend",
                    value = spendVal,
                    caption = row1?.marketingSpend?.subtitle ?: "Total Ad Spend",
                    sparklineColor = chartViolet
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "ROAS",
                    value = roasVal,
                    caption = row1?.roas?.subtitle ?: "Blended Return",
                    sparklineColor = chartEmerald
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "Marketing Orders",
                    value = ordersVal,
                    caption = row1?.ordersFromMarketing?.subtitle ?: "All Channels",
                    sparklineColor = chartCyan
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "New Customers",
                    value = newCustVal,
                    caption = row1?.newCustomers?.subtitle ?: "Acquired",
                    sparklineColor = chartAmber
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DashboardMetricCard(
                    title = "Conversion Rate",
                    value = convRateVal,
                    caption = row1?.conversionRate?.subtitle ?: "Overall",
                    sparklineColor = chartEmerald
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    caption: String,
    sparklineColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Canvas(
                modifier = Modifier
                    .width(tokens.fieldHeight)
                    .height(tokens.screenPadding)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.65f,
                        size.width * 0.65f, size.height * 0.35f,
                        size.width, size.height * 0.2f
                    )
                }
                drawPath(path = path, color = sparklineColor, style = Stroke(width = 3.5f))
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

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
            fontSize = tokens.caption,
            color = mutedText,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =========================================================================
// SECTION 2: KEY PERFORMANCE RATIOS
// =========================================================================

@Composable
private fun KeyPerformanceRatiosSection(
    summaryData: MarketingDashboardSummaryData?,
    campaignsList: List<CampaignItemDto>
) {
    val tokens = LocalAppTokens.current
    val row2 = summaryData?.kpiRibbonRow2

    val returningCust = row2?.returningCustomers?.value?.takeIf { it != "--" } ?: "0"
    val activeCampaigns = row2?.activeCampaigns?.value?.takeIf { it != "--" }
        ?: campaignsList.count { it.status.equals("Active", ignoreCase = true) }.toString()
    val aovVal = row2?.avgOrderValue?.value?.takeIf { it != "--" }?.let { "${currencySymbol()}$it" } ?: "${currencySymbol()}0"
    val webTrafficVal = row2?.websiteTraffic?.value?.takeIf { it != "--" } ?: "0"
    val cpaVal = row2?.costPerAcquisition?.value?.takeIf { it != "--" }?.let { "${currencySymbol()}$it" } ?: "${currencySymbol()}0"
    val repeatRateVal = row2?.repeatPurchaseRate?.value?.takeIf { it != "--" } ?: "0%"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Text(
            text = "KEY PERFORMANCE RATIOS",
            fontSize = tokens.bodySmall,
            color = Primary,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Returning Cust.",
                value = returningCust,
                badgeText = "Retention",
                badgeTextColor = greentext,
                badgeBg = greenBg
            )
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Campaigns",
                value = activeCampaigns,
                badgeText = "Active",
                badgeTextColor = greentext,
                badgeBg = greenBg
            )
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Avg Order Val.",
                value = aovVal,
                badgeText = "AOV",
                badgeTextColor = greentext,
                badgeBg = greenBg
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Web Traffic",
                value = webTrafficVal,
                badgeText = "Visits",
                badgeTextColor = greentext,
                badgeBg = greenBg
            )
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "CPA (Cost/Acq)",
                value = cpaVal,
                badgeText = "Cost",
                badgeTextColor = yellowText,
                badgeBg = yellowBg
            )
            RatioItemBox(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Repeat Rate",
                value = repeatRateVal,
                badgeText = "Repeat",
                badgeTextColor = greentext,
                badgeBg = greenBg
            )
        }
    }
}

@Composable
private fun RatioItemBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    badgeText: String,
    badgeTextColor: Color,
    badgeBg: Color
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
            .background(badgeGrey)
            .padding(tokens.extraPadding / 2),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 4)) {
            Text(
                text = label,
                fontSize = tokens.caption,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .padding(top = tokens.extraPadding / 4)
                .clip(RoundedCornerShape(tokens.cardCornerRadius / 4))
                .background(badgeBg)
                .padding(horizontal = tokens.extraPadding / 3, vertical = tokens.extraPadding / 6)
        ) {
            Text(
                text = badgeText,
                fontSize = tokens.label,
                color = badgeTextColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// =========================================================================
// SECTION 3: MARKETING GROWTH OVERVIEW
// =========================================================================

@SuppressLint("DefaultLocale")
@Composable
private fun MarketingGrowthOverviewSection(
    activeRange: String,
    onRangeSelected: (String) -> Unit,
    trendList: List<GrowthTrendItemDto>
) {
    val tokens = LocalAppTokens.current
    val ranges = listOf("7D", "30D", "3M", "6M", "1Y")
//    val ranges = listOf("7D", "30D", "3M", "6M", "1Y")

    val totalPromotions = trendList.sumOf { it.promotions }
    val totalSpend = trendList.sumOf { it.spend }
    val totalRevenue = trendList.sumOf { it.revenue }
    val blendedRoas = if (totalSpend > 0) totalRevenue / totalSpend else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Text(
            text = "Marketing Growth Overview",
            fontSize = tokens.bodyMedium,
            color = blueText,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                    .background(light_grey)
                    .padding(tokens.extraPadding / 5),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 4)
            ) {
                ranges.forEach { range ->
                    val isSelected = range == activeRange
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                            .background(if (isSelected) darkPurple else light_grey)
                            .clickable { onRangeSelected(range) }
                            .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.extraPadding / 3)
                    ) {
                        Text(
                            text = range,
                            fontSize = tokens.caption,
                            color = if (isSelected) whiteBg else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "ROAS: ${String.format("%.2f", blendedRoas)}x",
                fontSize = tokens.caption,
                color = greentext,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = chartBlue, label = "Promotions")
                ChartLegendItem(color = chartViolet, label = "Spend (${currencySymbol()})")
                ChartLegendItem(color = chartEmerald, label = "Revenue (${currencySymbol()})")
            }

            Text(
                text = "Promo: $totalPromotions",
                fontSize = tokens.caption,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        DynamicGrowthTrendCanvasChart(trendList = trendList)
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(tokens.extraPadding / 2)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(tokens.extraPadding / 4))
        Text(
            text = label,
            fontSize = tokens.caption,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DynamicGrowthTrendCanvasChart(
    trendList: List<GrowthTrendItemDto>
) {
    val tokens = LocalAppTokens.current
    val maxSpend = trendList.maxOfOrNull { it.spend }?.takeIf { it > 0.0 } ?: 1000.0
    val maxRev = trendList.maxOfOrNull { it.revenue }?.takeIf { it > 0.0 } ?: 1000.0
    val maxValue = maxOf(maxSpend, maxRev)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.cardHeight * 1.3f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepY = size.height / 4
                val strokeEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                for (i in 0..4) {
                    val y = i * stepY
                    drawLine(
                        color = grey_border,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f,
                        pathEffect = strokeEffect
                    )
                }

                if (trendList.isNotEmpty()) {
                    val stepX = if (trendList.size > 1) size.width / (trendList.size - 1) else size.width

                    // Spend Curve
                    val spendPath = Path()
                    trendList.forEachIndexed { index, item ->
                        val x = index * stepX
                        val y = (size.height - (item.spend / maxValue * size.height * 0.8f)).toFloat().coerceIn(0f, size.height)
                        if (index == 0) spendPath.moveTo(x, y) else spendPath.lineTo(x, y)
                    }
                    drawPath(path = spendPath, color = chartViolet, style = Stroke(width = 3.5f))

                    // Revenue Curve
                    val revPath = Path()
                    trendList.forEachIndexed { index, item ->
                        val x = index * stepX
                        val y = (size.height - (item.revenue / maxValue * size.height * 0.8f)).toFloat().coerceIn(0f, size.height)
                        if (index == 0) revPath.moveTo(x, y) else revPath.lineTo(x, y)
                    }
                    drawPath(path = revPath, color = chartEmerald, style = Stroke(width = 3.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 2))

        // Month Labels from API
        if (trendList.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendList.forEach { item ->
                    Text(
                        text = item.month.ifBlank { item.id },
                        fontSize = tokens.caption,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =========================================================================
// SECTION 4: MARKETING FUNNEL
// =========================================================================

@Composable
private fun MarketingFunnelSection(
    funnelData: ConversionFunnelDto?,
    campaignsList: List<CampaignItemDto>,
    channelData: ChannelOverviewData?,
    onDetailsClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val stages = funnelData?.stages ?: emptyList()
    val maxCount = stages.maxOfOrNull { it.count }?.takeIf { it > 0 } ?: 1L

    val overallConv = funnelData?.summary?.overallConversionRate ?: "0.00%"
    val visitorToOrder = funnelData?.summary?.visitorToOrderRate ?: "0.0%"
    val topCampaign = campaignsList.maxByOrNull { it.spendValue }?.campaignName ?: campaignsList.firstOrNull()?.campaignName ?: "--"
    val bestChannel = channelData?.channelPerformanceMatrix?.maxByOrNull { it.raw?.conv ?: 0L }?.channel ?: "--"

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
                text = "Marketing Funnel",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.clickable { onDetailsClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Details",
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

        // Funnel Stage Rows
        if (stages.isNotEmpty()) {
            stages.forEachIndexed { index, stageItem ->
                val fraction = (stageItem.count.toFloat() / maxCount).coerceIn(0.08f, 1f)
                val isLast = index == stages.lastIndex
                FunnelBarItem(
                    stage = stageItem.stage.replace("_", " "),
                    count = stageItem.count.toString(),
                    convText = "${stageItem.conversionRate}% conv.",
                    dropText = if (stageItem.dropoffRate > 0) "-${stageItem.dropoffRate}% drop" else null,
                    fillFraction = fraction,
                    barColor = if (isLast) Primary else primary_light,
                    textColor = if (isLast) whiteBg else Primary
                )
            }
        } else {
            Text(
                text = "No funnel metrics available for this period",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        // 4 Summary Sub-cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            FunnelSummaryCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Overall Conversion",
                metric = overallConv,
                metricColor = Primary,
                caption = "Reach → Order"
            )
            FunnelSummaryCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Top Campaign",
                metric = topCampaign,
                metricColor = redText,
                caption = "Active Focus"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            FunnelSummaryCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Best Channel",
                metric = bestChannel,
                metricColor = TextPrimary,
                caption = "Top Conversions"
            )
            FunnelSummaryCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Visitor-to-Order",
                metric = visitorToOrder,
                metricColor = TextPrimary,
                caption = "Conversion Rate"
            )
        }
    }
}

@Composable
private fun FunnelBarItem(
    stage: String,
    count: String,
    convText: String,
    dropText: String? = null,
    fillFraction: Float,
    barColor: Color,
    textColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stage,
                fontSize = tokens.caption,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (dropText != null) {
                    Spacer(modifier = Modifier.width(tokens.extraPadding / 2))
                    Text(
                        text = dropText,
                        fontSize = tokens.caption,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.buttonHeight / 1.7f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                .background(light_grey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction)
                    .height(tokens.buttonHeight / 1.7f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                    .background(barColor)
                    .padding(horizontal = tokens.screenPadding / 2),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = convText,
                    fontSize = tokens.label,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FunnelSummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    metric: String,
    metricColor: Color,
    caption: String
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
            .background(badgeGrey)
            .padding(tokens.screenPadding / 2),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 4)) {
            Text(text = label, fontSize = tokens.caption, color = TextSecondary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = metric, fontSize = tokens.bodySmall, color = metricColor, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = caption, fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// =========================================================================
// SECTION 5: MARKETING CHANNELS TABLE
// =========================================================================

@Composable
private fun MarketingChannelsSection(
    channelData: ChannelOverviewData?,
    onFilterClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val matrix = channelData?.channelPerformanceMatrix ?: emptyList()

    val totalReach = channelData?.kpiCards?.get("totalReach")?.value ?: matrix.sumOf { it.raw?.spend?.toLong() ?: 0L }.toString()
    val totalPromotions = matrix.sumOf { it.raw?.prom ?: 0L }.toString()
    val totalRevenue = channelData?.kpiCards?.get("totalRevenue")?.value ?: "${currencySymbol()}0"
    val overallRoi = channelData?.kpiCards?.get("overallRoi")?.value ?: "0%"

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
            Column {
                Text(
                    text = "Marketing Channels",
                    fontSize = tokens.bodyMedium,
                    color = blueText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Reach, promotions & ROI attribution",
                    fontSize = tokens.caption,
                    color = mutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius / 2))
                    .clickable { onFilterClick() }
                    .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.extraPadding / 3)
            ) {
                Text(text = "View All", fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                .background(badgeGrey)
                .padding(horizontal = tokens.extraPadding / 2, vertical = tokens.extraPadding / 3),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "CHANNEL", fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f))
            Text(text = "SPEND", fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.8f))
            Text(text = "CONV / ROAS", fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.9f))
        }

        // Channel Rows from API
        if (matrix.isNotEmpty()) {
            matrix.take(6).forEach { channelItem ->
                ChannelRowItem(
                    title = channelItem.channel,
                    sub = channelItem.type,
                    spend = channelItem.spend,
                    convRoas = "${channelItem.conversions} / ${channelItem.roas}"
                )
            }
        } else {
            Text(
                text = "No channel performance metrics available",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }

        // Channels Summary Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                .background(badgeGrey)
                .padding(tokens.screenPadding / 2),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChannelStatFooterItem(label = "Total Reach", value = totalReach)
            ChannelStatFooterItem(label = "Promotions", value = totalPromotions)
            ChannelStatFooterItem(label = "Attributed Rev.", value = totalRevenue)
            ChannelStatFooterItem(label = "Paid ROI", value = overallRoi, valueColor = greentext)
        }
    }
}

@Composable
private fun ChannelRowItem(
    title: String,
    sub: String,
    spend: String,
    convRoas: String
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding / 4),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            Text(text = title, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = sub, fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
        }

        Text(text = spend, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.8f))
        Text(text = convRoas, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.9f))
    }
}

@Composable
private fun ChannelStatFooterItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 5)) {
        Text(text = label, fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = tokens.bodySmall, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

// =========================================================================
// SECTION 6: SOCIAL MEDIA PERFORMANCE
// =========================================================================

@Composable
private fun SocialMediaPerformanceSection(
    socialOverview: SocialOverviewData?
) {
    val tokens = LocalAppTokens.current
    val row1 = socialOverview?.kpiRow1
    val row2 = socialOverview?.kpiRow2
    val posts = socialOverview?.posts ?: emptyList()

    val followers = row1?.get("totalFollowers")?.value?.takeIf { it != "--" } ?: "0"
    val reach = row1?.get("totalReach")?.value?.takeIf { it != "--" } ?: "0"
    val engagement = row1?.get("totalEngagement")?.value?.takeIf { it != "--" } ?: "0"

    val activeCount = posts.count { it.status.equals("ACTIVE", ignoreCase = true) }
    val pausedCount = posts.count { it.status.equals("PAUSED", ignoreCase = true) }

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
                text = "Social Media Performance",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Eng. Rate: ${row2?.get("engagementRate")?.value ?: "0%"}",
                fontSize = tokens.caption,
                color = greentext,
                fontWeight = FontWeight.Medium
            )
        }

        // Top 3 Social Metric Boxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            SocialMetricCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Followers", value = followers)
            SocialMetricCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Total Reach", value = reach)
            SocialMetricCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Engagement", value = engagement)
        }

        // Status Indicator Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(tokens.extraPadding / 2).clip(CircleShape).background(chartEmerald))
                Spacer(modifier = Modifier.width(tokens.extraPadding / 3))
                Text(text = "$activeCount Active", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(tokens.extraPadding / 2).clip(CircleShape).background(chartAmber))
                Spacer(modifier = Modifier.width(tokens.extraPadding / 3))
                Text(text = "$pausedCount Paused", fontSize = tokens.caption, color = yellowText, fontWeight = FontWeight.Medium)
            }
        }

        // Posts from API
        if (posts.isNotEmpty()) {
            posts.take(4).forEach { post ->
                SocialPlatformRowItem(
                    name = post.headline.ifBlank { post.name.take(28) },
                    metrics = "Reach ${post.reach} • ${post.clicks} Clicks",
                    statusBadge = post.status,
                    badgeBg = if (post.status.equals("ACTIVE", ignoreCase = true)) greenBg else yellowBg,
                    badgeText = if (post.status.equals("ACTIVE", ignoreCase = true)) greentext else yellowText
                )
            }
        } else {
            Text(
                text = "No synced social campaigns available",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SocialMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
            .background(badgeGrey)
            .padding(tokens.screenPadding / 2),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 5)
    ) {
        Text(text = label, fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium, maxLines = 1)
        Text(text = value, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun SocialPlatformRowItem(
    name: String,
    metrics: String,
    statusBadge: String,
    badgeBg: Color,
    badgeText: Color
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding / 5),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = metrics, fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(tokens.cardCornerRadius / 3))
                .background(badgeBg)
                .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.extraPadding / 4)
        ) {
            Text(text = statusBadge, fontSize = tokens.label, color = badgeText, fontWeight = FontWeight.Medium)
        }
    }
}

// =========================================================================
// SECTION 7: WEBSITE PERFORMANCE SCORES
// =========================================================================

@Composable
private fun WebsitePerformanceScoresSection(
    webData: WebAnalyticsOverviewData?
) {
    val tokens = LocalAppTokens.current
    val row1 = webData?.kpiRibbonRow1
    val summary = webData?.conversionSummary
    val trend = webData?.trafficTrend ?: emptyList()

    val pageViews = summary?.pageViews ?: row1?.get("pageViews")?.value ?: "0"
    val clicks = summary?.clicks ?: row1?.get("clicks")?.value ?: "0"
    val conversions = summary?.totalConversions ?: "0"
    val engRate = row1?.get("engagementRate")?.value ?: "0%"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Text(
            text = "WEBSITE PERFORMANCE",
            fontSize = tokens.bodySmall,
            color = Primary,
            fontWeight = FontWeight.Medium
        )

        // Web Stats Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            RatioItemBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Page Views", value = pageViews, badgeText = "Views", badgeTextColor = greentext, badgeBg = greenBg)
            RatioItemBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Clicks", value = clicks, badgeText = "Clicks", badgeTextColor = greentext, badgeBg = greenBg)
            RatioItemBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Conversions", value = conversions, badgeText = "Goal", badgeTextColor = greentext, badgeBg = greenBg)
            RatioItemBox(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Eng. Rate", value = engRate, badgeText = "Rate", badgeTextColor = greentext, badgeBg = greenBg)
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        // Traffic Growth Trend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Traffic Trend",
                fontSize = tokens.bodySmall,
                color = blueText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = webData?.lastSynced?.take(10) ?: "",
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.cardHeight * 1.1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val blueLineY = size.height * 0.35f
                val cyanLineY = size.height * 0.65f

                val filledPath = Path().apply {
                    moveTo(0f, cyanLineY)
                    lineTo(size.width, cyanLineY * 0.85f)
                    lineTo(size.width, size.height * 0.95f)
                    lineTo(0f, size.height * 0.95f)
                    close()
                }
                drawPath(path = filledPath, color = primary_light)

                drawLine(
                    color = Primary,
                    start = Offset(0f, blueLineY),
                    end = Offset(size.width, blueLineY * 0.85f),
                    strokeWidth = 3.5f
                )

                drawLine(
                    color = chartCyan,
                    start = Offset(0f, cyanLineY),
                    end = Offset(size.width, cyanLineY * 0.85f),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}

// =========================================================================
// SECTION 8: CUSTOMER & ORDER GROWTH (MATCHING SCREENSHOT 4 EXACTLY)
// =========================================================================

@Composable
private fun CustomerOrderGrowthSection(
    customerData: CustomerOverviewData?,
    summaryData: MarketingDashboardSummaryData?,
    campaignsList: List<CampaignItemDto>
) {
    val tokens = LocalAppTokens.current
    val custRow1 = customerData?.kpiRow1
    val summaryRow1 = summaryData?.kpiRibbonRow1
    val summaryRow2 = summaryData?.kpiRibbonRow2

    val enquiriesVal = custRow1?.get("totalCustomers")?.value?.takeIf { it != "--" } ?: "0"
    val newCustomersVal = custRow1?.get("newCustomers")?.value?.takeIf { it != "--" }
        ?: summaryRow1?.newCustomers?.value?.takeIf { it != "--" } ?: "0"
    val ordersGeneratedVal = summaryRow1?.ordersFromMarketing?.value?.takeIf { it != "--" } ?: "0"
    val convertedCustomersVal = custRow1?.get("returningCustomers")?.value?.takeIf { it != "--" }
        ?: summaryRow2?.returningCustomers?.value?.takeIf { it != "--" } ?: "0"
    val returningCustomersVal = summaryRow2?.returningCustomers?.value?.takeIf { it != "--" } ?: "0"
    val campaignOrdersVal = campaignsList.sumOf { it.customerOrders }.toString()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Customer & Order Growth",
                fontSize = tokens.bodyMedium,
                color = blueText,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View All",
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

        // Row 1: Total Enquiries & New Customers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerGrowthCardItem(
                    title = "Total Customer Enquiries",
                    value = enquiriesVal,
                    caption = "Total Inquiries",
                    sparklineColor = chartViolet
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerGrowthCardItem(
                    title = "New Customers",
                    value = newCustomersVal,
                    caption = "Acquired",
                    sparklineColor = chartCyan
                )
            }
        }

        // Row 2: Orders Generated & Converted Customers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerGrowthCardItem(
                    title = "Orders Generated",
                    value = ordersGeneratedVal,
                    caption = "Tracked Orders",
                    sparklineColor = chartEmerald
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerGrowthCardItem(
                    title = "Converted Customers",
                    value = convertedCustomersVal,
                    caption = "Converted",
                    sparklineColor = chartViolet
                )
            }
        }

        // Row 3: Action Cards (Returning Customers & Campaign Orders)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerActionCardItem(
                    title = "Returning Customers",
                    value = returningCustomersVal,
                    caption = "Action Required",
                    sparklineColor = chartAmber
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                CustomerActionCardItem(
                    title = "Campaign Orders",
                    value = campaignOrdersVal,
                    caption = "Campaign Driven",
                    sparklineColor = redText
                )
            }
        }
    }
}

@Composable
private fun CustomerGrowthCardItem(
    title: String,
    value: String,
    caption: String,
    sparklineColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 4)) {
            Text(
                text = title,
                fontSize = tokens.caption,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = caption,
                fontSize = tokens.label,
                color = greentext,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.extraPadding)
        ) {
            drawLine(
                color = sparklineColor,
                start = Offset(0f, size.height * 0.8f),
                end = Offset(size.width, size.height * 0.2f),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
private fun CustomerActionCardItem(
    title: String,
    value: String,
    caption: String,
    sparklineColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(tokens.cardCornerRadius / 15, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding / 2 + tokens.extraPadding / 4),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header with Title and Single-Line ACTION Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(tokens.extraPadding / 4))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius / 4))
                        .background(redBg)
                        .padding(horizontal = tokens.extraPadding / 2, vertical = tokens.extraPadding / 6)
                ) {
                    Text(
                        text = "ACTION",
                        fontSize = tokens.label,
                        color = redText,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            Text(
                text = caption,
                fontSize = tokens.label,
                color = yellowText,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 4))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.extraPadding)
        ) {
            drawLine(
                color = sparklineColor,
                start = Offset(0f, size.height * 0.8f),
                end = Offset(size.width, size.height * 0.2f),
                strokeWidth = 3f
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding / 3))

        // Re-engage action button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(tokens.cardCornerRadius / 15, Primary, RoundedCornerShape(tokens.cardCornerRadius / 2))
                .clickable { }
                .padding(vertical = tokens.extraPadding / 3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Re-engage →",
                fontSize = tokens.caption,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}