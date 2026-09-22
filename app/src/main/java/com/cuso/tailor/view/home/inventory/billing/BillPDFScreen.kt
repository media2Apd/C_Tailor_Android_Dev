@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.billing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar

data class LineItemDetail(
    val title: String,
    val subtitle: String? = null,
    val sku: String,
    val taxRate: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double? = null,
    val totalAmount: Double
)

data class AccountingEntry(
    val accountName: String,
    val debit: Double,
    val credit: Double
)

@Composable
fun BillPdfPreviewScreen(
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onConvertToBillClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val lineItems = listOf(
        LineItemDetail(
            title = "Enterprise ERP Software License",
            subtitle = "(Annual subscription)",
            sku = "SKU-ERP-001",
            taxRate = "10%",
            quantity = 10,
            unitPrice = 1200.0,
            discount = 800.0,
            totalAmount = 11340.0
        ),
        LineItemDetail(
            title = "Implementation Services",
            subtitle = "(50 Hours)",
            sku = "SKU-SERV-IMP",
            taxRate = "10%",
            quantity = 50,
            unitPrice = 150.0,
            discount = null,
            totalAmount = 8250.0
        ),
        LineItemDetail(
            title = "Custom Module Development",
            sku = "SKU-DEV-CUST",
            taxRate = "10%",
            quantity = 1,
            unitPrice = 3500.0,
            discount = null,
            totalAmount = 3850.0
        )
    )

    val accountingEntries = listOf(
        AccountingEntry("Accounts Receivable", 450.0, 0.0),
        AccountingEntry("Revenue", 0.0, 450.0)
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleBar(title = "Preview PDF", onClose = onClose)
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
                .padding(vertical = tokens.extraPadding)
        ) {

                Column(Modifier.fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = 20.dp)) {
                    // Document Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PO-88995",
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                .background(greenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PAID",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = greentext
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize * 1.1f)
                        )
                    }

                    Text(
                        text = "Purchase Orders / Bill-88995",
                        fontSize = tokens.caption,
                        color = close_color,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Top Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.fieldHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, grey_border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = TextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize * 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .weight(1.3f)
                                .height(tokens.fieldHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, grey_border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = TextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize * 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Preview",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .size(tokens.fieldHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, grey_border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = TextSecondary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }

                        Button(
                            onClick = onConvertToBillClick,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(tokens.fieldHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Convert to Bill",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                }
                Spacer(Modifier.height(tokens.screenPadding))

                Spacer(modifier = Modifier.height(tokens.screenPadding))
                Column(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {

                    // Main Document Card Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                            .border(
                                1.dp,
                                sectionBorder,
                                RoundedCornerShape(tokens.cardCornerRadius)
                            )
                            .padding(tokens.screenPadding)
                    ) {
                        // Header (Company Logo/Details & Invoice Details)
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
                                            .background(redText)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RELDA",
                                        fontSize = tokens.h2,
                                        fontWeight = FontWeight.Bold,
                                        color = redText
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "123 Innovation Way, Tech Park, San Francisco, CA 94105, USA\nEmail: finance@apexglobal.com\nPhone: +1 (415) 555-0123",
                                    fontSize = tokens.caption,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            Column(
                                modifier = Modifier.weight(0.9f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "INVOICE",
                                    fontSize = tokens.h2,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Invoice No: ",
                                        fontSize = tokens.caption,
                                        color = close_color
                                    )
                                    Text(
                                        text = "INV-2024-001",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Invoice Date: ",
                                        fontSize = tokens.caption,
                                        color = close_color
                                    )
                                    Text(
                                        text = "Oct 28, 2024",
                                        fontSize = tokens.caption,
                                        color = TextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Due Date: ",
                                        fontSize = tokens.caption,
                                        color = close_color
                                    )
                                    Text(
                                        text = "Nov 25, 2024",
                                        fontSize = tokens.caption,
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(light_blue)
                                        .border(1.dp, light_blue_border, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$  |  ₹  |  €  |  AED",
                                        fontSize = tokens.label,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Tax Reg Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    light_blue_border,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TAX REG:",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = close_color
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GST/VAT/ABN/EIN: GB123456789/US987654321",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Bill To Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(headerBg)
                                .border(
                                    1.dp,
                                    sectionBorder,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(tokens.extraPadding)
                        ) {
                            Text(
                                text = "BILL TO:",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acme Corp International",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "456 Business Ave, London, EC1A 1BB, UK",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = "Phone: +44 20 7946 0958",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = "Email: accounts@acmecorp.com",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                        // Ship To Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerBg)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(
                                    1.dp,
                                    sectionBorder,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(tokens.extraPadding)
                        ) {
                            Text(
                                text = "SHIP TO:",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = close_color
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Acme Corp Warehouse",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "789 Logistics Blvd, Manchester, M1 1AA, UK",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order ID: ORD-9876",
                                    fontSize = tokens.caption,
                                    color = close_color
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(light_blue)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "P-2024-Q4",
                                        fontSize = tokens.label,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.screenPadding))

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
                                color = close_color,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "3 Items",
                                fontSize = tokens.label,
                                color = close_color
                            )
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                        // Line Items Cards
                        lineItems.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .border(
                                        1.dp,
                                        sectionBorder,
                                        RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                    )
                                    .background(headerBg)
                                    .padding(tokens.extraPadding)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (item.subtitle != null) {
                                            Text(
                                                text = item.subtitle,
                                                fontSize = tokens.caption,
                                                color = close_color
                                            )
                                        }
                                        Text(
                                            text = "HSN/SKU: ${item.sku}",
                                            fontSize = tokens.label,
                                            color = close_color
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${"%,.2f".format(item.totalAmount)}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Tax: ${item.taxRate}",
                                            fontSize = tokens.label,
                                            color = close_color
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Metric pill row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                        .background(modelBg)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Qty", fontSize = tokens.label, color = close_color)
                                        Text(
                                            text = "${item.quantity}",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Unit Price",
                                            fontSize = tokens.label,
                                            color = close_color
                                        )
                                        Text(
                                            text = "₹${"%,.2f".format(item.unitPrice)}",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Discount",
                                            fontSize = tokens.label,
                                            color = close_color
                                        )
                                        Text(
                                            text = if (item.discount != null) "₹${
                                                "%,.2f".format(
                                                    item.discount
                                                )
                                            }" else "—",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Payment Method Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(
                                    1.dp,
                                    sectionBorder,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(tokens.extraPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Method: Bank Transfer / Card / UPI",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bank: Global Commerce Bank\nA/C: 123456789012\nIFSC/SWIFT: GCBU0123XXX",
                                    fontSize = tokens.label,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.5.dp, TextPrimary, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "QR",
                                        fontSize = tokens.h2,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "UPI QR Pay",
                                    fontSize = tokens.label,
                                    color = close_color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Subtotal Breakdown
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal:",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "₹23,050.00",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Discount:",
                                    fontSize = tokens.bodySmall,
                                    color = greentext
                                )
                                Text(
                                    text = "-₹800.00",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = greentext
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tax Breakdown (VAT 10%):",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "₹2,245.00",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Shipping/Handling:",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "₹150.00",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Grand Total Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue)
                                .border(
                                    1.dp,
                                    light_blue_border,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRAND TOTAL:",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "₹24,845.00",
                                    fontSize = tokens.h2,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        Text(
                            text = "TERMS & CONDITIONS:\nPayment due within 30 days of invoice date. Late fees may apply. Goods remain property of Apex Global Solutions until paid in full.",
                            fontSize = tokens.label,
                            color = close_color,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(tokens.screenPadding))

                        // Signature Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Signature",
                                    fontFamily = FontFamily.Cursive,
                                    fontSize = 24.sp,
                                    color = Primary
                                )
                                HorizontalDivider(
                                    modifier = Modifier.width(140.dp),
                                    color = dividerColor,
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "AUTHORIZED SIGNATURE",
                                    fontSize = tokens.label,
                                    color = close_color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Created with auto Invoice",
                            fontSize = tokens.label,
                            color = close_color
                        )
                    }
                }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // Accounting Impact Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(tokens.screenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCOUNTING IMPACT",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(light_blue)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Double-entry",
                            fontSize = tokens.label,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Table Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ACCOUNT",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = close_color,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "DC",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = close_color,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "CR",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = close_color,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    accountingEntries.forEachIndexed { index, entry ->
                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.accountName,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "${entry.debit.toInt()}",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${entry.credit.toInt()}",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Journal Entry Footer Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Entry: ", fontSize = tokens.caption, color = close_color)
                        Text(text = "DR0014", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "| Date: 02 Mar 2026", fontSize = tokens.caption, color = close_color)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(light_blue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Posted",
                                fontSize = tokens.label,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "[View Journal Entry]",
                        fontSize = tokens.caption,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))
        }
    }
}