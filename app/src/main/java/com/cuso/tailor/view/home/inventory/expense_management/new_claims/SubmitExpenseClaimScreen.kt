@file:Suppress("unused", "UNUSED_PARAMETER", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.expense_management.new_claims

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.hr.attendance_management.employees.uriToFile
import com.cuso.tailor.viewmodel.Authenticate
import com.cuso.tailor.viewmodel.HrViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitExpenseClaimScreen(
    claimId: String? = null,
    hrViewModel: HrViewModel = hiltViewModel(),
    authViewModel: Authenticate = hiltViewModel(),
    onClose: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var expenseTitle by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var taxInput by remember { mutableStateOf("") }
    val attachedFiles = remember { mutableStateListOf<Uri>() }
    var showDatePicker by remember { mutableStateOf(false) }

    val activeTypes by hrViewModel.activeExpenseTypes.collectAsStateWithLifecycle()
    val categoryOptions = remember(activeTypes) { activeTypes.map { it.name } }

    val claimDetail by hrViewModel.expenseClaimDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by hrViewModel.isLoadingExpenseClaimDetail.collectAsStateWithLifecycle()
    val isSubmitting by hrViewModel.isSubmittingExpenseClaim.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachedFiles.addAll(uris)
    }

    LaunchedEffect(Unit) {
        hrViewModel.fetchActiveExpenseTypes()
        if (!claimId.isNullOrBlank()) {
            hrViewModel.fetchExpenseClaimDetail(claimId)
        } else {
            hrViewModel.clearExpenseClaimDetail()
        }
    }

    LaunchedEffect(claimDetail) {
        claimDetail?.let { detail ->
            expenseTitle = detail.title.orEmpty()
            description = detail.description.orEmpty()
            amount = if (detail.amount > 0) detail.amount.toString() else ""
            taxInput = if ((detail.taxAmount ?: 0.0) > 0) (detail.taxAmount ?: 0.0).toString() else ""
            selectedCategoryId = detail.category?.id.orEmpty()
            selectedCategoryName = detail.category?.name.orEmpty()
            detail.expenseDate?.let {
                expenseDate = it.take(10)
            }
        }
    }

    val userEntity by authViewModel.user.collectAsStateWithLifecycle()
    val employeeName = userEntity?.let { "${it.firstName} ${it.lastName}".trim() } ?: "Rajesh Kumar"

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = whiteBg,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar(
                    title = if (claimId.isNullOrBlank()) "Submit Expense claim" else "Edit Expense Claim",
                    onClose = {
                        hrViewModel.clearExpenseClaimDetail()
                        onClose()
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoadingDetail) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(PanelBg)
                    .verticalScroll(scrollState)
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Expense Details
                FormSectionCard(title = "Expense Details") {
                    FormLabel(text = "Expense Title", isRequired = true)
                    FormTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        placeholder = "Enter claim title..."
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel(text = "Expense Date")
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        FormTextField(
                            value = expenseDate,
                            onValueChange = { },
                            enabled = false,
                            placeholder = "Select Date"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    FormLabel(text = "Category", isRequired = true)
                    FormDropdown(
                        value = selectedCategoryName,
                        expanded = isCategoryDropdownExpanded,
                        onExpandChange = { isCategoryDropdownExpanded = it },
                        options = categoryOptions,
                        onOptionSelected = { chosenName ->
                            selectedCategoryName = chosenName
                            selectedCategoryId = activeTypes.firstOrNull { it.name == chosenName }?.id.orEmpty()
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel(text = "Description")
                    FormTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Provide extra description here..."
                    )
                }

                // Section 2: Amount & Tax
                FormSectionCard(title = "Amount & Tax") {
                    FormLabel(text = "Amount", isRequired = true)
                    FormTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = "${currencySymbol()}0.00",
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(Modifier.height(12.dp))

                    FormLabel(text = "Tax Input")
                    FormTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        placeholder = "${currencySymbol()}0.00",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                // Section 3: Receipt Attachment (Reusing ImageUploadSection)
                FormSectionCard(title = "Receipt Attachment") {
                    ImageUploadSection(
                        selectedImages = attachedFiles.toList(),
                        onBrowseClick = { filePickerLauncher.launch("*/*") },
                        onRemoveImage = { uri -> attachedFiles.remove(uri) },
                        maxFiles = 5,
                        subtitle = "Attach bills or receipts for this expense",
                        supportedFormatsText = "Only support .jpg, .png, and .pdf files up to 5MB"
                    )
                }

                // Section 4: Employee Information
                FormSectionCard(title = "Employee Information") {
                    ReadonlyInfoField(label = "Name", value = employeeName)
                    Spacer(Modifier.height(8.dp))
                    ReadonlyInfoField(label = "Department", value = "-")
                    Spacer(Modifier.height(8.dp))
                    ReadonlyInfoField(label = "Reporting Manager", value = "-")
                }

                val handleSaveClaim: (String) -> Unit = { targetStatus ->
                    val files: List<File> = attachedFiles.map { uri ->
                        uriToFile(context, uri)
                    }

                    hrViewModel.submitExpenseClaim(
                        claimId = claimId,
                        title = expenseTitle,
                        description = description,
                        amount = amount,
                        taxAmount = taxInput,
                        currency = "INR",
                        status = targetStatus,
                        expenseDate = expenseDate,
                        category = selectedCategoryId,
                        files = files,
                        onSuccess = { successMsg ->
                            DynamicIslandManager.showSuccess(successMsg)
                            hrViewModel.clearExpenseClaimDetail()
                            onSubmitSuccess()
                        },
                        onError = { errorMsg ->
                            DynamicIslandManager.showError(errorMsg)
                        }
                    )
                }

                // Actions
                Button(
                    onClick = { handleSaveClaim("Pending_Approval") },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize), strokeWidth = 2.dp)
                    } else {
                        Text(text = "Submit Claim", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                    }
                }

                OutlinedButton(
                    onClick = { handleSaveClaim("Draft") },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
                ) {
                    Text(text = "Save as Draft", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ReadonlyInfoField(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(PanelBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = tokens.label, color = headerGrey)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

