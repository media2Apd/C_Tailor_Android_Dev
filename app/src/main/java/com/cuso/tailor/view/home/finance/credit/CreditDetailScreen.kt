package com.cuso.tailor.view.home.finance.credit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.ApplyCreditToInvoiceRequest
import com.cuso.tailor.model.finance.CreditNoteItemDto
import com.cuso.tailor.model.finance.CustomerOutstandingInvoiceItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.pdfgenerator.CreditNotePdfGenerator
import com.cuso.tailor.viewmodel.ApplyCreditState
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.VoidCreditNoteState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CreditNoteDetailScreen(
    creditNoteId: String,
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onEdit: ((CreditNoteItemDto) -> Unit)? = null,
    onApplyToInvoice: ((CreditNoteItemDto) -> Unit)? = null,
    onVoidCredit: ((CreditNoteItemDto) -> Unit)? = null,
    onSendAndPost: ((CreditNoteItemDto) -> Unit)? = null
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val creditNote by viewModel.selectedCreditNote.collectAsState()
    val allocations by viewModel.creditNoteAllocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showApplyDialog by remember { mutableStateOf(false) }
    var showVoidDialog by remember { mutableStateOf(false) }

    // Helper to refresh credit note details, allocations, and list
    fun refreshAllCreditNoteData() {
        viewModel.fetchCreditNoteById(creditNoteId)
        viewModel.fetchCreditNoteAllocations(creditNoteId)
        viewModel.fetchCreditNotes()
    }

    LaunchedEffect(creditNoteId) {
        refreshAllCreditNoteData()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Credit Details", onClose = onBack)
            }
        }
    ) { padding ->
        if (isLoading || creditNote == null) {
            ListSkeleton()
        } else {
            val item: CreditNoteItemDto = creditNote!!

            val formattedDate = formatIsoDate(item.creditNoteDate)
            val customerName = item.customerSnapshot?.name
                ?: item.customerId?.fullName
                ?: ""
            val branchName = item.branchId?.name ?: ""

            // Construct billing address
            val addressObj = item.customerSnapshot?.billingAddress ?: item.customerId?.billingAddress
            val fullAddress = addressObj?.let {
                listOfNotNull(it.flatNo, it.street, it.city, it.subdivisionCode, it.pincode)
                    .filter { s -> s.isNotBlank() }
                    .joinToString(", ")
            } ?: "Address not specified"

            val pdfGenerator = remember { CreditNotePdfGenerator(context) }
            var isDownloadingPdf by remember { mutableStateOf(false) }

            // Normalize status checks
            val statusUpper = item.status.uppercase(Locale.US).trim()
            val isDraft = statusUpper == "DRAFT"
            val isOpen = statusUpper == "OPEN"

            // Dialog for applying credits to an invoice
            if (showApplyDialog) {
                ApplyCreditToInvoiceDialog(
                    creditNote = item,
                    viewModel = viewModel,
                    onDismiss = {
                        showApplyDialog = false
                        refreshAllCreditNoteData()
                    }
                )
            }

            // Dialog for voiding the credit note
            if (showVoidDialog) {
                VoidCreditNoteDialog(
                    creditNoteNumber = item.creditNoteNumber,
                    creditNoteId = creditNoteId.ifBlank { item.id },
                    viewModel = viewModel,
                    onDismiss = { showVoidDialog = false },
                    onVoidSuccess = {
                        showVoidDialog = false
                        refreshAllCreditNoteData()
                        onVoidCredit?.invoke(item)
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // =============================================================
                // Card 1: Document Header, Status and Dynamic Action Buttons
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.75f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.creditNoteNumber,
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TitleColor
                                    )
                                    Spacer(Modifier.width(8.dp))

                                    val isClosed = item.status.contains("Applied", ignoreCase = true) || item.status.contains("Closed", ignoreCase = true)
                                    val isVoid = item.status.contains("Void", ignoreCase = true)

                                    StatusBadge(
                                        text = item.status.replace("_", " "),
                                        bgColor = when {
                                            isVoid -> redBg
                                            isClosed -> greenBg
                                            else -> yellowBg
                                        },
                                        textColor = when {
                                            isVoid -> redText
                                            isClosed -> greentext
                                            else -> yellowText
                                        }
                                    )
                                }

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    text = "Raised on $formattedDate",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // =============================================================
                        // Dynamic Status Action Buttons (Horizontally Scrollable)
                        // =============================================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DRAFT State: Edit button appears first
                            if (isDraft) {
                                OutlinedButton(
                                    onClick = { onEdit?.invoke(item) },
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderGray),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit",
                                        tint = TitleColor,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Edit",
                                        color = TitleColor,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // PDF Button (Universal)
                            OutlinedButton(
                                onClick = {
                                    isDownloadingPdf = true
                                    pdfGenerator.downloadCreditNotePdf(item, allocations) { saved ->
                                        isDownloadingPdf = false
                                        if (saved != null) {
                                            Toast.makeText(context, "Saved to Downloads: ${saved.displayName}", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isDownloadingPdf,
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "PDF",
                                    tint = TitleColor,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isDownloadingPdf) "Generating..." else "PDF",
                                    color = TitleColor,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Print Button (Universal)
                            OutlinedButton(
                                onClick = {
                                    pdfGenerator.printCreditNote(item, allocations)
                                },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Print,
                                    contentDescription = "Print",
                                    tint = TitleColor,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Print",
                                    color = TitleColor,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // OPEN State: Apply to Invoice & Void Credit buttons
                            if (isOpen) {
                                Button(
                                    onClick = {
                                        val custId = item.customerId?.id ?: item.customerSnapshot?.customerCode ?: ""
                                        if (custId.isNotBlank()) {
                                            viewModel.fetchCustomerOutstandingInvoices(custId)
                                        }
                                        showApplyDialog = true
                                        onApplyToInvoice?.invoke(item)
                                    },
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Layers,
                                        contentDescription = "Apply to Invoice",
                                        tint = Color.White,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Apply to Invoice",
                                        color = Color.White,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showVoidDialog = true },
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderGray),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = "Void Credit",
                                        tint = redText,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Void Credit",
                                        color = redText,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // DRAFT State: Send & Post to GL button
                            if (isDraft) {
                                Button(
                                    onClick = {
                                        val targetId = creditNoteId.trim().ifBlank { item.id }
                                        viewModel.sendCreditNote(targetId) {
                                            Toast.makeText(context, "Credit note sent and posted to GL successfully", Toast.LENGTH_SHORT).show()
                                            refreshAllCreditNoteData()
                                            onSendAndPost?.invoke(item)
                                        }
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Send,
                                            contentDescription = "Send & Post to GL",
                                            tint = Color.White,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Send & Post to GL",
                                            color = Color.White,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // Card 2: Customer Identity, Billing & Applied Invoices Table
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.75f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            val logoUrl = item.companySnapshot?.logoUrl

                            // Company Logo Container
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(whiteBg)
                                    .border(0.5.dp, BorderGray, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!logoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = logoUrl,
                                        contentDescription = "Company Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Primary, CircleShape)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "CUSTOMER CREDITS",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = headerGrey
                                )
                                Text(
                                    text = "CREDITS NOTE. ${item.creditNoteNumber}",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "credits remaining ₹${String.format(Locale.US, "%.2f", item.remainingBalance)}",
                                    fontSize = tokens.caption,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Customer Details
                        Text(
                            text = customerName,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = fullAddress,
                            fontSize = tokens.caption,
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(3.dp))

                        val gstNumber = item.customerSnapshot?.taxRegistrationNumber
                        if (!gstNumber.isNullOrBlank()) {
                            Text(
                                text = "GSTIN: $gstNumber",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        val phone = item.customerSnapshot?.phone ?: item.customerId?.mobileNumber
                        val email = item.customerSnapshot?.email ?: item.customerId?.email
                        val contactLine = listOfNotNull(phone?.let { "+$it" }, email).joinToString(" • ")
                        if (contactLine.isNotBlank()) {
                            Text(
                                text = contactLine,
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = dividerColor
                        )

                        // Customer Address & Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CUSTOMER ADDRESS",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = branchName,
                                    fontSize = tokens.bodySmall,
                                    color = TitleColor,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "DATE",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = tokens.bodySmall,
                                    color = TitleColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Applied Bills Section Title
                        Text(
                            text = "APPLIED BILLS",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )

                        Spacer(Modifier.height(8.dp))

                        // Table Header Row with Balanced Weights
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(badgeGrey)
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BILL NUMBER",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(2.0f)
                            )
                            Text(
                                text = "BILL DATE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "RATE",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = "AMOUNT",
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                        }

                        if (allocations.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No applied bills found",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        } else {
                            // Rows mapped from the Allocations API response
                            allocations.forEach { alloc ->
                                val invNumber = alloc.invoiceId?.invoiceNumber ?: "—"
                                val billDate = formatIsoDate(alloc.appliedDate)
                                val rateAmount = alloc.invoiceId?.grandTotal ?: alloc.amountApplied

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = invNumber,
                                        fontSize = tokens.bodySmall,
                                        color = TitleColor,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(2.0f)
                                    )
                                    Text(
                                        text = billDate,
                                        fontSize = tokens.bodySmall,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", rateAmount)}",
                                        fontSize = tokens.bodySmall,
                                        color = TitleColor,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", alloc.amountApplied)}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TitleColor,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }
                                HorizontalDivider(color = dividerColor)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Total Amount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", item.grandTotal)}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Credits Remaining Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Credits Remaining",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", item.remainingBalance)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// Utility function to format ISO date strings
private fun formatIsoDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val date = parser.parse(isoDate)
        date?.let { formatter.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

/**
 * Void Credit Note Dialog with Reason Input
 */
@Composable
fun VoidCreditNoteDialog(
    creditNoteNumber: String,
    creditNoteId: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onVoidSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    var voidReason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val voidCreditNoteState by viewModel.voidCreditNoteState.collectAsState()
    val isSubmitting = voidCreditNoteState is VoidCreditNoteState.Loading

    LaunchedEffect(voidCreditNoteState) {
        if (voidCreditNoteState is VoidCreditNoteState.Error) {
            Toast.makeText(context, (voidCreditNoteState as VoidCreditNoteState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetVoidCreditNoteState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = redText,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Void Credit Note",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Warning Notice Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(redBg.copy(alpha = 0.5f))
                        .border(1.dp, redText.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = redText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Voiding credit note $creditNoteNumber will reverse all associated general ledger entries. This action cannot be undone.",
                            fontSize = tokens.caption,
                            color = redText,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Reason for Voiding Field
                FormLabel(
                    text = "Reason for Voiding",
                    isRequired = true
                )

                FormTextField(
                    value = voidReason,
                    onValueChange = {
                        voidReason = it
                        if (it.isNotBlank()) isError = false
                    },
                    isError = isError,
                    errorMessage = if (isError) "Please provide a reason to void this credit note." else null,
                    placeholder = "e.g., Issued in error, customer request",
                    containerColor = badgeGrey,
                    textColor = TitleColor
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TitleColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (voidReason.isBlank()) {
                                isError = true
                                return@Button
                            }

                            viewModel.voidCreditNote(
                                creditNoteId = creditNoteId,
                                reason = voidReason.trim()
                            ) { successMessage ->
                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                viewModel.resetVoidCreditNoteState()
                                onVoidSuccess()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = redText)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Confirm Void",
                                color = Color.White,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Apply Credit To Invoice Dialog
 */
@Composable
fun ApplyCreditToInvoiceDialog(
    creditNote: CreditNoteItemDto,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val outstandingInvoices by viewModel.outstandingInvoices.collectAsState()
    val isLoadingInvoices by viewModel.isLoadingOutstandingInvoices.collectAsState()
    val applyCreditState by viewModel.applyCreditState.collectAsState()

    var selectedInvoice by remember { mutableStateOf<CustomerOutstandingInvoiceItem?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var enteredAmount by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", creditNote.remainingBalance))
    }

    // Map outstanding invoice objects to dropdown display labels
    val invoiceDisplayOptions = remember(outstandingInvoices) {
        outstandingInvoices.map { inv ->
            "${inv.invoiceNumber} (Due: ₹${String.format(Locale.US, "%.2f", inv.balanceDue)})"
        }
    }

    fun onInvoiceSelected(invoice: CustomerOutstandingInvoiceItem) {
        selectedInvoice = invoice
        val maxApplicable = minOf(creditNote.remainingBalance, invoice.balanceDue)
        enteredAmount = String.format(Locale.US, "%.2f", maxApplicable)
    }

    LaunchedEffect(applyCreditState) {
        if (applyCreditState is ApplyCreditState.Error) {
            Toast.makeText(context, (applyCreditState as ApplyCreditState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetApplyCreditState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Layers,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Apply Credits to Invoice",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Available Credit Balance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(greenBg.copy(alpha = 0.5f))
                        .border(1.dp, greentext.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE CREDIT BALANCE",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format(Locale.US, "%,.2f", creditNote.remainingBalance)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Select Outstanding Invoice Dropdown using FormDropdown
                FormDropdown(
                    label = "Select Outstanding Invoice",
                    isRequired = true,
                    value = selectedInvoice?.let {
                        "${it.invoiceNumber} (Due: ₹${String.format(Locale.US, "%.2f", it.balanceDue)})"
                    } ?: if (isLoadingInvoices) "Loading invoices..." else "Choose Invoice",
                    expanded = isDropdownExpanded,
                    onExpandChange = { isDropdownExpanded = it },
                    options = invoiceDisplayOptions,
                    onOptionSelected = { selectedLabel ->
                        val matched = outstandingInvoices.find { inv ->
                            "${inv.invoiceNumber} (Due: ₹${String.format(Locale.US, "%.2f", inv.balanceDue)})" == selectedLabel
                        }
                        if (matched != null) {
                            onInvoiceSelected(matched)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Amount to Apply Field using FormLabel and FormTextField
                FormLabel(
                    text = "Amount to Apply (INR)",
                    isRequired = true
                )

                FormTextField(
                    value = enteredAmount,
                    onValueChange = { enteredAmount = it },
                    keyboardType = KeyboardType.Decimal,
                    placeholder = "0.00",
                    containerColor = badgeGrey,
                    textColor = TitleColor
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(Modifier.height(16.dp))

                // Bottom Actions: Cancel & Confirm Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TitleColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    val isSubmittingAction = applyCreditState is ApplyCreditState.Loading
                    Button(
                        onClick = {
                            val inv = selectedInvoice
                            val amount = enteredAmount.toDoubleOrNull()

                            if (inv == null) {
                                Toast.makeText(context, "Please select an invoice", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (amount == null || amount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (amount > creditNote.remainingBalance) {
                                Toast.makeText(context, "Amount cannot exceed available credit balance", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (amount > inv.balanceDue) {
                                Toast.makeText(context, "Amount cannot exceed invoice balance due", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val currentIsoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())

                            val request = ApplyCreditToInvoiceRequest(
                                creditNoteId = creditNote.id,
                                invoiceId = inv.id,
                                amountApplied = amount,
                                appliedDate = currentIsoDate
                            )

                            viewModel.applyCreditNoteToInvoice(request) { successMessage ->
                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        enabled = !isSubmittingAction,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isSubmittingAction) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Confirm & Offset",
                                color = Color.White,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}