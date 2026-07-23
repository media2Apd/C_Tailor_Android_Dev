//@file:Suppress("UNUSED_PARAMETER", "UNUSED", "RedundantSuppression", "unused")

package com.cuso.mobile.view.home.finance

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.InvoiceItem
import com.cuso.mobile.model.finance.InvoiceViewOneData
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.pdfgenerator.InvoicePdfGenerator
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.DataCardField
import com.cuso.mobile.view.home.reusablecomposables.DataCardImage
import com.cuso.mobile.view.home.reusablecomposables.ListSkeleton
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.view.home.reusablecomposables.SearchFilterBar
import com.cuso.mobile.viewmodel.FinanceViewModel
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.ProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val InvPrimary = Color(0xFF3B3BF9)
private val InvTextDark = Color(0xFF111827)
private val InvTextMuted = Color(0xFF9CA3AF)
private val InvGreen = Color(0xFF16A34A)
private val InvRed = Color(0xFFEF4444)
private val InvYellow = Color(0xFFF59E0B)
private val InvBgLight = Color(0xFFF5F5F7)

// ─────────────────────────────────────────────────────────────
// FinanceInvoiceScreen — "All Invoice" list (View All)
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceInvoiceScreen(
    onClose: () -> Unit,
    onInvoiceClick: (InvoiceItem) -> Unit
) {
    val viewModel: FinanceViewModel = hiltViewModel()

    val invoices by viewModel.invoiceList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingInvoices.collectAsStateWithLifecycle()
    val error by viewModel.invoiceError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchInvoices()
    }

    val filteredInvoices = invoices.filter { inv ->
        searchQuery.isBlank() ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.displayCustomerName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvBgLight)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All Customers", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InvTextDark)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = InvTextDark,
                modifier = Modifier.size(24.dp).clickable { onClose() }
            )
        }

        // ── Breadcrumb ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Finance", fontSize = 12.sp, color = InvTextMuted)
            Text("  >  ", fontSize = 12.sp, color = InvTextMuted)
            Text("Sales Invoice", fontSize = 12.sp, color = InvPrimary, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = "Search Invoices...",
            accentColor = BluePrimary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { /* TODO: open filter drawer */ }
        )

        // ── Body ──
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    ListSkeleton()
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Something went wrong", color = InvRed, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchInvoices() }) { Text("Retry") }
                        }
                    }
                }
                filteredInvoices.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = InvTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No invoices found", fontSize = 14.sp, color = InvTextMuted)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredInvoices, key = { it.id }) { invoice ->
                            InvoiceDataCard(
                                invoice = invoice,
                                onClick = {
                                    viewModel.selectInvoice(invoice)
                                    onInvoiceClick(invoice)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceDataCard(invoice: InvoiceItem, onClick: () -> Unit) {
    val (badgeText, badgeColor) = statusColorsOfInvoice(invoice.status)

    DataCard(
        item = invoice,
        image = DataCardImage(
            vector = Icons.Default.Person,
            size = 20.dp,
            backgroundColor = Color.Transparent,
            tint = InvTextMuted
        ),
        title = invoice.invoiceNumber,
        subtitle = "${invoice.displayCustomerName} • Customer",
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
                text = formatInvoiceDate(invoice.dueDate ?: invoice.invoiceDate)
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

private fun statusColorsOfInvoice(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "paid" -> "Active" to InvGreen
    "partial" -> "Partial" to InvYellow
    "unpaid", "overdue" -> "Overdue" to InvRed
    else -> "Unknown" to InvTextMuted
}

private fun formatInvoiceDate(dateString: String): String {
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
private fun InvoiceHeaderCard(invoice: InvoiceViewOneData) {
    val (badgeText, badgeColor) = statusColorsOfInvoice(invoice.status)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "INVOICE-${invoice.invoiceNumber.filter { it.isDigit() }.ifEmpty { invoice.id.takeLast(5) }}",
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
                tint = InvTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Raised on ${formatInvoiceDate(invoice.invoiceDate)} | ${invoice.salesOrderId?.let { "Department Production " } ?: "Sales Invoice"}",
            fontSize = 12.5.sp,
            color = InvTextMuted
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE))
}

// ─────────────────────────────────────────────────────────────
// InvoiceDetailScreen — WebView preview, same pattern as
// CreateQuotationScreen's Step3PricingSummary (AndroidView + WebView
// loading pdfGenerator.buildXHtml(), no duplicated static Compose UI).
// `token` is needed to load the organization/logo, same as the
// quotation screen's ProfileViewModel.loadOrganization(token) call.
// ─────────────────────────────────────────────────────────────
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    token: String,
    onClose: () -> Unit
) {
    val viewModel: FinanceViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val context = LocalContext.current
    val pdfGenerator = remember { InvoicePdfGenerator(context) }

    val invoiceDetail by viewModel.invoiceDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingInvoiceDetail.collectAsStateWithLifecycle()
    val error by viewModel.invoiceDetailError.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        viewModel.fetchInvoiceDetail(invoiceId)
    }
    LaunchedEffect(Unit) {
        profileViewModel.loadOrganization(token)
    }

    // ── org logo → base64 data URI, same pattern as CreateQuotationScreen ──
    val organizationLogoUrl = remember(profileState) {
        (profileState as? ProfileUiState.Success)
            ?.data?.organization?.organizationPicture ?: ""
    }
    var logoBase64 by remember { mutableStateOf("") }
    LaunchedEffect(organizationLogoUrl) {
        if (organizationLogoUrl.isNotEmpty()) {
            logoBase64 = withContext(Dispatchers.IO) {
                try {
                    val bytes = java.net.URL(organizationLogoUrl).readBytes()
                    "data:image/png;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    // ── Maps InvoiceViewOneData (API response) -> InvoicePdfGenerator.InvoiceData ──
    // Every value here comes from the API; only truly missing optional
    // fields fall back to an empty/neutral default.
    fun InvoiceViewOneData.toPdfData(): InvoicePdfGenerator.InvoiceData {
        return InvoicePdfGenerator.InvoiceData(
            invoiceNumber = invoiceNumber,
            invoiceDate = formatInvoiceDate(invoiceDate),
            dueDate = formatInvoiceDate(dueDate ?: invoiceDate),
            status = status,
            customerName = customer?.name ?: customerId?.name ?: customerId?.id ?: "Walk-in Customer",            billToAddress = customer?.billingAddress ?: "",
            billToPhone = customer?.phone ?: "",
            billToEmail = customer?.email ?: "",
            shipToAddress = customer?.shippingAddress ?: customer?.billingAddress ?: "",
            orderReference = salesOrderId ?: "",
            items = items.map { item ->
                InvoicePdfGenerator.InvoiceItemData(
                    description = item.description,
                    hsnSku = item.hsnSku ?: "-",
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    discount = item.discount ?: "-",
                    tax = item.tax,
                    total = item.total
                )
            },
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            shippingAmount = shippingAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentMethod = paymentMethod ?: "Bank Transfer / Card / UPI",
            bankName = bankDetails?.bankName ?: "",
            accountNo = bankDetails?.accountNo ?: "",
            ifscSwift = bankDetails?.ifscSwift ?: "",
            termsAndConditions = termsAndConditions
                ?: "Payment due within 30 days of invoice date. Late fees may apply.",
            companyName = organization?.name ?: "",
            companyAddress = organization?.address ?: "",
            companyEmail = organization?.email ?: "",
            companyPhone = organization?.phone ?: "",
            companyGst = organization?.gstNumber ?: "",
            // prefer the invoice's own organization logo, fall back to the
            // profile-loaded org logo (same source used in the quotation screen)
            logoUrl = (organization?.logoUrl?.takeIf { it.isNotEmpty() }) ?: logoBase64.ifEmpty { null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvBgLight)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = InvTextDark,
                modifier = Modifier.size(24.dp).clickable { onClose() }
            )
            Spacer(Modifier.width(12.dp))
            Text("All Invoice", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InvTextDark)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = InvTextDark,
                modifier = Modifier.size(24.dp).clickable { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InvPrimary)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = InvRed, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "Failed to load invoice", color = InvRed, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.fetchInvoiceDetail(invoiceId) }) {
                            Text("Retry")
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onClose) {
                            Text("Go Back")
                        }
                    }
                }
            }
            invoiceDetail != null -> {
                val invoice = invoiceDetail!!
                val pdfData = remember(invoice, logoBase64) { invoice.toPdfData() }

                Column(modifier = Modifier.fillMaxSize()) {
                    InvoiceHeaderCard(invoice = invoice)

                    // ── Action icons (Download / Share) — same as quotation screen ──
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp),
//                        horizontalArrangement = Arrangement.End,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = {
//                                if (!isDownloading) {
//                                    isDownloading = true
//                                    pdfGenerator.downloadInvoicePdf(pdfData) { saved ->
//                                        isDownloading = false
//                                        val msg = if (saved != null && saved.exists()) {
//                                            "Invoice downloaded to Downloads folder"
//                                        } else {
//                                            "Failed to download invoice"
//                                        }
//                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                            },
//                            enabled = !isDownloading,
//                            modifier = Modifier.size(40.dp)
//                        ) {
//                            if (isDownloading) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(20.dp),
//                                    strokeWidth = 2.dp,
//                                    color = InvPrimary
//                                )
//                            } else {
//                                Icon(Icons.Default.Download, contentDescription = "Download", tint = InvPrimary)
//                            }
//                        }
//
//                        IconButton(
//                            onClick = {
//                                pdfGenerator.generatePdfFromHtml(
//                                    data = pdfData,
//                                    saveToDownloads = false
//                                ) { saved ->
//                                    if (saved != null && saved.exists()) {
//                                        val shareUri = if (saved.file != null) {
//                                            androidx.core.content.FileProvider.getUriForFile(
//                                                context,
//                                                "${context.packageName}.fileprovider",
//                                                saved.file
//                                            )
//                                        } else {
//                                            saved.uri
//                                        }
//                                        shareUri?.let { uri ->
//                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
//                                                type = "application/pdf"
//                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
//                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                                            }
//                                            context.startActivity(
//                                                android.content.Intent.createChooser(shareIntent, "Share Invoice PDF")
//                                            )
//                                        }
//                                    } else {
//                                        Toast.makeText(context, "Failed to generate invoice PDF", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                            },
//                            modifier = Modifier.size(40.dp)
//                        ) {
//                            Icon(Icons.Default.Share, contentDescription = "Share", tint = InvPrimary)
//                        }
//                    }

                    // ── WebView renders the SAME html used for PDF export ──
                    // (identical pattern to CreateQuotationScreen's preview AndroidView)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .background(Color.White)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                android.webkit.WebView(ctx).apply {
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    webViewClient = android.webkit.WebViewClient()
                                    loadDataWithBaseURL(
                                        null,
                                        pdfGenerator.buildInvoiceHtml(pdfData),
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(
                                    null,
                                    pdfGenerator.buildInvoiceHtml(pdfData),
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // ── Bottom action buttons ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pdfGenerator.generatePdfFromHtml(
                                    data = pdfData,
                                    saveToDownloads = false
                                ) { saved ->
                                    if (saved != null && saved.exists()) {
                                        val shareUri = if (saved.file != null) {
                                            androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                saved.file
                                            )
                                        } else {
                                            saved.uri
                                        }
                                        shareUri?.let { uri ->
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                android.content.Intent.createChooser(shareIntent, "Share Invoice PDF")
                                            )
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to generate invoice PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, InvPrimary)
                        ) {
                            Icon(Icons.Default.Share, null, tint = InvPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share PDF", color = InvPrimary, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {
                                if (!isDownloading) {
                                    isDownloading = true
                                    pdfGenerator.downloadInvoicePdf(pdfData) { saved ->
                                        isDownloading = false
                                        val msg = if (saved != null && saved.exists()) {
                                            "Invoice downloaded to Downloads folder"
                                        } else {
                                            "Failed to download invoice"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InvPrimary)
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Download ", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}