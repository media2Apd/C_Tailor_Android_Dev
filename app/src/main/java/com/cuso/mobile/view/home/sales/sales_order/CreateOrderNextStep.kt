package com.cuso.mobile.view.home.sales.sales_order

import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.cuso.mobile.R
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.viewmodel.SalesViewModel

// ─── Colors ───────────────────────────────────────────────────────────────────
private val AccentBlue = Color(0xFF4F46E5)
private val AccentBlueBg = Color(0xFFF0EEFE)
private val SuccessGreen = Color(0xFF22C55E)
private val SectionBg = Color(0xFFFFFFFF)
private val PageBg = Color(0xFFF3F4F6)
private val BorderColor = Color(0xFFE5E7EB)
private val LabelGray = Color(0xFF9CA3AF)
private val TextPrimary = Color(0xFF111827)
private val TextSecond = Color(0xFF6B7280)

// ─── Data holder ──────────────────────────────────────────────────────────────
data class OrderReviewData(
    val orderId: String? = null,          // NEW
    val customerId: String,
    val branchId: String? = null,
    val fullName: String,
    val countryCode: String,
    val phone: String,
    val gender: String?=null,
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
    val existingImageUrls: List<String> = emptyList(),   // NEW
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
    val salesViewModel: SalesViewModel=hiltViewModel()
    // ── State ──
    var unitPrices by remember(orderData.garments) {
        mutableStateOf(
            orderData.garments.associate { g ->
                g.id to (if (g.price > 0) g.price.toInt().toString() else "")
            }
        )
    }
    val isEditMode = orderData.orderId != null   // NEW

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

    val actionState by salesOrderViewModel.actionState.collectAsStateWithLifecycle()

    // ✅ FIX — removed the duplicate LaunchedEffect that used to fire onSaveOrder()
    // a second time with an empty/dummy CreateOrderRequest whenever actionState
    // became Success. Navigation now happens ONLY once, from the real
    // createOrder(...) completion callback inside buildAndSaveOrder(), with the
    // actual saved request data. `actionState` is now only used to drive the
    // Save Order button's loading/disabled state below.

    // ── Show a toast if the order creation fails ──
    LaunchedEffect(actionState) {
        if (actionState is OrderActionState.Error) {
            val message = (actionState as OrderActionState.Error).message
            Toast.makeText(context, message.ifBlank { "Failed to save order" }, Toast.LENGTH_LONG).show()
            salesOrderViewModel.resetActionState()
        }
    }

    // ✅ Build the request once, reused by the "Save Order" bottom bar button
    // ✅ Build the request once, reused by the "Save Order" bottom bar button
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
                category = g.categoryId,
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

        // ✅ Common success callback - clears garments after successful save/update
        val onOrderSuccess: (Any?) -> Unit = { _ ->
            // Clear all selected garments from Room database
            salesViewModel.clearAllSelectedGarments()
            // Notify the caller that order was saved
            onSaveOrder(request)
        }

        // ✅ Call based on isEditMode with the success callback
        if (isEditMode) {
            salesOrderViewModel.updateOrder(
                orderId = orderData.orderId,
                request = request,
                existingImages = orderData.existingImageUrls,
                imageParts = context.createImageParts(orderData.designImages),
                voiceNotePart = context.createVoiceNotePart(orderData.voiceNoteUri),
                onSuccess = onOrderSuccess
            )
        } else {
            salesOrderViewModel.createOrder(
                request = request,
                imageParts = context.createImageParts(orderData.designImages),
                voiceNotePart = context.createVoiceNotePart(orderData.voiceNoteUri),
                onSuccess = onOrderSuccess
            )
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
                            ) { onBack()
                            salesViewModel.clearAllSelectedGarments()}
                    )
                }
                HorizontalDivider(color = BorderColor)
            }
        },
        // AFTER
        containerColor = Color.White

    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp),   // reserve space so last section isn't hidden behind the fab
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            // ── 1. BILLING DETAILS ──
            SectionCard {
                SectionHeader(
                    icon = R.drawable.billing,
                    title = "Billing Details",
                    expanded = expandedSection == "billing",
                    onToggle = { expandedSection = if (expandedSection == "billing") "" else "billing" }
                )

                AnimatedVisibility(
                    visible = expandedSection == "billing",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Text(
                            "ITEM",
                            fontSize = 10.sp,
                            color = LabelGray,
                            letterSpacing = 0.06.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        if (orderData.garments.isEmpty()) {
                            Text("No items added", fontSize = 14.sp, color = LabelGray, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            orderData.garments.forEachIndexed { index, garment ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        garment.categoryName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("Qty", fontSize = 11.sp, color = LabelGray, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
                                    Text("Unit Price", fontSize = 11.sp, color = LabelGray, modifier = Modifier.width(110.dp), textAlign = TextAlign.End)
                                }
                                Spacer(Modifier.height(6.dp))

                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                            .background(PageBg)
                                            .padding(vertical = 6.dp),
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
                                                            itemCharges =
                                                                itemCharges.toMutableMap().apply {
                                                                    this[garment.id] =
                                                                        (this[garment.id].orEmpty()).filter { it.id != charge.id }
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
                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }

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
                                                    globalCharges =
                                                        globalCharges.filter { it.id != charge.id }
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

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items: ${orderData.garments.size}", fontSize = 14.sp, color = TextSecond)
                            Text("Subtotal   ₹${"%.2f".format(subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }
            }

            // ── 2. PAYMENT SUMMARY ──
            SectionCard {
                SectionHeader(
                    icon = R.drawable.rupee,
                    title = "Payment Summary",
                    expanded = expandedSection == "payment",
                    onToggle = { expandedSection = if (expandedSection == "payment") "" else "payment" }
                )

                AnimatedVisibility(
                    visible = expandedSection == "payment",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 14.sp, color = TextSecond)
                                Text("₹${"%.2f".format(subtotal)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }

                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Discount", fontSize = 14.sp, color = TextSecond, modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("₹", fontSize = 13.sp, color = LabelGray)
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
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("₹${"%.2f".format(grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Paid So Far", fontSize = 13.sp, color = TextSecond)
                                Text("₹${"%.2f".format(orderData.paidSoFar)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecond)
                                Text("₹${"%.2f".format(balanceDue)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }

            // ── 3. DELIVERY SCHEDULE ──
            SectionCard {
                SectionHeader(
                    icon = R.drawable.delivery,
                    title = "Delivery Schedule",
                    expanded = expandedSection == "delivery",
                    onToggle = { expandedSection = if (expandedSection == "delivery") "" else "delivery" }
                )

                AnimatedVisibility(
                    visible = expandedSection == "delivery",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trial Date", fontSize = 14.sp, color = TextSecond)
                            Text(
                                orderData.trialDate.ifBlank { "Not Scheduled" },
                                fontSize = 14.sp,
                                color = if (orderData.trialDate.isBlank()) LabelGray else TextPrimary
                            )
                        }
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentBlueBg)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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


                Spacer(Modifier.height(12.dp))
            }

            StepNavigationFab(
                showBack = true,
                onBack = onBack,
                backLabel = "Back to Edit",
                backEnabled = actionState !is OrderActionState.Loading,   // ✅ block "Back" while saving
                backWidthFraction = 0.42f,
                trailingWidthFraction = 0.42f,
                trailingAction = TrailingFabAction.Update(
                    isLoading = actionState is OrderActionState.Loading,
                    label = if (isEditMode) "Update Order" else "Save Order",
                    onClick = { buildAndSaveOrder() }
                )
            )
        }
    }
}

// ─── Helper ──────────────────────────────────────────────────────────────────

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
            .height(40.dp)
            .padding(horizontal = 8.dp),
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text("0", fontSize = 13.sp, color = LabelGray)
                    }
                    inner()
                }
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
            .height(40.dp)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text("Charge Name", fontSize = 13.sp, color = LabelGray)
                    }
                    inner()
                }
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
            modifier = Modifier.padding(horizontal = 10.dp),
            content = content
        )
    }
}
@Suppress("UNUSED_PARAMETER")

@Composable
private fun SectionHeader(
    @DrawableRes icon: Int,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    bottomPadding: Dp = 14.dp
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onToggle() }
                .padding(horizontal = 5.dp, vertical = 15.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )

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
                modifier = Modifier.size(25.dp)
            )
        }
        if (!expanded) {
            HorizontalDivider(color = BorderColor)
        }
    }
}