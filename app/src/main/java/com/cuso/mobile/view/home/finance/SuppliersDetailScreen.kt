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
package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.PrimaryBorder

private val AccentPurple = Color(0xFF3B3BF9)
private val AccentPurpleSoft = Color(0xFFEEEEFE)
private val TextPrimary = Color(0xFF111827)
private val mutedTextDark = Color(0xFF6B7280)
private val SectionBg = Color(0xFFF9FAFB)
private val BorderLight = Color(0xFFF0F0F0)

@Composable
fun SupplierDetailScreen(
    supplier: SupplierRow,
    onClose: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    // Define tabs with icons
    val tabs = listOf(
        TabItem(
            label = "Overview",
            iconPainter = R.drawable.ic_sheet_timer,
        ),
        TabItem(
            label = "Transactions",
            iconPainter = R.drawable.ic_transaction_sheet,
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("All Suppliers", onClose = onClose)
            }

//            ScreenBreadcrumb(
//                segments = listOf("Finance", "All Suppliers"),
//                onClick = onBreadcrumbClick
//            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Spacer(Modifier.height(12.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(horizontal = tokens.extraPadding, vertical = 10.dp)
                ) {

                    // ── Name + contact row ──
                    Text(
                        supplier.name,
                        fontSize = tokens.h2,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_phone),
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize * 0.78f)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("916369460554", fontSize = tokens.caption, color = mutedTextDark)
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_mail_2),
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize * 0.78f)
                        )
                        Spacer(Modifier.width(3.dp))

                        val email = "arjun@royalfurnitures.com"
                        val displayEmail = email.take(20) + "..."

                        Text(
                            text = displayEmail,
                            fontSize = tokens.caption,
                            color = mutedTextDark,
                            maxLines = 1
                        )

                        Spacer(Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize * 0.78f)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("20-03-2026", fontSize = tokens.caption, color = mutedTextDark)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {

                    // ── Reusable SettingsTabs ──
                    SettingsTabs(
                        tabs = tabs,
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = whiteBg,
                        selectedBackgroundColor = Color(0xFFEEF0FF),
                        selectedTextColor = Primary,
                        unselectedTextColor = TextSecondary,
                        selectedIconColor = Primary,
                        unselectedIconColor = TextSecondary,
                        borderColor = Color(0xFFE5E7EB),
                        cornerRadius = 12.dp,
                        selectedCornerRadius = 10.dp
                    )

                    Spacer(Modifier.height(20.dp))

                    when (selectedTabIndex) {
                        0 -> SupplierOverviewTab()
                        1 -> SupplierTransactionsTab()
                    }

                    Spacer(Modifier.height(80.dp)) // Extra space for FAB
                }
            }
        }

        // ── StepNavigationFab at bottom with Share and Download ──
        StepNavigationFab(
            modifier = Modifier.fillMaxSize(),
            showBack = true,
            onBack = {
                isSharing = true
                // Handle Share action
                // In real implementation, call your share function
            },
            showBackArrow = false,
            showTrailingArrow = false,
            backLabel = "Share",
            backEnabled = !isSharing,
            backWidthFraction = 0.28f,
            trailingAction = TrailingFabAction.Next(
                label = "Download Invoice",
                enabled = !isDownloading,
                onClick = {
                    isDownloading = true
                    // Handle Download action
                    // In real implementation, call your download function
                }
            )
        )
    }

    // Reset states when done (in real implementation)
    LaunchedEffect(isDownloading) {
//        if (isDownloading) {
//            // Simulate async operation
//            // Reset after completion
//        }
    }

    LaunchedEffect(isSharing) {
//        if (isSharing) {
//            // Simulate async operation
//            // Reset after completion
//        }
    }
}

// ─────────────────────────────────────────────────────────────
// OVERVIEW TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierOverviewTab() {
    val tokens = LocalAppTokens.current

    Column {
        // ── Outstanding / Unused credits ──
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.extraPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Outstanding Receivables", fontSize = tokens.bodySmall, color = mutedText)
                Spacer(Modifier.height(6.dp))
                Text("₹4,86,900.00", fontSize = tokens.bodyMedium, color = TextPrimary)
            }

            VerticalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp),
                color = PrimaryBorder
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text("Unused Credits", fontSize = tokens.bodySmall, color = mutedText)
                Spacer(Modifier.height(6.dp))
                Text("₹0", fontSize = tokens.bodyMedium, color = TextPrimary)
            }
        }
        HorizontalDivider(Modifier.background(PrimaryBorder))


        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(16.dp))

        Text("Customer Information", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        InfoRow("Type", "Business")
        InfoRow("Contact", "Raji")
        InfoRow("Phone", "916369460554")
        InfoRow("Email", "arjun@royalfurnitures.com")
        InfoRow("GST", "33ABCDE1234F1Z5")
        InfoRow("Payment Terms", "Net 30")
        InfoRow("Payment Mode", "Bank Transfer")

        Spacer(Modifier.height(20.dp))
        Text("Addresses", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))

        AddressBlock(
            label = "Billing Address",
            address = "Emily Johnson Vendor, 452 Industrial\nParkway, Suite 102, New Delhi, Delhi 110001.",
            email = "emily.j@vendor.com",
            phone = "+91 98765 43210"
        )
        Spacer(Modifier.height(14.dp))
        AddressBlock(
            label = "Shipping Address",
            address = "Emily Johnson Vendor, 452 Industrial\nParkway, Suite 102, New Delhi, Delhi 110001.",
            email = null,
            phone = null
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = BorderLight)
}

