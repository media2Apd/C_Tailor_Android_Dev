@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.procurement.billslist

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ProcurementBillDetailData
import com.cuso.tailor.model.inventory.RecordPaymentRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordBillPaymentScreen(
    billId: String,
    onClose: () -> Unit,
    onPaymentSuccess: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val billDetail by inventoryViewModel.selectedBillDetail.collectAsStateWithLifecycle()
    val isLoading by inventoryViewModel.isLoadingBillDetail.collectAsStateWithLifecycle()
    val error by inventoryViewModel.billDetailError.collectAsStateWithLifecycle()

    val isRecordingPayment by inventoryViewModel.isRecordingPayment.collectAsStateWithLifecycle()
    val recordPaymentError by inventoryViewModel.recordPaymentErrorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(recordPaymentError) {
        recordPaymentError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearRecordPaymentError()
        }
    }

    LaunchedEffect(billId) {
        if (billId.isNotBlank() && billDetail?.id != billId) {
            inventoryViewModel.fetchBillDetail(billId)
        }
    }

    LaunchedEffect(Unit) {
        financeViewModel.fetchChartOfAccountsDropdown(context = "payment_source")
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar("Record Payment Form", onClose = onClose)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                error != null && billDetail == null -> {
                    AppErrorState(
                        title = "Failed to load bill details",
                        message = error ?: "Unknown error occurred",
                        onRetry = {
                            if (billId.isNotBlank()) {
                                inventoryViewModel.fetchBillDetail(billId)
                            }
                        }
                    )
                }

                billDetail != null -> {
                    RecordPaymentFormContent(
                        detail = billDetail!!,
                        tokens = tokens,
                        financeViewModel = financeViewModel,
                        isSubmitting = isRecordingPayment,
                        onCancel = onClose,
                        onSubmitPayment = { request ->
                            inventoryViewModel.recordPayment(
                                billId = billDetail!!.id,
                                request = request,
                                onSuccess = onPaymentSuccess
                            )
                        }
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "-",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordPaymentFormContent(
    detail: ProcurementBillDetailData,
    tokens: AppDesignTokens,
    financeViewModel: FinanceViewModel,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onSubmitPayment: (RecordPaymentRequest) -> Unit
) {
    val context = LocalContext.current
    val paymentSourceAccounts by financeViewModel.accountDropdownList.collectAsStateWithLifecycle()

    val paidThroughOptions = remember(paymentSourceAccounts) {
        paymentSourceAccounts.map { it.accountName }.ifEmpty { listOf("-") }
    }

    var location by remember(detail.warehouseId) {
        mutableStateOf(detail.warehouseId?.name?.ifBlank { null } ?: "-")
    }
    var locationExpanded by remember { mutableStateOf(false) }

    var paymentMode by remember {
        mutableStateOf("Cash")
    }
    var paymentModeExpanded by remember { mutableStateOf(false) }

    var paymentMadeAmount by remember(detail.balanceDue) {
        mutableStateOf(
            if (detail.balanceDue > 0) {
                if (detail.balanceDue % 1.0 == 0.0) detail.balanceDue.toLong().toString() else detail.balanceDue.toString()
            } else ""
        )
    }

    var paymentNumber by remember(detail.billNumber, detail.id) {
        val identifier = detail.billNumber.ifBlank { detail.id.takeLast(6) }.ifBlank { "-" }
        mutableStateOf("PAY-$identifier")
    }

    var paidThroughAccountName by remember { mutableStateOf("") }
    var paidThroughExpanded by remember { mutableStateOf(false) }

    val initialDate = remember(detail.billDate) {
        formatIsoToDatePicker(detail.billDate)
    }
    var paymentDate by remember(detail.billDate) {
        mutableStateOf(initialDate)
    }

    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var sendEmailNotification by remember { mutableStateOf(false) }
    var attachedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            attachedUris = (attachedUris + uris).distinct()
        }
    }

    val locationOptions = remember(detail.warehouseId) {
        listOfNotNull(detail.warehouseId?.name?.ifBlank { null }).ifEmpty { listOf("-") }
    }

    val paymentModeOptions = listOf(
        "Cash",
        "Bank Transfer (NEFT/RTGS)",
        "UPI",
        "Cheque",
        "Credit / Debit Card"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentPadding = PaddingValues(
                top = tokens.extraPadding,
                bottom = 90.dp // Leave clearance for StepNavigationFab
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // Header Subtitle
            item {
                Spacer(Modifier.height(tokens.screenPadding))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
                    ) {
                        val billIdHeader = detail.billNumber.ifBlank { detail.id.ifBlank { "-" } }
                        Text(
                            text = "Payment for #$billIdHeader",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Record a new payment to vendor and update balance.",
                            fontSize = tokens.caption,
                            color = close_color
                        )
                    }
                }
            }

            // 1. Location Dropdown
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormDropdown(
                        label = "Location",
                        isRequired = false,
                        value = location,
                        expanded = locationExpanded,
                        onExpandChange = { locationExpanded = it },
                        options = locationOptions,
                        onOptionSelected = {
                            location = it
                            locationExpanded = false
                        }
                    )
                }
            }

            // 2. Payment Mode Dropdown
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormDropdown(
                        label = "Payment Mode",
                        isRequired = true,
                        value = paymentMode,
                        expanded = paymentModeExpanded,
                        onExpandChange = { paymentModeExpanded = it },
                        options = paymentModeOptions,
                        onOptionSelected = {
                            paymentMode = it
                            paymentModeExpanded = false
                        }
                    )
                }
            }

            // 3. Payment Made (INR)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Payment Made (INR)", isRequired = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(horizontal = tokens.cardPadding * 0.6f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹ ",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            FormTextField(
                                value = paymentMadeAmount,
                                onValueChange = { paymentMadeAmount = it },
                                keyboardType = KeyboardType.Decimal,
                                placeholder = "Enter amount",
                                borderColor = Color.Transparent,
                                containerColor = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 4. Info Banner: Remaining Balance
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF))
                            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            val formattedBalance = if (detail.balanceDue > 0) "₹${formatIndianNumber(detail.balanceDue)}" else "₹0"
                            Text(
                                text = "Remaining balance for this vendor is $formattedBalance.",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // 5. Payment #
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Payment #", isRequired = false)
                    FormTextField(
                        value = paymentNumber,
                        onValueChange = { paymentNumber = it },
                        placeholder = "Auto-generated on save",
                        enabled = false
                    )
                }
            }

            // 6. Paid Through Dropdown
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormDropdown(
                        label = "Paid Through",
                        isRequired = true,
                        value = paidThroughAccountName.ifBlank { "Select Account" },
                        expanded = paidThroughExpanded,
                        onExpandChange = { paidThroughExpanded = it },
                        options = paidThroughOptions,
                        onOptionSelected = { selectedAccountName ->
                            paidThroughAccountName = selectedAccountName
                            paidThroughExpanded = false
                        }
                    )
                }
            }

            // 7. Payment Date Field
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Payment Made On", isRequired = true)
                    DatePickerField(
                        value = paymentDate,
                        onDateSelected = { selectedDate ->
                            paymentDate = selectedDate
                        }
                    )
                }
            }

            // 8. Reference #
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Reference # (Optional)", isRequired = false)
                    FormTextField(
                        value = referenceNumber,
                        onValueChange = { referenceNumber = it },
                        placeholder = "e.g. TXN990234"
                    )
                }
            }

            // 9. Notes
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Notes", isRequired = false)
                    FormTextArea(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Add any internal remarks or additional details...",
                        minLines = 3,
                        maxLines = 4
                    )
                }
            }

            // 10. Attachments
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormLabel(text = "Attachments", isRequired = false)
                    ImageUploadSection(
                        isImage = false,
                        selectedImages = attachedUris,
                        onBrowseClick = { filePickerLauncher.launch("*/*") },
                        onRemoveImage = { removedUri ->
                            attachedUris = attachedUris - removedUri
                        },
                        documentUploadText = "Click to upload or drag and drop files\nMaximum 5MB per file (PDF, JPEG, PNG)",
                        uploadBoxHeight = 120.dp,
                        previewHeaderTitle = "ATTACHMENTS"
                    )
                }
            }

            // 11. Email Notification Checkbox
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sendEmailNotification = !sendEmailNotification },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCheckbox(
                            checked = sendEmailNotification,
                            onCheckedChange = { sendEmailNotification = it }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Send a Payment Made email notification to vendor",
                            fontSize = tokens.caption,
                            color = title_color
                        )
                    }
                }
            }
        }

        // 12. Bottom Navigation FAB Bar
        StepNavigationFab(
            modifier = Modifier.align(Alignment.BottomCenter),
            showBack = true,
            backLabel = "Cancel",
            showBackArrow = false,
            onBack = onCancel,
            isLoading = isSubmitting,
            trailingAction = TrailingFabAction.Update(
                label = "Save Payment",
                isLoading = isSubmitting,
                enabled = !isSubmitting,
                onClick = {
                    val parsedAmount = paymentMadeAmount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        Toast.makeText(context, "Please enter a valid payment amount", Toast.LENGTH_SHORT).show()
                        return@Update
                    }

                    val selectedAccount = paymentSourceAccounts.firstOrNull { it.accountName == paidThroughAccountName }
                    if (selectedAccount == null) {
                        Toast.makeText(context, "Please select a Paid Through account", Toast.LENGTH_SHORT).show()
                        return@Update
                    }

                    // Convert dd-MM-yyyy to yyyy-MM-dd required by the backend
                    val backendFormattedDate = formatDateToBackend(paymentDate)

                    val request = RecordPaymentRequest(
                        branchId = null,
                        billId = detail.id,
                        paymentDate = backendFormattedDate,
                        amount = parsedAmount,
                        paymentMode = mapPaymentModeToBackend(paymentMode),
                        referenceNumber = referenceNumber.trim(),
                        paidFromAccountId = selectedAccount.id,
                        notes = notes.trim()
                    )

                    onSubmitPayment(request)
                }
            )
        )
    }
}

/**
 * Maps UI payment modes directly to backend expected values.
 */
private fun mapPaymentModeToBackend(mode: String): String {
    return when (mode) {
        "Cash" -> "Cash"
        "Bank Transfer (NEFT/RTGS)" -> "Bank"
        "UPI" -> "UPI"
        "Cheque" -> "Cheque"
        "Credit / Debit Card" -> "Card"
        else -> "Cash"
    }
}

/**
 * Converts dd-MM-yyyy date picker output to yyyy-MM-dd for backend payload.
 */
private fun formatDateToBackend(dateStr: String): String {
    return try {
        val parts = dateStr.trim().split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
    } catch (_: Exception) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}

/**
 * Converts ISO 8601 string into dd-MM-yyyy for DatePickerField display.
 */
private fun formatIsoToDatePicker(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val datePart = isoDate.substringBefore("T")
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            datePart
        }
    } catch (_: Exception) {
        ""
    }
}