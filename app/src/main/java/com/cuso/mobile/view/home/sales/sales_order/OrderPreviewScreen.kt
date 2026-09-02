package com.cuso.mobile.view.home.sales.sales_order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.formatIndianNumber

@Composable
fun OrderPreviewScreen(
    orderData: OrderReviewData,
    onClose: () -> Unit = {},
    onConfirmOrder: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    // ── Side Attached Share Menu State ──
    var isShareMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // ── Accordion Expansion States ──
    var customerDetailsExpanded by rememberSaveable { mutableStateOf(true) }
    var orderInfoExpanded by rememberSaveable { mutableStateOf(true) }
    var measurementsSummaryExpanded by rememberSaveable { mutableStateOf(true) }
    var itemsPricingExpanded by rememberSaveable { mutableStateOf(true) }
    var chargesPaymentExpanded by rememberSaveable { mutableStateOf(true) }
    var paymentStatusExpanded by rememberSaveable { mutableStateOf(true) }
    var additionalNotesExpanded by rememberSaveable { mutableStateOf(true) }

    // ── Form & Payment States ──
    var collectedBy by rememberSaveable { mutableStateOf("Store Associate A") }
    var collectedByExpanded by remember { mutableStateOf(false) }

    var isFullAdvance by rememberSaveable { mutableStateOf(false) }
    var paymentAmountReceived by rememberSaveable { mutableStateOf("0") }
    var operationalNotes by rememberSaveable { mutableStateOf("") }

    // ── Financial Totals ──
    val subtotal = 6200.0
    val discountAmount = 620.0
    val taxAmount = 1004.40
    val grandTotal = subtotal - discountAmount + taxAmount
    val advancePaid = 2000.0
    val balanceDue = grandTotal - advancePaid

    Scaffold(
        topBar = {
            TitleBar(
                title = "Order Preview",
                onClose = onClose
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = 120.dp) // Bottom clearance for floating buttons
            ) {
                // ── Top Header Banner ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                ) {
                    Text(
                        text = "Order Preview & Invoice Summary",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Please review the order details and invoice summary below for ${orderData.fullName.ifBlank { "Rajesh Mehta" }}.",
                        fontSize = tokens.caption,
                        color = Color(0xFF64748B)
                    )
                }

                HorizontalDivider(color = dividerColor)

                // ─────────────────────────────────────────────────────────────
                // 1. CUSTOMER DETAILS
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Customer Details",
                    expanded = customerDetailsExpanded,
                    onHeaderClick = { customerDetailsExpanded = !customerDetailsExpanded }
                ) {
                    KeyValueRow(label = "Customer Name", value = orderData.fullName.ifBlank { "Rajesh Mehta" })
                    KeyValueRow(label = "Mobile", value = orderData.phone.ifBlank { "+91 98524 68719" })
                    KeyValueRow(label = "Email", value = "rajesh.mehta@example.com")
                    KeyValueRow(label = "Address", value = orderData.address.ifBlank { "Murai Mal No 8 Bakki-II" })
                    KeyValueRow(label = "Customer Type", value = "Individual")
                    KeyValueRow(label = "Garment Type", value = orderData.garments.firstOrNull()?.categoryName ?: "Wedding Shirt")
                }

                // ─────────────────────────────────────────────────────────────
                // 2. ORDER INFORMATION
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Order Information",
                    expanded = orderInfoExpanded,
                    onHeaderClick = { orderInfoExpanded = !orderInfoExpanded }
                ) {
                    KeyValueRow(label = "Order ID", value = orderData.orderId ?: "ORD-1001", isValuePrimary = true)
                    KeyValueRow(label = "Order Date", value = orderData.orderDate.ifBlank { "15 Oct 2025" })
                    KeyValueRow(label = "Description", value = "Mens Wedding Attire Set")
                    KeyValueRow(label = "Order Type", value = "New Stitching")
                    KeyValueRow(label = "Brand", value = "Raymond")
                    KeyValueRow(label = "Priority", value = "Normal")
                    KeyValueRow(label = "Expected Delivery", value = orderData.deliveryDate.ifBlank { "30 Dec 2025" })
                    KeyValueRow(label = "Delivery Method", value = "Store Pickup")
                }

                // ─────────────────────────────────────────────────────────────
                // 3. MEASUREMENTS SUMMARY
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Measurements Summary",
                    expanded = measurementsSummaryExpanded,
                    onHeaderClick = { measurementsSummaryExpanded = !measurementsSummaryExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileBadge(label = "Profile: Wedding Shirt", isPrimary = true)
                        ProfileBadge(label = "Unit: Inches", isPrimary = false)
                        ProfileBadge(label = "Fit: Regular Fit", isPrimary = false)
                    }

                    Spacer(Modifier.height(14.dp))

                    MeasurementPillsGrid(
                        measurements = listOf(
                            "Chest" to "42", "Waist" to "38", "Seat/Hip" to "40",
                            "Shoulder" to "18", "Back W" to "16", "Across Sh" to "17.5",
                            "Shirt L" to "30", "Sleeve" to "24", "Bicep" to "14",
                            "Wrist" to "7", "Collar" to "16", "Fr Chest" to "21"
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Special Instructions")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFAFAFA))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "None provided",
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 4. ORDER ITEMS & PRICING
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Order Items & Pricing",
                    expanded = itemsPricingExpanded,
                    onHeaderClick = { itemsPricingExpanded = !itemsPricingExpanded }
                ) {
                    PriceLineItem("Wedding Shirt (Raymond) x2", "₹3,000.00")
                    PriceLineItem("Formal Trouser (Raymond) x1", "₹1,200.00")
                    PriceLineItem("Waistcoat (Custom) x1", "₹2,000.00")

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(Modifier.height(8.dp))

                    PriceSummaryLine("Subtotal", "₹${formatIndianNumber(subtotal)}")
                    PriceSummaryLine("Discount (10%)", "- ₹${formatIndianNumber(discountAmount)}", isRed = true)
                    PriceSummaryLine("Tax (GST 18%)", "+ ₹${formatIndianNumber(taxAmount)}")

                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                        Text("₹${formatIndianNumber(grandTotal)}", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 5. CHARGES & PAYMENT DETAILS
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Charges & Payment Details",
                    expanded = chargesPaymentExpanded,
                    onHeaderClick = { chargesPaymentExpanded = !chargesPaymentExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("+ Add Custom Charges", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                        Text("Discount: ₹0", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                    }

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Payment Method")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(whiteBg)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cash Payment", fontSize = 13.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    FormDropdown(
                        label = "Collected By",
                        value = collectedBy,
                        expanded = collectedByExpanded,
                        onExpandChange = { collectedByExpanded = it },
                        options = listOf("Store Associate A", "Store Associate B", "Manager"),
                        onOptionSelected = { collectedBy = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Payment Type")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isFullAdvance = true }
                        ) {
                            RadioButton(
                                selected = isFullAdvance,
                                onClick = { isFullAdvance = true },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Text("Full Advance", fontSize = 13.sp, color = Color(0xFF334155))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isFullAdvance = false }
                        ) {
                            RadioButton(
                                selected = !isFullAdvance,
                                onClick = { isFullAdvance = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Text("Without Advance", fontSize = 13.sp, color = Color(0xFF334155))
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Payment Amount Received")
                    FormTextField(
                        value = paymentAmountReceived,
                        onValueChange = { paymentAmountReceived = it },
                        placeholder = "₹ 0",
                        keyboardType = KeyboardType.Number
                    )
                    Text("* No payment collected right now.", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Order Notes")
                    FormTextArea(
                        value = operationalNotes,
                        onValueChange = { operationalNotes = it },
                        placeholder = "Type any operational notes here..."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 6. PAYMENT & INVOICE STATUS
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Payment & Invoice Status",
                    expanded = paymentStatusExpanded,
                    onHeaderClick = { paymentStatusExpanded = !paymentStatusExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Advance Paid", fontSize = 11.sp, color = Color(0xFF166534))
                                Spacer(Modifier.height(4.dp))
                                Text("₹${formatIndianNumber(advancePaid)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Balance Due", fontSize = 11.sp, color = Color(0xFF991B1B))
                                Spacer(Modifier.height(4.dp))
                                Text("₹${formatIndianNumber(balanceDue)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Payment Mode:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("UPI (GPay)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "An automated invoice confirmation SMS and Email will be sent to the customer upon confirmation.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 7. ADDITIONAL NOTES
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Additional Notes",
                    expanded = additionalNotesExpanded,
                    onHeaderClick = { additionalNotesExpanded = !additionalNotesExpanded }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("1. Customer prefers slim fit adjustments on shirt collar.", fontSize = 12.sp, color = Color(0xFF92400E), lineHeight = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("2. Double-check measurements before cutting.", fontSize = 12.sp, color = Color(0xFF92400E), lineHeight = 18.sp)
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // FLOATING SHARE DOCK (RIGHT-TO-LEFT SLIDING CIRCULAR PILL)
            // ─────────────────────────────────────────────────────────────
            val arrowRotation by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isShareMenuExpanded) 180f else 0f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                label = "ShareArrowRotation"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 90.dp) // Positioned above bottom FABs
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(whiteBg)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    // Expanding Action Buttons (Slides from Right to Left)
                    AnimatedVisibility(
                        visible = isShareMenuExpanded,
                        enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                        ) {
                            ShareActionButton(
                                label = "WhatsApp",
                                icon = Icons.Default.Whatsapp
                            )
                            ShareActionButton(
                                label = "PDF",
                                icon = Icons.Default.Download
                            )
                            ShareActionButton(
                                label = "Email",
                                icon = Icons.Default.Email
                            )
                        }
                    }

                    // Circular Toggle Button with 180° Rotation
                    Surface(
                        onClick = { isShareMenuExpanded = !isShareMenuExpanded },
                        shape = CircleShape,
                        color = if (isShareMenuExpanded) Color(0xFFF1F5F9) else whiteBg,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Toggle Share Options",
                                tint = Primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = if (isShareMenuExpanded) 0.dp else 2.dp)
                                    .graphicsLayer { rotationZ = arrowRotation } // Smooth 180 degree rotation
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // FLOATING BOTTOM ACTION BAR (CANCEL ON LEFT, CONFIRM ON RIGHT)
            // ─────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back / Cancel Button on Left
                BackFabButton(
                    showArrow = false,
                    onClick = onCancel,
                    label = "Cancel"
                )

                // Trailing FAB (Confirm and Finalize) on Right
                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = "Confirm and Finalize Order",
                        onClick = onConfirmOrder
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE HELPER VIEWS
// ─────────────────────────────────────────────────────────────
@Composable
private fun KeyValueRow(label: String, value: String, isValuePrimary: Boolean = false) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (isValuePrimary) FontWeight.Bold else FontWeight.Medium,
            color = if (isValuePrimary) Primary else title_color
        )
    }
}

@Composable
private fun ProfileBadge(label: String, isPrimary: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPrimary) Color(0xFFEEF2FF) else Color(0xFFF1F5F9))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isPrimary) Primary else Color(0xFF64748B)
        )
    }
}

@Composable
private fun MeasurementPillsGrid(measurements: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        measurements.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (label, value) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.width(6.dp))
                            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLineItem(name: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 13.sp, color = Color(0xFF334155))
        Text(text = price, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun PriceSummaryLine(label: String, value: String, isRed: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isRed) redText else title_color
        )
    }
}

@Composable
private fun ShareActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Primary),
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Primary)
    }
}