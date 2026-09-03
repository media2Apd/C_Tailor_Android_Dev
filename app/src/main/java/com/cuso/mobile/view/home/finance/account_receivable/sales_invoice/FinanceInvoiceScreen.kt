@file:Suppress("unused")

// ─────────────────────────────────────────────────────────────
// FinanceInvoiceScreen — "All Invoice" list (View All)
// ─────────────────────────────────────────────────────────────

package com.cuso.mobile.view.home.finance.account_receivable.sales_invoice

import android.content.Intent
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.pdfgenerator.InvoicePdfGenerator
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DataCardImage
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.FinanceViewModel
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.viewmodel.ProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.core.content.FileProvider
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.URL

private val InvPrimary = Color(0xFF3B3BF9)
private val InvTextDark = Color(0xFF111827)
private val InvmutedText = Color(0xFF9CA3AF)
private val InvGreen = Color(0xFF16A34A)
private val InvRed = redText
private val InvYellow = Color(0xFFF59E0B)

// ─────────────────────────────────────────────────────────────
// FinanceInvoiceScreen — "All Invoice" list with Infinite Scroll
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceInvoiceScreen(
    onClose: () -> Unit,
    onInvoiceClick: (InvoiceItem) -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    val viewModel: FinanceViewModel = hiltViewModel()

    val invoices by viewModel.invoiceList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingInvoices.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreInvoices.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreInvoices.collectAsStateWithLifecycle()
    val error by viewModel.invoiceError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Fetch initial list
    LaunchedEffect(Unit) {
        viewModel.fetchInvoices()
    }

    // Infinite scroll detection: triggers loadMore when 3 items away from the bottom
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom && canLoadMore && !isLoadingMore && !isLoading) {
                    viewModel.loadMoreInvoices()
                }
            }
    }

    val filteredInvoices = invoices.filter { inv ->
        searchQuery.isBlank() ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.displayCustomerName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All Invoices", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InvTextDark)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = InvTextDark,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClose() }
            )
        }


        // ── Search Bar ──
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search Invoices...",
            accentColor = BluePrimary,
            borderColor = BorderGray,
            textSecondaryColor = TextSecondary,
            onFilterClick = { }
        )
        HorizontalDivider(color = title_border)

        // ── Body ──
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    ListSkeleton()
                }
                error != null -> {
                    AppErrorState(
                        title = "Failed to load finance Invoice",
                        message = "Something went wrong. Please check your connection and try again.",
                        onRetry = { viewModel.fetchInvoices() }
                    )
                }
                filteredInvoices.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = InvmutedText,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("No invoices found", fontSize = 14.sp, color = InvmutedText)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
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

                        // Bottom loading indicator for pagination
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CirculerProgressIndicatorSmall()
                                }
                            }
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
            painter = painterResource( R.drawable.person),
            size = 30.dp,
            backgroundColor = Color.Transparent,
            tint = blackTitle
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
    else -> "Unknown" to InvmutedText
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
                tint = InvmutedText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Raised on ${formatInvoiceDate(invoice.invoiceDate)} | ${invoice.salesOrderId?.let { "Department Production " } ?: "Sales Invoice"}",
            fontSize = 12.5.sp,
            color = InvmutedText
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

    // Root-level notification state — replaces Toast for share/download
    // success & failure, shown via DynamicIslandSuccess/Error below.
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    val bytes = URL(organizationLogoUrl).readBytes()
                    "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
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
        val safeItems = (items ?: emptyList()).map { item ->
            InvoicePdfGenerator.InvoiceItemData(
                description = item.description ?: "Item",
                hsnSku = item.hsnSku ?: "-",
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                discount = item.discount ?: "-",
                tax = item.tax,
                total = item.total
            )
        }

        return InvoicePdfGenerator.InvoiceData(
            invoiceNumber = invoiceNumber,
            invoiceDate = formatInvoiceDate(invoiceDate),
            dueDate = formatInvoiceDate(dueDate ?: invoiceDate),
            status = status,
            customerName = customer?.name ?: customerId?.name ?: customerId?.id ?: "Walk-in Customer",
            billToAddress = customer?.billingAddress ?: shippingAddressSnapshot?.fullAddress ?: "",
            billToPhone = customer?.phone ?: customerId?.mobile ?: "",
            billToEmail = customer?.email ?: customerId?.email ?: "",
            shipToAddress = customer?.shippingAddress ?: shippingAddressSnapshot?.fullAddress ?: customer?.billingAddress ?: "",
            orderReference = salesOrderId?.orderNumber ?: salesOrderId?.id ?: "",
            items = safeItems,
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
            logoUrl = (organization?.logoUrl?.takeIf { it.isNotEmpty() }) ?: logoBase64.ifEmpty { null }
        )
    }

    // Outer wrapper Box: keeps the notification banners above everything
    // else on this screen (including the top bar), at TopCenter position,
    // so they are never clipped.
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("All Invoices", onClose = onClose)

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

                        // ── WebView renders the SAME html used for PDF export ──
                        // (identical pattern to CreateQuotationScreen's preview AndroidView)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .background(whiteBg)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.setSupportZoom(true)
                                        settings.builtInZoomControls = true
                                        settings.displayZoomControls = false
                                        webViewClient = WebViewClient()
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
                            val shareFileName = "share_inv_${invoice.invoiceNumber}_${System.currentTimeMillis()}.pdf"
                            OutlinedButton(
                                onClick = {
                                    pdfGenerator.generatePdfFromHtml(
                                        data = pdfData,
                                        fileName = shareFileName,
                                        saveToDownloads = false
                                    ) { saved ->
                                        if (saved != null && saved.exists()) {
                                            val shareUri = if (saved.file != null) {
                                                FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    saved.file
                                                )
                                            } else {
                                                saved.uri
                                            }
                                            shareUri?.let { uri ->
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(shareIntent, "Share Invoice PDF")
                                                )
                                            }
                                        } else {
                                            errorMessage = "Failed to generate invoice PDF"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Primary)
                            ) {
                                Icon(Icons.Default.Share, null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Share PDF", color = Primary, fontSize = 14.sp)
                            }
                            Button(
                                onClick = {
                                    if (!isDownloading) {
                                        isDownloading = true
                                        pdfGenerator.downloadInvoicePdf(pdfData) { saved ->
                                            isDownloading = false
                                            if (saved != null && saved.exists()) {
                                                successMessage = "Invoice downloaded to Downloads folder"
                                            } else {
                                                errorMessage = "Failed to download invoice"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = whiteBg
                                    )
                                } else {
                                    Icon(Icons.Default.Download, null, tint = whiteBg, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Download ", color = whiteBg, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}