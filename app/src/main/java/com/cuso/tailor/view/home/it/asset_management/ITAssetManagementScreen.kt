package com.cuso.tailor.view.home.it.asset_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.AssetMetricsDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItAssetListScreen(
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDetail: (String) -> Unit,
    onNavigateRegister: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val filterState = rememberFilterDrawerState()

    val assets by viewModel.filteredAssets.collectAsState()
    val metrics by viewModel.assetMetrics.collectAsState()
    val searchQuery by viewModel.assetSearchQuery.collectAsState()
    val selectedStatus by viewModel.selectedAssetStatus.collectAsState()
    val selectedType by viewModel.selectedAssetType.collectAsState()
    val isLoading by viewModel.isLoadingAssets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAssetDashboard()
    }

    val filterSections = remember(selectedStatus, selectedType) {
        listOf(
            FilterSection(
                title = "Asset Status",
                options = listOf(
                    FilterOption("Assigned", "Assigned", selectedStatus == "Assigned"),
                    FilterOption("Available", "Available", selectedStatus == "Available"),
                    FilterOption("Under Maintenance", "Under Maintenance", selectedStatus == "Under Maintenance"),
                    FilterOption("Disposal Due", "Disposal Due", selectedStatus == "Disposal Due")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Asset Type",
                options = listOf(
                    FilterOption("Hardware", "Hardware", selectedType == "Hardware"),
                    FilterOption("Software", "Software", selectedType == "Software"),
                    FilterOption("License", "License", selectedType == "License"),
                    FilterOption("Peripheral", "Peripheral", selectedType == "Peripheral")
                ),
                isMultiSelect = false
            )
        )
    }

    val activeFilterCount = (if (!selectedStatus.isNullOrBlank()) 1 else 0) +
            (if (!selectedType.isNullOrBlank()) 1 else 0)

    val fabConfig = remember(onNavigateRegister) {
        FabConfig(
            label = "Register Asset",
            icon = Icons.Default.Add,
            onClick = onNavigateRegister
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = fabConfig
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Asset",
                subtitle = "IT Asset Management",
                onClose = onNavigateBack,
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                            .background(whiteBg)
                            .clickable { viewModel.fetchAssetDashboard() }
                            .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.screenPadding / 3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = title_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.screenPadding / 4))
                        Text(
                            text = "Refresh",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onAssetSearchQueryChange(it) },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                filterCount = activeFilterCount,
                onFilterClick = { filterState.open() }
            )

            if (isLoading && assets.isEmpty()) {
                ListSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    item{
                        metrics?.let {
                            AssetMetricsGrid(metrics = it)
                        }
                    }
                    items(items = assets, key = { it.assetId.ifEmpty { it.id.orEmpty() } }) { asset ->
                        val (statusBg, statusText) = getAssetStatusColors(asset.status)

                        DataCard(
                            item = asset,
                            title = asset.assetName,
                            titleFontWeight = FontWeight.Medium,
                            titleColor = blackTitle,
                            smalltitle = asset.assetId,
                            subtitle = asset.type.uppercase(),
                            code = null,
                            topBadgeText = asset.status,
                            topBadgeBgColor = statusBg,
                            topBadgeTextColor = statusText,
                            topBadgeInline = true,
                            showDivider = false,
                            actions = listOf(
                                MenuAction(
                                    label = "View Details",
                                    onClick = { onNavigateDetail(asset.assetId.ifEmpty { asset.id.orEmpty() }) }
                                )
                            ),
                            onClick = { onNavigateDetail(asset.assetId.ifEmpty { asset.id.orEmpty() }) },
                            footerAsColumns = true,
                            footerFields = listOf(
                                DataCardField(
                                    label = "LOCATION",
                                    text = asset.location ?: "--",
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                ),
                                DataCardField(
                                    label = "SERIAL / LIC",
                                    text = asset.serialOrLicense ?: "--",
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                ),
                                DataCardField(
                                    label = "ASSET ID",
                                    text = asset.assetId,
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                )
                            ),
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ASSIGNED TO :",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                        Spacer(Modifier.width(tokens.screenPadding / 4))
                                        Text(
                                            text = asset.assignedTo ?: "Unassigned",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Normal,
                                            color = TextPrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "LIFECYCLE :",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                        Spacer(Modifier.width(tokens.screenPadding / 4))
                                        Text(
                                            text = asset.lifecycle ?: "--",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Normal,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(tokens.buttonHeight + tokens.screenPadding))
                    }
                }
            }
        }

        FilterDrawer(
            state = filterState,
            title = "Filter Assets",
            sections = filterSections,
            onApply = { updatedSections ->
                val statusSec = updatedSections.find { it.title == "Asset Status" }
                val selectedStatusOption = statusSec?.options?.find { it.isSelected }?.id
                viewModel.onAssetStatusFilterChange(selectedStatusOption)

                val typeSec = updatedSections.find { it.title == "Asset Type" }
                val selectedTypeOption = typeSec?.options?.find { it.isSelected }?.id
                viewModel.onAssetTypeFilterChange(selectedTypeOption)
            },
            onClearAll = {
                viewModel.clearAssetFilters()
            }
        )
    }
}

@Composable
private fun AssetMetricsGrid(metrics: AssetMetricsDto) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "TOTAL ASSETS",
                value = metrics.totalAssets.toString(),
                valueColor = Primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "ASSIGNED",
                value = metrics.assigned.toString(),
                valueColor = greentext
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "AVAILABLE",
                value = metrics.available.toString(),
                valueColor = TextPrimary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "UNDER MAINTENANCE",
                value = metrics.underMaintenance.toString(),
                valueColor = Primary
            )
        }

        MetricCard(
            modifier = Modifier.fillMaxWidth(),
            label = "DISPOSAL DUE",
            value = metrics.disposalDue.toString(),
            valueColor = TextPrimary
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding * 0.75f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)) {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

fun getAssetStatusColors(status: String?): Pair<Color, Color> {
    return when (status?.lowercase()) {
        "assigned", "active" -> Pair(greenBg, greentext)
        "available" -> Pair(light_blue, Primary)
        "under maintenance", "maintenance" -> Pair(yellowBg, yellowText)
        "disposal due" -> Pair(redBg, redText)
        else -> Pair(light_grey, TextSecondary)
    }
}