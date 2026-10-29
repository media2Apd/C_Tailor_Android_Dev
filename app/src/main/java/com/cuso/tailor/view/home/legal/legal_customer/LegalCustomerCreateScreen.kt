@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.model.legal.LegalCustomerDocumentDto
import com.cuso.tailor.model.settings.BranchItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateLegalCustomerScreen(
    onClose: () -> Unit,
    recordIdToEdit: String? = null,
    viewModel: LegalViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    orderViewModel: SalesOrderViewModel = hiltViewModel(),
    financeViewModel: FinanceViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !recordIdToEdit.isNullOrBlank()

    // ── External Data Fetching Observables ──
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branchList: List<BranchItem> = remember(branchState) {
        when (val state = branchState) {
            is BranchUiState.Success -> state.branches
            else -> emptyList()
        }
    }

    val memberList by hrViewModel.members.collectAsStateWithLifecycle()
    val contractList by viewModel.contracts.collectAsStateWithLifecycle()

    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val customerList = remember(customerUiState) {
        when (val state = customerUiState) {
            is CustomerUiState.Success -> state.customers
            else -> emptyList()
        }
    }

    val orderUiState by orderViewModel.orderState.collectAsStateWithLifecycle()
    val rawOrders = remember(orderUiState) {
        when (val state = orderUiState) {
            is OrderUiState.Success -> state.orders
            else -> emptyList()
        }
    }

    val rawInvoices by financeViewModel.invoiceList.collectAsStateWithLifecycle()

    val customerDetail by viewModel.customerDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingCustomerDetail.collectAsStateWithLifecycle()

    // ── Static Dropdown Options (Matching React) ──
    val legalMatterTypes = remember { listOf("Enterprise", "Retail", "Wholesale", "Franchise", "Distributor", "Other") }
    val consentTypes = remember { listOf("Signed", "Pending", "Expired", "Withdrawn", "Not Required") }
    val termsTypes = remember { listOf("Standard", "Custom", "Custom MSA", "Franchise Agreement", "Distribution Agreement") }
    val riskLevels = remember { listOf("Low", "Medium", "High") }
    val priorities = remember { listOf("Low", "Medium", "High", "Critical") }
    val countries = remember {
        listOf("India", "United States", "United Kingdom", "United Arab Emirates", "Singapore", "Germany", "Australia")
    }
    val disputeReferences = remember {
        listOf("Contract Dispute", "Property Dispute", "Employment Dispute", "Payment Dispute", "Commercial Dispute", "Other")
    }

    // ── Form Input States ──
    var customerId by remember { mutableStateOf("") }
    var customerCode by remember { mutableStateOf("") }
    var selectedCustomerName by remember { mutableStateOf("") }
    var isCustomerExpanded by remember { mutableStateOf(false) }

    var legalMatterType by remember { mutableStateOf("") }
    var isLegalMatterExpanded by remember { mutableStateOf(false) }

    var orderReference by remember { mutableStateOf("") }
    var isOrderExpanded by remember { mutableStateOf(false) }

    var invoiceReference by remember { mutableStateOf("") }
    var isInvoiceExpanded by remember { mutableStateOf(false) }

    var contractId by remember { mutableStateOf("") }
    var isContractExpanded by remember { mutableStateOf(false) }

    var consentType by remember { mutableStateOf("") }
    var isConsentExpanded by remember { mutableStateOf(false) }

    var termsType by remember { mutableStateOf("") }
    var isTermsExpanded by remember { mutableStateOf(false) }

    var cancellationTerms by remember { mutableStateOf("") }
    var refundTerms by remember { mutableStateOf("") }

    var effectiveDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }

    var disputeReference by remember { mutableStateOf("") }
    var isDisputeExpanded by remember { mutableStateOf(false) }

    var legalDescription by remember { mutableStateOf("") }

    var riskLevel by remember { mutableStateOf("") }
    var isRiskExpanded by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("") }
    var isPriorityExpanded by remember { mutableStateOf(false) }

    var countryName by remember { mutableStateOf("") }
    var isCountryExpanded by remember { mutableStateOf(false) }

    var branchId by remember { mutableStateOf("") }
    var isBranchExpanded by remember { mutableStateOf(false) }

    var legalOwner by remember { mutableStateOf("") }
    var isLegalOwnerExpanded by remember { mutableStateOf(false) }

    // ── Document Binary Files Pickers (Max 5 files each, <= 5MB) ──
    val supportingDocuments = remember { mutableStateListOf<Uri>() }
    val consentDocuments = remember { mutableStateListOf<Uri>() }
    val legalCorrespondence = remember { mutableStateListOf<Uri>() }

    // Existing Documents from API (Edit mode)
    var existingSupportingDocs by remember { mutableStateOf<List<LegalCustomerDocumentDto>>(emptyList()) }
    var existingConsentDocs by remember { mutableStateOf<List<LegalCustomerDocumentDto>>(emptyList()) }
    var existingCorrespondenceDocs by remember { mutableStateOf<List<LegalCustomerDocumentDto>>(emptyList()) }

    var isSubmitting by remember { mutableStateOf(false) }

    val supportingDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { if (supportingDocuments.size < 5 && !supportingDocuments.contains(it)) supportingDocuments.add(it) }
    }
    val consentDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { if (consentDocuments.size < 5 && !consentDocuments.contains(it)) consentDocuments.add(it) }
    }
    val correspondenceDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { if (legalCorrespondence.size < 5 && !legalCorrespondence.contains(it)) legalCorrespondence.add(it) }
    }

    // ── Filter Orders and Invoices by Selected Customer ──
    val customerOrders = remember(customerId, rawOrders) {
        if (customerId.isBlank()) rawOrders
        else rawOrders.filter { order ->
            order.customerId?.id == customerId || order.customerId?.id == customerId
        }
    }

    val customerInvoices = remember(customerId, rawInvoices) {
        if (customerId.isBlank()) rawInvoices
        else rawInvoices.filter { invoice ->
            invoice.customerId?.id == customerId || invoice.customerId?.id == customerId
        }
    }

    // ── Fetch Initial Dependencies ──
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers(page = 1, limit = 100)
        customerViewModel.loadCustomers(isRefresh = true)
        orderViewModel.fetchOrders(page = 1, limit = 100)
        financeViewModel.fetchInvoices(page = 1, limit = 100)
        viewModel.fetchContracts(page = 1, limit = 100)
    }

    LaunchedEffect(recordIdToEdit) {
        if (isEditMode) {
            viewModel.fetchLegalCustomerDetail(recordIdToEdit)
        } else {
            viewModel.clearLegalCustomerDetail()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalCustomerDetail()
        }
    }

    // ── Pre-fill Fields in Edit Mode ──
    LaunchedEffect(customerDetail) {
        customerDetail?.let { detail ->
            customerId = detail.customerId?.id.orEmpty().ifBlank { detail.customerId?.id.orEmpty() }
            customerCode = detail.customerCode.orEmpty()
            selectedCustomerName = detail.customerId?.name ?: customerList.find { it.id == customerId }?.name.orEmpty()

            legalMatterType = detail.legalMatterType.orEmpty()
            orderReference = detail.orderReference.orEmpty()
            invoiceReference = detail.invoiceReference.orEmpty()
            contractId = detail.contractId?.id.orEmpty()

            consentType = detail.consentType.orEmpty()
            termsType = detail.termsType.orEmpty()
            cancellationTerms = detail.cancellationTerms.orEmpty()
            refundTerms = detail.refundTerms.orEmpty()

            effectiveDate = detail.effectiveDate?.take(10).orEmpty()
            expiryDate = detail.expiryDate?.take(10).orEmpty()

            disputeReference = detail.disputeReference.orEmpty()
            legalDescription = detail.legalDescription.orEmpty()
            riskLevel = detail.riskLevel.orEmpty()
            priority = detail.priority.orEmpty()
            countryName = detail.countryName.orEmpty()

            branchId = detail.branchId?.id.orEmpty()
            legalOwner = detail.legalOwner?.id.orEmpty()

            existingSupportingDocs = detail.supportingDocuments.orEmpty()
            existingConsentDocs = detail.consentDocuments.orEmpty()
            existingCorrespondenceDocs = detail.legalCorrespondence.orEmpty()
        }
    }

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

    fun getContractLabel(c: LegalContractDto): String {
        return listOfNotNull(c.contractNumber, c.contractName).filter { it.isNotBlank() }.joinToString(" - ")
    }

    val selectedBranchName = branchList.find { it.id == branchId }?.name.orEmpty().ifBlank { "Select Branch..." }
    val selectedContractName = contractList.find { it.id == contractId }?.let { getContractLabel(it) }.orEmpty().ifBlank { "Select Contract..." }
    val selectedLegalOwnerName = memberList.find { it._id == legalOwner }?.displayName().orEmpty().ifBlank { "Select Legal Owner..." }
    fun formatOrderLabel(orderId: String?): String {
        if (orderId.isNullOrBlank()) return "Select order..."
        val clean = orderId.trim()
        val shortId = if (clean.length >= 6) clean.takeLast(6) else clean
        return "#$shortId"
    }

    val selectedOrderName = if (orderReference.isNotBlank()) {
        formatOrderLabel(orderReference)
    } else {
        "Select order..."
    }

    val orderDropdownOptions = remember(customerOrders) {
        listOf("Select order...") + customerOrders.map { order ->
            val id = order.id.ifBlank { "Order Not Found" }
            formatOrderLabel(id)
        }
    }
    val selectedInvoiceName = customerInvoices.find { it.id == invoiceReference }?.invoiceNumber.orEmpty().ifBlank { "Select Invoice (Optional)..." }

    // ── Validation matching React ──
    fun validate(): Boolean {
        if (customerId.isBlank()) {
            Toast.makeText(context, "Customer is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (legalMatterType.isBlank()) {
            Toast.makeText(context, "Legal matter type is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (legalOwner.isBlank()) {
            Toast.makeText(context, "Legal owner is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (disputeReference.isBlank()) {
            Toast.makeText(context, "Dispute reference is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (effectiveDate.isNotBlank() && expiryDate.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val eff = sdf.parse(toSimpleDate(effectiveDate))
                val exp = sdf.parse(toSimpleDate(expiryDate))
                if (eff != null && exp != null && exp.before(eff)) {
                    Toast.makeText(context, "Expiry date must be after the effective date", Toast.LENGTH_SHORT).show()
                    return false
                }
            } catch (_: Exception) {}
        }
        return true
    }

    // ── Submit Handling (Create & Update multipart) ──
    fun handleSubmit() {
        if (!validate()) return

        isSubmitting = true

        val fields = mutableMapOf<String, RequestBody>()
        fun addField(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        // Exact keys matching React initialFormState
        addField("customerId", customerId)
        addField("legalMatterType", legalMatterType)
        addField("orderReference", orderReference)
        addField("invoiceReference", invoiceReference)
        addField("contractId", contractId)
        addField("consentType", consentType)
        addField("termsType", termsType)
        addField("cancellationTerms", cancellationTerms)
        addField("refundTerms", refundTerms)
        addField("effectiveDate", toSimpleDate(effectiveDate))
        addField("expiryDate", toSimpleDate(expiryDate))
        addField("disputeReference", disputeReference)
        addField("legalDescription", legalDescription)
        addField("riskLevel", riskLevel)
        addField("priority", priority)
        addField("countryName", countryName)
        addField("branchId", branchId)
        addField("legalOwner", legalOwner)

        // Multi-category Multipart Documents
        val supportingParts = supportingDocuments.mapNotNull { uriToMultipartPart(context, it, "supportingDocuments") }
        val consentParts = consentDocuments.mapNotNull { uriToMultipartPart(context, it, "consentDocuments") }
        val correspondenceParts = legalCorrespondence.mapNotNull { uriToMultipartPart(context, it, "legalCorrespondence") }

        if (isEditMode) {
            viewModel.updateLegalCustomer(
                recordId = recordIdToEdit,
                fields = fields,
                supportingDocuments = supportingParts,
                consentDocuments = consentParts,
                legalCorrespondence = correspondenceParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Customer legal record updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalCustomers()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to update record", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            viewModel.createLegalCustomer(
                fields = fields,
                supportingDocuments = supportingParts,
                consentDocuments = consentParts,
                legalCorrespondence = correspondenceParts,
                onSuccess = {
                    isSubmitting = false
                    Toast.makeText(context, "Customer legal record created successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLegalCustomers()
                    onClose()
                },
                onError = { err ->
                    isSubmitting = false
                    Toast.makeText(context, err ?: "Failed to create record", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Update Record" else "Create Records",
                subtitle = customerDetail?.legalCustomerNumber ?: "Back to Legal Management",
                onClose = onClose
            )
        },
        bottomBar = {
            Surface(color = whiteBg, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.weight(1.3f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        enabled = !isSubmitting && !isLoadingDetail
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = whiteBg, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Spacer(Modifier.width(6.dp))
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
        if (isLoadingDetail && isEditMode) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
            ) {
                // ── Section 1: Customer Reference ──
                CustomerSectionCard(stepNumber = "1", title = "Customer Reference") {
                    FormDropdown(
                        label = "CUSTOMER",
                        isRequired = true,
                        value = selectedCustomerName.ifBlank { "Select customer..." },
                        expanded = isCustomerExpanded,
                        onExpandChange = { isCustomerExpanded = it },
                        options = customerList.map { it.name },
                        onOptionSelected = { name ->
                            val item = customerList.find { it.name == name }
                            customerId = item?.id.orEmpty()
                            selectedCustomerName = name
                            customerCode = item?.id.orEmpty()
                        }
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "CUSTOMER ID")
                        FormTextField(
                            value = customerCode,
                            onValueChange = {},
                            placeholder = "Filled from the customer",
                            enabled = false
                        )
                    }

                    FormDropdown(
                        label = "LEGAL MATTER TYPE",
                        isRequired = true,
                        value = legalMatterType.ifBlank { "Select Legal Matter Type..." },
                        expanded = isLegalMatterExpanded,
                        onExpandChange = { isLegalMatterExpanded = it },
                        options = legalMatterTypes,
                        onOptionSelected = { legalMatterType = it }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "ORDER REFERENCE",
                            value = selectedOrderName,
                            expanded = isOrderExpanded,
                            onExpandChange = { isOrderExpanded = it },
                            options = orderDropdownOptions,
                            onOptionSelected = { selectedLabel ->
                                if (selectedLabel == "Select order...") {
                                    orderReference = ""
                                } else {
                                    val matchedOrder = customerOrders.find { order ->
                                        formatOrderLabel(order.id) == selectedLabel
                                    }
                                    orderReference = matchedOrder?.id.orEmpty()
                                }
                            }
                        )

                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "INVOICE REFERENCE",
                            value = selectedInvoiceName,
                            expanded = isInvoiceExpanded,
                            onExpandChange = { isInvoiceExpanded = it },
                            options = customerInvoices.map { it.invoiceNumber },
                            onOptionSelected = { invNo ->
                                invoiceReference = customerInvoices.find { it.invoiceNumber == invNo }?.id.orEmpty()
                            }
                        )
                    }

                    FormDropdown(
                        label = "CONTRACT REFERENCE",
                        value = selectedContractName,
                        expanded = isContractExpanded,
                        onExpandChange = { isContractExpanded = it },
                        options = contractList.map { getContractLabel(it) },
                        onOptionSelected = { label ->
                            contractId = contractList.find { getContractLabel(it) == label }?.id.orEmpty()
                        }
                    )
                }

                // ── Section 2: Legal Terms ──
                CustomerSectionCard(stepNumber = "2", title = "Legal Terms") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "CONSENT TYPE",
                            value = consentType.ifBlank { "Select Consent Type..." },
                            expanded = isConsentExpanded,
                            onExpandChange = { isConsentExpanded = it },
                            options = consentTypes,
                            onOptionSelected = { consentType = it }
                        )

                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "TERMS TYPE",
                            value = termsType.ifBlank { "Select Terms Type..." },
                            expanded = isTermsExpanded,
                            onExpandChange = { isTermsExpanded = it },
                            options = termsTypes,
                            onOptionSelected = { termsType = it }
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "CANCELLATION TERMS")
                        FormTextArea(
                            value = cancellationTerms,
                            onValueChange = { cancellationTerms = it },
                            placeholder = "Cancellation terms agreed with the customer"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "REFUND TERMS")
                        FormTextArea(
                            value = refundTerms,
                            onValueChange = { refundTerms = it },
                            placeholder = "Refund terms agreed with the customer"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("EFFECTIVE DATE")
                            DatePickerField(value = effectiveDate, onDateSelected = { effectiveDate = it })
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("EXPIRY DATE")
                            DatePickerField(value = expiryDate, onDateSelected = { expiryDate = it })
                        }
                    }
                }

                // ── Section 3: Legal Matter ──
                CustomerSectionCard(stepNumber = "3", title = "Legal Matter") {
                    FormDropdown(
                        label = "DISPUTE REFERENCE",
                        isRequired = true,
                        value = disputeReference.ifBlank { "Select Dispute Reference..." },
                        expanded = isDisputeExpanded,
                        onExpandChange = { isDisputeExpanded = it },
                        options = disputeReferences,
                        onOptionSelected = { disputeReference = it }
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LEGAL DESCRIPTION")
                        FormTextArea(
                            value = legalDescription,
                            onValueChange = { legalDescription = it },
                            placeholder = "Describe the legal matter in detail"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "RISK LEVEL",
                            value = riskLevel.ifBlank { "Select Risk Level..." },
                            expanded = isRiskExpanded,
                            onExpandChange = { isRiskExpanded = it },
                            options = riskLevels,
                            onOptionSelected = { riskLevel = it }
                        )

                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "PRIORITY",
                            value = priority.ifBlank { "Select Priority..." },
                            expanded = isPriorityExpanded,
                            onExpandChange = { isPriorityExpanded = it },
                            options = priorities,
                            onOptionSelected = { priority = it }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "COUNTRY",
                            value = countryName.ifBlank { "Select Country..." },
                            expanded = isCountryExpanded,
                            onExpandChange = { isCountryExpanded = it },
                            options = countries,
                            onOptionSelected = { countryName = it }
                        )

                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "BRANCH",
                            value = selectedBranchName,
                            expanded = isBranchExpanded,
                            onExpandChange = { isBranchExpanded = it },
                            options = branchList.map { it.name.orEmpty() },
                            onOptionSelected = { name ->
                                val b = branchList.find { it.name == name }
                                branchId = b?.id.orEmpty()
                            }
                        )
                    }

                    FormDropdown(
                        label = "LEGAL OWNER",
                        isRequired = true,
                        value = selectedLegalOwnerName,
                        expanded = isLegalOwnerExpanded,
                        onExpandChange = { isLegalOwnerExpanded = it },
                        options = memberList.map { it.displayName() },
                        onOptionSelected = { name ->
                            legalOwner = memberList.find { it.displayName() == name }?._id.orEmpty()
                        }
                    )
                }

                // ── Section 4: Documents (Supporting, Consent, Correspondence) ──
                CustomerSectionCard(stepNumber = "4", title = "Documents") {
                    // Supporting Documents
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "SUPPORTING DOCUMENTS")
                        if (existingSupportingDocs.isNotEmpty()) {
                            ExistingFilesView(files = existingSupportingDocs)
                            Spacer(Modifier.height(8.dp))
                        }
                        ImageUploadSection(
                            selectedImages = supportingDocuments,
                            onBrowseClick = { supportingDocPicker.launch("*/*") },
                            onRemoveImage = { supportingDocuments.remove(it) },
                            maxFiles = 5,
                            title = "Supporting Documents",
                            subtitle = "Upload PDF, JPG, PNG or WEBP up to 5 MB",
                            supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                        )
                    }

                    // Consent Documents
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "CONSENT DOCUMENTS")
                        if (existingConsentDocs.isNotEmpty()) {
                            ExistingFilesView(files = existingConsentDocs)
                            Spacer(Modifier.height(8.dp))
                        }
                        ImageUploadSection(
                            selectedImages = consentDocuments,
                            onBrowseClick = { consentDocPicker.launch("*/*") },
                            onRemoveImage = { consentDocuments.remove(it) },
                            maxFiles = 5,
                            title = "Consent Documents",
                            subtitle = "Upload PDF, JPG, PNG or WEBP up to 5 MB",
                            supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                        )
                    }

                    // Legal Correspondence
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "LEGAL CORRESPONDENCE")
                        if (existingCorrespondenceDocs.isNotEmpty()) {
                            ExistingFilesView(files = existingCorrespondenceDocs)
                            Spacer(Modifier.height(8.dp))
                        }
                        ImageUploadSection(
                            selectedImages = legalCorrespondence,
                            onBrowseClick = { correspondenceDocPicker.launch("*/*") },
                            onRemoveImage = { legalCorrespondence.remove(it) },
                            maxFiles = 5,
                            title = "Legal Correspondence",
                            subtitle = "Upload PDF, JPG, PNG or WEBP up to 5 MB",
                            supportedFormatsText = "PDF, JPG, PNG or WEBP up to 5 MB"
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Existing Files Display Helper Composable
// ─────────────────────────────────────────────────────────────

@Composable
private fun ExistingFilesView(files: List<LegalCustomerDocumentDto>) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        files.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeGrey)
                    .clickable {
                        if (!file.documentUrl.isNullOrBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, file.documentUrl.toUri()))
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = file.documentName ?: "Document",
                        fontSize = tokens.caption,
                        color = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open file",
                    tint = headerGrey,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerSectionCard(
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