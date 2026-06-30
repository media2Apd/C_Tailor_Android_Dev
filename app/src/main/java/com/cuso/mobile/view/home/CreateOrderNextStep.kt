package com.example.tailorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.database.entities.SelectedGarment

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

// ─── Data holder passed in from CreateOrderScreen ──────────────────────────────
// Fill this from the state you already collect in CreateOrderScreen.kt and pass
// it down when navigating to this "next step" screen. No more static text.
data class OrderReviewData(
    val fullName: String,
    val countryCode: String,
    val phone: String,
    val gender: String,
    val dressFor: String,
    val address: String,
    val garments: List<SelectedGarment>,
    val trialDate: String,      // empty string => "Not Scheduled"
    val deliveryDate: String,   // empty string => "Not Scheduled"
    val discount: Double = 0.0,
    val paidSoFar: Double = 0.0
)

// ─── Entry Point ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderNextStep(
    orderData: OrderReviewData,
    onBack: () -> Unit = {},
    onSaveOrder: () -> Unit = {}
) {
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
            BillingDetailsSection(orderData.garments, orderData.discount)
            DeliveryScheduleSection(orderData.trialDate, orderData.deliveryDate)
            PaymentSummarySection(orderData.garments, orderData.discount, orderData.paidSoFar)
            ActionButtons(onBack, onSaveOrder)
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
            // NOTE: SelectedGarment doesn't currently expose chest/sleeve length fields.
            // Once you add a `measurements: List<MeasurementField>` (or similar) to
            // SelectedGarment, swap the placeholders below for the real values, e.g.:
            // val chest = garment.measurements.find { it.id == "chest" }?.value ?: "-"
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

// ─── Billing Details ──────────────────────────────────────────────────────────
@Composable
private fun BillingDetailsSection(garments: List<SelectedGarment>, discount: Double) {
    val subtotal = garments.sumOf { it.price * it.quantity }

    SectionCard {
        SectionHeader(icon = Icons.Default.Description, title = "Billing Details")

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ITEM", fontSize = 10.sp, color = LabelGray, modifier = Modifier.weight(1f))
            Text("QTY", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(48.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("UNIT PRICE (₹)", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 6.dp))

        if (garments.isEmpty()) {
            Text("No items added", fontSize = 13.sp, color = LabelGray)
        } else {
            garments.forEach { garment ->
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
                    Row(
                        modifier = Modifier
                            .width(90.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                            .background(PageBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("₹", fontSize = 13.sp, color = LabelGray)
                        Spacer(Modifier.width(4.dp))
                        Text(garment.price.toInt().toString(), fontSize = 13.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Items: ${garments.size}", fontSize = 13.sp, color = TextSecond)
            Text("Subtotal   ₹${"%.2f".format(subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
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

// ─── Payment Summary ──────────────────────────────────────────────────────────
@Composable
private fun PaymentSummarySection(garments: List<SelectedGarment>, discount: Double, paidSoFar: Double) {
    val subtotal = garments.sumOf { it.price * it.quantity }
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
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("- ₹", fontSize = 13.sp, color = LabelGray)
                Spacer(Modifier.width(4.dp))
                Text(discount.toInt().toString(), fontSize = 13.sp, color = TextPrimary)
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
private fun ActionButtons(onBack: () -> Unit, onSaveOrder: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(8.dp),
            border = ButtonDefaults.outlinedButtonBorder,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text("Back to Edit", fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Button(
            onClick = onSaveOrder,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Save Order", fontSize = 14.sp)
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