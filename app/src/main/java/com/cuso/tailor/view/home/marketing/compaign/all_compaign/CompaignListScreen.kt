package com.cuso.tailor.view.home.marketing.compaign.all_compaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.MarketingViewModel

@Composable
fun AllCampaignsListScreen(
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val filterState = rememberFilterDrawerState()

    val campaigns by viewModel.filteredCampaigns.collectAsState()
    val searchQuery by viewModel.campaignSearchQuery.collectAsState()
    val selectedStatus by viewModel.selectedCampaignStatus.collectAsState()
    val selectedPlatform by viewModel.selectedCampaignPlatform.collectAsState()
    val isLoading by viewModel.isLoadingCampaigns.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCampaigns()
    }

    val filterSections = remember(selectedStatus, selectedPlatform) {
        listOf(
            FilterSection(
                title = "Campaign Status",
                options = listOf(
                    FilterOption("Active", "Active", selectedStatus == "Active"),
                    FilterOption("Paused", "Paused", selectedStatus == "Paused"),
                    FilterOption("Scheduled", "Scheduled", selectedStatus == "Scheduled"),
                    FilterOption("Completed", "Completed", selectedStatus == "Completed")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Platform",
                options = listOf(
                    FilterOption("Meta", "Meta", selectedPlatform == "Meta"),
                    FilterOption("Google", "Google", selectedPlatform == "Google")
                ),
                isMultiSelect = false
            )
        )
    }

    val activeFilterCount = (if (!selectedStatus.isNullOrBlank()) 1 else 0) +
            (if (!selectedPlatform.isNullOrBlank()) 1 else 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "All Campaigns",
                subtitle = "Marketing Campaigns Ledger",
                onClose = onNavigateBack,
                trailingContent = {
                    IconButton(onClick = { viewModel.fetchCampaigns() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = title_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onCampaignSearchQueryChange(it) },
                placeholder = "Search campaign name, code...",
                showFilterIcon = true,
                filterCount = activeFilterCount,
                onFilterClick = { filterState.open() }
            )

            if (isLoading && campaigns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(tokens.buttonHeight)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(items = campaigns, key = { it.campaignCode }) { campaign ->
                        val (statusBg, statusText) = when (campaign.status.lowercase()) {
                            "active" -> Pair(greenBg, greentext)
                            "paused" -> Pair(light_grey, TextSecondary)
                            "scheduled" -> Pair(yellowBg, yellowText)
                            else -> Pair(light_blue, Primary)
                        }

                        DataCard(
                            item = campaign,
                            title = campaign.campaignName,
                            titleFontWeight = FontWeight.Bold,
                            titleColor = title_color,
                            code = campaign.campaignCode,
                            topBadgeText = campaign.status,
                            topBadgeBgColor = statusBg,
                            topBadgeTextColor = statusText,
                            topBadgeInline = true,
                            showDivider = false,
                            onClick = { onNavigateToDetail(campaign.campaignCode) },
                            footerAsColumns = true,
                            footerFields = listOf(
                                DataCardField(
                                    label = "Platform",
                                    text = campaign.platform,
                                    labelColor = mutedText,
                                    textColor = TextPrimary
                                ),
                                DataCardField(
                                    label = "Spend",
                                    text = "${currencySymbol()}${(campaign.spendValue / 1000).toInt()}K",
                                    labelColor = mutedText,
                                    textColor = TextPrimary
                                ),
                                DataCardField(
                                    label = "ROAS",
                                    text = campaign.roas,
                                    labelColor = mutedText,
                                    textColor = greentext,
                                    valueFontWeight = FontWeight.Bold
                                )
                            ),
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Mini stat breakdown
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Primary_background, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Promotions", fontSize = tokens.label, color = mutedText)
                                            Text(campaign.promotions.toString(), fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Conversions", fontSize = tokens.label, color = mutedText)
                                            Text(campaign.customerOrders.toString(), fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Revenue", fontSize = tokens.label, color = mutedText)
                                            Text("${currencySymbol()}${(campaign.revenueValue / 1000).toInt()}K", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                    }

                                    // Timeline text footer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Start: ${campaign.startDate}",
                                            fontSize = tokens.label,
                                            color = mutedText
                                        )
                                        Text(
                                            text = "End: ${campaign.endDate}",
                                            fontSize = tokens.label,
                                            color = mutedText
                                        )
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
                    }
                }
            }
        }

        FilterDrawer(
            state = filterState,
            title = "Filter Campaigns",
            sections = filterSections,
            onApply = { updated ->
                val statusOpt = updated.find { it.title == "Campaign Status" }?.options?.find { it.isSelected }?.id
                viewModel.onCampaignStatusFilterChange(statusOpt)

                val platOpt = updated.find { it.title == "Platform" }?.options?.find { it.isSelected }?.id
                viewModel.onCampaignPlatformFilterChange(platOpt)
            },
            onClearAll = { viewModel.clearCampaignFilters() }
        )
    }
}