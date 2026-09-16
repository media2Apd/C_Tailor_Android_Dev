package com.cuso.mobile.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

@Composable
fun PurchaseDetailScreen(
    onClose: () -> Unit,
    onPreviewPdf: () -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onRecordPayment: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Purchase Detail", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // ── Section 1: Header + Action Buttons ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(tokens.screenPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bill-88995", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
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

                    Spacer(Modifier.height(2.dp))
                    Text("Order number - 245623", fontSize = tokens.caption, color = mutedText)

                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Preview PDF Button
                        OutlinedButton(
                            onClick = onPreviewPdf,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(sectionBorder)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                        ) {
                            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = title_color, modifier = Modifier.size(tokens.iconSize * 0.85f))
                            Spacer(Modifier.width(4.dp))
                            Text("Preview PDF", fontSize = tokens.caption, color = title_color)
                        }

                        // Download PDF Icon Button
                        Box(
                            modifier = Modifier
                                .size(tokens.buttonHeight * 0.85f)
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .clickable { onDownloadPdf() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = "Download", tint = title_color, modifier = Modifier.size(tokens.iconSize * 0.9f))
                        }

                        Spacer(Modifier.weight(1f))

                        // Record Payment Button
                        Button(
                            onClick = onRecordPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                        ) {
                            Text("Record Payment", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = whiteBg)
                        }
                    }
                }
            }

            // ── Section 2: Purchase Details ──
            item {
                DetailCardContainer(title = "PURCHASE DETAILS", tokens = tokens) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vendor", fontSize = tokens.label, color = mutedText)
                            Text("Emily Johnson", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bill Date", fontSize = tokens.label, color = mutedText)
                            Text("Emily Johnson", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Due Date", fontSize = tokens.label, color = mutedText)
                            Text("5 Aug 2025", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Balance Due", fontSize = tokens.label, color = mutedText)
                            Text("Main Warehouse", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }
                }
            }

            // ── Section 3: Items ──
            item {
                DetailCardContainer(title = "ITEMS", tokens = tokens) {
                    listOf(
                        Triple("Shirt", "88940", "₹50,000"),
                        Triple("Pant", "88940", "₹50,000")
                    ).forEach { (itemName, qty, amount) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(itemName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                    Text("Account: Product", fontSize = tokens.caption, color = mutedText)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Amount", fontSize = tokens.label, color = mutedText)
                                    Text(amount, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Qty:  $qty", fontSize = tokens.caption, color = mutedText)
                                Text("Rate:  ₹50,000,00.00", fontSize = tokens.caption, color = mutedText)
                            }
                        }
                        HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
                        Spacer(Modifier.height(10.dp))
                    }

                    // Financial summary
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = tokens.caption, color = TextSecondary)
                        Text("₹11,497.00", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Due", fontSize = tokens.caption, color = TextSecondary)
                        Text("₹11,497.00", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                        Text("₹11,997.00", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }

            // ── Section 4: Transaction History ──
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TRANSACTION HISTORY", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
                        Text("2 Shipments recorded", fontSize = tokens.caption, color = mutedText)
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    listOf(
                        Triple("TR-8845-1", "Oct 14, 2023", "Success"),
                        Triple("TR-8845-2", "Oct 18, 2023", "Pending")
                    ).forEach { (trCode, date, status) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding * 1.2f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(trCode, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                            .background(if (status == "Success") greenBg else activity_purple_bg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(status, fontSize = tokens.label, color = if (status == "Success") darkGreenBg else Primary)
                                    }
                                }
                                Text(date, fontSize = tokens.caption, color = mutedText)

                                Spacer(Modifier.height(tokens.extraPadding))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mode:  Cash", fontSize = tokens.caption, color = TextSecondary)
                                    Text("₹10,000", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCardContainer(
    title: String,
    tokens: AppDesignTokens,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(title, fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            content()
        }
    }
}