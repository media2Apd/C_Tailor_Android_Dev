package com.cuso.mobile.view.home.finance.account_receivable.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.CustomerItemV2
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.viewmodel.FinanceViewModel

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

    val tabs = listOf(
        TabItem(label = "Overview", icon = Icons.Default.Dashboard),
        TabItem(label = "Transactions", icon = Icons.AutoMirrored.Filled.ReceiptLong)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // --- Top Bar ---
        TitleBar(title = "All Customers", onClose = onClose)
        HorizontalDivider(color = dividerColor)

        if (customer == null && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CirculerProgressIndicatorSmall()
            }
            return
        }

        if (customer == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

        Spacer(Modifier.height(tokens.extraPadding))

        // --- Customer Header Details ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Text(
                text = customer.name,
                fontSize = tokens.h2,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )

            Spacer(Modifier.height(tokens.extraPadding / 2))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                // Phone
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

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = iconMuted,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatDate(customer.createdAt),
                        fontSize = tokens.bodySmall,
                        color = close_color
                    )
                }
            }
        }

        HorizontalDivider(color = dividerColor)

        // --- Tabs Section ---
        SettingsTabs(
            tabs = tabs,
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        )

        Spacer(Modifier.height(tokens.extraPadding))

        // --- Dynamic Tab Content ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> CustomerOverviewTab(customer = customer)
                1 -> CustomerTransactionsTab()
            }
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
        // --- Receivables & Unused Credits 2-Column Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .border(width = 1.dp, color = dividerColor)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            // Outstanding Receivables
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Outstanding Receivables",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹ ${formatIndianNumber(customer.outstanding ?: 0.0)}",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            }

            // Vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(tokens.fieldHeight)
                    .background(sectionBorder)
            )

            Spacer(Modifier.width(tokens.screenPadding))

            // Unused Credits
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unused Credits",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹ 0",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // --- Section: Customer Information Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Customer Information",
                fontSize = tokens.bodyLarge,
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
                label = "Type",
                value = customer.type.replaceFirstChar { it.uppercase() }
            )
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // --- Section: Addresses Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Addresses",
                fontSize = tokens.bodyLarge,
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
            val addressText = customer.address?.addressLine?.takeIf { it.isNotBlank() } ?: "N/A"

            AddressInfoRow(label = "Billing Address", address = addressText)
            HorizontalDivider(color = dividerColor)

            AddressInfoRow(label = "Shipping Address", address = addressText)
        }

        Spacer(Modifier.height(tokens.screenPadding * 2))
    }
}

// ─────────────────────────────────────────────────────────────
// 2. TRANSACTIONS TAB (Empty State)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomerTransactionsTab() {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Transaction History",
            fontSize = tokens.bodyLarge,
            color = title_color,
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        )

        HorizontalDivider(color = dividerColor)

        // Centered Empty State
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
                // Themed Empty Icon Container
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
// Helper Row Components
// ─────────────────────────────────────────────────────────────
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
            fontSize = tokens.bodyMedium,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = close_color,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = address,
                fontSize = tokens.bodyMedium,
                color = textSubdued,
                fontWeight = FontWeight.Medium
            )
        }
    }
}