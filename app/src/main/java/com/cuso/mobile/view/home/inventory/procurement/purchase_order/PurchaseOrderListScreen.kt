package com.cuso.mobile.view.home.inventory.procurement.purchase_order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.PurchaseOrder
import com.cuso.mobile.model.inventory.PurchaseRequisition
import com.cuso.mobile.model.inventory.WarehouseRef
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun POListScreen(
    viewModel: InventoryViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (PurchaseOrder) -> Unit,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val requisitions by viewModel.requisitionsList.collectAsState()
    val orders by viewModel.purchaseOrdersList.collectAsState()
    val isLoadingRequisitions by viewModel.isLoadingRequisitions.collectAsState()
    val isLoadingPO by viewModel.isLoadingPurchaseOrders.collectAsState()
    val isLoading = isLoadingRequisitions || isLoadingPO

    var searchQuery by remember { mutableStateOf("") }

    val filterDrawerState = rememberFilterDrawerState()

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

    // Fetch live requisitions and purchase orders on load
    LaunchedEffect(Unit) {
        viewModel.fetchAllRequisitions()
        viewModel.fetchAllPurchaseOrders()
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create Order",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TitleBar(
                title = "All orders",
                onClose = onClose
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.fetchAllRequisitions(search = it.takeIf { s -> s.isNotBlank() })
                    viewModel.fetchAllPurchaseOrders(search = it.takeIf { s -> s.isNotBlank() })
                },
                placeholder = "Search Purchase Orders...",
                showFilterIcon = true,
                onFilterClick = {
                    filterDrawerState.open()
                }
            )

            if (isLoading && requisitions.isEmpty() && orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = tokens.buttonHeight + 80.dp
                    )
                ) {
                    if (requisitions.isNotEmpty()) {
                        items(requisitions) { req ->
                            RequisitionCard(
                                requisition = req,
                                onViewDetails = {
                                    val mappedPo = PurchaseOrder(
                                        id = req.id,
                                        poNumber = req.prNumber,
                                        orderStatus = req.approvalStatus ?: "Draft",
                                        poType = req.priority ?: "Standard",
                                        poDate = req.createdAt,
                                        eta = req.requiredByDate,
                                        subtotal = req.estimatedSubtotal ?: 0.0,
                                        taxTotal = req.estimatedTax ?: 0.0,
                                        grandTotal = req.estimatedTotal ?: 0.0
                                    )
                                    onNavigateToDetail(mappedPo)
                                }
                            )
                        }
                    } else {
                        items(orders) { order ->
                            PurchaseOrderCard(
                                order = order,
                                onViewDetails = { onNavigateToDetail(order) }
                            )
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
                viewModel.fetchAllRequisitions(
                    search = searchQuery.takeIf { it.isNotBlank() }
                )
            },
            onClearAll = {
                filterSections = initialFilterSections
                viewModel.fetchAllRequisitions(
                    search = searchQuery.takeIf { it.isNotBlank() }
                )
            }
        )
    }
}

@Composable
fun RequisitionCard(
    requisition: PurchaseRequisition,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = requisition.prNumber ?: "PR-000",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusPill(status = requisition.approvalStatus ?: "Draft")
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
                    Text(text = "DEPARTMENT", fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Text(
                        text = requisition.department ?: "Production",
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
                        text = requisition.priority ?: "High",
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
                        text = requisition.warehouseDisplayName,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "REQUIRED BY", fontSize = tokens.label, color = TextSecondary)
                    Text(text = requisition.requiredByDate?.substringBefore("T") ?: "N/A", fontSize = tokens.bodySmall, color = TextPrimary)
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
                    Text(text = "Estimated Total", fontSize = tokens.label, color = mutedText)
                    Text(
                        text = "₹${(requisition.estimatedTotal ?: 0.0).toInt()}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Row(
                    modifier = Modifier.clickable { onViewDetails() },
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
fun PurchaseOrderCard(
    order: PurchaseOrder,
    onViewDetails: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
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
                StatusPill(status = order.orderStatus ?: "OPEN")
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
                        text = "Sri Lakshmi textiles",
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
                        text = order.poType ?: "Standard",
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
                        text = (order.warehouseId as? WarehouseRef)?.name ?: "Central Store",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "ORDER DATE", fontSize = tokens.label, color = TextSecondary)
                    Text(text = order.poDate?.substringBefore("T") ?: "Jan 24, 2026", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "EXPECTED", fontSize = tokens.label, color = TextSecondary)
                    Text(text = order.eta?.substringBefore("T") ?: "Feb 1, 2026", fontSize = tokens.bodySmall, color = TextPrimary)
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
                        text = "₹${(order.grandTotal ?: 0.0).toInt()}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Row(
                    modifier = Modifier.clickable { onViewDetails() },
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
    val (bg, txtColor) = when (status.lowercase()) {
        "completed", "received", "approved", "open" -> greenBg to greentext
        "pending", "pending approval", "not received", "unpaid", "draft" -> orangeBg to orangeText
        "in transit" -> primary_light to BluePrimary
        else -> greenBg to greentext
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
            text = status,
            color = txtColor,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold
        )
    }
}