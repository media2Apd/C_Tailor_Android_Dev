@file:Suppress("unused", "DEPRECATION")

package com.cuso.tailor.view.home.inventory.expense_management.new_claims

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Timeline
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
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.hr.attendance_management.employees.uriToFile
import com.cuso.tailor.viewmodel.HrViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditExpenseClaimScreen(
    claimId: String,
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val activeCategories by hrViewModel.activeExpenseTypes.collectAsStateWithLifecycle()
    val categoryNames = remember(activeCategories) { activeCategories.map { it.name } }

    val claimDetail by hrViewModel.expenseClaimDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by hrViewModel.isLoadingExpenseClaimDetail.collectAsStateWithLifecycle()
    val isSubmitting by hrViewModel.isSubmittingExpenseClaim.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf("") }

    var baseAmount by remember { mutableStateOf("") }
    var taxAmount by remember { mutableStateOf("") }

    var employeeName by remember { mutableStateOf("—") }
    var departmentName by remember { mutableStateOf("—") }
    var expenseOwner by remember { mutableStateOf("—") }

    var currentStatus by remember { mutableStateOf("Approved") }
    var approverName by remember { mutableStateOf("—") }
    var approvalDate by remember { mutableStateOf("—") }
    var internalComments by remember { mutableStateOf("") }

    val attachedFiles = remember { mutableStateListOf<Uri>() }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    // Use single get content or limit to 1-3 files to avoid memory exhaustion
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachedFiles.clear()
        attachedFiles.addAll(uris.take(5))
    }

    // Clean up when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            attachedFiles.clear()
            hrViewModel.clearExpenseClaimDetail()
        }
    }

    LaunchedEffect(claimId) {
        if (activeCategories.isEmpty()) {
            hrViewModel.fetchActiveExpenseTypes()
        }
        if (claimId.isNotBlank()) {
            hrViewModel.fetchExpenseClaimDetail(claimId)
        }
    }

    // Prefill form values from View-One API only once
    var isFormPrefilled by remember { mutableStateOf(false) }
    LaunchedEffect(claimDetail) {
        claimDetail?.let { d ->
            if (!isFormPrefilled) {
                title = d.title.orEmpty()
                description = d.description.orEmpty()
                selectedCategoryId = d.category?.id.orEmpty()
                selectedCategoryName = d.category?.name.orEmpty()
                expenseDate = formatClaimEditDate(d.expenseDate)

                baseAmount = if (d.amount > 0.0) d.amount.toInt().toString() else ""
                taxAmount = if ((d.taxAmount ?: 0.0) > 0.0) (d.taxAmount ?: 0.0).toInt().toString() else ""

                employeeName = d.employee?.fullName?.ifBlank { "—" } ?: "—"
                departmentName = "—"
                expenseOwner = "—"

                currentStatus = d.status?.ifBlank { "Approved" } ?: "Approved"
                approverName = "—"
                approvalDate = "—"
                isFormPrefilled = true
            }
        }
    }

    val baseVal = baseAmount.toDoubleOrNull() ?: 0.0
    val taxVal = taxAmount.toDoubleOrNull() ?: 0.0
    val grandTotal = baseVal + taxVal

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Expense Record",
                onClose = {
                    hrViewModel.clearExpenseClaimDetail()
                    onClose()
                }
            )
        }
    ) { paddingValues ->
        if (isLoadingDetail && !isFormPrefilled) {
            ListSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                item { Spacer(Modifier.height(tokens.extraPadding * 0.4f)) }

                // 1. Basic Information Card
                item {
                    SectionCardContainer(title = "Basic Information", icon = Icons.Outlined.Description) {
                        FormLabel(text = "Expense Title")
                        FormTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Enter expense title"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Description")
                        FormTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Enter description"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Category")
                        FormDropdown(
                            value = selectedCategoryName.ifBlank { "Select Category" },
                            expanded = isCategoryDropdownExpanded,
                            onExpandChange = { isCategoryDropdownExpanded = it },
                            options = categoryNames,
                            onOptionSelected = { chosenName ->
                                selectedCategoryName = chosenName
                                selectedCategoryId = activeCategories.firstOrNull { it.name == chosenName }?.id.orEmpty()
                            }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Expense Date")
                        DatePickerField(
                            value = expenseDate,
                            onDateSelected = { selectedDate ->
                                expenseDate = selectedDate
                            }
                        )
                    }
                }

                // 2. Amount Details Card
                item {
                    SectionCardContainer(title = "Amount Details", icon = Icons.Outlined.AccountBalanceWallet) {
                        FormLabel(text = "Base Amount")
                        FormTextField(
                            value = baseAmount,
                            onValueChange = { baseAmount = it },
                            placeholder = "₹ 0",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Tax (100%)")
                        FormTextField(
                            value = taxAmount,
                            onValueChange = { taxAmount = it },
                            placeholder = "₹ 0",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Total Amount")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(primary_light)
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 1.4f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%.2f", grandTotal)}",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Bill / Receipt")
                        ImageUploadSection(
                            selectedImages = attachedFiles.toList(),
                            onBrowseClick = { filePickerLauncher.launch("*/*") },
                            onRemoveImage = { uri -> attachedFiles.remove(uri) },
                            maxFiles = 5,
                            title = "Receipt Attachment",
                            subtitle = "Attach bills or receipts for this expense",
                            supportedFormatsText = "Only support .jpg, .png, and .pdf files up to 5MB"
                        )
                    }
                }

                // 3. Employee Info Card
                item {
                    SectionCardContainer(title = "Employee Info", icon = Icons.Outlined.Group) {
                        FormLabel(text = "Employee Name")
                        FormTextField(
                            value = employeeName,
                            onValueChange = {},
                            enabled = false
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Department")
                        FormTextField(
                            value = departmentName,
                            onValueChange = {},
                            enabled = false
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel(text = "Expense Owner")
                        FormTextField(
                            value = expenseOwner,
                            onValueChange = {},
                            enabled = false
                        )
                    }
                }

                // 4. Current Status Card
                item {
                    SectionCardContainer(title = "Current Status", icon = Icons.Outlined.Timeline) {
                        FormLabel(text = "Processing State")
                        FormDropdown(
                            value = currentStatus,
                            expanded = isStatusDropdownExpanded,
                            onExpandChange = { isStatusDropdownExpanded = it },
                            options = listOf("Pending", "Approved", "Rejected"),
                            onOptionSelected = { chosenStatus ->
                                currentStatus = chosenStatus
                            }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(primary_light)
                                .padding(tokens.extraPadding * 1.2f)
                        ) {
                            Text(
                                text = "This expense is approved and waiting for the finance audit.",
                                fontSize = tokens.bodySmall,
                                color = darkPurple
                            )
                        }
                    }
                }

                // 5. Approval Workflow Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timeline,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Text(
                                        text = "Approval Workflow",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .background(light_blue_border)
                                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Payment Processing next",
                                        fontSize = tokens.caption,
                                        color = darkPurple,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            FormLabel(text = "Approver Name")
                            FormTextField(
                                value = approverName,
                                onValueChange = {},
                                enabled = false
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                            FormLabel(text = "Approval Date")
                            FormTextField(
                                value = approvalDate,
                                onValueChange = {},
                                enabled = false
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                            FormLabel(text = "Internal Comments")
                            FormTextArea(
                                value = internalComments,
                                onValueChange = { internalComments = it },
                                placeholder = "Add internal notes here..."
                            )
                        }
                    }
                }

                // Bottom Action Buttons (Cancel & Submit)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.extraPadding * 0.8f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                hrViewModel.clearExpenseClaimDetail()
                                onClose()
                            },
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, sectionBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = TextSecondary
                            ),
                            modifier = Modifier
                                .height(tokens.buttonHeight)
                                .padding(end = tokens.extraPadding * 1.2f)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val files: List<File> = attachedFiles.map { uri ->
                                    uriToFile(context, uri)
                                }

                                hrViewModel.submitExpenseClaim(
                                    claimId = claimId,
                                    title = title,
                                    description = description,
                                    amount = baseAmount,
                                    taxAmount = taxAmount,
                                    currency = "INR",
                                    status = currentStatus,
                                    internalComments = internalComments,
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
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(tokens.buttonHeight)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Submit",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(tokens.screenPadding)) }
            }
        }
    }
}

@Composable
private fun SectionCardContainer(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
            content()
        }
    }
}

private fun formatClaimEditDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(raw)
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date!!)
    } catch (_: Exception) {
        raw.take(10)
    }
}