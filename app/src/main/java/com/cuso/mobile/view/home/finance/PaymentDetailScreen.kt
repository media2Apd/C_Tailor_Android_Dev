package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Static data models ──
data class PaymentHistoryEntry(
    val date: String,
    val referenceId: String,
    val mode: String,
    val amount: String
)

data class OrderLineItem(
    val name: String,
    val qty: Int,
    val rate: String,
    val total: String
)

// ── Static dummy data (mirrors the screenshot) ──
private val dummyOrderItems = listOf(
    OrderLineItem("Cotton Saree", 3, "₹1,200", "₹3,600"),
    OrderLineItem("Blouse Stitching", 3, "₹250", "₹750")
)

private val dummyPaymentHistory = listOf(
    PaymentHistoryEntry("12/02/2026", "ID: UPI452190", "UPI", "₹1,000"),
    PaymentHistoryEntry("14/02/2026", "ID: CASH17712", "CASH", "₹1,000")
)

@Composable
fun PaymentDetailScreen(
    onClose: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

        // ── FIXED TOP HEADER ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("All Payment", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(22.dp).run { this }
                    .clickable {
                        onClose()
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Contact Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Raji", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("916369460554", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("arjun@royalfurnitures.com", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("20-03-2026", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }

            // ── Payment Information Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Payment Information", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                DetailFieldRow("Order ID", "ORD0014")
                DetailFieldRow("Customer Name", "Meena Textiles")
                DetailFieldRow("Phone", "916369460554")
                DetailFieldRow("Order Date", "22/02/2026")
                DetailFieldRow("Delivery Date", "12/02/2026")
            }

            // ── Order Summary Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Order Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                dummyOrderItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                            Text("Qty: ${item.qty} | Rate: ${item.rate}", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                        Text(item.total, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                DetailFieldRow("Subtotal", "₹4,350", boldValue = true)
                DetailFieldRow("Tax(0%)", "₹0", boldValue = true)
                DetailFieldRow("Total Amount", "₹4,350", boldValue = true)
                DetailFieldRow("Amount Paid", "₹2,000", valueColor = Color(0xFF16A34A), boldValue = true)

                // ── Balance Due highlighted ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFDEDEE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balance Due", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626))
                    Text("₹2,350", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }

            // ── Payment History Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Payment History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))

                dummyPaymentHistory.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(entry.date, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                            Text(entry.referenceId, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentModeBadge(entry.mode)
                            Text(entry.amount, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Reusable label-value row ──
@Composable
private fun DetailFieldRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827),
    boldValue: Boolean = false
) {
    Column {
        Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (boldValue) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ── UPI / CASH badge ──
@Composable
private fun PaymentModeBadge(mode: String) {
    val (bg, text) = when (mode.uppercase()) {
        "UPI" -> Color(0xFFDCE9FF) to Color(0xFF2563EB)
        "CASH" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        else -> Color(0xFFE5E7EB) to Color(0xFF6B7280)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(mode, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = text)
    }
}