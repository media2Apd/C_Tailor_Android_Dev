@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable"
)
package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redtext
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowtext
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.reusablecomposables.DataCardImage
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.OrderActionState
import com.cuso.mobile.viewmodel.OrderUiState
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────
@Suppress("UNUSED_PARAMETER")

@Composable
fun SalesOrderScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onViewOrder: (String) -> Unit = {},
    onEditOrder: (String) -> Unit = {}
) {
    val viewModel: SalesOrderViewModel = hiltViewModel()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    var itemsPerPage by remember { mutableIntStateOf(10) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(page, itemsPerPage, statusFilter, searchQuery) {
        viewModel.fetchOrders(
            page = page,
            limit = itemsPerPage,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    LaunchedEffect(orderState) {
        if (orderState is OrderUiState.Error) {
            coroutineScope.launch { snackbarHostState.showSnackbar((orderState as OrderUiState.Error).message) }
        }
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is OrderActionState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(s.message) }
                viewModel.resetActionState()
            }
            is OrderActionState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(s.message) }
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    val isLoading = orderState is OrderUiState.Loading
    val orders = (orderState as? OrderUiState.Success)?.orders ?: emptyList()
    val total = (orderState as? OrderUiState.Success)?.total ?: 0
    val totalPages = (orderState as? OrderUiState.Success)?.totalPages ?: 1

    val statusOptions = listOf(
        "all" to "All Statuses",
        "pending" to "Pending",
        "confirmed" to "Confirmed",
        "processing" to "Processing",
        "completed" to "Completed",
        "cancelled" to "Cancelled"
    )

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
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

            // ── FIXED TOP HEADER ──
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sales Orders", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "close",
                        modifier = Modifier.size(22.dp).clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                }
            }

            // ── Breadcrumb + Search + Status filter ──
            Column(modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF8F9FF))
            ) {
                ScreenBreadcrumb(segments = listOf("Sales", "Sales Orders"), onClick = {})
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = "Search Customers...",
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
                    orderState is OrderUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    (orderState as OrderUiState.Error).message,
                                    color = Color.Red, textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchOrders(
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
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    items(orders) { order ->
                                        val (_, statusTextColor) = orderStatusColors(order.status)
                                        val garmentNames = order.garments.joinToString(", ") { it.categoryName }.ifEmpty { "—" }
                                        DataCard(
                                            item = order,
                                            image = DataCardImage(
                                                vector = Icons.Default.Person,
                                                size = 50.dp,
                                                backgroundColor = BorderGray,
                                                tint = Color(0xFF9CA3AF)
                                            ),
                                            topBadgeText = order.status?.replaceFirstChar { it.uppercase() } ?: "—",
                                            topBadgeTextColor = statusTextColor,
                                            topBadgeBgColor = statusTextColor.copy(alpha = 0.14f),
                                            topBadgeInline = true,
                                            title = order.customerId?.name ?: "Unknown",
                                            subtitle = order.orderNumber,
                                            footerAsRows = true,
                                            footerFields = listOf(
                                                DataCardField(label = "Items", text = garmentNames),
                                                DataCardField(label = "Price", text = order.totalAmount?.let { "₹$it" } ?: "—"),
                                                DataCardField(label = "Date Of Delivery", text = order.deliveryDate.toDisplayDate()),
                                                DataCardField(label = "Priority", text = "—")
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                                MenuAction("Edit", Icons.Default.Edit) { onEditOrder(order.id) }
                                            )
                                        )
                                    }
                                }

                                // ── Pagination Footer ──
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
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

fun orderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "confirmed"  -> greenBg to greentext
    "pending"    -> yellowBg to yellowtext
    "processing" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
    "completed"  -> greenBg to greentext
    "cancelled"  -> redBg to redtext
    else         -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
}

fun paymentStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid"    -> greenBg to greentext
    "partial" -> yellowBg to yellowtext
    "unpaid"  -> redBg to redtext
    else      -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
}
fun Long?.toDisplayDate(): String {
    if (this == null) return "—"
    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
}