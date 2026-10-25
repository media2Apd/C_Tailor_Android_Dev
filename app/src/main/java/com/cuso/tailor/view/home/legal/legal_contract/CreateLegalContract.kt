package com.cuso.tailor.view.home.legal.legal_contract

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.CountryAndStatePicker
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun CreateLegalContractScreen(
    onClose: () -> Unit,
    contractIdToEdit: String? = null,
    viewModel: LegalViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !contractIdToEdit.isNullOrBlank()

    // ── Fetch External Dropdowns ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        when (val state = branchState) {
            is BranchUiState.Success -> state.branches
            else -> emptyList()
        }
    }

    val memberList by hrViewModel.members.collectAsStateWithLifecycle()
    val paymentTermsList by financeViewModel.paymentTermsList.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(page = 1, limit = 100)
        financeViewModel.fetchPaymentTerms()
    }

    // ── Static Selection Options ──
    val contractTypeOptions = remember {
        listOf("Enterprise", "Service Agreement", "Supply Agreement", "NDA", "Employment", "Lease", "Licensing", "Partnership")
    }
    val partyTypeOptions = remember {
        listOf("Customer", "Vendor", "Supplier", "Partner", "Employee", "Other")
    }
    val renewalTypeOptions = remember {
        listOf("Auto Renewal", "Manual Renewal", "No Renewal")
    }
    val noticePeriodOptions = remember {
        listOf("15 days", "30 days", "45 days", "60 days", "90 days")
    }
    val currencyOptions = remember {
        listOf("INR", "USD", "EUR", "GBP", "AED", "SGD")
    }
    val riskOptions = remember {
        listOf("Low", "Medium", "High")
    }
    val priorityOptions = remember {
        listOf("Low", "Medium", "High", "Urgent")
    }
    val confidentialityOptions = remember {
        listOf("Public", "Internal", "Confidential", "Strictly Confidential")
    }
    val categoryOptions = remember {
        listOf("Commercial", "Operational", "Financial", "HR", "IT", "Regulatory")
    }

    // ── Form State: 1. Basic Information ──
    var contractType by remember { mutableStateOf("") }
    var isContractTypeExpanded by remember { mutableStateOf(false) }

    var contractName by remember { mutableStateOf("") }
    var contractNumber by remember { mutableStateOf("") }
    var contractDescription by remember { mutableStateOf("") }

    // Dynamic Country and State / Region Selection
    var country by remember { mutableStateOf("India") }
    var region by remember { mutableStateOf("") }

    var selectedBranchId by remember { mutableStateOf("") }
    var isBranchExpanded by remember { mutableStateOf(false) }

    var legalEntity by remember { mutableStateOf("") }

    // ── Form State: 2. Parties ──
    var partyType by remember { mutableStateOf("Customer") }
    var isPartyTypeExpanded by remember { mutableStateOf(false) }

    var primaryParty by remember { mutableStateOf("") }
    var secondaryParty by remember { mutableStateOf("0") }

    var selectedInternalOwnerId by remember { mutableStateOf("") }
    var isInternalOwnerExpanded by remember { mutableStateOf(false) }

    var selectedLegalOwnerId by remember { mutableStateOf("") }
    var isLegalOwnerExpanded by remember { mutableStateOf(false) }

    // ── Form State: 3. Contract Terms ──
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var renewalType by remember { mutableStateOf("Manual Renewal") }
    var isRenewalTypeExpanded by remember { mutableStateOf(false) }

    var renewalNoticePeriod by remember { mutableStateOf("15 days") }
    var isRenewalNoticePeriodExpanded by remember { mutableStateOf(false) }

    var contractValue by remember { mutableStateOf("") }

    var currency by remember { mutableStateOf("INR") }
    var isCurrencyExpanded by remember { mutableStateOf(false) }

    var selectedPaymentTermsId by remember { mutableStateOf("") }
    var isPaymentTermsExpanded by remember { mutableStateOf(false) }

    var keyTermsAndConditions by remember { mutableStateOf("") }

    // ── Form State: 4. Risk & Classification ──
    var riskLevel by remember { mutableStateOf("Low") }
    var isRiskLevelExpanded by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("High") }
    var isPriorityExpanded by remember { mutableStateOf(false) }

    var confidentiality by remember { mutableStateOf("Internal") }
    var isConfidentialityExpanded by remember { mutableStateOf(false) }

    var contractCategory by remember { mutableStateOf("Commercial") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // ── Form State: 5. Documents ──
    val mainContractFiles = remember { mutableStateListOf<Uri>() }
    val supportingDocFiles = remember { mutableStateListOf<Uri>() }
    var version by remember { mutableIntStateOf(1) }
    var isSubmitting by remember { mutableStateOf(false) }

    // File pickers
    val mainContractPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (mainContractFiles.size < 5 && !mainContractFiles.contains(uri)) {
                mainContractFiles.add(uri)
            }
        }
    }

    val supportingDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (supportingDocFiles.size < 5 && !supportingDocFiles.contains(uri)) {
                supportingDocFiles.add(uri)
            }
        }
    }

    // ── Pre-fill on Edit Mode ──
    val contractDetail by viewModel.contractDetail.collectAsStateWithLifecycle()
    LaunchedEffect(contractIdToEdit) {
        if (!contractIdToEdit.isNullOrBlank()) {
            viewModel.loadContractDetail(contractIdToEdit)
        }
    }

    LaunchedEffect(contractDetail) {
        val c = contractDetail
        if (isEditMode && c != null && c.id == contractIdToEdit) {
            contractType = c.contractType.orEmpty()
            contractName = c.contractName.orEmpty()
            contractNumber = c.contractNumber.orEmpty()
            contractDescription = c.contractDescription.orEmpty()
            country = c.countryName ?: ""
            region = c.region.orEmpty()
            selectedBranchId = when {
                c.branchId?.isJsonObject == true -> c.branchId.asJsonObject.get("_id")?.asString.orEmpty()
                c.branchId?.isJsonPrimitive == true -> c.branchId.asString.orEmpty()
                else -> ""
            }
            legalEntity = c.legalEntity.orEmpty()

            partyType = c.partyType ?: ""
            primaryParty = c.primaryParty.orEmpty()
            secondaryParty = c.secondaryParty ?: "0"
            selectedInternalOwnerId = c.internalBusinessOwner?.id.orEmpty()
            selectedLegalOwnerId = c.legalOwner?.id.orEmpty()

            startDate = c.startDate?.take(10).orEmpty()
            endDate = c.endDate?.take(10).orEmpty()
            renewalType = c.renewalType ?: ""
            renewalNoticePeriod = "${c.renewalNoticePeriod} days"
            contractValue = c.contractValue.toString()
            currency = c.currency ?: "INR"
            selectedPaymentTermsId = c.paymentTerms?.id.orEmpty()
            keyTermsAndConditions = c.keyTermsAndConditions.orEmpty()

            riskLevel = c.riskLevel ?: ""
            priority = c.priority ?: ""
            confidentiality = c.confidentiality ?: ""
            contractCategory = c.contractCategory ?: ""
            version = c.version ?: 1
        }
    }

    // ── Dropdown Display Text Resolutions ──
    val selectedBranchName = branchList.find { it.id == selectedBranchId }?.name.orEmpty().ifBlank { "Select Branch..." }
    val selectedInternalOwnerName = memberList.find { it._id == selectedInternalOwnerId }?.displayName().orEmpty().ifBlank { "Select Internal Business Owner..." }
    val selectedLegalOwnerName = memberList.find { it._id == selectedLegalOwnerId }?.displayName().orEmpty().ifBlank { "Select Legal Owner..." }
    val selectedPaymentTermsName = paymentTermsList.find { it.id == selectedPaymentTermsId }?.name ?: "Select Payment Terms..."

    // ── API Submission Handler ──
    fun handleSubmit() {
        if (contractType.isBlank()) {
            Toast.makeText(context, "Contract type is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (contractName.isBlank()) {
            Toast.makeText(context, "Contract name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedBranchId.isBlank()) {
            Toast.makeText(context, "Branch is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (startDate.isBlank() || endDate.isBlank()) {
            Toast.makeText(context, "Start date and End date are required", Toast.LENGTH_SHORT).show()
            return
        }

        // For create mode, a main contract document is required
        if (!isEditMode && mainContractFiles.isEmpty()) {
            Toast.makeText(context, "Upload main contract document", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        // Build text/plain RequestBody fields exactly matching the update payload screenshot
        val fields = mutableMapOf<String, RequestBody>()
        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        val noticeDays = renewalNoticePeriod.replace(" days", "").trim().toIntOrNull() ?: 15

        // Format dates to standard yyyy-MM-dd expected by the server
        fun toSimpleDate(raw: String): String {
            val clean = raw.trim().replace("/", "-")
            val parts = clean.split("-")
            return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                clean
            }
        }

        // ── 1. Basic Information ──
        addField("contractType", contractType)
        addField("contractName", contractName)
        addField("contractDescription", contractDescription)
        addField("countryName", country)
        addField("region", region)
        addField("branchId", selectedBranchId)
        addField("legalEntity", legalEntity.ifBlank { "Nexus Boutique" })

        // ── 2. Parties ──
        addField("partyType", partyType)
        addField("primaryParty", primaryParty)
        addField("secondaryParty", secondaryParty.ifBlank { "0" })
        addField("internalBusinessOwner", selectedInternalOwnerId)
        addField("legalOwner", selectedLegalOwnerId)

        // ── 3. Terms ──
        addField("startDate", toSimpleDate(startDate))
        addField("endDate", toSimpleDate(endDate))
        addField("renewalType", renewalType)
        addField("renewalNoticePeriod", noticeDays.toString())
        addField("contractValue", contractValue.ifBlank { "0" })
        addField("currency", currency)
        addField("paymentTerms", selectedPaymentTermsId)
        addField("keyTermsAndConditions", keyTermsAndConditions)

        // ── 4. Risk & Classification ──
        addField("riskLevel", riskLevel)
        addField("priority", priority)
        addField("confidentiality", confidentiality)
        addField("contractCategory", contractCategory)

        // ── 5. Documents ──
        val mainDocParts = mainContractFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "mainDocuments")
        }
        val supportingDocParts = supportingDocFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "supportingDocuments")
        }

        if (isEditMode) {
            // Update Existing Contract
            viewModel.updateContract(
                contractId = contractIdToEdit,
                fields = fields,
                mainFiles = mainDocParts,
                supportingFiles = supportingDocParts,
                onSuccess = { _ ->
                    isSubmitting = false
                    Toast.makeText(context, "Legal contract updated successfully", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to update contract", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            // Create New Contract
            viewModel.createContract(
                fields = fields,
                mainFiles = mainDocParts,
                supportingFiles = supportingDocParts,
                onSuccess = { _ ->
                    isSubmitting = false
                    Toast.makeText(context, "Legal contract created successfully", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to create contract", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Contract" else "Create Contracts",
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
                        border = BorderStroke(1.dp, BorderGray),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
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
                                fontWeight = FontWeight.SemiBold,
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
            // ── Section 1: Basic Information ──
            ContractSectionCard(stepNumber = "1", title = "Basic Information") {
                FormDropdown(
                    label = "CONTRACT TYPE",
                    isRequired = true,
                    value = contractType,
                    expanded = isContractTypeExpanded,
                    onExpandChange = { isContractTypeExpanded = it },
                    options = contractTypeOptions,
                    onOptionSelected = { contractType = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "CONTRACT NAME", isRequired = true)
                    FormTextField(
                        value = contractName,
                        onValueChange = { contractName = it },
                        placeholder = "Enter contract name"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "CONTRACT NUMBER")
                    FormTextField(
                        value = contractNumber,
                        onValueChange = { contractNumber = it },
                        placeholder = "Auto-generated if empty",
                        enabled = false
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "CONTRACT DESCRIPTION")
                    FormTextArea(
                        value = contractDescription,
                        onValueChange = { contractDescription = it },
                        placeholder = "Describe the purpose and scope of this contract"
                    )
                }

                // Dynamic Country & State / Region Picker
                CountryAndStatePicker(
                    selectedCountry = country,
                    selectedState = region,
                    onCountryChange = { country = it },
                    onStateChange = { region = it }
                )

                FormDropdown(
                    label = "BRANCH",
                    isRequired = true,
                    value = selectedBranchName,
                    expanded = isBranchExpanded,
                    onExpandChange = { isBranchExpanded = it },
                    options = branchList.map { it.name.orEmpty() },
                    onOptionSelected = { name ->
                        selectedBranchId = branchList.find { it.name == name }?.id.orEmpty()
                    }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "LEGAL ENTITY", isRequired = true)
                    FormTextField(
                        value = legalEntity.ifBlank { "Nexus Boutique" },
                        onValueChange = { legalEntity = it },
                        placeholder = "Enter legal entity name"
                    )
                }
            }

            // ── Section 2: Parties ──
            ContractSectionCard(stepNumber = "2", title = "Parties") {
                FormDropdown(
                    label = "PARTY TYPE",
                    isRequired = true,
                    value = partyType,
                    expanded = isPartyTypeExpanded,
                    onExpandChange = { isPartyTypeExpanded = it },
                    options = partyTypeOptions,
                    onOptionSelected = { partyType = it }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "PARTY / CUSTOMER / VENDOR", isRequired = true)
                    FormTextField(
                        value = primaryParty,
                        onValueChange = { primaryParty = it },
                        placeholder = "Enter or search party name"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "SECONDARY PARTY")
                    FormTextField(
                        value = secondaryParty,
                        onValueChange = { secondaryParty = it },
                        placeholder = "Optional secondary party"
                    )
                }

                FormDropdown(
                    label = "INTERNAL BUSINESS OWNER",
                    isRequired = true,
                    value = selectedInternalOwnerName,
                    expanded = isInternalOwnerExpanded,
                    onExpandChange = { isInternalOwnerExpanded = it },
                    options = memberList.map { it.displayName() },
                    onOptionSelected = { selectedName ->
                        selectedInternalOwnerId = memberList.find { it.displayName() == selectedName }?._id.orEmpty()
                    }
                )

                FormDropdown(
                    label = "LEGAL OWNER",
                    isRequired = true,
                    value = selectedLegalOwnerName,
                    expanded = isLegalOwnerExpanded,
                    onExpandChange = { isLegalOwnerExpanded = it },
                    options = memberList.map { it.displayName() },
                    onOptionSelected = { selectedName ->
                        selectedLegalOwnerId = memberList.find { it.displayName() == selectedName }?._id.orEmpty()
                    }
                )
            }

            // ── Section 3: Contract Terms ──
            ContractSectionCard(stepNumber = "3", title = "Contract Terms") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "START DATE", isRequired = true)
                        DatePickerField(
                            value = startDate,
                            onDateSelected = { startDate = it }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "END DATE", isRequired = true)
                        DatePickerField(
                            value = endDate,
                            onDateSelected = { endDate = it }
                        )
                    }
                }

                FormDropdown(
                    label = "RENEWAL TYPE",
                    isRequired = true,
                    value = renewalType,
                    expanded = isRenewalTypeExpanded,
                    onExpandChange = { isRenewalTypeExpanded = it },
                    options = renewalTypeOptions,
                    onOptionSelected = { renewalType = it }
                )

                FormDropdown(
                    label = "RENEWAL NOTICE PERIOD",
                    isRequired = true,
                    value = renewalNoticePeriod,
                    expanded = isRenewalNoticePeriodExpanded,
                    onExpandChange = { isRenewalNoticePeriodExpanded = it },
                    options = noticePeriodOptions,
                    onOptionSelected = { renewalNoticePeriod = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        FormLabel(text = "CONTRACT VALUE")
                        FormTextField(
                            value = contractValue,
                            onValueChange = { contractValue = it },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    FormDropdown(
                        modifier = Modifier.weight(1f),
                        label = "CURRENCY",
                        isRequired = true,
                        value = currency,
                        expanded = isCurrencyExpanded,
                        onExpandChange = { isCurrencyExpanded = it },
                        options = currencyOptions,
                        onOptionSelected = { currency = it }
                    )
                }

                FormDropdown(
                    label = "PAYMENT TERMS",
                    isRequired = true,
                    value = selectedPaymentTermsName,
                    expanded = isPaymentTermsExpanded,
                    onExpandChange = { isPaymentTermsExpanded = it },
                    options = paymentTermsList.map { it.name },
                    onOptionSelected = { name ->
                        selectedPaymentTermsId = paymentTermsList.find { it.name == name }?.id.orEmpty()
                    }
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "KEY TERMS & CONDITIONS")
                    FormTextArea(
                        value = keyTermsAndConditions,
                        onValueChange = { keyTermsAndConditions = it },
                        placeholder = "Enter key terms, obligations, and conditions"
                    )
                }
            }

            // ── Section 4: Risk & Classification ──
            ContractSectionCard(stepNumber = "4", title = "Risk & Classification") {
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
                    label = "CONTRACT CATEGORY",
                    isRequired = true,
                    value = contractCategory,
                    expanded = isCategoryExpanded,
                    onExpandChange = { isCategoryExpanded = it },
                    options = categoryOptions,
                    onOptionSelected = { contractCategory = it }
                )
            }

            // ── Section 5: Documents ──
            ContractSectionCard(stepNumber = "5", title = "Documents") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "UPLOAD MAIN CONTRACT", isRequired = true)
                    ImageUploadSection(
                        selectedImages = mainContractFiles,
                        onBrowseClick = { mainContractPicker.launch("*/*") },
                        onRemoveImage = { file -> mainContractFiles.remove(file) },
                        maxFiles = 5,
                        subtitle = "PDF, JPG, PNG or WEBP up to 5MB",
                        supportedFormatsText = "Max 5 files (Images auto-compressed to <5MB)"
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "VERSION")
                    FormTextField(
                        value = if (isEditMode) "v$version" else "1",
                        onValueChange = {},
                        placeholder = "1",
                        enabled = false
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "SUPPORTING DOCUMENTS")
                    ImageUploadSection(
                        selectedImages = supportingDocFiles,
                        onBrowseClick = { supportingDocPicker.launch("*/*") },
                        onRemoveImage = { file -> supportingDocFiles.remove(file) },
                        maxFiles = 5,
                        subtitle = "PDF, JPG, PNG or WEBP up to 5MB",
                        supportedFormatsText = "Max 5 files (Images auto-compressed to <5MB)"
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Section Container Card with Numbered Circle
// ─────────────────────────────────────────────────────────────

@Composable
private fun ContractSectionCard(
    stepNumber: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
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
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }

            HorizontalDivider(color = light_grey, thickness = 1.dp)

            content()
        }
    }
}