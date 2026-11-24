@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.reports.inventory.report

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

/*
 * ASSUMPTIONS (adjust to your real DTOs / FilterSection):
 *
 * FilterSection(title: String, options: List<String>, selected: Set<String>)
 *
 * StockSummaryDto: totalActiveSkus, totalUnits, stockValuation, lowStockItems, outOfStockItems
 * StockSummaryItemDtoReport: name, sku, statusBadge, category, isFabric, uom,
 *                            totalValuation, onHand, reserved, available
 *
 * LowStockReorderSummaryDto: lowStockSkus, unitsDeficit, outOfStockCount,
 *                            automatedRulesActive, pendingPoApprovals, estReorderCost
 * LowStockReorderItemDto: name, sku, statusBadge, estCost, stockAvailable, threshold, uom,
 *                         suggestedOrder, preferredSupplier, automationRule
 *
 * StockMovementSummaryDto: totalMovements, totalInwardQty, totalOutwardQty, manualAdjustments
 * StockMovementItemDto: itemName, sku, movementType, isPositive, quantity, uom,
 *                       movementDate (ISO string), warehouse, bin, handledBy, reason
 *
 * MaterialConsumptionSummaryDto: consumptionEvents, totalConsumedUnits, consumedValuation, fabricDrawEvents
 * MaterialConsumptionItemDto: itemName, sku, quantity, uom, totalValue, consumptionDate (ISO string),
 *                             materialClassification, warehouse, handledBy, usageStream
 *
 * InventoryValuationSummaryDto: assetCostValue, expectedRetailRevenue, potentialGrossProfit, overallGrossMargin
 * InventoryValuationItemDto: name, sku, category, retailPrice, costPrice, stockUnits,
 *                            assetCostValue, expectedRetailValue, potentialProfit, marginPercent
 *
 * All numeric fields are assumed to be Double (Int is fine for counts).
 */

// =============================================================================
// SHARED UI PIECES FOR REPORT RECORD CARDS
// =============================================================================

/** Small coloured pill shown at the top-right of a record card (e.g. "In Stock"). */
data class ReportBadge(
    val text: String,
    val textColor: Color,
    val bgColor: Color
)

@Composable
fun ReportStatusChip(badge: ReportBadge) {
    val tokens = LocalAppTokens.current
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 2f),
        color = badge.bgColor,
        // Soft border built from the text colour so the chip works with any palette pair
        border = BorderStroke(tokens.extraPadding * 0.08f, badge.textColor.copy(alpha = 0.25f))
    ) {
        Text(
            text = badge.text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = badge.textColor,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = tokens.extraPadding * 0.9f,
                vertical = tokens.extraPadding * 0.3f
            )
        )
    }
}

/**
 * Generic record card used by every report list item:
 * header (title, code, badge, menu) -> divider -> summary row -> detail grid box.
 */
@Composable
fun ReportRecordCard(
    title: String,
    code: String,
    badge: ReportBadge?,
    summaryLabel: String,
    gridRows: List<List<ReportGridCell>>,
    modifier: Modifier = Modifier,
    summaryTrailing: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = tokens.screenPadding,
                vertical = tokens.extraPadding * 0.5f
            ),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(tokens.extraPadding * 0.1f, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.extraPadding * 1.2f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
        ) {
            // Header: title + code on the left, badge and overflow menu on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = code,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                if (badge != null) {
                    ReportStatusChip(badge)
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = iconMuted,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }

            androidx.compose.material3.HorizontalDivider(color = dividerColor)

            // Summary row: label on the left, key value on the right
            ReportInlineMetricRow(label = summaryLabel) {
                summaryTrailing()
            }

            // Detail grid box with the remaining fields
            ReportDetailGridBox(rows = gridRows)
        }
    }
}

/** Bold value text used in the summary row trailing slot. */
@Composable
private fun SummaryValue(text: String, color: Color = TextPrimary) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = color,
        maxLines = 1
    )
}

