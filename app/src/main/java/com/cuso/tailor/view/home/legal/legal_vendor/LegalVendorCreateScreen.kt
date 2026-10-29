package com.cuso.tailor.view.home.legal.legal_vendor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale

// Standard dropdown options matching the web application
private val SUPPLIER_TYPES = listOf(
    "Raw Material", "Manufacturing", "Logistics", "Services",
    "IT / Software", "Packaging", "Distributor", "Other"
)

private val COUNTRIES = listOf(
    "India", "United States", "United Kingdom", "United Arab Emirates",
    "Singapore", "Germany", "Australia"
)

private val AGREEMENT_TYPES = listOf(
    "Master Service Agreement", "Supply Agreement", "Purchase Agreement",
    "NDA", "Service Level Agreement", "Framework Agreement", "Other"
)

private val DEFAULT_PAYMENT_TERMS = listOf(
    "Advance", "On Delivery", "Net 15", "Net 30", "Net 45", "Net 60", "Due on Receipt"
)

private val NOTICE_PERIOD_OPTIONS = listOf(
    "None", "15 days", "30 days", "45 days", "60 days", "90 days"
)

private val RISK_LEVELS = listOf("Low", "Medium", "High")
private val PRIORITIES = listOf("Low", "Medium", "High", "Critical")

