@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")


package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.CustomerItemV2
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.FinanceViewModel

private val CustPrimary = Color(0xFF3B3BF9)
private val CustPrimarySoft = Color(0xFFEEEEFE)
private val CustTextDark = Color(0xFF111827)
private val CustmutedText = Color(0xFF9CA3AF)
private val CustGreen = Color(0xFF22C55E)
private val CustRed = Color(0xFFEF4444)
private val CustYellow = Color(0xFFF59E0B)
private val CustBgLight = Color(0xFFF5F5F7)

// ─────────────────────────────────────────────────────────────
// Static Mock Data
// ─────────────────────────────────────────────────────────────

data class StaticCustomer(
    val id: String,
    val name: String,
    val mobile: String,
    val type: String,
    val address: String,
    val status: String,
    val outstanding: Int,
    val totalSpend: Int,
    val pendingPayment: Int,
    val createdAt: String,
    val billingAddress: String = "sdsdsdsds",
    val shippingAddress: String = "sdsdsdsds"
)

private val mockCustomers = listOf(
    StaticCustomer(
        id = "1",
        name = "Kuberan J",
        mobile = "916369460554",
        type = "Individual",
        address = "Chennai, Tamil Nadu",
        status = "Active",
        outstanding = 2801,
        totalSpend = 45000,
        pendingPayment = 0,
        createdAt = "2026-03-20T00:00:00.000Z",
        billingAddress = "sdsdsdsds",
        shippingAddress = "sdsdsdsds"
    ),
    StaticCustomer(
        id = "2",
        name = "Nithish Kumar",
        mobile = "9345483369",
        type = "Individual",
        address = "Chennai, Tamil Nadu",
        status = "Active",
        outstanding = 15964,
        totalSpend = 45000,
        pendingPayment = 5000,
        createdAt = "2026-07-07T09:06:38.519Z",
        billingAddress = "sdsdsdsds",
        shippingAddress = "sdsdsdsds"
    ),
    StaticCustomer(
        id = "3",
        name = "Joseph",
        mobile = "1234123412",
        type = "Individual",
        address = "Chennai, Tamil Nadu",
        status = "Active",
        outstanding = 100,
        totalSpend = 12000,
        pendingPayment = 0,
        createdAt = "2026-07-09T06:24:07.882Z",
        billingAddress = "sdsdsdsds",
        shippingAddress = "sdsdsdsds"
    ),
    StaticCustomer(
        id = "4",
        name = "Sasi Kumar",
        mobile = "1542345455",
        type = "Individual",
        address = "Chennai, Tamil Nadu",
        status = "Active",
        outstanding = 1040,
        totalSpend = 25000,
        pendingPayment = 2000,
        createdAt = "2026-07-13T06:41:41.327Z",
        billingAddress = "sdsdsdsds",
        shippingAddress = "sdsdsdsds"
    )
)

