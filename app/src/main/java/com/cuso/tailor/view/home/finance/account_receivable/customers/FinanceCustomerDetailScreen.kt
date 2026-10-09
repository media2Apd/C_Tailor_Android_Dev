package com.cuso.tailor.view.home.finance.account_receivable.customers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.CirculerProgressIndicatorSmall
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CustomerDetailViewScreen(
    customerId: String,
    onClose: () -> Unit,
    customerData: CustomerItemV2? = null,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val customerListResponse by viewModel.financeCustomerList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFinanceCustomers.collectAsStateWithLifecycle()

    val customer = customerData ?: customerListResponse?.data?.find { it._id == customerId }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Screen திறக்கும் போது இந்த customer-ன் orders-ஐ fetch செய்கிறோம்
    LaunchedEffect(customerId) {
        if (customerId.isNotBlank()) {
            viewModel.fetchCustomerOrders(customerId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCustomerOrders()
        }
    }

    val tabs = listOf(
        "Overview" to Icons.Default.Dashboard,
        "Purchase Orders" to Icons.Outlined.ShoppingCart,
        "Transactions" to Icons.AutoMirrored.Filled.ReceiptLong,
        "Preferences" to Icons.Outlined.Tune,
        "Notes & Tags" to Icons.Outlined.Label
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFC))
    ) {
        TitleBar(title = "All Customers", onClose = onClose)
        HorizontalDivider(color = dividerColor)

        if (customer == null && isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CirculerProgressIndicatorSmall()
            }
            return
        }

        if (customer == null) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Customer details not found",
                    color = TextSecondary,
                    fontSize = tokens.bodyMedium
                )
            }
            return
        }

        // Header Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Text(
                text = customer.name,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )

            Spacer(Modifier.height(tokens.extraPadding / 2))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = customer.mobile.ifBlank { "N/A" },
                        fontSize = tokens.bodySmall,
                        color = close_color
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatCustomerDate(customer.createdAt),
                        fontSize = tokens.bodySmall,
                        color = close_color
                    )
                }
            }
        }

        HorizontalDivider(color = dividerColor)

        // Scrollable Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Primary else Color.Transparent,
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { selectedTabIndex = index }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.second,
                            contentDescription = tab.first,
                            tint = if (isSelected) Color.White else iconMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.first,
                            color = if (isSelected) Color.White else title_color,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = dividerColor)

        // Tab Body Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> CustomerOverviewTab(customer = customer)
                1 -> CustomerPurchaseOrdersTab(viewModel = viewModel)
                2 -> CustomerTransactionsTab()
                3 -> CustomerPreferencesTab(customer = customer)
                4 -> CustomerNotesAndTagsTab(customer = customer)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. PURCHASE ORDERS TAB (Live API Data)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerPurchaseOrdersTab(viewModel: FinanceViewModel) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCustomerOrders.collectAsStateWithLifecycle()
    val errorMsg by viewModel.customerOrdersError.collectAsStateWithLifecycle()

    // Real-time Metrics Calculation from API
    val totalOrdersCount = orders.size
    val openOrdersCount = orders.count { !it.status.equals("Delivered", true) && !it.status.equals("Cancelled", true) }
    val completedCount = orders.count { it.status.equals("Delivered", true) || it.status.equals("Completed", true) }
    val totalInvoicedValue = orders.sumOf { it.grandTotal ?: 0.0 }

    // Search Filtering
    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) orders
        else {
            orders.filter {
                (it.orderCode ?: "").contains(searchQuery, ignoreCase = true) ||
                        it.items.any { item -> (item.itemDescription ?: "").contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    if (isLoading && orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CirculerProgressIndicatorSmall()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // --- 4 Metrics Summary Cards Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL ORDERS",
                value = "$totalOrdersCount",
                subtitle = "All time customer orders",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "OPEN ORDERS",
                value = "$openOrdersCount",
                subtitle = "Awaiting dispatch/bill",
                icon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                    )
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "COMPLETED",
                value = "$completedCount",
                subtitle = "Successfully settled",
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            SummaryMetricCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL VALUE",
                value = "₹${formatIndianNumber(totalInvoicedValue)}",
                subtitle = "Net invoiced (YTD)",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Paid,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // --- Search & Filter Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = {
                    Text(
                        text = "Search SO number, customer...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = whiteBg,
                    focusedContainerColor = whiteBg,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Primary
                ),
                singleLine = true
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .size(48.dp)
                    .clickable { /* Filter Action */ }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = title_color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // --- Header Section ---
        Text(
            text = "ORDERS HISTORY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B82F6),
            letterSpacing = 0.5.sp
        )

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (errorMsg != null) errorMsg!! else "No orders found for this customer",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            filteredOrders.forEach { order ->
                CustomerOrderCardItem(order = order)
            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// Order Card Connected to API DTO
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerOrderCardItem(order: CustomerOrderItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Code, Date & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderCode ?: "ORD-N/A",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatCustomerDate(order.orderDate ?: order.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Dynamic Status Badge
                val statusText = (order.status ?: "Pending").replace("_", " ")
                val isCompleted = statusText.contains("Delivered", true) || statusText.contains("Completed", true)
                val badgeBg = if (isCompleted) Color(0xFFE6F4EA) else Color(0xFFEFF6FF)
                val badgeText = if (isCompleted) Color(0xFF137333) else Color(0xFF1D4ED8)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeText.copy(alpha = 0.2f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeText)
                        )
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeText
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(10.dp))

            // Items Summary text
            val itemsSummary = remember(order.items) {
                val count = order.items.size
                val names = order.items.mapNotNull { it.itemDescription }.take(3).joinToString(", ")
                if (count > 0) "$count items: $names" else "No items specified"
            }

            Text(
                text = itemsSummary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Spacer(Modifier.height(12.dp))

            // Order Value & Balance Due Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ORDER VALUE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "ORDER VALUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "₹${formatIndianNumber(order.grandTotal ?: 0.0)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                    }
                }

                // BALANCE DUE
                val balanceDue = order.balanceAmount ?: 0.0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "BALANCE DUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(2.dp))
                        val isZero = balanceDue == 0.0
                        Text(
                            text = "₹${formatIndianNumber(balanceDue)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isZero) Color(0xFF10B981) else Color(0xFF2563EB)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // View Details Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Navigate to Order Detail */ },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Summary Metric Card Component
// ─────────────────────────────────────────────────────────────
@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                icon()
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = title_color
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. OVERVIEW TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerOverviewTab(customer: CustomerItemV2) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .border(width = 1.dp, color = dividerColor)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Outstanding Receivables",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹ ${formatIndianNumber(customer.outstanding ?: 0.0)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(tokens.fieldHeight)
                    .background(sectionBorder)
            )

            Spacer(Modifier.width(tokens.screenPadding))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unused Credits",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹ 0",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Customer Information",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color,
                modifier = Modifier.padding(horizontal = tokens.screenPadding)
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgLight)
                .padding(horizontal = tokens.screenPadding)
        ) {
            DetailInfoRow(label = "Name", value = customer.name)
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Phone", value = customer.mobile.ifBlank { "N/A" })
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(
                label = "Email",
                value = customer.email?.takeIf { it.isNotBlank() } ?: "N/A"
            )
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(
                label = "Type",
                value = customer.type.replaceFirstChar { it.uppercase() }
            )
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(
                label = "Status",
                value = customer.status?.takeIf { it.isNotBlank() } ?: "Active"
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Addresses",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color,
                modifier = Modifier.padding(horizontal = tokens.screenPadding)
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgLight)
                .padding(horizontal = tokens.screenPadding)
        ) {
            val billingAddressText = formatFullAddress(customer.billingAddress ?: customer.address)
            val shippingAddressText = formatFullAddress(customer.address ?: customer.billingAddress)

            AddressInfoRow(label = "Billing Address", address = billingAddressText)
            HorizontalDivider(color = dividerColor)

            AddressInfoRow(label = "Shipping Address", address = shippingAddressText)
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// 3. TRANSACTIONS TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerTransactionsTab() {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Transaction History",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color,
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        )

        HorizontalDivider(color = dividerColor)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.cardHeight * 0.75f)
                        .background(transactionSheetBg, RoundedCornerShape(tokens.cardCornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_transaction_sheet),
                        contentDescription = "Transactions",
                        tint = transactionSheetTint,
                        modifier = Modifier.size(tokens.iconSize * 2.4f)
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "No Transactions Found",
                    fontSize = tokens.bodyMedium,
                    color = close_color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 4. PREFERENCES TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerPreferencesTab(customer: CustomerItemV2) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "CUSTOMER PREFERENCES & TERMS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Customer Level",
                        value = "Regular",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Preferred Contact Method",
                        value = "Whatsapp",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Language",
                        value = "English",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Credit Limit",
                        value = "₹0",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Credit Period",
                        value = "0 Days",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Account Status",
                        value = customer.status?.takeIf { it.isNotBlank() } ?: "Active",
                        valueColor = Color(0xFF137333),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

@Composable
private fun PreferenceColumnItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = title_color
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 5. NOTES & TAGS TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerNotesAndTagsTab(customer: CustomerItemV2) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding)
    ) {
        Text(
            text = "Customer Notes & Tags",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Manage internal remarks, bespoke logs, and segmentation.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {

        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// Helper Row Components & Formatters
// ─────────────────────────────────────────────────────────────
private fun formatFullAddress(addr: CustomerAddressV2?): String {
    if (addr == null) return "N/A"
    val parts = linkedSetOf<String>()
    addr.flatNo?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.street?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.areaZone?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.addressLine?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.area?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.city?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.state?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.pincode?.takeIf { it.isNotBlank() }?.let { parts.add("PIN: ${it.trim()}") }

    return if (parts.isEmpty()) "N/A" else parts.joinToString(", ")
}

private fun formatCustomerDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "N/A"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(raw.take(10)) ?: return raw.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding)
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = iconMuted,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            color = textSubdued,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AddressInfoRow(label: String, address: String) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding)
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = iconMuted,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = close_color,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(tokens.iconSize)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = address,
                fontSize = tokens.bodySmall,
                color = textSubdued,
                fontWeight = FontWeight.Medium,
                lineHeight = tokens.bodyMedium.value.dp.value.let { tokens.bodySmall * 1.35f }
            )
        }
    }
}