// =============================================================================
// SMALL HELPERS (formatting, colours, filter plumbing)
// =============================================================================

/** Shows whole numbers without decimals, otherwise two decimals. */
@SuppressLint("DefaultLocale")
private fun Double.fmtQty(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else String.format("%.2f", this)

/** Signed quantity such as +10 or -25. */
private fun Double.fmtSigned(): String =
    if (this >= 0) "+${this.fmtQty()}" else this.fmtQty()

/** Maps a stock status label to a (text, background) colour pair from Color.kt. */
private fun stockStatusColors(label: String): Pair<Color, Color> {
    val key = label.lowercase()
    return when {
        key.contains("out") -> redText to redBg
        key.contains("low") -> yellowText to yellowBg
        key.contains("in stock") || key.contains("available") -> greentext to greenBg
        else -> Primary to primary_light
    }
}

/** Maps a stock movement type to a (text, background) colour pair. */
private fun movementTypeColors(type: String): Pair<Color, Color> {
    val key = type.lowercase()
    return when {
        key.contains("purchase") || key.contains("receipt") || key.contains("return") -> greentext to greenBg
        key.contains("sale") || key.contains("consum") || key.contains("issue") -> redText to redBg
        key.contains("adjust") -> orangeText to orangeBg
        else -> Primary to primary_light
    }
}

/** Reads the selected values of one filter section by its title. */
private fun List<FilterSection>.selectedOf(title: String): Set<String> =
    find { it.title == title }
        ?.options
        ?.filter { it.isSelected }
        ?.map { it.id }
        ?.toSet()
        .orEmpty()

/** Counts how many filter values are currently active across all sections. */
private fun List<FilterSection>.activeCount(): Int =
    sumOf { section -> section.options.count { it.isSelected } }

/** Returns a copy of the sections with every selection cleared. */
private fun List<FilterSection>.cleared(): List<FilterSection> =
    map { section ->
        section.copy(options = section.options.map { it.copy(isSelected = false) })
    }

// =============================================================================
// 1. STOCK SUMMARY REPORT
// =============================================================================

@Composable
fun StockSummaryReportScreen(
    onClose: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val paging by viewModel.stockSummaryPagingState.collectAsState()
    val items by viewModel.filteredStockSummaryList.collectAsState()
    val query by viewModel.stockSummarySearchQuery.collectAsState()
    val summary by viewModel.stockSummary.collectAsState()

    // Local copy of the filter sections so the active filter count can be shown
    var sections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("In Stock", "In Stock"),
                        FilterOption("Low Stock", "Low Stock"),
                        FilterOption("Out of Stock", "Out of Stock")
                    )
                ),
                FilterSection(
                    title = "Type",
                    options = listOf(
                        FilterOption("Fabric", "Fabric"),
                        FilterOption("Standard Item", "Standard Item")
                    )
                )
            )
        )
    }

    // Initial load
    LaunchedEffect(Unit) { viewModel.loadStockSummaryReport() }

    // Top metric cards
    val metrics = listOf(
        ReportMetricSpec(
            label = "TOTAL ACTIVE SKUS",
            value = "${summary?.totalSkus ?: 0}",
            subtitle = "${summary?.totalOnHandUnits?.fmtQty() ?: "0"} total units in stock",
            icon = Icons.Default.GridView,
            iconTint = purpleText,
            iconBgColor = purpleBg
        ),
        ReportMetricSpec(
            label = "STOCK VALUATION",
            value = viewModel.formatCurrency(summary?.totalStockValuation ?: 0.0),
            subtitle = "On-hand asset value",
            icon = Icons.Default.CurrencyRupee,
            valueColor = greentext,
            iconTint = greentext,
            iconBgColor = greenBg
        ),
        ReportMetricSpec(
            label = "LOW STOCK ITEMS",
            value = "${summary?.lowStockCount ?: 0}",
            subtitle = "Below reorder point",
            icon = Icons.Default.Warning,
            valueColor = orangeText,
            iconTint = yellowText,
            iconBgColor = yellowBg
        ),
        ReportMetricSpec(
            label = "OUT OF STOCK",
            value = "${summary?.outOfStockCount ?: 0}",
            subtitle = "Depleted inventory",
            icon = Icons.Default.Block,
            valueColor = redText,
            iconTint = redText,
            iconBgColor = redBg
        )
    )

    ReportInfinitelyPaginatedScaffold(
        title = "Stock Summary Report",
        sectionTitle = "Stock Inventory Directory",
        searchQuery = query,
        onSearchQueryChange = viewModel::onStockSummarySearchQueryChange,
        searchPlaceholder = "Search by name, SKU or barcode...",
        metrics = metrics,
        pagingState = paging,
        filteredItems = items,
        itemKey = { it.id },
        filterSections = sections,
        activeFilterCount = sections.activeCount(),
        onApplyFilters = { applied ->
            sections = applied
            viewModel.applyStockSummaryFilters(
                statuses = applied.selectedOf("Status"),
                types = applied.selectedOf("Type")
            )
        },
        onClearAllFilters = {
            sections = sections.cleared()
            viewModel.clearStockSummaryFilters()
        },
        onLoadMore = { viewModel.loadStockSummaryReport(isLoadMore = true) },
        onShareClick = { },
        onExportClick = { },
        onClose = onClose,
        emptyMessage = "No stock records found."
    ) { item ->
        val (txt, bg) = stockStatusColors(item.statusBadge)
        ReportRecordCard(
            title = item.name,
            code = item.sku,
            badge = ReportBadge(item.statusBadge, txt, bg),
            summaryLabel = "Total Valuation",
            gridRows = listOf(
                listOf(
                    ReportGridCell("Category", item.category),
                    ReportGridCell("Type", if (item.isFabric) "Fabric" else "Standard Item"),
                    ReportGridCell("UOM", item.unit)
                ),
                listOf(
                    ReportGridCell("On Hand", item.onHandQuantity.fmtQty()),
                    ReportGridCell("Reserved", item.reservedQuantity.fmtQty()),
                    ReportGridCell("Available", item.availableQuantity.fmtQty())
                )
            )
        ) {
            SummaryValue(viewModel.formatCurrency(item.stockValuation))
        }
    }
}

