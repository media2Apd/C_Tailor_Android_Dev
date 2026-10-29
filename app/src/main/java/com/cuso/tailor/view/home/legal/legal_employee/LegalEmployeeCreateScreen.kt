@file:Suppress("unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.legal.legal_employee

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalDocumentItem
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.model.settings.DepartmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

private val EMPLOYMENT_TYPES = listOf("Full Time", "Part Time", "Contract", "Temporary", "Intern", "Consultant")
private val LEGAL_RECORD_TYPES = listOf(
    "Employment Agreement",
    "NDA",
    "Code of Conduct",
    "Statutory Compliance",
    "Disciplinary",
    "Termination",
    "Grievance",
    "Other"
)
private val LABOUR_DOCUMENT_TYPES = listOf(
    "Employment Contract",
    "Offer Letter",
    "Appointment Letter",
    "PF / ESI Registration",
    "Gratuity Nomination",
    "Standing Orders",
    "Relieving Letter",
    "Other"
)
private val NOTICE_PERIODS = listOf("15 Days", "30 Days", "45 Days", "60 Days", "90 Days")
private val RISK_LEVELS = listOf("Low", "Medium", "High")

// Maximum number of files allowed per upload section
private const val MAX_FILES_PER_SECTION = 5

@Composable
fun LegalEmployeeCreateEditScreen(
    idToEdit: String? = null,
    viewModel: LegalViewModel,
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    departmentViewModel: DepartmentViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isEditMode = !idToEdit.isNullOrEmpty()

    val detailRecord by viewModel.employeeDetail.collectAsState()
    val isDetailLoading by viewModel.isLoadingEmployeeDetail.collectAsState()
    val isSaving by viewModel.isSavingEmployee.collectAsState()

    // Remote data sources
    val membersList by hrViewModel.members.collectAsState()
    val branchUiState by branchViewModel.uiState.collectAsState()
    val departmentUiState by departmentViewModel.uiState.collectAsState()
    val contractList by viewModel.contracts.collectAsState()

    // Parse branch list from state
    val branchList: List<BranchItem> = remember(branchUiState) {
        if (branchUiState is BranchUiState.Success) {
            (branchUiState as BranchUiState.Success).branches
        } else {
            emptyList()
        }
    }

    val departmentList: List<DepartmentItem> = remember(departmentUiState) {
        if (departmentUiState is DepartmentUiState.Success) {
            (departmentUiState as DepartmentUiState.Success).departments
        } else {
            emptyList()
        }
    }

    // Dynamic Legal Entity options derived directly from the Branch API response
    val legalEntityOptions = remember(branchList) {
        branchList.mapNotNull { it.name }
            .filter { it.isNotBlank() }
            .distinct()
    }

    // Form states
    var recordNumber by remember { mutableStateOf("") }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var employeeCode by remember { mutableStateOf("") }
    var selectedEmploymentType by remember { mutableStateOf("") }
    var selectedDepartmentId by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    var legalEntity by remember { mutableStateOf("") }

    var selectedLegalRecordType by remember { mutableStateOf("") }
    var selectedContractId by remember { mutableStateOf("") }
    var selectedLabourDocumentType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var renewalDate by remember { mutableStateOf("") }
    var selectedNoticePeriod by remember { mutableStateOf("") }

    var selectedHrOwnerId by remember { mutableStateOf("") }
    var selectedLegalOwnerId by remember { mutableStateOf("") }
    var selectedRiskLevel by remember { mutableStateOf("") }

    // Existing documents saved on the server
    var existingContractDocs by remember { mutableStateOf<List<LegalDocumentItem>>(emptyList()) }
    var existingLabourDocs by remember { mutableStateOf<List<LegalDocumentItem>>(emptyList()) }
    var existingSupportingDocs by remember { mutableStateOf<List<LegalDocumentItem>>(emptyList()) }

    // Newly picked files stored as URIs
    val newContractFiles = remember { mutableStateListOf<Uri>() }
    val newLabourFiles = remember { mutableStateListOf<Uri>() }
    val newSupportingFiles = remember { mutableStateListOf<Uri>() }

    // Dropdown expanded states
    var empExpanded by remember { mutableStateOf(false) }
    var empTypeExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    var branchExpanded by remember { mutableStateOf(false) }
    var entityExpanded by remember { mutableStateOf(false) }
    var recTypeExpanded by remember { mutableStateOf(false) }
    var contractExpanded by remember { mutableStateOf(false) }
    var docTypeExpanded by remember { mutableStateOf(false) }
    var noticeExpanded by remember { mutableStateOf(false) }
    var hrOwnerExpanded by remember { mutableStateOf(false) }
    var legalOwnerExpanded by remember { mutableStateOf(false) }
    var riskExpanded by remember { mutableStateOf(false) }

    // Multi-file launcher for contract documents
    val contractPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (newContractFiles.size < MAX_FILES_PER_SECTION && !newContractFiles.contains(uri)) {
                newContractFiles.add(uri)
            }
        }
    }

    // Multi-file launcher for labour documents
    val labourPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (newLabourFiles.size < MAX_FILES_PER_SECTION && !newLabourFiles.contains(uri)) {
                newLabourFiles.add(uri)
            }
        }
    }

    // Multi-file launcher for supporting documents
    val supportingPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (newSupportingFiles.size < MAX_FILES_PER_SECTION && !newSupportingFiles.contains(uri)) {
                newSupportingFiles.add(uri)
            }
        }
    }

    // Fetch initial data sources
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers()
        branchViewModel.loadBranches()
        departmentViewModel.loadDepartments()
        viewModel.fetchContracts()
    }

    // Fetch detail in edit mode
    LaunchedEffect(idToEdit) {
        if (!idToEdit.isNullOrEmpty()) {
            viewModel.fetchLegalEmployeeDetail(idToEdit)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalEmployeeDetail()
        }
    }

    // Prefill form states when editing
    LaunchedEffect(detailRecord) {
        val item = detailRecord
        if (item != null && isEditMode) {
            recordNumber = item.legalRecordNumber
            selectedEmployeeId = item.employeeId?.id ?: ""
            employeeCode = item.employeeCode.ifEmpty { item.employeeId?.memberId ?: "" }
            selectedEmploymentType = item.employmentType
            selectedDepartmentId = item.departmentId?.id ?: ""
            selectedBranchId = item.branchId?.id ?: ""
            legalEntity = item.legalEntity
            selectedLegalRecordType = item.legalRecordType
            selectedContractId = item.contractId?.id ?: ""
            selectedLabourDocumentType = item.labourDocumentType
            description = item.description
            startDate = formatDateOnly(item.startDate)
            endDate = formatDateOnly(item.endDate)
            renewalDate = formatDateOnly(item.renewalDate)
            selectedNoticePeriod = item.noticePeriod ?: ""
            selectedHrOwnerId = item.hrOwner?.id ?: ""
            selectedLegalOwnerId = item.legalOwner?.id ?: ""
            selectedRiskLevel = item.riskLevel

            existingContractDocs = item.employmentContractDocuments
            existingLabourDocs = item.labourDocuments
            existingSupportingDocs = item.supportingDocuments
        }
    }

    // Auto-update employeeCode when employee selection changes
    LaunchedEffect(selectedEmployeeId, membersList) {
        if (selectedEmployeeId.isNotBlank()) {
            val matched = membersList.find { it._id == selectedEmployeeId }
            if (matched != null && !matched.memberId.isNullOrBlank()) {
                employeeCode = matched.memberId
            }
        }
    }

    val isContractRequired = selectedLabourDocumentType == "Employment Contract"
    val isFormLoading = isEditMode && isDetailLoading

    fun validate(): Boolean {
        if (selectedEmployeeId.isBlank()) {
            DynamicIslandManager.showError("Employee is required")
            return false
        }
        if (selectedEmploymentType.isBlank()) {
            DynamicIslandManager.showError("Employment type is required")
            return false
        }
        if (selectedLegalRecordType.isBlank()) {
            DynamicIslandManager.showError("Legal record type is required")
            return false
        }
        if (selectedLabourDocumentType.isBlank()) {
            DynamicIslandManager.showError("Labour document type is required")
            return false
        }
        if (isContractRequired && selectedContractId.isBlank()) {
            DynamicIslandManager.showError("Contract is required for Employment Contract")
            return false
        }
        if (selectedHrOwnerId.isBlank()) {
            DynamicIslandManager.showError("HR owner is required")
            return false
        }
        if (selectedLegalOwnerId.isBlank()) {
            DynamicIslandManager.showError("Legal owner is required")
            return false
        }
        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val s = sdf.parse(startDate)
                val e = sdf.parse(endDate)
                if (s != null && e != null && !e.after(s)) {
                    DynamicIslandManager.showError("End date must be after the start date")
                    return false
                }
            } catch (_: Exception) {}
        }
        if (newContractFiles.size > MAX_FILES_PER_SECTION ||
            newLabourFiles.size > MAX_FILES_PER_SECTION ||
            newSupportingFiles.size > MAX_FILES_PER_SECTION
        ) {
            DynamicIslandManager.showError("You can upload up to $MAX_FILES_PER_SECTION files per section")
            return false
        }
        return true
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
                color = whiteBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (!validate()) return@Button

                            val fields = mutableMapOf<String, RequestBody>()
                            fun addField(key: String, value: String) {
                                if (value.isNotBlank()) {
                                    fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                }
                            }

                            addField("employeeId", selectedEmployeeId)
                            if (employeeCode.isNotBlank()) {
                                addField("employeeCode", employeeCode)
                            }
                            addField("employmentType", selectedEmploymentType)
                            addField("departmentId", selectedDepartmentId)
                            addField("branchId", selectedBranchId)
                            addField("legalEntity", legalEntity)
                            addField("legalRecordType", selectedLegalRecordType)
                            addField("contractId", selectedContractId)
                            addField("labourDocumentType", selectedLabourDocumentType)
                            addField("description", description)
                            addField("startDate", startDate.toIsoDate())
                            addField("endDate", endDate.toIsoDate())
                            addField("renewalDate", renewalDate.toIsoDate())
                            addField("noticePeriod", selectedNoticePeriod)
                            addField("hrOwner", selectedHrOwnerId)
                            addField("legalOwner", selectedLegalOwnerId)
                            addField("riskLevel", selectedRiskLevel)

                            fun toParts(uris: List<Uri>, partName: String): List<MultipartBody.Part> {
                                return uris.mapNotNull { uri -> uriToMultipartPart(context, uri, partName) }
                            }

                            val cDocs = toParts(newContractFiles, "employmentContractDocuments")
                            val lDocs = toParts(newLabourFiles, "labourDocuments")
                            val sDocs = toParts(newSupportingFiles, "supportingDocuments")

                            if (isEditMode) {
                                viewModel.updateLegalEmployee(
                                    recordId = idToEdit,
                                    fields = fields,
                                    contractDocs = cDocs,
                                    labourDocs = lDocs,
                                    supportingDocs = sDocs,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Record updated successfully")
                                        onNavigateBack()
                                    },
                                    onError = { DynamicIslandManager.showError(it ?: "Failed to update record") }
                                )
                            } else {
                                viewModel.createLegalEmployee(
                                    fields = fields,
                                    contractDocs = cDocs,
                                    labourDocs = lDocs,
                                    supportingDocs = sDocs,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Record created successfully")
                                        onNavigateBack()
                                    },
                                    onError = { DynamicIslandManager.showError(it ?: "Failed to create record") }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(tokens.iconSize),
                                color = whiteBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isEditMode) "Save Changes" else "Save Record",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isFormLoading) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Transparent)
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Section 1: Employee Details
                FormCardWrapper(number = "1", title = "Employee Details") {
                    FormLabel(text = "EMPLOYEE", isRequired = true)
                    val memberNameOptions = remember(membersList) {
                        membersList.map { "${it.firstName} ${it.lastName}".trim().ifEmpty { it.memberId ?: "" } }
                    }
                    val selectedMemberName = remember(selectedEmployeeId, membersList) {
                        membersList.find { it._id == selectedEmployeeId }?.let { "${it.firstName} ${it.lastName}".trim() } ?: ""
                    }

                    FormDropdown(
                        label = null,
                        value = selectedMemberName.ifEmpty { "Select employee..." },
                        expanded = empExpanded,
                        onExpandChange = { empExpanded = it },
                        options = memberNameOptions,
                        onOptionSelected = { chosenName ->
                            val found = membersList.find { "${it.firstName} ${it.lastName}".trim() == chosenName }
                            if (found != null) {
                                selectedEmployeeId = found._id
                                employeeCode = found.memberId ?: ""
                            }
                        },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "EMPLOYEE ID")
                    FormTextField(
                        value = employeeCode,
                        onValueChange = {},
                        placeholder = "Filled from the employee",
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "EMPLOYMENT TYPE",
                        value = selectedEmploymentType.ifEmpty { "Select Employment Type..." },
                        expanded = empTypeExpanded,
                        onExpandChange = { empTypeExpanded = it },
                        options = EMPLOYMENT_TYPES,
                        onOptionSelected = { selectedEmploymentType = it },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    val deptOptions = remember(departmentList) {
                        departmentList.map { it.name }
                    }
                    val currentDeptName = remember(selectedDepartmentId, departmentList) {
                        departmentList.find { it._id == selectedDepartmentId }?.name.orEmpty()
                    }
                    FormDropdown(
                        label = "DEPARTMENT",
                        value = currentDeptName.ifEmpty { "Select Department..." },
                        expanded = deptExpanded,
                        onExpandChange = { deptExpanded = it },
                        options = deptOptions,
                        onOptionSelected = { name ->
                            selectedDepartmentId = departmentList.find { it.name == name }?._id.orEmpty()
                        }
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    val branchOptions = remember(branchList) {
                        branchList.mapNotNull { it.name }
                    }
                    val currentBranchName = remember(selectedBranchId, branchList) {
                        branchList.find { it.id == selectedBranchId }?.name.orEmpty()
                    }
                    FormDropdown(
                        label = "BRANCH",
                        value = currentBranchName.ifEmpty { "Select Branch..." },
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchOptions,
                        onOptionSelected = { name ->
                            val selectedBranch = branchList.find { it.name == name }
                            selectedBranchId = selectedBranch?.id.orEmpty()
                            // Automatically align legal entity if it is not selected yet
                            if (legalEntity.isBlank()) {
                                legalEntity = name
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    // Legal Entity options populated dynamically from Branch API
                    FormDropdown(
                        label = "LEGAL ENTITY",
                        value = legalEntity.ifEmpty { "Select Legal Entity..." },
                        expanded = entityExpanded,
                        onExpandChange = { entityExpanded = it },
                        options = legalEntityOptions,
                        onOptionSelected = { legalEntity = it }
                    )
                }

                // Section 2: Legal Record
                FormCardWrapper(number = "2", title = "Legal Record") {
                    FormDropdown(
                        label = "LEGAL RECORD TYPE",
                        value = selectedLegalRecordType.ifEmpty { "Select Legal Record Type..." },
                        expanded = recTypeExpanded,
                        onExpandChange = { recTypeExpanded = it },
                        options = LEGAL_RECORD_TYPES,
                        onOptionSelected = { selectedLegalRecordType = it },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    val contractLabels = remember(contractList) {
                        contractList.map { contract ->
                            listOfNotNull(contract.contractNumber, contract.contractName)
                                .filter { it.isNotBlank() }
                                .joinToString(" - ")
                        }.filter { it.isNotBlank() }
                    }
                    val currentContractLabel = remember(selectedContractId, contractList) {
                        contractList.find { it.id == selectedContractId }?.let { contract ->
                            listOfNotNull(contract.contractNumber, contract.contractName)
                                .filter { it.isNotBlank() }
                                .joinToString(" - ")
                        } ?: ""
                    }
                    FormDropdown(
                        label = "CONTRACT REFERENCE",
                        value = currentContractLabel.ifEmpty { "Select contract..." },
                        expanded = contractExpanded,
                        onExpandChange = { contractExpanded = it },
                        options = contractLabels,
                        onOptionSelected = { selectedLabel ->
                            selectedContractId = contractList.find { contract ->
                                listOfNotNull(contract.contractNumber, contract.contractName)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" - ") == selectedLabel
                            }?.id ?: ""
                        },
                        isRequired = isContractRequired
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "LABOUR DOCUMENT TYPE",
                        value = selectedLabourDocumentType.ifEmpty { "Select Labour Document Type..." },
                        expanded = docTypeExpanded,
                        onExpandChange = { docTypeExpanded = it },
                        options = LABOUR_DOCUMENT_TYPES,
                        onOptionSelected = { selectedLabourDocumentType = it },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "DESCRIPTION")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe this legal record"
                    )
                }

                // Section 3: Employment Dates
                FormCardWrapper(number = "3", title = "Employment Dates") {
                    FormLabel(text = "START DATE")
                    DatePickerField(value = startDate, onDateSelected = { startDate = it })

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "END DATE")
                    DatePickerField(value = endDate, onDateSelected = { endDate = it })

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormLabel(text = "RENEWAL DATE")
                    DatePickerField(value = renewalDate, onDateSelected = { renewalDate = it })

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "NOTICE PERIOD",
                        value = selectedNoticePeriod.ifEmpty { "Select Notice Period..." },
                        expanded = noticeExpanded,
                        onExpandChange = { noticeExpanded = it },
                        options = NOTICE_PERIODS,
                        onOptionSelected = { selectedNoticePeriod = it }
                    )
                }

                // Section 4: Ownership
                FormCardWrapper(number = "4", title = "Ownership") {
                    val memberNames = remember(membersList) {
                        membersList.map { "${it.firstName} ${it.lastName}".trim().ifEmpty { it.memberId ?: "" } }
                    }

                    val hrOwnerName = remember(selectedHrOwnerId, membersList) {
                        membersList.find { it._id == selectedHrOwnerId }?.let { "${it.firstName} ${it.lastName}".trim() } ?: ""
                    }
                    FormDropdown(
                        label = "HR OWNER",
                        value = hrOwnerName.ifEmpty { "Select HR Owner..." },
                        expanded = hrOwnerExpanded,
                        onExpandChange = { hrOwnerExpanded = it },
                        options = memberNames,
                        onOptionSelected = { name ->
                            selectedHrOwnerId = membersList.find { "${it.firstName} ${it.lastName}".trim() == name }?._id ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    val legalOwnerName = remember(selectedLegalOwnerId, membersList) {
                        membersList.find { it._id == selectedLegalOwnerId }?.let { "${it.firstName} ${it.lastName}".trim() } ?: ""
                    }
                    FormDropdown(
                        label = "LEGAL OWNER",
                        value = legalOwnerName.ifEmpty { "Select Legal Owner..." },
                        expanded = legalOwnerExpanded,
                        onExpandChange = { legalOwnerExpanded = it },
                        options = memberNames,
                        onOptionSelected = { name ->
                            selectedLegalOwnerId = membersList.find { "${it.firstName} ${it.lastName}".trim() == name }?._id ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    FormDropdown(
                        label = "RISK LEVEL",
                        value = selectedRiskLevel.ifEmpty { "Select Risk Level..." },
                        expanded = riskExpanded,
                        onExpandChange = { riskExpanded = it },
                        options = RISK_LEVELS,
                        onOptionSelected = { selectedRiskLevel = it }
                    )
                }

                // Section 5: Documents
                FormCardWrapper(number = "5", title = "Documents") {

                    // Employment Contract documents
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "EMPLOYMENT CONTRACT")

                        ExistingDocumentsSection(
                            title = "",
                            documents = existingContractDocs,
                            onDocClick = { doc -> if (doc.documentUrl.isNotBlank()) uriHandler.openUri(doc.documentUrl) }
                        )

                        ImageUploadSection(
                            selectedImages = newContractFiles,
                            onBrowseClick = { contractPicker.launch("*/*") },
                            onRemoveImage = { uri -> newContractFiles.remove(uri) },
                            maxFiles = MAX_FILES_PER_SECTION,
                            title = "Employment Contract",
                            subtitle = "Click to upload or drag & drop (PDF, JPG, PNG or WEBP up to 5 MB)",
                            supportedFormatsText = "Max 5 files (PDF, JPG, PNG or WEBP up to 5 MB)"
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    // Labour documents
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LABOUR DOCUMENTS")

                        ExistingDocumentsSection(
                            title = "",
                            documents = existingLabourDocs,
                            onDocClick = { doc -> if (doc.documentUrl.isNotBlank()) uriHandler.openUri(doc.documentUrl) }
                        )

                        ImageUploadSection(
                            selectedImages = newLabourFiles,
                            onBrowseClick = { labourPicker.launch("*/*") },
                            onRemoveImage = { uri -> newLabourFiles.remove(uri) },
                            maxFiles = MAX_FILES_PER_SECTION,
                            title = "Labour Documents",
                            subtitle = "Click to upload or drag & drop (PDF, JPG, PNG or WEBP up to 5 MB)",
                            supportedFormatsText = "Max 5 files (PDF, JPG, PNG or WEBP up to 5 MB)"
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    // Supporting documents
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "SUPPORTING DOCUMENTS")

                        ExistingDocumentsSection(
                            title = "",
                            documents = existingSupportingDocs,
                            onDocClick = { doc -> if (doc.documentUrl.isNotBlank()) uriHandler.openUri(doc.documentUrl) }
                        )

                        ImageUploadSection(
                            selectedImages = newSupportingFiles,
                            onBrowseClick = { supportingPicker.launch("*/*") },
                            onRemoveImage = { uri -> newSupportingFiles.remove(uri) },
                            maxFiles = MAX_FILES_PER_SECTION,
                            title = "Supporting Documents",
                            subtitle = "Click to upload or drag & drop (PDF, JPG, PNG or WEBP up to 5 MB)",
                            supportedFormatsText = "Max 5 files (PDF, JPG, PNG or WEBP up to 5 MB)"
                        )
                    }

                    Spacer(modifier = Modifier.height(tokens.screenPadding))

                    // AES retention and security policy banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Primary_background)
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                            .padding(tokens.extraPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = greentext,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uploaded agreements are encrypted with AES-256 and subject to legal hold retention policies.",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
            }
        }
    }
}

// Shows documents already uploaded to the server as clickable chips
@Composable
private fun ExistingDocumentsSection(
    title: String,
    documents: List<LegalDocumentItem>,
    onDocClick: (LegalDocumentItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    if (title.isNotBlank()) {
        FormLabel(text = title)
    }
    if (documents.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            documents.forEach { doc ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(Primary_background)
                        .clickable { onDocClick(doc) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = doc.documentName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// Numbered section wrapper card
@Composable
private fun FormCardWrapper(
    number: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.fieldHeight)
                        .background(Primary_background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        fontSize = tokens.bodySmall,
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

            Spacer(modifier = Modifier.height(tokens.extraPadding))
            content()
        }
    }
}

// Converts ISO date strings to dd-MM-yyyy format for pickers
private fun formatDateOnly(raw: String): String {
    if (raw.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}