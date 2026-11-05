@file:Suppress("UNUSED_PARAMETER", "SpellCheckingInspection", "unusedVariable")

package com.cuso.tailor.view.home.finance.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.TaxRateItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Dynamic Metric Summary Data Model
// ---------------------------------------------------------------------------
data class TaxStatItem(
    val label: String,
    val value: String,
    val statusText: String,
    val statusColor: Color,
    val valueColor: Color = TextPrimary
)

// ---------------------------------------------------------------------------
// Main Tax Rates Screen
// ---------------------------------------------------------------------------
@Composable
fun TaxRatesScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onAddTaxRate: () -> Unit,
    onAddTaxGroup: () -> Unit,
    onEditRate: (TaxRateItemDto) -> Unit = {},
    onDeleteRate: (TaxRateItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val listState = rememberLazyListState()

    // ── ViewModel State Observers ──
    val taxRates by viewModel.taxRates.collectAsState()
    val isLoading by viewModel.isLoadingTaxRates.collectAsState()
    val isLoadingMore by viewModel.isLoadingMoreTaxRates.collectAsState()
    val canLoadMore by viewModel.canLoadMoreTaxRates.collectAsState()
    val errorMessage by viewModel.taxRatesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isFirstLaunch by remember { mutableStateOf(true) }

    // ── Initial Fetch and Debounced Search ──
    LaunchedEffect(searchQuery) {
        if (isFirstLaunch) {
            isFirstLaunch = false
            viewModel.fetchTaxRates(page = 1)
        } else {
            delay(400)
            viewModel.fetchTaxRates(page = 1, search = searchQuery.takeIf { it.isNotBlank() })
        }
    }

    // ── Infinite Scroll Detection ──
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoading && !isLoadingMore) {
            viewModel.loadMoreTaxRates()
        }
    }

    // ── Dynamic Metric Calculations from Live Data ──
    val activeCount = remember(taxRates) {
        taxRates.count { it.status.equals("Active", ignoreCase = true) }
    }
    val inactiveCount = remember(taxRates) {
        taxRates.size - activeCount
    }
    val federalCount = remember(taxRates) {
        taxRates.count { it.jurisdictionLevel.equals("Federal", ignoreCase = true) }
    }
    val stateCount = remember(taxRates) {
        taxRates.count { it.jurisdictionLevel.equals("State_Province", ignoreCase = true) }
    }

    val dynamicStats = remember(taxRates) {
        listOf(
            TaxStatItem("Active Tax Rates", activeCount.toString(), "Compliant", greentext),
            TaxStatItem("Inactive Tax Rates", inactiveCount.toString(), if (inactiveCount == 0) "None" else "Deactivated", if (inactiveCount == 0) mutedText else redText),
            TaxStatItem("Central / Federal", federalCount.toString(), "CGST / IGST", blueText),
            TaxStatItem("State / Provincial", stateCount.toString(), "SGST / UTGST", chartIndigo)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = FabConfig(
                label = "Add Tax",
                icon = Icons.Default.KeyboardArrowDown,
                menuActions = listOf(
                    MenuAction(
                        label = "Add Tax Rate",
                        onClick = onAddTaxRate
                    ),
                    MenuAction(
                        label = "Add Tax Group",
                        onClick = onAddTaxGroup
                    )
                ),
                bottomPadding = 50.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                // Page Title Header
                TitleBar(title = "Tax Rates & Rules", onClose = onClose)
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Search and Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by rate name, tax tag, or rate...",
                    accentColor = Primary,
                    borderColor = sectionBorder,
                    textSecondaryColor = mutedText,
                    height = tokens.fieldHeight,
                    onFilterClick = { }
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = tokens.buttonHeight * 2)
                ) {

                    // 2x2 Dynamic Metrics Grid
                    item {
                        TaxStatsGrid(stats = dynamicStats)
                        Spacer(Modifier.height(10.dp))
                    }

                    // Initial Loading Skeleton
                    if (isLoading && taxRates.isEmpty()) {
                        item {
                            ListSkeleton()
                        }
                    }

                    // Error Message Display
                    if (errorMessage != null && taxRates.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                color = redBg,
                                border = BorderStroke(1.dp, redText.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    fontSize = tokens.bodySmall,
                                    color = redText,
                                    modifier = Modifier.padding(14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Empty State
                    if (!isLoading && taxRates.isEmpty() && errorMessage == null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No tax rates match your search" else "No tax rates found",
                                    fontSize = tokens.bodyMedium,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    // Dynamic API Items rendered using reusable DataCard
                    items(taxRates, key = { it.id }) { item ->
                        TaxRateDataCardItem(
                            item = item,
                            onEdit = { onEditRate(item) },
                            onDelete = { onDeleteRate(item) }
                        )
                    }

                    // Infinite Scroll Loading Footer
                    if (isLoadingMore) {
                        item {
                            ThreeDotLoading()
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2x2 Stats Grid Component
// ---------------------------------------------------------------------------
@Composable
private fun TaxStatsGrid(stats: List<TaxStatItem>, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    if (stats.size < 4) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[0], modifier = Modifier.weight(1f))
            VerticalDivider(Modifier.height(80.dp), thickness = 1.dp, color = dividerColor)
            TaxStatCell(stats[1], modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = dividerColor, thickness = 1.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            TaxStatCell(stats[2], modifier = Modifier.weight(1f))
            VerticalDivider(Modifier.height(80.dp), thickness = 1.dp, color = dividerColor)
            TaxStatCell(stats[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TaxStatCell(stat: TaxStatItem, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(
            text = stat.label,
            fontSize = tokens.caption,
            color = close_color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stat.value,
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = stat.valueColor
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stat.statusColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stat.statusText,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = stat.statusColor
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tax Rate Card Component Reusing Mandatory DataCard
// ---------------------------------------------------------------------------
@Composable
private fun TaxRateDataCardItem(
    item: TaxRateItemDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isActive = item.status.equals("Active", ignoreCase = true)

    // Formatted date string from ISO timestamp
    val formattedDate = remember(item.updatedAt, item.createdAt) {
        val raw = item.updatedAt ?: item.createdAt
        if (!raw.isNullOrBlank() && raw.length >= 10) {
            val parts = raw.take(10).split("-")
            if (parts.size == 3) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthIndex = (parts[1].toIntOrNull() ?: 1) - 1
                val monthName = months.getOrElse(monthIndex) { parts[1] }
                "${parts[2]} $monthName ${parts[0]}"
            } else raw.take(10)
        } else {
            "--"
        }
    }

    val metaText = remember(item.taxType, item.applicableFor) {
        val type = item.taxType ?: "GST"
        val scope = item.applicableFor ?: "Both"
        "$type • Applicable to $scope"
    }

    val tagText = item.taxTag ?: item.jurisdictionLevel ?: "Taxable"

    DataCard(
        item = item,
        onClick = { onEdit() },
        title = item.name,
        subtitle = metaText,
        titleFontWeight = FontWeight.Medium,
        titleColor = title_color,
        topBadgeText = item.status,
        topBadgeTextColor = if (isActive) greentext else redText,
        topBadgeBgColor = if (isActive) greenBg else redBg,
        topBadgeCornerRadius = 20.dp,
        actions = listOf(
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                onClick = onEdit
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Default.Delete,
                textColor = redText,
                onClick = onDelete
            )
        ),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rate Pill and Tax Classification Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rate Percentage Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(primary_light)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rate: ${item.rate}%",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    // Scope Tag Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(Primary_background)
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tagText,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                // Sales Tax Ledger Account details (if present)
                item.salesTaxAccountId?.let { account ->
                    Text(
                        text = "Account: ${account.accountName} (${account.accountCode})",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Footer Row: Formatted Date and Jurisdiction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updated: $formattedDate",
                        fontSize = tokens.caption,
                        color = close_color
                    )

                    item.jurisdictionLevel?.let { level ->
                        Text(
                            text = level.replace("_", " "),
                            fontSize = tokens.caption,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    )
}