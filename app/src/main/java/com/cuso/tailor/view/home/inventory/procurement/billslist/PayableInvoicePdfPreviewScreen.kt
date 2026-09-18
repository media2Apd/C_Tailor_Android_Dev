package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*

@Composable
fun PayableInvoicePdfPreviewScreen(
    onClose: () -> Unit,
    onEdit: () -> Unit = {},
    onConvertToBill: () -> Unit = {},
    onDownloadPdf: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Preview PDF", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
        ) {
            // Header Info & Actions
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PO-88995", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                .background(greenBg)
                                .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
                        ) {
                            Text("PAID", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = darkGreenBg)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { }, modifier = Modifier.size(tokens.iconSize * 1.3f)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText)
                        }
                    }

                    Text("Purchase Orders / Bill-88995", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedActionButton(tokens = tokens, label = "Edit", icon = Icons.Outlined.Edit, onClick = onEdit)
                        OutlinedActionButton(tokens = tokens, label = "Preview PDF", icon = Icons.Outlined.Visibility, onClick = { })

                        Box(
                            modifier = Modifier
                                .size(tokens.buttonHeight * 0.85f)
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onDownloadPdf, modifier = Modifier.size(tokens.iconSize * 1.2f)) {
                                Icon(Icons.Outlined.Download, contentDescription = "Download", tint = title_color, modifier = Modifier.size(tokens.iconSize * 0.9f))
                            }
                        }

                        Button(
                            onClick = onConvertToBill,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                        ) {
                            Text("Convert to Bill", fontSize = tokens.caption, color = whiteBg, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Rendered Invoice Sheet Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding * 1.2f)) {
                        // Company Logo + INVOICE header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(redBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("R", color = redText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("RELDA", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = redText)
                                    Text("Billing receipt", fontSize = tokens.caption, color = mutedText)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("INVOICE", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
                                Text("INVOICE NO: INV-2024-001", fontSize = tokens.label, color = mutedText)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        // Bill to & Ship to Cards
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.extraPadding * 1.2f)
                        ) {
                            Column {
                                Text("BILL TO:", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = Primary)
                                Text("Acme Corp International", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                Text("456 Business Ave, London, EC1A 1BB, UK", fontSize = tokens.caption, color = mutedText)
                                Text("Phone: +44 20 7946 0958", fontSize = tokens.caption, color = mutedText)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.extraPadding * 1.2f)
                        ) {
                            Column {
                                Text("SHIP TO:", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = Primary)
                                Text("Acme Corp Warehouse", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                Text("789 Logistics Blvd, Manchester, M1 1AA, UK", fontSize = tokens.caption, color = mutedText)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                        // Line Items
                        Text("LINE ITEMS & SERVICES", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        listOf(
                            Triple("Enterprise ERP Software License", "Qty: 10 • Unit Price: ₹1,200.00", "₹11,340.00"),
                            Triple("Implementation Services (50 Hours)", "Qty: 50 • Unit Price: ₹150.00", "₹8,250.00"),
                            Triple("Custom Module Development", "Qty: 1 • Unit Price: ₹3,500.00", "₹3,850.00")
                        ).forEach { (name, sub, total) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .padding(tokens.extraPadding)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                        Text(sub, fontSize = tokens.caption, color = mutedText)
                                    }
                                    Text(total, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Financial totals
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = tokens.caption, color = mutedText)
                                Text("₹23,050.00", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:", fontSize = tokens.caption, color = complete_button_bg)
                                Text("-₹800.00", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = complete_button_bg)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tax Breakdown (VAT 10%):", fontSize = tokens.caption, color = mutedText)
                                Text("₹2,245.00", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Shipping/Handling:", fontSize = tokens.caption, color = mutedText)
                                Text("₹150.00", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                            }

                            Spacer(Modifier.height(tokens.extraPadding))

                            // Grand Total Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(activity_purple_bg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.2f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("GRAND TOTAL:", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                                    Text("₹24,845.00", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                        // Bank Details & Mini QR
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.extraPadding * 1.2f)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Method: Bank Transfer / Card / UPI", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Bank: Global Commerce Bank", fontSize = tokens.label, color = mutedText)
                                    Text("A/C: 123456789012", fontSize = tokens.label, color = mutedText)
                                    Text("IFSC/SWIFT: GCBUS33XXX", fontSize = tokens.label, color = mutedText)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .border(1.dp, title_border, RoundedCornerShape(6.dp))
                                        .background(light_grey),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("QR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = title_color)
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                        // Signature Block
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text("Signature", fontFamily = FontFamily.Cursive, fontSize = 24.sp, color = activity_purple)
                            Box(modifier = Modifier.width(120.dp).height(1.dp).background(title_color))
                            Spacer(Modifier.height(4.dp))
                            Text("AUTHORIZED SIGNATURE", fontSize = 9.sp, color = mutedText)
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        Text(
                            text = "Created with cuso invoice",
                            fontSize = tokens.label,
                            color = mutedText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // ── Section: Accounting Impact (Double Entry) ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ACCOUNTING IMPACT", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeGrey)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Double-entry", fontSize = tokens.label, color = mutedText)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Table
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(badgeGrey)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ACCOUNT", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                            Row {
                                Text("DC", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                                Spacer(Modifier.width(28.dp))
                                Text("CR", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = mutedText)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Accounts Receivable", fontSize = tokens.caption, color = title_color)
                            Row {
                                Text("450", fontSize = tokens.caption, color = title_color)
                                Spacer(Modifier.width(34.dp))
                                Text("0", fontSize = tokens.caption, color = title_color)
                            }
                        }
                        HorizontalDivider(color = grey_border.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Revenue", fontSize = tokens.caption, color = title_color)
                            Row {
                                Text("0", fontSize = tokens.caption, color = title_color)
                                Spacer(Modifier.width(34.dp))
                                Text("450", fontSize = tokens.caption, color = title_color)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Entry: DR0014   |   Date: 02 Mar 2026", fontSize = tokens.label, color = mutedText)
                            Text("[View Journal Entry]", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedActionButton(
    tokens: AppDesignTokens,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .border(width = 1.dp, color = sectionBorder, shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = title_color, modifier = Modifier.size(tokens.iconSize * 0.75f))
                Spacer(Modifier.width(4.dp))
            }
            Text(text = label, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
        }
    }
}