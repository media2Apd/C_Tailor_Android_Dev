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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.modelGray
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.TitleBar

@Composable
fun PreviewPdfScreen(
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onConvertToBillClick: () -> Unit = {},
    onViewJournalEntryClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Preview PDF", onClose = onClose)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // ── Section 1: PO Header & Action Controls ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PO-88995",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))

                        Surface(
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = primary_light
                        ) {
                            Text(
                                text = "PAID",
                                color = Primary,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Purchase Orders / Bill-88995",
                    fontSize = tokens.caption,
                    color = mutedText
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit Button
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = modelGray,
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(tokens.iconSize * 0.75f),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Preview PDF Button
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = modelGray,
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(tokens.iconSize * 0.75f),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preview PDF",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Download Button
                    Surface(
                        onClick = { },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        color = modelGray,
                        modifier = Modifier.size(tokens.buttonHeight * 0.85f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download",
                                modifier = Modifier.size(tokens.iconSize * 0.85f),
                                tint = TextSecondary
                            )
                        }
                    }

                    // Convert to Bill Button
                    Button(
                        onClick = onConvertToBillClick,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .height(tokens.buttonHeight * 0.85f)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Convert to Bill",
                            fontSize = tokens.caption,
                            color = whiteBg,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 1.4f))

            // ── Section 2: Invoice Document Paper Sheet ──
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, grey_border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
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
                                        .size(tokens.iconSize * 1.2f)
                                        .clip(CircleShape)
                                        .background(Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.iconSize * 0.5f)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(whiteBg)
                                    )
                                }
                                Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))
                                Text(
                                    text = "RELDA",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary
                                )
                            }

                            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                            Text(
                                text = "123 Innovation Way, Tech Park, San Francisco, CA 94105, USA\nEmail: finance@apexglobal.com\nPhone: +1 (415) 555-0123",
                                fontSize = tokens.caption,
                                lineHeight = tokens.caption * 1.4f,
                                color = mutedText
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text(
                                text = "INVOICE",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Invoice No: INV-2024-001",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Invoice Date: Oct 28, 2024",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Text(
                                text = "Due Date: Nov 25, 2024",
                                fontSize = tokens.caption,
                                color = mutedText
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Currency Selector Pill
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.25f),
                                border = BorderStroke(1.dp, grey_border),
                                color = modelGray
                            ) {
                                Text(
                                    text = "$  /  ₹  /  €  /  AED",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.4f))

                    // TAX REG Header Card
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                        color = modelGray,
                        border = BorderStroke(1.dp, grey_border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(60.dp)) {
                                Text("TAX", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
                                Text("REG:", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
                            }

                            Column {
                                Text(
                                    text = "GST/VAT/ABN/EIN :",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "GB123456789/US987654321",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    // BILL TO Container
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        color = whiteBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
                            Text(
                                text = "BILL TO:",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acme Corp International",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "456 Business Ave, London, EC1A 1BB, UK\nPhone: +44 20 7946 0958\nEmail: accounts@acmecorp.com",
                                fontSize = tokens.caption,
                                lineHeight = tokens.caption * 1.4f,
                                color = mutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    // SHIP TO Container
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        color = whiteBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
                            Text(
                                text = "SHIP TO:",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acme Corp Warehouse",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "789 Logistics Blvd, Manchester, M1 1AA, UK",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order ID: ORD-9876",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.25f),
                                    color = modelGray
                                ) {
                                    Text(
                                        text = "P-2024-Q4",
                                        fontSize = tokens.caption,
                                        color = mutedText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.6f))

                    // Line Items Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LINE ITEMS & SERVICES",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "3 Items",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                    PdfLineItemCard(
                        title = "Enterprise ERP Software License",
                        sub = "(Annual subscription)",
                        sku = "SKU-ERP-001",
                        totalPrice = "₹11,340.00",
                        tax = "Tax: 10%",
                        qty = "10",
                        unitPrice = "₹1,200.00",
                        discount = "₹800.00",
                        tokens = tokens
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                    PdfLineItemCard(
                        title = "Implementation Services",
                        sub = "(50 Hours)",
                        sku = "SKU-SERV-IMP",
                        totalPrice = "₹8,250.00",
                        tax = "Tax: 10%",
                        qty = "50",
                        unitPrice = "₹150.00",
                        discount = "—",
                        tokens = tokens
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                    PdfLineItemCard(
                        title = "Custom Module Development",
                        sub = "",
                        sku = "SKU-DEV-CUST",
                        totalPrice = "₹3,850.00",
                        tax = "Tax: 10%",
                        qty = "1",
                        unitPrice = "₹3,500.00",
                        discount = "—",
                        tokens = tokens
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.4f))

                    // Bank & UPI Payment Card
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, grey_border),
                        color = whiteBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(tokens.extraPadding * 1.2f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Method: Bank Transfer / Card / UPI",
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bank: Global Commerce Bank\nA/C: 123456789012\nIFSC/SWIFT: GCBU0123XXX",
                                    fontSize = tokens.caption,
                                    lineHeight = tokens.caption * 1.4f,
                                    color = mutedText
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(tokens.iconSize * 2f)
                                )
                                Text(
                                    text = "UPI QR Pay",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = mutedText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.4f))

                    // Cost Calculation Breakdown
                    PdfCostRow(label = "Subtotal:", value = "₹23,050.00", tokens = tokens)
                    PdfCostRow(label = "Discount:", value = "-₹800.00", isPrimaryHighlight = true, tokens = tokens)
                    PdfCostRow(label = "Tax Breakdown (VAT 10%):", value = "₹2,245.00", tokens = tokens)
                    PdfCostRow(label = "Shipping/Handling:", value = "₹150.00", tokens = tokens)

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                    // Grand Total Container
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                        border = BorderStroke(1.dp, dividerColor),
                        color = modelGray,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GRAND TOTAL:",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹24,845.00",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.4f))

                    // Terms and Conditions
                    Text(
                        text = "TERMS & CONDITIONS:",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = mutedText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Payment due within 30 days of invoice date. Late fees may apply. Goods remain property of Apex Global Solutions until paid in full.",
                        fontSize = tokens.caption,
                        lineHeight = tokens.caption * 1.4f,
                        color = mutedText
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding * 1.6f))

                    // Footer with Digital Signature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Created with auto Invoice",
                                fontSize = tokens.caption,
                                color = iconMuted
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(tokens.iconSize * 0.6f),
                                tint = iconMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Signature",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Cursive,
                                color = Primary
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(90.dp),
                                thickness = 2.dp,
                                color = dividerColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AUTHORIZED SIGNATURE",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = iconMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 1.6f))

            // ── Section 3: Accounting Impact ──
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
                    Text(
                        text = "ACCOUNTING IMPACT",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.25f),
                        color = modelGray
                    ) {
                        Text(
                            text = "Double-entry",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = mutedText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                // Double Entry Table Container
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(modelGray)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ACCOUNT",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = mutedText,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "DC",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = mutedText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "CR",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = mutedText,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = dividerColor)

                        // Row 1: Accounts Receivable
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts Receivable",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "450",
                                fontSize = tokens.label,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "0",
                                fontSize = tokens.label,
                                color = iconMuted,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = dividerColor)

                        // Row 2: Revenue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Revenue",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "0",
                                fontSize = tokens.label,
                                color = iconMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "450",
                                fontSize = tokens.label,
                                color = TextPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Entry Status & View Journal Link Container
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg,
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
                                fontSize = tokens.caption,
                                color = iconMuted
                            )
                            Text(
                                text = "DR0014",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "  |  Date: ",
                                fontSize = tokens.caption,
                                color = iconMuted
                            )
                            Text(
                                text = "02 Mar 2026",
                                fontSize = tokens.caption,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.25f),
                                color = primary_light
                            ) {
                                Text(
                                    text = "Posted",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "[View Journal Entry]",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.clickable { onViewJournalEntryClick() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding * 2f))
        }
    }
}

