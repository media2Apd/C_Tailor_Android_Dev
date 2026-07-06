package com.cuso.mobile.view.home.sales.sales_order

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.ChargeRequest
import com.cuso.mobile.model.CreateGarmentRequestForCreateOrder
import com.cuso.mobile.model.CreateOrderRequest
import com.cuso.mobile.model.CustomerRequest
import com.cuso.mobile.model.FabricDetailsRequest
import com.cuso.mobile.model.GarmentModelRequest
import com.cuso.mobile.model.MeasurementValueRequest
import com.cuso.mobile.model.PaymentDetailsRequest
import com.cuso.mobile.model.image_voice.createImageParts
import com.cuso.mobile.model.image_voice.createVoiceNotePart
import com.cuso.mobile.viewmodel.OrderActionState
import com.cuso.mobile.viewmodel.SalesOrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Colors ───────────────────────────────────────────────────────────────────
private val AccentBlue = Color(0xFF3B82F6)
private val AccentBlueBg = Color(0xFFEFF6FF)
private val SuccessGreen = Color(0xFF22C55E)
private val SectionBg = Color(0xFFFFFFFF)
private val PageBg = Color(0xFFF3F4F6)
private val BorderColor = Color(0xFFE5E7EB)
private val LabelGray = Color(0xFF9CA3AF)
private val TextPrimary = Color(0xFF111827)
private val TextSecond = Color(0xFF6B7280)

// ─── Data holder ──────────────────────────────────────────────────────────────
data class OrderReviewData(
    val customerId: String,
    val branchId: String? = null,
    val fullName: String,
    val countryCode: String,
    val phone: String,
    val gender: String,
    val dressFor: String,
    val address: String,
    val garments: List<SelectedGarment>,
    val orderDate: String = "",
    val source: String = "Walk-in",
    val trialDate: String,
    val deliveryDate: String,
    val discount: Double = 0.0,
    val paidSoFar: Double = 0.0,
    val designImages: List<Uri> = emptyList(),
    val voiceNoteUri: Uri? = null
)

// ─── Charge model ──────────────────────────────────────────────────────────────
data class ChargeItem(
    val id: String,
    val name: String = "",
    val amount: String = ""
)

private fun newChargeId(): String = "charge_${System.nanoTime()}"

