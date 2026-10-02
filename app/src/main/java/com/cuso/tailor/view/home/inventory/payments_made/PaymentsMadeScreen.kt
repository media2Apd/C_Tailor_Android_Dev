@file:Suppress("unused", "SpellCheckingInspection", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.payments_made

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.PaymentSupplierAddress
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.view.home.pdfgenerator.PaymentReceiptPdfGenerator
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.core.graphics.toColorInt
import com.cuso.tailor.view.composable.ListSkeleton

// =============================================================================
// ALL INVENTORY PAYMENT SCREEN WITH VOID CARD DISABLED
// =============================================================================

@Composable
fun AllInventoryPaymentScreen(
    onClose: () -> Unit = {},
    onPaymentSelect: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val paymentsList by viewModel.paymentsMadeList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingPaymentsMade.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMorePaymentsMade.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMorePaymentsMade.collectAsStateWithLifecycle()
    val error by viewModel.paymentsMadeError.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllPaymentsMade(page = 1)
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3 && canLoadMore && !isLoading && !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMorePaymentsMade()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                TitleBar(
                    title = "All Payment",
                    onClose = onClose
                )
            }
        },
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.onPaymentsMadeSearchQueryChanged(it)
                },
                placeholder = "Search Customers...",
                onFilterClick = onFilterClick
            )

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        ListSkeleton()
                    }

                    error != null && paymentsList.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load payments",
                            message = error ?: "-",
                            onRetry = { viewModel.fetchAllPaymentsMade(page = 1) }
                        )
                    }

                    paymentsList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payments found",
                                fontSize = tokens.bodyMedium,
                                color = close_color
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = paymentsList,
                                key = { it.id }
                            ) { payment ->
                                // Check if this record is voided
                                val isVoid = payment.status.trim().equals("Void", ignoreCase = true)
                                val (statusBg, statusTextColor) = resolvePaymentStatusColors(payment.status)
                                val refCode = payment.referenceNumber?.takeIf { it.isNotBlank() } ?: payment.paymentNumber

                                DataCard(
                                    item = payment,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isVoid) 0.55f else 1.0f) // Visually disable the card when voided
                                        .background(if (isVoid) Color(0xFFF9FAFB) else whiteBg),
                                    showDateIcon = false,
                                    dateText = "${formatPaymentDate(payment.paymentDate)}   ",
                                    code = "#$refCode#",
                                    topBadgeText = payment.status.ifBlank { "-" },
                                    topBadgeBgColor = if (isVoid) Color(0xFFFEE2E2) else statusBg,
                                    topBadgeTextColor = if (isVoid) Color(0xFFDC2626) else statusTextColor,
                                    topBadgeDotColor = if (isVoid) Color(0xFFDC2626) else statusTextColor,
                                    topBadgeShowDot = true,
                                    showActionsInHeader = !isVoid, // Hide header menu for voided items
                                    actions = if (isVoid) emptyList() else listOf(
                                        MenuAction(
                                            label = "View Details",
                                            icon = Icons.Outlined.RemoveRedEye,
                                            onClick = { onPaymentSelect(payment.id) }
                                        )
                                    ),
                                    showHeaderDivider = true,
                                    showDivider = false,
                                    // Nullify click callback when voided so navigation is completely blocked
                                    onClick = { if (isVoid) null else { onPaymentSelect(payment.id) } },
                                    content = {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Middle Row: SUPPLIER and AMOUNT PAID
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "SUPPLIER",
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = mutedText,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.supplierId?.name?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodyMedium,
                                                        color = if (isVoid) Color(0xFF9CA3AF) else TextPrimary
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "AMOUNT PAID",
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Medium,
                                                        color = mutedText,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "₹${formatIndianNumber(payment.amount)}",
                                                        fontSize = tokens.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isVoid) Color(0xFF9CA3AF) else Primary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Bottom Row: PO Number, Mode, Unused
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "PO Number",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.billId?.billNumber?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isVoid) Color(0xFF9CA3AF) else TextPrimary
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "Mode",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = payment.paymentMode?.ifBlank { null } ?: "-",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isVoid) Color(0xFF9CA3AF) else TextPrimary
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(
                                                        text = "Unused",
                                                        fontSize = tokens.caption,
                                                        color = close_color
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    val unusedVal = payment.billId?.balanceDue ?: 0.0
                                                    Text(
                                                        text = "${unusedVal.toInt()}",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isVoid) Color(0xFF9CA3AF) else TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            if (isLoadingMore) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                            item {
                                Spacer(Modifier.height(50.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
// =============================================================================
// PAYMENT OVERVIEW DETAIL SCREEN
// =============================================================================

@Composable
fun PaymentOverviewDetailScreen(
    paymentId: String = "",
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPreviewPdfClick: () -> Unit = {},
    onSendEmailClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    val paymentDetail by viewModel.selectedPurchasePaymentDetail.collectAsState()
    val isLoading by viewModel.isLoadingPaymentDetail.collectAsState()
    val errorMessage by viewModel.paymentDetailError.collectAsState()
    val documentTemplates by viewModel.documentTemplates.collectAsState()

    val isVoiding by viewModel.isVoidingPayment.collectAsState()
    val voidError by viewModel.voidPaymentError.collectAsState()

    var showVoidDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    LaunchedEffect(paymentId) {
        if (paymentId.isNotBlank()) {
            viewModel.fetchPurchasePaymentDetail(paymentId)
        }
        viewModel.fetchDocumentTemplates(docType = "paymentReceipt")
        viewModel.fetchPurchasePayments(page = 1, limit = 10)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPurchasePaymentDetail()
        }
    }

    val activeTemplate = remember(documentTemplates, paymentDetail?.templateTheme) {
        documentTemplates.find { it.templateKey.equals(paymentDetail?.templateTheme, ignoreCase = true) }
            ?: documentTemplates.find { it.isDefault }
    }

    val dynamicPrimaryColor = remember(activeTemplate) {
        parseHexToColor(activeTemplate?.designConfig?.primaryColor, Primary)
    }
    val showSignature = activeTemplate?.designConfig?.showSignature ?: true

    // Check if the current payment is already voided
    val isVoided = paymentDetail?.status.equals("Void", ignoreCase = true)

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Payments Made Overview",
                    onClose = onClose
                )
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = dynamicPrimaryColor)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Loading payment details...", fontSize = tokens.bodySmall, color = TextSecondary)
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tokens.screenPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = errorMessage ?: "-", fontSize = tokens.bodyMedium, color = Color(0xFFDC2626), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (paymentId.isNotBlank()) viewModel.fetchPurchasePaymentDetail(paymentId)
                                viewModel.fetchDocumentTemplates(docType = "paymentReceipt")
                                viewModel.fetchPurchasePayments(page = 1, limit = 10)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimaryColor)
                        ) {
                            Text(text = "Retry", color = whiteBg)
                        }
                    }
                }

                paymentDetail != null -> {
                    val payment = paymentDetail!!

                    val displayPaymentNumber = payment.paymentNumber.ifBlank { "-" }
                    val displayStatus = payment.status.ifBlank { "PAID" }
                    val (statusBg, statusTextColor) = resolvePaymentStatusColors(displayStatus)
                    val displayNavigationPath = if (!payment.billId?.billNumber.isNullOrBlank()) {
                        "Bills / ${payment.billId.billNumber}"
                    } else {
                        "-"
                    }

                    val supplier = payment.supplierId
                    val displayVendorName = supplier?.name?.ifBlank { "-" } ?: "-"
                    val displayVendorEmail = supplier?.contact?.email?.ifBlank { "" } ?: ""
                    val displayVendorPhone = supplier?.contact?.phone?.ifBlank { "" } ?: ""
                    val displayVendorAddress = supplier?.billingAddress?.let { formatAddressToString(it) }?.ifBlank { "-" } ?: "-"

                    val displayReceiptNumber = payment.paymentNumber.ifBlank { "-" }
                    val displayReceiptDate = formatIsoToDisplayDate(payment.paymentDate)

                    val currency = payment.billId?.currency?.ifBlank { "INR" } ?: "INR"
                    val currencySymbol = if (currency.equals("INR", ignoreCase = true)) "₹" else "$currency "
                    val displayTotalAmountPaid = "$currencySymbol${formatDouble(payment.amount)}"
                    val displayRecipientName = supplier?.name?.ifBlank { "-" } ?: "-"
                    val displayReferenceNumber = payment.referenceNumber?.ifBlank { "-" } ?: "-"
                    val displayPaymentMethod = payment.paymentMode.ifBlank { "Bank · Bank Account" }
                    val displayAmountInWords = convertAmountToWords(payment.amount, currency)

                    val displayPreparedBy = payment.createdBy?.let { user ->
                        val fullName = listOfNotNull(user.firstName, user.lastName).filter { it.isNotBlank() }.joinToString(" ")
                        fullName.ifBlank { user.memberId ?: "-" }
                    } ?: "-"

                    val handleDownloadReceiptPdf = {
                        isGeneratingPdf = true
                        val generator = PaymentReceiptPdfGenerator(context)

                        val billItems = if (payment.billId != null) {
                            listOf(
                                PaymentReceiptPdfGenerator.AppliedBillItem(
                                    billNumber = payment.billId.billNumber.ifBlank { "-" },
                                    billDate = formatIsoToDisplayDate(payment.billId.billDate),
                                    billAmount = payment.billId.grandTotal,
                                    paidAmount = payment.amount,
                                    balanceDue = payment.billId.balanceDue
                                )
                            )
                        } else emptyList()

                        val receiptData = PaymentReceiptPdfGenerator.PaymentReceiptData(
                            receiptNumber = displayReceiptNumber,
                            receiptDate = displayReceiptDate,
                            status = displayStatus,
                            vendorName = displayVendorName,
                            vendorEmail = displayVendorEmail,
                            vendorPhone = displayVendorPhone,
                            vendorAddress = displayVendorAddress,
                            totalAmountPaid = payment.amount,
                            paidTo = displayRecipientName,
                            referenceNumber = displayReferenceNumber,
                            paymentMode = displayPaymentMethod,
                            amountInWords = displayAmountInWords,
                            currencySymbol = currencySymbol,
                            bills = billItems,
                            preparedBy = displayPreparedBy,
                            showSignature = showSignature,
                            primaryColorHex = activeTemplate?.designConfig?.primaryColor ?: "#5B45E0"
                        )

                        generator.downloadReceiptPdf(receiptData) { savedPdf ->
                            isGeneratingPdf = false
                            if (savedPdf != null && savedPdf.exists()) {
                                Toast.makeText(context, "Receipt downloaded to Downloads: ${savedPdf.displayName}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to generate receipt PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Header Action Card
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                                .padding(horizontal = tokens.screenPadding, vertical = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayPaymentNumber,
                                    fontSize = tokens.h2,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = displayStatus,
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Options Overflow Menu (with Void action)
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(whiteBg)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isVoided) "Payment Voided" else "Void Payment",
                                                    color = if (isVoided) mutedText else Color(0xFFDC2626),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Block,
                                                    contentDescription = null,
                                                    tint = if (isVoided) mutedText else Color(0xFFDC2626)
                                                )
                                            },
                                            enabled = !isVoided,
                                            onClick = {
                                                showMenu = false
                                                showVoidDialog = true
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = displayNavigationPath,
                                fontSize = tokens.caption,
                                color = close_color,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Direct Void Button (Opens Confirmation Dialog)
                                OutlinedButton(
                                    onClick = { showVoidDialog = true },
                                    enabled = !isVoided,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.fieldHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    border = BorderStroke(1.dp, if (isVoided) Color(0xFFE5E7EB) else Color(0xFFDC2626)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFDC2626),
                                        disabledContentColor = Color(0xFF9CA3AF)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = if (isVoided) "Voided" else "Void",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isVoided) mutedText else Color(0xFFDC2626)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        handleDownloadReceiptPdf()
                                        onPreviewPdfClick()
                                    },
                                    enabled = !isGeneratingPdf,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(tokens.fieldHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    if (isGeneratingPdf) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = dynamicPrimaryColor
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Downloading,
                                            contentDescription = null,
                                            modifier = Modifier.size(tokens.iconSize * 0.9f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download PDF", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Button(
                                    onClick = onSendEmailClick,
                                    enabled = !isVoided,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(tokens.fieldHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Primary,
                                        disabledContainerColor = disabled
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "Send Email",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = whiteBg
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Receipt Content Preview Card
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                                    .padding(tokens.screenPadding * 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(background_light_purple),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_shirts),
                                                contentDescription = null,
                                                tint = dynamicPrimaryColor,
                                                modifier = Modifier.size(tokens.iconSize * 1.3f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = displayVendorName,
                                                fontSize = tokens.bodySmall,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(statusBg)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = displayStatus,
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusTextColor,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "PAYMENT RECEIPT",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            textAlign = TextAlign.End,
                                            lineHeight = 20.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(text = "Receipt #: ", fontSize = tokens.bodySmall, color = TextSecondary)
                                            Text(text = displayReceiptNumber, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(text = "Date: ", fontSize = tokens.bodySmall, color = TextSecondary)
                                            Text(text = displayReceiptDate, fontSize = tokens.bodySmall, fontWeight = FontWeight.Normal, color = TextPrimary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Primary_background, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                                        .padding(tokens.extraPadding)
                                ) {
                                    if (displayVendorEmail.isNotBlank()) {
                                        Text(text = displayVendorEmail, fontSize = tokens.bodySmall, color = title_color)
                                    }
                                    if (displayVendorPhone.isNotBlank()) {
                                        Text(text = displayVendorPhone, fontSize = tokens.bodySmall, color = TextSecondary)
                                    }
                                    Text(text = displayVendorAddress, fontSize = tokens.bodySmall, color = TextSecondary)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                                        .background(light_blue)
                                        .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "TOTAL AMOUNT PAID",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = dynamicPrimaryColor,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = displayTotalAmountPaid,
                                            fontSize = tokens.h1,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Text(text = "PAID TO", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                        Text(text = displayRecipientName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "REFERENCE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                        Text(text = displayReferenceNumber, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = "MODE OF PAYMENT", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = dynamicPrimaryColor, modifier = Modifier.size(tokens.iconSize))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = displayPaymentMethod, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = "AMOUNT IN WORDS", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = displayAmountInWords, fontSize = tokens.caption, fontStyle = FontStyle.Italic, color = TextSecondary)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = "APPLIED BILLS", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = textSubdued, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, dividerColor, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(headerBg)
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = "BILL DETAILS", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1.5f))
                                        Text(text = "AMOUNT", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = headerGrey, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                        Text(text = "PAID", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = headerGrey, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    }

                                    val bill = payment.billId
                                    if (bill != null) {
                                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text(text = bill.billNumber.ifBlank { "-" }, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                                Text(text = formatIsoToDisplayDate(bill.billDate), fontSize = tokens.caption, color = mutedText)
                                            }
                                            Text(text = "$currencySymbol${formatDouble(bill.grandTotal)}", fontSize = tokens.bodySmall, color = TextSecondary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                            Text(text = "$currencySymbol${formatDouble(payment.amount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                        }
                                    } else {
                                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = "-", fontSize = tokens.bodySmall, color = TextSecondary)
                                        }
                                    }

                                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(headerBg).padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Total Applied Amount", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(2.5f))
                                        Text(text = "$currencySymbol${formatDouble(payment.amount)}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.BottomCenter) {
                                            Text(text = displayPreparedBy, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "PREPARED BY", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = close_color)
                                    }

                                    if (showSignature) {
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.fillMaxWidth().height(24.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "AUTHORIZED SIGNATURE", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = close_color)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "This is a computer-generated receipt and does not require a physical signature.",
                                    fontSize = tokens.caption,
                                    color = close_color,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Created with cuso invoice", fontSize = tokens.label, color = title_color)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "-", fontSize = tokens.bodyLarge, color = TextSecondary)
                    }
                }
            }
        }
    }

    // =========================================================================
    // VOID PAYMENT CONFIRMATION DIALOG WITH REASON TEXTAREA
    // =========================================================================
    if (showVoidDialog) {
        var voidReason by remember { mutableStateOf("") }
        var isReasonBlankError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { if (!isVoiding) showVoidDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Void Payment",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to void this payment? This action cannot be undone. Please specify a reason below.",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = {
                            voidReason = it
                            if (it.isNotBlank()) isReasonBlankError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = {
                            Text(text = "Enter void reason (required)...", fontSize = tokens.bodySmall, color = mutedText)
                        },
                        isError = isReasonBlankError,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = dividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 4
                    )

                    if (isReasonBlankError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Void reason is required",
                            fontSize = tokens.caption,
                            color = Color(0xFFDC2626)
                        )
                    }

                    if (voidError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = voidError ?: "An error occurred",
                            fontSize = tokens.caption,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showVoidDialog = false },
                            enabled = !isVoiding,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text(text = "Cancel", fontSize = tokens.bodySmall)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (voidReason.isBlank()) {
                                    isReasonBlankError = true
                                } else {
                                    viewModel.voidPayment(
                                        paymentId = paymentId,
                                        reason = voidReason.trim(),
                                        onSuccess = {
                                            showVoidDialog = false
                                            Toast.makeText(context, "Payment voided successfully", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            enabled = !isVoiding,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                disabledContainerColor = Color(0xFFFCA5A5)
                            )
                        ) {
                            if (isVoiding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = whiteBg
                                )
                            } else {
                                Text(text = "Void Payment", fontSize = tokens.bodySmall, color = whiteBg)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// HELPER FORMATTING FUNCTIONS
// =============================================================================

private fun formatPaymentDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = inputFormat.parse(isoDate)
        parsed?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun resolvePaymentStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "completed", "paid", "success" -> greenBg to greentext
        "void", "cancelled" -> redBg to redText
        "pending" -> background_light_purple to Primary
        else -> light_grey to TextSecondary
    }
}

private fun formatAddressToString(address: PaymentSupplierAddress): String {
    val components = listOfNotNull(
        address.flatNo,
        address.street,
        address.areaZone,
        address.city,
        address.subdivisionName,
        address.countryName,
        address.pincode
    ).filter { it.isNotBlank() }

    return if (components.isEmpty()) "-" else components.joinToString(", ")
}

private fun formatIsoToDisplayDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoString) ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(isoString)
        if (date != null) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        } else {
            "-"
        }
    } catch (_: Exception) {
        isoString.take(10)
    }
}

private fun formatDouble(value: Double?): String {
    if (value == null) return "-"
    return String.format(Locale.getDefault(), "%,.2f", value)
}

private fun parseHexToColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(hex.toColorInt())
    } catch (_: Exception) {
        fallback
    }
}

private fun convertAmountToWords(amount: Double?, currency: String = "INR"): String {
    if (amount == null || amount <= 0.0) return "-"

    val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convertNumber(n: Long): String {
        return when {
            n < 0 -> "-"
            n < 20 -> units[n.toInt()]
            n < 100 -> tens[(n / 10).toInt()] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
            n < 1000 -> units[(n / 100).toInt()] + " Hundred" + (if (n % 100 != 0L) " and " + convertNumber(n % 100) else "")
            n < 100000 -> convertNumber(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertNumber(n % 1000) else "")
            n < 10000000 -> convertNumber(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertNumber(n % 100000) else "")
            else -> convertNumber(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertNumber(n % 10000000) else "")
        }
    }

    return try {
        val totalPaise = Math.round(amount * 100)
        val majorUnits = totalPaise / 100
        val minorUnits = totalPaise % 100

        val majorUnitName = if (currency.equals("INR", ignoreCase = true)) "Rupees" else currency
        val minorUnitName = if (currency.equals("INR", ignoreCase = true)) "Paise" else "Cents"

        var result = if (majorUnits > 0) convertNumber(majorUnits) + " $majorUnitName" else ""
        if (minorUnits > 0) {
            result += (if (result.isNotBlank()) " and " else "") + convertNumber(minorUnits) + " $minorUnitName"
        }
        if (result.isBlank()) "-" else "$result Only"
    } catch (_: Exception) {
        "-"
    }
}