// =============================================================================
// 2. LOW STOCK & REORDER REPORT
// =============================================================================

@Composable
fun LowStockReorderReportScreen(
    onClose: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val paging by viewModel.lowStockPagingState.collectAsState()
    val items by viewModel.filteredLowStockList.collectAsState()
    val query by viewModel.lowStockSearchQuery.collectAsState()
    val summary by viewModel.lowStockSummary.collectAsState()

    var sections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Low Stock", "Low Stock"),
                        FilterOption("Out of Stock", "Out of Stock")
                    )
                )
            )
        )
    }

    LaunchedEffect(Unit) { viewModel.loadLowStockReorderReport() }

    val metrics = listOf(
        ReportMetricSpec(
            label = "LOW STOCK SKUS",
            value = "${summary?.totalLowStockItems ?: 0}",
            subtitle = "${summary?.totalShortageUnits?.fmtQty() ?: "0"} units deficit",
            icon = Icons.Default.Warning,
            valueColor = orangeText,
            iconTint = yellowText,
            iconBgColor = yellowBg
        ),
        ReportMetricSpec(
            label = "COMPLETELY OUT OF STOCK",
            value = "${summary?.outOfStockItems ?: 0}",
            subtitle = "Urgent replenishment",
            icon = Icons.Default.Block,
            valueColor = redText,
            subtitleColor = redText,
            iconTint = redText,
            iconBgColor = redBg
        ),
        ReportMetricSpec(
            label = "AUTOMATED RULES ACTIVE",
            value = "${summary?.autoRulesCoveredCount ?: 0}",
            subtitle = "${summary?.pendingApprovalCount ?: 0} pending PO approvals",
            icon = Icons.Default.Bolt,
            valueColor = Primary,
            subtitleColor = Primary,
            iconTint = Primary,
            iconBgColor = primary_light
        ),
        ReportMetricSpec(
            label = "EST. REORDER COST",
            value = viewModel.formatCurrency(summary?.totalEstimatedCost ?: 0.0),
            subtitle = "Procurement budget needed",
            icon = Icons.Default.CurrencyRupee,
            valueColor = greentext,
            iconTint = greentext,
            iconBgColor = greenBg
        )
    )

    ReportInfinitelyPaginatedScaffold(
        title = "Low Stock & Reorder Report",
        sectionTitle = "Reorder Procurement Schedule",
        searchQuery = query,
        onSearchQueryChange = viewModel::onLowStockSearchQueryChange,
        searchPlaceholder = "Search by name, SKU or supplier...",
        metrics = metrics,
        pagingState = paging,
        filteredItems = items,
        itemKey = { it.id },
        filterSections = sections,
        activeFilterCount = sections.activeCount(),
        onApplyFilters = { applied ->
            sections = applied
            viewModel.applyLowStockFilters(applied.selectedOf("Status"))
        },
        onClearAllFilters = {
            sections = sections.cleared()
            viewModel.clearLowStockFilters()
        },
        onLoadMore = { viewModel.loadLowStockReorderReport(isLoadMore = true) },
        onShareClick = { },
        onExportClick = { },
        onClose = onClose,
        emptyMessage = "No low stock items found."
    ) { item ->
        val (txt, bg) = stockStatusColors(item.statusBadge)
        ReportRecordCard(
            title = item.name,
            code = item.sku,
            badge = ReportBadge(item.statusBadge, txt, bg),
            summaryLabel = "Est. Cost",
            gridRows = listOf(
                listOf(
                    ReportGridCell("Stock Available", item.availableQuantity.fmtQty()),
                    ReportGridCell("Threshold", item.reorderLevel.fmtQty()),
                    ReportGridCell("UOM", item.unit)
                ),
                listOf(
                    // Highlighted values: suggested order in primary, automation rule in orange
                    ReportGridCell("Suggested Order", "${item.suggestedOrderQty.fmtQty()} ${item.unit}", Primary),
                    ReportGridCell("Preferred Supplier", item.preferredSupplier),
                    ReportGridCell("Automation Rule", item.automationRule, orangeText)
                )
            )
        ) {
            SummaryValue(viewModel.formatCurrency(item.estimatedCost))
        }
    }
}