// ─────────────────────────────────────────────────────────────
// FinanceCustomerScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceCustomerScreen(
    onClose: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onCustomerEdit: (String) -> Unit = onCustomerClick,
    onBreadCrumbClick: () -> Unit ={}


) {
    val viewModel: FinanceViewModel = hiltViewModel()

    val customerListResponse by viewModel.financeCustomerList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFinanceCustomers.collectAsStateWithLifecycle()
    val error by viewModel.financeCustomerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }



    LaunchedEffect(Unit) {
        viewModel.fetchCustomerForFinance()
    }

    val allCustomers = customerListResponse?.data ?: emptyList()

    val filteredCustomers = allCustomers.filter { customer ->
        val matchesSearch = searchQuery.isBlank() ||
                customer.name.contains(searchQuery, ignoreCase = true) || customer.mobile.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Active" -> customer.status.equals("Active", ignoreCase = true)
            "Inactive" -> customer.status.equals("Inactive", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {


        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    ListSkeleton()
                }

                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Something went wrong", color = CustRed, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchCustomerForFinance() }) { Text("Retry") }
                        }
                    }
                }

                filteredCustomers.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonOff,
                                null,
                                tint = CustmutedText,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isNotBlank() || selectedFilter != "All")
                                    "No matching customers found"
                                else "No customers yet",
                                fontSize = 14.sp,
                                color = CustmutedText
                            )
                        }
                    }
                }

                else -> {
                    var searchQuery by remember { mutableStateOf("") }

                    Column(modifier = Modifier.fillMaxSize()) {

                        // ── Fixed header (scroll ஆகாது) ──
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                        ) {

                            // Title row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "All Customers",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF111827),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable { onClose() }
                                )
                            }

                            // ── Breadcrumb + SearchFilterBar — F8F9FF background block ──
                            Column(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                ScreenBreadcrumb(
                                    segments = listOf("Finance", "Customer"),
                                    onClick = {onBreadCrumbClick()}
                                )

                                SearchFilterBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.padding(
                                        horizontal = 20.dp,
                                        vertical = 12.dp
                                    ),
                                    placeholder = "Search Customers...",
                                    accentColor = BluePrimary,
                                    borderColor = BorderGray,
                                    textSecondaryColor = TextSecondary,
                                    onFilterClick = { }
                                )
                            }
                        }

                        // ── Scrollable list ──
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredCustomers, key = { it._id }) { customer ->
                                CustomerCardItem(
                                    customer = customer,
                                    onClick = { onCustomerClick(customer._id) },
                                    onEdit = { onCustomerEdit(customer._id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerCardItem(
    customer: CustomerItemV2,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val (badgeText, badgeColor) = statusColorsOfCustomer(customer.status)

    val typeAndAddress = "${customer.type.replaceFirstChar { it.uppercase() }} • " +
            (customer.address?.addressLine?.takeIf { it.isNotBlank() } ?: "N/A")

    DataCard(
        item = customer,
        topBadgeText = badgeText,
        topBadgeTextColor = badgeColor,
        topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
        title = customer.name,
        footerFields = listOf(
            DataCardField(
                icon = Icons.Default.Phone,
                text = customer.mobile
            ),
            DataCardField(
                text = typeAndAddress
            ),
            DataCardField(
                text = "Outstanding: ₹${formatIndianNumber(customer.outstanding ?: 0.0)}"
            )
        ),
        actions = listOf(
            MenuAction("View", Icons.Default.Visibility) { onClick() },
            MenuAction("Edit", Icons.Default.Edit) { onEdit() }
        ),
        onClick = { onClick() }
    )
}

private fun statusColorsOfCustomer(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "active" -> "Active" to Color(0xFF16A34A)
    "inactive" -> "Inactive" to Color(0xFF6B7280)
    else -> "Unknown" to Color(0xFF9CA3AF)
}

@Composable
private fun StatusBadgeStatic(status: String) {
    val isActive = status.equals("Active", ignoreCase = true)
    val color = if (isActive) CustGreen else Color(0xFF9CA3AF)
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(5.dp))
        Text(status, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}

// ─────────────────────────────────────────────────────────────
// CustomerDetailScreen - Exactly as per design
// ─────────────────────────────────────────────────────────────
@Composable
fun CustomerDetailScreenStatic(
    customerId: String,
    onClose: () -> Unit,
    isEditMode: Boolean = false     // 👈 NEW

) {
    val viewModel: FinanceViewModel = hiltViewModel()

    val detail by viewModel.financeCustomerDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFinanceCustomerDetail.collectAsStateWithLifecycle()
    val error by viewModel.financeCustomerDetailError.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("overview") }

    LaunchedEffect(customerId) {
        viewModel.getFinanceCustomerViewOne(customerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CustBgLight)
    ) {
        // Top bar with Back and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("All Customers", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CustTextDark)
            Spacer(Modifier.weight(1f))

            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = CustTextDark,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CustPrimary)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error ?: "Something went wrong", color = CustRed, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.getFinanceCustomerViewOne(customerId) }) { Text("Retry") }
                    }
                }
            }
            detail != null -> {
                val d = detail!!
                val customer = StaticCustomer(
                    id = customerId,
                    name = d.customerInformation.name,
                    mobile = d.customerInformation.phone,
                    type = d.customerInformation.type.replaceFirstChar { it.uppercase() },
                    address = d.billingAddress.addressLine ?: "-",
                    status = "Active",
                    outstanding = d.financialSummary.outstandingReceivables.toInt(),
                    totalSpend = 0,
                    pendingPayment = 0,
                    createdAt = d.customerInformation.createdAt,
                    billingAddress = d.billingAddress.addressLine ?: "-",
                    shippingAddress = d.shippingAddress.addressLine ?: "-"
                )

                // Customer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(customer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CustTextDark)

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = CustmutedText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(customer.mobile, fontSize = 14.sp, color = CustmutedText)
                        Spacer(Modifier.width(20.dp))

                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CustmutedText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formatDate(customer.createdAt), fontSize = 14.sp, color = CustmutedText)
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(whiteBg)
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    TabButtonStatic(
                        label = "Overview",
                        isSelected = selectedTab == "overview",
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = "overview" }

                    Spacer(Modifier.width(6.dp))

                    TabButtonStatic(
                        label = "Transactions",
                        isSelected = selectedTab == "transactions",
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = "transactions" }
                }

                when (selectedTab) {
                    "overview" -> CustomerOverviewTab(customer)
                    "transactions" -> CustomerTransactionsTab()
                }
            }
        }
    }
}