// ─── Entry Point ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderNextStep(
    orderData: OrderReviewData,
    onBack: () -> Unit = {},
    onSaveOrder: (CreateOrderRequest) -> Unit = {}
) {
    val context = LocalContext.current
    val salesOrderViewModel: SalesOrderViewModel = hiltViewModel()

    // ── State ──
    var unitPrices by remember(orderData.garments) {
        mutableStateOf(
            orderData.garments.associate { g ->
                g.id to (if (g.price > 0) g.price.toInt().toString() else "")
            }
        )
    }

    var itemCharges by remember(orderData.garments) {
        mutableStateOf(mapOf<String, List<ChargeItem>>())
    }

    var globalCharges by remember { mutableStateOf(listOf<ChargeItem>()) }
    var discountText by remember { mutableStateOf(if (orderData.discount > 0) orderData.discount.toInt().toString() else "") }

    // ── Accordion state: only one section expanded at a time ──
    var expandedSection by remember { mutableStateOf("billing") } // "billing" | "payment" | "delivery"

    // ── Computed totals ──
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
    val grandTotal = (subtotal - discountValue).coerceAtLeast(0.0)
    val balanceDue = (grandTotal - orderData.paidSoFar).coerceAtLeast(0.0)

    // ✅ NEW — build the request once, reused by the floating "Save Order" FAB button
    fun buildAndSaveOrder() {
        val garmentRequests = orderData.garments.map { g ->
            val price = unitPrices[g.id]?.toDoubleOrNull() ?: 0.0

            val charges = itemCharges[g.id].orEmpty().mapNotNull { c ->
                val amt = c.amount.toDoubleOrNull()
                if (c.name.isNotBlank() && amt != null) {
                    ChargeRequest(name = c.name, amount = amt)
                } else null
            }
            val chargeTotal = charges.sumOf { it.amount }

            CreateGarmentRequestForCreateOrder(
                category = g.category,
                categoryName = g.categoryName,
                models = g.models.map { modelName -> GarmentModelRequest(modelName = modelName) },
                measurements = g.measurements
                    .filter { it.label.isNotBlank() }
                    .associate { m ->
                        m.label to MeasurementValueRequest(
                            value = listOf(m.value),
                            inputType = "Number",
                            unit = m.unit
                        )
                    }
                    .takeIf { it.isNotEmpty() },
                quantity = g.quantity,
                priority = g.priority,
                trialRequired = g.trialRequired,
                fabricDetails = FabricDetailsRequest(
                    fabricSource = g.fabricSource,
                    fabricType = g.fabricType,
                    color = g.colorTone,
                    pattern = g.pattern
                ),
                stitchingCharge = if (price > 0) price.toInt().toString() else null,
                price = price,
                total = (price * g.quantity) + chargeTotal,
                additionalCharges = charges
            )
        }

        val globalChargeRequests = globalCharges.mapNotNull { c ->
            val amt = c.amount.toDoubleOrNull()
            if (c.name.isNotBlank() && amt != null) {
                ChargeRequest(name = c.name, amount = amt)
            } else null
        }

        val request = CreateOrderRequest(
            customer = CustomerRequest(
                name = orderData.fullName,
                mobile = "${orderData.countryCode.removePrefix("+")}${orderData.phone}",
                address = orderData.address,
                gender = orderData.gender
            ),
            branch = orderData.branchId.orEmpty(),
            wearerType = orderData.dressFor,
            source = orderData.source,
            orderType = "Direct Orders",
            garments = garmentRequests,
            paymentDetails = PaymentDetailsRequest(
                discount = discountValue,
                summaryAdditionalCharges = globalChargeRequests,
                paymentAmount = orderData.paidSoFar
            ),
            orderDate = orderData.orderDate.toIsoDateOrNull()
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            trialDate = orderData.trialDate.toIsoDateOrNull(),
            deliveryDate = orderData.deliveryDate.toIsoDateOrNull(),
            totalAmount = grandTotal,
            status = "confirmed"
        )

        salesOrderViewModel.createOrder(
            request = request,
            imageParts = context.createImageParts(orderData.designImages),
            voiceNotePart = context.createVoiceNotePart(orderData.voiceNoteUri)
        ) { orderItem ->
            onSaveOrder(request)
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SectionBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        "Create Order",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onBack() }
                    )
                }
                HorizontalDivider(color = BorderColor)
            }
        },
        containerColor = PageBg
    ) { padding ->
        // ✅ NEW — outer Box lets the floating pill buttons sit ON TOP of the scrollable content,
        // exactly like CustomerDetailScreen's Back/Next FAB pattern
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp), // ✅ reserve space so content isn't hidden behind floating buttons
                verticalArrangement = Arrangement.spacedBy(0.dp) // ✅ CHANGED — gap between cards removed (was 12.dp)
            ) {
                // ── 1. BILLING DETAILS ──
                SectionCard {
                    SectionHeader(
                        icon = Icons.Default.Description,
                        title = "Billing Details",
                        expanded = expandedSection == "billing",
                        onToggle = { expandedSection = if (expandedSection == "billing") "" else "billing" }
                    )

                    if (expandedSection == "billing") {
                        // ── Header Row ──
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("ITEM", fontSize = 10.sp, color = LabelGray, modifier = Modifier.weight(1f))
                            Text("QTY", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                            Text("Unit Price", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(110.dp), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 6.dp))

                        if (orderData.garments.isEmpty()) {
                            Text("No items added", fontSize = 14.sp, color = LabelGray, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            orderData.garments.forEachIndexed { index, garment ->
                                // ── Garment Row ──
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        garment.categoryName,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                            .background(PageBg)
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            garment.quantity.toString().padStart(2, '0'),
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    PriceInputBox(
                                        value = unitPrices[garment.id].orEmpty(),
                                        onValueChange = {
                                            unitPrices = unitPrices.toMutableMap().apply { this[garment.id] = it }
                                        },
                                        width = 110.dp
                                    )
                                }

                                // ── Item Charges ──
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
                                                    onValueChange = { newName ->
                                                        itemCharges = itemCharges.toMutableMap().apply {
                                                            this[garment.id] = (this[garment.id].orEmpty()).map {
                                                                if (it.id == charge.id) it.copy(name = newName) else it
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                PriceInputBox(
                                                    value = charge.amount,
                                                    onValueChange = { newAmount ->
                                                        itemCharges = itemCharges.toMutableMap().apply {
                                                            this[garment.id] = (this[garment.id].orEmpty()).map {
                                                                if (it.id == charge.id) it.copy(amount = newAmount) else it
                                                            }
                                                        }
                                                    },
                                                    width = 70.dp
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = LabelGray,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable {
                                                            itemCharges = itemCharges.toMutableMap().apply {
                                                                this[garment.id] = (this[garment.id].orEmpty()).filter { it.id != charge.id }
                                                            }
                                                        }
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
                                    modifier = Modifier.clickable {
                                        val current = itemCharges[garment.id].orEmpty()
                                        itemCharges = itemCharges.toMutableMap().apply {
                                            this[garment.id] = current + ChargeItem(id = newChargeId())
                                        }
                                    }
                                )

                                if (index != orderData.garments.lastIndex) {
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }

                        // ── Global / Additional Charges ──
                        Spacer(Modifier.height(16.dp))
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
                                            onValueChange = { newName ->
                                                globalCharges = globalCharges.map {
                                                    if (it.id == charge.id) it.copy(name = newName) else it
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        PriceInputBox(
                                            value = charge.amount,
                                            onValueChange = { newAmount ->
                                                globalCharges = globalCharges.map {
                                                    if (it.id == charge.id) it.copy(amount = newAmount) else it
                                                }
                                            },
                                            width = 80.dp
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = LabelGray,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    globalCharges = globalCharges.filter { it.id != charge.id }
                                                }
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
                            modifier = Modifier.clickable {
                                globalCharges = globalCharges + ChargeItem(id = newChargeId())
                            }
                        )

                        // ── Total Items & Subtotal ──
                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items: ${orderData.garments.size}", fontSize = 14.sp, color = TextSecond)
                            Text("Subtotal   ₹${"%.2f".format(subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }

                // ── 2. PAYMENT SUMMARY ──
                SectionCard {
                    SectionHeader(
                        icon = Icons.Default.CurrencyRupee,
                        title = "Payment Summary",
                        expanded = expandedSection == "payment",
                        onToggle = { expandedSection = if (expandedSection == "payment") "" else "payment" }
                    )

                    if (expandedSection == "payment") {
                        // ── Subtotal ──
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 14.sp, color = TextSecond)
                            Text("₹${"%.2f".format(subtotal)}", fontSize = 14.sp, color = TextPrimary)
                        }

                        // ── Discount Row ──
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
                                            discountText = newValue
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

                        // ── Grand Total ──
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("₹${"%.2f".format(grandTotal)}", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(Modifier.height(6.dp))

                        // ── Paid So Far ──
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid So Far", fontSize = 13.sp, color = TextSecond)
                            Text("₹${"%.2f".format(orderData.paidSoFar)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
                        }
                        Spacer(Modifier.height(4.dp))

                        // ── Balance Due ──
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Due", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecond)
                            Text("₹${"%.2f".format(balanceDue)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
                        }
                    }
                }

                // ── 3. DELIVERY SCHEDULE ──
                SectionCard {
                    SectionHeader(
                        icon = Icons.Default.CalendarMonth,
                        title = "Delivery Schedule",
                        expanded = expandedSection == "delivery",
                        onToggle = { expandedSection = if (expandedSection == "delivery") "" else "delivery" }
                    )

                    if (expandedSection == "delivery") {
                        // ── Trial Date ──
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Trial Date", fontSize = 14.sp, color = TextSecond)
                            Text(
                                orderData.trialDate.ifBlank { "Not Scheduled" },
                                fontSize = 14.sp,
                                color = if (orderData.trialDate.isBlank()) LabelGray else TextPrimary
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        // ── Final Delivery ──
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
                                orderData.deliveryDate.ifBlank { "Not Scheduled" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }

                // ✅ NEW — Floating "Back to Edit" pill button (bottom-start, overlays content)
                // Styled exactly like CustomerDetailScreen's Back FAB button
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF111827)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 50.dp, vertical = 14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 24.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Back to Edit", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.padding(horizontal = 50.dp))
                // ✅ NEW — Floating "Save Order" pill button (bottom-end, overlays content)
                // Styled exactly like CustomerDetailScreen's Next/Update FAB button
                Button(
                    onClick = { buildAndSaveOrder() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 50.dp, vertical = 14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 24.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save Order", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

        }

    }
}

// ─── Helper ──────────────────────────────────────────────────────────────────
private fun String.toIsoDateOrNull(): String? {
    if (isBlank()) return null
    val parts = split("-")
    return if (parts.size == 3) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else null
}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
private fun PriceInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp
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

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = SectionBg),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp), // ✅ CHANGED — top/bottom padding removed, header's own vertical padding handles spacing
            content = content
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    bottomPadding: Dp = 14.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggle() }
            .padding(horizontal = 10.dp, vertical = 15.dp)
    ) {
        // ── Icon badge (small colored square behind icon) ──
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AccentBlueBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = LabelGray,
            modifier = Modifier.size(20.dp)
        )
    }
}