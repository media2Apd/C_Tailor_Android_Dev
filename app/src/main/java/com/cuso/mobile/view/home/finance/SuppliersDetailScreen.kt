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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.title_font
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar

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
    var selectedTab by remember { mutableStateOf("Overview") }

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

        ScreenBreadcrumb(
            segments = listOf("Finance", "All Suppliers"),
            onClick = onBreadcrumbClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Name + contact row ──
            Text(supplier.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("916369460554", fontSize = 12.sp, color = mutedTextDark)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Email, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("arjun@royalfurnitures.com", fontSize = 12.sp, color = mutedTextDark)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("20-03-2026", fontSize = 12.sp, color = mutedTextDark)
            }

            Spacer(Modifier.height(16.dp))

            // ── Tab switcher ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SectionBg, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                listOf("Overview" to Icons.Default.Description, "Transactions" to Icons.Default.Receipt).forEach { (tab, icon) ->
                    val isSelected = tab == selectedTab
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentPurple else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) whiteBg else mutedTextDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tab,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) whiteBg else mutedTextDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            when (selectedTab) {
                "Overview" -> SupplierOverviewTab()
                "Transactions" -> SupplierTransactionsTab()
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// OVERVIEW TAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun SupplierOverviewTab() {
    Column {
        // ── Outstanding / Unused credits ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Outstanding Receivables", fontSize = 12.sp, color = mutedText)
                Spacer(Modifier.height(4.dp))
                Text("₹4,86,900.00", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column {
                Text("Unused Credits", fontSize = 12.sp, color = mutedText)
                Spacer(Modifier.height(4.dp))
                Text("₹0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(16.dp))

        Text("Customer Information", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        InfoRow("Type", "Business")
        InfoRow("Contact", "Raji")
        InfoRow("Phone", "916369460554")
        InfoRow("Email", "arjun@royalfurnitures.com")
        InfoRow("GST", "33ABCDE1234F1Z5")
        InfoRow("Payment Terms", "Net 30")
        InfoRow("Payment Mode", "Bank Transfer")

        Spacer(Modifier.height(20.dp))
        Text("Addresses", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = 12.sp, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = BorderLight)
}

@Composable
private fun AddressBlock(label: String, address: String, email: String?, phone: String?) {
    Column {
        Text(label, fontSize = 12.sp, color = mutedText)
        Spacer(Modifier.height(6.dp))
        Row {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(address, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
        }
        if (email != null || phone != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                email?.let {
                    Icon(Icons.Default.Email, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, fontSize = 12.sp, color = mutedTextDark)
                    Spacer(Modifier.width(12.dp))
                }
                phone?.let {
                    Icon(Icons.Default.Call, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, fontSize = 12.sp, color = mutedTextDark)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        // ── Invoice header ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("RELDA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                Spacer(Modifier.height(4.dp))
                Text("123 Innovative Way, Palo Park, CA 94103, USA", fontSize = 9.sp, color = mutedText)
                Text("finance@relda.com", fontSize = 9.sp, color = mutedText)
                Text("+1 (415) 555-0123", fontSize = 9.sp, color = mutedText)
                Text("GST/VAT/ABN: GB123456789/DE987654321", fontSize = 9.sp, color = mutedText)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("INVOICE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Invoice No: INV-2024-001", fontSize = 9.sp, color = mutedText)
                Text("Invoice Date: Oct 26, 2024", fontSize = 9.sp, color = mutedText)
                Text("Due Date: Nov 25, 2024", fontSize = 9.sp, color = mutedText)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Bill To", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("Acme Corp International", fontSize = 10.sp, color = mutedTextDark)
                Text("450 Business Ave, Suite 100, USA", fontSize = 10.sp, color = mutedTextDark)
                Text("Phone: +1 234 567 890", fontSize = 10.sp, color = mutedTextDark)
                Text("Email: accounts@acmecorp.com", fontSize = 10.sp, color = mutedTextDark)
            }
            Column(Modifier.weight(1f)) {
                Text("Ship To", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("Acme Corp Warehouse, 789 Logistics Blvd,", fontSize = 10.sp, color = mutedTextDark)
                Text("Manchester, M1 1AA, UK", fontSize = 10.sp, color = mutedTextDark)
                Text("Reference:", fontSize = 10.sp, color = mutedTextDark)
                Text("Order ID: ORD-9876", fontSize = 10.sp, color = mutedTextDark)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(8.dp))

        // ── Table header ──
        Row(Modifier.fillMaxWidth()) {
            Text("Item/Description", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(2.5f))
            Text("HSN/SKU", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(1f))
            Text("Qty", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(0.6f))
            Text("Total", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = mutedText, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = BorderLight)

        dummyInvoiceLines.forEach { line ->
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(line.desc, fontSize = 9.sp, color = TextPrimary, modifier = Modifier.weight(2.5f), lineHeight = 12.sp)
                Text(line.hsn, fontSize = 9.sp, color = mutedTextDark, modifier = Modifier.weight(1f))
                Text("${line.qty}", fontSize = 9.sp, color = mutedTextDark, modifier = Modifier.weight(0.6f))
                Text("₹${line.total}", fontSize = 9.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderLight)
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Payment Method: Bank Transfer / Card / UPI", fontSize = 9.sp, color = mutedTextDark)
                Spacer(Modifier.height(4.dp))
                Text("Bank Details:", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Bank Name: Global Commerce Bank", fontSize = 9.sp, color = mutedTextDark)
                Text("Account No: 1234567890", fontSize = 9.sp, color = mutedTextDark)
                Text("IFSC/SWIFT: GCB123US6", fontSize = 9.sp, color = mutedTextDark)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Subtotal: ₹23,850.00", fontSize = 9.sp, color = mutedTextDark)
                Text("Discount: -₹600.00", fontSize = 9.sp, color = mutedTextDark)
                Text("Tax Standard (VAT 10%): ₹2,245.00", fontSize = 9.sp, color = mutedTextDark)
                Text("Shipping/Handling: ₹150.00", fontSize = 9.sp, color = mutedTextDark)
                Spacer(Modifier.height(4.dp))
                Text("Grand Total: ₹24,845.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Terms & Conditions", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(
            "Payment due within 30 days of invoice date. Late fees may apply. Goods remain property of Apex Global Solutions until paid in full.",
            fontSize = 8.sp,
            color = mutedText
        )
    }

    Spacer(Modifier.height(20.dp))

    // ── Action buttons ──
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Share PDF", fontWeight = FontWeight.Medium)
        }
        Button(
            onClick = { },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Download Invoice", color = whiteBg, fontWeight = FontWeight.Medium)
        }
    }
}