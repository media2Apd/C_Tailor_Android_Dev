@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused"
)
package com.cuso.tailor.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.ErrorMapper
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
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.OrderActionState
import com.cuso.tailor.viewmodel.OrderUiState
import com.cuso.tailor.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getDefaultOrderFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Order Status",
        options = listOf(
            FilterOption("confirmed", "Confirmed"),
            FilterOption("completed", "Completed"),
            FilterOption("pending", "Pending"),
            FilterOption("cancelled", "Cancelled"),
            FilterOption("in_production", "In Production")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Priority",
        options = listOf(
            FilterOption("high", "High"),
            FilterOption("medium", "Medium"),
            FilterOption("low", "Low")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Payment Status",
        options = listOf(
            FilterOption("paid", "Paid"),
            FilterOption("partial", "Partial"),
            FilterOption("unpaid", "Unpaid")
        ),
        isMultiSelect = true
    )
)

@Composable
fun SalesOrderScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: SalesOrderViewModel = hiltViewModel()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember { mutableStateOf(getDefaultOrderFilterSections()) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore) {
                    viewModel.loadMoreOrders()
                }
            }
    }

    LaunchedEffect(searchQuery, statusFilter) {
        delay(400)
        viewModel.fetchOrders(
            page = 1,
            limit = 10,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    LaunchedEffect(orderState) {
        if (orderState is OrderUiState.Error) {
            errorMessage = ErrorMapper.map((orderState as OrderUiState.Error).message)
        }
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is OrderActionState.Success -> {
                successMessage = s.message.ifBlank { "Order created successfully" }
                viewModel.resetActionState()
            }
            is OrderActionState.Error -> {
                errorMessage = ErrorMapper.map(s.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    // Active filter counter for the badge on the filter button
    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section ->
                section.options.count { it.isSelected }
            }
        }
    }

    val isLoading = orderState is OrderUiState.Loading
    val orders = (orderState as? OrderUiState.Success)?.orders ?: emptyList()

    // Filter orders locally by selected Order Status, Priority, and Payment Status
    val filteredOrders by remember(orders, searchQuery, filterSections) {
        derivedStateOf {
            val selectedStatuses = filterSections.find { it.title == "Order Status" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
            val selectedPriorities = filterSections.find { it.title == "Priority" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()
            val selectedPaymentStatuses = filterSections.find { it.title == "Payment Status" }
                ?.options?.filter { it.isSelected }?.map { it.label } ?: emptyList()

            orders.filter { order ->
                val customerName = order.customerId?.name.orEmpty()
                val orderCode = order.orderNumber

                val matchesSearch = searchQuery.isBlank() ||
                        customerName.contains(searchQuery, ignoreCase = true) ||
                        orderCode.contains(searchQuery, ignoreCase = true)

                val normalizedStatus = order.status?.replace("_", " ") ?: ""
                val matchesStatus = selectedStatuses.isEmpty() ||
                        selectedStatuses.any {
                            it.equals(normalizedStatus, ignoreCase = true) || it.equals(order.status, ignoreCase = true)
                        }

                val matchesPriority = selectedPriorities.isEmpty() ||
                        selectedPriorities.any { it.equals(order.source, ignoreCase = true) }

                val matchesPayment = selectedPaymentStatuses.isEmpty() ||
                        selectedPaymentStatuses.any { it.equals(order.paymentStatus, ignoreCase = true) }

                matchesSearch && matchesStatus && matchesPriority && matchesPayment
            }
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Create Order",
            icon = Icons.Default.Add,
            onClick = { onCreateOrder() },
            bottomPadding = 50.dp
        ),
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

                // Top Bar
                TitleBar("All Orders", onClose = onBack)

                HorizontalDivider(color = dividerColor)

                // Container hosting Content and slide-in Filter Drawer below TitleBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Orders...",
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            filterCount = activeFilterCount,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when {
                                isLoading && filteredOrders.isEmpty() -> {
                                    ListSkeleton()
                                }
                                orderState is OrderUiState.Error && filteredOrders.isEmpty() -> {
                                    AppErrorState(
                                        title = "Failed to load Orders",
                                        message = "Something went wrong. Please check your connection and try again.",
                                        onRetry = { viewModel.fetchOrders() }
                                    )
                                }
                                filteredOrders.isEmpty() -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank() || activeFilterCount > 0) {
                                                    "No orders match your filter criteria"
                                                } else {
                                                    "No orders found"
                                                },
                                                color = Color.Gray,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        items(filteredOrders, key = { it.id }) { order ->
                                            val status = order.status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Processing"
                                            val (statusBg, statusTextColor) = orderStatusColors(status)

                                            val garmentCategory = order.garments.firstOrNull()?.categoryName ?: "Garment"
                                            val totalQuantity = order.garments.sumOf { it.quantity }.toInt().let { if (it <= 0) 1 else it }
                                            val subtitleSummary = "$garmentCategory  •  Qty $totalQuantity"

                                            val paymentTag = order.paymentStatus ?: "Prepaid"
                                            val branchTag = order.branchName ?: "Main Branch"

                                            DataCard(
                                                item = order,
                                                eyebrowText = if (order.orderNumber.startsWith("ORD-", ignoreCase = true)) {
                                                    order.orderNumber
                                                } else {
                                                    "ORD-${order.orderNumber}"
                                                },
                                                showActionsInHeader = true,
                                                eyebrowColor = title_color,
                                                title = order.customerId?.name.orEmpty().ifBlank { "Unknown Customer" },
                                                subtitle = subtitleSummary,
                                                topBadgeText = status,
                                                topBadgeTextColor = statusTextColor,
                                                topBadgeBgColor = statusBg,
                                                footerTags = listOf(paymentTag, branchTag),
                                                footerFields = listOf(
                                                    DataCardField(
                                                        label = "Order Value",
                                                        text = "₹${formatIndianNumber(order.totalAmount)}",
                                                        textColor = Primary,
                                                        valueFontWeight = FontWeight.SemiBold,
                                                        asColumn = true
                                                    )
                                                ),
                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                                    MenuAction("Edit", Icons.Default.Edit) { onEditOrder(order.id) }
                                                ),
                                                onClick = { onViewOrder(order.id) }
                                            )
                                        }

                                        if (isLoadingMore) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CirculerProgressIndicatorSmall()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FilterDrawer overlay rendered strictly below TitleBar
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filter Orders",
                        sections = filterSections,
                        onApply = { updatedSections -> filterSections = updatedSections },
                        onClearAll = {
                            filterSections = filterSections.map { section ->
                                section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
                            }
                        }
                    )
                }
            }

            // Dynamic Island Notifications
            DynamicIslandSuccess(
                modifier = Modifier.align(Alignment.TopCenter),
                message = successMessage,
                onDismiss = { successMessage = null }
            )

            DynamicIslandError(
                modifier = Modifier.align(Alignment.TopCenter),
                message = errorMessage,
                onDismiss = { errorMessage = null }
            )
        }
    }
}

// -------------------------------------------------------------
// Helpers
// -------------------------------------------------------------

fun orderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "confirmed"   -> greenBg to greentext
    "pending"     -> yellowBg to yellowText
    "processing", "in_production", "in production" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
    "completed"   -> greenBg to greentext
    "cancelled"   -> redBg to redText
    else          -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
}

fun paymentStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid"    -> greenBg to greentext
    "partial" -> yellowBg to yellowText
    "unpaid"  -> redBg to redText
    else      -> light_grey to Color(0xFF6B7280)
}

fun Long?.toDisplayDate(): String {
    if (this == null) return "—"
    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
}