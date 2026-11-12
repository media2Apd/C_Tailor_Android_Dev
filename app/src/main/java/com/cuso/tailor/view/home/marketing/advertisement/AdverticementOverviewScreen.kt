package com.cuso.tailor.view.home.marketing.advertisement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.marketing.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.MarketingViewModel
import java.text.DecimalFormat

@Composable
fun AdvertisingOverviewScreen(
    viewModel: MarketingViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isLoading by viewModel.isLoading.collectAsState()
    val overviewData by viewModel.overviewData.collectAsState()
    val oauthList by viewModel.oauthStatusList.collectAsState()
    val metaAccounts by viewModel.metaAccounts.collectAsState()
    val googleAccounts by viewModel.googleAccounts.collectAsState()
    val selectedMetaAccount by viewModel.selectedMetaAccount.collectAsState()
    val selectedGoogleAccount by viewModel.selectedGoogleAccount.collectAsState()
    val isSyncing by viewModel.isSyncingPlatforms.collectAsState()
    val campaigns by viewModel.campaignsList.collectAsState()
    val creatives by viewModel.creativesList.collectAsState()
    val selectedDays by viewModel.advertisingSelectedDays.collectAsState()
    val platformFilter by viewModel.advertisingPlatformFilter.collectAsState()
    val statusFilter by viewModel.advertisingStatusFilter.collectAsState()
    val searchQuery by viewModel.advertisingCampaignQuery.collectAsState()
    val sortOption by viewModel.advertisingSortOption.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllAdvertisingApiData()
    }

    val filteredCampaigns = remember(campaigns, searchQuery, platformFilter, statusFilter, sortOption) {
        campaigns.filter { campaign ->
            val matchesQuery = searchQuery.isBlank() ||
                    campaign.campaignName.contains(searchQuery, ignoreCase = true) ||
                    campaign.campaignCode.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = platformFilter == "All" || campaign.platform.contains(platformFilter, ignoreCase = true)
            val matchesStatus = statusFilter == "All" || campaign.status.equals(statusFilter, ignoreCase = true)
            matchesQuery && matchesPlatform && matchesStatus
        }.sortedByDescending {
            when (sortOption) {
                "Conversions" -> it.conversions.toDouble()
                "Revenue" -> it.revenueValue
                else -> it.spendValue
            }
        }
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = null
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Advertising",
                onClose = onNavigateBack
            )

            if (isLoading && overviewData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // 1. Live Ad Platform Connections Card
                    item {
                        LiveAdPlatformConnectionsCard(
                            oauthList = oauthList,
                            metaAccounts = metaAccounts,
                            googleAccounts = googleAccounts,
                            selectedMetaAccount = selectedMetaAccount,
                            selectedGoogleAccount = selectedGoogleAccount,
                            isSyncing = isSyncing,
                            onSyncClick = { viewModel.triggerPlatformSync() },
                            onSaveAccount = { platform, id -> viewModel.saveSelectedAccount(platform, id) },
                            onSelectMetaAccount = { viewModel.onMetaAccountSelected(it) },
                            onSelectGoogleAccount = { viewModel.onGoogleAccountSelected(it) }
                        )
                    }

                    // 2. Dropdown Filters (Days, Platforms, Statuses)
                    item {
                        FilterDropdownsRow(
                            selectedDays = selectedDays,
                            selectedPlatform = platformFilter,
                            selectedStatus = statusFilter,
                            onDaysChange = { viewModel.onAdvertisingTimeframeSelected(it) },
                            onPlatformChange = { viewModel.onAdvertisingPlatformFilterChange(it) },
                            onStatusChange = { viewModel.onAdvertisingStatusFilterChange(it) }
                        )
                    }

                    // 3. Top and Second Row KPIs
                    item {
                        AdvertisingKpisSection(overviewData = overviewData)
                    }

                    // 4. Advertising Performance Trend
                    item {
                        PerformanceTrendSection()
                    }

                    // 5. Platform Performance Drilldown and Breakdown
                    item {
                        PlatformPerformanceCardsAndTable(
                            platformCards = overviewData?.platformCards ?: emptyList()
                        )
                    }

                    // 6. Campaign Performance and Status Distribution
                    item {
                        CampaignPerformanceAndStatusDistribution(
                            campaigns = filteredCampaigns,
                            searchQuery = searchQuery,
                            sortOption = sortOption,
                            onSearchQueryChange = { viewModel.onAdvertisingCampaignQueryChange(it) },
                            onSortOptionChange = { viewModel.onAdvertisingSortChange(it) },
                            totalCampaignCount = campaigns.size,
                            activeCount = campaigns.count { it.status.equals("Active", ignoreCase = true) },
                            pausedCount = campaigns.count { it.status.equals("Paused", ignoreCase = true) },
                            scheduledCount = campaigns.count { it.status.equals("Scheduled", ignoreCase = true) },
                            completedCount = campaigns.count { it.status.equals("Completed", ignoreCase = true) },
                            stoppedCount = campaigns.count { it.status.equals("Stopped", ignoreCase = true) }
                        )
                    }

                    // 7. Top Performing Ads
                    item {
                        TopPerformingAdsCard(creatives = creatives)
                    }

                    // 8. Creative Performance and Recent Activity
                    item {
                        CreativePerformanceAndRecentActivity(creatives = creatives)
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION COMPONENTS
// =========================================================================

@Composable
private fun LiveAdPlatformConnectionsCard(
    oauthList: List<OAuthPlatformStatusDto>,
    metaAccounts: List<AdAccountItemDto>,
    googleAccounts: List<AdAccountItemDto>,
    selectedMetaAccount: String,
    selectedGoogleAccount: String,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onSaveAccount: (String, String) -> Unit,
    onSelectMetaAccount: (String) -> Unit,
    onSelectGoogleAccount: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val metaOauth = oauthList.find { it.platform == "META_ADS" }
    val googleOauth = oauthList.find { it.platform == "GOOGLE_ADS" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Ad Platform Connections",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Text(
                        text = "Connect an authorized account, select its ad account, then sync live campaign metrics.",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
                Surface(
                    modifier = Modifier.clickable(enabled = !isSyncing) { onSyncClick() },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = Primary
                ) {
                    Text(
                        text = if (isSyncing) "Syncing..." else "Sync now",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg,
                        modifier = Modifier.padding(
                            horizontal = tokens.screenPadding * 0.75f,
                            vertical = tokens.otpBoxSpacing * 0.75f
                        )
                    )
                }
            }

            PlatformAccountIntegrationBox(
                platformTitle = "Meta Ads",
                statusLabel = if (!metaOauth?.adAccountId.isNullOrBlank()) "Account selected" else if (metaOauth?.status == "CONNECTED") "Connected — select account" else "Not connected",
                accounts = metaAccounts,
                selectedAccountId = selectedMetaAccount,
                lastSyncTime = metaOauth?.lastSyncedAt,
                isSyncFailed = metaOauth?.lastSyncStatus == "FAILED",
                onAccountSelected = onSelectMetaAccount,
                onSaveAccountClick = { onSaveAccount("META_ADS", selectedMetaAccount) }
            )

            PlatformAccountIntegrationBox(
                platformTitle = "Google Ads",
                statusLabel = if (!googleOauth?.adAccountId.isNullOrBlank()) "Account selected" else if (googleOauth?.status == "CONNECTED") "Connected — select account" else "Not connected",
                accounts = googleAccounts,
                selectedAccountId = selectedGoogleAccount,
                lastSyncTime = googleOauth?.lastSyncedAt,
                isSyncFailed = googleOauth?.lastSyncStatus == "FAILED",
                onAccountSelected = onSelectGoogleAccount,
                onSaveAccountClick = { onSaveAccount("GOOGLE_ADS", selectedGoogleAccount) }
            )
        }
    }
}

