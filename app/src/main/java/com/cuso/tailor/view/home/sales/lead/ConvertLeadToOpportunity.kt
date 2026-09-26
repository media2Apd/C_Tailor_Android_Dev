package com.cuso.tailor.view.home.sales.lead

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.cuso.tailor.model.sales.ConvertLeadToOpportunityRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatLeadDate
import com.cuso.tailor.view.home.toIsoDate
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.OpportunityActionState
import com.cuso.tailor.viewmodel.OpportunityViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import kotlinx.coroutines.delay

@Composable
fun ConvertLeadToOpportunityScreen(
    onClose: () -> Unit,
    onConversionSuccess: () -> Unit,
    salesViewModel: SalesViewModel = hiltViewModel(),
    opportunityViewModel: OpportunityViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Fetch initial data
    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        salesViewModel.fetchStaff()
        opportunityViewModel.fetchOpportunityStages()
    }

    val selectedLead by salesViewModel.selectedLead.collectAsStateWithLifecycle()
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val stages by opportunityViewModel.opportunityStages.collectAsStateWithLifecycle()
    val convertLeadState by opportunityViewModel.convertLeadState.collectAsStateWithLifecycle()

    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()

    // Form fields prefilled from currently selected lead
    var dealName by remember(selectedLead) {
        val leadName = selectedLead?.fullName.orEmpty()
        mutableStateOf(if (leadName.isNotBlank()) "$leadName - Opportunity" else "")
    }

    var selectedBranchId by remember(branches) {
        mutableStateOf(branches.firstOrNull()?.id.orEmpty())
    }
    var selectedBranchName by remember(selectedBranchId, branches) {
        mutableStateOf(branches.find { it.id == selectedBranchId }?.name.orEmpty())
    }

    var selectedStageId by remember(stages) {
        mutableStateOf(stages.firstOrNull()?.id.orEmpty())
    }
    var selectedStageName by remember(selectedStageId, stages) {
        mutableStateOf(stages.find { it.id == selectedStageId }?.name ?: "New Opportunity")
    }

    var selectedSalespersonId by remember(selectedLead, staffList) {
        val assignedStaff = selectedLead?.assignedStaff.orEmpty()
        val matchingStaff = staffList.find { it.id == assignedStaff } ?: staffList.firstOrNull()
        mutableStateOf(matchingStaff?.id.orEmpty())
    }
    var selectedSalespersonName by remember(selectedSalespersonId, staffList) {
        val staff = staffList.find { it.id == selectedSalespersonId }
        mutableStateOf(staff?.let { "${it.firstName} ${it.lastName}".trim() } ?: "")
    }

    var estimatedValue by remember(selectedLead) {
        val budget = selectedLead?.budgetMax ?: selectedLead?.budgetMin ?: 0
        mutableStateOf(if (budget > 0) budget.toString() else "")
    }

    var expectedClosingDate by remember(selectedLead) {
        mutableStateOf(formatLeadDate(selectedLead?.requiredDate))
    }

    var transitionNotes by remember(selectedLead) {
        mutableStateOf(selectedLead?.internalNotes.orEmpty())
    }

    var branchDropdownExpanded by remember { mutableStateOf(false) }
    var stageDropdownExpanded by remember { mutableStateOf(false) }
    var salespersonDropdownExpanded by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val branchOptions = remember(branches) { branches.mapNotNull { it.name } }
    val stageOptions = remember(stages) { stages.map { it.name }.ifEmpty { listOf("New Opportunity") } }
    val staffOptions = remember(staffList) { staffList.map { "${it.firstName} ${it.lastName}".trim() } }

    // Handle convert lead API state
    LaunchedEffect(convertLeadState) {
        when (val state = convertLeadState) {
            is OpportunityActionState.Success -> {
                successMessage = state.message
                opportunityViewModel.resetActionStates()
                salesViewModel.fetchTableLeads()
                delay(900)
                onConversionSuccess()
            }
            is OpportunityActionState.Error -> {
                validationError = state.message
                opportunityViewModel.resetActionStates()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = tokens.buttonHeight + tokens.extraPadding * 2)
            ) {
                // Top TitleBar
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar(
                        title = "Convert Lead to Opportunity",
                        onClose = onClose
                    )
                    HorizontalDivider(color = title_border)
                }

                // Sub-header banner card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .padding(tokens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight * 0.95f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(Color(0xFF3730A3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize * 1.1f)
                        )
                    }
                    Spacer(Modifier.padding(top = tokens.extraPadding))
                    Column {
                        Text(
                            text = "Convert Lead to Opportunity",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Qualify this lead and establish sales pipeline tracking.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }

                HorizontalDivider(color = grey_border)

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg)
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // Opportunity Deal Name
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "OPPORTUNITY DEAL NAME *", isRequired = false)
                            FormTextField(
                                value = dealName,
                                onValueChange = { dealName = it },
                                placeholder = "Enter deal name"
                            )
                        }

                        // Row: Branch Location & Initial Pipeline Stage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "BRANCH LOCATION *",
                                    value = selectedBranchName.ifBlank { "Select Branch" },
                                    expanded = branchDropdownExpanded,
                                    onExpandChange = { branchDropdownExpanded = it },
                                    options = branchOptions,
                                    onOptionSelected = { name ->
                                        selectedBranchName = name
                                        selectedBranchId = branches.find { it.name == name }?.id.orEmpty()
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "INITIAL PIPELINE STAGE *",
                                    value = selectedStageName.ifBlank { "Select Stage" },
                                    expanded = stageDropdownExpanded,
                                    onExpandChange = { stageDropdownExpanded = it },
                                    options = stageOptions,
                                    onOptionSelected = { stageName ->
                                        selectedStageName = stageName
                                        selectedStageId = stages.find { it.name == stageName }?.id.orEmpty()
                                    }
                                )
                            }
                        }

                        // Row: Assigned Salesperson & Estimated Deal Value
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "ASSIGNED SALESPERSON *",
                                    value = selectedSalespersonName.ifBlank { "Select Salesperson" },
                                    expanded = salespersonDropdownExpanded,
                                    onExpandChange = { salespersonDropdownExpanded = it },
                                    options = staffOptions,
                                    onOptionSelected = { name ->
                                        selectedSalespersonName = name
                                        val staff = staffList.find { "${it.firstName} ${it.lastName}".trim() == name }
                                        selectedSalespersonId = staff?.id.orEmpty()
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    FormLabel(text = "ESTIMATED DEAL VALUE", isRequired = false)
                                    FormTextField(
                                        value = estimatedValue,
                                        onValueChange = { if (it.all { ch -> ch.isDigit() }) estimatedValue = it },
                                        placeholder = "e.g. 50000",
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }
                        }

                        // Expected Closing Date
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "EXPECTED CLOSING DATE", isRequired = false)
                            DatePickerField(
                                value = expectedClosingDate,
                                onDateSelected = { expectedClosingDate = it }
                            )
                        }

                        // Transition Notes
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FormLabel(text = "TRANSITION NOTES", isRequired = false)
                            FormTextArea(
                                value = transitionNotes,
                                onValueChange = { transitionNotes = it },
                                placeholder = "Enter transition notes...",
                                minLines = 4,
                                maxLines = 6
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar using reusable StepNavigationFab
            StepNavigationFab(
                showBack = true,
                backLabel = "Cancel",
                showBackArrow = false,
                onBack = onClose,
                trailingAction = TrailingFabAction.Update(
                    label = "Confirm Conversion",
                    isLoading = convertLeadState is OpportunityActionState.Loading,
                    onClick = {
                        val leadId = selectedLead?.id.orEmpty()
                        if (leadId.isBlank()) {
                            validationError = "Lead ID not found"
                            return@Update
                        }
                        if (dealName.isBlank()) {
                            validationError = "Opportunity Deal Name is required"
                            return@Update
                        }
                        if (selectedBranchId.isBlank()) {
                            validationError = "Please select a branch location"
                            return@Update
                        }
                        if (selectedStageId.isBlank()) {
                            validationError = "Please select an initial pipeline stage"
                            return@Update
                        }
                        if (selectedSalespersonId.isBlank()) {
                            validationError = "Please select an assigned salesperson"
                            return@Update
                        }

                        validationError = null
                        val isoDate = expectedClosingDate.toIsoDate().ifBlank { expectedClosingDate }

                        val payload = ConvertLeadToOpportunityRequest(
                            leadId = leadId,
                            name = dealName.trim(),
                            branchId = selectedBranchId,
                            stageId = selectedStageId,
                            salespersonId = selectedSalespersonId,
                            estimatedValue = estimatedValue.toDoubleOrNull() ?: 0.0,
                            expectedClosingDate = isoDate.takeIf { it.isNotBlank() },
                            notes = transitionNotes.trim().takeIf { it.isNotBlank() }
                        )

                        opportunityViewModel.convertLeadToOpportunity(payload)
                    }
                ),
                showTrailingArrow = false,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            DynamicIslandError(
                modifier = Modifier.align(Alignment.TopCenter),
                message = validationError,
                onDismiss = { validationError = null }
            )

            DynamicIslandSuccess(
                modifier = Modifier.align(Alignment.TopCenter),
                message = successMessage,
                onDismiss = { successMessage = null }
            )
        }
    }
}