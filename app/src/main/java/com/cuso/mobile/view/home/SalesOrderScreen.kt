package com.cuso.mobile.view.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.model.OrderItem
import com.cuso.mobile.viewmodel.OrderActionState
import com.cuso.mobile.viewmodel.OrderUiState
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun SalesOrderScreen(
    navController: NavController,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateOrder: () -> Unit = {}
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
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }

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
            coroutineScope.launch {
                snackbarHostState.showSnackbar((orderState as OrderUiState.Error).message)
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {

            // ── Top Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onBack() },
                        tint = Color(0xFF111827)
                    )
                    Text(
                        "Sales Orders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (total > 0) {
                        Surface(
                            color = Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "$total orders",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF3B3BF9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Button(
                        onClick = { onCreateOrder() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3BF9)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Create Order", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // ── Filter Bar ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; page = 1 },
                            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
                            cursorBrush = SolidColor(Color(0xFF3B3BF9)),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search orders…", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // Status filter dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .clickable { showStatusDropdown = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                        Text(
                            statusOptions.find { it.first == statusFilter }?.second ?: "All",
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
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
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(label, color = Color(0xFF111827))
                                        if (value == statusFilter) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = { statusFilter = value; page = 1; showStatusDropdown = false }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── Content ──────────────────────────────────────────
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B3BF9))
                    }
                }

                orderState is OrderUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                (orderState as OrderUiState.Error).message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
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
                        Column {
                            SalesOrderTable(
                                orders = orders,
                                onStatusChange = { orderId, status ->
                                    viewModel.updateOrderStatus(
                                        orderId = orderId,
                                        status = status,
                                        currentPage = page,
                                        limit = itemsPerPage,
                                        search = searchQuery.takeIf { it.isNotBlank() },
                                        statusFilter = statusFilter.takeIf { it != "all" }
                                    )
                                }
                            )

                            // ── Pagination ──────────────────────────────
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val from = (page - 1) * itemsPerPage + 1
                                    val to = minOf(page * itemsPerPage, total)
                                    Text("Showing $from - $to of $total", fontSize = 13.sp, color = Color(0xFF6B7280))

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                                .clickable { showItemsPerPageDropdown = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("$itemsPerPage per page", fontSize = 13.sp, color = Color(0xFF374151))
                                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showItemsPerPageDropdown,
                                            onDismissRequest = { showItemsPerPageDropdown = false },
                                            containerColor = Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            listOf(10, 25, 50, 100).forEach { count ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                            Text("$count per page", color = Color(0xFF111827))
                                                            if (count == itemsPerPage) Icon(Icons.Default.Check, null, tint = Color(0xFF3B3BF9), modifier = Modifier.size(16.dp))
                                                        }
                                                    },
                                                    onClick = { itemsPerPage = count; page = 1; showItemsPerPageDropdown = false }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Page buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (page > 1) page-- },
                                        enabled = page > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous",
                                            tint = if (page > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                    val startPage = maxOf(1, page - 2)
                                    val endPage = minOf(totalPages, page + 2)
                                    for (p in startPage..endPage) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (p == page) Color(0xFF3B3BF9) else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { page = p },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                p.toString(),
                                                fontSize = 13.sp,
                                                color = if (p == page) Color.White else Color(0xFF6B7280),
                                                fontWeight = if (p == page) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { if (page < totalPages) page++ },
                                        enabled = page < totalPages,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next",
                                            tint = if (page < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Table
// ─────────────────────────────────────────────────────────────

@Composable
fun SalesOrderTable(
    orders: List<OrderItem>,
    onStatusChange: (String, String) -> Unit = { _, _ -> }
) {
    val horizontalScrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()

    // Column widths matching screenshot
    val checkWidth   = 40.dp
    val orderIdWidth = 110.dp
    val statusWidth  = 120.dp
    val customerWidth= 150.dp
    val priorityWidth= 90.dp
    val garmentWidth = 120.dp
    val paymentWidth = 110.dp
    val dateWidth    = 110.dp
    val deliveryWidth= 110.dp
    val totalWidth   = 90.dp
    val actionWidth  = 70.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
    ) {
        // ── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .background(Color(0xFFF9FAFB))
                .border(BorderStroke(1.dp, Color(0xFFE5E7EB)))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox placeholder
            Box(modifier = Modifier.width(checkWidth), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.dp, Color(0xFF9CA3AF), RoundedCornerShape(3.dp))
                )
            }
            SalesHeaderCell("Order Id",    orderIdWidth,  Color(0xFF3B3BF9))
            SalesHeaderCell("Status",      statusWidth)
            SalesHeaderCell("Customer",    customerWidth, Color(0xFF3B3BF9))
            SalesHeaderCell("Priority",    priorityWidth, Color(0xFF3B3BF9))
            SalesHeaderCell("Garments",    garmentWidth,  Color(0xFF3B3BF9))
            SalesHeaderCell("Payment",     paymentWidth,  Color(0xFF3B3BF9))
            SalesHeaderCell("Order Date",  dateWidth,     Color(0xFF3B3BF9))
            SalesHeaderCell("Delivery",    deliveryWidth, Color(0xFF3B3BF9))
            SalesHeaderCell("Total",       totalWidth,    Color(0xFF3B3BF9))
            SalesHeaderCell("Action",      actionWidth)
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))

        // ── Rows ─────────────────────────────────────────────────
        LazyColumn(state = lazyListState, modifier = Modifier.heightIn(max = 600.dp)) {
            items(orders, key = { it.id }) { order ->
                SalesOrderRow(
                    order = order,
                    checkWidth    = checkWidth,
                    orderIdWidth  = orderIdWidth,
                    statusWidth   = statusWidth,
                    customerWidth = customerWidth,
                    priorityWidth = priorityWidth,
                    garmentWidth  = garmentWidth,
                    paymentWidth  = paymentWidth,
                    dateWidth     = dateWidth,
                    deliveryWidth = deliveryWidth,
                    totalWidth    = totalWidth,
                    actionWidth   = actionWidth,
                    onStatusChange = { newStatus -> onStatusChange(order.id, newStatus) }
                )
                HorizontalDivider(color = Color(0xFFF3F4F6))
            }
        }
    }
}

