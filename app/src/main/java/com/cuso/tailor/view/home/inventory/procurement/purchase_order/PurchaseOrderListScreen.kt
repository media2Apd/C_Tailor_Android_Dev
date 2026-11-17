@file:Suppress("SpellCheckingInspection", "unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.purchase_order

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.SupplierBillInfo
import com.cuso.tailor.model.inventory.WarehouseRef
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextLog
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.currencySymbol
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

private const val TAG = "PO_LIST"

@Composable
fun POListScreen(
    viewModel: InventoryViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (PurchaseOrder) -> Unit,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val poPagingState by viewModel.purchaseOrdersPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.purchaseOrdersError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchAllPurchaseOrders()
        } else {
            delay(400)
            val query = searchQuery.trim().ifBlank { null }
            viewModel.fetchAllPurchaseOrders(search = query)
        }
    }

    val initialFilterSections = remember {
        listOf(
            FilterSection(
                title = "Status",
                type = FilterSectionType.CHECKBOX_LIST,
                icon = Icons.Filled.Sell,
                isMultiSelect = true,
                options = listOf(
                    FilterOption(id = "open", label = "Open"),
                    FilterOption(id = "pending", label = "Pending"),
                    FilterOption(id = "received", label = "Received"),
                    FilterOption(id = "completed", label = "Completed")
                )
            ),
            FilterSection(
                title = "Date Range",
                type = FilterSectionType.CHIP_GRID,
                icon = Icons.Filled.DateRange,
                options = listOf(
                    FilterOption(id = "today", label = "Today"),
                    FilterOption(id = "this_week", label = "This Week"),
                    FilterOption(id = "this_month", label = "This Month"),
                    FilterOption(id = "all_time", label = "All Time", isSelected = true)
                )
            ),
            FilterSection(
                title = "PO Type",
                type = FilterSectionType.CHIP_ROW,
                icon = Icons.Filled.ShoppingBag,
                options = listOf(
                    FilterOption(id = "standard", label = "Standard"),
                    FilterOption(id = "urgent", label = "Urgent"),
                    FilterOption(id = "dropship", label = "Dropship")
                )
            ),
            FilterSection(
                title = "Amount Range",
                type = FilterSectionType.AMOUNT_RANGE,
                icon = Icons.Filled.CurrencyRupee,
                options = emptyList()
            )
        )
    }

    var filterSections by remember { mutableStateOf(initialFilterSections) }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create Order",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        ),
        fabVisible = !poPagingState.isInitialLoading
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Purchase Orders",
                onClose = onClose
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Purchase Orders...",
                showFilterIcon = true,
                onFilterClick = { filterDrawerState.open() }
            )

            when {
                errorMessage != null && poPagingState.items.isEmpty() && !poPagingState.isInitialLoading -> {
                    AppErrorState(
                        title = "Failed to load purchase orders",
                        message = errorMessage ?: "Something went wrong. Please check your connection.",
                        onRetry = {
                            val query = searchQuery.trim().ifBlank { null }
                            viewModel.fetchAllPurchaseOrders(search = query)
                        }
                    )
                }

                !poPagingState.isInitialLoading && poPagingState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No purchase orders found" else "No matching purchase orders found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                }

                else -> {
                    PaginatedDataList(
                        state = poPagingState,
                        onLoadMore = { viewModel.loadMorePurchaseOrders() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tokens.screenPadding)
                    ) { order ->
                        Column {
                            PurchaseOrderCard(
                                order = order,
                                onViewDetails = {
                                    Log.d(TAG, "PurchaseOrder clicked -> id: '${order.id}', poNumber: '${order.poNumber}'")
                                    onNavigateToDetail(order)
                                }
                            )
                            Spacer(modifier = Modifier.height(tokens.extraPadding))
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Orders",
            sections = filterSections,
            onApply = { appliedSections ->
                filterSections = appliedSections
                val query = searchQuery.takeIf { it.isNotBlank() }
                viewModel.fetchAllPurchaseOrders(search = query)
            },
            onClearAll = {
                filterSections = initialFilterSections
                val query = searchQuery.takeIf { it.isNotBlank() }
                viewModel.fetchAllPurchaseOrders(search = query)
            }
        )
    }
}

@Composable
fun PurchaseOrderCard(
    order: PurchaseOrder,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val warehouseDisplayName = when (val wh = order.warehouseId) {
        is WarehouseRef -> wh.name
        is Map<*, *> -> wh["name"]?.toString() ?: "Central Store"
        is String -> wh.ifBlank { "Central Store" }
        else -> "Central Store"
    }

    val supplierDisplayName = when (val sup = order.supplierId) {
        is SupplierBillInfo -> sup.name
        is Map<*, *> -> sup["name"]?.toString() ?: "Supplier"
        is String -> sup.ifBlank { "Supplier" }
        else -> "Supplier"
    }

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
    ) {
        Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.poNumber ?: "PO-000",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusPill(status = order.orderStatus)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = iconMuted,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "SUPPLIER", fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        text = supplierDisplayName,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(badgeGrey, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.poType.ifBlank { "Standard" },
                        fontSize = tokens.label,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = "WAREHOUSE", fontSize = tokens.label, color = TextSecondary)
                    Text(
                        text = warehouseDisplayName,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "ORDER DATE", fontSize = tokens.label, color = TextSecondary)
                    Text(text = order.poDate?.substringBefore("T") ?: "-", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "EXPECTED", fontSize = tokens.label, color = TextSecondary)
                    Text(text = order.eta?.substringBefore("T") ?: "-", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Order Total", fontSize = tokens.label, color = mutedText)
                    Text(
                        text = "${currencySymbol()}${order.grandTotal.toInt()}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Details",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val tokens = LocalAppTokens.current
    val cleanStatus = status.ifBlank { "" }
    val (bg, txtColor) = when (cleanStatus.lowercase()) {
        "completed", "received", "approved", "open" -> greenBg to greentext
        "pending", "pending approval", "not received", "unpaid", "draft" -> yellowBg to yellowText
        "in transit" -> light_blue_border to blueText
        "rejected", "cancelled" -> redBg to redText
        else -> light_grey to TextLog
    }

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(txtColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = cleanStatus,
            color = txtColor,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold
        )
    }
}