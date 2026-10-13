@file:Suppress("unused", "AssignedValueIsNeverRead")

// ─────────────────────────────────────────────────────────────
// FinanceInvoiceScreen — Adaptive Invoice List & Detail Screens
// ─────────────────────────────────────────────────────────────

package com.cuso.tailor.view.home.finance.account_receivable.sales_invoice

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Print
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.InvoiceItem
import com.cuso.tailor.model.finance.InvoiceViewOneData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.view.home.pdfgenerator.InvoicePdfGenerator
import com.cuso.tailor.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.net.URL

// ─────────────────────────────────────────────────────────────
// 1. FinanceInvoiceScreen — "All Invoices" List with Infinite Scroll
// ─────────────────────────────────────────────────────────────
@Composable
fun FinanceInvoiceScreen(
    onClose: () -> Unit,
    onInvoiceClick: (InvoiceItem) -> Unit,
    onCreateInvoice: () -> Unit = {},
    onRecordPayment: (InvoiceViewOneData) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: FinanceViewModel = hiltViewModel()

    val invoices by viewModel.invoiceList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingInvoices.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreInvoices.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreInvoices.collectAsStateWithLifecycle()
    val error by viewModel.invoiceError.collectAsStateWithLifecycle()

    // Delete state observation
    val deleteInvoiceState by viewModel.deleteInvoiceState.collectAsStateWithLifecycle()
    var invoiceToDelete by remember { mutableStateOf<InvoiceItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Fetch initial list
    LaunchedEffect(Unit) {
        viewModel.fetchInvoices()
    }

    // Handle delete API response feedback
    LaunchedEffect(deleteInvoiceState) {
        when (val state = deleteInvoiceState) {
            is DeleteInvoiceState.Success -> {
                DynamicIslandManager.showSuccess(state.message)
                invoiceToDelete = null
                viewModel.resetDeleteInvoiceState()
            }
            is DeleteInvoiceState.Error -> {
                DynamicIslandManager.showError(state.message)
                viewModel.resetDeleteInvoiceState()
            }
            else -> Unit
        }
    }

    // Pagination listener when scrolling reaches near bottom
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

    // Draggable FAB for creating invoice
    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Create Invoice",
            icon = Icons.Default.Add,
            onClick = onCreateInvoice
        ),
        fabVisible = !isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar(title = "All Invoices", onClose = onClose)
            }

            // ── Search & Filter Bar ──
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

            // ── Screen Body Content ──
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.iconSize * 2.2f)
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                                Text(
                                    text = "No invoices found",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = mutedText
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = tokens.extraPadding * 0.5f, bottom = tokens.buttonHeight * 2f)
                        ) {
                            items(filteredInvoices, key = { it.id }) { invoice ->
                                InvoiceDataCard(
                                    invoice = invoice,
                                    onClick = {
                                        viewModel.selectInvoice(invoice)
                                        onInvoiceClick(invoice)
                                    },
                                    onDelete = {
                                        invoiceToDelete = invoice
                                    }
                                )
                            }

                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(tokens.screenPadding),
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

    // ── Delete Confirmation Dialog ──
    if (invoiceToDelete != null) {
        val invoice = invoiceToDelete!!
        DeleteModel(
            title = "Delete Invoice",
            message = "Are you sure you want to delete invoice ${invoice.invoiceNumber}? This action cannot be reversed.",
            onDismiss = { invoiceToDelete = null },
            onDelete = {
                viewModel.deleteInvoice(invoice.id)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 2. Invoice List Item Card (with View and Delete Actions)
// ─────────────────────────────────────────────────────────────
@Composable
private fun InvoiceDataCard(
    invoice: InvoiceItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val (badgeText, badgeColor) = statusColorsOfInvoice(invoice.status)

    DataCard(
        item = invoice,
        image = DataCardImage(
            painter = painterResource(R.drawable.person),
            size = tokens.imageDp * 0.5f,
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
        actions = buildList {
            add(
                MenuAction(
                    label = "View",
                    icon = Icons.Default.Visibility,
                    onClick = onClick
                )
            )
            // Show Delete option only when invoice status is Draft
            if (invoice.status.equals("draft", ignoreCase = true)) {
                add(
                    MenuAction(
                        label = "Delete",
                        icon = Icons.Outlined.Delete,
                        onClick = onDelete
                    )
                )
            }
        },
        onClick = { onClick() }
    )
}

// Status color resolver
private fun statusColorsOfInvoice(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "sent" -> "Sent" to Primary
    "paid" -> "Paid" to darkGreenBg
    "draft" -> "Draft" to yellowText
    "void", "voided" -> "Void" to redText
    else -> "Unknown" to mutedText
}

// Date formatter
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

// ─────────────────────────────────────────────────────────────
// 3. Invoice Header Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun InvoiceHeaderCard(invoice: InvoiceViewOneData) {
    val tokens = LocalAppTokens.current
    val (badgeText, badgeColor) = statusColorsOfInvoice(invoice.status)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = invoice.invoiceNumber,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.width(tokens.extraPadding))

            val pillBackground = if (invoice.status.equals("sent", ignoreCase = true)) {
                primary_light
            } else {
                badgeColor.copy(alpha = 0.10f)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(pillBackground)
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.25f)
            ) {
                Text(
                    text = badgeText,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = badgeColor
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

        Text(
            text = "Issued on ${formatInvoiceDate(invoice.invoiceDate)}",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = mutedText
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 4. Void Invoice Confirmation Dialog
// ─────────────────────────────────────────────────────────────
@Composable
fun VoidInvoiceDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmVoid: (reason: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Void Invoice",
                fontWeight = FontWeight.Medium,
                fontSize = tokens.bodyMedium,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Are you sure you want to void this invoice? This action cannot be reversed. Please enter a reason:",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = tokens.extraPadding)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (it.isNotBlank()) isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = title_color,
                        focusedTextColor = title_color,
                        focusedBorderColor = title_color,
                        unfocusedTextColor = title_color,
                        unfocusedLabelColor = title_color,
                        unfocusedBorderColor = title_color
                    ),
                    label = { Text("Reason for Voiding *", fontSize = tokens.bodySmall) },
                    placeholder = { Text("e.g. Order cancelled by customer", fontSize = tokens.bodySmall) },
                    minLines = 3,
                    maxLines = 4,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Please enter a valid reason", color = redText, fontSize = tokens.caption)
                        }
                    },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().isBlank()) {
                        isError = true
                    } else {
                        onConfirmVoid(reason.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = redText),
                enabled = !isLoading,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(tokens.iconSize * 0.8f),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                }
                Text("Confirm Void", color = whiteBg, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            ) {
                Text("Cancel", fontSize = tokens.bodySmall,color =  title_color, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        containerColor = whiteBg
    )
}

// ─────────────────────────────────────────────────────────────
// 5. Action Bar (Print, Void, Download PDF, Send, Record Payment)
// ─────────────────────────────────────────────────────────────
@Composable
private fun InvoiceActionBar(
    showRecordPayment: Boolean,
    showVoid: Boolean,
    showSend: Boolean = false,
    onDownloadPdf: () -> Unit,
    onPrint: () -> Unit,
    onRecordPayment: () -> Unit,
    onVoid: () -> Unit,
    onSend: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.8f),
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InvoiceOutlinedAction(
            label = "Print",
            icon = Icons.Outlined.Print,
            tint = TextPrimary,
            onClick = onPrint
        )

        if (showVoid) {
            InvoiceOutlinedAction(
                label = "Void",
                icon = Icons.Outlined.Cancel,
                tint = redText,
                onClick = onVoid
            )
        }

        InvoiceIconOnlyAction(
            icon = Icons.Outlined.FileDownload,
            contentDescription = "Download PDF",
            onClick = onDownloadPdf
        )

        // Displayed next to Record Payment when invoice status is Draft
        if (showSend) {
            InvoicePrimaryAction(
                label = "Send",
                onClick = onSend
            )
        }

        if (showRecordPayment) {
            InvoicePrimaryAction(
                label = "Record Payment",
                onClick = onRecordPayment
            )
        }
    }
}

@Composable
private fun InvoiceOutlinedAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        border = BorderStroke(1.dp, BorderGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = whiteBg,
            contentColor = tint
        ),
        contentPadding = PaddingValues(horizontal = tokens.screenPadding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(tokens.iconSize)
        )
        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

@Composable
private fun InvoiceIconOnlyAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        border = BorderStroke(1.dp, BorderGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = whiteBg,
            contentColor = TextPrimary
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(tokens.iconSize * 1.1f)
        )
    }
}

