package com.cuso.mobile.view.home.sales.ordermanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.OrderManagementItem
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardBadge
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.sales.sales_order.orderStatusColors
import com.cuso.mobile.view.home.sales.sales_order.paymentStatusColors
import com.cuso.mobile.viewmodel.OrderManagementUiState
import com.cuso.mobile.viewmodel.OrderManagementViewModel

// ─────────────────────────────────────────────────────────────
// Order Management — real API (OrderManagementViewModel), separate from SalesOrderScreen.kt
// ─────────────────────────────────────────────────────────────
@Composable
fun OrderManagementScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {}
) {
    val viewModel: OrderManagementViewModel = hiltViewModel()   // ✅ CHANGED
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    val itemsPerPage = 10
    var showStatusDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(page, statusFilter, searchQuery) {
        viewModel.fetchOrderManagement(   // ✅ CHANGED
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
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

            // ── Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Order Management", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "close",
                        modifier = Modifier.size(22.dp).clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                }
            }

            // ── Breadcrumb + Search + Filter ──
            Column(
                modifier = Modifier
                    .background(Color(0xFFF8F9FF))
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sales", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                    Text("Orders Management", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; page = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Search Customers...", fontSize = 14.sp, color = Color.Black)
                                inner()
                            }
                        )
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                                .clickable { showStatusDropdown = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.FilterList, "Filter", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            statusOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(label, color = Color(0xFF111827))
                                            if (value == statusFilter) Icon(Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    onClick = { statusFilter = value; page = 1; showStatusDropdown = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Content ──
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF3B3BF9))
                        }
                    }
                    orderState is OrderManagementUiState.Error -> {   // ✅ CHANGED
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
                                ) { Text("Retry", color = Color.White) }
                            }
                        }
                    }
                    orderState is OrderManagementUiState.Success -> {   // ✅ CHANGED
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
                                        OrderManagementCard(   // ✅ CHANGED — extracted, uses flat OrderManagementItem
                                            order = order,
                                            onView = { onViewOrder(order.id) },
                                            onEdit = { onEditOrder(order.id) }
                                        )
                                    }
                                }

                                // ── Pagination footer ──
                                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
                                    Column {
                                        HorizontalDivider(color = Color(0xFFF0F0F0))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val from = if (total == 0) 0 else (page - 1) * itemsPerPage + 1
                                            val to = minOf(page * itemsPerPage, total)
                                            Text("Showing $from - $to of $total", fontSize = 13.sp, color = Color(0xFF6B7280))
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                IconButton(onClick = { if (page > 1) page-- }, enabled = page > 1, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.ChevronLeft, "Previous", tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB))
                                                }
                                                Text("$page - $totalPages", fontSize = 13.sp, color = Color(0xFF374151))
                                                IconButton(onClick = { if (page < totalPages) page++ }, enabled = page < totalPages, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.ChevronRight, "Next", tint = if (page < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB))
                                                }
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

        Button(
            onClick = { onCreateOrder() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Create Order", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
    val (orderBg, orderTextColor) = orderStatusColors(order.orderStatus)             // ✅ CHANGED — order.status -> order.orderStatus
    val (paymentBg, paymentTextColor) = paymentStatusColors(order.paymentStatus)      // ✅ CHANGED — always non-null string now

    DataCard(
        item = order,
        dateText = "Order ID: ${order.orderNumber}",
        dateIcon = Icons.Default.Receipt,
        topBadgeText = order.orderStatus.replaceFirstChar { it.uppercase() },
        topBadgeTextColor = orderTextColor,
        topBadgeBgColor = orderTextColor.copy(alpha = 0.14f),
        topBadgeInline = false,
        title = order.customerName?.takeIf { it.isNotBlank() } ?: "Unknown",
        subtitle = "+91 ${order.mobile?.takeLast(10) ?: "—"}",
        footerFields = listOf(
            DataCardField(icon = Icons.Default.Checkroom, text = order.garments.ifBlank { "—" }),
            DataCardField(icon = Icons.Default.CalendarMonth, text = "Delivery: ${formatIsoDate(order.deliveryDate)}"),
            DataCardField(
                asRow = true,
                label = order.paymentStatus.replaceFirstChar { it.uppercase() },
                labelColor = paymentTextColor,
                labelBackgroundColor = paymentTextColor,   // ✅ badge/pill background back-a
                text = "Total: ₹${formatIndianNumber((order.totalAmount ?: 0.0).toInt())}\n₹${formatIndianNumber(order.balanceAmount.toInt())} Due",
                textColor = Color(0xFF111827)
            )
        ),
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onView() },
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        )
    )
}

// ── ISO date "2026-07-02T00:00:00.000Z" -> "02 Jul 2026" ──
// order.deliveryDate ஒரு ISO string (Long timestamp இல்ல), அதனால sales_order package-ல
// இருக்கிற Long.toDisplayDate() extension இங்க வேலை செய்யாது — தனி formatter
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