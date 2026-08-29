@file:Suppress("unused")

package com.cuso.mobile.view.home.finance.account_payable.purchase_invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.title_border

private val InvPrimary = Color(0xFF3B3BF9)
private val InvTextDark = Color(0xFF111827)
private val InvmutedText = Color(0xFF9CA3AF)
private val InvGreen = Color(0xFF16A34A)
private val InvRed = Color(0xFFEF4444)
private val InvYellow = Color(0xFFF59E0B)

// Static data model — no API, sample data only
data class PurchaseInvoiceItem(
    val id: String,
    val invoiceNumber: String,
    val displaySupplierName: String,
    val status: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val invoiceDate: String,
    val dueDate: String?
)

// Static sample dataset for preview / offline UI
private fun samplePurchaseInvoices(): List<PurchaseInvoiceItem> = listOf(
    PurchaseInvoiceItem(
        id = "1",
        invoiceNumber = "PINV-1001",
        displaySupplierName = "Om Textiles Pvt Ltd",
        status = "paid",
        totalAmount = 45000.0,
        paidAmount = 45000.0,
        balanceAmount = 0.0,
        invoiceDate = "2026-06-01",
        dueDate = "2026-06-15"
    ),
    PurchaseInvoiceItem(
        id = "2",
        invoiceNumber = "PINV-1002",
        displaySupplierName = "Shree Fabrics",
        status = "partial",
        totalAmount = 30000.0,
        paidAmount = 15000.0,
        balanceAmount = 15000.0,
        invoiceDate = "2026-06-05",
        dueDate = "2026-06-20"
    ),
    PurchaseInvoiceItem(
        id = "3",
        invoiceNumber = "PINV-1003",
        displaySupplierName = "Global Thread Traders",
        status = "unpaid",
        totalAmount = 52000.0,
        paidAmount = 0.0,
        balanceAmount = 52000.0,
        invoiceDate = "2026-05-20",
        dueDate = "2026-06-05"
    ),
    PurchaseInvoiceItem(
        id = "4",
        invoiceNumber = "PINV-1004",
        displaySupplierName = "Om Textiles Pvt Ltd",
        status = "paid",
        totalAmount = 18500.0,
        paidAmount = 18500.0,
        balanceAmount = 0.0,
        invoiceDate = "2026-06-08",
        dueDate = "2026-06-18"
    )
)

// ─────────────────────────────────────────────────────────────
// PurchaseInvoiceScreen — "All Purchase Invoices" list (static data)
// ─────────────────────────────────────────────────────────────
@Composable
fun PurchaseInvoiceScreen(
    onClose: () -> Unit,
    onInvoiceClick: (PurchaseInvoiceItem) -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    // Static in-memory list — no ViewModel, no API call
    val invoices = remember { samplePurchaseInvoices() }

    var searchQuery by remember { mutableStateOf("") }

    val filteredInvoices = invoices.filter { inv ->
        searchQuery.isBlank() ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.displaySupplierName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
           TitleBar("All Invoices", onClose = onClose)
        }

        // Breadcrumb

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Purchase Invoices...",
            accentColor = BluePrimary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { /* filter drawer not wired in static version */ }
        )
        HorizontalDivider(color = title_border)

        // Body — static list, no loading/error states needed
        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredInvoices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = InvmutedText, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No invoices found", fontSize = 14.sp, color = InvmutedText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        PurchaseInvoiceDataCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseInvoiceDataCard(invoice: PurchaseInvoiceItem, onClick: () -> Unit) {
    val (badgeText, badgeColor) = statusColorsOfPurchaseInvoice(invoice.status)

    DataCard(
        item = invoice,
        image = DataCardImage(
            painter = painterResource(R.drawable.person),
            size = 30.dp,
            backgroundColor = Color.Transparent,
            tint = blackTitle
        ),
        title = invoice.invoiceNumber,
        subtitle = "${invoice.displaySupplierName} • Supplier",
        topBadgeText = badgeText,
        topBadgeTextColor = badgeColor,
        topBadgeBgColor = badgeColor.copy(alpha = 0.14f),
        topBadgeInline = true,
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(label = "Amount", text = "₹${formatIndianNumber(invoice.totalAmount)}"),
            DataCardField(label = "Paid", text = "₹${formatIndianNumber(invoice.paidAmount)}"),
            DataCardField(label = "Balance", text = "₹${formatIndianNumber(invoice.balanceAmount)}"),
            DataCardField(
                label = "Due Date",
                text = formatPurchaseInvoiceDate(invoice.dueDate ?: invoice.invoiceDate)
            )
        ),
        actions = listOf(
            MenuAction(
                label = "View",
                icon = Icons.Default.Visibility,
                onClick = onClick
            )
        ),
        onClick = { onClick() }
    )
}

private fun statusColorsOfPurchaseInvoice(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "paid" -> "Active" to InvGreen
    "partial" -> "Partial" to InvYellow
    "unpaid", "overdue" -> "Overdue" to InvRed
    else -> "Unknown" to InvmutedText
}

private fun formatPurchaseInvoiceDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            val month = when (parts[1]) {
                "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
                "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
                "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                else -> parts[1]
            }
            "${parts[2]} $month ${parts[0]}"
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}

@Composable
private fun PurchaseInvoiceHeaderCard(invoice: PurchaseInvoiceViewOneData) {
    val (badgeText, badgeColor) = statusColorsOfPurchaseInvoice(invoice.status)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PINVOICE-${invoice.invoiceNumber.filter { it.isDigit() }.ifEmpty { invoice.id.takeLast(5) }}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InvTextDark
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(badgeText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
                }
            }
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More",
                tint = InvmutedText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Raised on ${formatPurchaseInvoiceDate(invoice.invoiceDate)} | Purchase Invoice",
            fontSize = 12.5.sp,
            color = InvmutedText
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE))
}

// Static model for the detail screen — replace with real fields as needed
data class PurchaseInvoiceViewOneData(
    val id: String,
    val invoiceNumber: String,
    val status: String,
    val invoiceDate: String
)

// ─────────────────────────────────────────────────────────────
// PurchaseInvoiceDetailScreen — static placeholder detail view
// (no WebView/PDF wiring here since the source screen's PDF
// pipeline is API-driven; swap in a real generator when ready)
// ─────────────────────────────────────────────────────────────
@Composable
fun PurchaseInvoiceDetailScreen(
    invoiceId: String,
    onClose: () -> Unit
) {
    // Static sample detail — replace with real lookup by invoiceId later
    val invoice = remember {
        PurchaseInvoiceViewOneData(
            id = invoiceId,
            invoiceNumber = "PINV-1001",
            status = "paid",
            invoiceDate = "2026-06-01"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("All Invoices", onClose = onClose)
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        PurchaseInvoiceHeaderCard(invoice = invoice)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
                .background(whiteBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Invoice preview placeholder", color = InvmutedText, fontSize = 14.sp)
        }

        // Bottom action buttons — static, no PDF wiring
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* wire share when PDF generator is ready */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Primary)
            ) {
                Icon(Icons.Default.Share, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share PDF", color = Primary, fontSize = 14.sp)
            }
            Button(
                onClick = { /* wire download when PDF generator is ready */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Download, null, tint = whiteBg, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Download ", color = whiteBg, fontSize = 14.sp)
            }
        }
    }
}