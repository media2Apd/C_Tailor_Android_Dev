package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardBadge
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.cuso.mobile.view.home.reusablecomposables.DataCardImage
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

@Composable
fun SalesOrderScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onViewOrder: (String) -> Unit = {}   // ✅ CHANGED — OrderItem இல்ல, String (orderId) expect பண்ணணும்
) {
    val viewModel: SalesOrderViewModel = hiltViewModel()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var page by remember { mutableStateOf(1) }
    var itemsPerPage by remember { mutableStateOf(10) }
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

    Box(modifier = Modifier.fillMaxSize()) {
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
//                    if (total > 0) {
//                        Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(20.dp)) {
//                            Text(
//                                "$total orders",
//                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
//                                fontSize = 12.sp,
//                                color = Color(0xFF3B3BF9),
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        }
//                    }
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
            Column(
                modifier = Modifier
                    .background(Color(0xFFF8F9FF))
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sales", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                    Text("Sales Orders", fontSize = 13.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold)
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
                                if (searchQuery.isEmpty()) Text("Search orders...", fontSize = 14.sp, color = Color.Black)
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
                if (statusFilter != "all") {
                    Spacer(Modifier.height(8.dp))
                    Surface(color = Color(0xFFEEF2FF), shape = RoundedCornerShape(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                statusOptions.find { it.first == statusFilter }?.second ?: "",
                                fontSize = 12.sp, color = Color(0xFF3B3BF9), fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close, null, tint = Color(0xFF3B3BF9),
                                modifier = Modifier.size(14.dp).clickable { statusFilter = "all"; page = 1 }
                            )
                        }
                    }
                }
            }

            // ── Content ──
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF3B3BF9)) }
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
                                                vector = Icons.Default.Checkroom,
                                                size = 50.dp,
                                                backgroundColor = Color.Transparent,
                                                tint = Color(0xFF9CA3AF)
                                            ),
                                            badge = DataCardBadge(
                                                text = order.status?.replaceFirstChar { it.uppercase() } ?: "—",
                                                color = statusTextColor
                                            ),
                                            badgeInline = true,                      // ✅ badge next to title, like Customer screen
                                            title = order.customerId?.name ?: "Unknown",
                                            subtitle = order.orderNumber,
                                            footerAsRows = true,                     // ✅ "label   value" rows, like Customer screen
                                            footerFields = listOf(
                                                DataCardField(label = "Items", text = garmentNames),
                                                DataCardField(label = "Price",text = order.totalAmount?.let { "₹$it" } ?: "—" ),
                                                DataCardField(label = "Date Of Delivery",text = order.deliveryDate.toDisplayDate()),
                                                DataCardField(label = "Priority", text= "—")
                                            ),
                                            actions = listOf(
                                                MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                                MenuAction("Edit", Icons.Default.Edit) { /* TODO */ }
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

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))

        // ── FAB Create Order Button ──
        Button(
            onClick = { onCreateOrder() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Create Order", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ... rest of the composables remain the same (SalesOrderTable, SalesOrderRow, SalesOrderCard, helpers) ...

// ─────────────────────────────────────────────────────────────
// Table (top section)
// ─────────────────────────────────────────────────────────────



//private fun Long?.toDisplayDate(): String {
//    if (this == null) return "—"
//    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
//}

// ─────────────────────────────────────────────────────────────
// 🔁 ONE column list for orders — reused by table header, table cells
// and card footer fields.
// ─────────────────────────────────────────────────────────────
//private fun orderColumns(): List<DataColumn<OrderItem>> = listOf(
//    DataColumn("orderId", "Order Id", 110.dp) { order ->
//        Text(order.orderNumber, fontSize = 13.sp, fontWeight = FontWeight.Medium,
//            color = Color(0xFF3B3BF9), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("status", "Status", 120.dp) { order ->
//        val (bg, textColor) = orderStatusColors(order.status)
//        Box(modifier = Modifier.background(bg, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
//            Text(order.status?.replaceFirstChar { it.uppercase() } ?: "—", fontSize = 12.sp,
//                color = textColor, fontWeight = FontWeight.Medium)
//        }
//    },
//    DataColumn("customer", "Customer", 150.dp) { order ->
//        Column {
//            Text(order.customerId?.name ?: "Unknown", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
//            Text(order.customerId?.mobile ?: "", fontSize = 11.sp, color = Color(0xFF6B7280))
//        }
//    },
//    DataColumn("priority", "Priority", 90.dp) { order ->
//        Text(order.source?.replaceFirstChar { it.uppercase() } ?: "Normal", fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("garments", "Garments", 120.dp) { order ->
//        val names = order.garments.joinToString(", ") { it.categoryName }.ifEmpty { "—" }
//        Text(names, fontSize = 13.sp, color = Color(0xFF374151), maxLines = 1, overflow = TextOverflow.Ellipsis)
//    },
//    DataColumn("payment", "Payment", 110.dp) { order ->
//        val paymentStatus = order.paymentStatus ?: "unpaid"
//        val (bg, textColor) = paymentStatusColors(paymentStatus)
//        Box(
//            modifier = Modifier.background(bg, RoundedCornerShape(20.dp))
//                .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
//                .padding(horizontal = 8.dp, vertical = 4.dp)
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                Box(modifier = Modifier.size(6.dp).background(textColor, RoundedCornerShape(3.dp)))
//                Text(paymentStatus, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
//            }
//        }
//    },
//    DataColumn("orderDate", "Order Date", 110.dp) { order ->
//        Text(order.orderDate.toDisplayDate(), fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("delivery", "Delivery", 110.dp) { order ->
//        Text(order.deliveryDate.toDisplayDate(), fontSize = 13.sp, color = Color(0xFF374151))
//    },
//    DataColumn("total", "Total", 90.dp) { order ->
//        Text(order.totalAmount?.let { "₹${it}" } ?: "—", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
//    },
//    DataColumn("action", "Action", 70.dp, cellAlignment = Alignment.Center) {
//        ActionDropdownMenu(
//            actions = listOf(
//                MenuAction("View", Icons.Default.Visibility) {},
//                MenuAction("Edit", Icons.Default.Edit) {}
//            )
//        )
//    }
//)
//
//
//
//// ── Card view — same call site as before ──
//@Composable
//fun SalesOrderCard(order: OrderItem, onStatusChange: (String) -> Unit = {}) {
//    val columns = orderColumns()
//    val statusColumn = columns.first { it.key == "status" }
//    val paymentColumn = columns.first { it.key == "payment" }
//    val (_, statusTextColor) = orderStatusColors(order.status)
//    val footerFields = columns.filter { it.key in listOf("orderDate", "delivery", "total") }
//
//    DataCard(
//        item = order,
//        leading = {
//            Box(modifier = Modifier.width(4.dp).height(20.dp).background(statusTextColor, RoundedCornerShape(2.dp)))
//        },
//        title = {
//            Text(order.orderNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
//        },
//        trailing = {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                statusColumn.cellContent(order)
//                ActionDropdownMenu(
//                    icon = Icons.Default.MoreVert,
//                    actions = listOf(
//                        MenuAction("View", Icons.Default.Visibility) {},
//                        MenuAction("Edit", Icons.Default.Edit) {}
//                    )
//                )
//            }
//        },
//        middleContent = {
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
//                Box(
//                    modifier = Modifier.size(42.dp).background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(order.customerId?.name?.firstOrNull()?.uppercase() ?: "?", fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold, color = Color(0xFF3B3BF9))
//                }
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(order.customerId?.name ?: "Unknown", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
//                        color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
//                    val names = order.garments.joinToString(", ") { it.categoryName }.ifEmpty { "—" }
//                    Text(names, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
//                }
//                paymentColumn.cellContent(order)
//            }
//        },
//        fields = footerFields,
//        fieldsPerRow = 3
//    )
//}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────
// Idha SalesOrderScreen.kt file-la (அல்லது தனி Mappers.kt file-la) add pannunga

fun orderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "confirmed"  -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "pending"    -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
    "processing" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
    "completed"  -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
    "cancelled"  -> Color(0xFFFFEBEE) to Color(0xFFF44336)
    else         -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
}

fun paymentStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid"    -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "partial" -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
    "unpaid"  -> Color(0xFFFFF8E6) to Color(0xFFD97706)
    else      -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
}
fun Long?.toDisplayDate(): String {
    if (this == null) return "—"
    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
}