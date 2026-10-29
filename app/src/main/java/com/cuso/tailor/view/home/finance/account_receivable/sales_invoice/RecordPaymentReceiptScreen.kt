@file:Suppress("unused", "UNUSED_VARIABLE")

package com.cuso.tailor.view.home.finance.account_receivable.sales_invoice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.InvoiceViewOneData
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.AppCheckbox
import com.cuso.tailor.view.composable.BackFabButton
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.composable.TrailingFabButton
import com.cuso.tailor.view.home.formatIndianNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun RecordPaymentReceiptScreen(
    invoice: InvoiceViewOneData,
    onClose: () -> Unit,
    isLoading: Boolean = false,
    branchViewModel: BranchViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
    onSavePayment: (
        customerId: String,
        branchId: String,
        invoiceId: String,
        amount: Double,
        paymentMode: String,
        depositToAccountId: String,
        paymentDate: String,
        referenceNo: String,
        notes: String,
        attachmentUris: List<Uri>,
        sendNotification: Boolean
    ) -> Unit
) {
    val tokens = LocalAppTokens.current

    // ── Dynamic IDs Extracted from Invoice Data ──
    val dynamicInvoiceId = invoice.id
    val invoiceNumber = invoice.invoiceNumber

    val dynamicCustomerId = invoice.customerId?.id
        ?: invoice.customer?.id
        ?: invoice.customerSnapshot?.customerCode
        ?: ""

    val customerName = invoice.customerSnapshot?.name
        ?: invoice.customer?.name
        ?: invoice.customerId?.name
        ?: ""

    val customerCode = invoice.customerSnapshot?.customerCode
        ?: invoice.customerId?.id
        ?: invoice.customer?.id
        ?: ""

    // Trigger branch loading
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
    }

    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList = remember(branchState) {
        (branchState as? BranchUiState.Success)?.branches ?: emptyList()
    }
    val branchOptions = remember(branchList) {
        branchList.mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
    }

    val defaultBranchId = invoice.branchId ?: ""
    var selectedBranchId by remember(defaultBranchId) { mutableStateOf(defaultBranchId) }
    var selectedBranchLabel by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    // Auto-select invoice branch name or first branch from API
    LaunchedEffect(branchList, defaultBranchId) {
        if (branchList.isNotEmpty()) {
            val matchingBranch = branchList.find { it.id == defaultBranchId } ?: branchList.first()
            selectedBranchId = matchingBranch.id
            selectedBranchLabel = matchingBranch.name.orEmpty()
        }
    }

    val outstandingBalance = invoice.balanceAmount

    // ── Payment Mode State ──
    var selectedPaymentMode by remember { mutableStateOf("") }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    // Fetch Chart of Accounts dropdown with context=payment_source
    LaunchedEffect(Unit) {
        financeViewModel.fetchChartOfAccountsDropdown(context = "payment_source")
    }

    val accountDropdownList by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()
    val isAccountsLoading by financeViewModel.isLoadingAccountDropdown.collectAsStateWithLifecycle()

    var selectedDepositAccountLabel by remember { mutableStateOf("") }
    var selectedDepositAccountId by remember { mutableStateOf("") }
    var depositAccountExpanded by remember { mutableStateOf(false) }

    // Auto-select first account when list is loaded
    LaunchedEffect(accountDropdownList) {
        if (accountDropdownList.isNotEmpty() && selectedDepositAccountId.isBlank()) {
            val firstAccount = accountDropdownList.first()
            selectedDepositAccountId = firstAccount.id
            selectedDepositAccountLabel = "${firstAccount.accountName} (${firstAccount.accountCode})"
        }
    }

    // ── Amount & Errors ──
    var amountReceivedText by remember { mutableStateOf(outstandingBalance.toInt().toString()) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // ── Reference / UTR Number ──
    var referenceNumber by remember { mutableStateOf("") }

    // ── Date Handling ──
    val todayDisplayDate = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }
    var paymentDisplayDate by remember { mutableStateOf(todayDisplayDate) }

    // ── Notes ──
    var notes by remember { mutableStateOf("Payment for invoice $invoiceNumber") }
    var sendNotification by remember { mutableStateOf(false) }

    // ── Reusable Attachments ──
    val uploadedAttachments = remember { mutableStateListOf<Uri>() }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uploadedAttachments.addAll(uris)
    }

    // Helper: Converts DD-MM-YYYY to ISO-8601 (e.g., 2026-10-12T00:00:00.000Z)
    fun formatDisplayDateToIso(displayDate: String): String {
        return try {
            val parser = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsedDate = parser.parse(displayDate) ?: Date()
            isoFormatter.format(parsedDate)
        } catch (_: Exception) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                Modifier.fillMaxWidth()
            ) {
                TitleBar(title = "Record Payment", onClose = onClose)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // ── Allocated Invoice Banner ──
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Payment for #$invoiceNumber",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "Record a new payment to customer and update balance.",
                    fontSize = tokens.caption,
                    color = close_color
                )
            }

            // ── Dynamic Customer Field (Read-only) ──
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                FormLabel(text = "Customer", isRequired = true)
                FormTextField(
                    value = "$customerName ($customerCode)",
                    onValueChange = {},
                    enabled = false,
                    containerColor = primary_light.copy(alpha = 0.4f),
                    borderColor = grey_border
                )
            }

            // ── Branch Dropdown ──
            FormDropdown(
                label = "Branch",
                value = selectedBranchLabel.ifBlank { "Select Branch" },
                expanded = branchExpanded,
                onExpandChange = { branchExpanded = it },
                options = branchOptions,
                onOptionSelected = { branchName ->
                    selectedBranchLabel = branchName
                    selectedBranchId = branchList.find { it.name == branchName }?.id ?: selectedBranchId
                }
            )

            // ── Payment Mode Dropdown ──
            FormDropdown(
                label = "Payment Mode",
                value = selectedPaymentMode,
                expanded = paymentModeExpanded,
                isRequired = true,
                onExpandChange = { paymentModeExpanded = it },
                options = listOf(
                    "Cash",
                    "UPI",
                    "Card",
                    "Bank Transfer (NEFT/RTGS)",
                    "Cheque"
                ),
                onOptionSelected = { selectedPaymentMode = it }
            )

            // ── Amount Received Field with 'Pay Full' Shortcut ──
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormLabel(text = "Amount Received (INR)", isRequired = true)
                    Text(
                        text = "Pay Full (${currencySymbol()}${formatIndianNumber(outstandingBalance.toLong())})",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        modifier = Modifier.clickable {
                            amountReceivedText = outstandingBalance.toInt().toString()
                            amountError = null
                        }
                    )
                }

                FormTextField(
                    value = amountReceivedText,
                    onValueChange = {
                        amountReceivedText = it
                        if (amountError != null) amountError = null
                    },
                    keyboardType = KeyboardType.Number,
                    placeholder = "Enter amount",
                    isError = amountError != null,
                    errorMessage = amountError
                )
            }

            // ── Remaining Balance Notice Alert Box ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(primary_light)
                    .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .padding(horizontal = tokens.screenPadding * 0.8f, vertical = tokens.extraPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(tokens.extraPadding))
                    Text(
                        text = "Remaining balance for this invoice is ${currencySymbol()}${formatIndianNumber(outstandingBalance.toLong())}.",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            // ── Deposit To Account (GL Ledger) Dropdown ──
            // ── Deposit To Account (GL Ledger) Dropdown ──
            FormDropdown(
                label = "Deposit To Account (GL Ledger)",
                value = when {
                    isAccountsLoading -> "Loading accounts..."
                    selectedDepositAccountLabel.isNotBlank() -> selectedDepositAccountLabel
                    else -> "Select Account"
                },
                expanded = depositAccountExpanded && !isAccountsLoading,
                isRequired = true,
                onExpandChange = { depositAccountExpanded = it },
                options = accountDropdownList.map { "${it.accountName} (${it.accountCode})" },
                onOptionSelected = { selectedLabel ->
                    selectedDepositAccountLabel = selectedLabel
                    selectedDepositAccountId = accountDropdownList.find {
                        "${it.accountName} (${it.accountCode})" == selectedLabel
                    }?.id.orEmpty()
                }
            )
            // ── Payment Date Field ──
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                FormLabel(text = "Payment Date", isRequired = true)
                DatePickerField(
                    value = paymentDisplayDate,
                    onDateSelected = { selectedDate ->
                        paymentDisplayDate = selectedDate
                    }
                )
            }

            // ── Reference / UTR Number Field ──
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                FormLabel(text = "Reference / UTR No. (Optional)", isRequired = false)
                FormTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    placeholder = "e.g. UPI-99884 or TXN990234"
                )
            }

            // ── Notes / Remarks Field ──
            Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.25f)) {
                FormLabel(text = "Notes", isRequired = false)
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Add any internal remarks or additional details...",
                    minLines = 3,
                    maxLines = 4
                )
            }

            // ── Reused ImageUploadSection for Attachment Slips ──
            ImageUploadSection(
                selectedImages = uploadedAttachments,
                title = "Receipt Slip / Attachment",
                subtitle = "Upload receipt slip or payment proof",
                supportedFormatsText = "Max 5MB per file (PDF, JPEG, PNG)",
                onBrowseClick = { filePickerLauncher.launch("*/*") },
                onRemoveImage = { uploadedAttachments.remove(it) },
                maxFiles = 3
            )

            // ── Notification Checkbox ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { sendNotification = !sendNotification },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(
                    checked = sendNotification,
                    onCheckedChange = { sendNotification = it }
                )
                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                Text(
                    text = "Send a Payment confirmation notification to customer",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

            // ── Normal Flow Action Buttons (Scrollable With Form) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.extraPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                BackFabButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    onClick = onClose,
                    label = "Cancel",
                    showArrow = false,
                    enabled = !isLoading
                )

                // Confirm & Post button
                TrailingFabButton(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(tokens.buttonHeight),
                    action = TrailingFabAction.Update(
                        label = "Confirm & Post",
                        isLoading = isLoading,
                        enabled = !isLoading,
                        onClick = {
                            val parsedAmount = amountReceivedText.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                amountError = "Please enter a valid amount"
                            } else {
                                amountError = null
                                val isoDate = formatDisplayDateToIso(paymentDisplayDate)

                                onSavePayment(
                                    dynamicCustomerId,
                                    selectedBranchId,
                                    dynamicInvoiceId,
                                    parsedAmount,
                                    selectedPaymentMode,
                                    selectedDepositAccountId,
                                    isoDate,
                                    referenceNumber,
                                    notes,
                                    uploadedAttachments.toList(),
                                    sendNotification
                                )
                            }
                        }
                    ),
                    showArrow = false
                )
            }

            Spacer(Modifier.height(tokens.screenPadding * 0.5f))
        }
    }
}