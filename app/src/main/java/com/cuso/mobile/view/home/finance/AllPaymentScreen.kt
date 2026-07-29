package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary

// ── Static data model ──
data class PaymentItem(
    val paymentId: String,
    val customerName: String,
    val status: String,
    val date: String,
    val paymentMode: String,
    val amount: String,
    val allocatedInvoice: String
)

// ── Static dummy list ──
private val dummyPayments = listOf(
    PaymentItem("PAY-001", "Ravi", "Completed", "25 Feb 2026", "Cash", "₹500", "INV-001"),
    PaymentItem("PAY-001", "Ravi", "Completed", "25 Feb 2026", "Cash", "₹500", "INV-001"),
    PaymentItem("PAY-001", "Ravi", "Completed", "25 Feb 2026", "Cash", "₹500", "INV-001"),
    PaymentItem("PAY-001", "Ravi", "Completed", "25 Feb 2026", "Cash", "₹500", "INV-001")
)

private fun statusColorOf(status: String): Color = when (status.lowercase()) {
    "completed" -> Color(0xFF3B3BF9)
    "pending" -> Color(0xFFF59E0B)
    "failed" -> Color(0xFFEF4444)
    else -> Color(0xFF6B7280)
}

@Composable
fun AllPaymentScreen(
    onClose: () -> Unit = {},
    onViewPayment: (PaymentItem) -> Unit = {},
    onBreadCrumbClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPayments = dummyPayments.filter { p ->
        searchQuery.isBlank() ||
                p.customerName.contains(searchQuery, ignoreCase = true) ||
                p.paymentId.contains(searchQuery, ignoreCase = true)
    }

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
                modifier = Modifier.size(22.dp)
                    .clickable{
                        onClose()
                    }
            )
        }

        // ── Breadcrumb + Search ──
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            ScreenBreadcrumb(
                segments = listOf("Finance", "Purchase Invoice"),
                onClick = { onBreadCrumbClick() }
            )
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = "Search Customers...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { }
            )
        }

        // ── Payment List ──
        if (filteredPayments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No payments found", color = Color.Gray, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                items(filteredPayments) { payment ->
                    DataCard(
                        item = payment,
                        topBadgeText = payment.status,
                        topBadgeTextColor = statusColorOf(payment.status),
                        topBadgeBgColor = statusColorOf(payment.status).copy(alpha = 0.14f),
                        title = payment.paymentId,
                        subtitle = "${payment.customerName} • Customer",
                        footerFields = listOf(
                            DataCardField(icon = Icons.Default.CalendarMonth, text = payment.date),
                            DataCardField(icon = Icons.Default.Payments, text = payment.paymentMode),
                            DataCardField(icon = Icons.Default.CurrencyRupee, text = payment.amount),
                            DataCardField(icon = Icons.Default.Description, text = payment.allocatedInvoice)
                        ),
                        actions = listOf(
                            MenuAction("View", Icons.Default.Visibility) { onViewPayment(payment) }
                        )
                    )
                }
            }
        }
    }
}