@Composable
private fun PlatformAccountIntegrationBox(
    platformTitle: String,
    statusLabel: String,
    accounts: List<AdAccountItemDto>,
    selectedAccountId: String,
    lastSyncTime: String?,
    isSyncFailed: Boolean,
    onAccountSelected: (String) -> Unit,
    onSaveAccountClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var isDropdownOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.screenPadding * 0.75f),
            verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.75f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = platformTitle,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = statusLabel,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = if (statusLabel.contains("Account selected")) greentext else mutedText
                )
            }

            // Reusing FormDropdown for account selection
            val currentAccount = accounts.find { it.id == selectedAccountId }
            val accountDisplayOptions = accounts.map { "${it.name} (${it.id})" }
            val currentDisplayText = if (currentAccount != null) "${currentAccount.name} (${currentAccount.id})" else "Select ad account"

            FormDropdown(
                value = currentDisplayText,
                expanded = isDropdownOpen,
                onExpandChange = { isDropdownOpen = it },
                options = accountDisplayOptions,
                onOptionSelected = { selected ->
                    val matching = accounts.find { "${it.name} (${it.id})" == selected }
                    if (matching != null) {
                        onAccountSelected(matching.id)
                    }
                }
            )

            // Save and Reconnect Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight * 0.85f)
                        .clickable(enabled = selectedAccountId.isNotBlank()) { onSaveAccountClick() },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = if (selectedAccountId.isNotBlank()) whiteBg else light_grey,
                    border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Save account",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedAccountId.isNotBlank()) TextPrimary else mutedText
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight * 0.85f)
                        .clickable { /* Trigger OAuth reconnect flow */ },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    color = whiteBg,
                    border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Reconnect",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            if (!lastSyncTime.isNullOrBlank()) {
                Text(
                    text = "${if (isSyncFailed) "Last sync failed: " else "Last sync: "} $lastSyncTime",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Normal,
                    color = if (isSyncFailed) redText else mutedText
                )
            }
        }
    }
}