@Composable
private fun TabButtonStatic(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier                      // ✅ use the passed-in modifier, not `Modifier`
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) CustPrimary else CustmutedText
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Overview Tab
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerOverviewTab(customer: StaticCustomer) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues( vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Summary - Outstanding & Unused Credits
        // Financial Summary - single card, two columns with a divider
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)          // 👈 add this
                    .background(whiteBg)
                    .padding(vertical = 10.dp)
                    .padding(horizontal = 14.dp)


            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Outstanding Receivables", fontSize = 12.sp, color = CustmutedText)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "₹ ${formatIndianNumber(customer.outstanding)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustTextDark
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = CustmutedText
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    Text("Unused Credits", fontSize = 12.sp, color = CustmutedText)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "₹ ${formatIndianNumber(0)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustTextDark
                    )
                }
            }
        }

        // Customer Information
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CustBgLight )

            ) {
                Row(
                    Modifier.background(whiteBg)
                        .padding(14.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Customer Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CustTextDark
                    )
                }

                Spacer(Modifier.height(12.dp))

                InfoRow("Name", customer.name)
                InfoRow("Phone", customer.mobile)
                InfoRow("Type", customer.type)
            }
        }

        // Addresses
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CustBgLight)

            ) {
                Row(
                    Modifier.background(whiteBg)
                        .padding(14.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Addresses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CustTextDark
                    )
                }

                Spacer(Modifier.height(12.dp))

                AddressRow("Billing Address", customer.billingAddress)
                AddressRow("Shipping Address", customer.shippingAddress)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(horizontal=16.dp,vertical = 8.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            color = CustmutedText
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            color = CustTextDark,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(color = Color(0xFFC7C4D8), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun AddressRow(label: String, value: String) {
    Row(
        Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = CustTextDark
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = CustmutedText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))

            Text(
                value,
                fontSize = 14.sp,
                color = CustTextDark
            )
        }
        Spacer(Modifier.padding(top = 5.dp))

        HorizontalDivider(
            color = Color(0xFFC7C4D8),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Transactions Tab
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerTransactionsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Row(
            Modifier.background(whiteBg)
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Transaction History",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CustTextDark
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CustBgLight)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CustPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = CustPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "No Transactions Found",
                    fontSize = 14.sp,
                    color = CustmutedText
                )
            }
        }
    }
}