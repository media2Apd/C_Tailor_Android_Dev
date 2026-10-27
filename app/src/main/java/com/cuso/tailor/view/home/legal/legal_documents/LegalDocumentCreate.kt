@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_documents

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.legal.LegalComplianceDto
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateUpdateLegalDocumentScreen(
    onClose: () -> Unit,
    documentIdToEdit: String? = null,
    viewModel: LegalViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !documentIdToEdit.isNullOrBlank()

    // ── 1. API Data Fetching ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        when (val state = branchState) {
            is BranchUiState.Success -> state.branches
            else -> emptyList()
        }
    }
    val memberList by hrViewModel.members.collectAsStateWithLifecycle()
    val contractList by viewModel.contracts.collectAsStateWithLifecycle()
    val complianceList by viewModel.compliances.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(page = 1, limit = 100)
        viewModel.fetchContracts()
        viewModel.fetchCompliances()
    }

    // ── 2. View One Detail Fetch on Edit Mode ──
    val documentDetail by viewModel.legalDocumentDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingLegalDocumentDetail.collectAsStateWithLifecycle()

    LaunchedEffect(documentIdToEdit) {
        if (!documentIdToEdit.isNullOrBlank()) {
            viewModel.fetchLegalDocumentDetail(documentIdToEdit)
        } else {
            viewModel.clearLegalDocumentDetail()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalDocumentDetail()
        }
    }

    // ── 3. Static Dropdown Options (From React Code) ──
    val documentTypeOptions = remember {
        listOf(
            "Contract", "Agreement", "NDA", "Policy", "License",
            "Certificate", "Notice", "Power of Attorney", "Board Resolution", "Other"
        )
    }
    val categoryOptions = remember {
        listOf(
            "Corporate", "Commercial", "Employment", "Tax",
            "Regulatory", "Intellectual Property", "Litigation", "Property"
        )
    }
    val countryOptions = remember {
        listOf("India", "United States", "United Kingdom", "United Arab Emirates", "Singapore", "Germany", "Australia")
    }
    val confidentialityOptions = remember {
        listOf("Public", "Internal", "Confidential", "Strictly Confidential")
    }
    val accessLevelOptions = remember {
        listOf("Public", "Internal", "Restricted", "Read Only", "Read/Write")
    }
    val riskLevelOptions = remember {
        listOf("Low", "Medium", "High")
    }
    val legalEntityOptions = remember {
        listOf("Enterprise Legal Holding Inc.", "Global Operations LLC", "Nexus Technologies Ltd", "Cuso International Corp")
    }

    // ── 4. Form State: Section 1 (Document Information) ──
    var documentName by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("") }
    var isDocumentTypeExpanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var documentNumber by remember { mutableStateOf("") }
    var version by remember { mutableStateOf(if (isEditMode) "" else "v1") }

    // ── 5. Form State: Section 2 (Related Record) ──
    var selectedContractId by remember { mutableStateOf("") }
    var isContractExpanded by remember { mutableStateOf(false) }
    var selectedComplianceId by remember { mutableStateOf("") }
    var isComplianceExpanded by remember { mutableStateOf(false) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var isEmployeeExpanded by remember { mutableStateOf(false) }

    // ── 6. Form State: Section 3 (Validity) ──
    var issueDate by remember { mutableStateOf("") }
    var effectiveDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }

    // ── 7. Form State: Section 4 (Classification & Security) ──
    var countryName by remember { mutableStateOf("") }
    var isCountryExpanded by remember { mutableStateOf(false) }
    var selectedBranchId by remember { mutableStateOf("") }
    var isBranchExpanded by remember { mutableStateOf(false) }
    var legalEntity by remember { mutableStateOf("") }
    var isLegalEntityExpanded by remember { mutableStateOf(false) }
    var confidentiality by remember { mutableStateOf("") }
    var isConfidentialityExpanded by remember { mutableStateOf(false) }
    var accessLevel by remember { mutableStateOf("") }
    var isAccessLevelExpanded by remember { mutableStateOf(false) }
    var riskLevel by remember { mutableStateOf("") }
    var isRiskLevelExpanded by remember { mutableStateOf(false) }

    // ── 8. Form State: Section 5 (File & Description) ──
    val mainDocumentFiles = remember { mutableStateListOf<Uri>() }
    val supportingDocumentFiles = remember { mutableStateListOf<Uri>() }
    var versionDescription by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Helper to format ISO date string to YYYY-MM-DD
    fun toSimpleDate(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val clean = raw.take(10).replace("/", "-")
        val parts = clean.split("-")
        return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            clean
        }
    }

    // Helper to format Contract label
    fun getContractLabel(contract: LegalContractDto): String {
        return listOfNotNull(contract.contractNumber, contract.contractName).filter { it.isNotBlank() }.joinToString(" - ")
    }

    // Helper to format Compliance label
    fun getComplianceLabel(compliance: LegalComplianceDto): String {
        return listOfNotNull(compliance.complianceName).filter { it.isNotBlank() }.joinToString(" - ")
    }

    // ── 9. Prefill on View One API Response ──
    LaunchedEffect(documentDetail) {
        val detail = documentDetail
        if (isEditMode && detail != null && detail.id == documentIdToEdit) {
            // Section 1
            documentName = detail.documentName.orEmpty()
            documentType = detail.documentType.orEmpty()
            category = detail.category.orEmpty()
            documentNumber = detail.documentNumber.orEmpty()

            val currentVer = detail.versions?.find { it.isCurrentVersion == true } ?: detail.versions?.lastOrNull()
            version = currentVer?.version ?: detail.version ?: "v1"

            // Section 2: Related Records
            val links = detail.relatedRecords ?: emptyList()
            val contractRecord = links.find { it.recordType.equals("Contract", ignoreCase = true) }
            selectedContractId = contractRecord?.recordId.orEmpty()

            val complianceRecord = links.find { it.recordType.equals("Compliance", ignoreCase = true) }
            selectedComplianceId = complianceRecord?.recordId.orEmpty()

            val employeeRecord = links.find { it.recordType.equals("Employee", ignoreCase = true) }
            selectedEmployeeId = employeeRecord?.recordId.orEmpty()

            // Section 3: Validity
            issueDate = toSimpleDate(detail.issueDate)
            effectiveDate = toSimpleDate(detail.effectiveDate)
            expiryDate = toSimpleDate(detail.expiryDate)

            // Section 4: Classification & Security
            countryName = detail.country.orEmpty()
            selectedBranchId = detail.branchId?.id.orEmpty()
            legalEntity = detail.legalEntity.orEmpty()
            confidentiality = detail.confidentiality.orEmpty()
            accessLevel = detail.accessLevel.orEmpty()
            riskLevel = detail.riskLevel.orEmpty()

            // Section 5: Description
            description = detail.description.orEmpty()
        }
    }

    // ── 10. Dropdown Display Names ──
    val selectedBranchName = branchList.find { it.id == selectedBranchId }?.name.orEmpty().ifBlank { "Select Branch..." }
    val selectedEmployeeName = memberList.find { it._id == selectedEmployeeId }?.displayName().orEmpty().ifBlank { "Select Employee..." }
    val selectedContractName = contractList.find { it.id == selectedContractId }?.let { getContractLabel(it) }.orEmpty().ifBlank { "Select Contract..." }
    val selectedComplianceName = complianceList.find { it.id == selectedComplianceId }?.let { getComplianceLabel(it) }.orEmpty().ifBlank { "Select Compliance..." }

    // ── 11. File Pickers ──
    val mainDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            mainDocumentFiles.clear()
            mainDocumentFiles.add(uri)
        }
    }

    val supportingDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (supportingDocumentFiles.size < 4 && !supportingDocumentFiles.contains(uri)) {
                supportingDocumentFiles.add(uri)
            }
        }
    }

    // Helper to validate date sequence
    fun parseDate(d: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(toSimpleDate(d))?.time
        } catch (_: Exception) {
            null
        }
    }

    // ── 12. Form Submission with React Validation ──
    fun handleSubmit() {
        if (documentName.isBlank()) {
            Toast.makeText(context, "Document name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (documentType.isBlank()) {
            Toast.makeText(context, "Document type is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (category.isBlank()) {
            Toast.makeText(context, "Category is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (effectiveDate.isBlank()) {
            Toast.makeText(context, "Effective date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (countryName.isBlank()) {
            Toast.makeText(context, "Country is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (confidentiality.isBlank()) {
            Toast.makeText(context, "Confidentiality is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (accessLevel.isBlank()) {
            Toast.makeText(context, "Access level is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (riskLevel.isBlank()) {
            Toast.makeText(context, "Risk level is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isEditMode && mainDocumentFiles.isEmpty()) {
            Toast.makeText(context, "Upload the main document", Toast.LENGTH_SHORT).show()
            return
        }
        if (mainDocumentFiles.size + supportingDocumentFiles.size > 5) {
            Toast.makeText(context, "You can upload up to 5 files in total", Toast.LENGTH_SHORT).show()
            return
        }

        // Date validity checks
        val effTime = parseDate(effectiveDate)
        val expTime = parseDate(expiryDate)
        val issTime = parseDate(issueDate)

        if (expTime != null && effTime != null && expTime < effTime) {
            Toast.makeText(context, "Expiry date can't be before the effective date", Toast.LENGTH_SHORT).show()
            return
        }
        if (issTime != null && effTime != null && effTime < issTime) {
            Toast.makeText(context, "Effective date can't be before the issue date", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        val fields = mutableMapOf<String, RequestBody>()
        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        // Section 1: Document Information
        addField("documentName", documentName)
        addField("documentType", documentType)
        addField("category", category)

        // Section 2: Related Records
        val relatedRecordsList = mutableListOf<Map<String, String>>()
        if (selectedContractId.isNotBlank()) {
            relatedRecordsList.add(
                mapOf(
                    "recordType" to "Contract",
                    "recordId" to selectedContractId,
                    "recordName" to selectedContractName
                )
            )
        }
        if (selectedComplianceId.isNotBlank()) {
            relatedRecordsList.add(
                mapOf(
                    "recordType" to "Compliance",
                    "recordId" to selectedComplianceId,
                    "recordName" to selectedComplianceName
                )
            )
        }
        if (selectedEmployeeId.isNotBlank()) {
            relatedRecordsList.add(
                mapOf(
                    "recordType" to "Employee",
                    "recordId" to selectedEmployeeId,
                    "recordName" to selectedEmployeeName
                )
            )
        }
        if (relatedRecordsList.isNotEmpty()) {
            addField("relatedRecords", Gson().toJson(relatedRecordsList))
        }

        // Section 3: Validity
        addField("issueDate", toSimpleDate(issueDate))
        addField("effectiveDate", toSimpleDate(effectiveDate))
        addField("expiryDate", toSimpleDate(expiryDate))

        // Section 4: Classification & Security
        addField("countryName", countryName)
        addField("branchId", selectedBranchId)
        addField("legalEntity", legalEntity)
        addField("confidentiality", confidentiality)
        addField("accessLevel", accessLevel)
        addField("riskLevel", riskLevel)

        // Section 5: Description
        addField("description", description)
        if (versionDescription.isNotBlank()) {
            addField("versionDescription", versionDescription)
        }

        val allFileParts = (mainDocumentFiles + supportingDocumentFiles).mapNotNull { uri ->
            uriToMultipartPart(context, uri, "files")
        }

        if (isEditMode && documentIdToEdit.isNotBlank()) {
            viewModel.updateLegalDocument(
                documentId = documentIdToEdit,
                fields = fields,
                files = allFileParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Document updated successfully", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to update document", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.createLegalDocument(
                fields = fields,
                files = allFileParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Document created successfully", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to create document", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update Documents" else "New Document",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        enabled = !isSubmitting
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) "Save Changes" else "Save Application",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
            ) {
                // ── Section 1: Document Information ──
                DocumentSectionContainer(stepNumber = "1", title = "Document Information") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "DOCUMENT NAME", isRequired = true)
                        FormTextField(
                            value = documentName,
                            onValueChange = { documentName = it },
                            placeholder = "Enter document name"
                        )
                    }

                    FormDropdown(
                        label = "DOCUMENT TYPE",
                        isRequired = true,
                        value = documentType,
                        expanded = isDocumentTypeExpanded,
                        onExpandChange = { isDocumentTypeExpanded = it },
                        options = documentTypeOptions,
                        onOptionSelected = { documentType = it }
                    )

                    FormDropdown(
                        label = "CATEGORY",
                        isRequired = true,
                        value = category,
                        expanded = isCategoryExpanded,
                        onExpandChange = { isCategoryExpanded = it },
                        options = categoryOptions,
                        onOptionSelected = { category = it }
                    )

                    // Disabled: Document Number is auto-generated
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "DOCUMENT NUMBER")
                        FormTextField(
                            value = documentNumber,
                            onValueChange = { },
                            placeholder = "Auto-generated on save",
                            enabled = false
                        )
                    }

                    // Disabled: Version is managed by file uploads
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "VERSION")
                        FormTextField(
                            value = version,
                            onValueChange = { },
                            placeholder = "v1",
                            enabled = false
                        )
                    }
                }

                // ── Section 2: Related Record (Connected to APIs) ──
                DocumentSectionContainer(stepNumber = "2", title = "Related Record") {
                    // Contract Dropdown (Fetched from Contracts API)
                    FormDropdown(
                        label = "RELATED CONTRACT",
                        value = selectedContractName,
                        expanded = isContractExpanded,
                        onExpandChange = { isContractExpanded = it },
                        options = contractList.map { getContractLabel(it) },
                        onOptionSelected = { label ->
                            selectedContractId = contractList.find { getContractLabel(it) == label }?.id.orEmpty()
                        }
                    )

                    // Compliance Dropdown (Fetched from Compliances API)
                    FormDropdown(
                        label = "RELATED COMPLIANCE",
                        value = selectedComplianceName,
                        expanded = isComplianceExpanded,
                        onExpandChange = { isComplianceExpanded = it },
                        options = complianceList.map { getComplianceLabel(it) },
                        onOptionSelected = { label ->
                            selectedComplianceId = complianceList.find { getComplianceLabel(it) == label }?.id.orEmpty()
                        }
                    )

                    // Employee Dropdown (Fetched from HR Members API)
                    FormDropdown(
                        label = "RELATED EMPLOYEE",
                        value = selectedEmployeeName,
                        expanded = isEmployeeExpanded,
                        onExpandChange = { isEmployeeExpanded = it },
                        options = memberList.map { it.displayName() },
                        onOptionSelected = { name ->
                            selectedEmployeeId = memberList.find { it.displayName() == name }?._id.orEmpty()
                        }
                    )
                }

                // ── Section 3: Validity ──
                DocumentSectionContainer(stepNumber = "3", title = "Validity") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "ISSUE DATE")
                        DatePickerField(
                            value = issueDate,
                            onDateSelected = { issueDate = it }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "EFFECTIVE DATE", isRequired = true)
                        DatePickerField(
                            value = effectiveDate,
                            onDateSelected = { effectiveDate = it }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "EXPIRY DATE")
                        DatePickerField(
                            value = expiryDate,
                            onDateSelected = { expiryDate = it }
                        )
                    }
                }

                // ── Section 4: Classification & Security ──
                DocumentSectionContainer(stepNumber = "4", title = "Classification & Security") {
                    FormDropdown(
                        label = "COUNTRY",
                        isRequired = true,
                        value = countryName,
                        expanded = isCountryExpanded,
                        onExpandChange = { isCountryExpanded = it },
                        options = countryOptions,
                        onOptionSelected = { countryName = it }
                    )

                    // Branch Dropdown (Fetched from Branches API)
                    FormDropdown(
                        label = "BRANCH",
                        value = selectedBranchName,
                        expanded = isBranchExpanded,
                        onExpandChange = { isBranchExpanded = it },
                        options = branchList.map { it.name.orEmpty() },
                        onOptionSelected = { name ->
                            selectedBranchId = branchList.find { it.name == name }?.id.orEmpty()
                        }
                    )

                    FormDropdown(
                        label = "LEGAL ENTITY",
                        value = legalEntity,
                        expanded = isLegalEntityExpanded,
                        onExpandChange = { isLegalEntityExpanded = it },
                        options = legalEntityOptions,
                        onOptionSelected = { legalEntity = it }
                    )

                    FormDropdown(
                        label = "CONFIDENTIALITY",
                        isRequired = true,
                        value = confidentiality,
                        expanded = isConfidentialityExpanded,
                        onExpandChange = { isConfidentialityExpanded = it },
                        options = confidentialityOptions,
                        onOptionSelected = { confidentiality = it }
                    )

                    FormDropdown(
                        label = "ACCESS LEVEL",
                        isRequired = true,
                        value = accessLevel,
                        expanded = isAccessLevelExpanded,
                        onExpandChange = { isAccessLevelExpanded = it },
                        options = accessLevelOptions,
                        onOptionSelected = { accessLevel = it }
                    )

                    FormDropdown(
                        label = "RISK LEVEL",
                        isRequired = true,
                        value = riskLevel,
                        expanded = isRiskLevelExpanded,
                        onExpandChange = { isRiskLevelExpanded = it },
                        options = riskLevelOptions,
                        onOptionSelected = { riskLevel = it }
                    )
                }

                // ── Section 5: File & Description ──
                DocumentSectionContainer(stepNumber = "5", title = "File & Description") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "MAIN DOCUMENT", isRequired = !isEditMode)
                        ImageUploadSection(
                            selectedImages = mainDocumentFiles,
                            onBrowseClick = { mainDocumentPicker.launch("*/*") },
                            onRemoveImage = { file -> mainDocumentFiles.remove(file) },
                            maxFiles = 1,
                            title = "Main Document",
                            subtitle = "Click to upload or drag & drop (PDF, DOCX, XLSX up to 5 MB)",
                            supportedFormatsText = "PDF, DOCX, XLSX up to 5 MB"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "SUPPORTING DOCUMENTS")
                        ImageUploadSection(
                            selectedImages = supportingDocumentFiles,
                            onBrowseClick = { supportingDocumentPicker.launch("*/*") },
                            onRemoveImage = { file -> supportingDocumentFiles.remove(file) },
                            maxFiles = 4,
                            title = "Supporting Documents",
                            subtitle = "Click to upload or drag & drop (PDF, DOCX, XLSX up to 5 MB)",
                            supportedFormatsText = "Max 4 files (PDF, DOCX, XLSX up to 5 MB)"
                        )
                    }

                    if (mainDocumentFiles.isNotEmpty() || supportingDocumentFiles.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "VERSION NOTES")
                            FormTextField(
                                value = versionDescription,
                                onValueChange = { versionDescription = it },
                                placeholder = "e.g. Initial upload, corrected appendix"
                            )
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "DESCRIPTION")
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "What this document covers"
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Reusable Numbered Section Card Container ──
@Composable
fun DocumentSectionContainer(
    stepNumber: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Header: Step number badge and section title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(primary_light, CircleShape)
                        .border(1.dp, Primary.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            HorizontalDivider(color = light_grey, thickness = 1.dp)

            // Section Fields
            content()
        }
    }
}