// =============================================================================
// 3. STOCK MOVEMENT REPORT
// =============================================================================

@Composable
fun StockMovementReportScreen(
    onClose: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val paging by viewModel.stockMovementPagingState.collectAsState()
    val items by viewModel.filteredStockMovementList.collectAsState()
    val query by viewModel.stockMovementSearchQuery.collectAsState()
    val summary by viewModel.stockMovementSummary.collectAsState()

    var sections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Movement Type",
                    options = listOf(
                        FilterOption("Purchase", "Purchase"),
                        FilterOption("Sale", "Sale"),
                        FilterOption("Adjustment", "Adjustment"),
                        FilterOption("Consumption", "Consumption"),
                        FilterOption("Transfer", "Transfer")
                    )
                ),
                // Values must stay lowercase: the ViewModel compares against "inward" / "outward"
                FilterSection(
                    title = "Direction",
                    options = listOf(
                        FilterOption("inward", "Inward"),
                        FilterOption("outward", "Outward")
                    )
                )
            )
        )
    }

    LaunchedEffect(Unit) { viewModel.loadStockMovementReport() }

    val metrics = listOf(
        ReportMetricSpec(
            label = "TOTAL MOVEMENTS",
            value = "${summary?.totalMovementsCount ?: 0}",
            subtitle = "Ledger transitions",
            icon = Icons.Default.SwapHoriz,
            iconTint = purpleText,
            iconBgColor = purpleBg
        ),
        ReportMetricSpec(
            label = "TOTAL INWARD QTY",
            value = (summary?.totalInwardQty ?: 0.0).fmtSigned(),
            subtitle = "Received into inventory",
            icon = Icons.Default.SouthWest,
            valueColor = greentext,
            iconTint = greentext,
            iconBgColor = greenBg
        ),
        ReportMetricSpec(
            label = "TOTAL OUTWARD QTY",
            // Outward total may already be negative; force a minus sign for display
            value = "-${kotlin.math.abs(summary?.totalOutwardQty ?: 0.0).fmtQty()}",
            subtitle = "Sold & consumed",
            icon = Icons.Default.NorthEast,
            valueColor = redText,
            iconTint = redText,
            iconBgColor = redBg
        ),
        ReportMetricSpec(
            label = "MANUAL ADJUSTMENTS",
            value = "${summary?.adjustmentsCount ?: 0}",
            subtitle = "Physical corrections",
            icon = Icons.Default.Tune,
            valueColor = orangeText,
            iconTint = orangeText,
            iconBgColor = orangeBg
        )
    )

    ReportInfinitelyPaginatedScaffold(
        title = "Stock Movement Report",
        sectionTitle = "Stock Movement Ledger",
        searchQuery = query,
        onSearchQueryChange = viewModel::onStockMovementSearchQueryChange,
        searchPlaceholder = "Search by item, SKU or warehouse...",
        metrics = metrics,
        pagingState = paging,
        filteredItems = items,
        itemKey = { it.id },
        filterSections = sections,
        activeFilterCount = sections.activeCount(),
        onApplyFilters = { applied ->
            sections = applied
            viewModel.applyStockMovementFilters(
                movementTypes = applied.selectedOf("Movement Type"),
                directions = applied.selectedOf("Direction")
            )
        },
        onClearAllFilters = {
            sections = sections.cleared()
            viewModel.clearStockMovementFilters()
        },
        onLoadMore = { viewModel.loadStockMovementReport(isLoadMore = true) },
        onShareClick = { },
        onExportClick = { },
        onClose = onClose,
        emptyMessage = "No stock movements found."
    ) { item ->
        val (txt, bg) = movementTypeColors(item.movementType)
        // Inward movements are green, outward movements are red
        val qtyColor = if (item.isPositive) greentext else redText
        ReportRecordCard(
            title = item.itemName,
            code = item.sku,
            badge = ReportBadge(item.movementType.uppercase(), txt, bg),
            summaryLabel = "Quantity & UOM",
            gridRows = listOf(
                listOf(
                    ReportGridCell("Date / Time", viewModel.formatDate(item.createdAt)),
                    ReportGridCell("Warehouse", item.warehouse)
                ),
                listOf(
                    ReportGridCell("Bin / Rack", item.bin),
                    ReportGridCell("Logged By", item.handledBy)
                ),
                listOf(
                    ReportGridCell("Reason / Notes", item.reason)
                )
            )
        ) {
            SummaryValue(
                text = "${item.quantity} ${item.unit}",
                color = qtyColor
            )
        }
    }
}