@Composable
private fun FilterDropdownsRow(
    selectedDays: Int,
    selectedPlatform: String,
    selectedStatus: String,
    onDaysChange: (Int) -> Unit,
    onPlatformChange: (String) -> Unit,
    onStatusChange: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var isDaysOpen by remember { mutableStateOf(false) }
    var isPlatformOpen by remember { mutableStateOf(false) }
    var isStatusOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
    ) {
        // Reusable Days Dropdown
        FormDropdown(
            modifier = Modifier.width(tokens.buttonHeight * 3.4f),
            value = "Last $selectedDays Days",
            expanded = isDaysOpen,
            onExpandChange = { isDaysOpen = it },
            options = listOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Last 90 Days"),
            onOptionSelected = { selected ->
                val days = selected.replace("Last ", "").replace(" Days", "").trim().toIntOrNull() ?: 7
                onDaysChange(days)
            }
        )

        // Reusable Platform Dropdown
        FormDropdown(
            modifier = Modifier.width(tokens.buttonHeight * 3.4f),
            value = if (selectedPlatform == "All") "All Platforms" else selectedPlatform,
            expanded = isPlatformOpen,
            onExpandChange = { isPlatformOpen = it },
            options = listOf("All Platforms", "Meta", "Google", "LinkedIn", "TikTok Ads", "Microsoft Ads"),
            onOptionSelected = { selected ->
                val plat = if (selected == "All Platforms") "All" else selected
                onPlatformChange(plat)
            }
        )

        // Reusable Status Dropdown
        FormDropdown(
            modifier = Modifier.width(tokens.buttonHeight * 3.4f),
            value = if (selectedStatus == "All") "All Statuses" else selectedStatus,
            expanded = isStatusOpen,
            onExpandChange = { isStatusOpen = it },
            options = listOf("All Statuses", "Active", "Paused", "Completed"),
            onOptionSelected = { selected ->
                val st = if (selected == "All Statuses") "All" else selected
                onStatusChange(st)
            }
        )
    }
}

@Composable
private fun AdvertisingKpisSection(
    overviewData: AdvertisingOverviewData?
) {
    val tokens = LocalAppTokens.current
    val top = overviewData?.topRowKpis
    val second = overviewData?.secondRowKpis

    val kpiList = listOf(
        Pair("TOTAL AD SPEND", formatCurrencyValue(top?.totalAdSpend?.value ?: 0.0)),
        Pair("IMPRESSIONS", formatCompactValue(top?.impressions?.value ?: 0.0)),
        Pair("REACH", formatCompactValue(top?.reach?.value ?: 0.0)),
        Pair("CLICKS", formatCompactValue(top?.clicks?.value ?: 0.0)),
        Pair("CTR", "${top?.ctr?.value ?: 0.0}%"),
        Pair("CPC", formatCurrencyValue(top?.cpc?.value ?: 0.0)),
        Pair("CPM", formatCurrencyValue(top?.cpm?.value ?: 0.0)),
        Pair("PROMOTIONS", "${second?.promotions?.value?.toInt() ?: 0}"),
        Pair("CONVERSIONS", "${second?.conversions?.value?.toInt() ?: 0}"),
        Pair("CPA", formatCurrencyValue(second?.cpa?.value ?: 0.0)),
        Pair("REVENUE", formatCurrencyValue(second?.revenue?.value ?: 0.0)),
        Pair("ROAS", "${second?.roas?.value ?: 0.0}x"),
        Pair("ROI", "${second?.roi?.value?.toInt() ?: 0}%")
    )

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        kpiList.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                rowPair.forEach { (label, value) ->
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(tokens.screenPadding * 0.75f),
                                verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = label, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                                }
                                Text(text = value, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = title_color)
                            }
                        }
                    }
                }
                if (rowPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PerformanceTrendSection() {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
        ) {
            Text(
                text = "Advertising Performance Trend",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = "Daily performance trend data is not available from the current sync yet.",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )
        }
    }
}