@Composable
private fun AddressBlock(label: String, address: String, email: String?, phone: String?) {
    val tokens = LocalAppTokens.current

    Column {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(6.dp))
        Row {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(tokens.iconSize * 0.9f))
            Spacer(Modifier.width(6.dp))
            Text(address, fontSize = tokens.bodySmall, color = TextPrimary, lineHeight = tokens.bodyMedium)
        }
        if (email != null || phone != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                email?.let {
                    Icon(Icons.Default.Email, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(tokens.iconSize * 0.78f))
                    Spacer(Modifier.width(4.dp))
                    Text(it, fontSize = tokens.caption, color = mutedTextDark)
                    Spacer(Modifier.width(12.dp))
                }
                phone?.let {
                    Icon(Icons.Default.Call, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(tokens.iconSize * 0.78f))
                    Spacer(Modifier.width(4.dp))
                    Text(it, fontSize = tokens.caption, color = mutedTextDark)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TRANSACTIONS TAB
// ─────────────────────────────────────────────────────────────
private data class InvoiceLine(val desc: String, val hsn: String, val qty: Int, val unitPrice: Double, val discount: String, val tax: String, val total: Double)

private val dummyInvoiceLines = listOf(
    InvoiceLine("Enterprise ERP Software License (Annual subscription)", "SKU-ERP-001", 10, 1250.00, "10%", "15%", 11140.00),
    InvoiceLine("Implementation Services (Onsite setup)", "SKU-SERV-INR", 8, 950.00, "5%", "15%", 8550.00),
    InvoiceLine("Custom Module Development", "SKU-DEV-CUST", 7, 1150.00, "10%", "15%", 8550.00)
)

@Composable
private fun SupplierTransactionsTab() {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(16.dp)
    ) {
        // ── Invoice header ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("RELDA", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                Spacer(Modifier.height(4.dp))
                Text("123 Innovative Way, Palo Park, CA 94103, USA", fontSize = tokens.label, color = mutedText)
                Text("finance@relda.com", fontSize = tokens.label, color = mutedText)
                Text("+1 (415) 555-0123", fontSize = tokens.label, color = mutedText)
                Text("GST/VAT/ABN: GB123456789/DE987654321", fontSize = tokens.label, color = mutedText)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("INVOICE", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Invoice No: INV-2024-001", fontSize = tokens.label, color = mutedText)
                Text("Invoice Date: Oct 26, 2024", fontSize = tokens.label, color = mutedText)
                Text("Due Date: Nov 25, 2024", fontSize = tokens.label, color = mutedText)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Bill To", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("Acme Corp International", fontSize = tokens.label, color = mutedTextDark)
                Text("450 Business Ave, Suite 100, USA", fontSize = tokens.label, color = mutedTextDark)
                Text("Phone: +1 234 567 890", fontSize = tokens.label, color = mutedTextDark)
                Text("Email: accounts@acmecorp.com", fontSize = tokens.label, color = mutedTextDark)
            }
            Column(Modifier.weight(1f)) {
                Text("Ship To", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("Acme Corp Warehouse, 789 Logistics Blvd,", fontSize = tokens.label, color = mutedTextDark)
                Text("Manchester, M1 1AA, UK", fontSize = tokens.label, color = mutedTextDark)
                Text("Reference:", fontSize = tokens.label, color = mutedTextDark)
                Text("Order ID: ORD-9876", fontSize = tokens.label, color = mutedTextDark)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(8.dp))

        // ── Table header ──
        Row(Modifier.fillMaxWidth()) {
            Text("Item/Description", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(2.5f))
            Text("HSN/SKU", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(1f))
            Text("Qty", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(0.6f))
            Text("Total", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = BorderLight)

        dummyInvoiceLines.forEach { line ->
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(line.desc, fontSize = tokens.label, color = TextPrimary, modifier = Modifier.weight(2.5f), lineHeight = tokens.caption)
                Text(line.hsn, fontSize = tokens.label, color = mutedTextDark, modifier = Modifier.weight(1f))
                Text("${line.qty}", fontSize = tokens.label, color = mutedTextDark, modifier = Modifier.weight(0.6f))
                Text("₹${line.total}", fontSize = tokens.label, color = TextPrimary, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Payment Method: Bank Transfer / Card / UPI", fontSize = tokens.label, color = mutedTextDark)
                Spacer(Modifier.height(4.dp))
                Text("Bank Details:", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Bank Name: Global Commerce Bank", fontSize = tokens.label, color = mutedTextDark)
                Text("Account No: 1234567890", fontSize = tokens.label, color = mutedTextDark)
                Text("IFSC/SWIFT: GCB123US6", fontSize = tokens.label, color = mutedTextDark)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Subtotal: ₹23,850.00", fontSize = tokens.label, color = mutedTextDark)
                Text("Discount: -₹600.00", fontSize = tokens.label, color = mutedTextDark)
                Text("Tax Standard (VAT 10%): ₹2,245.00", fontSize = tokens.label, color = mutedTextDark)
                Text("Shipping/Handling: ₹150.00", fontSize = tokens.label, color = mutedTextDark)
                Spacer(Modifier.height(4.dp))
                Text("Grand Total: ₹24,845.00", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Terms & Conditions", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(
            "Payment due within 30 days of invoice date. Late fees may apply. Goods remain property of Apex Global Solutions until paid in full.",
            fontSize = tokens.label,
            color = mutedText
        )
    }
}