@file:Suppress("unused","unusedVariable")

package com.cuso.mobile.view.home.finance.account_payable.payment_mode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

// --- UI Data Models ---
private data class PaymentListEntry(
    val id: String,
    val customerName: String,
    val customerRole: String = "Customer",
    val status: String,
    val date: String,
    val paymentMode: String,
    val amount: Int,
    val invoiceId: String
)

// --- Sample Data ---
private val samplePayments = listOf(
    PaymentListEntry("PAY-1024", "Ravi Teja", "Premium Customer", "Completed", "25 Feb 2026", "Online Transfer", 15500, "INV-882"),
    PaymentListEntry("PAY-1025", "Sarah Connor", "Customer", "Pending", "24 Feb 2026", "Cash", 2400, "INV-901"),
    PaymentListEntry("PAY-1026", "John Wick", "Wholesale", "Completed", "23 Feb 2026", "Cheque", 45000, "INV-772"),
    PaymentListEntry("PAY-1027", "Ellen Ripley", "Customer", "Failed", "22 Feb 2026", "Online Transfer", 890, "INV-102")
)

@Composable
fun AllPaymentListScreen(
    onClose: () -> Unit = {},
    onPaymentClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // --- State Management ---
    var searchText by remember { mutableStateOf("") }
    val filterDrawerState = rememberFilterDrawerState()

    // Define Filter Sections for the Drawer
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Status",
                icon = Icons.Default.Sell,
                type = FilterSectionType.CHECKBOX_LIST,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("completed", "Completed"),
                    FilterOption("pending", "Pending"),
                    FilterOption("failed", "Failed")
                )
            ),
            FilterSection(
                title = "Payment Mode",
                icon = Icons.Default.Payments,
                type = FilterSectionType.CHIP_ROW,
                options = listOf(
                    FilterOption("cash", "Cash"),
                    FilterOption("online", "Online"),
                    FilterOption("cheque", "Cheque")
                )
            ),
            FilterSection(
                title = "Date Range",
                icon = Icons.Default.History,
                type = FilterSectionType.CHIP_GRID,
                options = listOf(
                    FilterOption("today", "Today"),
                    FilterOption("yesterday", "Yesterday"),
                    FilterOption("this_week", "This Week")
                )
            )
        )
    }

    // Search Logic
    val filteredPayments = remember(searchText) {
        if (searchText.isBlank()) samplePayments
        else samplePayments.filter {
            it.customerName.contains(searchText, ignoreCase = true) ||
                    it.id.contains(searchText, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar("All Payments", onClose = onClose)
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Primary_background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // --- Navigation Breadcrumb ---

                // --- Search + Filter Bar (Using Reusable Component) ---
                SearchFilterBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    placeholder = "Search payments...",
                    showFilterIcon = true,
                    onFilterClick = { filterDrawerState.open() }
                )

                // --- Payment List Container ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(filteredPayments) { entry ->
                        // Reusing DataCard Component

                        DataCard(
                            showDateIcon = false,
                            item = entry,
                            title = entry.id,
                            subtitle = "${entry.customerName} • ${entry.customerRole}",
// Status styling logic
                            topBadgeText = entry.status,
                            topBadgeBgColor = when(entry.status) {
                                "Completed" -> Color(0xFFDCFCE7)
                                "Pending" -> Color(0xFFFEF9C3)
                                else -> Color(0xFFFEE2E2)
                            },
                            topBadgeTextColor = when(entry.status) {
                                "Completed" -> Color(0xFF10B981)
                                "Pending" -> Color(0xFFA16207)
                                else -> Color(0xFFEF4444)
                            },
                            footerFields = listOf(
                                DataCardField(
                                    label = "Date",
                                    text = entry.date,
                                    asRow = true
                                ),
                                DataCardField(
                                    label = "Payment Mode",
                                    text = entry.paymentMode,
                                    asRow = true
                                ),
                                DataCardField(
                                    label = "Amount",
                                    text = "${entry.amount}",
                                    asRow = true
                                ),
                                DataCardField(
                                    label = "Allocated Invoice",
                                    text = entry.invoiceId,
                                    asRow = true
                                )
                            ),
                            actions = listOf(
                                MenuAction("View ") { onPaymentClick(entry.id) },
                                MenuAction("Edit") { /* Action */ }
                            ),
                            onClick = { onPaymentClick(entry.id) }
                        )
                    }
                }
            }
        }

        // --- Filter Drawer Overlay ---
        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                filterDrawerState.close()
            },
            onClearAll = {
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllPaymentListScreenPreview() {
    AllPaymentListScreen()
}