@Composable
private fun PlatformPerformanceCardsAndTable(
    platformCards: List<AdvertisingPlatformCardDto>
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Platform Performance",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Text(
                text = "Click a platform to drill down",
                fontSize = tokens.label,
                color = mutedText
            )
        }

        if (platformCards.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
            ) {
                Box(modifier = Modifier.padding(tokens.screenPadding), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Connect an ad account and sync it to see platform performance.",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        } else {
            platformCards.forEach { card ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.75f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.iconSize * 1.2f)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = card.platform.take(1),
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                            Spacer(modifier = Modifier.width(tokens.otpBoxSpacing * 0.75f))
                            Text(text = card.platform, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }

                        Text(text = formatCurrencyValue(card.spend), fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = title_color)
                        Text(text = "ROAS ${card.roas}x • CTR ${card.ctr}%", fontSize = tokens.caption, color = mutedText)

                        HorizontalDivider(color = grey_border)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(tokens.otpBoxSpacing * 0.75f).clip(CircleShape).background(greentext))
                            Spacer(modifier = Modifier.width(tokens.otpBoxSpacing * 0.5f))
                            Text(text = "Recorded metrics", fontSize = tokens.label, color = greentext)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "Platform Breakdown",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(tokens.otpBoxSpacing))

                    platformCards.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = tokens.otpBoxSpacing * 0.75f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = p.platform, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(text = "Spend: ${formatCurrencyValue(p.spend)}", fontSize = tokens.caption, color = mutedText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${formatCompactValue(p.impressions.toDouble())} Impr. • ${p.clicks} Clicks", fontSize = tokens.caption, color = TextSecondary)
                                Text(text = "ROAS: ${p.roas}x", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = greentext)
                            }
                        }
                        HorizontalDivider(color = grey_border)
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignPerformanceAndStatusDistribution(
    campaigns: List<CampaignItemDto>,
    searchQuery: String,
    sortOption: String,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (String) -> Unit,
    totalCampaignCount: Int,
    activeCount: Int,
    pausedCount: Int,
    scheduledCount: Int,
    completedCount: Int,
    stoppedCount: Int
) {
    val tokens = LocalAppTokens.current
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                Text(
                    text = "Campaign Performance",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reusable Search Filter Bar with search-alone mode enabled
                    Box(modifier = Modifier.weight(1.4f)) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            placeholder = "Search campaigns...",
                            isSearchBarAlone = true,
                            height = tokens.fieldHeight
                        )
                    }

                    // Reusable Form Dropdown for sorting
                    Box(modifier = Modifier.weight(1f)) {
                        FormDropdown(
                            value = "Sort: $sortOption",
                            expanded = isSortMenuOpen,
                            onExpandChange = { isSortMenuOpen = it },
                            options = listOf("Sort: Spend", "Sort: Conversions", "Sort: Revenue"),
                            onOptionSelected = { selected ->
                                onSortOptionChange(selected.removePrefix("Sort: ").trim())
                            }
                        )
                    }
                }
            }
        }

        if (campaigns.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
            ) {
                Box(modifier = Modifier.padding(tokens.screenPadding), contentAlignment = Alignment.Center) {
                    Text("No synchronized campaigns match these filters.", fontSize = tokens.caption, color = mutedText)
                }
            }
        } else {
            campaigns.take(10).forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.campaignName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = item.campaignCode, fontSize = tokens.label, color = mutedText)
                            }
                            StatusPillBadge(status = item.status)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Spend: ${formatCurrencyValue(item.spendValue)}", fontSize = tokens.caption, color = TextPrimary)
                            Text(text = "${formatCompactValue(item.impressions.toDouble())} Impr. • ${item.clicks} Clicks", fontSize = tokens.caption, color = TextSecondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Conv: ${item.conversions} • CTR: ${item.ctr}%", fontSize = tokens.label, color = mutedText)
                            Text(text = "ROAS: ${item.roas}", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = Primary)
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                Text(text = "Ad Status", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text(text = "Campaign distribution by provider status", fontSize = tokens.caption, color = mutedText)

                Column(verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.75f)) {
                    StatusDistributionRow(status = "Active", count = activeCount, total = totalCampaignCount, dotColor = greentext)
                    StatusDistributionRow(status = "Scheduled", count = scheduledCount, total = totalCampaignCount, dotColor = Primary)
                    StatusDistributionRow(status = "Completed", count = completedCount, total = totalCampaignCount, dotColor = TextSecondary)
                    StatusDistributionRow(status = "Paused", count = pausedCount, total = totalCampaignCount, dotColor = yellowText)
                    StatusDistributionRow(status = "Stopped", count = stoppedCount, total = totalCampaignCount, dotColor = redText)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    color = light_grey
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding * 0.75f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$totalCampaignCount", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Medium, color = title_color)
                        Text(text = "Total Campaigns", fontSize = tokens.caption, color = mutedText)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDistributionRow(
    status: String,
    count: Int,
    total: Int,
    dotColor: Color
) {
    val tokens = LocalAppTokens.current
    val progress = if (total > 0) (count.toFloat() / total.toFloat()) else 0f

    Column(verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.25f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(tokens.otpBoxSpacing).clip(CircleShape).background(dotColor))
                Spacer(modifier = Modifier.width(tokens.otpBoxSpacing * 0.5f))
                Text(text = status, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            Text(text = "$count campaigns", fontSize = tokens.caption, color = mutedText)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.otpBoxSpacing * 0.5f)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                .background(light_grey)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(progress)
                        .background(dotColor)
                )
            }
            if (1f - progress > 0f) {
                Spacer(modifier = Modifier.weight((1f - progress).coerceAtLeast(0.01f)))
            }
        }
    }
}

