package com.example.tailorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.ChargeRequest
import com.cuso.mobile.model.CreateGarmentRequestForCreateOrder
import com.cuso.mobile.model.CreateOrderRequest
import com.cuso.mobile.viewmodel.SalesOrderViewModel

// ─── Colors ───────────────────────────────────────────────────────────────────
private val AccentBlue   = Color(0xFF3B82F6)
private val AccentBlueBg = Color(0xFFEFF6FF)
private val SuccessGreen = Color(0xFF22C55E)
private val SectionBg    = Color(0xFFFFFFFF)
private val PageBg       = Color(0xFFF3F4F6)
private val BorderColor  = Color(0xFFE5E7EB)
private val LabelGray    = Color(0xFF9CA3AF)
private val TextPrimary  = Color(0xFF111827)
private val TextSecond   = Color(0xFF6B7280)
private val DangerRed    = Color(0xFFEF4444)

// ─── Data holder passed in from CreateOrderScreen ──────────────────────────────
data class OrderReviewData(
    val customerId: String,             // ✅ NEW — Mongo _id of the customer, required by API
    val branchId: String? = null,       // ✅ NEW — Mongo _id of the branch
    val fullName: String,
    val countryCode: String,
    val phone: String,
    val gender: String,
    val dressFor: String,
    val address: String,
    val garments: List<SelectedGarment>,
    val trialDate: String,      // empty string => "Not Scheduled"   (expects yyyy-MM-dd if non-empty)
    val deliveryDate: String,   // empty string => "Not Scheduled"   (expects yyyy-MM-dd if non-empty)
    val discount: Double = 0.0,
    val paidSoFar: Double = 0.0
)

// ─── Charge model used for both "Item Charge" (per garment) and "Global Charge" (order level) ──
data class ChargeItem(
    val id: String,
    val name: String = "",
    val amount: String = ""   // kept as String so the text field stays fully editable while typing
)

private fun newChargeId(): String = "charge_${System.nanoTime()}"

