@file:Suppress("UNUSED_PARAMETER")

package com.cuso.tailor.view.home.finance.receipts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CustomerOutstandingInvoiceItem
import com.cuso.tailor.model.sales.CustomerItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.CustomerUiState
import com.cuso.tailor.viewmodel.CustomerViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Payment Mode Option Model
private data class PaymentModeOption(
    val label: String,
    val icon: ImageVector
)

private val paymentModes = listOf(
    PaymentModeOption("Cash", Icons.Outlined.Payments),
    PaymentModeOption("UPI", Icons.Outlined.QrCodeScanner),
    PaymentModeOption("Card", Icons.Outlined.CreditCard),
    PaymentModeOption("Bank Transfer", Icons.Outlined.AccountBalance),
    PaymentModeOption("Cheque", Icons.AutoMirrored.Outlined.ReceiptLong)
)

@Composable
fun CreatePaymentReceiptScreen(
    financeViewModel: FinanceViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onPaymentRecorded: () -> Unit = {}
){
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Observe customer list from CustomerViewModel
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val customers: List<CustomerItem> = remember(customerUiState) {
        (customerUiState as? CustomerUiState.Success)?.customers.orEmpty()
    }

    // Observe outstanding invoices & deposit accounts from FinanceViewModel
    val outstandingInvoices by financeViewModel.outstandingInvoices.collectAsStateWithLifecycle()
    val depositAccounts by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()

    // Form states
    var selectedCustomerId by remember { mutableStateOf("") }
    var selectedCustomerName by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("Head Office") }
    var selectedInvoice by remember { mutableStateOf<CustomerOutstandingInvoiceItem?>(null) }

    var amountReceived by remember { mutableStateOf("") }
    var selectedDepositAccountId by remember { mutableStateOf("") }
    var selectedDepositAccountName by remember { mutableStateOf("Cash on Hand (100200)") }
    var selectedPaymentMode by remember { mutableStateOf("Cash") }
    var referenceNumber by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
    }
    var transactionNotes by remember { mutableStateOf("") }
    var attachedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dropdown expanded states
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }
    var invoiceDropdownExpanded by remember { mutableStateOf(false) }
    var depositAccountDropdownExpanded by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            attachedUris = (attachedUris + uris).take(2)
        }
    }

    // Trigger initial load: customers from CustomerViewModel, accounts from FinanceViewModel
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers(isRefresh = true)
        financeViewModel.fetchChartOfAccountsDropdown("payment_source")
    }

    // Set default deposit account
    LaunchedEffect(depositAccounts) {
        if (depositAccounts.isNotEmpty() && selectedDepositAccountId.isBlank()) {
            val defaultAcc = depositAccounts.first()
            selectedDepositAccountId = defaultAcc.id
            selectedDepositAccountName = "${defaultAcc.accountName} (${defaultAcc.accountCode})"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Title Bar
        TitleBar(
            title = "Record Payment",
            onClose = onClose
        )

        HorizontalDivider(color = grey_border, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // ── CARD 1: CUSTOMER & ENTITY SETUP ──
            SetupSectionCard(title = "CUSTOMER & ENTITY SETUP") {
                // Customer FormDropdown populated from CustomerViewModel
                val customerOptions = remember(customers) {
                    customers.map {
                        it.fullName?.takeIf { name -> name.isNotBlank() }
                            ?: it.custName?.takeIf { name -> name.isNotBlank() }
                            ?: "Unnamed Customer"
                    }
                }

                FormDropdown(
                    label = "Customer",
                    isRequired = true,
                    value = selectedCustomerName.ifBlank { "Select Customer" },
                    expanded = customerDropdownExpanded,
                    onExpandChange = { customerDropdownExpanded = it },
                    options = customerOptions,
                    onOptionSelected = { selectedName ->
                        val matchedCustomer = customers.find { customer ->
                            val name = customer.fullName?.takeIf { it.isNotBlank() }
                                ?: customer.custName?.takeIf { it.isNotBlank() }
                                ?: "Unnamed Customer"
                            name == selectedName
                        }
                        if (matchedCustomer != null) {
                            selectedCustomerId = matchedCustomer.id
                            selectedCustomerName = selectedName
                            selectedInvoice = null
                            // Fetch outstanding invoices for this selected customer
                            financeViewModel.fetchCustomerOutstandingInvoices(matchedCustomer.id)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Branch FormDropdown
                FormDropdown(
                    label = "Branch",
                    value = selectedBranch,
                    expanded = branchDropdownExpanded,
                    onExpandChange = { branchDropdownExpanded = it },
                    options = listOf("Head Office", "Main Store", "Warehouse"),
                    onOptionSelected = { selectedBranch = it }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Allocate to Invoice FormDropdown (Dynamically populated from outstanding invoices API)
                val invoiceOptions = remember(outstandingInvoices) {
                    listOf("None (Direct Advance)") + outstandingInvoices.map {
                        "${it.invoiceNumber}  •  Due: ₹${it.balanceDue.toInt()}"
                    }
                }

                val invoiceDisplayValue = when {
                    selectedCustomerId.isBlank() -> "Select customer first to view unpaid in..."
                    selectedInvoice != null -> "${selectedInvoice?.invoiceNumber}  •  Due: ₹${selectedInvoice?.balanceDue?.toInt()}"
                    outstandingInvoices.isEmpty() -> "No outstanding invoices found"
                    else -> "Select Invoice (Optional)"
                }

                FormDropdown(
                    label = "Allocate to Invoice (Optional)",
                    value = invoiceDisplayValue,
                    expanded = invoiceDropdownExpanded,
                    onExpandChange = {
                        if (selectedCustomerId.isNotBlank() && outstandingInvoices.isNotEmpty()) {
                            invoiceDropdownExpanded = it
                        }
                    },
                    options = invoiceOptions,
                    onOptionSelected = { selectedOption ->
                        if (selectedOption.startsWith("None")) {
                            selectedInvoice = null
                        } else {
                            val matchedInvoice = outstandingInvoices.find {
                                "${it.invoiceNumber}  •  Due: ₹${it.balanceDue.toInt()}" == selectedOption
                            }
                            selectedInvoice = matchedInvoice
                            // Pre-fill amount received with outstanding balance due
                            matchedInvoice?.let {
                                amountReceived = it.balanceDue.toInt().toString()
                            }
                        }
                    },
                    enabled = selectedCustomerId.isNotBlank() && outstandingInvoices.isNotEmpty()
                )
            }

            // ── CARD 2: TRANSACTION & AMOUNT DETAILS ──
            SetupSectionCard(title = "TRANSACTION & AMOUNT DETAILS") {
                // Amount Received FormTextField
                FormLabel(text = "Amount Received (INR)", isRequired = true)
                FormTextField(
                    value = amountReceived,
                    onValueChange = { amountReceived = it },
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Number,
                    borderColor = grey_border
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Deposit Account FormDropdown
                val accountOptions = remember(depositAccounts) {
                    depositAccounts.map { "${it.accountName} (${it.accountCode})" }
                }

                FormDropdown(
                    label = "Deposit Account (GL Ledger)",
                    isRequired = true,
                    value = selectedDepositAccountName,
                    expanded = depositAccountDropdownExpanded,
                    onExpandChange = { depositAccountDropdownExpanded = it },
                    options = accountOptions,
                    onOptionSelected = { selectedAccountStr ->
                        val matchedAccount = depositAccounts.find {
                            "${it.accountName} (${it.accountCode})" == selectedAccountStr
                        }
                        if (matchedAccount != null) {
                            selectedDepositAccountId = matchedAccount.id
                            selectedDepositAccountName = selectedAccountStr
                        }
                    }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Payment Mode Buttons
                FormLabel(text = "Payment Mode", isRequired = true)
                PaymentModeSelectionGrid(
                    selectedMode = selectedPaymentMode,
                    onModeSelected = { selectedPaymentMode = it }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Reference / UTR Number FormTextField
                FormLabel(text = "Reference / UTR / Cheque No. (Opt.)", isRequired = false)
                FormTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    placeholder = "e.g. UPI-984729482",
                    borderColor = grey_border
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Payment Date using DatePickerField
                FormLabel(text = "Payment Date", isRequired = true)
                DatePickerField(
                    value = paymentDate,
                    onDateSelected = { paymentDate = it }
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Transaction Notes FormTextArea
                FormLabel(text = "Transaction Notes", isRequired = false)
                FormTextArea(
                    value = transactionNotes,
                    onValueChange = { transactionNotes = it },
                    placeholder = "Internal accounting remarks or receipt details...",
                    minLines = 3,
                    maxLines = 5,
                    borderColor = BorderGray,
                    focusedBorderColor = Primary
                )
            }

            // ── CARD 3: RECEIPT ALLOCATION SUMMARY ──
            SetupSectionCard(title = "RECEIPT ALLOCATION SUMMARY") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt Type:",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (selectedInvoice != null) "Invoice Settlement" else "Direct Customer Advance",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Received:",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "₹${amountReceived.ifBlank { "0" }}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // General Ledger Notice Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(primary_light.copy(alpha = 0.5f))
                        .padding(tokens.extraPadding)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "General Ledger Settlement:",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = "Confirming this payment will debit the selected Cash/Bank account and credit Accounts Receivable in the General Ledger.",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── CARD 4: REUSABLE IMAGE UPLOAD SECTION ──
            SetupSectionCard(title = "RECEIPT SLIP / ATTACHMENTS") {
                ImageUploadSection(
                    selectedImages = attachedUris,
                    onBrowseClick = { filePickerLauncher.launch("image/*") },
                    onRemoveImage = { uri -> attachedUris = attachedUris.filter { it != uri } },
                    maxFiles = 2,
                    title = "Upload Slip",
                    subtitle = "PNG, JPG or PDF • Max 2 files (Images auto-compressed to <5MB)",
                    supportedFormatsText = "Only support .jpg, .png and .pdf files",
                    browseText = "Upload Slip"
                )
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding))

            // Confirm & Record Payment Button
            Button(
                onClick = {
                    val amountVal = amountReceived.toDoubleOrNull() ?: 0.0
                    if (selectedCustomerId.isBlank()) {
                        DynamicIslandManager.showError("Please select a customer")
                        return@Button
                    }
                    if (amountVal <= 0.0) {
                        DynamicIslandManager.showError("Please enter a valid amount")
                        return@Button
                    }

                    val attachmentParts = attachedUris.mapNotNull { uri ->
                        uriToMultipartPart(context, uri, "attachments")
                    }

                    financeViewModel.recordCustomerPayment(
                        customerId = selectedCustomerId,
                        branchId = "6ab2063f95aeff058133deb1", // Default active branch ID
                        invoiceId = selectedInvoice?.id.orEmpty(),
                        amount = amountVal,
                        paymentMode = selectedPaymentMode,
                        depositToAccountId = selectedDepositAccountId.ifBlank { "6ac65e3509793a1c9b2bb2fc" },
                        paymentDate = paymentDate,
                        referenceNumber = referenceNumber,
                        notes = transactionNotes,
                        attachmentParts = attachmentParts,
                        onSuccess = { successMessage ->
                            DynamicIslandManager.showSuccess(successMessage)
                            financeViewModel.fetchCustomerReceipts(page = 1)
                            onPaymentRecorded()
                            onClose()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Confirm & Record Payment",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
            }

            Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
        }
    }
}

/**
 * Reusable Card container for setup sections.
 */
@Composable
private fun SetupSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Spacer(modifier = Modifier.height(tokens.extraPadding))
            content()
        }
    }
}

/**
 * Payment mode selection boxes matching UI.
 */
@Composable
private fun PaymentModeSelectionGrid(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
        // Row 1: Cash, UPI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
        ) {
            paymentModes.take(2).forEach { mode ->
                PaymentModeBox(
                    mode = mode,
                    isSelected = selectedMode == mode.label,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(mode.label) }
                )
            }
        }

        // Row 2: Card, Bank Transfer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
        ) {
            paymentModes.slice(2..3).forEach { mode ->
                PaymentModeBox(
                    mode = mode,
                    isSelected = selectedMode == mode.label,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(mode.label) }
                )
            }
        }

        // Row 3: Cheque
        Row(
            modifier = Modifier.fillMaxWidth(0.485f)
        ) {
            PaymentModeBox(
                mode = paymentModes.last(),
                isSelected = selectedMode == paymentModes.last().label,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onModeSelected(paymentModes.last().label) }
            )
        }
    }
}

@Composable
private fun PaymentModeBox(
    mode: PaymentModeOption,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(if (isSelected) primary_light else whiteBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else BorderGray,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = mode.icon,
                contentDescription = mode.label,
                tint = if (isSelected) Primary else TextSecondary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mode.label,
                fontSize = tokens.caption,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) Primary else TextSecondary
            )
        }
    }
}