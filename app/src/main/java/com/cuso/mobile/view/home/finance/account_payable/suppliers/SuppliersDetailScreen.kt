@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "SameParameterValue"
)

package com.cuso.mobile.view.home.finance.account_payable.suppliers

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.TitleBar

@Composable
fun SupplierDetailScreen(
    supplier: SupplierRow,
    onClose: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem(
            label = "Overview",
            icon = Icons.Default.Dashboard
        ),
        TabItem(
            label = "Transactions",
            icon = Icons.AutoMirrored.Filled.ReceiptLong
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // --- Top Bar ---
            TitleBar("All Suppliers", onClose = onClose)
            HorizontalDivider(color = dividerColor)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Spacer(Modifier.height(tokens.extraPadding))

                // --- Fixed Supplier Header Details (Clean 2-Line Structure) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                ) {
                    Text(
                        text = supplier.name,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )

                    Spacer(Modifier.height(8.dp))

                    // Line 1: Phone & Date
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
                                text = "916369460554",
                                fontSize = tokens.bodySmall,
                                color = close_color,
                                maxLines = 1
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
                                text = "20-03-2026",
                                fontSize = tokens.bodySmall,
                                color = close_color,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Line 2: Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "arjun@royalfurnitures.com",
                            fontSize = tokens.bodySmall,
                            color = close_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                when (selectedTabIndex) {
                    0 -> SupplierOverviewTab()
                    1 -> SupplierTransactionsTab()
                }

                Spacer(Modifier.height(tokens.screenPadding * 2))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 1. OVERVIEW TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierOverviewTab() {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = "Outstanding Payables",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹ 4,86,900.00",
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

        // --- Section: Supplier Information Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(vertical = tokens.extraPadding)
        ) {
            Text(
                text = "Supplier Information",
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
            DetailInfoRow(label = "Type", value = "Business")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Contact", value = "Raji")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Phone", value = "916369460554")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Email", value = "arjun@royalfurnitures.com")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "GST", value = "33ABCDE1234F1Z5")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Payment Terms", value = "Net 30")
            HorizontalDivider(color = dividerColor)

            DetailInfoRow(label = "Payment Mode", value = "Bank Transfer")
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
            AddressInfoRow(
                label = "Billing Address",
                address = "Emily Johnson Vendor, 452 Industrial Parkway, Suite 102, New Delhi, Delhi 110001."
            )
            HorizontalDivider(color = dividerColor)

            AddressInfoRow(
                label = "Shipping Address",
                address = "Emily Johnson Vendor, 452 Industrial Parkway, Suite 102, New Delhi, Delhi 110001."
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 2. TRANSACTIONS TAB (Empty State)
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierTransactionsTab() {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Transaction History",
            fontSize = tokens.bodyLarge,
            color = title_color,
            modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        )

        HorizontalDivider(color = dividerColor)

        // Centered Themed Empty State
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.screenPadding * 2),
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