// ─── Entry Point ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderNextStep(
    orderData: OrderReviewData,
    onBack: () -> Unit = {},
    onSaveOrder: (CreateOrderRequest) -> Unit = {}   // ✅ now hands back the fully built request
) {
    // ── Editable unit price per garment (garment.id -> price text) ──
    var unitPrices by remember(orderData.garments) {
        mutableStateOf(
            orderData.garments.associate { g ->
                g.id to (if (g.price > 0) g.price.toInt().toString() else "")
            }
        )
    }

    // ── Per-garment "Item Charges" (garment.id -> list of ChargeItem) ──
    var itemCharges by remember(orderData.garments) {
        mutableStateOf(mapOf<String, List<ChargeItem>>())
    }
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()


    // ── Order-level "Global Charges" ──
    var globalCharges by remember { mutableStateOf(listOf<ChargeItem>()) }

    // ── Discount (editable too, so Payment Summary reacts live) ──
    var discountText by remember { mutableStateOf(if (orderData.discount > 0) orderData.discount.toInt().toString() else "") }

    // ── Computed subtotal: (qty * unit price) for every garment + all item charges + all global charges ──
    val subtotal = remember(unitPrices, itemCharges, globalCharges, orderData.garments) {
        val garmentTotal = orderData.garments.sumOf { g ->
            val price = unitPrices[g.id]?.toDoubleOrNull() ?: 0.0
            price * g.quantity
        }
        val itemChargeTotal = itemCharges.values.sumOf { list ->
            list.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        }
        val globalChargeTotal = globalCharges.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        garmentTotal + itemChargeTotal + globalChargeTotal
    }

    val discountValue = discountText.toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = { CreateOrderTopBar(onBack) },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CustomerDetailsSection(orderData)

            GarmentsSection(orderData.garments)

            BillingDetailsSection(
                garments = orderData.garments,
                unitPrices = unitPrices,
                onUnitPriceChange = { garmentId, newValue ->
                    unitPrices = unitPrices.toMutableMap().apply { this[garmentId] = newValue }
                },
                itemCharges = itemCharges,
                onAddItemCharge = { garmentId ->
                    val current = itemCharges[garmentId].orEmpty()
                    itemCharges = itemCharges.toMutableMap().apply {
                        this[garmentId] = current + ChargeItem(id = newChargeId())
                    }
                },
                onItemChargeNameChange = { garmentId, chargeId, newName ->
                    itemCharges = itemCharges.toMutableMap().apply {
                        this[garmentId] = (this[garmentId].orEmpty()).map {
                            if (it.id == chargeId) it.copy(name = newName) else it
                        }
                    }
                },
                onItemChargeAmountChange = { garmentId, chargeId, newAmount ->
                    itemCharges = itemCharges.toMutableMap().apply {
                        this[garmentId] = (this[garmentId].orEmpty()).map {
                            if (it.id == chargeId) it.copy(amount = newAmount) else it
                        }
                    }
                },
                onRemoveItemCharge = { garmentId, chargeId ->
                    itemCharges = itemCharges.toMutableMap().apply {
                        this[garmentId] = (this[garmentId].orEmpty()).filter { it.id != chargeId }
                    }
                },
                globalCharges = globalCharges,
                onAddGlobalCharge = {
                    globalCharges = globalCharges + ChargeItem(id = newChargeId())
                },
                onGlobalChargeNameChange = { chargeId, newName ->
                    globalCharges = globalCharges.map {
                        if (it.id == chargeId) it.copy(name = newName) else it
                    }
                },
                onGlobalChargeAmountChange = { chargeId, newAmount ->
                    globalCharges = globalCharges.map {
                        if (it.id == chargeId) it.copy(amount = newAmount) else it
                    }
                },
                onRemoveGlobalCharge = { chargeId ->
                    globalCharges = globalCharges.filter { it.id != chargeId }
                },
                subtotal = subtotal
            )

            DeliveryScheduleSection(orderData.trialDate, orderData.deliveryDate)

            PaymentSummarySection(
                subtotal = subtotal,
                discountText = discountText,
                onDiscountChange = { discountText = it },
                paidSoFar = orderData.paidSoFar
            )

            val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()

            ActionButtons(
                onBack = onBack,
                onSaveOrder = {
                    // ── Build the per-garment request list (price, qty, total, item charges) ──
                    val garmentRequests = orderData.garments.map { g ->
                        val price = unitPrices[g.id]?.toDoubleOrNull() ?: 0.0

                        val charges = itemCharges[g.id].orEmpty().mapNotNull { c ->
                            val amt = c.amount.toDoubleOrNull()
                            if (c.name.isNotBlank() && amt != null) {
                                ChargeRequest(
                                    name = c.name,
                                    amount = amt
                                )
                            } else {
                                null
                            }
                        }

                        val chargeTotal = charges.sumOf { it.amount }

                        CreateGarmentRequestForCreateOrder(
                            category = g.category,
                            categoryName = g.categoryName,
                            models = g.models ?: emptyList(),
                            quantity = g.quantity,
                            price = price,
                            total = (price * g.quantity) + chargeTotal,
                            additionalCharges = charges
                        )
                    }

                    // ── Build the order-level (global) charges ──
                    val globalChargeRequests = globalCharges.mapNotNull { c ->
                        val amt = c.amount.toDoubleOrNull()
                        if (c.name.isNotBlank() && amt != null) {
                            ChargeRequest(
                                name = c.name,
                                amount = amt
                            )
                        } else {
                            null
                        }
                    }

                    val today = java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    val grandTotal = (subtotal - discountValue).coerceAtLeast(0.0)
                    val balance = (grandTotal - orderData.paidSoFar).coerceAtLeast(0.0)

                    val paymentStatus = when {
                        orderData.paidSoFar <= 0.0 -> "unpaid"
                        balance <= 0.0 -> "paid"
                        else -> "partial"
                    }

                    val request = CreateOrderRequest(
                        customerId = orderData.customerId,
                        branch = orderData.branchId,
                        wearerType = orderData.dressFor,
                        garments = garmentRequests,
                        summaryAdditionalCharges = globalChargeRequests,
                        discount = discountValue,
                        totalAmount = grandTotal,
                        totalPaid = orderData.paidSoFar,
                        paymentStatus = paymentStatus,
                        source = "Walk-in",
                        orderDate = today,
                        trialDate = orderData.trialDate.ifBlank { null },
                        deliveryDate = orderData.deliveryDate.ifBlank { null },
                        status = "confirmed"
                    )

                    // ✅ Create Order API Call
                    salesOrderViewModel.createOrder(request) { createdOrder ->
                        // Success
                        onSaveOrder(request) // Navigate / Back / Success action
                    }
                }
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrderTopBar(onBack: () -> Unit) {
    Column {
        HorizontalDivider(color = BorderColor)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SectionBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = TextSecond)
            }
            Spacer(Modifier.width(6.dp))
            Text("Create Order", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        HorizontalDivider(color = BorderColor)
    }
}

// ─── Customer Details ─────────────────────────────────────────────────────────
@Composable
private fun CustomerDetailsSection(data: OrderReviewData) {
    SectionCard {
        SectionHeader(icon = Icons.Default.Info, title = "Customer Details")
        LabelValue(label = "FULL NAME", value = data.fullName.ifBlank { "—" })
        Spacer(Modifier.height(8.dp))
        LabelValue(label = "CONTACT INFO", value = "${data.countryCode} ${data.phone}".trim().ifBlank { "—" })
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                LabelValue(label = "GENDER / PROFILE", value = "${data.gender} / ${data.dressFor}")
            }
            Box(Modifier.weight(1f)) {
                LabelValue(label = "SHIPPING ADDRESS", value = data.address.ifBlank { "—" }, bold = false)
            }
        }
    }
}

