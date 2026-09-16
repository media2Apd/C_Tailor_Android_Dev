package com.cuso.mobile.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.TitleBar

@Composable
fun PreviewPdfScreen(
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onConvertToBillClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TitleBar(
                title = "Preview PDF",
                onClose = onClose
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // PO Subheader & Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "PO-88995", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(10.dp), color = greenBg) {
                            Text(
                                text = "PAID",
                                color = darkGreenBg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = iconMuted)
                }

                Text(text = "Purchase Orders / Bill-88995", fontSize = 11.sp, color = mutedText)

                Spacer(Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, grey_border),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp, color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, grey_border),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Preview PDF", fontSize = 11.sp, color = TextPrimary)
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(34.dp)
                            .border(1.dp, grey_border, RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                    }

                    Button(
                        onClick = onConvertToBillClick,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Convert to Bill", fontSize = 11.sp, color = whiteBg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // White Sheet Container for Document
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, grey_border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Invoice Brand Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFE11D48), CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("RELDA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("123 Innovation Way, Tech Park, San\nFrancisco, CA 94105, USA\nEmail: finance@apexglobal.com\nPhone: +1 (415) 555-0123", fontSize = 10.sp, color = close_color)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("INVOICE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("Invoice No: INV-2024-001", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Invoice Date: Oct 28, 2024", fontSize = 10.sp, color = close_color)
                            Text("Due Date: Nov 25, 2024", fontSize = 10.sp, color = close_color)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // TAX REG Box
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text("TAX REG:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = darkGreenBg)
                            Spacer(Modifier.width(6.dp))
                            Text("GST/VAT/ABN/EIN: GB123456789/US987654321", fontSize = 10.sp, color = darkGreenBg)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // BILL TO Box
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, grey_border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("BILL TO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                            Text("Acme Corp International", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("456 Business Ave, London, EC1A 1BB, UK\nPhone: +44 20 7946 0958\nEmail: accounts@acmecorp.com", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // SHIP TO Box
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, grey_border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SHIP TO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                            Text("Acme Corp Warehouse", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("789 Logistics Blvd, Manchester, M1 1AA, UK", fontSize = 10.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Order ID: ORD-9876", fontSize = 10.sp, color = close_color)
                                Spacer(Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = badgeGrey) {
                                    Text("P-2024-Q4", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Line Items & Services
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LINE ITEMS & SERVICES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = close_color)
                        Text("3 items", fontSize = 10.sp, color = mutedText)
                    }

                    Spacer(Modifier.height(8.dp))

                    PdfLineItemCard("Enterprise ERP Software License", "(Annual subscription)", "SKU-ERP-001", "₹11,340.00", "10", "₹1,200.00", "₹800.00")
                    Spacer(Modifier.height(8.dp))
                    PdfLineItemCard("Implementation Services", "(50 Hours)", "SKU-SERV-IMP", "₹8,250.00", "50", "₹150.00", "—")
                    Spacer(Modifier.height(8.dp))
                    PdfLineItemCard("Custom Module Development", "", "SKU-DEV-CUST", "₹3,850.00", "1", "₹3,500.00", "—")

                    Spacer(Modifier.height(14.dp))

                    // Bank & Payment QR Details
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, grey_border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Method: Bank Transfer / Card / UPI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text("Bank: Global Commerce Bank\nA/C: 123456789012\nIFSC/SWIFT: GCBU0123XXX", fontSize = 10.sp, color = close_color)
                            }

                            // UPI QR Code Placeholder
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(48.dp))
                                Text("UPI QR Pay", fontSize = 8.sp, color = mutedText)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Cost Breakdown
                    PdfCostRow("Subtotal:", "₹23,050.00")
                    PdfCostRow("Discount:", "-₹800.00", isGreen = true)
                    PdfCostRow("Tax Breakdown (VAT 10%):", "₹2,245.00")
                    PdfCostRow("Shipping/Handling:", "₹150.00")

                    Spacer(Modifier.height(8.dp))

                    // Grand Total Box
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GRAND TOTAL:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("₹24,845.00", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("TERMS & CONDITIONS:\nPayment due within 30 days of invoice date. Late fees may apply.", fontSize = 9.sp, color = close_color)

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Created with auto Invoice", fontSize = 9.sp, color = mutedText)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Signature", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                            HorizontalDivider(modifier = Modifier.width(100.dp), color = dividerColor)
                            Text("AUTHORIZED SIGNATURE", fontSize = 8.sp, color = mutedText)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Accounting Impact Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(tokens.screenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ACCOUNTING IMPACT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Surface(shape = RoundedCornerShape(4.dp), color = badgeGrey) {
                        Text("Double-entry", fontSize = 10.sp, color = close_color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Table Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = close_color, modifier = Modifier.weight(2f))
                    Text("DC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = close_color, modifier = Modifier.weight(1f))
                    Text("CR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = close_color, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 6.dp))

                // Row 1
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Accounts Receivable", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(2f))
                    Text("450", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text("0", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(6.dp))

                // Row 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Revenue", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(2f))
                    Text("0", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text("450", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                // Entry Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Entry: DR0014 | Date: 02 Mar 2026", fontSize = 10.sp, color = mutedText)
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                            Text("Posted", fontSize = 9.sp, color = Primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text("[View Journal Entry]", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun PdfLineItemCard(
    title: String,
    sub: String,
    sku: String,
    price: String,
    qty: String,
    unitPrice: String,
    discount: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, grey_border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (sub.isNotEmpty()) Text(sub, fontSize = 9.sp, color = close_color)
                    Text("HSN/SKU: $sku", fontSize = 9.sp, color = mutedText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Tax: 10%", fontSize = 9.sp, color = mutedText)
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Qty", fontSize = 9.sp, color = mutedText)
                        Text(qty, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Unit Price", fontSize = 9.sp, color = mutedText)
                        Text(unitPrice, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Discount", fontSize = 9.sp, color = mutedText)
                        Text(discount, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun PdfCostRow(label: String, value: String, isGreen: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = close_color)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isGreen) darkGreenBg else TextPrimary)
    }
}