package com.cuso.tailor.view.home.marketing.compaign.all_compaign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.marketing.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.MarketingViewModel

@Composable
fun CampaignDetailOverviewScreen(
    campaignCode: String,
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Performance", "Audience", "Creative", "Timeline")

    val overview by viewModel.campaignOverview.collectAsState()
    val performance by viewModel.campaignPerformance.collectAsState()
    val audience by viewModel.campaignAudience.collectAsState()
    val creatives by viewModel.campaignCreatives.collectAsState()
    val timeline by viewModel.campaignTimeline.collectAsState()
    val isLoading by viewModel.isLoadingCampaignDetail.collectAsState()

    LaunchedEffect(campaignCode) {
        viewModel.loadCampaignDetailData(campaignCode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Campaign Overview",
            subtitle = "Marketing Suite",
            onClose = onNavigateBack
        )

        // Tab Row Bar
        SettingsTabs(
            tabs = tabTitles.map { TabItem(label = it) },
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            modifier = Modifier.padding(horizontal = tokens.screenPadding), // match screen padding
            height = 44.dp                                                  // taller for touch comfort
        )

        if (isLoading && overview == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                // Common Top Identity Card
                overview?.let { ov ->
                    TopCampaignHeaderCard(overview = ov)
                }

                // Render Active Tab Content
                when (selectedTabIndex) {
                    0 -> CampaignOverviewTab(overview = overview)
                    1 -> CampaignPerformanceTab(performance = performance)
                    2 -> CampaignAudienceTab(audience = audience)
                    3 -> CampaignCreativeTab(creatives = creatives)
                    4 -> CampaignTimelineTab(timeline = timeline)
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
            }
        }
    }
}

// =========================================================================
// TAB 1: OVERVIEW
// =========================================================================
@Composable
private fun CampaignOverviewTab(overview: CampaignOverviewDetailsDto?) {
    val tokens = LocalAppTokens.current
    if (overview == null) return

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
        Text("KEY PERFORMANCE INDICATORS", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        // 2-Column KPI Cards Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
            MetricGridCard(modifier = Modifier.weight(1f), label = "Campaign Spend", value = "₹${overview.campaignSpend}", subtext = "Budget: ₹${overview.budget}")
            MetricGridCard(modifier = Modifier.weight(1f), label = "Marketing Revenue", value = "₹${overview.marketingRevenue}", subtext = "+${overview.campaignRoi}x Yield", valueColor = Primary)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
            MetricGridCard(modifier = Modifier.weight(1f), label = "Campaign ROI", value = "${overview.campaignRoi}x", subtext = "Blended Return")
            MetricGridCard(modifier = Modifier.weight(1f), label = "ROI %", value = "${overview.roiPercent.toInt()}%", subtext = "Exceeds Target", valueColor = greentext)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
            MetricGridCard(modifier = Modifier.weight(1f), label = "Impressions", value = overview.impressions.toString(), subtext = "Total delivered")
            MetricGridCard(modifier = Modifier.weight(1f), label = "Reach", value = overview.reach.toString(), subtext = "Unique users")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
            MetricGridCard(modifier = Modifier.weight(1f), label = "Clicks", value = overview.clicks.toString(), subtext = "CTR ${overview.ctr}%")
            MetricGridCard(modifier = Modifier.weight(1f), label = "CPM", value = "₹${overview.cpm}", subtext = "Cost / 1k imp")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
            MetricGridCard(modifier = Modifier.weight(1f), label = "Customer Orders", value = overview.customerOrders.toString(), subtext = "CPL ₹${overview.cpl}")
            MetricGridCard(modifier = Modifier.weight(1f), label = "Order Conversions", value = overview.orderConversions.toString(), subtext = "CPA ₹${overview.cpa}")
        }

        // Campaign Details Specifications Table
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                .padding(tokens.screenPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                Text("• Campaign Details", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                SpecRow("Campaign Name", overview.metadata?.campaignName.orEmpty())
                SpecRow("Campaign ID", overview.metadata?.campaignCode.orEmpty())
                SpecRow("Platform", overview.metadata?.platform.orEmpty())
                SpecRow("Channel", overview.metadata?.channelType.orEmpty())
                SpecRow("Objective", overview.metadata?.objective.orEmpty())
                SpecRow("Status", overview.metadata?.status.orEmpty())
                SpecRow("Start Date", overview.metadata?.startDate?.take(10).orEmpty())
                SpecRow("End Date", overview.metadata?.endDate?.take(10).orEmpty())
                SpecRow("Total Budget", "₹${overview.budget}")
                SpecRow("Total Spend", "₹${overview.campaignSpend}")
                SpecRow("Remaining Budget", "₹${overview.remainingBudget}", valueColor = greentext)
            }
        }
    }
}