@Composable
private fun SalesHeaderCell(
    text: String,
    width: Dp,
    color: Color = Color(0xFF6B7280)
) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

// ─────────────────────────────────────────────────────────────
// Row
// ─────────────────────────────────────────────────────────────

@Composable
fun SalesOrderRow(
    order: OrderItem,
    checkWidth: Dp,
    orderIdWidth: Dp,
    statusWidth: Dp,
    customerWidth: Dp,
    priorityWidth: Dp,
    garmentWidth: Dp,
    paymentWidth: Dp,
    dateWidth: Dp,
    deliveryWidth: Dp,
    totalWidth: Dp,
    actionWidth: Dp,
    onStatusChange: (String) -> Unit = {}
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }

    // Format epoch millis → dd/MM/yyyy
    fun Long?.toDisplayDate(): String {
        if (this == null) return "—"
        return runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
        }.getOrDefault("—")
    }

    // Garments: join all categoryName values
    val garmentNames = remember(order.garments) {
        order.garments.joinToString(", ") { it.categoryName }.ifEmpty { "—" }
    }

    // Total
    val totalText = order.totalAmount?.let { "₹${formatAmount(it)}" } ?: "—"

    // Status chip colors
    val (statusBg, statusTextColor) = orderStatusColors(order.status)

    // Payment chip
    val paymentStatus = order.paymentStatus ?: "unpaid"
    val (paymentBg, paymentTextColor) = paymentStatusColors(paymentStatus)

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Checkbox ──
        Box(modifier = Modifier.width(checkWidth), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(1.dp, if (checked) Color(0xFF3B3BF9) else Color(0xFF9CA3AF), RoundedCornerShape(3.dp))
                    .background(if (checked) Color(0xFF3B3BF9) else Color.White, RoundedCornerShape(3.dp))
                    .clickable { checked = !checked },
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }

        // ── Order Id ──
        Text(
            text = order.orderNumber,
            modifier = Modifier.width(orderIdWidth),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF3B3BF9),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Status chip ──
        Box(modifier = Modifier.width(statusWidth)) {
            Box(
                modifier = Modifier
                    .background(statusBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    order.status?.replaceFirstChar { it.uppercase() } ?: "—",
                    fontSize = 12.sp,
                    color = statusTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Customer ──
        Column(modifier = Modifier.width(customerWidth)) {
            Text(
                text = order.customerId?.name ?: "Unknown",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = order.customerId?.mobile ?: "",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }

        // ── Priority (source field used as priority) ──
        Text(
            text = order.source?.replaceFirstChar { it.uppercase() } ?: "Normal",
            modifier = Modifier.width(priorityWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151)
        )

        // ── Garments ──
        Text(
            text = garmentNames,
            modifier = Modifier.width(garmentWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ── Payment chip ──
        Box(modifier = Modifier.width(paymentWidth)) {
            Box(
                modifier = Modifier
                    .background(paymentBg, RoundedCornerShape(20.dp))
                    .border(1.dp, paymentTextColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(paymentTextColor, RoundedCornerShape(3.dp))
                    )
                    Text(
                        paymentStatus,
                        fontSize = 12.sp,
                        color = paymentTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Order Date ──
        Text(
            text = order.orderDate.toDisplayDate(),
            modifier = Modifier.width(dateWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151)
        )

        // ── Delivery Date ──
        Text(
            text = order.deliveryDate.toDisplayDate(),
            modifier = Modifier.width(deliveryWidth),
            fontSize = 13.sp,
            color = Color(0xFF374151)
        )

        // ── Total ──
        Text(
            text = totalText,
            modifier = Modifier.width(totalWidth),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        // ── Action ··· ──
        Box(modifier = Modifier.width(actionWidth), contentAlignment = Alignment.Center) {
            Box {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "Actions",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { actionMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = actionMenuExpanded,
                    onDismissRequest = { actionMenuExpanded = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Visibility, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                Text("View", color = Color(0xFF111827))
                            }
                        },
                        onClick = { actionMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                Text("Edit", color = Color(0xFF111827))
                            }
                        },
                        onClick = { actionMenuExpanded = false }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

fun formatAmount(amount: Int): String = when {
    amount >= 100_000 -> String.format("%.1fL", amount / 100_000.0)
    amount >= 1_000   -> String.format("%.1fK", amount / 1_000.0)
    else              -> amount.toString()
}

fun orderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "confirmed"  -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "pending"    -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
    "processing" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
    "completed"  -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
    "cancelled"  -> Color(0xFFFFEBEE) to Color(0xFFF44336)
    else         -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
}

fun paymentStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "paid"         -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "partial"      -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
    "unpaid"       -> Color(0xFFFFF8E6) to Color(0xFFD97706)
    else           -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
}