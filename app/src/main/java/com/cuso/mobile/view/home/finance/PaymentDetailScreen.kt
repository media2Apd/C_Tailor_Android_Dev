@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable", "VariableNeverRead"
)
package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar

// ── Colors used only inside this screen ──
private val LabelGray = Color(0xFF9CA3AF)
private val ValueDark = Color(0xFF111827)

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
fun PaymentDetailScreenAR(
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {

        // ── FIXED TOP HEADER ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TitleBar("All Payments", onClose = onClose)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(5.dp))

            // ── Contact Card — name, phone, email, order date in one row ──
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.extraPadding, vertical = 10.dp)
            ) {

                // ── Name + contact row ──
                Text(
                    "Raji",
                    fontSize = tokens.h2,
                    color = title_color
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone),
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize * 0.78f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text("+91 6369460554", fontSize = tokens.caption, color = mutedText)
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_mail_2),
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize * 0.78f)
                    )
                    Spacer(Modifier.width(3.dp))

                    val email = "arjun@royalfurnitures.com"
                    val displayEmail = email.take(20) + "..."

                    Text(
                        text = displayEmail,
                        fontSize = tokens.caption,
                        color = mutedText,
                        maxLines = 1
                    )

                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize * 0.78f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text("20-03-2026", fontSize = tokens.caption, color = mutedText)
                }
            }

            HeaderSec("Payment Information")

            // ── Payment Information Card — each field separated by a thin divider ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                FieldRowColumn("Order ID", "ORD0014")
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

                FieldRowColumn("Customer Name", "Meena Textiles")
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

                FieldRowColumn("Phone", "916369460554")
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

                FieldRowColumn("Order Date", "22/02/2026")
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

                FieldRowColumn("Delivery Date", "12/02/2026")
            }

            HeaderSec("Order Summary")

            // ── Order Summary Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                dummyOrderItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                item.name,
                                fontSize = tokens.bodyMedium,
                                color = title_color
                            )
                            Text(
                                "Qty: ${item.qty} | Rate: ${item.rate}",
                                fontSize = tokens.bodySmall,
                                color = LabelGray
                            )
                        }
                        Text(item.total, fontSize = tokens.bodyMedium, color = title_color)
                    }
                    HorizontalDivider(color = BorderGray)

                }


                DetailFieldRow("Subtotal", "₹4,350")
                FieldRowNormal("Tax(0%)", "₹0")

                HorizontalDivider(color = BorderGray)

                FieldRowNormal("Total Amount", "₹4,350")
                DetailFieldRow("Amount Paid", "₹2,000", valueColor = greentext)

                // ── Balance Due highlighted ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(redBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balance Due", fontSize = tokens.bodyMedium, color = redText)
                    Text("₹2,350", fontSize = tokens.bodyMedium, color = redText)
                }
            }

            HeaderSec("Payment History")

            // ── Payment History Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dummyPaymentHistory.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                entry.date,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = ValueDark
                            )
                            Text(entry.referenceId, fontSize = tokens.caption, color = LabelGray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentModeBadge(entry.mode)
                            Text(entry.amount, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = ValueDark)
                        }
                    }
                    HorizontalDivider(color = BorderGray)

                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Section header — white background strip that separates each card block ──
@Composable
private fun HeaderSec(head: String) {
    val tokens = LocalAppTokens.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
    ) {
        Text(head, fontSize = tokens.bodyLarge, color = title_color)
    }
}

// ── Reusable label-value row ──
@Composable
private fun DetailFieldRow(
    label: String,
    value: String,
    valueColor: Color = ValueDark,
    boldValue: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = tokens.bodySmall, color = LabelGray)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = tokens.bodySmall,
            color = valueColor
        )
    }
}
@Composable
private fun FieldRowColumn(
    label: String,
    value: String,
    valueColor: Color = ValueDark,
    boldValue: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = tokens.bodySmall, color = LabelGray)
        Spacer(Modifier.height(10.dp))
        Text(
            value,
            fontSize = tokens.bodySmall,
            color = valueColor
        )
    }
}
@Composable
private fun FieldRowNormal(
    label: String,
    value: String,
    valueColor: Color = ValueDark,
    boldValue: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = tokens.bodyMedium, color = title_color)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = tokens.bodyMedium,
            color = valueColor
        )
    }
}

// ── UPI / CASH badge ──
@Composable
private fun PaymentModeBadge(mode: String) {
    val tokens = LocalAppTokens.current
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
        Text(mode, fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = text)
    }
}