// =========================================================================
// TAB 2: PERFORMANCE
// =========================================================================
@Composable
private fun CampaignPerformanceTab(performance: CampaignPerformanceData?) {
    val tokens = LocalAppTokens.current
    if (performance == null) return

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
        Text("CONVERSION FUNNEL", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                .padding(tokens.screenPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                performance.funnel.forEach { stage ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stage.stage, fontSize = tokens.bodySmall, color = TextSecondary)
                            Text("${stage.count} (${stage.percentage}%)", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(light_grey)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    // Convert to Float first before coerceIn
                                    .fillMaxWidth((stage.percentage.toFloat() / 100f).coerceIn(0.04f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Primary)
                            )
                        }
                    }
                }
            }
        }

        // Performance Totals Block
        performance.totals?.let { tot ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                MetricGridCard(modifier = Modifier.weight(1f), label = "Total Spend", value = "₹${tot.spend}")
                MetricGridCard(modifier = Modifier.weight(1f), label = "Total Revenue", value = "₹${tot.revenue}", valueColor = Primary)
                MetricGridCard(modifier = Modifier.weight(1f), label = "ROAS", value = "${tot.roas}x", valueColor = greentext)
            }
        }
    }
}

// =========================================================================
// TAB 3: AUDIENCE
// =========================================================================
@Composable
private fun CampaignAudienceTab(audience: List<CampaignAudienceQuarterDto>) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
        Text("AUDIENCE BREAKDOWN BY QUARTER", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                .padding(tokens.screenPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PERIOD", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                    Text("SPEND", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                    Text("PROMOTIONS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                    Text("CONV.", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                }
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                if (audience.isEmpty()) {
                    Text("No quarterly audience records available.", fontSize = tokens.bodySmall, color = mutedText)
                } else {
                    audience.forEach { q ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(q.period, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                            Text("₹${q.spend}", fontSize = tokens.bodySmall, color = TextPrimary)
                            Text(q.promotions.toString(), fontSize = tokens.bodySmall, color = TextPrimary)
                            Text(q.conversions.toString(), fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = greentext)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: CREATIVE
// =========================================================================
@Composable
private fun CampaignCreativeTab(creatives: List<CampaignCreativeItemDto>) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
        Text("CREATIVE ASSETS (${creatives.size})", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        if (creatives.isEmpty()) {
            Text("No creative assets synced for this campaign.", fontSize = tokens.bodySmall, color = mutedText)
        } else {
            creatives.forEach { cr ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(whiteBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(tokens.screenPadding)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        // Thumbnail with play button if video
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(light_grey),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cr.mediaThumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = cr.mediaThumbnailUrl,
                                    contentDescription = cr.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = whiteBg, modifier = Modifier.size(28.dp))
                            }
                        }

                        Text(cr.creativeCode, fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Bold)
                        Text(cr.name.ifBlank { cr.headline }, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)

                        // Creative metrics preview card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Primary_background, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.screenPadding / 2),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Impressions", fontSize = tokens.label, color = mutedText)
                                Text(cr.impressions.toString(), fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                            }
                            Column {
                                Text("Clicks", fontSize = tokens.label, color = mutedText)
                                Text(cr.clicks.toString(), fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                            }
                            Column {
                                Text("CTR", fontSize = tokens.label, color = mutedText)
                                Text(cr.ctr ?: "--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 5: TIMELINE
// =========================================================================
@Composable
private fun CampaignTimelineTab(timeline: List<CampaignTimelineEventDto>) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
        Text("CAMPAIGN LIFECYCLE TIMELINE", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                .background(whiteBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                .padding(tokens.screenPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                if (timeline.isEmpty()) {
                    Text("No lifecycle timeline events available.", fontSize = tokens.bodySmall, color = mutedText)
                } else {
                    timeline.forEach { ev ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (ev.isHighlighted) Primary else greentext)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ev.eventTitle, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                Text("Source: ${ev.eventSource}", fontSize = tokens.caption, color = mutedText)
                            }
                            Text(ev.eventTimestamp.take(10), fontSize = tokens.caption, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// HELPER REUSABLE WIDGETS
// =========================================================================
@Composable
private fun TopCampaignHeaderCard(overview: CampaignOverviewDetailsDto) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.imageDp * 0.8f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(light_blue),
                contentAlignment = Alignment.Center
            ) {
                Text("M", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = overview.metadata?.campaignName.orEmpty(),
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                            .background(greenBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(overview.metadata?.status ?: "Active", fontSize = tokens.label, color = greentext, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "ID: ${overview.metadata?.campaignCode.orEmpty()}",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }
        }
    }
}

@Composable
private fun MetricGridCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String = "", // Added default empty string parameter
    valueColor: Color = title_color
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
            .padding(tokens.screenPadding * 0.8f)
    ) {
        Column {
            Text(label, fontSize = tokens.caption, color = mutedText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
            if (subtext.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtext, fontSize = tokens.label, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, valueColor: Color = title_color) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}