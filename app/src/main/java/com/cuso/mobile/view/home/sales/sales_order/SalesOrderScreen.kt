@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused"
)
package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowText
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.OrderActionState
import com.cuso.mobile.viewmodel.OrderUiState
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.home.formatIndianNumber


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

    val isLoading = orderState is OrderUiState.Loading
    val orders = (orderState as? OrderUiState.Success)?.orders ?: emptyList()

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



                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Customers...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                HorizontalDivider(color = dividerColor)

                // Dynamic Orders Content List
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        isLoading -> {
                            ListSkeleton()
                        }
                        orderState is OrderUiState.Error -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = (orderState as OrderUiState.Error).message,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.fetchOrders(
                                                page = 1,
                                                limit = 10,
                                                search = searchQuery.takeIf { it.isNotBlank() },
                                                status = statusFilter.takeIf { it != "all" }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Retry", color = whiteBg)
                                    }
                                }
                            }
                        }
                        orderState is OrderUiState.Success -> {
                            if (orders.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Receipt, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("No orders found", color = Color.Gray, fontSize = 15.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(orders, key = { it.id }) { order ->
                                        val status = order.status?.replaceFirstChar { it.uppercase() } ?: "Processing"
                                        val (statusBg, statusTextColor) = orderStatusColors(status)

                                        val garmentCategory = order.garments.firstOrNull()?.categoryName ?: "Garments"
                                        val totalQuantity = order.garments.sumOf { it.quantity }.let { if (it <= 0) 1 else it }
                                        val subtitleSummary = "$garmentCategory  •  Qty $totalQuantity"

                                        val paymentTag =  "Prepaid"
                                        val branchTag = "BLR-S"

                                        // Image matching Order Card Item
                                        DataCard(
                                            item = order,
                                            eyebrowText = "ORD-${order.orderNumber}",
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
                                                    text = "₹${formatIndianNumber(order.totalAmount ?: 0.0)}",
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

private fun formatGarmentsSummary(garments: String): String {
    if (garments.isBlank()) return "—"
    val names = garments.split(",").map { it.trim().substringBefore("(").trim() }
    return when {
        names.isEmpty() -> "—"
        names.size == 1 -> names[0]
        else -> "${names[0]}, ${names.size - 1} more"
    }
}

// -------------------------------------------------------------
// Helpers
// -------------------------------------------------------------

fun orderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "confirmed"  -> greenBg to greentext
    "pending"    -> yellowBg to yellowText
    "processing" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
    "completed"  -> greenBg to greentext
    "cancelled"  -> redBg to redText
    else         -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
}

fun paymentStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid"    -> greenBg to greentext
    "partial" -> yellowBg to yellowText
    "unpaid"  -> redBg to redText
    else      -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
}

fun Long?.toDisplayDate(): String {
    if (this == null) return "—"
    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
}