@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "VariableNeverRead"
)

package com.cuso.mobile.view.home.services.service_status.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.OrderManagementItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.StatusBadge
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.sales.sales_order.orderStatusColors
import com.cuso.mobile.view.home.sales.sales_order.paymentStatusColors
import com.cuso.mobile.viewmodel.OrderManagementUiState
import com.cuso.mobile.viewmodel.OrderManagementViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ServiceStatusScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val viewModel: OrderManagementViewModel = hiltViewModel()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    val itemsPerPage = 10

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
                    viewModel.loadMoreOrderManagement(limit = itemsPerPage)
                }
            }
    }

    LaunchedEffect(searchQuery, statusFilter) {
        delay(400)
        viewModel.fetchOrderManagement(
            page = 1,
            limit = itemsPerPage,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    val isLoading = orderState is OrderManagementUiState.Loading
    val orders = (orderState as? OrderManagementUiState.Success)?.orders ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar("Service Status", onClose = onBack)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Service Status...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )
            }

            HorizontalDivider(color = title_border)

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    orderState is OrderManagementUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    (orderState as OrderManagementUiState.Error).message,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchOrderManagement(
                                            page = 1,
                                            limit = itemsPerPage,
                                            search = searchQuery.takeIf { it.isNotBlank() },
                                            status = statusFilter.takeIf { it != "all" }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Retry", color = whiteBg)
                                }
                            }
                        }
                    }

                    orderState is OrderManagementUiState.Success -> {
                        if (orders.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Receipt, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No service records found", color = Color.Gray, fontSize = 15.sp)
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    items(orders, key = { it.id }) { order ->
                                        ServiceStatusCard(
                                            order = order,
                                            onView = { onViewOrder(order.id) },
                                            onEdit = { onEditOrder(order.id) }
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

                    else -> Unit
                }
            }
        }
    }
}

// Backward-compatible alias for existing references
@Composable
fun OrderManagementScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    ServiceStatusScreen(
        navController = navController,
        onMenuClick = onMenuClick,
        onBack = onBack,
        onViewOrder = onViewOrder,
        onEditOrder = onEditOrder,
        onBreadCrumbClick = onBreadCrumbClick
    )
}

@Composable
private fun ServiceStatusCard(
    order: OrderManagementItem,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    val (orderBgColor, orderTextColor) = orderStatusColors(order.orderStatus)
    val (paymentBgColor, paymentTextColor) = paymentStatusColors(order.paymentStatus)

    DataCard(
        item = order,
        showDateIcon = false,
        dateText = "Order ID: ${order.orderNumber}",
        topBadgeText = order.orderStatus.replaceFirstChar { it.uppercase() },
        topBadgeTextColor = orderTextColor,
        topBadgeBgColor = orderBgColor,
        topBadgeInline = false,
        showActionsInHeader = true,
        title = order.customerName?.takeIf { it.isNotBlank() } ?: "Unknown",
        subtitle = "+91 ${order.mobile?.takeLast(10) ?: "—"}",
        footerFields = listOf(
            DataCardField(text = order.garments, textColor = mutedText),
            DataCardField(text = "Delivery: ${formatIsoDate(order.deliveryDate)}", textColor = mutedText)
        ),
        content = {
            val tokens = LocalAppTokens.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                StatusBadge(
                    text = order.paymentStatus.replaceFirstChar { it.uppercase() },
                    bgColor = paymentBgColor,
                    textColor = paymentTextColor
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ₹${formatIndianNumber((order.totalAmount ?: 0.0).toInt())}",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${formatIndianNumber(order.balanceAmount.toInt())}",
                            fontSize = tokens.bodyLarge,
                            color = blackTitle
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Due",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                }
            }
        },
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onView() }
        )
    )
}

private fun formatIsoDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val datePart = iso.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val month = months.getOrNull(parts[1].toInt() - 1) ?: parts[1]
            "${parts[2]} $month ${parts[0]}"
        } else iso
    } catch (_: Exception) {
        iso
    }
}