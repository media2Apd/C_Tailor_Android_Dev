@file:Suppress("VariableNeverRead", "UNUSED_VALUE", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.sales.oppertunity.oppertunities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.CreateGarmentSpecPayload
import com.cuso.tailor.model.sales.CreateOpportunityRequest
import com.cuso.tailor.model.sales.GarmentCategoryDto
import com.cuso.tailor.model.settings.GarmentItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatLeadDate
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.CustomerUiState
import com.cuso.tailor.viewmodel.CustomerViewModel
import com.cuso.tailor.viewmodel.OpportunityActionState
import com.cuso.tailor.viewmodel.OpportunityDetailUiState
import com.cuso.tailor.viewmodel.OpportunityViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

enum class ClientType {
    CUSTOMER, LEAD
}

data class GarmentSpecificationRow(
    val templateId: String = "",
    val templateName: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val quantity: String = "1",
    val availableCategories: List<GarmentCategoryDto> = emptyList(),
    val isLoadingCategories: Boolean = false
)

@Composable
fun CreateOpportunityScreen(
    opportunityId: String? = null,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    opportunityViewModel: OpportunityViewModel = hiltViewModel(),
    salesViewModel: SalesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !opportunityId.isNullOrBlank()

    // Trigger initial API calls on screen load
    LaunchedEffect(opportunityId) {
        branchViewModel.loadBranches()
        salesViewModel.fetchStaff()
        salesViewModel.fetchTableLeads()
        settingsViewModel.fetchGarments()
        customerViewModel.loadCustomers()
        opportunityViewModel.fetchOpportunityStages()

        if (isEditMode && opportunityId.isNotBlank()) {
            opportunityViewModel.loadOpportunityDetail(opportunityId)
        }
    }

    // State observers
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val tableLeads by salesViewModel.tableLeads.collectAsStateWithLifecycle()
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val allGarments by settingsViewModel.garments.collectAsStateWithLifecycle()
    val apiStages by opportunityViewModel.opportunityStages.collectAsStateWithLifecycle()
    val createState by opportunityViewModel.createState.collectAsStateWithLifecycle()
    val detailState by opportunityViewModel.detailState.collectAsStateWithLifecycle()

    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Active garment options from settings
    val activeGarments: List<GarmentItem> = remember(allGarments) {
        allGarments.filter { it.status.equals("Active", ignoreCase = true) }
    }
    val garmentTemplateOptions: List<String> = remember(activeGarments) {
        activeGarments.mapNotNull { it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.name.takeIf { n -> n.isNotBlank() } }
    }

    // Branch options
    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()
    val branchDisplayOptions: List<String> = remember(branches) {
        branches.mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
    }

    // Customer options
    val customers = (customerUiState as? CustomerUiState.Success)?.customers ?: emptyList()
    val customerDisplayOptions: List<String> = remember(customers) {
        customers.map { "${it.name} (${it.mobile ?: "—"})" }
    }

    // Filter out converted leads so only unconverted leads appear in client identification
    val unconvertedLeads = remember(tableLeads) {
        tableLeads.filter {
            it.isConverted != true && !it.effectiveStatus.contains("Convert", ignoreCase = true)
        }
    }
    val leadDisplayOptions: List<String> = remember(unconvertedLeads) {
        unconvertedLeads.map { "${it.name} (${it.phone})" }
    }

    // Staff salesperson options
    val staffOptions: List<String> = remember(staffList) {
        staffList.map { "${it.firstName} ${it.lastName}".trim() }
    }
    val staffIdMap = remember(staffList) {
        staffList.associate { "${it.firstName} ${it.lastName}".trim() to it.id }
    }

    // Stage options from API
    val stageOptions: List<String> = remember(apiStages) {
        apiStages.map { it.name }.ifEmpty {
            listOf("New Opportunity", "Qualification", "Proposal/Quotation", "Negotiation", "Closed Won", "Closed Lost")
        }
    }

    // Form fields
    var selectedClientType by remember { mutableStateOf(ClientType.CUSTOMER) }
    var selectedClientId by remember { mutableStateOf("") }
    var selectedClientDisplayName by remember { mutableStateOf("") }

    var opportunityName by remember { mutableStateOf("") }
    var selectedBranchId by remember { mutableStateOf("") }
    var selectedBranchName by remember { mutableStateOf("") }

    var selectedStageId by remember { mutableStateOf("") }
    var opportunityStage by remember { mutableStateOf("") }
    var estimatedValue by remember { mutableStateOf("") }

    var selectedSalespersonId by remember { mutableStateOf("") }
    var selectedSalespersonName by remember { mutableStateOf("") }

    var expectedClosingDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var garmentSpecs by remember {
        mutableStateOf(listOf(GarmentSpecificationRow()))
    }

    // Prefill form in edit mode from detail API response
    LaunchedEffect(detailState) {
        if (isEditMode && detailState is OpportunityDetailUiState.Success) {
            val item = (detailState as OpportunityDetailUiState.Success).item
            opportunityName = item.name.orEmpty()
            selectedBranchId = item.branch?.id.orEmpty()
            selectedBranchName = item.branch?.name.orEmpty()
            selectedStageId = item.stage?.id.orEmpty()
            opportunityStage = item.stage?.name ?: "New Opportunity"
            estimatedValue = item.estimatedValue?.toLong()?.toString().orEmpty()
            selectedSalespersonId = item.salesperson?.id.orEmpty()
            selectedSalespersonName = "${item.salesperson?.firstName.orEmpty()} ${item.salesperson?.lastName.orEmpty()}".trim()
            expectedClosingDate = formatLeadDate(item.expectedClosingDate)
            notes = item.notes.orEmpty()

            if (item.customer != null) {
                selectedClientType = ClientType.CUSTOMER
                selectedClientId = item.customer.id
                selectedClientDisplayName = "${item.customer.fullName} (${item.customer.mobileNumber ?: "—"})"
            }

            // Populate garment specifications and fetch categories for each garment
            item.garmentSpecifications?.takeIf { it.isNotEmpty() }?.let { specs ->
                val initialSpecs = specs.map { spec ->
                    val gId = spec.garment?.id.orEmpty()
                    GarmentSpecificationRow(
                        templateId = gId,
                        templateName = spec.garment?.displayName ?: spec.garment?.name.orEmpty(),
                        categoryId = spec.garmentCategory?.id.orEmpty(),
                        categoryName = spec.garmentCategory?.displayName ?: spec.garmentCategory?.name.orEmpty(),
                        quantity = spec.quantity.toString(),
                        isLoadingCategories = gId.isNotBlank()
                    )
                }
                garmentSpecs = initialSpecs

                specs.forEachIndexed { index, spec ->
                    val garmentId = spec.garment?.id.orEmpty()
                    if (garmentId.isNotBlank()) {
                        salesViewModel.fetchGarmentCategoriesByGarmentId(garmentId) { categories ->
                            garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                if (i == index) row.copy(
                                    availableCategories = categories,
                                    isLoadingCategories = false
                                ) else row
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto-select defaults in create mode
    LaunchedEffect(branches) {
        if (!isEditMode && selectedBranchId.isBlank() && branches.isNotEmpty()) {
            selectedBranchId = branches.first().id
            selectedBranchName = branches.first().name.orEmpty()
        }
    }
    LaunchedEffect(apiStages) {
        if (!isEditMode && selectedStageId.isBlank() && apiStages.isNotEmpty()) {
            selectedStageId = apiStages.first().id
            opportunityStage = apiStages.first().name
        }
    }
    LaunchedEffect(staffList) {
        if (!isEditMode && selectedSalespersonId.isBlank() && staffList.isNotEmpty()) {
            selectedSalespersonId = staffList.first().id
            selectedSalespersonName = "${staffList.first().firstName} ${staffList.first().lastName}".trim()
        }
    }

    // Handle submit/action state
    LaunchedEffect(createState) {
        when (createState) {
            is OpportunityActionState.Success -> {
                successMessage = (createState as OpportunityActionState.Success).message
                delay(800)
                opportunityViewModel.resetActionStates()
                onSubmit()
            }
            is OpportunityActionState.Error -> {
                validationError = (createState as OpportunityActionState.Error).message
                opportunityViewModel.resetActionStates()
            }
            else -> Unit
        }
    }

    // Dropdown expansion state
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }
    var stageDropdownExpanded by remember { mutableStateOf(false) }
    var salespersonDropdownExpanded by remember { mutableStateOf(false) }

    fun handleSaveOpportunity() {
        if (selectedClientId.isBlank()) {
            validationError = "Please select a ${if (selectedClientType == ClientType.CUSTOMER) "customer" else "lead"}"
            return
        }
        if (opportunityName.isBlank()) {
            validationError = "Opportunity Name is required"
            return
        }
        if (selectedBranchId.isBlank()) {
            validationError = "Please select a branch location"
            return
        }
        if (selectedStageId.isBlank()) {
            validationError = "Please select an opportunity stage"
            return
        }
        if (selectedSalespersonId.isBlank()) {
            validationError = "Please select an assigned salesperson"
            return
        }
        if (estimatedValue.isBlank()) {
            validationError = "Estimated Deal Value is required"
            return
        }
        if (expectedClosingDate.isBlank()) {
            validationError = "Expected Closing Date is required"
            return
        }

        val parsedAmount = estimatedValue.toDoubleOrNull() ?: 0.0

        val garmentPayloads = garmentSpecs.mapNotNull { row ->
            if (row.templateId.isNotBlank() && row.categoryId.isNotBlank()) {
                CreateGarmentSpecPayload(
                    garmentId = row.templateId,
                    garmentCategoryId = row.categoryId,
                    quantity = row.quantity.toIntOrNull() ?: 1
                )
            } else null
        }

        if (garmentPayloads.isEmpty()) {
            validationError = "Please add at least one complete garment specification"
            return
        }

        val formattedDate = expectedClosingDate.toIsoDate().ifBlank { expectedClosingDate }

        val request = CreateOpportunityRequest(
            name = opportunityName.trim(),
            branchId = selectedBranchId,
            customerId = if (selectedClientType == ClientType.CUSTOMER) selectedClientId else null,
            leadId = if (selectedClientType == ClientType.LEAD) selectedClientId else null,
            stageId = selectedStageId,
            salespersonId = selectedSalespersonId,
            estimatedValue = parsedAmount,
            expectedClosingDate = formattedDate,
            notes = notes.trim().takeIf { it.isNotBlank() },
            garmentSpecifications = garmentPayloads
        )

        // API call dispatch
        if (isEditMode && opportunityId.isNotBlank()) {
//            opportunityViewModel.updateOpportunity(opportunityId, request)
        } else {
            opportunityViewModel.createOpportunity(request)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(bottom = tokens.buttonHeight + tokens.extraPadding * 2)
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar(
                    title = if (isEditMode) "Edit Opportunity" else "Create Opportunities",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }

            if (isEditMode && detailState is OpportunityDetailUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CirculerProgressIndicatorReuse()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Spacer(Modifier.padding(top = tokens.extraPadding))

                    // Section 1: Client Identification
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CLIENT IDENTIFICATION *",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, light_blue_border),
                                color = Primary_background
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                            .background(if (selectedClientType == ClientType.CUSTOMER) Primary else Color.Transparent)
                                            .clickable {
                                                selectedClientType = ClientType.CUSTOMER
                                                selectedClientId = ""
                                                selectedClientDisplayName = ""
                                            }
                                            .padding(horizontal = tokens.screenPadding * 0.6f, vertical = tokens.extraPadding * 0.35f)
                                    ) {
                                        Text(
                                            text = "Customer",
                                            color = if (selectedClientType == ClientType.CUSTOMER) whiteBg else TextPrimary,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.3f))
                                            .background(if (selectedClientType == ClientType.LEAD) Primary else Color.Transparent)
                                            .clickable {
                                                selectedClientType = ClientType.LEAD
                                                selectedClientId = ""
                                                selectedClientDisplayName = ""
                                            }
                                            .padding(horizontal = tokens.screenPadding * 0.6f, vertical = tokens.extraPadding * 0.35f)
                                    ) {
                                        Text(
                                            text = "Lead",
                                            color = if (selectedClientType == ClientType.LEAD) whiteBg else TextPrimary,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (selectedClientType == ClientType.CUSTOMER)
                                "Select an existing customer (multiple deals permitted)"
                            else
                                "Select an unassigned lead (single deal per lead)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )

                        FormDropdown(
                            label = null,
                            isRequired = true,
                            value = selectedClientDisplayName.ifBlank {
                                if (selectedClientType == ClientType.CUSTOMER) "Search and select customer" else "Select unconverted lead"
                            },
                            expanded = clientDropdownExpanded,
                            onExpandChange = { clientDropdownExpanded = it },
                            options = if (selectedClientType == ClientType.CUSTOMER) customerDisplayOptions else leadDisplayOptions,
                            onOptionSelected = { selected ->
                                selectedClientDisplayName = selected
                                if (selectedClientType == ClientType.CUSTOMER) {
                                    val found = customers.find { "${it.name} (${it.mobile ?: "—"})" == selected }
                                    selectedClientId = found?.id.orEmpty()
                                } else {
                                    val found = unconvertedLeads.find { "${it.name} (${it.phone})" == selected }
                                    selectedClientId = found?.id.orEmpty()
                                }
                            }
                        )
                    }

                    // Section 2: Opportunity Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Text(
                            text = "1. Opportunity Details",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "Opportunity Name", isRequired = true)
                            FormTextField(
                                value = opportunityName,
                                onValueChange = { opportunityName = it },
                                placeholder = "e.g., Bulk Silk Saree Order - Festive Season"
                            )
                        }

                        FormDropdown(
                            label = "Branch Location",
                            isRequired = true,
                            value = selectedBranchName.ifBlank { "Select Branch" },
                            expanded = branchDropdownExpanded,
                            onExpandChange = { branchDropdownExpanded = it },
                            options = branchDisplayOptions,
                            onOptionSelected = { name ->
                                selectedBranchName = name
                                selectedBranchId = branches.find { it.name == name }?.id.orEmpty()
                            }
                        )

                        FormDropdown(
                            label = "Opportunity Stage",
                            isRequired = true,
                            value = opportunityStage.ifBlank { "Select Stage" },
                            expanded = stageDropdownExpanded,
                            onExpandChange = { stageDropdownExpanded = it },
                            options = stageOptions,
                            onOptionSelected = { stageName ->
                                opportunityStage = stageName
                                selectedStageId = apiStages.find { it.name == stageName }?.id.orEmpty()
                            }
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "Estimated Deal Value (INR)", isRequired = true)
                            FormTextField(
                                value = estimatedValue,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) estimatedValue = input
                                },
                                placeholder = "e.g., 45000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    // Section 3: Garment Specifications
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GARMENT SPECIFICATIONS",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Specify garment construction templates, categories, and quantities",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                            }

                            Spacer(Modifier.width(tokens.extraPadding * 0.5f))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .clickable {
                                        garmentSpecs = garmentSpecs + GarmentSpecificationRow()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Add Garment",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        garmentSpecs.forEachIndexed { index, specRow ->
                            var templateExpanded by remember { mutableStateOf(false) }
                            var categoryExpanded by remember { mutableStateOf(false) }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, grey_border),
                                color = Primary_background
                            ) {
                                Column(
                                    modifier = Modifier.padding(tokens.screenPadding * 0.75f),
                                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Item #${index + 1}",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = headerGrey
                                        )

                                        if (garmentSpecs.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    garmentSpecs = garmentSpecs.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(tokens.iconSize * 1.2f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = redText,
                                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                                )
                                            }
                                        }
                                    }

                                    FormDropdown(
                                        label = "GARMENT TEMPLATE *",
                                        isRequired = true,
                                        value = specRow.templateName.ifEmpty { "Select Garment" },
                                        expanded = templateExpanded,
                                        onExpandChange = { templateExpanded = it },
                                        options = garmentTemplateOptions,
                                        onOptionSelected = { selectedTemplateName ->
                                            val selectedGarmentId = activeGarments.find {
                                                (it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.name) == selectedTemplateName
                                            }?.id ?: ""

                                            garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                if (i == index) row.copy(
                                                    templateId = selectedGarmentId,
                                                    templateName = selectedTemplateName,
                                                    categoryId = "",
                                                    categoryName = "",
                                                    isLoadingCategories = true
                                                ) else row
                                            }

                                            salesViewModel.fetchGarmentCategoriesByGarmentId(selectedGarmentId) { categories ->
                                                garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                    if (i == index) row.copy(
                                                        availableCategories = categories,
                                                        isLoadingCategories = false
                                                    ) else row
                                                }
                                            }
                                        }
                                    )

                                    val categoryOptions: List<String> = remember(specRow.availableCategories) {
                                        specRow.availableCategories.mapNotNull { it.displayName.takeIf { d -> d.isNotBlank() }
                                            ?: it.name?.takeIf { n -> n.isNotBlank() } }
                                    }

                                    FormDropdown(
                                        label = "GARMENT CATEGORY / CUT PROFILE",
                                        value = when {
                                            specRow.isLoadingCategories -> "Loading categories..."
                                            specRow.categoryName.isNotBlank() -> specRow.categoryName
                                            specRow.templateId.isBlank() -> "Select Garment First"
                                            else -> "Select Category"
                                        },
                                        expanded = categoryExpanded && !specRow.isLoadingCategories && specRow.templateId.isNotBlank(),
                                        onExpandChange = { categoryExpanded = it },
                                        options = categoryOptions,
                                        onOptionSelected = { selectedCategoryName ->
                                            val selectedCatId = specRow.availableCategories.find {
                                                (it.displayName.takeIf { d -> d.isNotBlank() } ?: it.name) == selectedCategoryName
                                            }?.id ?: ""

                                            garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                if (i == index) row.copy(
                                                    categoryId = selectedCatId,
                                                    categoryName = selectedCategoryName
                                                ) else row
                                            }
                                        }
                                    )

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        FormLabel(text = "QUANTITY *", isRequired = true)
                                        FormTextField(
                                            value = specRow.quantity,
                                            onValueChange = { qty ->
                                                if (qty.all { it.isDigit() }) {
                                                    garmentSpecs = garmentSpecs.mapIndexed { i, row ->
                                                        if (i == index) row.copy(quantity = qty) else row
                                                    }
                                                }
                                            },
                                            placeholder = "1",
                                            keyboardType = KeyboardType.Number
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 4: Deal Information
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Text(
                            text = "2. Deal Information",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        FormDropdown(
                            label = "Assigned Salesperson",
                            isRequired = true,
                            value = selectedSalespersonName.ifBlank { "Select Salesperson" },
                            expanded = salespersonDropdownExpanded,
                            onExpandChange = { salespersonDropdownExpanded = it },
                            options = staffOptions,
                            onOptionSelected = { name ->
                                selectedSalespersonName = name
                                selectedSalespersonId = staffIdMap[name].orEmpty()
                            }
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "Expected Closing Date", isRequired = true)
                            DatePickerField(
                                value = expectedClosingDate,
                                onDateSelected = { expectedClosingDate = it }
                            )
                        }
                    }

                    // Section 5: Notes & Requirements
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                    ) {
                        Text(
                            text = "3. Notes & Requirements",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        FormLabel(text = "Notes & Requirements", isRequired = false)
                        FormTextArea(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = "Enter specific client requirements, custom weaving details, or meeting notes...",
                            minLines = 4,
                            maxLines = 6
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            showBackArrow = false,
            onBack = onClose,
            trailingAction = TrailingFabAction.Update(
                label = if (isEditMode) "Update" else "Create Opportunity",
                isLoading = createState is OpportunityActionState.Loading,
                onClick = { handleSaveOpportunity() }
            ),
            showTrailingArrow = false,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = validationError,
            onDismiss = { validationError = null }
        )
    }
}