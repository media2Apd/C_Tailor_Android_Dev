
@file:Suppress(
    "UNUSED_PARAMETER",
    "unused","unusedVariable"
)

package com.cuso.tailor.view.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.ApprovalRuleItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SettingsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.cuso.tailor.model.settings.ApprovalConditionDto
import com.cuso.tailor.model.settings.CreateApprovalRuleRequest
import com.cuso.tailor.model.settings.CreateApproverLevelDto
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.viewmodel.HrViewModel
@Composable
fun ApprovalRulesScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onCreateRule: () -> Unit = {},
    onEditRule: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val approvalRules by viewModel.approvalRules.collectAsState()
    val isLoading by viewModel.isLoadingApprovalRules.collectAsState()
    val canLoadMore by viewModel.canLoadMoreApprovalRules.collectAsState()
    val errorMessage by viewModel.approvalRulesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Deletion Modal States
    var ruleToDelete by remember { mutableStateOf<ApprovalRuleItemDto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.fetchApprovalRules(page = 1)
    }

    // Trigger pagination when reaching end of list
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoading) {
            viewModel.loadMoreApprovalRules()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TitleBar(
                    title = "Approval Rules",
                    onClose = onBack
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Primary_background)
            ) {
                FabScaffold(
                    fab = FabConfig(
                        label = "Create Rule",
                        icon = Icons.Default.Add,
                        onClick = onCreateRule
                    ),
                    fabVisible = !showDeleteDialog
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        // Search bar
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { query ->
                                searchQuery = query
                                viewModel.onApprovalRulesSearchQueryChanged(query)
                            },
                            placeholder = "Search by rule name or entity...",
                            showFilterIcon = true,
                            height = tokens.fieldHeight * 1.15f
                        )

                        HorizontalDivider(color = title_border)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (isLoading && approvalRules.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            } else if (approvalRules.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Rule,
                                            contentDescription = null,
                                            tint = mutedText,
                                            modifier = Modifier.size(tokens.iconSize * 2.5f)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No matching approval rules found" else "No approval rules found",
                                            color = mutedText,
                                            fontSize = tokens.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(approvalRules, key = { it.id }) { rule ->
                                        ApprovalRuleCard(
                                            rule = rule,
                                            onEdit = { onEditRule(rule.id) },
                                            onToggleStatus = {
                                                viewModel.toggleApprovalRuleStatus(rule.id)
                                            },
                                            onDelete = {
                                                ruleToDelete = rule
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Modal
        if (showDeleteDialog && ruleToDelete != null) {
            val target = ruleToDelete!!
            DeleteModel(
                title = "Delete Approval Rule",
                message = "You are going to delete \"${target.name}\" for ${target.entityType}. Are you sure?",
                checkboxText = "I confirm this approval rule deletion",
                cancelText = "No, Keep It.",
                confirmText = "Yes, Delete!",
                onDismiss = {
                    showDeleteDialog = false
                    ruleToDelete = null
                },
                onDelete = {
                    showDeleteDialog = false
                    viewModel.deleteApprovalRule(
                        id = target.id,
                        onSuccess = {
                            ruleToDelete = null
                        },
                        onError = {
                            ruleToDelete = null
                        }
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DATA CARD ITEM
// ─────────────────────────────────────────────────────────────

@Composable
private fun ApprovalRuleCard(
    rule: ApprovalRuleItemDto,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = rule.status.equals("Active", ignoreCase = true)
    val statusBg = if (isActive) greenBg else redBg
    val statusText = if (isActive) greentext else redText

    // Format created date (e.g. 2026-11-01)
    val formattedDate = rule.createdAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: rule.createdAt

    // Format approver summary
    val approverSummary = rule.approverLevels.joinToString(", ") { level ->
        "${level.approverType} (L${level.level})"
    }.ifBlank { "None" }

    DataCard(
        item = rule,
        title = rule.name,
        dateText = formattedDate,
        topBadgeText = rule.status,
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        actions = listOf(
            MenuAction(label = "Edit Rule", icon = Icons.Default.Edit) { onEdit() },
            MenuAction(label = "Toggle Status", icon = Icons.Default.Sync) { onToggleStatus() },
            MenuAction(label = "Delete", icon = Icons.Default.Delete) { onDelete() }
        ),
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Target Document",
                text = rule.entityType,
                textColor = title_color
            ),
            DataCardField(
                label = "Evaluation Criteria",
                text = rule.condition?.toReadableCriteria() ?: "No condition",
                textColor = TextPrimary
            ),
            DataCardField(
                label = "Approver Level",
                text = approverSummary,
                textColor = TextSecondary
            )
        )
    )
}

// ── Target Document Options ──
private data class EntityOption(val id: String, val label: String, val defaultField: String)

private val ENTITY_OPTIONS = listOf(
    EntityOption("SalesOrder", "Sales Order", "grandTotal"),
    EntityOption("SalesInvoice", "Sales Invoice", "grandTotal"),
    EntityOption("PurchaseBill", "Purchase Bill", "grandTotal"),
    EntityOption("ServiceRequest", "Service Request", "totalCharges"),
    EntityOption("CustomerComplaint", "Customer Complaint", "priority"),
    EntityOption("Requisition", "Purchase Request", "totalEstimatedCost"),
    EntityOption("JournalEntry", "Journal Entry", "totalDebit"),
    EntityOption("CreditNote", "Credit Note", "totalAmount"),
    EntityOption("VendorCredit", "Vendor Credit", "totalAmount"),
    EntityOption("Opportunity", "Opportunity", "estimatedValue"),
    EntityOption("Quotation", "Quotation", "grandTotal"),
    EntityOption("Customer", "Customer", "creditLimit")
)

// ── Operator Options ──
private data class OperatorOption(val id: String, val label: String)

private val OPERATOR_OPTIONS = listOf(
    OperatorOption("gt", "Greater than (>)"),
    OperatorOption("gte", "Greater than or equal (>=)"),
    OperatorOption("lt", "Less than (<)"),
    OperatorOption("lte", "Less than or equal (<=)"),
    OperatorOption("eq", "Equals (=)"),
    OperatorOption("ne", "Not Equals (!=)")
)

// ── Approver Mode Options ──
private val APPROVER_MODES = listOf("Reporting Manager", "Role / Group", "Specific Member")
private val MANAGER_STEP_OPTIONS = listOf("Direct Manager (1 Step)", "Manager's Manager (2 Steps)")

// ── Local Form State for Individual Stage ──
private data class StageFormState(
    val level: Int,
    val approverMode: String = "Reporting Manager",
    val managerStep: String = "Direct Manager (1 Step)",
    val selectedRoleId: String = "",
    val selectedApproverId: String = ""
)

@Composable
fun CreateEditApprovalRuleScreen(
    ruleId: String? = null,
    approvalViewModel: SettingsViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !ruleId.isNullOrBlank()

    // HR Data: Roles & Members for Dropdowns
    val roles by hrViewModel.roles.collectAsState()
    val members by hrViewModel.members.collectAsState()
    val isSaving by approvalViewModel.isSavingRule.collectAsState()

    // Form fields state
    var ruleName by remember { mutableStateOf("") }
    var selectedEntity by remember { mutableStateOf(ENTITY_OPTIONS.first()) }
    var priorityRank by remember { mutableStateOf("1") }

    // Conditional criteria state
    var applyCondition by remember { mutableStateOf(false) }
    var conditionField by remember { mutableStateOf("grandTotal") }
    var selectedOperator by remember { mutableStateOf(OPERATOR_OPTIONS.first()) }
    var conditionValue by remember { mutableStateOf("10000") }

    // Multi-level stages state
    var stages by remember {
        mutableStateOf(listOf(StageFormState(level = 1)))
    }

    // Dropdown expanded states
    var entityDropdownExpanded by remember { mutableStateOf(false) }
    var operatorDropdownExpanded by remember { mutableStateOf(false) }

    // Fetch initial metadata
    LaunchedEffect(Unit) {
        hrViewModel.fetchRoles()
        hrViewModel.fetchMembers(limit = 100)
    }

    // Prefill form in edit mode
    LaunchedEffect(ruleId) {
        if (isEditMode) {
            approvalViewModel.fetchApprovalRuleById(ruleId) { rule ->
                ruleName = rule.name
                priorityRank = rule.priority.toString()

                val matchedEntity = ENTITY_OPTIONS.find { it.id == rule.entityType } ?: ENTITY_OPTIONS.first()
                selectedEntity = matchedEntity

                if (rule.condition != null && rule.condition.field.isNotBlank()) {
                    applyCondition = true
                    conditionField = rule.condition.field
                    selectedOperator = OPERATOR_OPTIONS.find { it.id == rule.condition.operator } ?: OPERATOR_OPTIONS.first()
                    conditionValue = rule.condition.value?.toString() ?: ""
                } else {
                    applyCondition = false
                }

                if (rule.approverLevels.isNotEmpty()) {
                    stages = rule.approverLevels.map { lvl ->
                        StageFormState(
                            level = lvl.level,
                            approverMode = when (lvl.approverType) {
                                "Role" -> "Role / Group"
                                "SpecificUser" -> "Specific Member"
                                else -> "Reporting Manager"
                            },
                            managerStep = if (lvl.managerLevel == 2) "Manager's Manager (2 Steps)" else "Direct Manager (1 Step)",
                            selectedRoleId = lvl.roleId ?: "",
                            selectedApproverId = lvl.approverId ?: ""
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Approval Rule" else "Create Approval Rule",
                onClose = {
                    approvalViewModel.clearSelectedRuleDetail()
                    onClose()
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (ruleName.isBlank()) {
                                DynamicIslandManager.showError("Rule name is required")
                                return@Button
                            }

                            // Validate stages
                            for (stage in stages) {
                                if (stage.approverMode == "Role / Group" && stage.selectedRoleId.isBlank()) {
                                    DynamicIslandManager.showError("Please select a role for Stage ${stage.level}")
                                    return@Button
                                }
                                if (stage.approverMode == "Specific Member" && stage.selectedApproverId.isBlank()) {
                                    DynamicIslandManager.showError("Please select a member for Stage ${stage.level}")
                                    return@Button
                                }
                            }

                            val approverLevels = stages.map { stage ->
                                CreateApproverLevelDto(
                                    level = stage.level,
                                    approverType = when (stage.approverMode) {
                                        "Role / Group" -> "Role"
                                        "Specific Member" -> "SpecificUser"
                                        else -> "ReportingManager"
                                    },
                                    managerLevel = if (stage.approverMode == "Reporting Manager") {
                                        if (stage.managerStep.contains("2")) 2 else 1
                                    } else null,
                                    roleId = if (stage.approverMode == "Role / Group") stage.selectedRoleId else null,
                                    approverId = if (stage.approverMode == "Specific Member") stage.selectedApproverId else null
                                )
                            }

                            val payload = CreateApprovalRuleRequest(
                                name = ruleName.trim(),
                                entityType = selectedEntity.id,
                                priority = priorityRank.toIntOrNull() ?: 1,
                                condition = if (applyCondition) {
                                    ApprovalConditionDto(
                                        field = conditionField.trim(),
                                        operator = selectedOperator.id,
                                        value = conditionValue.toDoubleOrNull() ?: conditionValue
                                    )
                                } else null,
                                approverLevels = approverLevels
                            )

                            if (isEditMode) {
                                approvalViewModel.updateApprovalRule(
                                    ruleId = ruleId,
                                    request = payload,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Approval rule updated successfully")
                                        onClose()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            } else {
                                approvalViewModel.createApprovalRule(
                                    request = payload,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Approval rule created successfully")
                                        onClose()
                                    },
                                    onError = { DynamicIslandManager.showError(it) }
                                )
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize))
                        } else {
                            Text(
                                text = if (isEditMode) "Update Rule" else "Save Rule",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Rule Name ──
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FormLabel( "Rule Name", isRequired = true)
                FormTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    placeholder = "e.g. Sales Orders Above 50,000",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── 2. Target Document & Priority Rank ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Target Document",
                        value = selectedEntity.label,
                        expanded = entityDropdownExpanded,
                        onExpandChange = { entityDropdownExpanded = it },
                        options = ENTITY_OPTIONS.map { it.label },
                        onOptionSelected = { label ->
                            val matched = ENTITY_OPTIONS.find { it.label == label } ?: ENTITY_OPTIONS.first()
                            selectedEntity = matched
                            conditionField = matched.defaultField
                        },
                        isRequired = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Priority Rank (Higher Wins)")
                    Spacer(Modifier.height(4.dp))
                    FormTextField(
                        value = priorityRank,
                        onValueChange = { priorityRank = it },
                        keyboardType = KeyboardType.Number,
                        placeholder = "1",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── 3. Conditional Criteria Checkbox Card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { applyCondition = !applyCondition }
                    ) {
                        Checkbox(
                            checked = applyCondition,
                            onCheckedChange = { applyCondition = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Apply conditional criteria (Leave unchecked for Universal rule)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    AnimatedVisibility(
                        visible = applyCondition,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = grey_border)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Field Path
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Field Path")
                                    Spacer(Modifier.height(4.dp))
                                    FormTextField(
                                        value = conditionField,
                                        onValueChange = { conditionField = it },
                                        placeholder = "grandTotal",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Operator
                                Column(modifier = Modifier.weight(1.3f)) {
                                    FormDropdown(
                                        label = "Operator",
                                        value = selectedOperator.label,
                                        expanded = operatorDropdownExpanded,
                                        onExpandChange = { operatorDropdownExpanded = it },
                                        options = OPERATOR_OPTIONS.map { it.label },
                                        onOptionSelected = { label ->
                                            selectedOperator = OPERATOR_OPTIONS.find { it.label == label } ?: OPERATOR_OPTIONS.first()
                                        }
                                    )
                                }

                                // Value
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Value")
                                    Spacer(Modifier.height(4.dp))
                                    FormTextField(
                                        value = conditionValue,
                                        onValueChange = { conditionValue = it },
                                        keyboardType = KeyboardType.Number,
                                        placeholder = "10000",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 4. Approval Levels Sequence ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Approval Levels Sequence",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Order in which approvers must sign off",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                Text(
                    text = "+ Add Stage",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val nextLevel = stages.size + 1
                            stages = stages + StageFormState(level = nextLevel)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // ── 5. Stages List ──
            stages.forEachIndexed { index, stage ->
                StageCard(
                    stage = stage,
                    stageNumber = index + 1,
                    canDelete = stages.size > 1,
                    rolesList = roles.map { it._id to it.name },
                    membersList = members.map { it._id to "${it.firstName} ${it.lastName}" },
                    onDelete = {
                        stages = stages.filterIndexed { i, _ -> i != index }
                            .mapIndexed { i, s -> s.copy(level = i + 1) }
                    },
                    onStageChange = { updated ->
                        stages = stages.mapIndexed { i, s -> if (i == index) updated else s }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STAGE CARD ITEM
// ─────────────────────────────────────────────────────────────

@Composable
private fun StageCard(
    stage: StageFormState,
    stageNumber: Int,
    canDelete: Boolean,
    rolesList: List<Pair<String, String>>,
    membersList: List<Pair<String, String>>,
    onDelete: () -> Unit,
    onStageChange: (StageFormState) -> Unit
) {
    val tokens = LocalAppTokens.current
    var modeDropdownExpanded by remember { mutableStateOf(false) }
    var subDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Stage Name & Delete Trash Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stage $stageNumber",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Stage",
                            tint = redText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = grey_border)

            // Two Dropdown Columns: Approver Mode & Step/Role/Member
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Approver Mode Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    FormDropdown(
                        label = "Approver Mode",
                        value = stage.approverMode,
                        expanded = modeDropdownExpanded,
                        onExpandChange = { modeDropdownExpanded = it },
                        options = APPROVER_MODES,
                        onOptionSelected = { selectedMode ->
                            onStageChange(
                                stage.copy(
                                    approverMode = selectedMode,
                                    selectedRoleId = "",
                                    selectedApproverId = ""
                                )
                            )
                        }
                    )
                }

                // Second Dynamic Dropdown depending on Approver Mode
                Column(modifier = Modifier.weight(1f)) {
                    when (stage.approverMode) {
                        "Reporting Manager" -> {
                            FormDropdown(
                                label = "Manager Hierarchy Step",
                                value = stage.managerStep,
                                expanded = subDropdownExpanded,
                                onExpandChange = { subDropdownExpanded = it },
                                options = MANAGER_STEP_OPTIONS,
                                onOptionSelected = { step ->
                                    onStageChange(stage.copy(managerStep = step))
                                }
                            )
                        }

                        "Role / Group" -> {
                            val currentRoleName = rolesList.find { it.first == stage.selectedRoleId }?.second ?: "Select Role"
                            FormDropdown(
                                label = "Select Role",
                                value = currentRoleName,
                                expanded = subDropdownExpanded,
                                onExpandChange = { subDropdownExpanded = it },
                                options = rolesList.map { it.second },
                                onOptionSelected = { roleName ->
                                    val matched = rolesList.find { it.second == roleName }
                                    if (matched != null) {
                                        onStageChange(stage.copy(selectedRoleId = matched.first))
                                    }
                                }
                            )
                        }

                        "Specific Member" -> {
                            val currentMemberName = membersList.find { it.first == stage.selectedApproverId }?.second ?: "Select Member"
                            FormDropdown(
                                label = "Select Member",
                                value = currentMemberName,
                                expanded = subDropdownExpanded,
                                onExpandChange = { subDropdownExpanded = it },
                                options = membersList.map { it.second },
                                onOptionSelected = { memberName ->
                                    val matched = membersList.find { it.second == memberName }
                                    if (matched != null) {
                                        onStageChange(stage.copy(selectedApproverId = matched.first))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}