@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead"
)
package com.cuso.mobile.view.home.sales.ordermanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.cuso.mobile.model.sales.OrderManagementItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.home.sales.sales_order.orderStatusColors
import com.cuso.mobile.view.home.sales.sales_order.paymentStatusColors
import com.cuso.mobile.viewmodel.OrderManagementUiState
import com.cuso.mobile.viewmodel.OrderManagementViewModel

// ─────────────────────────────────────────────────────────────
// Order Management — real API (OrderManagementViewModel), separate from SalesOrderScreen.kt
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
fun OrderManagementScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit ={}

) {
    val viewModel: OrderManagementViewModel = hiltViewModel()   //   CHANGED
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10
    var showStatusDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(page, statusFilter, searchQuery) {
        viewModel.fetchOrderManagement(   //   CHANGED
            page = page,
            limit = itemsPerPage,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    val isLoading = orderState is OrderManagementUiState.Loading
    val orders = (orderState as? OrderManagementUiState.Success)?.orders ?: emptyList()
    val total = (orderState as? OrderManagementUiState.Success)?.total ?: 0
    val totalPages = (orderState as? OrderManagementUiState.Success)?.totalPages ?: 1

    val statusOptions = listOf(
        "all" to "All Statuses",
        "pending" to "Pending",
        "confirmed" to "Confirmed",
        "processing" to "Processing",
        "completed" to "Completed",
        "draft" to "Draft",
        "cancelled" to "Cancelled"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

            // ── Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar("Orders Management", onClose = onBack)

            }

            // ── Breadcrumb + Search + Filter ──
            Column(modifier = Modifier.fillMaxWidth())
            {
                ScreenBreadcrumb(segments = listOf("Sales", "Orders Management"), onClick = {onBreadCrumbClick()})
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = "Search Orders...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { /* TODO: open filter drawer */ }
                )
            }

            // ── Content ──
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> {
                        ListSkeleton()
                    }
                    orderState is OrderManagementUiState.Error -> {   //   CHANGED
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    (orderState as OrderManagementUiState.Error).message,
                                    color = Color.Red, textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchOrderManagement(
                                            page = page, limit = itemsPerPage,
                                            search = searchQuery.takeIf { it.isNotBlank() },
                                            status = statusFilter.takeIf { it != "all" }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Retry", color = whiteBg) }
                            }
                        }
                    }
                    orderState is OrderManagementUiState.Success -> {   //   CHANGED
                        if (orders.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Receipt, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No orders found", color = Color.Gray, fontSize = 15.sp)
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    items(orders) { order ->
                                        OrderManagementCard(   //   CHANGED — extracted, uses flat OrderManagementItem
                                            order = order,
                                            onView = { onViewOrder(order.id) },
                                            onEdit = { onEditOrder(order.id) }
                                        )
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

// ── Individual order card — uses flat OrderManagementItem fields ──
@Composable
private fun OrderManagementCard(
    order: OrderManagementItem,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    val (orderBgColor, orderTextColor) = orderStatusColors(order.orderStatus)
    val (paymentBgColor, paymentTextColor) = paymentStatusColors(order.paymentStatus)

    DataCard(
        item = order,
        dateText = "Order ID: ${order.orderNumber}",
        topBadgeText = order.orderStatus.replaceFirstChar { it.uppercase() },
        topBadgeTextColor = orderTextColor,
        topBadgeBgColor = orderBgColor,
        topBadgeInline = false,
        title = order.customerName?.takeIf { it.isNotBlank() } ?: "Unknown",
        subtitle = "+91 ${order.mobile?.takeLast(10) ?: "—"}",
        footerFields = listOf(
            DataCardField(text = order.garments.ifBlank { "—" }),
            DataCardField( text = "Delivery: ${formatIsoDate(order.deliveryDate)}"),
            DataCardField(
                asRow = true,
                label = order.paymentStatus.replaceFirstChar { it.uppercase() },
                labelColor = paymentTextColor,
                labelBackgroundColor = paymentBgColor,
                text = "Total: ₹${formatIndianNumber((order.totalAmount ?: 0.0).toInt())}\n₹${formatIndianNumber(order.balanceAmount.toInt())} Due",
                textColor = blackTitle
            )
        ),
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onView() },
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        )
    )
}

// ── ISO date "2026-07-02T00:00:00.000Z" -> "02 Jul 2026" ──

private fun formatIsoDate(iso: String): String {
    if (iso.isBlank()) return "—"
    return try {
        val datePart = iso.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val month = months.getOrNull(parts[1].toInt() - 1) ?: parts[1]
            "${parts[2]} $month ${parts[0]}"
        } else iso
    } catch (_: Exception) { iso }
}