// ── Line Item Card ──
@Composable
fun PdfLineItemCard(
    title: String,
    sub: String,
    sku: String,
    totalPrice: String,
    tax: String,
    qty: String,
    unitPrice: String,
    discount: String,
    tokens: AppDesignTokens
) {
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
        border = BorderStroke(1.dp, grey_border),
        color = whiteBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (sub.isNotEmpty()) {
                        Text(
                            text = sub,
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    Text(
                        text = "HSN/SKU:  $sku",
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = totalPrice,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = tax,
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                color = modelGray,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Qty", fontSize = tokens.caption, color = iconMuted)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(qty, fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Unit Price", fontSize = tokens.caption, color = iconMuted)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(unitPrice, fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Discount", fontSize = tokens.caption, color = iconMuted)
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(discount, fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

// ── Cost Breakdown Row ──
@Composable
fun PdfCostRow(
    label: String,
    value: String,
    isPrimaryHighlight: Boolean = false,
    tokens: AppDesignTokens
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
            fontSize = tokens.label,
            color = mutedText
        )
        Text(
            text = value,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = if (isPrimaryHighlight) Primary else TextPrimary
        )
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
    val tokens = LocalAppTokens.current

    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
        border = BorderStroke(1.dp, grey_border),
        color = whiteBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
            // Header: Title, Subtitle, and Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (sub.isNotEmpty()) {
                        Text(
                            text = sub,
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    Text(
                        text = "HSN/SKU:  $sku",
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = totalPrice,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = tax,
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

            // Sub-box containing Qty, Unit Price, and Discount
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.3f),
                color = modelGray,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Qty",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = qty,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Unit Price",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = unitPrice,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Discount",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = discount,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
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
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.label,
            color = mutedText
        )
        Text(
            text = value,
            fontSize = tokens.label,
            fontWeight = FontWeight.SemiBold,
            color = if (isGreen) Primary else TextPrimary
        )
    }
}