// ─── Garments ─────────────────────────────────────────────────────────────────
@Composable
private fun GarmentsSection(garments: List<SelectedGarment>) {
    SectionCard {
        SectionHeader(icon = Icons.Default.Checkroom, title = "Garments")
        if (garments.isEmpty()) {
            Text("No garments added", fontSize = 14.sp, color = LabelGray)
            return@SectionCard
        }
        garments.forEachIndexed { index, garment ->
            Text(garment.categoryName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    MeasurementField(
                        label = "QTY",
                        value = garment.quantity.toString(),
                        unit = ""
                    )
                }
                Box(Modifier.weight(1f)) {
                    MeasurementField(
                        label = "FABRIC / COLOR",
                        value = listOf(garment.fabricType, garment.colorTone)
                            .filter { it.isNotBlank() }
                            .joinToString(" / ")
                            .ifBlank { "-" },
                        unit = ""
                    )
                }
            }
            if (index != garments.lastIndex) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ─── Billing Details (FULLY RESPONSIVE) ────────────────────────────────────────
@Composable
private fun BillingDetailsSection(
    garments: List<SelectedGarment>,
    unitPrices: Map<String, String>,
    onUnitPriceChange: (garmentId: String, newValue: String) -> Unit,
    itemCharges: Map<String, List<ChargeItem>>,
    onAddItemCharge: (garmentId: String) -> Unit,
    onItemChargeNameChange: (garmentId: String, chargeId: String, newName: String) -> Unit,
    onItemChargeAmountChange: (garmentId: String, chargeId: String, newAmount: String) -> Unit,
    onRemoveItemCharge: (garmentId: String, chargeId: String) -> Unit,
    globalCharges: List<ChargeItem>,
    onAddGlobalCharge: () -> Unit,
    onGlobalChargeNameChange: (chargeId: String, newName: String) -> Unit,
    onGlobalChargeAmountChange: (chargeId: String, newAmount: String) -> Unit,
    onRemoveGlobalCharge: (chargeId: String) -> Unit,
    subtotal: Double
) {
    SectionCard {
        SectionHeader(icon = Icons.Default.Description, title = "Billing Details")

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ITEM", fontSize = 10.sp, color = LabelGray, modifier = Modifier.weight(1f))
            Text("QTY", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(48.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("UNIT PRICE (₹)", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(110.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 6.dp))

        if (garments.isEmpty()) {
            Text("No items added", fontSize = 13.sp, color = LabelGray)
        } else {
            garments.forEach { garment ->
                // ── Garment row: name + qty + editable unit price ──
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(garment.categoryName, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                            .background(PageBg)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(garment.quantity.toString().padStart(2, '0'), fontSize = 13.sp, color = TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    PriceInputBox(
                        value = unitPrices[garment.id].orEmpty(),
                        onValueChange = { onUnitPriceChange(garment.id, it) },
                        width = 110.dp
                    )
                }

                // ── Item charges for this garment ──
                val charges = itemCharges[garment.id].orEmpty()
                if (charges.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        charges.forEach { charge ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("+", fontSize = 13.sp, color = LabelGray)
                                Spacer(Modifier.width(6.dp))
                                ChargeNameInput(
                                    value = charge.name,
                                    onValueChange = { onItemChargeNameChange(garment.id, charge.id, it) },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                PriceInputBox(
                                    value = charge.amount,
                                    onValueChange = { onItemChargeAmountChange(garment.id, charge.id, it) },
                                    width = 70.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove charge",
                                    tint = LabelGray,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveItemCharge(garment.id, charge.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "+ Add Item Charge",
                    fontSize = 13.sp,
                    color = AccentBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onAddItemCharge(garment.id) }
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Global / Additional Charges ──
        Text(
            "GLOBAL / ADDITIONAL CHARGES",
            fontSize = 10.sp,
            color = LabelGray,
            letterSpacing = 0.06.sp
        )
        Spacer(Modifier.height(8.dp))

        if (globalCharges.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                globalCharges.forEach { charge ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ChargeNameInput(
                            value = charge.name,
                            onValueChange = { onGlobalChargeNameChange(charge.id, it) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        PriceInputBox(
                            value = charge.amount,
                            onValueChange = { onGlobalChargeAmountChange(charge.id, it) },
                            width = 80.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove charge",
                            tint = LabelGray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRemoveGlobalCharge(charge.id) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            "+ Add Global Charge",
            fontSize = 13.sp,
            color = AccentBlue,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onAddGlobalCharge() }
        )

        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Items: ${garments.size}", fontSize = 13.sp, color = TextSecond)
            Text("Subtotal   ₹${"%.2f".format(subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

// ── Reusable price input box (editable, ₹ prefix, numeric only) ──
@Composable
private fun PriceInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    width: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(6.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
            .background(PageBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("₹", fontSize = 13.sp, color = LabelGray)
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("0", fontSize = 13.sp, color = LabelGray)
                }
                inner()
            }
        )
    }
}

// ── Reusable charge-name input (e.g. "Embroidery", "Express Delivery") ──
@Composable
private fun ChargeNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
            .background(PageBg)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("Charge Name", fontSize = 13.sp, color = LabelGray)
                }
                inner()
            }
        )
    }
}

// ─── Delivery Schedule ────────────────────────────────────────────────────────
@Composable
private fun DeliveryScheduleSection(trialDate: String, deliveryDate: String) {
    SectionCard {
        SectionHeader(icon = Icons.Default.CalendarMonth, title = "Delivery Schedule")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Trial Date", fontSize = 14.sp, color = TextSecond)
            Text(
                trialDate.ifBlank { "Not Scheduled" },
                fontSize = 14.sp,
                color = if (trialDate.isBlank()) LabelGray else TextPrimary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AccentBlueBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Final Delivery", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AccentBlue)
            Text(
                deliveryDate.ifBlank { "Not Scheduled" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AccentBlue
            )
        }
    }
}

// ─── Payment Summary (now driven by computed subtotal + editable discount) ────
@Composable
private fun PaymentSummarySection(
    subtotal: Double,
    discountText: String,
    onDiscountChange: (String) -> Unit,
    paidSoFar: Double
) {
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)
    val balanceDue = (grandTotal - paidSoFar).coerceAtLeast(0.0)

    SectionCard {
        SectionHeader(icon = Icons.Default.CurrencyRupee, title = "Payment Summary")

        PaymentRow(label = "Subtotal", value = "₹${"%.2f".format(subtotal)}")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Discount", fontSize = 14.sp, color = TextSecond, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .background(PageBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("- ₹", fontSize = 13.sp, color = LabelGray)
                Spacer(Modifier.width(4.dp))
                BasicTextField(
                    value = discountText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onDiscountChange(newValue)
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(50.dp),
                    decorationBox = { inner ->
                        if (discountText.isEmpty()) {
                            Text("0", fontSize = 13.sp, color = LabelGray)
                        }
                        inner()
                    }
                )
            }
        }

        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("₹${"%.2f".format(grandTotal)}", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Paid So Far", fontSize = 13.sp, color = TextSecond)
            Text("₹${"%.2f".format(paidSoFar)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Balance Due", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecond)
            Text("₹${"%.2f".format(balanceDue)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
        }
    }
}

// ─── Action Buttons ───────────────────────────────────────────────────────────
@Composable
private fun ActionButtons(
    onBack: () -> Unit,
    onSaveOrder: () -> Unit,
    salesOrderViewModel: SalesOrderViewModel = hiltViewModel()
) {
    val actionState by salesOrderViewModel.actionState.collectAsState()
    val isSaving = actionState is com.cuso.mobile.viewmodel.OrderActionState.Loading

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(8.dp),
            border = ButtonDefaults.outlinedButtonBorder,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            enabled = !isSaving
        ) {
            Text("Back to Edit", fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Button(
            onClick = onSaveOrder,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(6.dp))
                Text("Saving...", fontSize = 14.sp)
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save Order", fontSize = 14.sp)
            }
        }
    }

    // Handle action state
    LaunchedEffect(actionState) {
        when (actionState) {
            is com.cuso.mobile.viewmodel.OrderActionState.Success -> {
                // Order created successfully
                // You can add a callback here if needed
                salesOrderViewModel.resetActionState()
            }
            is com.cuso.mobile.viewmodel.OrderActionState.Error -> {
                // Show error - you might want to pass an error callback
                salesOrderViewModel.resetActionState()
            }
            else -> {}
        }
    }
}

// ─── Reusable Composables ─────────────────────────────────────────────────────
@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SectionBg),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AccentBlue)
    }
}

@Composable
private fun LabelValue(label: String, value: String, bold: Boolean = true) {
    Column {
        Text(label, fontSize = 10.sp, color = LabelGray, letterSpacing = 0.06.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
            color = TextPrimary
        )
    }
}

@Composable
private fun MeasurementField(label: String, value: String, unit: String) {
    Column {
        Text(label, fontSize = 10.sp, color = LabelGray, letterSpacing = 0.06.sp)
        Spacer(Modifier.height(2.dp))
        Row {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.alignByBaseline())
            if (unit.isNotBlank()) {
                Spacer(Modifier.width(4.dp))
                Text(unit, fontSize = 12.sp, color = LabelGray, modifier = Modifier.alignByBaseline())
            }
        }
    }
}

@Composable
private fun PaymentRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecond)
        Text(value, fontSize = 14.sp, color = TextPrimary)
    }
}