// =============================================================================
// 4. MATERIAL CONSUMPTION REPORT
// =============================================================================

@Composable
fun MaterialConsumptionReportScreen(
    onClose: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val paging by viewModel.materialConsumptionPagingState.collectAsState()
    val items by viewModel.filteredMaterialConsumptionList.collectAsState()
    val query by viewModel.materialConsumptionSearchQuery.collectAsState()
    val summary by viewModel.materialConsumptionSummary.collectAsState()

    var sections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Usage Stream",
                    options = listOf(
                        FilterOption("PRODUCTION CONSUMPTION", "Production Consumption"),
                        FilterOption("JOB CARD", "Job Card"),
                        FilterOption("OVERHEAD", "Overhead")
                    )
                )
            )
        )
    }

    LaunchedEffect(Unit) { viewModel.loadMaterialConsumptionReport() }

    val metrics = listOf(
        ReportMetricSpec(
            label = "CONSUMPTION EVENTS",
            value = "${summary?.totalConsumptionEvents ?: 0}",
            subtitle = "Job card & overhead cuts",
            icon = Icons.Default.MonitorHeart,
            iconTint = Primary,
            iconBgColor = primary_light
        ),
        ReportMetricSpec(
            label = "TOTAL CONSUMED UNITS",
            value = (summary?.totalUnitsConsumed ?: 0.0).fmtQty(),
            subtitle = "Meters & components drawn",
            icon = Icons.Default.ContentCut,
            valueColor = purpleText,
            iconTint = purpleText,
            iconBgColor = purpleBg
        ),
        ReportMetricSpec(
            label = "CONSUMED VALUATION",
            value = viewModel.formatCurrency(summary?.totalConsumptionValuation ?: 0.0),
            subtitle = "Material cost absorbed",
            icon = Icons.Default.AttachMoney,
            valueColor = greentext,
            iconTint = greentext,
            iconBgColor = greenBg
        ),
        ReportMetricSpec(
            label = "FABRIC DRAW EVENTS",
            value = "${summary?.fabricEventsCount ?: 0}",
            subtitle = "Bespoke fabric issues",
            icon = Icons.Default.Description,
            valueColor = orangeText,
            iconTint = orangeText,
            iconBgColor = orangeBg
        )
    )

    ReportInfinitelyPaginatedScaffold(
        title = "Material Consumption Report",
        sectionTitle = "Material Consumption Registry",
        searchQuery = query,
        onSearchQueryChange = viewModel::onMaterialConsumptionSearchQueryChange,
        searchPlaceholder = "Search by item, SKU or staff...",
        metrics = metrics,
        pagingState = paging,
        filteredItems = items,
        itemKey = { it.id },
        filterSections = sections,
        activeFilterCount = sections.activeCount(),
        onApplyFilters = { applied ->
            sections = applied
            viewModel.applyMaterialConsumptionFilters(applied.selectedOf("Usage Stream"))
        },
        onClearAllFilters = {
            sections = sections.cleared()
            viewModel.clearMaterialConsumptionFilters()
        },
        onLoadMore = { viewModel.loadMaterialConsumptionReport(isLoadMore = true) },
        onShareClick = { },
        onExportClick = { },
        onClose = onClose,
        emptyMessage = "No consumption records found."
    ) { item ->
        ReportRecordCard(
            title = item.itemName,
            code = item.sku,
            // This card has no status badge in the design
            badge = null,
            summaryLabel = "Consumed Qty",
            gridRows = listOf(
                listOf(
                    ReportGridCell("Date / Time", viewModel.formatDate(item.createdAt)),
                    ReportGridCell("Material Classification", item.materialClassification)
                ),
                listOf(
                    ReportGridCell("Location", item.warehouse)
                ),
                listOf(
                    ReportGridCell("Craftsman / Staff", item.handledBy),
                    ReportGridCell("Usage Stream", item.usageStream)
                )
            )
        ) {
            // Trailing slot: "1 Pieces | Rs 900" with a thin divider between the two values
            val tokens = LocalAppTokens.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                SummaryValue("${item.consumedQuantity.fmtQty()} ${item.unit}")
                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                Box(
                    modifier = Modifier
                        .width(tokens.extraPadding * 0.1f)
                        .height(tokens.iconSize * 0.7f)
                        .background(dividerColor)
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                SummaryValue(viewModel.formatCurrency(item.valuation), greentext)
            }
        }
    }
}

