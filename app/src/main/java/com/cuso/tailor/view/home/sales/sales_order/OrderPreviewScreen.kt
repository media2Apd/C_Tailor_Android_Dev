@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.tailor.view.home.sales.sales_order

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.OrderReviewData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
data class OrderConfirmData(
    val paymentAmountReceived: Double = 0.0,
    val paymentMode: String = "Cash",
    val isFullPayment: Boolean = false,
    val collectedBy: String = "",
    val orderNotes: String = ""
)
@Composable
fun OrderPreviewScreen(
    orderData: OrderReviewData,
    onClose: () -> Unit = {},
    onConfirmOrder: (OrderConfirmData) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    // ── Flow state saved by Create Order and Measurement Entry ──
    val draft = OrderFlowStore.draft
    val measurementList = OrderFlowStore.measurements.values.toList()
    val pricing = draft?.pricing ?: OrderPricingResult()

    // Edit mode: order already has a backend id (not just a typed order code)
    val isEditMode = !orderData.editOrderId.isNullOrBlank()
    // ── Resolved display values (draft first, then orderData as fallback) ──
    val customerName = (draft?.customerName ?: "").ifBlank { orderData.fullName }
    val customerPhone = (draft?.phone ?: "").ifBlank { orderData.phone }
    val customerAddress = (draft?.address ?: "").ifBlank { orderData.address }
    val resolvedOrderId = (draft?.orderId ?: "").ifBlank { orderData.orderId ?: "" }
    val resolvedOrderDate = (draft?.orderDate ?: "").ifBlank { orderData.orderDate }
    val resolvedDelivery = (draft?.expectedDeliveryDate ?: "").ifBlank { orderData.deliveryDate }
    val garmentTitles = pricing.lines.filter { it.type == "Garment" }.joinToString(", ") { it.title }
        .ifBlank { orderData.garments.firstOrNull()?.categoryName ?: "" }

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

    val savedPreview = remember { OrderFlowStore.previewPayment }

    var collectedBy by rememberSaveable { mutableStateOf(savedPreview?.collectedBy ?: "Store Associate A") }
    var collectedByExpanded by remember { mutableStateOf(false) }

    val advanceFromCreateOrder = remember(draft, orderData) {
        val fromDraft = draft?.advanceAmount?.toDoubleOrNull()
        val value = fromDraft ?: orderData.paidSoFar
        if (value > 0.0) value.toInt().toString() else "0"
    }

    var isFullAdvance by rememberSaveable {
        mutableStateOf(savedPreview?.isFullAdvance ?: (draft?.paymentType == "Full Payment"))
    }
    var paymentAmountReceived by rememberSaveable {
        mutableStateOf(savedPreview?.paymentAmountReceived ?: advanceFromCreateOrder)
    }
    var operationalNotes by rememberSaveable { mutableStateOf(savedPreview?.orderNotes ?: "") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Ovvoru maatramum store-la save: back panna vandhaalum irukkum
    LaunchedEffect(collectedBy, isFullAdvance, paymentAmountReceived, operationalNotes) {
        OrderFlowStore.previewPayment = PreviewPaymentState(
            collectedBy = collectedBy,
            isFullAdvance = isFullAdvance,
            paymentAmountReceived = paymentAmountReceived,
            orderNotes = operationalNotes
        )
    }

    if (showDiscardDialog) {
        DiscardOrderConfirmDialog(
            onConfirmDiscard = {
                showDiscardDialog = false
                OrderFlowStore.clear()
                onCancel()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    // ── Financial Totals (all derived from the saved order flow) ──
    val subtotal = pricing.subtotal
    val discountAmount = pricing.discount
    val taxAmount = pricing.tax
    val grandTotal = pricing.grandTotal
    val advancePaid = paymentAmountReceived.toDoubleOrNull() ?: 0.0
    val balanceDue = (grandTotal - advancePaid).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update Order Preview" else "Order Preview",
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
                        text = "Please review the order details and invoice summary below for ${customerName.ifBlank { "the customer" }}.",
                        fontSize = tokens.caption,
                        color = headerGrey
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
                    KeyValueRow(label = "Customer Name", value = customerName.ifBlank { "-" })
                    KeyValueRow(label = "Mobile", value = customerPhone.ifBlank { "-" })
                    KeyValueRow(label = "Email", value = (draft?.email ?: "").ifBlank { "-" })
                    KeyValueRow(label = "Address", value = customerAddress.ifBlank { "-" })
                    KeyValueRow(
                        label = "Customer Type",
                        value = (draft?.customerType ?: "").ifBlank { orderData.dressFor.ifBlank { "Individual" } }
                    )
                    KeyValueRow(label = "Branch", value = (draft?.branchName ?: "").ifBlank { "-" })
                    KeyValueRow(label = "Garment Type", value = garmentTitles.ifBlank { "-" })
                }

                // ─────────────────────────────────────────────────────────────
                // 2. ORDER INFORMATION
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Order Information",
                    expanded = orderInfoExpanded,
                    onHeaderClick = { orderInfoExpanded = !orderInfoExpanded }
                ) {
                    KeyValueRow(
                        label = "Order ID",
                        value = resolvedOrderId.ifBlank { "Auto-generated" },
                        isValuePrimary = true
                    )
                    KeyValueRow(label = "Order Date", value = resolvedOrderDate.ifBlank { "-" })
                    KeyValueRow(label = "Order Type", value = (draft?.orderType ?: "").ifBlank { "New Stitching" })
                    KeyValueRow(
                        label = "Priority",
                        value = orderData.garments.firstOrNull()?.priority?.ifBlank { "Medium" } ?: "Medium"
                    )
                    KeyValueRow(label = "Expected Delivery", value = resolvedDelivery.ifBlank { "-" })
                    KeyValueRow(label = "Total Items", value = pricing.lines.size.toString())
                }

                // ─────────────────────────────────────────────────────────────
                // 3. MEASUREMENTS SUMMARY (from Measurement Entry + category API fields)
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Measurements Summary",
                    expanded = measurementsSummaryExpanded,
                    onHeaderClick = { measurementsSummaryExpanded = !measurementsSummaryExpanded }
                ) {
                    if (measurementList.isEmpty()) {
                        Text(
                            text = "No measurements recorded for this order.",
                            fontSize = tokens.bodySmall,
                            color = headerGrey
                        )
                    } else {
                        measurementList.forEachIndexed { snapshotIndex, snapshot ->
                            val garmentHeader = if (snapshot.garmentCategory.isNotBlank()) {
                                "${snapshot.garmentType} – ${snapshot.garmentCategory}"
                            } else {
                                snapshot.garmentType
                            }

                            Text(
                                text = garmentHeader,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileBadge(label = "Profile: ${snapshot.profileName.ifBlank { "-" }}", isPrimary = true)
                                ProfileBadge(label = "Unit: ${snapshot.unit}", isPrimary = false)
                                ProfileBadge(label = snapshot.fitType, isPrimary = false)
                            }

                            Spacer(Modifier.height(8.dp))

                            if (snapshot.measurementDate.isNotBlank()) {
                                KeyValueRow(label = "Measured On", value = snapshot.measurementDate)
                            }
                            if (snapshot.takenBy.isNotBlank()) {
                                KeyValueRow(label = "Taken By", value = snapshot.takenBy)
                            }

                            Spacer(Modifier.height(8.dp))

                            if (snapshot.fields.isEmpty()) {
                                Text(
                                    text = "No measurement fields available.",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            } else {
                                snapshot.fields.groupBy { it.group }.forEach { (groupName, groupFields) ->
                                    Text(
                                        text = groupName.uppercase(),
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color,
                                        modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                                    )
                                    MeasurementPillsGrid(
                                        measurements = groupFields.map { field ->
                                            field.label to "${formatMeasurementValue(field.value)} ${field.unit}"
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            FormLabel("Special Instructions")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = snapshot.specialInstructions.ifBlank { "None provided" },
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155)
                                )
                            }

                            if (snapshotIndex < measurementList.lastIndex) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = dividerColor)
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 4. ORDER ITEMS AND PRICING
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Order Items & Pricing",
                    expanded = itemsPricingExpanded,
                    onHeaderClick = { itemsPricingExpanded = !itemsPricingExpanded }
                ) {
                    if (pricing.lines.isEmpty()) {
                        Text(
                            text = "No items added to this order.",
                            fontSize = tokens.bodySmall,
                            color = headerGrey
                        )
                    } else {
                        pricing.lines.forEachIndexed { lineIndex, line ->
                            PriceLineItem(
                                name = "#${line.itemNumber} ${line.title} x${line.quantity}",
                                price = "₹${formatIndianNumber(line.total)}"
                            )

                            // Component breakdown for this item
                            if (line.stitching > 0.0) {
                                PriceBreakdownLine("Stitching", "₹${formatIndianNumber(line.stitching)}")
                            }
                            if (line.fabric > 0.0) {
                                PriceBreakdownLine("Fabric", "₹${formatIndianNumber(line.fabric)}")
                            }
                            if (line.additionalWork > 0.0) {
                                PriceBreakdownLine("Additional Work", "₹${formatIndianNumber(line.additionalWork)}")
                            }
                            PriceBreakdownLine("GST", "₹${formatIndianNumber(line.gst)}")

                            // Item configuration details entered in Create Order
                            line.details.forEach { (detailLabel, detailValue) ->
                                PriceBreakdownLine(detailLabel, detailValue)
                            }

                            if (lineIndex < pricing.lines.lastIndex) {
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider(color = dividerColor)
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(Modifier.height(8.dp))

                    PriceSummaryLine("Subtotal", "₹${formatIndianNumber(subtotal)}")
                    PriceSummaryLine("Discount", "- ₹${formatIndianNumber(discountAmount)}", isRed = true)
                    PriceSummaryLine("Tax (GST)", "+ ₹${formatIndianNumber(taxAmount)}")

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
                // 5. CHARGES AND PAYMENT DETAILS
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
                        Text("Discount: ₹${formatIndianNumber(discountAmount)}", fontSize = tokens.bodySmall, color = headerGrey)
                    }

                    Spacer(Modifier.height(12.dp))

                    FormLabel("Payment Method")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(whiteBg)
                            .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cash Payment", fontSize = 13.sp, color = title_color, fontWeight = FontWeight.Medium)
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
                            modifier = Modifier.clickable {
                                isFullAdvance = true
                                paymentAmountReceived = grandTotal.toInt().toString()
                            }
                        ) {
                            AppRadioButton(
                                selected = isFullAdvance,
                                onClick = {
                                    isFullAdvance = true
                                    paymentAmountReceived = grandTotal.toInt().toString()
                                }
                            )
                            Text("Full Advance", fontSize = 13.sp, color = Color(0xFF334155))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isFullAdvance = false }
                        ) {
                            AppRadioButton(
                                selected = !isFullAdvance,
                                onClick = { isFullAdvance = false }
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
                    Text(
                        text = if (advancePaid > 0.0) {
                            "* Payment of ₹${formatIndianNumber(advancePaid)} will be recorded with this order."
                        } else {
                            "* No payment collected right now."
                        },
                        fontSize = 11.sp,
                        color = headerGrey,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    FormLabel("Order Notes")
                    FormTextArea(
                        value = operationalNotes,
                        onValueChange = { operationalNotes = it },
                        placeholder = "Type any operational notes here..."
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 6. PAYMENT AND INVOICE STATUS
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
                        Text("Payment Type:", fontSize = 12.sp, color = headerGrey)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(grey_border)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = (draft?.paymentType ?: "").ifBlank { "Advance" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "An automated invoice confirmation SMS and Email will be sent to the customer upon confirmation.",
                        fontSize = 11.sp,
                        color = headerGrey,
                        lineHeight = 15.sp
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 7. ADDITIONAL NOTES (collected from items, measurements and this screen)
                // ─────────────────────────────────────────────────────────────
                AccordionSection(
                    title = "Additional Notes",
                    expanded = additionalNotesExpanded,
                    onHeaderClick = { additionalNotesExpanded = !additionalNotesExpanded }
                ) {
                    val notes = buildList {
                        pricing.lines.forEach { line ->
                            line.details.firstOrNull { it.first == "Instructions" }?.let { instruction ->
                                add("Item #${line.itemNumber} (${line.title}): ${instruction.second}")
                            }
                        }
                        measurementList.forEach { snapshot ->
                            if (snapshot.specialInstructions.isNotBlank()) {
                                add("Measurement (${snapshot.garmentType}): ${snapshot.specialInstructions}")
                            }
                        }
                        if (operationalNotes.isNotBlank()) {
                            add("Order note: $operationalNotes")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (notes.isEmpty()) {
                                Text(
                                    text = "No additional notes.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            } else {
                                notes.forEachIndexed { noteIndex, note ->
                                    Text(
                                        text = "${noteIndex + 1}. $note",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E),
                                        lineHeight = 18.sp
                                    )
                                    if (noteIndex < notes.lastIndex) {
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
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
                    // Expanding action buttons (slides from right to left)
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

                    // Circular toggle button with 180 degree rotation
                    Surface(
                        onClick = { isShareMenuExpanded = !isShareMenuExpanded },
                        shape = CircleShape,
                        color = if (isShareMenuExpanded) grey_border else whiteBg,
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
                                    .graphicsLayer { rotationZ = arrowRotation }
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
                // Cancel button on the left: also clears the saved flow state
                BackFabButton(
                    showArrow = false,
                    onClick = { showDiscardDialog = true },
                    label = "Cancel"
                )

                // Trailing FAB (confirm and finalize) on the right
                // Bottom FAB  (Before: onClick = onConfirmOrder)
                TrailingFabButton(
                    action = TrailingFabAction.Next(
                        label = if (isEditMode) "Update Order" else "Confirm and Finalize Order",
                        onClick = {
                            onConfirmOrder(
                                OrderConfirmData(
                                    paymentAmountReceived = advancePaid,
                                    paymentMode = "Cash",
                                    isFullPayment = isFullAdvance,
                                    collectedBy = collectedBy,
                                    orderNotes = operationalNotes
                                )
                            )
                        }
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
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (isValuePrimary) FontWeight.Bold else FontWeight.Medium,
            color = if (isValuePrimary) Primary else title_color,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ProfileBadge(label: String, isPrimary: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPrimary) Color(0xFFEEF2FF) else grey_border)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isPrimary) Primary else headerGrey,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Two pills per row so long field names from the API stay readable. */
@Composable
private fun MeasurementPillsGrid(measurements: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        measurements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeGrey)
                            .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = value,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color,
                            maxLines = 1
                        )
                    }
                }

                // Keep the last odd pill at half width
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
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
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = price, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = title_color)
    }
}

/** Small secondary line shown under an item (component price or configuration detail). */
@Composable
private fun PriceBreakdownLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 1.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = headerGrey)
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f, fill = false)
        )
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
        Text(text = label, fontSize = 12.sp, color = headerGrey)
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