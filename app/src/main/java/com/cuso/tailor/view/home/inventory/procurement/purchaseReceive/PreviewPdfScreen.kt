@file:Suppress("unused", "unusedVariable", "AssignedValueIsNeverUsed")

package com.cuso.tailor.view.home.inventory.procurement.purchaseReceive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens

@Composable
fun PreviewPdfScreen(
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onConvertToBillClick: () -> Unit = {},
    onViewJournalEntryClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Theme and palette definitions
    val pageBg = Color(0xFFF8FAFC)
    val cardBg = Color(0xFFFFFFFF)
    val brandRed = Color(0xFFE11D48)
    val brandIndigo = Color(0xFF4338CA)
    val textMain = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)
    val textLight = Color(0xFF94A3B8)
    val borderColor = Color(0xFFE2E8F0)
    val subCardBg = Color(0xFFF8FAFC)
    val greenSuccess = Color(0xFF16A34A)
    val greenBgLight = Color(0xFFDCFCE7)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // Clean Top Bar with Title and Dismiss Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview PDF",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textMuted
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // =================================================================
            // 1. PO SUB-HEADER & ACTION CONTROLS
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
            ) {
                // ── Header Row: PO Number, Status Badge & Menu ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PO-88995",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Pill-shaped PAID Badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = "PAID",
                                color = Color(0xFF059669),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Purchase Orders / Bill-88995",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── Action Buttons Row ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Edit Button
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF334155)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }

                    // 2. Preview PDF Button
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF334155)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preview PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }

                    // 3. Download Icon Button
                    Surface(
                        onClick = { },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download",
                                modifier = Modifier.size(17.dp),
                                tint = Color(0xFF334155)
                            )
                        }
                    }

                    // 4. Convert to Bill Button
                    Button(
                        onClick = onConvertToBillClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4338CA)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Convert to Bill",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // 2. INVOICE DOCUMENT PAPER SHEET
            // =================================================================
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Brand on left, Invoice Info on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(brandRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.White)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RELDA",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = brandRed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "123 Innovation Way, Tech Park, San Francisco, CA 94105, USA\nEmail: finance@apexglobal.com\nPhone: +1 (415) 555-0123",
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = textMuted
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text(
                                text = "INVOICE",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Invoice No: INV-2024-001",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Text(
                                text = "Invoice Date: Oct 28, 2024",
                                fontSize = 10.sp,
                                color = textMuted
                            )
                            Text(
                                text = "Due Date: Nov 25, 2024",
                                fontSize = 10.sp,
                                color = textMuted
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Currency Selector Pill
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, borderColor),
                                color = subCardBg
                            ) {
                                Text(
                                    text = "$  /  ₹  /  €  /  AED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TAX REG Header Card
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subCardBg,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(60.dp)) {
                                Text("TAX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted)
                                Text("REG:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted)
                            }

                            Column {
                                Text(
                                    text = "GST/VAT/ABN/EIN :",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMain
                                )
                                Text(
                                    text = "GB123456789/US987654321",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMain
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // BILL TO Container
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, borderColor),
                        color = cardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "BILL TO:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandIndigo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acme Corp International",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "456 Business Ave, London, EC1A 1BB, UK\nPhone: +44 20 7946 0958\nEmail: accounts@acmecorp.com",
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SHIP TO Container
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, borderColor),
                        color = cardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SHIP TO:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandIndigo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acme Corp Warehouse",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "789 Logistics Blvd, Manchester, M1 1AA, UK",
                                fontSize = 10.sp,
                                color = textMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order ID: ORD-9876",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textMain
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "P-2024-Q4",
                                        fontSize = 9.sp,
                                        color = textMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // LINE ITEMS & SERVICES Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LINE ITEMS & SERVICES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "3 Items",
                            fontSize = 10.sp,
                            color = textLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item 1
                    PdfLineItemCard(
                        title = "Enterprise ERP Software License",
                        sub = "(Annual subscription)",
                        sku = "SKU-ERP-001",
                        totalPrice = "₹11,340.00",
                        tax = "Tax: 10%",
                        qty = "10",
                        unitPrice = "₹1,200.00",
                        discount = "₹800.00"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item 2
                    PdfLineItemCard(
                        title = "Implementation Services",
                        sub = "(50 Hours)",
                        sku = "SKU-SERV-IMP",
                        totalPrice = "₹8,250.00",
                        tax = "Tax: 10%",
                        qty = "50",
                        unitPrice = "₹150.00",
                        discount = "—"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item 3
                    PdfLineItemCard(
                        title = "Custom Module Development",
                        sub = "",
                        sku = "SKU-DEV-CUST",
                        totalPrice = "₹3,850.00",
                        tax = "Tax: 10%",
                        qty = "1",
                        unitPrice = "₹3,500.00",
                        discount = "—"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bank & UPI Payment Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, borderColor),
                        color = cardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Method: Bank Transfer / Card / UPI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMain
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bank: Global Commerce Bank\nA/C: 123456789012\nIFSC/SWIFT: GCBU0123XXX",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = textMuted
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    tint = textMain,
                                    modifier = Modifier.size(46.dp)
                                )
                                Text(
                                    text = "UPI QR Pay",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cost Calculation Breakdown
                    PdfCostRow(label = "Subtotal:", value = "₹23,050.00")
                    PdfCostRow(label = "Discount:", value = "-₹800.00", isGreen = true)
                    PdfCostRow(label = "Tax Breakdown (VAT 10%):", value = "₹2,245.00")
                    PdfCostRow(label = "Shipping/Handling:", value = "₹150.00")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grand Total Container
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        color = subCardBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GRAND TOTAL:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Text(
                                text = "₹24,845.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Terms and Conditions
                    Text(
                        text = "TERMS & CONDITIONS:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Payment due within 30 days of invoice date. Late fees may apply. Goods remain property of Apex Global Solutions until paid in full.",
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        color = textMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Footer with Digital Signature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Created with auto Invoice",
                                fontSize = 9.sp,
                                color = textLight
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = textLight
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Signature",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Cursive,
                                color = Color(0xFF2563EB)
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(90.dp),
                                thickness = 2.dp,
                                color = borderColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AUTHORIZED SIGNATURE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = textLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // =================================================================
            // 3. ACCOUNTING IMPACT SECTION
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(tokens.screenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCOUNTING IMPACT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Double-entry",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Double Entry Table Container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, borderColor),
                    color = cardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(subCardBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ACCOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "DC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "CR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = borderColor)

                        // Row 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts Receivable",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMain,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "450",
                                fontSize = 11.sp,
                                color = textMain,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "0",
                                fontSize = 11.sp,
                                color = textLight,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = borderColor)

                        // Row 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Revenue",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMain,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "0",
                                fontSize = 11.sp,
                                color = textLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "450",
                                fontSize = 11.sp,
                                color = textMain,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Entry Status & View Journal Link Container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, borderColor),
                    color = cardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Entry: ",
                                fontSize = 10.sp,
                                color = textLight
                            )
                            Text(
                                text = "DR0014",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                            Text(
                                text = "  |  Date: ",
                                fontSize = 10.sp,
                                color = textLight
                            )
                            Text(
                                text = "02 Mar 2026",
                                fontSize = 10.sp,
                                color = textMain
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "Posted",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "[View Journal Entry]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.clickable { onViewJournalEntryClick() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// =============================================================================
// HELPER COMPOSABLES
// =============================================================================

@Composable
fun PdfLineItemCard(
    title: String,
    sub: String,
    sku: String,
    totalPrice: String,
    tax: String,
    qty: String,
    unitPrice: String,
    discount: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (sub.isNotEmpty()) {
                        Text(
                            text = sub,
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "HSN/SKU:  $sku",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = totalPrice,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = tax,
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-box containing Qty, Unit Price, and Discount
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Qty", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(qty, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column {
                        Text("Unit Price", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(unitPrice, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Discount", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(discount, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}

@Composable
fun PdfCostRow(
    label: String,
    value: String,
    isGreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isGreen) Color(0xFF16A34A) else Color(0xFF0F172A)
        )
    }
}