// =============================================================================
// 5. INVENTORY VALUATION REPORT
// =============================================================================

@Composable
fun InventoryValuationReportScreen(
    onClose: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val paging by viewModel.inventoryValuationPagingState.collectAsState()
    val items by viewModel.filteredInventoryValuationList.collectAsState()
    val query by viewModel.inventoryValuationSearchQuery.collectAsState()
    val summary by viewModel.inventoryValuationSummary.collectAsState()

    var sections by remember {
        mutableStateOf(
            listOf(
                // Values must stay lowercase: the ViewModel compares against these exact keys
                FilterSection(
                    title = "Margin",
                    options = listOf(
                        FilterOption("positive", "Positive"),
                        FilterOption("negative", "Negative"),
                        FilterOption("breakeven", "Breakeven")
                    )
                )
            )
        )
    }

    LaunchedEffect(Unit) { viewModel.loadInventoryValuationReport() }

    val metrics = listOf(
        ReportMetricSpec(
            label = "ASSET COST VALUE",
            value = viewModel.formatCurrency(summary?.totalAssetCostValue ?: 0.0),
            subtitle = "Total cost investment",
            icon = Icons.Default.Link,
            valueColor = Primary,
            iconTint = Primary,
            iconBgColor = primary_light
        ),
        ReportMetricSpec(
            label = "EXPECTED RETAIL REVENUE",
            value = viewModel.formatCurrency(summary?.totalRetailValue ?: 0.0),
            subtitle = "Total realizable sales",
            icon = Icons.Default.AttachMoney,
            valueColor = greentext,
            iconTint = greentext,
            iconBgColor = greenBg
        ),
        ReportMetricSpec(
            label = "POTENTIAL GROSS PROFIT",
            value = viewModel.formatCurrency(summary?.totalPotentialProfit ?: 0.0),
            subtitle = "Forecasted profit gain",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            valueColor = purpleText,
            iconTint = purpleText,
            iconBgColor = purpleBg
        ),
        ReportMetricSpec(
            label = "OVERALL GROSS MARGIN",
            value = summary?.overallMargin ?: "0%",
            subtitle = "Across all stock units",
            icon = Icons.Default.Schedule,
            iconTint = orangeText,
            iconBgColor = orangeBg
        )
    )

    ReportInfinitelyPaginatedScaffold(
        title = "Inventory Valuation Report",
        sectionTitle = "Stock Valuation Ledger",
        searchQuery = query,
        onSearchQueryChange = viewModel::onInventoryValuationSearchQueryChange,
        searchPlaceholder = "Search by name, SKU or category...",
        metrics = metrics,
        pagingState = paging,
        filteredItems = items,
        itemKey = { it.id },
        filterSections = sections,
        activeFilterCount = sections.activeCount(),
        onApplyFilters = { applied ->
            sections = applied
            viewModel.applyInventoryValuationFilters(applied.selectedOf("Margin"))
        },
        onClearAllFilters = {
            sections = sections.cleared()
            viewModel.clearInventoryValuationFilters()
        },
        onLoadMore = { viewModel.loadInventoryValuationReport(isLoadMore = true) },
        onShareClick = { },
        onExportClick = { },
        onClose = onClose,
        emptyMessage = "No valuation records found."
    ) { item ->
        // Margin is green when profitable, red when negative, neutral at break-even
        val marginColor = when {
            item.potentialProfit > 0.0 -> greentext
            item.potentialProfit < 0.0 -> redText
            else -> TextPrimary
        }
        ReportRecordCard(
            title = item.name,
            code = item.sku,
            // Category is shown as the green chip in the design
            badge = ReportBadge(item.category, greentext, greenBg),
            summaryLabel = "Retail Price (\u20B9)",
            gridRows = listOf(
                listOf(
                    ReportGridCell("Asset Cost Value", viewModel.formatCurrency(item.totalCostValue), Primary),
                    ReportGridCell("Expected Retail Value", viewModel.formatCurrency(item.totalRetailValue), greentext),
                    ReportGridCell("Est. Margin", item.marginPercent, marginColor)
                ),
                listOf(
                    ReportGridCell("Stock Units", item.currentStock.fmtQty()),
                    ReportGridCell("Category", item.category),
                    ReportGridCell("Cost (\u20B9)", viewModel.formatCurrency(item.costPrice))
                )
            )
        ) {
            SummaryValue(viewModel.formatCurrency(item.sellingPrice))
        }
    }
}