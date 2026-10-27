@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_license

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.model.settings.DepartmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.DepartmentUiState
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateLegalLicenseScreen(
    onClose: () -> Unit,
    licenseIdToEdit: String? = null,
    viewModel: LegalViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !licenseIdToEdit.isNullOrBlank()

    // ── External Data Fetching ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        when (val state = branchState) {
            is BranchUiState.Success -> state.branches
            else -> emptyList()
        }
    }

    val memberList by hrViewModel.members.collectAsStateWithLifecycle()

    val departmentState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val departmentList: List<DepartmentItem> = remember(departmentState) {
        when (val state = departmentState) {
            is DepartmentUiState.Success -> state.departments
            else -> emptyList()
        }
    }

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(page = 1, limit = 100)
        departmentViewModel.loadDepartments()
    }

    // ── View One Detail Fetch on Edit Mode ──
    val licenseDetail by viewModel.licenseDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingLicenseDetail.collectAsStateWithLifecycle()

    LaunchedEffect(licenseIdToEdit) {
        if (!licenseIdToEdit.isNullOrBlank()) {
            viewModel.fetchLicenseDetail(licenseIdToEdit)
        } else {
            viewModel.clearLicenseDetail()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLicenseDetail()
        }
    }

    // ── Static Dropdown Options ──
    val licenseTypeOptions = remember {
        listOf(
            "Trade License",
            "Tax Registration",
            "Business Registration",
            "Import / Export License",
            "Professional License",
            "Industry Permit",
            "Environmental Permit",
            "Health & Safety Permit",
            "Software License",
            "Other"
        )
    }
    val countryOptions = remember {
        listOf("India", "United States", "United Kingdom", "United Arab Emirates", "Singapore", "Germany", "Australia")
    }
    val legalEntityOptions = remember {
        listOf("Nexus Boutique", "Cuso Tailor Private Limited", "Holdings Ltd")
    }
    val noticePeriodOptions = remember {
        listOf("15 days", "30 days", "45 days", "60 days", "90 days", "120 days")
    }
    val riskLevelOptions = remember {
        listOf("Low", "Medium", "High")
    }

    // ── Form State: Section 1 (License Information) ──
    var licenseType by remember { mutableStateOf("") }
    var isLicenseTypeExpanded by remember { mutableStateOf(false) }
    var licenseName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var issuingAuthority by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ── Form State: Section 2 (Location) ──
    var countryName by remember { mutableStateOf("") }
    var isCountryExpanded by remember { mutableStateOf(false) }
    var region by remember { mutableStateOf("") }
    var jurisdiction by remember { mutableStateOf("") }
    var legalEntity by remember { mutableStateOf("") }
    var isLegalEntityExpanded by remember { mutableStateOf(false) }
    var selectedBranchId by remember { mutableStateOf("") }
    var isBranchExpanded by remember { mutableStateOf(false) }

    // ── Form State: Section 3 (Validity) ──
    var issueDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var renewalDate by remember { mutableStateOf("") }
    var renewalNoticePeriod by remember { mutableStateOf("30 days") }
    var isRenewalNoticeExpanded by remember { mutableStateOf(false) }

    // ── Form State: Section 4 (Ownership) ──
    var selectedLicenseOwnerId by remember { mutableStateOf("") }
    var isLicenseOwnerExpanded by remember { mutableStateOf(false) }
    var selectedResponsibleTeamId by remember { mutableStateOf("") }
    var isResponsibleTeamExpanded by remember { mutableStateOf(false) }
    var savedStatus by remember { mutableStateOf("Active") }
    var riskLevel by remember { mutableStateOf("") }
    var isRiskLevelExpanded by remember { mutableStateOf(false) }

    // ── Form State: Section 5 (Documents & Notes) ──
    val certificateFiles = remember { mutableStateListOf<Uri>() }
    val supportingDocFiles = remember { mutableStateListOf<Uri>() }
    var notes by remember { mutableStateOf("") }
    var hasEffectiveDate by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Helper to format ISO date string to YYYY-MM-DD
    fun toSimpleDate(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val clean = raw.trim().take(10).replace("/", "-")
        val parts = clean.split("-")
        return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            clean
        }
    }

    fun parseDate(dateStr: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(toSimpleDate(dateStr))?.time
        } catch (_: Exception) {
            null
        }
    }

    // ── Prefill on View One API Response ──
    LaunchedEffect(licenseDetail) {
        val detail = licenseDetail
        if (isEditMode && detail != null && detail.id == licenseIdToEdit) {
            // Section 1: License Information
            licenseType = detail.licenseType.orEmpty()
            licenseName = detail.licenseName.orEmpty()
            licenseNumber = detail.licenseNumber.orEmpty()
            issuingAuthority = detail.issuingAuthority.orEmpty()
            description = detail.description.orEmpty()

            // Section 2: Location
            countryName = detail.countryName ?: detail.country.orEmpty()
            region = detail.region.orEmpty()
            jurisdiction = detail.jurisdiction.orEmpty()
            legalEntity = detail.legalEntity.orEmpty()
            selectedBranchId = detail.branchId?.id.orEmpty()

            // Section 3: Validity
            issueDate = toSimpleDate(detail.issueDate)
            expiryDate = toSimpleDate(detail.expiryDate)
            renewalDate = toSimpleDate(detail.renewalDate)
            renewalNoticePeriod = detail.renewalNoticePeriod?.let { "$it days" } ?: "30 days"
            hasEffectiveDate = !detail.effectiveDate.isNullOrBlank()

            // Section 4: Ownership
            selectedLicenseOwnerId = detail.licenseOwner?.id.orEmpty()
            selectedResponsibleTeamId = detail.responsibleTeam?.id.orEmpty()
            savedStatus = detail.status ?: "Active"
            riskLevel = detail.riskLevel.orEmpty()

            // Section 5: Documents & Notes
            notes = detail.notes.orEmpty()
        }
    }

    val selectedBranchName = branchList.find { it.id == selectedBranchId }?.name.orEmpty().ifBlank { "Select Branch..." }
    val selectedLicenseOwnerName = memberList.find { it._id == selectedLicenseOwnerId }?.displayName().orEmpty().ifBlank { "Select License Owner..." }
    val selectedResponsibleTeamName = departmentList.find { it._id == selectedResponsibleTeamId }?.name.orEmpty().ifBlank {
        if (licenseDetail?.responsibleTeam?.id == selectedResponsibleTeamId) {
            licenseDetail?.responsibleTeam?.name.orEmpty()
        } else {
            "Select Responsible Team..."
        }
    }

    // ── File Pickers ──
    val certificatePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            certificateFiles.clear()
            certificateFiles.add(uri)
        }
    }

    val supportingDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (supportingDocFiles.size < 10 && !supportingDocFiles.contains(uri)) {
                supportingDocFiles.add(uri)
            }
        }
    }

    // ── Form Validation & Submission ──
    fun handleSubmit() {
        if (licenseType.isBlank()) {
            Toast.makeText(context, "License type is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (licenseName.isBlank()) {
            Toast.makeText(context, "License name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (licenseNumber.isBlank()) {
            Toast.makeText(context, "License number is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (issuingAuthority.isBlank()) {
            Toast.makeText(context, "Issuing authority is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (countryName.isBlank()) {
            Toast.makeText(context, "Country is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (issueDate.isBlank()) {
            Toast.makeText(context, "Issue date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (expiryDate.isBlank()) {
            Toast.makeText(context, "Expiry date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedLicenseOwnerId.isBlank()) {
            Toast.makeText(context, "License owner is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (riskLevel.isBlank()) {
            Toast.makeText(context, "Risk level is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isEditMode && certificateFiles.isEmpty()) {
            Toast.makeText(context, "Upload the license certificate", Toast.LENGTH_SHORT).show()
            return
        }

        // Date sequence validations
        val parsedIssue = parseDate(issueDate)
        val parsedExpiry = parseDate(expiryDate)
        val parsedRenewal = parseDate(renewalDate)

        if (parsedIssue != null && parsedExpiry != null && parsedExpiry <= parsedIssue) {
            Toast.makeText(context, "Expiry date must be after the issue date", Toast.LENGTH_SHORT).show()
            return
        }
        if (parsedRenewal != null && parsedExpiry != null && parsedRenewal > parsedExpiry) {
            Toast.makeText(context, "Renewal date can't be after the expiry date", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        val fields = mutableMapOf<String, RequestBody>()
        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        // 1. License Information
        addField("licenseType", licenseType)
        addField("licenseName", licenseName)
        addField("licenseNumber", licenseNumber)
        addField("issuingAuthority", issuingAuthority)
        addField("description", description)

        // 2. Location
        addField("countryName", countryName)
        addField("region", region)
        addField("jurisdiction", jurisdiction)
        addField("legalEntity", legalEntity)
        addField("branchId", selectedBranchId)

        // 3. Validity & Dates
        addField("issueDate", toSimpleDate(issueDate))
        addField("expiryDate", toSimpleDate(expiryDate))
        addField("renewalDate", toSimpleDate(renewalDate))
        val noticeDays = renewalNoticePeriod.filter { it.isDigit() }.ifBlank { "30" }
        addField("renewalNoticePeriod", noticeDays)

        // 4. Ownership
        addField("licenseOwner", selectedLicenseOwnerId)
        addField("responsibleTeam", selectedResponsibleTeamId)
        addField("riskLevel", riskLevel)

        // 5. Notes
        addField("notes", notes)

        // 6. Effective Date logic (Sent on create, omitted on update when already present)
        if (!isEditMode || !hasEffectiveDate) {
            addField("effectiveDate", toSimpleDate(issueDate))
        }

        // 7. Multipart Binary Files
        val certificateParts = certificateFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "licenseCertificate")
        }
        val supportingParts = supportingDocFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "supportingDocuments")
        }

        if (isEditMode && licenseIdToEdit.isNotBlank()) {
            viewModel.updateLegalLicense(
                licenseId = licenseIdToEdit,
                fields = fields,
                certificateFiles = certificateParts,
                supportingFiles = supportingParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Legal license updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalLicenses()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to update legal license", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.createLegalLicense(
                fields = fields,
                certificateFiles = certificateParts,
                supportingFiles = supportingParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Legal license created successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalLicenses()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to create legal license", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update License" else "Create License",
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
                                text = if (isEditMode) "Save Changes" else "Save License",
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
                CircularProgressIndicator(
                    color = Primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
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
                // ── Section 1: License Information ──
                LicenseSectionCard(stepNumber = "1", title = "License Information") {
                    FormDropdown(
                        label = "LICENSE TYPE",
                        isRequired = true,
                        value = licenseType,
                        expanded = isLicenseTypeExpanded,
                        onExpandChange = { isLicenseTypeExpanded = it },
                        options = licenseTypeOptions,
                        onOptionSelected = { licenseType = it }
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LICENSE NAME", isRequired = true)
                        FormTextField(
                            value = licenseName,
                            onValueChange = { licenseName = it },
                            placeholder = "Enter license name"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LICENSE NUMBER", isRequired = true)
                        FormTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            placeholder = "Enter license / registration number"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "ISSUING AUTHORITY", isRequired = true)
                        FormTextField(
                            value = issuingAuthority,
                            onValueChange = { issuingAuthority = it },
                            placeholder = "Enter issuing authority or body"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "DESCRIPTION")
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "What this license covers"
                        )
                    }
                }

                // ── Section 2: Location ──
                LicenseSectionCard(stepNumber = "2", title = "Location") {
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
                            placeholder = "e.g. Tamil Nadu, Dubai"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "JURISDICTION")
                        FormTextField(
                            value = jurisdiction,
                            onValueChange = { jurisdiction = it },
                            placeholder = "e.g. Federal, State, Municipal"
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

                // ── Section 3: Validity ──
                LicenseSectionCard(stepNumber = "3", title = "Validity") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "ISSUE DATE", isRequired = true)
                        DatePickerField(
                            value = issueDate,
                            onDateSelected = { issueDate = it }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "EXPIRY DATE", isRequired = true)
                        DatePickerField(
                            value = expiryDate,
                            onDateSelected = { expiryDate = it }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "RENEWAL DATE")
                        DatePickerField(
                            value = renewalDate,
                            onDateSelected = { renewalDate = it }
                        )
                    }

                    FormDropdown(
                        label = "RENEWAL NOTICE PERIOD",
                        value = renewalNoticePeriod,
                        expanded = isRenewalNoticeExpanded,
                        onExpandChange = { isRenewalNoticeExpanded = it },
                        options = noticePeriodOptions,
                        onOptionSelected = { renewalNoticePeriod = it }
                    )
                }

                // ── Section 4: Ownership ──
                LicenseSectionCard(stepNumber = "4", title = "Ownership") {
                    FormDropdown(
                        label = "LICENSE OWNER",
                        isRequired = true,
                        value = selectedLicenseOwnerName,
                        expanded = isLicenseOwnerExpanded,
                        onExpandChange = { isLicenseOwnerExpanded = it },
                        options = memberList.map { it.displayName() },
                        onOptionSelected = { name ->
                            selectedLicenseOwnerId = memberList.find { it.displayName() == name }?._id.orEmpty()
                        }
                    )

                    FormDropdown(
                        label = "RESPONSIBLE TEAM",
                        value = selectedResponsibleTeamName,
                        expanded = isResponsibleTeamExpanded,
                        onExpandChange = { isResponsibleTeamExpanded = it },
                        options = departmentList.map { it.name },
                        onOptionSelected = { name ->
                            val foundDept = departmentList.find { it.name == name }
                            selectedResponsibleTeamId = foundDept?._id.orEmpty()
                        }
                    )

                    // Disabled: Status is set automatically by the backend based on expiry date
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "STATUS")
                        FormTextField(
                            value = if (isEditMode) savedStatus else "Active",
                            onValueChange = {},
                            enabled = false,
                            placeholder = "Active"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Set automatically from the expiry date.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }

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

                // ── Section 5: Documents & Notes ──
                LicenseSectionCard(stepNumber = "5", title = "Documents & Notes") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LICENSE CERTIFICATE", isRequired = !isEditMode)
                        ImageUploadSection(
                            selectedImages = certificateFiles,
                            onBrowseClick = { certificatePicker.launch("*/*") },
                            onRemoveImage = { file -> certificateFiles.remove(file) },
                            maxFiles = 1,
                            title = "License Certificate",
                            subtitle = "Click to upload or drag & drop (PDF, JPG, PNG or WEBP up to 5 MB)",
                            supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "SUPPORTING DOCUMENTS")
                        ImageUploadSection(
                            selectedImages = supportingDocFiles,
                            onBrowseClick = { supportingDocPicker.launch("*/*") },
                            onRemoveImage = { file -> supportingDocFiles.remove(file) },
                            maxFiles = 10,
                            title = "Supporting Documents",
                            subtitle = "Click to upload or drag & drop (PDF, JPG, PNG or WEBP up to 5 MB)",
                            supportedFormatsText = "Max 10 files (PDF, JPG, PNG or WEBP up to 5 MB)"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "NOTES")
                        FormTextArea(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = "Add any notes"
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Reusable Numbered Section Container ──
@Composable
fun LicenseSectionCard(
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