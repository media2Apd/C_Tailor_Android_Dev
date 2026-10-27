@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_compliance

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
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun CreateLegalComplianceScreen(
    onClose: () -> Unit,
    complianceIdToEdit: String? = null,
    viewModel: LegalViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !complianceIdToEdit.isNullOrBlank()

    // ── External Data Fetching ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        when (val state = branchState) {
            is BranchUiState.Success -> state.branches
            else -> emptyList()
        }
    }

    val memberList by hrViewModel.members.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(page = 1, limit = 100)
    }

    // ── Pre-fill on Edit Mode ──
    val complianceDetail by viewModel.complianceDetail.collectAsStateWithLifecycle()
    LaunchedEffect(complianceIdToEdit) {
        if (!complianceIdToEdit.isNullOrBlank()) {
            viewModel.fetchComplianceDetail(complianceIdToEdit)
        }
    }

    // ── Static Option Lists (from React code) ──
    val requirementTypeOptions = remember {
        listOf("Filing", "Registration", "License", "Permit", "Certification", "Audit", "Report", "Payment", "Policy", "Training")
    }
    val categoryOptions = remember {
        listOf("Tax", "Labour", "Corporate", "Environmental", "Health & Safety", "Data Privacy", "Financial", "Industry Specific")
    }
    val applicabilityOptions = remember {
        listOf("All Entities", "Specific Entity", "Specific Branch", "Specific Department", "All Employees")
    }
    val countryOptions = remember {
        listOf("India", "United States", "United Kingdom", "United Arab Emirates", "Singapore", "Germany", "Australia")
    }
    val frequencyOptions = remember {
        listOf("One Time", "Monthly", "Quarterly", "Half Yearly", "Annually", "Event Based")
    }
    val responsibleTeamOptions = remember {
        listOf("Finance", "HR Team", "Legal", "Compliance", "Operations", "IT", "Administration")
    }
    val riskOptions = remember {
        listOf("Low", "Medium", "High")
    }
    val priorityOptions = remember {
        listOf("Low", "Medium", "High", "Urgent")
    }
    val legalEntityOptions = remember {
        listOf("Nexus Boutique", "Cuso Tailor Private Limited", "Holdings Ltd")
    }

    // ── Form State: Section 1 (Compliance Information) ──
    var complianceName by remember { mutableStateOf("") }
    var requirementType by remember { mutableStateOf("") }
    var isRequirementTypeExpanded by remember { mutableStateOf(false) }
    var regulationReference by remember { mutableStateOf("") }
    var complianceCategory by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var applicability by remember { mutableStateOf("") }
    var isApplicabilityExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // ── Form State: Section 2 (Jurisdiction) ──
    var countryName by remember { mutableStateOf("") }
    var isCountryExpanded by remember { mutableStateOf(false) }
    var region by remember { mutableStateOf("") }
    var jurisdiction by remember { mutableStateOf("") }
    var legalEntity by remember { mutableStateOf("") }
    var isLegalEntityExpanded by remember { mutableStateOf(false) }
    var selectedBranchId by remember { mutableStateOf("") }
    var isBranchExpanded by remember { mutableStateOf(false) }

    // ── Form State: Section 3 (Schedule) ──
    var frequency by remember { mutableStateOf("") }
    var isFrequencyExpanded by remember { mutableStateOf(false) }
    var effectiveDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var renewalReviewDate by remember { mutableStateOf("") }

    // ── Form State: Section 4 (Ownership & Priority) ──
    var responsibleTeam by remember { mutableStateOf("") }
    var isResponsibleTeamExpanded by remember { mutableStateOf(false) }
    var selectedComplianceOwnerId by remember { mutableStateOf("") }
    var isComplianceOwnerExpanded by remember { mutableStateOf(false) }
    var riskLevel by remember { mutableStateOf("") }
    var isRiskLevelExpanded by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf("") }
    var isPriorityExpanded by remember { mutableStateOf(false) }

    // ── Form State: Section 5 (Evidence & Documents) ──
    var requiredEvidence by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val supportingDocFiles = remember { mutableStateListOf<Uri>() }
    var isSubmitting by remember { mutableStateOf(false) }

    // File picker launcher (max 5 files)
    val supportingDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (supportingDocFiles.size < 5 && !supportingDocFiles.contains(uri)) {
                supportingDocFiles.add(uri)
            }
        }
    }

    // Populate data when editing
    LaunchedEffect(complianceDetail) {
        val detail = complianceDetail
        if (isEditMode && detail != null && detail.id == complianceIdToEdit) {
            complianceName = detail.complianceName.orEmpty()
            requirementType = detail.requirementType.orEmpty()
            regulationReference = detail.regulationReference.orEmpty()
            complianceCategory = detail.complianceCategory ?: ""
            applicability = detail.applicability ?: ""
            description = detail.description.orEmpty()

            countryName = detail.country ?: ""
            region = detail.region.orEmpty()
            jurisdiction = detail.jurisdiction.orEmpty()
            legalEntity = detail.legalEntity ?: ""
            selectedBranchId = detail.branchId?.id.orEmpty()

            frequency = detail.frequency ?: ""
            effectiveDate = detail.effectiveDate?.take(10).orEmpty()
            dueDate = detail.dueDate?.take(10).orEmpty()
            renewalReviewDate = detail.renewalReviewDate?.take(10).orEmpty()

            responsibleTeam = detail.responsibleTeam ?: ""
            selectedComplianceOwnerId = detail.complianceOwner?.id.orEmpty()
            riskLevel = detail.riskLevel ?: ""
            priority = detail.priority ?: ""

            requiredEvidence = detail.requiredEvidence.orEmpty()
            notes = detail.notes.orEmpty()
        }
    }

    val selectedBranchName = branchList.find { it.id == selectedBranchId }?.name.orEmpty().ifBlank { "Select Branch..." }
    val selectedComplianceOwnerName = memberList.find { it._id == selectedComplianceOwnerId }?.displayName().orEmpty().ifBlank { "Select Compliance Owner..." }

    // Convert date string to standard yyyy-MM-dd
    fun toSimpleDate(raw: String): String {
        val clean = raw.trim().replace("/", "-")
        val parts = clean.split("-")
        return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            clean
        }
    }

    // ── Form Submission with React Validation Logic ──
    fun handleSubmit() {
        if (complianceName.isBlank()) {
            Toast.makeText(context, "Compliance name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (requirementType.isBlank()) {
            Toast.makeText(context, "Requirement type is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (regulationReference.isBlank()) {
            Toast.makeText(context, "Regulation / reference is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (complianceCategory.isBlank()) {
            Toast.makeText(context, "Category is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (applicability.isBlank()) {
            Toast.makeText(context, "Applicability is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (countryName.isBlank()) {
            Toast.makeText(context, "Country is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (frequency.isBlank()) {
            Toast.makeText(context, "Frequency is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (effectiveDate.isBlank()) {
            Toast.makeText(context, "Effective date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (dueDate.isBlank()) {
            Toast.makeText(context, "Due date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (responsibleTeam.isBlank()) {
            Toast.makeText(context, "Responsible team is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedComplianceOwnerId.isBlank()) {
            Toast.makeText(context, "Compliance owner is required", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        val fields = mutableMapOf<String, RequestBody>()
        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        // 1. Compliance Information
        addField("complianceName", complianceName)
        addField("requirementType", requirementType)
        addField("regulationReference", regulationReference)
        addField("complianceCategory", complianceCategory)
        addField("applicability", applicability)
        addField("description", description)

        // 2. Jurisdiction
        addField("countryName", countryName)
        addField("region", region)
        addField("jurisdiction", jurisdiction)
        addField("legalEntity", legalEntity)
        addField("branchId", selectedBranchId)

        // 3. Schedule
        addField("frequency", frequency)
        addField("effectiveDate", toSimpleDate(effectiveDate))
        addField("dueDate", toSimpleDate(dueDate))
        addField("renewalReviewDate", toSimpleDate(renewalReviewDate))

        // 4. Ownership & Priority
        addField("responsibleTeam", responsibleTeam)
        addField("complianceOwner", selectedComplianceOwnerId)
        addField("riskLevel", riskLevel)
        addField("priority", priority)

        // 5. Evidence & Documents
        addField("requiredEvidence", requiredEvidence)
        addField("notes", notes)

        val supportingDocParts = supportingDocFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "supportingDocuments")
        }

        if (isEditMode) {
            viewModel.updateCompliance(
                complianceId = complianceIdToEdit,
                fields = fields,
                supportingFiles = supportingDocParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Compliance updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchCompliances()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to update compliance", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.createCompliance(
                fields = fields,
                supportingFiles = supportingDocParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Compliance created successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchCompliances()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to create compliance", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Compliance" else "New Compliance",
                subtitle = "Back to Compliance Management",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
        ) {
            // ── Section 1: Compliance Information ──
            ComplianceSectionCard(stepNumber = "1", title = "Compliance Information") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "COMPLIANCE NAME", isRequired = true)
                    FormTextField(
                        value = complianceName,
                        onValueChange = { complianceName = it },
                        placeholder = "Enter compliance requirement name"
                    )
                }

                FormDropdown(
                    label = "REQUIREMENT TYPE",
                    isRequired = true,
                    value = requirementType,
                    expanded = isRequirementTypeExpanded,
                    onExpandChange = { isRequirementTypeExpanded = it },
                    options = requirementTypeOptions,
                    onOptionSelected = { requirementType = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "REGULATION / REFERENCE", isRequired = true)
                    FormTextField(
                        value = regulationReference,
                        onValueChange = { regulationReference = it },
                        placeholder = "e.g. GDPR Article 5, ISO 27001"
                    )
                }

                FormDropdown(
                    label = "CATEGORY",
                    isRequired = true,
                    value = complianceCategory,
                    expanded = isCategoryExpanded,
                    onExpandChange = { isCategoryExpanded = it },
                    options = categoryOptions,
                    onOptionSelected = { complianceCategory = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "DESCRIPTION")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe the compliance requirement in detail"
                    )
                }

                FormDropdown(
                    label = "APPLICABILITY",
                    isRequired = true,
                    value = applicability,
                    expanded = isApplicabilityExpanded,
                    onExpandChange = { isApplicabilityExpanded = it },
                    options = applicabilityOptions,
                    onOptionSelected = { applicability = it }
                )
            }

            // ── Section 2: Jurisdiction ──
            ComplianceSectionCard(stepNumber = "2", title = "Jurisdiction") {
                FormDropdown(
                    label = "COUNTRY",
                    isRequired = true,
                    value = countryName,
                    expanded = isCountryExpanded,
                    onExpandChange = { isCountryExpanded = it },
                    options = countryOptions,
                    onOptionSelected = { countryName = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "REGION")
                    FormTextField(
                        value = region,
                        onValueChange = { region = it },
                        placeholder = "Enter region or territory"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "JURISDICTION")
                    FormTextField(
                        value = jurisdiction,
                        onValueChange = { jurisdiction = it },
                        placeholder = "e.g. Federal, State, EU"
                    )
                }

                FormDropdown(
                    label = "LEGAL ENTITY",
                    value = legalEntity,
                    expanded = isLegalEntityExpanded,
                    onExpandChange = { isLegalEntityExpanded = it },
                    options = legalEntityOptions,
                    onOptionSelected = { legalEntity = it }
                )

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
            }

            // ── Section 3: Schedule ──
            ComplianceSectionCard(stepNumber = "3", title = "Schedule") {
                FormDropdown(
                    label = "FREQUENCY",
                    isRequired = true,
                    value = frequency,
                    expanded = isFrequencyExpanded,
                    onExpandChange = { isFrequencyExpanded = it },
                    options = frequencyOptions,
                    onOptionSelected = { frequency = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "EFFECTIVE DATE", isRequired = true)
                    DatePickerField(
                        value = effectiveDate,
                        onDateSelected = { effectiveDate = it }
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "DUE DATE", isRequired = true)
                    DatePickerField(
                        value = dueDate,
                        onDateSelected = { dueDate = it }
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "RENEWAL / REVIEW DATE")
                    DatePickerField(
                        value = renewalReviewDate,
                        onDateSelected = { renewalReviewDate = it }
                    )
                }
            }

            // ── Section 4: Ownership & Priority ──
            ComplianceSectionCard(stepNumber = "4", title = "Ownership & Priority") {
                FormDropdown(
                    label = "RESPONSIBLE TEAM",
                    isRequired = true,
                    value = responsibleTeam,
                    expanded = isResponsibleTeamExpanded,
                    onExpandChange = { isResponsibleTeamExpanded = it },
                    options = responsibleTeamOptions,
                    onOptionSelected = { responsibleTeam = it }
                )

                FormDropdown(
                    label = "COMPLIANCE OWNER",
                    isRequired = true,
                    value = selectedComplianceOwnerName,
                    expanded = isComplianceOwnerExpanded,
                    onExpandChange = { isComplianceOwnerExpanded = it },
                    options = memberList.map { it.displayName() },
                    onOptionSelected = { name ->
                        selectedComplianceOwnerId = memberList.find { it.displayName() == name }?._id.orEmpty()
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormDropdown(
                        modifier = Modifier.weight(1f),
                        label = "RISK LEVEL",
                        isRequired = true,
                        value = riskLevel,
                        expanded = isRiskLevelExpanded,
                        onExpandChange = { isRiskLevelExpanded = it },
                        options = riskOptions,
                        onOptionSelected = { riskLevel = it }
                    )

                    FormDropdown(
                        modifier = Modifier.weight(1f),
                        label = "PRIORITY",
                        isRequired = true,
                        value = priority,
                        expanded = isPriorityExpanded,
                        onExpandChange = { isPriorityExpanded = it },
                        options = priorityOptions,
                        onOptionSelected = { priority = it }
                    )
                }
            }

            // ── Section 5: Evidence & Documents ──
            ComplianceSectionCard(stepNumber = "5", title = "Evidence & Documents") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "REQUIRED EVIDENCE")
                    FormTextArea(
                        value = requiredEvidence,
                        onValueChange = { requiredEvidence = it },
                        placeholder = "List the evidence required to demonstrate compliance"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "NOTES")
                    FormTextArea(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Enter internal notes or instructions"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "SUPPORTING DOCUMENTS")
                    ImageUploadSection(
                        selectedImages = supportingDocFiles,
                        onBrowseClick = { supportingDocPicker.launch("*/*") },
                        onRemoveImage = { file -> supportingDocFiles.remove(file) },
                        maxFiles = 5,
                        subtitle = "PDF, DOCX, XLSX up to 50 MB",
                        supportedFormatsText = "Max 5 files (Images/Files auto-compressed)"
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Reusable Numbered Section Container ──
@Composable
fun ComplianceSectionCard(
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

            content()
        }
    }
}