@Composable
private fun InvoicePrimaryAction(
    label: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Button(
        onClick = onClick,
        modifier = Modifier.height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        contentPadding = PaddingValues(horizontal = tokens.screenPadding)
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = whiteBg
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 6. InvoiceDetailScreen — WebView Preview & Operations
// ─────────────────────────────────────────────────────────────
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    token: String,
    onClose: () -> Unit,
    onRecordPayment: (InvoiceViewOneData) -> Unit = {},
    onVoidInvoice: (InvoiceViewOneData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: FinanceViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val context = LocalContext.current
    val pdfGenerator = remember { InvoicePdfGenerator(context) }

    var previewWebView by remember { mutableStateOf<WebView?>(null) }
    var showVoidDialog by remember { mutableStateOf(false) }

    val voidState by viewModel.voidInvoiceState.collectAsStateWithLifecycle()
    val createInvoiceState by viewModel.createInvoiceState.collectAsStateWithLifecycle()

    // Global Dynamic Island listener for void invoice response
    LaunchedEffect(voidState) {
        when (val state = voidState) {
            is VoidInvoiceState.Success -> {
                DynamicIslandManager.showSuccess(state.message)
                showVoidDialog = false
                viewModel.resetVoidInvoiceState()
            }
            is VoidInvoiceState.Error -> {
                DynamicIslandManager.showError(state.message)
                viewModel.resetVoidInvoiceState()
            }
            else -> Unit
        }
    }

    // Global Dynamic Island listener for standalone send invoice response
    LaunchedEffect(createInvoiceState) {
        when (val state = createInvoiceState) {
            is CreateInvoiceState.Success -> {
                DynamicIslandManager.showSuccess(state.message)
                viewModel.resetCreateInvoiceState()
            }
            is CreateInvoiceState.Error -> {
                DynamicIslandManager.showError(state.message)
                viewModel.resetCreateInvoiceState()
            }
            else -> Unit
        }
    }

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

    fun InvoiceViewOneData.toPdfData(): InvoicePdfGenerator.InvoiceData {
        val snapshot = customerSnapshot

        val safeItems = (items ?: emptyList()).map { item ->
            val rawDiscount = item.discount?.trim().orEmpty()
            val discountNumber = rawDiscount.toDoubleOrNull()
            val discountText = when {
                rawDiscount.isBlank() -> "-"
                discountNumber != null && discountNumber <= 0.0 -> "-"
                discountNumber != null && discountNumber % 1.0 == 0.0 -> "${discountNumber.toInt()}%"
                discountNumber != null -> "$discountNumber%"
                else -> rawDiscount
            }

            InvoicePdfGenerator.InvoiceItemData(
                description = item.itemName?.takeIf { it.isNotBlank() }
                    ?: item.description
                    ?: "Item",
                hsnSku = item.hsnCode?.takeIf { it.isNotBlank() }
                    ?: item.sacCode?.takeIf { it.isNotBlank() }
                    ?: item.hsnSku
                    ?: "-",
                quantity = item.quantity,
                unit = item.unit.orEmpty(),
                unitPrice = item.unitPrice,
                discount = discountText,
                tax = item.tax,
                total = item.total
            )
        }

        val billingText = snapshot?.billingAddress?.fullAddress?.takeIf { it.isNotBlank() }
            ?: customer?.billingAddress
            ?: shippingAddressSnapshot?.fullAddress
            ?: ""

        val shippingText = snapshot?.shippingAddress?.fullAddress?.takeIf { it.isNotBlank() }
            ?: customer?.shippingAddress
            ?: shippingAddressSnapshot?.fullAddress
            ?: billingText

        val companyPhoneText = organization?.phone.orEmpty().let {
            if (it.isNotBlank() && !it.startsWith("+")) "+$it" else it
        }

        return InvoicePdfGenerator.InvoiceData(
            invoiceNumber = invoiceNumber,
            invoiceDate = formatInvoiceDate(invoiceDate),
            dueDate = formatInvoiceDate(dueDate ?: invoiceDate),
            status = status,
            customerName = snapshot?.name?.takeIf { it.isNotBlank() }
                ?: customer?.name
                ?: customerId?.name
                ?: customerId?.id
                ?: "Walk-in Customer",
            billToAddress = billingText,
            billToPhone = snapshot?.phone?.takeIf { it.isNotBlank() }
                ?: customer?.phone
                ?: customerId?.mobile
                ?: "",
            billToEmail = snapshot?.email?.takeIf { it.isNotBlank() }
                ?: customer?.email
                ?: customerId?.email
                ?: "",
            shipToAddress = shippingText,
            orderReference = salesOrderId?.orderNumber ?: salesOrderId?.id ?: "",
            items = safeItems,
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            shippingAmount = shippingAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentMethod = paymentMethod ?: "Bank Transfer / UPI / Cheque",
            bankName = bankDetails?.bankName ?: "",
            accountNo = bankDetails?.accountNo ?: "",
            ifscSwift = bankDetails?.ifscSwift ?: "",
            termsAndConditions = termsAndConditions?.takeIf { it.isNotBlank() }
                ?: "Payment due within 30 days of invoice date.",
            companyName = organization?.name ?: "",
            companyAddress = organization?.address ?: "",
            companyEmail = organization?.email ?: "",
            companyPhone = companyPhoneText,
            companyGst = organization?.gstNumber ?: "",
            logoUrl = logoBase64.ifEmpty { null } ?: organization?.logoUrl?.takeIf { it.isNotEmpty() },
            signatoryLabel = authorizedSignatory?.designation?.takeIf { it.isNotBlank() } ?: "Authorized Signatory"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar(title = "All Invoices", onClose = onClose)
            }
            HorizontalDivider(color = BorderGray)

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = redText, modifier = Modifier.size(tokens.iconSize * 2.2f))
                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                            Text(error ?: "Failed to load invoice", color = redText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Normal)
                            Spacer(Modifier.height(tokens.extraPadding))
                            Button(onClick = { viewModel.fetchInvoiceDetail(invoiceId) }) {
                                Text("Retry", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                            TextButton(onClick = onClose) {
                                Text("Go Back", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                invoiceDetail != null -> {
                    val invoice = invoiceDetail!!
                    val pdfData = remember(invoice, logoBase64) { invoice.toPdfData() }
                    val isVoid = invoice.status.equals("void", ignoreCase = true)
                    val isDraft = invoice.status.equals("draft", ignoreCase = true)

                    Column(modifier = Modifier.fillMaxSize()) {
                        InvoiceHeaderCard(invoice = invoice)

                        InvoiceActionBar(
                            showRecordPayment = invoice.balanceAmount > 0.0 && !isVoid && !isDraft,
                            showVoid = !isVoid,
                            showSend = isDraft,
                            onDownloadPdf = {
                                if (!isDownloading) {
                                    isDownloading = true
                                    pdfGenerator.downloadInvoicePdf(pdfData) { saved ->
                                        isDownloading = false
                                        if (saved != null && saved.exists()) {
                                            DynamicIslandManager.showSuccess("Invoice downloaded to Downloads folder")
                                        } else {
                                            DynamicIslandManager.showError("Failed to download invoice")
                                        }
                                    }
                                }
                            },
                            onPrint = {
                                val webView = previewWebView
                                if (webView != null) {
                                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                                    val jobName = "Invoice ${invoice.invoiceNumber}"
                                    printManager.print(
                                        jobName,
                                        webView.createPrintDocumentAdapter(jobName),
                                        PrintAttributes.Builder().build()
                                    )
                                } else {
                                    DynamicIslandManager.showError("Invoice is not ready to print")
                                }
                            },
                            onRecordPayment = {
                                onRecordPayment(invoice)
                            },
                            onVoid = { showVoidDialog = true },
                            onSend = {
                                // Executes the standalone Send API
                                viewModel.sendInvoice(invoice.id)
                            }
                        )
                        HorizontalDivider(color = title_border)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = tokens.screenPadding * 0.5f)
                                .background(whiteBg)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        previewWebView = this
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
                    }
                }
            }
        }

        if (showVoidDialog) {
            VoidInvoiceDialog(
                isLoading = voidState is VoidInvoiceState.Loading,
                onDismiss = { showVoidDialog = false },
                onConfirmVoid = { reasonText ->
                    viewModel.voidInvoice(invoiceId = invoiceId, reason = reasonText)
                }
            )
        }
    }
}