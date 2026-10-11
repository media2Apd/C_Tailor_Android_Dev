@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")

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
import androidx.compose.material.icons.automirrored.outlined.Label
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
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
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

    // Fetch live orders for this customer upon opening
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
        "Notes & Tags" to Icons.AutoMirrored.Outlined.Label
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TitleBar(title = "All Customers", onClose = onClose)
        HorizontalDivider(color = dividerColor)

        if (customer == null && isLoading) {
            ListSkeleton()
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
        Spacer(Modifier.padding(top = 10.dp))
        // Header Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Text(
                text = customer.name,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
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
                        text = customer.mobile,
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
        Spacer(Modifier.padding(top = 10.dp))


        // Scrollable Tab Row
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
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = if (isSelected) Primary else Color.Transparent,
                    border = if (isSelected) null else BorderStroke(1.dp, BorderGray),
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
                            tint = if (isSelected) whiteBg else iconMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.first,
                            color = if (isSelected) whiteBg else title_color,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = dividerColor)
        Spacer(Modifier.padding(top = 10.dp))


        // Dynamic Tab Body Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> CustomerOverviewTab(customer = customer)
                1 -> CustomerPurchaseOrdersTab(viewModel = viewModel)
                2 -> CustomerTransactionsTab(viewModel = viewModel)
                3 -> CustomerPreferencesTab(customer = customer)
                4 -> CustomerNotesAndTagsTab(customer = customer)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. OVERVIEW TAB (Connected to live Customer Data)
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
                    text = "₹${formatIndianNumber(customer.outstanding ?: 0.0)}",
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
                    text = "Total Paid",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹${formatIndianNumber(customer.totalPaid ?: 0.0)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = greentext
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
            DetailInfoRow(label = "Customer Code", value = customer.customerCode ?: "N/A")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Name", value = customer.name)
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Phone", value = customer.mobile)
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(
                label = "Email",
                value = customer.email?.takeIf { it.isNotBlank() } ?: "N/A"
            )
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(
                label = "Type",
                value = customer.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
            val shippingAddressText = if (customer.sameAsBillingAddress == true) {
                billingAddressText
            } else {
                formatFullAddress(customer.shippingAddress ?: customer.address)
            }

            AddressInfoRow(label = "Billing Address", address = billingAddressText)
            HorizontalDivider(color = dividerColor)

            AddressInfoRow(label = "Shipping Address", address = shippingAddressText)
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// 2. PURCHASE ORDERS TAB (Live Orders using DataCard & SearchFilterBar)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerPurchaseOrdersTab(
    viewModel: FinanceViewModel,
    onOrderClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCustomerOrders.collectAsStateWithLifecycle()
    val errorMsg by viewModel.customerOrdersError.collectAsStateWithLifecycle()

    val totalOrdersCount = orders.size
    val openOrdersCount = orders.count {
        !it.status.equals("Delivered", ignoreCase = true) &&
                !it.status.equals("Cancelled", ignoreCase = true)
    }
    val completedCount = orders.count {
        it.status.equals("Delivered", ignoreCase = true) ||
                it.status.equals("Completed", ignoreCase = true)
    }
    val totalInvoicedValue = orders.sumOf { it.grandTotal ?: 0.0 }

    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) orders
        else {
            orders.filter { order ->
                (order.orderCode ?: "").contains(searchQuery, ignoreCase = true) ||
                        order.items.any { item ->
                            (item.itemDescription ?: "").contains(searchQuery, ignoreCase = true)
                        }
            }
        }
    }

    if (isLoading && orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CirculerProgressIndicatorSmall()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search order number, items...",
            accentColor = BluePrimary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )

        HorizontalDivider(color = dividerColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
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
                            tint = TextSecondary,
                            modifier = Modifier.size(tokens.iconSize)
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
                                .size(tokens.iconSize * 0.5f)
                                .clip(CircleShape)
                                .background(BluePrimary)
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
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
                            tint = greentext,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                )

                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "TOTAL VALUE",
                    value = "₹${formatIndianNumber(totalInvoicedValue)}",
                    subtitle = "Net invoiced value",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Paid,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                )
            }

            Text(
                text = "ORDERS HISTORY",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = BluePrimary
            )
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.screenPadding * 2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMsg ?: if (searchQuery.isNotBlank()) "No matching orders found" else "No orders found for this customer",
                    color = mutedText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                filteredOrders.forEach { order ->
                    CustomerOrderDataCard(
                        order = order,
                        onClick = { onOrderClick(order._id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// 3. TRANSACTIONS TAB (Live Payment Transactions from Orders)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerTransactionsTab(viewModel: FinanceViewModel) {
    val tokens = LocalAppTokens.current
    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCustomerOrders.collectAsStateWithLifecycle()

    // Extract real paid transactions from customer orders
    val transactions = remember(orders) {
        orders.filter { (it.advanceAmountPaid ?: 0.0) > 0.0 || it.paymentStatus == "Paid" }
    }

    if (isLoading && orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CirculerProgressIndicatorSmall()
        }
        return
    }

    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Transaction History (${transactions.size})",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color,
                modifier = Modifier.padding(horizontal = tokens.screenPadding)
            )

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = dividerColor)

            transactions.forEach { order ->
                val amount = order.advanceAmountPaid ?: order.grandTotal ?: 0.0
                val dateStr = formatCustomerDate(order.orderDate ?: order.createdAt)
                val statusText = order.paymentStatus ?: "Completed"

                DataCard(
                    item = order,
                    topBadgeText = statusText,
                    topBadgeTextColor = greentext,
                    topBadgeBgColor = greenBg,
                    topBadgeInline = true,
                    title = "Order Payment #${order.orderCode ?: "ORD"}",
                    footerFields = listOf(
                        DataCardField(
                            icon = Icons.Default.CalendarMonth,
                            text = dateStr
                        ),
                        DataCardField(
                            text = "Amount: ₹${formatIndianNumber(amount)}"
                        )
                    )
                )
            }

            Spacer(Modifier.height(tokens.screenPadding * 2))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 4. PREFERENCES TAB (Live API Data)
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
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                Text(
                    text = "CUSTOMER PREFERENCES & TERMS",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Customer Level",
                        value = customer.customerLevel?.takeIf { it.isNotBlank() } ?: "Regular",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Preferred Contact Method",
                        value = customer.preferredContactMethod?.takeIf { it.isNotBlank() } ?: "Whatsapp",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Preferred Language",
                        value = customer.preferredLanguage?.takeIf { it.isNotBlank() } ?: "English",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Credit Limit",
                        value = "₹${formatIndianNumber(customer.creditLimit ?: 0.0)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreferenceColumnItem(
                        label = "Credit Period",
                        value = "${customer.creditPeriodDays ?: 0} Days",
                        modifier = Modifier.weight(1f)
                    )
                    PreferenceColumnItem(
                        label = "Account Status",
                        value = customer.status?.takeIf { it.isNotBlank() } ?: "Active",
                        valueColor = if (customer.status.equals("Active", true)) greentext else redText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// 5. NOTES & TAGS TAB (Live API Data)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerNotesAndTagsTab(customer: CustomerItemV2) {
    val tokens = LocalAppTokens.current
    val customFields = customer.customFields
    val notes = customFields?.notes?.takeIf { it.isNotBlank() }
    val internalNotes = customFields?.internalNotes?.takeIf { it.isNotBlank() }
    val tags = customFields?.tags.orEmpty().filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(tokens.screenPadding)
    ) {
        Text(
            text = "Customer Notes & Tags",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Manage internal remarks, bespoke logs, and segmentation.",
            fontSize = tokens.caption,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(tokens.extraPadding))

        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                Text(
                    text = "Customer Notes",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notes ?: "No customer notes recorded.",
                    fontSize = tokens.bodySmall,
                    color = if (notes != null) title_color else mutedText,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "Internal Notes",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = internalNotes ?: "No internal notes recorded.",
                    fontSize = tokens.bodySmall,
                    color = if (internalNotes != null) title_color else mutedText,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "Tags",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))

                if (tags.isEmpty()) {
                    Text(
                        text = "No tags assigned.",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        fontWeight = FontWeight.Normal
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                color = primary_light,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = BluePrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// Subcomponents & Helpers
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerOrderDataCard(
    order: CustomerOrderItem,
    onClick: () -> Unit
) {
    val rawStatus = (order.status ?: "Pending").replace("_", " ")
    val (statusLabel, statusColors) = orderStatusColors(rawStatus)
    val (statusTextColor, statusBgColor) = statusColors

    val itemsSummary = remember(order.items) {
        val count = order.items.size
        val descriptions = order.items.mapNotNull { it.itemDescription }.take(2).joinToString(", ")
        if (count > 0 && descriptions.isNotBlank()) {
            "$count Items • $descriptions"
        } else if (count > 0) {
            "$count Items"
        } else {
            "No Items Specified"
        }
    }

    val dateFormatted = formatCustomerDate(order.orderDate ?: order.createdAt)
    val balance = order.balanceAmount ?: 0.0
    val grandTotal = order.grandTotal ?: 0.0

    DataCard(
        item = order,
        topBadgeText = statusLabel,
        topBadgeTextColor = statusTextColor,
        topBadgeBgColor = statusBgColor,
        topBadgeInline = true,
        title = order.orderCode ?: "ORD-N/A",
        footerFields = listOf(
            DataCardField(
                icon = Icons.Default.CalendarMonth,
                text = dateFormatted
            ),
            DataCardField(
                text = itemsSummary
            ),
            DataCardField(
                text = "Total: ₹${formatIndianNumber(grandTotal)} | Balance: ₹${formatIndianNumber(balance)}"
            )
        ),
        actions = listOf(
            MenuAction(
                label = "View Details",
                onClick = onClick
            )
        ),
        onClick = { onClick() }
    )
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                icon()
            }

            Spacer(Modifier.height(tokens.extraPadding / 2))

            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = mutedText
            )
        }
    }
}

@Composable
private fun PreferenceColumnItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = title_color
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = mutedText,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private fun formatFullAddress(addr: CustomerAddressV2?): String {
    if (addr == null) return "N/A"
    val parts = linkedSetOf<String>()
    addr.flatNo?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.street?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.areaZone?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.addressLine?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.city?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
    addr.subdivisionName?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
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
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun orderStatusColors(status: String): Pair<String, Pair<Color, Color>> {
    val normalized = status.lowercase()
    return when {
        normalized.contains("deliver") || normalized.contains("complete") -> {
            status to (greentext to greenBg)
        }
        normalized.contains("cancel") -> {
            status to (redText to redBg)
        }
        normalized.contains("progress") || normalized.contains("production") -> {
            status to (yellowText to yellowBg)
        }
        else -> {
            status to (BluePrimary to primary_light)
        }
    }
}