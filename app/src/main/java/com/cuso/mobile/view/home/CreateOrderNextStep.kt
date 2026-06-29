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

// ─── Entry Point ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderNextStep(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = { CreateOrderTopBar(onBack) },
        bottomBar = { CreateOrderBottomBar() },
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
            CustomerDetailsSection()
            GarmentsSection()
            BillingDetailsSection()
            DeliveryScheduleSection()
            PaymentSummarySection()
            ActionButtons(onBack)
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrderTopBar(onBack: () -> Unit) {
    Column {
        // Search toolbar
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search anything...", fontSize = 13.sp, color = LabelGray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = LabelGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderColor,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = PageBg,
                        unfocusedContainerColor = PageBg
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextSecond)
                }
            },
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = TextSecond) }
                IconButton(onClick = {}) { Icon(Icons.Default.Notifications, null, tint = TextSecond) }
                IconButton(onClick = {}) { Icon(Icons.Default.CalendarMonth, null, tint = TextSecond) }
                IconButton(onClick = {}) { Icon(Icons.Default.Settings, null, tint = TextSecond) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SectionBg)
        )
        HorizontalDivider(color = BorderColor)

        // Page header
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
private fun CustomerDetailsSection() {
    SectionCard {
        SectionHeader(icon = Icons.Default.Info, title = "Customer Details")
        LabelValue(label = "FULL NAME", value = "nithishkumar")
        Spacer(Modifier.height(8.dp))
        LabelValue(label = "CONTACT INFO", value = "+91 919345483369")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { LabelValue(label = "GENDER / PROFILE", value = "Male / Men") }
            Box(Modifier.weight(1f)) {
                LabelValue(label = "SHIPPING ADDRESS", value = "chennai", bold = false)
            }
        }
    }
}

// ─── Garments ─────────────────────────────────────────────────────────────────
@Composable
private fun GarmentsSection() {
    SectionCard {
        SectionHeader(icon = Icons.Default.Checkroom, title = "Garments")
        Text("Pant", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { MeasurementField(label = "CHEST", value = "12", unit = "inch") }
            Box(Modifier.weight(1f)) { MeasurementField(label = "SLEEVE LENGTH", value = "12", unit = "inch") }
        }
    }
}

// ─── Billing Details ──────────────────────────────────────────────────────────
@Composable
private fun BillingDetailsSection() {
    SectionCard {
        SectionHeader(icon = Icons.Default.Description, title = "Billing Details")

        // Column headers
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ITEM", fontSize = 10.sp, color = LabelGray, modifier = Modifier.weight(1f))
            Text("QTY", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(48.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("UNIT PRICE (₹)", fontSize = 10.sp, color = LabelGray, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 6.dp))

        // Billing row
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Pant", fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .background(PageBg)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("04", fontSize = 13.sp, color = TextPrimary)
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
                Text("0", fontSize = 13.sp, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
            Text("+ Add Item Charge", color = AccentBlue, fontSize = 13.sp)
        }

        Spacer(Modifier.height(4.dp))
        Text("GLOBAL / ADDITIONAL CHARGES", fontSize = 10.sp, color = LabelGray, letterSpacing = 0.06.sp)
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
            Text("+ Add Global Charge", color = AccentBlue, fontSize = 13.sp)
        }

        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Items: 1", fontSize = 13.sp, color = TextSecond)
            Text("Subtotal   ₹0", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

// ─── Delivery Schedule ────────────────────────────────────────────────────────
@Composable
private fun DeliveryScheduleSection() {
    SectionCard {
        SectionHeader(icon = Icons.Default.CalendarMonth, title = "Delivery Schedule")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Trial Date", fontSize = 14.sp, color = TextSecond)
            Text("Not Scheduled", fontSize = 14.sp, color = LabelGray)
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
            Text("2026-06-30", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AccentBlue)
        }
    }
}

// ─── Payment Summary ──────────────────────────────────────────────────────────
@Composable
private fun PaymentSummarySection() {
    SectionCard {
        SectionHeader(icon = Icons.Default.CurrencyRupee, title = "Payment Summary")

        PaymentRow(label = "Subtotal", value = "₹0.00")
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
                Text("0", fontSize = 13.sp, color = TextPrimary)
            }
        }

        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("₹0.00", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Paid So Far", fontSize = 13.sp, color = TextSecond)
            Text("₹0", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Balance Due", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecond)
            Text("₹0", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
        }
    }
}

// ─── Action Buttons ───────────────────────────────────────────────────────────
@Composable
private fun ActionButtons(onBack: () -> Unit) {
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
            onClick = {},
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Save Order", fontSize = 14.sp)
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────
@Composable
private fun CreateOrderBottomBar() {
    HorizontalDivider(color = BorderColor)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("v1.0", fontSize = 12.sp, color = LabelGray)
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(SuccessGreen))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("Feedback", "Refresh", "Activity", "Accessibility", "Help").forEach {
                Text(it, fontSize = 12.sp, color = LabelGray)
            }
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
        Row() {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary,modifier=Modifier. alignByBaseline())
            Spacer(Modifier.width(4.dp))
            Text(unit, fontSize = 12.sp, color = LabelGray,modifier=Modifier. alignByBaseline())
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