@Composable
fun CreateEditLegalVendorScreen(
    idToEdit: String? = null,
    viewModel: LegalViewModel,
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    inventoryViewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val isEditMode = !idToEdit.isNullOrBlank()

    // Section 1: Vendor State
    var vendorName by remember { mutableStateOf("") }
    var selectedSupplierId by remember { mutableStateOf("") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var supplierType by remember { mutableStateOf("") }
    var supplierTypeExpanded by remember { mutableStateOf(false) }

    var vendorRecordNumber by remember { mutableStateOf("") }

    var countryName by remember { mutableStateOf("") }
    var countryExpanded by remember { mutableStateOf(false) }

    var selectedBranchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    var legalEntity by remember { mutableStateOf("") }

    // Section 2: Agreement State
    var agreementType by remember { mutableStateOf("") }
    var agreementTypeExpanded by remember { mutableStateOf(false) }

    var selectedContractReferenceId by remember { mutableStateOf("") }
    var contractReferenceExpanded by remember { mutableStateOf(false) }

    var agreementDescription by remember { mutableStateOf("") }
    var sla by remember { mutableStateOf("") }
    var serviceTerms by remember { mutableStateOf("") }

    var paymentTerms by remember { mutableStateOf("") }
    var paymentTermsExpanded by remember { mutableStateOf(false) }

    // Section 3: Validity State (Dates in YYYY-MM-DD format)
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var renewalDate by remember { mutableStateOf("") }
    var renewalNoticePeriodDays by remember { mutableIntStateOf(30) }
    var noticePeriodExpanded by remember { mutableStateOf(false) }

    // Section 4: Ownership & Risk State
    var selectedProcurementOwnerId by remember { mutableStateOf("") }
    var procurementOwnerExpanded by remember { mutableStateOf(false) }

    var selectedLegalOwnerId by remember { mutableStateOf("") }
    var legalOwnerExpanded by remember { mutableStateOf(false) }

    var riskLevel by remember { mutableStateOf("") }
    var riskLevelExpanded by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("") }
    var priorityExpanded by remember { mutableStateOf(false) }

    // Section 5: Documents State
    var vendorAgreementUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var slaDocumentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var supportingDocumentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Multi-File Activity Result Launchers
    val vendorAgreementLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> vendorAgreementUris = (vendorAgreementUris + uris).take(5) }

    val slaDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> slaDocumentUris = (slaDocumentUris + uris).take(5) }

    val supportingDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> supportingDocumentUris = (supportingDocumentUris + uris).take(5) }

    // Branch collection
    val branchState by branchViewModel.uiState.collectAsState()
    val branchList: List<BranchItem> = remember(branchState) {
        if (branchState is BranchUiState.Success) {
            (branchState as BranchUiState.Success).branches
        } else {
            emptyList()
        }
    }

    // Members collection from HrViewModel
    val membersList by hrViewModel.members.collectAsState()

    // Contracts collection from LegalViewModel
    val contractsList by viewModel.contracts.collectAsState()

    // Suppliers collection from InventoryViewModel
    val suppliersList by inventoryViewModel.suppliers.collectAsState()

    // Payment Terms collection
    val apiPaymentTerms by inventoryViewModel.paymentTerms.collectAsState()
    val paymentTermsList: List<String> = remember(apiPaymentTerms) {
        if (apiPaymentTerms.isNotEmpty()) {
            apiPaymentTerms.map { it.name }.distinct()
        } else {
            DEFAULT_PAYMENT_TERMS
        }
    }

    val vendorDetail by viewModel.vendorDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingVendorDetail.collectAsState()
    val isSaving by viewModel.isSavingVendor.collectAsState()

    // Trigger API calls on initial load
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(limit = 100)
        viewModel.fetchContracts(limit = 100)
        inventoryViewModel.fetchSuppliers(limit = 100)
        inventoryViewModel.fetchPaymentTerms()
    }

    // Load Vendor details if editing
    LaunchedEffect(idToEdit) {
        if (isEditMode) {
            viewModel.fetchLegalVendorDetail(idToEdit)
        }
    }

    // Prefill form states when detail arrives from the API
    LaunchedEffect(vendorDetail) {
        vendorDetail?.let { detail ->
            if (isEditMode) {
                vendorName = detail.vendor
                supplierType = detail.supplierType
                vendorRecordNumber = detail.legalVendorNumber
                countryName = detail.displayCountry
                selectedBranchId = detail.branchId?.id.orEmpty()
                legalEntity = detail.legalEntity

                agreementType = detail.agreementType
                selectedContractReferenceId = detail.contractReference?.id.orEmpty()
                agreementDescription = detail.agreementDescription
                sla = detail.sla
                serviceTerms = detail.serviceTerms
                paymentTerms = detail.paymentTerms

                startDate = formatToDateInput(detail.startDate)
                endDate = formatToDateInput(detail.endDate)
                renewalDate = formatToDateInput(detail.renewalDate)
                renewalNoticePeriodDays = detail.renewalNoticePeriod

                selectedProcurementOwnerId = detail.procurementOwner?.id.orEmpty()
                selectedLegalOwnerId = detail.legalOwner?.id.orEmpty()
                riskLevel = detail.riskLevel
                priority = detail.priority
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalVendorDetail()
        }
    }

    fun toTextPart(value: String): RequestBody {
        return value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update Record" else "Create Records",
                onClose = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = whiteBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            // Build the exact form-data map matching the payload shown in the screenshots
                            val fieldsMap = mutableMapOf<String, RequestBody>()

                            if (vendorName.isNotBlank()) fieldsMap["vendor"] = toTextPart(vendorName)
                            if (supplierType.isNotBlank()) fieldsMap["supplierType"] = toTextPart(supplierType)
                            if (countryName.isNotBlank()) fieldsMap["countryName"] = toTextPart(countryName)
                            if (selectedBranchId.isNotBlank()) fieldsMap["branchId"] = toTextPart(selectedBranchId)
                            if (legalEntity.isNotBlank()) fieldsMap["legalEntity"] = toTextPart(legalEntity)

                            if (agreementType.isNotBlank()) fieldsMap["agreementType"] = toTextPart(agreementType)
                            if (selectedContractReferenceId.isNotBlank()) {
                                fieldsMap["contractReference"] = toTextPart(selectedContractReferenceId)
                            }
                            if (agreementDescription.isNotBlank()) fieldsMap["agreementDescription"] = toTextPart(agreementDescription)
                            if (sla.isNotBlank()) fieldsMap["sla"] = toTextPart(sla)
                            if (serviceTerms.isNotBlank()) fieldsMap["serviceTerms"] = toTextPart(serviceTerms)
                            if (paymentTerms.isNotBlank()) fieldsMap["paymentTerms"] = toTextPart(paymentTerms)

                            if (startDate.isNotBlank()) fieldsMap["startDate"] = toTextPart(formatToDateInput(startDate))
                            if (endDate.isNotBlank()) fieldsMap["endDate"] = toTextPart(formatToDateInput(endDate))
                            if (renewalDate.isNotBlank()) fieldsMap["renewalDate"] = toTextPart(formatToDateInput(renewalDate))
                            fieldsMap["renewalNoticePeriod"] = toTextPart(renewalNoticePeriodDays.toString())

                            if (selectedProcurementOwnerId.isNotBlank()) {
                                fieldsMap["procurementOwner"] = toTextPart(selectedProcurementOwnerId)
                            }
                            if (selectedLegalOwnerId.isNotBlank()) {
                                fieldsMap["legalOwner"] = toTextPart(selectedLegalOwnerId)
                            }

                            if (riskLevel.isNotBlank()) fieldsMap["riskLevel"] = toTextPart(riskLevel)
                            if (priority.isNotBlank()) fieldsMap["priority"] = toTextPart(priority)

                            // Prepare binary multi-part files
                            val vendorAgreementParts: List<MultipartBody.Part> = vendorAgreementUris.mapNotNull {
                                uriToMultipartPart(context, it, "vendorAgreement")
                            }
                            val slaDocumentParts: List<MultipartBody.Part> = slaDocumentUris.mapNotNull {
                                uriToMultipartPart(context, it, "slaDocuments")
                            }
                            val supportingDocumentParts: List<MultipartBody.Part> = supportingDocumentUris.mapNotNull {
                                uriToMultipartPart(context, it, "supportingDocuments")
                            }

                            if (isEditMode) {
                                viewModel.updateLegalVendor(
                                    vendorId = idToEdit,
                                    fields = fieldsMap,
                                    vendorAgreementFiles = vendorAgreementParts,
                                    slaFiles = slaDocumentParts,
                                    supportingFiles = supportingDocumentParts,
                                    onSuccess = { onNavigateBack() },
                                    onError = {}
                                )
                            } else {
                                viewModel.createLegalVendor(
                                    fields = fieldsMap,
                                    vendorAgreementFiles = vendorAgreementParts,
                                    slaFiles = slaDocumentParts,
                                    supportingFiles = supportingDocumentParts,
                                    onSuccess = { onNavigateBack() },
                                    onError = {}
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                modifier = Modifier.size(tokens.iconSize),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(tokens.iconSize),
                                tint = whiteBg
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) "Save Changes" else "Save Record",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isEditMode && isLoadingDetail) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(innerPadding)
                    .padding(horizontal = tokens.screenPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 1: Vendor Information ──
                FormSectionCard(number = 1, title = "Vendor") {
                    // Suppliers List Dropdown (Auto-fills Vendor Name)
                    val supplierOptions = suppliersList.map { it.name }.filter { it.isNotBlank() }
                    FormDropdown(
                        label = "Select Registered Supplier",
                        value = suppliersList.find { it.id == selectedSupplierId }?.name.orEmpty(),
                        expanded = supplierDropdownExpanded,
                        onExpandChange = { supplierDropdownExpanded = it },
                        options = supplierOptions,
                        onOptionSelected = { chosenSupplierName ->
                            val foundSupplier = suppliersList.find { it.name == chosenSupplierName }
                            if (foundSupplier != null) {
                                selectedSupplierId = foundSupplier.id
                                vendorName = foundSupplier.name
                            }
                        },
                        isRequired = false
                    )

                    FormLabel(text = "Vendor Name", isRequired = true)
                    FormTextField(
                        value = vendorName,
                        onValueChange = { vendorName = it },
                        placeholder = "Enter vendor name"
                    )

                    FormDropdown(
                        label = "Supplier Type",
                        value = supplierType,
                        expanded = supplierTypeExpanded,
                        onExpandChange = { supplierTypeExpanded = it },
                        options = SUPPLIER_TYPES,
                        onOptionSelected = { supplierType = it },
                        isRequired = true
                    )

                    FormLabel(text = "Vendor ID", isRequired = false)
                    FormTextField(
                        value = vendorRecordNumber,
                        onValueChange = {},
                        placeholder = "Auto-generated on save",
                        enabled = false
                    )

                    FormDropdown(
                        label = "Country",
                        value = countryName,
                        expanded = countryExpanded,
                        onExpandChange = { countryExpanded = it },
                        options = COUNTRIES,
                        onOptionSelected = { countryName = it },
                        isRequired = true
                    )

                    // Branch Options (Stores branchId)
                    val branchOptions = branchList.mapNotNull { it.name?.takeIf { name -> name.isNotBlank() } }
                    val selectedBranchName = branchList.find { it.id == selectedBranchId }?.name.orEmpty()
                    FormDropdown(
                        label = "Branch",
                        value = selectedBranchName,
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchOptions,
                        onOptionSelected = { chosenName ->
                            selectedBranchId = branchList.find { it.name == chosenName }?.id.orEmpty()
                        },
                        isRequired = false
                    )

                    FormLabel(text = "Legal Entity", isRequired = true)
                    FormTextField(
                        value = legalEntity,
                        onValueChange = { legalEntity = it },
                        placeholder = "Enter legal entity name"
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 2: Agreement Information ──
                FormSectionCard(number = 2, title = "Agreement") {
                    FormDropdown(
                        label = "Agreement Type",
                        value = agreementType,
                        expanded = agreementTypeExpanded,
                        onExpandChange = { agreementTypeExpanded = it },
                        options = AGREEMENT_TYPES,
                        onOptionSelected = { agreementType = it },
                        isRequired = true
                    )

                    // Contract References (Stores contractReference ID)
                    val contractOptions = contractsList.map { "${it.contractNumber} - ${it.contractName}".trim() }
                    val selectedContractName = contractsList.find { it.id == selectedContractReferenceId }?.let {
                        "${it.contractNumber} - ${it.contractName}".trim()
                    }.orEmpty()

                    FormDropdown(
                        label = "Contract Reference",
                        value = selectedContractName,
                        expanded = contractReferenceExpanded,
                        onExpandChange = { contractReferenceExpanded = it },
                        options = contractOptions,
                        onOptionSelected = { chosen ->
                            selectedContractReferenceId = contractsList.find {
                                "${it.contractNumber} - ${it.contractName}".trim() == chosen
                            }?.id.orEmpty()
                        },
                        isRequired = false
                    )

                    FormLabel(text = "Agreement Description", isRequired = false)
                    FormTextArea(
                        value = agreementDescription,
                        onValueChange = { agreementDescription = it },
                        placeholder = "What this agreement covers"
                    )

                    FormLabel(text = "SLA", isRequired = false)
                    FormTextField(
                        value = sla,
                        onValueChange = { sla = it },
                        placeholder = "e.g. 99.9% uptime, 4hr response"
                    )

                    FormLabel(text = "Service Terms", isRequired = false)
                    FormTextArea(
                        value = serviceTerms,
                        onValueChange = { serviceTerms = it },
                        placeholder = "Service terms agreed with the vendor"
                    )

                    // Payment Terms
                    FormDropdown(
                        label = "Payment Terms",
                        value = paymentTerms,
                        expanded = paymentTermsExpanded,
                        onExpandChange = { paymentTermsExpanded = it },
                        options = paymentTermsList,
                        onOptionSelected = { paymentTerms = it },
                        isRequired = false
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 3: Validity Dates ──
                FormSectionCard(number = 3, title = "Validity") {
                    FormLabel(text = "Start Date", isRequired = true)
                    DatePickerField(
                        value = startDate,
                        onDateSelected = { startDate = it }
                    )

                    FormLabel(text = "End Date", isRequired = true)
                    DatePickerField(
                        value = endDate,
                        onDateSelected = { endDate = it }
                    )

                    FormLabel(text = "Renewal Date", isRequired = false)
                    DatePickerField(
                        value = renewalDate,
                        onDateSelected = { renewalDate = it }
                    )

                    val selectedNoticePeriodLabel = if (renewalNoticePeriodDays == 0) "None" else "$renewalNoticePeriodDays days"
                    FormDropdown(
                        label = "Renewal Notice Period",
                        value = selectedNoticePeriodLabel,
                        expanded = noticePeriodExpanded,
                        onExpandChange = { noticePeriodExpanded = it },
                        options = NOTICE_PERIOD_OPTIONS,
                        onOptionSelected = { chosen ->
                            renewalNoticePeriodDays = when (chosen) {
                                "15 days" -> 15
                                "30 days" -> 30
                                "45 days" -> 45
                                "60 days" -> 60
                                "90 days" -> 90
                                else -> 0
                            }
                        },
                        isRequired = false
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 4: Ownership & Risk ──
                FormSectionCard(number = 4, title = "Ownership & Risk") {
                    // Members from HrViewModel for Procurement Owner (Stores procurementOwner ID)
                    val memberOptions = membersList.map { "${it.firstName} ${it.lastName}".trim() }

                    val selectedProcurementOwnerName = membersList.find { it._id == selectedProcurementOwnerId }?.let {
                        "${it.firstName} ${it.lastName}".trim()
                    }.orEmpty()

                    FormDropdown(
                        label = "Procurement Owner",
                        value = selectedProcurementOwnerName,
                        expanded = procurementOwnerExpanded,
                        onExpandChange = { procurementOwnerExpanded = it },
                        options = memberOptions,
                        onOptionSelected = { chosenName ->
                            selectedProcurementOwnerId = membersList.find {
                                "${it.firstName} ${it.lastName}".trim() == chosenName
                            }?._id.orEmpty()
                        },
                        isRequired = true
                    )

                    // Members from HrViewModel for Legal Owner (Stores legalOwner ID)
                    val selectedLegalOwnerName = membersList.find { it._id == selectedLegalOwnerId }?.let {
                        "${it.firstName} ${it.lastName}".trim()
                    }.orEmpty()

                    FormDropdown(
                        label = "Legal Owner",
                        value = selectedLegalOwnerName,
                        expanded = legalOwnerExpanded,
                        onExpandChange = { legalOwnerExpanded = it },
                        options = memberOptions,
                        onOptionSelected = { chosenName ->
                            selectedLegalOwnerId = membersList.find {
                                "${it.firstName} ${it.lastName}".trim() == chosenName
                            }?._id.orEmpty()
                        },
                        isRequired = true
                    )

                    FormDropdown(
                        label = "Risk Level",
                        value = riskLevel,
                        expanded = riskLevelExpanded,
                        onExpandChange = { riskLevelExpanded = it },
                        options = RISK_LEVELS,
                        onOptionSelected = { riskLevel = it },
                        isRequired = true
                    )

                    FormDropdown(
                        label = "Priority",
                        value = priority,
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = PRIORITIES,
                        onOptionSelected = { priority = it },
                        isRequired = true
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 5: Documents ──
                FormSectionCard(number = 5, title = "Documents") {
                    FormLabel(text = "Vendor Agreement", isRequired = !isEditMode)
                    ImageUploadSection(
                        selectedImages = vendorAgreementUris.map { it.toString() },
                        onBrowseClick = { vendorAgreementLauncher.launch("*/*") },
                        onRemoveImage = { uriStr ->
                            vendorAgreementUris = vendorAgreementUris.filter { it.toString() != uriStr }
                        },
                        maxFiles = 5,
                        title = "Click to upload vendor agreement",
                        subtitle = "PDF, DOCX, JPG, PNG up to 5 MB",
                        supportedFormatsText = "PDF, DOCX, JPG, PNG"
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    FormLabel(text = "SLA Documents", isRequired = false)
                    ImageUploadSection(
                        selectedImages = slaDocumentUris.map { it.toString() },
                        onBrowseClick = { slaDocumentLauncher.launch("*/*") },
                        onRemoveImage = { uriStr ->
                            slaDocumentUris = slaDocumentUris.filter { it.toString() != uriStr }
                        },
                        maxFiles = 5,
                        title = "Click to upload SLA documents",
                        subtitle = "PDF, DOCX, JPG, PNG up to 5 MB",
                        supportedFormatsText = "PDF, DOCX, JPG, PNG"
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    FormLabel(text = "Supporting Documents", isRequired = false)
                    ImageUploadSection(
                        selectedImages = supportingDocumentUris.map { it.toString() },
                        onBrowseClick = { supportingDocumentLauncher.launch("*/*") },
                        onRemoveImage = { uriStr ->
                            supportingDocumentUris = supportingDocumentUris.filter { it.toString() != uriStr }
                        },
                        maxFiles = 5,
                        title = "Click to upload supporting documents",
                        subtitle = "PDF, DOCX, JPG, PNG up to 5 MB",
                        supportedFormatsText = "PDF, DOCX, JPG, PNG"
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))
            }
        }
    }
}

// Helper to normalize any input date string to YYYY-MM-DD
private fun formatToDateInput(rawDate: String): String {
    if (rawDate.isBlank()) return ""
    val trimmed = rawDate.take(10)
    return try {
        if (trimmed.contains("-")) {
            val parts = trimmed.split("-")
            if (parts.size == 3 && parts[0].length == 4) {
                trimmed // Already YYYY-MM-DD
            } else if (parts.size == 3 && parts[2].length == 4) {
                "${parts[2]}-${parts[1]}-${parts[0]}" // Convert DD-MM-YYYY to YYYY-MM-DD
            } else {
                trimmed
            }
        } else {
            trimmed
        }
    } catch (_: Exception) {
        trimmed
    }
}

@Composable
private fun FormSectionCard(
    number: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.cardPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(primary_light, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(tokens.screenPadding))

        HorizontalDivider(color = BorderGray, thickness = 1.dp)
        content()
    }
}