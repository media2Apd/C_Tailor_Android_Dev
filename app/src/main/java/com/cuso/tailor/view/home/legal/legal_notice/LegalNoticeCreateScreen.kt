@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_notice

import android.net.Uri
import android.widget.Toast
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// Static dropdown options matching the web UI
private val CASE_TYPES = listOf("Notice", "Dispute", "Breach")
private val PARTY_TYPES = listOf("Customer", "Employee", "Vendor", "Supplier")
private val RISK_LEVELS = listOf("Low", "Medium", "High", "Critical")
private val PRIORITIES = listOf("Low", "Medium", "High", "Critical")
private val COUNTRIES = listOf(
    "India",
    "United States",
    "United Kingdom",
    "United Arab Emirates",
    "Singapore",
    "Germany",
    "Australia"
)

// Data holder for dynamic party dropdown options
private data class PartyOptionItem(val id: String, val name: String)

@Composable
fun CreateEditLegalNoticeScreen(
    idToEdit: String? = null,
    viewModel: LegalViewModel,
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    orderViewModel: SalesOrderViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val isEditMode = !idToEdit.isNullOrBlank()

    // ── 1. API Dependency States ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        if (branchState is BranchUiState.Success) (branchState as BranchUiState.Success).branches else emptyList()
    }

    val membersList by hrViewModel.members.collectAsStateWithLifecycle()
    val contractsList by viewModel.contracts.collectAsStateWithLifecycle()
    val vendorList by viewModel.legalVendors.collectAsStateWithLifecycle()

    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val customerList = remember(customerUiState) {
        if (customerUiState is CustomerUiState.Success) (customerUiState as CustomerUiState.Success).customers else emptyList()
    }

    val orderUiState by orderViewModel.orderState.collectAsStateWithLifecycle()
    val ordersList = remember(orderUiState) {
        if (orderUiState is OrderUiState.Success) (orderUiState as OrderUiState.Success).orders else emptyList()
    }

    val invoiceList by financeViewModel.invoiceList.collectAsStateWithLifecycle()

    val noticeDetail by viewModel.noticeDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingNoticeDetail.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSavingNotice.collectAsStateWithLifecycle()

    // ── 2. Form Input States ──
    // Section 1: Case Information
    var caseType by remember { mutableStateOf("") }
    var caseTypeExpanded by remember { mutableStateOf(false) }

    var caseTitle by remember { mutableStateOf("") }

    var partyType by remember { mutableStateOf("") }
    var partyTypeExpanded by remember { mutableStateOf(false) }

    var primaryPartyId by remember { mutableStateOf("") }
    var primaryPartyName by remember { mutableStateOf("") }
    var primaryPartyExpanded by remember { mutableStateOf(false) }

    // Section 2: References
    var selectedOrderId by remember { mutableStateOf("") }
    var orderExpanded by remember { mutableStateOf(false) }

    var selectedContractId by remember { mutableStateOf("") }
    var contractExpanded by remember { mutableStateOf(false) }

    var selectedInvoiceId by remember { mutableStateOf("") }
    var invoiceExpanded by remember { mutableStateOf(false) }

    var selectedRecordId by remember { mutableStateOf("") }
    var recordExpanded by remember { mutableStateOf(false) }

    // Section 3: Case Details
    var caseDescription by remember { mutableStateOf("") }

    var riskLevel by remember { mutableStateOf("") }
    var riskExpanded by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("Medium") }
    var priorityExpanded by remember { mutableStateOf(false) }

    var countryName by remember { mutableStateOf("India") }
    var countryExpanded by remember { mutableStateOf(false) }

    var jurisdiction by remember { mutableStateOf("") }

    var selectedBranchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    var selectedLegalOwnerId by remember { mutableStateOf("") }
    var legalOwnerExpanded by remember { mutableStateOf(false) }

    var nextDeadline by remember { mutableStateOf("") }

    // Section 4: Documents & Notes
    var caseDocumentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var supportingEvidenceUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var notes by remember { mutableStateOf("") }

    val caseDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        caseDocumentUris = (caseDocumentUris + it).take(10)
    }
    val supportingDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        supportingEvidenceUris = (supportingEvidenceUris + it).take(10)
    }

    // Locked case check matching React isLocked()
    val isLockedCase = isEditMode && (
            noticeDetail?.status.equals("Completed", ignoreCase = true) ||
                    noticeDetail?.status.equals("Case_Closed", ignoreCase = true) ||
                    noticeDetail?.caseStatus.equals("Case_Closed", ignoreCase = true)
            )

    // ── 3. Fetch Initial Data ──
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(limit = 100)
        viewModel.fetchContracts(limit = 100)
        viewModel.fetchLegalVendors(limit = 100)
        customerViewModel.loadCustomers(isRefresh = true)
        orderViewModel.fetchOrders(limit = 100)
        financeViewModel.fetchInvoices(limit = 100)
    }

    LaunchedEffect(idToEdit) {
        if (isEditMode) {
            viewModel.fetchLegalNoticeDetail(idToEdit)
        } else {
            viewModel.clearLegalNoticeDetail()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalNoticeDetail()
        }
    }

    // ── 4. Pre-fill Values on Edit Mode ──
    LaunchedEffect(noticeDetail) {
        noticeDetail?.let { detail ->
            if (isEditMode) {
                caseType = detail.caseType
                caseTitle = detail.caseTitle
                partyType = detail.partyType
                primaryPartyId = detail.primaryParty
                primaryPartyName = detail.primaryParty

                selectedOrderId = detail.relatedOrder?.id.orEmpty()
                selectedInvoiceId = detail.relatedInvoice?.id.orEmpty().ifEmpty { detail.relatedInvoice?.invoiceNumber.orEmpty() }
                selectedContractId = detail.relatedContract?.id.orEmpty()
                selectedRecordId = detail.relatedRecord.orEmpty()

                caseDescription = detail.caseDescription
                riskLevel = detail.riskLevel
                priority = detail.priority.ifBlank { "Medium" }
                countryName = detail.displayCountry.ifBlank { "India" }
                jurisdiction = detail.jurisdiction
                selectedBranchId = detail.branchId?.id.orEmpty()
                selectedLegalOwnerId = detail.legalOwner?.id.orEmpty()
                nextDeadline = detail.nextDeadline.take(10)
                notes = detail.notes
            }
        }
    }

    // ── 5. Dynamic Primary Party Resolution ──
    val dynamicPartyOptions: List<PartyOptionItem> = remember(partyType, customerList, membersList, vendorList) {
        when (partyType) {
            "Customer" -> customerList.map { PartyOptionItem(it.id, it.name) }
            "Employee" -> membersList.map { PartyOptionItem(it._id, "${it.firstName} ${it.lastName}".trim()) }
            "Vendor", "Supplier" -> vendorList.map { PartyOptionItem(it.id, it.vendor) }
            else -> emptyList()
        }
    }

    // Synchronize display label when an ID is already loaded in Edit mode
    LaunchedEffect(primaryPartyId, dynamicPartyOptions) {
        if (primaryPartyId.isNotBlank()) {
            val matched = dynamicPartyOptions.find { it.id == primaryPartyId }
            if (matched != null) {
                primaryPartyName = matched.name
            }
        }
    }

    // ── 6. Display Label Helpers ──
    fun getContractDisplay(contract: LegalContractDto): String {
        return listOfNotNull(contract.contractNumber, contract.contractName)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
    }

    val selectedContractDisplay = remember(selectedContractId, contractsList) {
        contractsList.find { it.id == selectedContractId }?.let { getContractDisplay(it) }.orEmpty()
    }

    val selectedInvoiceDisplay = remember(selectedInvoiceId, invoiceList) {
        invoiceList.find { it.id == selectedInvoiceId }?.let { it.invoiceNumber.ifBlank { it.id } }.orEmpty()
    }

    val selectedOrderDisplay = remember(selectedOrderId, ordersList) {
        ordersList.find { it.id == selectedOrderId }?.let {
            if (it.orderNumber.isNotBlank()) it.orderNumber else "#${it.id.takeLast(6)}"
        }.orEmpty()
    }

    val selectedBranchDisplay = remember(selectedBranchId, branchList) {
        branchList.find { it.id == selectedBranchId }?.name.orEmpty()
    }

    val selectedLegalOwnerDisplay = remember(selectedLegalOwnerId, membersList) {
        membersList.find { it._id == selectedLegalOwnerId }?.let { "${it.firstName} ${it.lastName}".trim() }.orEmpty()
    }

    // Normalizes UI dates to ISO YYYY-MM-DD
    fun formatToApiDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        val clean = rawDate.trim().replace("/", "-")
        val parts = clean.split("-")
        return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            clean.take(10)
        }
    }

    fun toTextPart(value: String): RequestBody {
        return value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // ── 7. Validation Matching Web Requirements ──
    fun validate(): Boolean {
        if (caseType.isBlank()) {
            Toast.makeText(context, "Case type is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (caseTitle.trim().length < 3) {
            Toast.makeText(context, "Case title needs at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (partyType.isBlank()) {
            Toast.makeText(context, "Party type is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (primaryPartyId.isBlank()) {
            Toast.makeText(context, "Primary party is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (caseDescription.trim().length < 10) {
            Toast.makeText(context, "Case description needs at least 10 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (riskLevel.isBlank()) {
            Toast.makeText(context, "Risk level is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedLegalOwnerId.isBlank()) {
            Toast.makeText(context, "Legal owner is required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ── 8. Form Submission (Exact Payload Construction) ──
    fun handleSubmit() {
        if (!validate()) return

        val fieldsMap = mutableMapOf<String, RequestBody>()

        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fieldsMap[key] = toTextPart(value)
            }
        }

        // Section 1 fields
        addField("caseType", caseType)
        addField("caseTitle", caseTitle)
        addField("partyType", partyType)
        addField("primaryParty", primaryPartyId)

        // Section 2: References (MongoDB ObjectIds)
        addField("relatedOrder", selectedOrderId)
        addField("relatedContract", selectedContractId)
        addField("relatedInvoice", selectedInvoiceId)
        addField("relatedRecord", selectedRecordId)

        // Section 3: Case details
        addField("caseDescription", caseDescription)
        addField("riskLevel", riskLevel)
        addField("priority", priority.ifBlank { "Medium" })
        addField("countryName", countryName)
        addField("jurisdiction", jurisdiction)
        addField("branchId", selectedBranchId)
        addField("legalOwner", selectedLegalOwnerId)
        addField("nextDeadline", formatToApiDate(nextDeadline))
        addField("notes", notes)

        // Section 4: Document files
        val caseDocParts = caseDocumentUris.mapNotNull { uriToMultipartPart(context, it, "caseDocuments") }
        val supportingParts = supportingEvidenceUris.mapNotNull { uriToMultipartPart(context, it, "supportingEvidence") }

        if (isEditMode) {
            viewModel.updateLegalNotice(
                noticeId = idToEdit,
                fields = fieldsMap,
                caseDocuments = caseDocParts,
                supportingEvidence = supportingParts,
                onSuccess = {
                    Toast.makeText(context, "Case updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalNotices()
                    onNavigateBack()
                },
                onError = { err ->
                    Toast.makeText(context, err ?: "Failed to update case", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.createLegalNotice(
                fields = fieldsMap,
                caseDocuments = caseDocParts,
                supportingEvidence = supportingParts,
                onSuccess = {
                    Toast.makeText(context, "Case created successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalNotices()
                    onNavigateBack()
                },
                onError = { err ->
                    Toast.makeText(context, err ?: "Failed to create case", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update Case" else "Create Case",
                onClose = onNavigateBack
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = whiteBg) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(tokens.screenPadding).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        enabled = !isSaving
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
                    }

                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        enabled = !isSaving && !isLockedCase && !isLoadingDetail
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) "Save Changes" else "Save Case",
                                fontSize = tokens.bodySmall,
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
                    .padding(innerPadding)
                    .padding(horizontal = tokens.screenPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // Locked case banner matching web dashboard
                if (isLockedCase) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = yellowBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, yellowText.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This case is ${noticeDetail?.status ?: "Completed"}, so it cannot be edited.",
                            color = yellowText,
                            fontSize = tokens.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(tokens.screenPadding))
                }

                // ── Section 1: Case Information ──
                FormSectionCard(number = 1, title = "Case Information") {
                    FormDropdown(
                        label = "CASE TYPE",
                        value = caseType.ifBlank { "Select Case Type..." },
                        expanded = caseTypeExpanded,
                        onExpandChange = { caseTypeExpanded = it },
                        options = CASE_TYPES,
                        onOptionSelected = { caseType = it },
                        isRequired = true
                    )

                    FormLabel(text = "CASE TITLE", isRequired = true)
                    FormTextField(
                        value = caseTitle,
                        onValueChange = { caseTitle = it },
                        placeholder = "Enter descriptive case title",
                        enabled = !isLockedCase
                    )

                    FormDropdown(
                        label = "PARTY TYPE",
                        value = partyType.ifBlank { "Select Party Type..." },
                        expanded = partyTypeExpanded,
                        onExpandChange = { partyTypeExpanded = it },
                        options = PARTY_TYPES,
                        onOptionSelected = { selectedType ->
                            partyType = selectedType
                            // Reset primary party selection when party type changes
                            primaryPartyId = ""
                            primaryPartyName = ""
                        },
                        isRequired = true
                    )

                    // Primary Party Dropdown (Disabled until Party Type is selected)
                    val isPartyDisabled = partyType.isBlank() || isLockedCase
                    val partyDropdownPlaceholder = if (partyType.isBlank()) {
                        "Select the party type first"
                    } else {
                        primaryPartyName.ifBlank { "Select ${partyType.lowercase()}..." }
                    }

                    FormDropdown(
                        label = if (partyType.isNotBlank()) "PRIMARY PARTY ($partyType)" else "PRIMARY PARTY",
                        value = partyDropdownPlaceholder,
                        expanded = if (isPartyDisabled) false else primaryPartyExpanded,
                        onExpandChange = { if (!isPartyDisabled) primaryPartyExpanded = it },
                        options = dynamicPartyOptions.map { it.name },
                        onOptionSelected = { chosenName ->
                            val matched = dynamicPartyOptions.find { it.name == chosenName }
                            if (matched != null) {
                                primaryPartyId = matched.id
                                primaryPartyName = matched.name
                            }
                        },
                        isRequired = true
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 2: References (API-fetched Contracts, Invoices & Orders) ──
                FormSectionCard(number = 2, title = "References") {
                    // Related Order (Dropdown from SalesOrderViewModel)
                    val orderOptions = remember(ordersList) {
                        listOf("Select order...") + ordersList.map {
                            if (it.orderNumber.isNotBlank()) it.orderNumber else "#${it.id.takeLast(6)}"
                        }
                    }
                    FormDropdown(
                        label = "RELATED ORDER",
                        value = selectedOrderDisplay.ifBlank { "Select order..." },
                        expanded = orderExpanded,
                        onExpandChange = { orderExpanded = it },
                        options = orderOptions,
                        onOptionSelected = { chosen ->
                            if (chosen == "Select order...") {
                                selectedOrderId = ""
                            } else {
                                val matched = ordersList.find {
                                    (if (it.orderNumber.isNotBlank()) it.orderNumber else "#${it.id.takeLast(6)}") == chosen
                                }
                                selectedOrderId = matched?.id.orEmpty()
                            }
                        },
                        isRequired = false
                    )

                    // Related Contract (Dropdown from LegalViewModel)
                    val contractOptions = remember(contractsList) {
                        listOf("Select contract...") + contractsList.map { getContractDisplay(it) }
                    }
                    FormDropdown(
                        label = "RELATED CONTRACT",
                        value = selectedContractDisplay.ifBlank { "Select contract..." },
                        expanded = contractExpanded,
                        onExpandChange = { contractExpanded = it },
                        options = contractOptions,
                        onOptionSelected = { chosen ->
                            if (chosen == "Select contract...") {
                                selectedContractId = ""
                            } else {
                                selectedContractId = contractsList.find { getContractDisplay(it) == chosen }?.id.orEmpty()
                            }
                        },
                        isRequired = false
                    )

                    // Related Invoice (Dropdown from FinanceViewModel)
                    val invoiceOptions = remember(invoiceList) {
                        listOf("Select invoice...") + invoiceList.map { it.invoiceNumber.ifBlank { it.id } }
                    }
                    FormDropdown(
                        label = "RELATED INVOICE",
                        value = selectedInvoiceDisplay.ifBlank { "Select invoice..." },
                        expanded = invoiceExpanded,
                        onExpandChange = { invoiceExpanded = it },
                        options = invoiceOptions,
                        onOptionSelected = { chosen ->
                            if (chosen == "Select invoice...") {
                                selectedInvoiceId = ""
                            } else {
                                selectedInvoiceId = invoiceList.find { (it.invoiceNumber.ifBlank { it.id }) == chosen }?.id.orEmpty()
                            }
                        },
                        isRequired = false
                    )

                    // Related Record (Dropdown placeholder matching screenshot 1)
                    FormDropdown(
                        label = "RELATED RECORD",
                        value = selectedRecordId.ifBlank { "Select record..." },
                        expanded = recordExpanded,
                        onExpandChange = { recordExpanded = it },
                        options = listOf("Select record..."),
                        onOptionSelected = { chosen ->
                            selectedRecordId = if (chosen == "Select record...") "" else chosen
                        },
                        isRequired = false
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 3: Case Details ──
                FormSectionCard(number = 3, title = "Case Details") {
                    FormLabel(text = "CASE DESCRIPTION", isRequired = true)
                    FormTextArea(
                        value = caseDescription,
                        onValueChange = { caseDescription = it },
                        placeholder = "Provide detailed description of the case",
                        enabled = !isLockedCase
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "RISK LEVEL",
                                value = riskLevel.ifBlank { "Select Risk Level..." },
                                expanded = riskExpanded,
                                onExpandChange = { riskExpanded = it },
                                options = RISK_LEVELS,
                                onOptionSelected = { riskLevel = it },
                                isRequired = true
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "PRIORITY",
                                value = priority.ifBlank { "Medium" },
                                expanded = priorityExpanded,
                                onExpandChange = { priorityExpanded = it },
                                options = PRIORITIES,
                                onOptionSelected = { priority = it }
                            )
                        }
                    }

                    FormDropdown(
                        label = "COUNTRY",
                        value = countryName.ifBlank { "Select Country..." },
                        expanded = countryExpanded,
                        onExpandChange = { countryExpanded = it },
                        options = COUNTRIES,
                        onOptionSelected = { countryName = it }
                    )

                    FormLabel(text = "JURISDICTION", isRequired = false)
                    FormTextField(
                        value = jurisdiction,
                        onValueChange = { jurisdiction = it },
                        placeholder = "e.g. High Court, Delhi",
                        enabled = !isLockedCase
                    )

                    // Branch (Dropdown from BranchViewModel)
                    val branchOptions = remember(branchList) {
                        listOf("Select Branch...") + branchList.mapNotNull { it.name?.takeIf { name -> name.isNotBlank() } }
                    }
                    FormDropdown(
                        label = "BRANCH",
                        value = selectedBranchDisplay.ifBlank { "Select Branch..." },
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branchOptions,
                        onOptionSelected = { chosen ->
                            selectedBranchId = if (chosen == "Select Branch...") "" else branchList.find { it.name == chosen }?.id.orEmpty()
                        }
                    )

                    // Legal Owner (Dropdown from HrViewModel)
                    val memberOptions = remember(membersList) {
                        listOf("Select Legal owner...") + membersList.map { "${it.firstName} ${it.lastName}".trim() }
                    }
                    FormDropdown(
                        label = "LEGAL OWNER",
                        value = selectedLegalOwnerDisplay.ifBlank { "Select Legal owner..." },
                        expanded = legalOwnerExpanded,
                        onExpandChange = { legalOwnerExpanded = it },
                        options = memberOptions,
                        onOptionSelected = { chosen ->
                            selectedLegalOwnerId = if (chosen == "Select Legal owner...") "" else membersList.find { "${it.firstName} ${it.lastName}".trim() == chosen }?._id.orEmpty()
                        },
                        isRequired = true
                    )

                    FormLabel(text = "NEXT DEADLINE", isRequired = false)
                    DatePickerField(
                        value = nextDeadline,
                        onDateSelected = { nextDeadline = it }
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                // ── Section 4: Documents & Notes ──
                FormSectionCard(number = 4, title = "Documents") {
                    FormLabel(text = "CASE DOCUMENTS", isRequired = false)
                    ImageUploadSection(
                        selectedImages = caseDocumentUris.map { it.toString() },
                        onBrowseClick = { caseDocLauncher.launch("*/*") },
                        onRemoveImage = { uri -> caseDocumentUris = caseDocumentUris.filter { it.toString() != uri } },
                        maxFiles = 10,
                        title = "Click to upload or drag & drop",
                        subtitle = "PDF, JPG, PNG or WEBP up to 5 MB",
                        supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    FormLabel(text = "SUPPORTING EVIDENCE", isRequired = false)
                    ImageUploadSection(
                        selectedImages = supportingEvidenceUris.map { it.toString() },
                        onBrowseClick = { supportingDocLauncher.launch("*/*") },
                        onRemoveImage = { uri -> supportingEvidenceUris = supportingEvidenceUris.filter { it.toString() != uri } },
                        maxFiles = 10,
                        title = "Click to upload or drag & drop",
                        subtitle = "PDF, JPG, PNG or WEBP up to 5 MB",
                        supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                    )

                    Spacer(modifier = Modifier.height(tokens.extraPadding))

                    FormLabel(text = "NOTES", isRequired = false)
                    FormTextArea(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Add any notes",
                        enabled = !isLockedCase
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
            }
        }
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(primary_light, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = number.toString(), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = Primary)
            }
            Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(tokens.screenPadding))
        HorizontalDivider(color = BorderGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(tokens.screenPadding))
        content()
    }
}