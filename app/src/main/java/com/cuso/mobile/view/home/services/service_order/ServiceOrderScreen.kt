@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused"
)
package com.cuso.mobile.view.home.services.service_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
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
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_border

@Composable
fun ServiceOrderScreen(
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
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Infinite scroll listener
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

    // Debounced search and status filter
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
                successMessage = s.message.ifBlank { "Service order updated successfully" }
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
//        fab = FabConfig(
//            label = "Create Service Order",
//            icon = Icons.Default.Add,
//            onClick = { onCreateOrder() },
//            bottomPadding = 50.dp
//        )
        fab=null,
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar("Service Orders", onClose = onBack)
                }

                Column(modifier = Modifier.fillMaxWidth()) {

                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Customers...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = { /* Open filter drawer if needed */ }
                    )
                }
                HorizontalDivider(color = title_border)

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
                                        Text("No service orders found", color = Color.Gray, fontSize = 15.sp)
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxWidth().weight(1f)
                                    ) {
                                        items(orders, key = { it.id }) { order ->
                                            val garmentNames = order.garments.joinToString(", ") {
                                                "${it.categoryName} (${it.quantity})"
                                            }.ifEmpty { "—" }

                                            val (statusBg, statusText) = when (order.status?.lowercase()) {
                                                "completed" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                                                "pending" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                                                "cancelled" -> Color(0xFFFEE2E2) to redText
                                                else -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                                            }

                                            val priorityColor = redText

                                            DataCard(
                                                item = order,
                                                eyebrowText = "Order ID: ${order.orderNumber}",
                                                eyebrowColor = Color(0xFF6B7280),
                                                topBadgeText = "Active",
                                                topBadgeBgColor = Color(0xFFEDE9FE),
                                                topBadgeTextColor = Color(0xFF4338CA),
                                                topBadgeShowDot = false,
                                                topBadgeInline = false,
                                                showActionsInHeader = true,
                                                title = order.customerId?.name ?: "Unknown",
                                                titleFontWeight = FontWeight.SemiBold,
                                                titleColor = Color(0xFF111827),
                                                actions = listOf(
                                                    MenuAction("View", Icons.Default.Visibility) { onViewOrder(order.id) },
                                                    MenuAction("Edit", Icons.Default.Edit) { onEditOrder(order.id) }
                                                ),
                                                onClick = { onViewOrder(order.id) },
                                                content = {
                                                    // Item Details (Tag Icon)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 2.dp)
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ic_tag),
                                                            contentDescription = null,
                                                            tint = Color(0xFF9CA3AF),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            text = garmentNames,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF6B7280),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Spacer(Modifier.height(6.dp))

                                                    // Delivery Date (Calendar Icon)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.ic_calendar),
                                                            contentDescription = null,
                                                            tint = Color(0xFF9CA3AF),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            text = "Delivery: ${order.deliveryDate.toDisplayDate()}",
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF6B7280)
                                                        )
                                                    }

                                                    Spacer(Modifier.height(10.dp))
                                                    HorizontalDivider(color = light_grey, thickness = 1.dp)
                                                    Spacer(Modifier.height(10.dp))

                                                    // Bottom Split: Priority & Status (Left) | Total & Paid (Right)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.Bottom
                                                    ) {
                                                        // Left Column: Priority + Status Pill
                                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(8.dp)
                                                                        .clip(CircleShape)
                                                                        .background(priorityColor)
                                                                )
                                                                Spacer(Modifier.width(6.dp))
                                                                Text(
                                                                    text = "High Priority",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = Color(0xFF374151)
                                                                )
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(20.dp))
                                                                    .background(statusBg)
                                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                                            ) {
                                                                Text(
                                                                    text = order.status?.replaceFirstChar { it.uppercase() } ?: "Completed",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = statusText
                                                                )
                                                            }
                                                        }

                                                        // Right Column: Total & Paid amount
                                                        Column(
                                                            horizontalAlignment = Alignment.End,
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "Total: ₹${order.totalAmount ?: 0}",
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF6B7280)
                                                            )
                                                            Row(verticalAlignment = Alignment.Bottom) {
                                                                Text(
                                                                    text = "₹${order.totalPaid ?: order.totalAmount ?: 0}",
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF111827)
                                                                )
                                                                Spacer(Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Paid",
                                                                    fontSize = 13.sp,
                                                                    color = Color(0xFF9CA3AF),
                                                                    modifier = Modifier.padding(bottom = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
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
            }

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

//private fun formatServiceGarmentsSummary(garments: String): String {
//    if (garments.isBlank()) return "—"
//    val names = garments.split(",").map { it.trim().substringBefore("(").trim() }
//    return when {
//        names.isEmpty() -> "—"
//        names.size == 1 -> names[0]
//        else -> "${names[0]}, ${names.size - 1} more"
//    }
//}
//
//private fun serviceOrderStatusColors(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
//    "confirmed"  -> greenBg to greentext
//    "pending"    -> yellowBg to yellowText
//    "processing" -> Color(0xFFF3E5F5) to Color(0xFF9C27B0)
//    "completed"  -> greenBg to greentext
//    "cancelled"  -> redBg to redText
//    else         -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
//}

private fun Long?.toDisplayDate(): String {
    if (this == null) return "—"
    return runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this)) }.getOrDefault("—")
}