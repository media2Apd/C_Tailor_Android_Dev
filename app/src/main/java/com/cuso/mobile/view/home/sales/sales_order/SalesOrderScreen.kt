//REFERENCE

@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable"
)
package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.MenuAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
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
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.OrderActionState
import com.cuso.mobile.viewmodel.OrderUiState
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar

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
    onEditOrder: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val viewModel: SalesOrderViewModel = hiltViewModel()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var page by remember { mutableIntStateOf(1) }
    var itemsPerPage by remember { mutableIntStateOf(10) }

    // Dynamic Island State variables
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(page, itemsPerPage, statusFilter, searchQuery) {
        viewModel.fetchOrders(
            page = page,
            limit = itemsPerPage,
            search = searchQuery.takeIf { it.isNotBlank() },
            status = statusFilter.takeIf { it != "all" }
        )
    }

    // Handle initial list load error
    LaunchedEffect(orderState) {
        if (orderState is OrderUiState.Error) {
            errorMessage = ErrorMapper.map((orderState as OrderUiState.Error).message)
        }
    }

    // Handle Order Create / Update / Action state
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is OrderActionState.Success -> {
                // Dynamic Island Success Message
                successMessage = s.message.ifBlank { "Order created successfully" }
                viewModel.resetActionState()
            }
            is OrderActionState.Error -> {
                // Dynamic Island Error Message
                errorMessage = ErrorMapper.map(s.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    val isLoading = orderState is OrderUiState.Loading
    val orders = (orderState as? OrderUiState.Success)?.orders ?: emptyList()
    val total = (orderState as? OrderUiState.Success)?.total ?: 0
    val totalPages = (orderState as? OrderUiState.Success)?.totalPages ?: 1

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

                // ── FIXED TOP HEADER ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TitleBar("Sales orders", onClose = onBack)

                }

                // ── Breadcrumb + Search + Status filter ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ScreenBreadcrumb(segments = listOf("Sales", "Sales Orders"), onClick = { onBreadCrumbClick() })
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        placeholder = "Search Customers...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* open filter drawer */ }
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))

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
                                        color = Color.Red,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.fetchOrders(
                                                page = page,
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
                                                    painter= painterResource(R.drawable.ic_person),
                                                    size = 30.dp,
                                                    backgroundColor = Color.Transparent,
                                                    tint = blackTitle
                                                ),
                                                topBadgeText = order.status?.replaceFirstChar { it.uppercase() } ?: "—",
                                                topBadgeTextColor = statusTextColor,
                                                topBadgeBgColor = statusTextColor.copy(alpha = 0.14f),
                                                topBadgeInline = true,
                                                title = order.customerId?.name ?: "Unknown",
                                                subtitle = "Order ID : ${order.orderNumber}",
                                                footerAsRows = true,
                                                footerFields = listOf(
                                                    DataCardField(label = "Items", text = formatGarmentsSummary(garmentNames), asRow = true),                                                    DataCardField(label = "Price", text = order.totalAmount?.let { "₹$it" } ?: "—", asRow = true),
                                                    DataCardField(label = "Date Of Delivery", text = order.deliveryDate.toDisplayDate(), asRow = true),
                                                    DataCardField(label = "Priority", text = "—", asRow = true)
                                                ),
                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                                    MenuAction("Edit", Icons.Default.Edit) { onEditOrder(order.id) }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Dynamic Island Success Notification ──
            DynamicIslandSuccess(
                modifier = Modifier.align(Alignment.TopCenter),
                message = successMessage,
                onDismiss = { successMessage = null }
            )

            // ── Dynamic Island Error Notification ──
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
        names.size == 1 -> names[0]                              // 1 item → "Pant"
        else -> "${names[0]}, ${names.size - 1} more"             // N items → "Pant, 2 more"
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