@Composable
private fun TopPerformingAdsCard(
    creatives: List<AdvertisingCreativeDto>
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
        ) {
            Text(text = "Top Performing Ads", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)

            if (creatives.isEmpty()) {
                Text(
                    text = "No ad creatives are available yet. Sync the connected account to load creatives.",
                    fontSize = tokens.caption,
                    color = mutedText,
                    modifier = Modifier.padding(vertical = tokens.otpBoxSpacing)
                )
            } else {
                creatives.take(5).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.otpBoxSpacing * 0.5f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "${item.platform} • ${item.formatType}", fontSize = tokens.label, color = mutedText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = formatCurrencyValue(item.spend), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text(text = "CTR: ${item.ctr}% • ROAS: ${item.roas}", fontSize = tokens.label, color = Primary)
                        }
                    }
                    HorizontalDivider(color = grey_border)
                }
            }
        }
    }
}

@Composable
private fun CreativePerformanceAndRecentActivity(
    creatives: List<AdvertisingCreativeDto>
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                Text(text = "Creative Performance", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text(text = "Top creatives by reach and conversion", fontSize = tokens.caption, color = mutedText)

                if (creatives.isEmpty()) {
                    Text(text = "No synchronized ad creative performance is available yet.", fontSize = tokens.caption, color = mutedText)
                } else {
                    creatives.take(4).chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
                        ) {
                            rowPair.forEach { creative ->
                                Box(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                        color = whiteBg,
                                        border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(tokens.buttonHeight * 2f)
                                                    .background(primary_light),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (creative.formatType.contains("Video", ignoreCase = true)) Icons.Outlined.PlayCircleOutline else Icons.Outlined.Image,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(tokens.iconSize * 1.5f)
                                                )
                                            }
                                            Column(modifier = Modifier.padding(tokens.screenPadding * 0.6f)) {
                                                Text(text = creative.name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(text = creative.formatType, fontSize = tokens.label, color = mutedText)
                                                Spacer(modifier = Modifier.height(tokens.otpBoxSpacing * 0.5f))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(text = "CTR: ${creative.ctr}%", fontSize = tokens.label, color = TextSecondary)
                                                    Text(text = "${creative.roas}x", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = greentext)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowPair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(tokens.cardCornerRadius * 0.08f, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing)
            ) {
                Text(text = "Recent Activity", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text(text = "Informational only • read-only", fontSize = tokens.caption, color = mutedText)
                Text(text = "Activity history is not available from the connected ad platforms yet.", fontSize = tokens.caption, color = mutedText)
            }
        }
    }
}

@Composable
private fun StatusPillBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor, dotColor) = when (status) {
        "Active" -> Triple(greenBg, greentext, greentext)
        "Paused" -> Triple(yellowBg, yellowText, yellowText)
        "Scheduled" -> Triple(primary_light, Primary, Primary)
        else -> Triple(light_grey, TextSecondary, mutedText)
    }

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = tokens.otpBoxSpacing * 0.75f, vertical = tokens.otpBoxSpacing * 0.25f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.otpBoxSpacing * 0.4f)
        ) {
            Box(modifier = Modifier.size(tokens.otpBoxSpacing * 0.6f).clip(CircleShape).background(dotColor))
            Text(text = status, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

// =========================================================================
// FORMATTING HELPERS
// =========================================================================

private fun formatCurrencyValue(amount: Double): String {
    val formatter = DecimalFormat("#,##,##0.##")
    return "${currencySymbol()}${formatter.format(amount)}"
}

private fun formatCompactValue(number: Double): String {
    return DecimalFormat("#,##0").format(number)
}