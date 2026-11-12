@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.settings.leave_policy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

private val EMPLOYMENT_STATUS_OPTIONS = listOf("full-time", "part-time", "contract", "volunteer")
private val ACCRUAL_OPTIONS = listOf("monthly", "quarterly", "yearly")
private val PRIORITY_OPTIONS = listOf("High / Tier 1 (Precedence)", "Normal / Tier 2")
private val CONFLICT_OPTIONS = listOf("require_hr_review", "higher_priority_wins")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateLeavePolicyScreen(
    viewModel: SettingsViewModel,
    editPolicyId: String? = null,
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isEdit = !editPolicyId.isNullOrBlank()

    // Load available leave types for bundle selection
    val availableLeaveTypes by viewModel.leaveTypes.collectAsState()
    val isSaving by viewModel.isSubmittingLeavePolicy.collectAsState()

    // ── 01. Policy Information State ──
    var policyName by remember { mutableStateOf("") }
    var policyCode by remember { mutableStateOf("") }
    var policyDescription by remember { mutableStateOf("") }
    var effectiveFrom by remember { mutableStateOf("") }
    var effectiveUntil by remember { mutableStateOf("") }
    var isIndefinite by remember { mutableStateOf(true) }

    // ── 02. Eligibility Engine State ──
    var selectedStatuses by remember { mutableStateOf<List<String>>(emptyList()) }
    var designationsText by remember { mutableStateOf("") }
    var gradesText by remember { mutableStateOf("") }
    var minServiceText by remember { mutableStateOf("90 Days from Joining Date") }
    var includeProbationers by remember { mutableStateOf(false) }
    var autoEvaluateProbation by remember { mutableStateOf(false) }

    // ── 03. Included Bundle Types State ──
    var bundleItems by remember { mutableStateOf<List<PolicyLeaveTypeItemDto>>(emptyList()) }
    var showAddTypeDialog by remember { mutableStateOf(false) }

    // ── 04. Assignment & Conflict State ──
    var assignmentMode by remember { mutableStateOf("automatic") }
    var priorityTier by remember { mutableIntStateOf(2) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var conflictResolution by remember { mutableStateOf("require_hr_review") }
    var conflictExpanded by remember { mutableStateOf(false) }

    // Load data if in edit mode
    LaunchedEffect(editPolicyId) {
        viewModel.fetchLeaveTypes(page = 1, limit = 50)
        if (isEdit) {
            viewModel.fetchLeavePolicyById(editPolicyId) { policy ->
                policyName = policy.name
                policyCode = policy.code
                policyDescription = policy.description.orEmpty()
                effectiveFrom = policy.effectiveFrom?.take(10).orEmpty()
                effectiveUntil = policy.effectiveUntil?.take(10).orEmpty()
                isIndefinite = policy.effectiveUntil.isNullOrBlank()

                policy.eligibility?.let { elig ->
                    selectedStatuses = elig.employmentStatuses
                    includeProbationers = elig.includeProbationers
                    autoEvaluateProbation = elig.autoEvaluateOnProbationClearance
                }

                bundleItems = policy.leaveTypes
                policy.assignment?.let { assign ->
                    assignmentMode = assign.mode
                    priorityTier = assign.priorityTier
                    conflictResolution = assign.conflictResolution
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = if (isEdit) "Edit Leave Policy" else "Create Leave Policy",
                onClose = onBack
            )
        },
        bottomBar = {
            Surface(
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", color = TextPrimary, fontSize = tokens.bodySmall)
                    }

                    if (!isEdit) {
                        OutlinedButton(
                            onClick = {
                                savePolicy(
                                    viewModel = viewModel,
                                    editId = null,
                                    status = "draft",
                                    name = policyName,
                                    description = policyDescription,
                                    effectiveFrom = effectiveFrom,
                                    effectiveUntil = if (isIndefinite) null else effectiveUntil,
                                    statuses = selectedStatuses,
                                    includeProbationers = includeProbationers,
                                    autoEvaluateProbation = autoEvaluateProbation,
                                    bundle = bundleItems,
                                    mode = assignmentMode,
                                    priorityTier = priorityTier,
                                    conflict = conflictResolution,
                                    onSuccess = onSavedSuccess
                                )
                            },
                            enabled = !isSaving,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight)
                        ) {
                            Text("Save as Draft", color = title_color, fontSize = tokens.bodySmall)
                        }
                    }

                    Button(
                        onClick = {
                            savePolicy(
                                viewModel = viewModel,
                                editId = editPolicyId,
                                status = "active",
                                name = policyName,
                                description = policyDescription,
                                effectiveFrom = effectiveFrom,
                                effectiveUntil = if (isIndefinite) null else effectiveUntil,
                                statuses = selectedStatuses,
                                includeProbationers = includeProbationers,
                                autoEvaluateProbation = autoEvaluateProbation,
                                bundle = bundleItems,
                                mode = assignmentMode,
                                priorityTier = priorityTier,
                                conflict = conflictResolution,
                                onSuccess = onSavedSuccess
                            )
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else if (isEdit) "Update Policy" else "Publish Policy",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        containerColor = Primary_background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Targeted Cohort Banner
            item {
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Primary)
                        }
                        Column {
                            Text(
                                text = "TARGETED COHORT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = mutedText
                            )
                            Text(
                                text = "${bundleItems.size} Included Leave Types",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                        }
                    }
                }
            }

            // ── Section 01: Policy Information ──
            item {
                SectionCard(stepNumber = "01", title = "Policy Information", subtitle = "Core identifier metadata and active lifecycle window") {
                    FormLabel(text = "Policy Name", isRequired = true)
                    FormTextField(
                        value = policyName,
                        onValueChange = { policyName = it },
                        placeholder = "e.g. Standard Employee Policy"
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Policy Code")
                    FormTextField(
                        value = if (policyCode.isNotBlank()) policyCode else "Auto-generated on save",
                        onValueChange = {},
                        enabled = false
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Policy Description")
                    FormTextArea(
                        value = policyDescription,
                        onValueChange = { policyDescription = it },
                        placeholder = "Describe employee cohort and terms..."
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Effective From", isRequired = true)
                    DatePickerField(
                        value = effectiveFrom,
                        onDateSelected = { effectiveFrom = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormLabel(text = "Effective Until (Optional)")
                        Text(
                            text = if (isIndefinite) "Set Expiry" else "Make Indefinite",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.clickable { isIndefinite = !isIndefinite }
                        )
                    }

                    if (isIndefinite) {
                        FormTextField(
                            value = "No Expiry / Open-ended",
                            onValueChange = {},
                            enabled = false
                        )
                    } else {
                        DatePickerField(
                            value = effectiveUntil,
                            onDateSelected = { effectiveUntil = it }
                        )
                    }
                }
            }

            // ── Section 02: Employee Eligibility Engine ──
            item {
                SectionCard(stepNumber = "02", title = "Employee Eligibility Engine", subtitle = "Define matching rules for eligible workforce personnel") {
                    FormLabel(text = "Employment Status Scope")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EMPLOYMENT_STATUS_OPTIONS.forEach { statusOption ->
                            val isSelected = selectedStatuses.contains(statusOption)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Primary else light_grey,
                                modifier = Modifier.clickable {
                                    selectedStatuses = if (isSelected) {
                                        selectedStatuses - statusOption
                                    } else {
                                        selectedStatuses + statusOption
                                    }
                                }
                            ) {
                                Text(
                                    text = statusOption.replace("-", " ").replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    color = if (isSelected) whiteBg else TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    FormLabel(text = "Designation Scope")
                    FormTextField(
                        value = designationsText,
                        onValueChange = { designationsText = it },
                        placeholder = "e.g. Master Tailor, Pattern Drafter"
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Employee Grade")
                    FormTextField(
                        value = gradesText,
                        onValueChange = { gradesText = it },
                        placeholder = "e.g. Grade A, Grade B"
                    )

                    Spacer(Modifier.height(10.dp))

                    FormLabel(text = "Minimum Service Requirement")
                    FormTextField(
                        value = minServiceText,
                        onValueChange = { minServiceText = it },
                        placeholder = "e.g. 90 Days from Joining Date"
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeProbationers,
                            onCheckedChange = { includeProbationers = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Text(
                            text = "Include Employees currently on Probation",
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = autoEvaluateProbation,
                            onCheckedChange = { autoEvaluateProbation = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Text(
                            text = "Auto-evaluate & assign upon Probation Clearance",
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            // ── Section 03: Included Leave Bundle ──
            item {
                SectionCard(
                    stepNumber = "03",
                    title = "Included Leave Types (Bundle)",
                    subtitle = "${bundleItems.size} Configured Types"
                ) {
                    Button(
                        onClick = { showAddTypeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = primary_light),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Primary)
                        Spacer(Modifier.width(6.dp))
                        Text("+ Add Leave Types to Bundle", color = Primary, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(12.dp))

                    if (bundleItems.isEmpty()) {
                        Text(
                            text = "No leave types bundled yet. Click above to add types.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            bundleItems.forEachIndexed { index, bItem ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    color = if (bItem.isOverridden) yellowBg else whiteBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (bItem.isOverridden) Color(0xFFFDE68A) else sectionBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Leave Type #${index + 1}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = title_color
                                                )
                                                if (bItem.isOverridden) {
                                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFEF3C7)) {
                                                        Text("Override", fontSize = 10.sp, color = yellowText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }

                                            Row {
                                                Text(
                                                    text = if (bItem.isOverridden) "Restore Default" else "Override",
                                                    fontSize = tokens.caption,
                                                    color = Primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable {
                                                            bundleItems = bundleItems.mapIndexed { idx, itm ->
                                                                if (idx == index) itm.copy(isOverridden = !itm.isOverridden) else itm
                                                            }
                                                        }
                                                        .padding(horizontal = 6.dp)
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove",
                                                    tint = redText,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable {
                                                            bundleItems = bundleItems.filterIndexed { idx, _ -> idx != index }
                                                        }
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Entitlement: ${bItem.entitlement?.quota ?: 0.0} ${bItem.entitlement?.unit ?: "days"}",
                                                fontSize = tokens.caption,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "Accrual: ${bItem.entitlement?.accrualFrequency ?: "yearly"}",
                                                fontSize = tokens.caption,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 04: Assignment Rules ──
            item {
                SectionCard(stepNumber = "04", title = "Assignment Rules & Overlap", subtitle = "Policy precedence and conflict handling") {
                    FormLabel(text = "Assignment Mode")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (assignmentMode == "automatic") primary_light else whiteBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (assignmentMode == "automatic") Primary else sectionBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { assignmentMode = "automatic" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Automatic", fontWeight = FontWeight.Bold, fontSize = tokens.bodySmall, color = title_color)
                                Text("Assign by eligibility", fontSize = 10.sp, color = mutedText)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (assignmentMode == "manual") primary_light else whiteBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (assignmentMode == "manual") Primary else sectionBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { assignmentMode = "manual" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Manual", fontWeight = FontWeight.Bold, fontSize = tokens.bodySmall, color = title_color)
                                Text("Handpick users later", fontSize = 10.sp, color = mutedText)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    FormDropdown(
                        label = "Policy Priority",
                        value = if (priorityTier == 1) "High / Tier 1 (Precedence)" else "Normal / Tier 2",
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = PRIORITY_OPTIONS,
                        onOptionSelected = { selected ->
                            priorityTier = if (selected.startsWith("High")) 1 else 2
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    FormDropdown(
                        label = "Conflict Resolution",
                        value = if (conflictResolution == "require_hr_review") "Require HR Review on Overlap" else "Auto-Merge Benefits",
                        expanded = conflictExpanded,
                        onExpandChange = { conflictExpanded = it },
                        options = listOf("Require HR Review on Overlap", "Auto-Merge Benefits"),
                        onOptionSelected = { selected ->
                            conflictResolution = if (selected.contains("Review")) "require_hr_review" else "higher_priority_wins"
                        }
                    )
                }
            }
        }
    }

    // Modal to add leave types to bundle
    if (showAddTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = {
                Text(
                    text = "Add Leave Type to Bundle",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    val unbundled = availableLeaveTypes.filter { lt ->
                        bundleItems.none { it.leaveTypeId == lt.id }
                    }

                    if (unbundled.isEmpty()) {
                        item {
                            Text("All available leave types are already in this bundle.", fontSize = tokens.bodySmall, color = mutedText)
                        }
                    } else {
                        items(unbundled) { lt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newItem = PolicyLeaveTypeItemDto(
                                            leaveTypeId = lt.id,
                                            entitlement = PolicyEntitlementDto(
                                                quota = lt.daysPerYear.toDouble(),
                                                unit = "days",
                                                accrualFrequency = lt.allocationMethod.ifBlank { "yearly" }
                                            ),
                                            defaultSnapshot = PolicyEntitlementDto(
                                                quota = lt.daysPerYear.toDouble(),
                                                unit = "days",
                                                accrualFrequency = lt.allocationMethod.ifBlank { "yearly" }
                                            ),
                                            isOverridden = false,
                                            sortOrder = bundleItems.size
                                        )
                                        bundleItems = bundleItems + newItem
                                        showAddTypeDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = lt.leaveTypeName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                    Text(text = "${lt.daysPerYear} days / year", fontSize = tokens.caption, color = mutedText)
                                }
                                Text("+ Add", color = Primary, fontSize = tokens.caption, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddTypeDialog = false }) {
                    Text("Close", color = TextPrimary)
                }
            },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius)
        )
    }
}

@Composable
private fun SectionCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stepNumber, color = whiteBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                    Text(text = subtitle, fontSize = tokens.caption, color = mutedText)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = grey_border, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            content()
        }
    }
}

private fun savePolicy(
    viewModel: SettingsViewModel,
    editId: String?,
    status: String,
    name: String,
    description: String,
    effectiveFrom: String,
    effectiveUntil: String?,
    statuses: List<String>,
    includeProbationers: Boolean,
    autoEvaluateProbation: Boolean,
    bundle: List<PolicyLeaveTypeItemDto>,
    mode: String,
    priorityTier: Int,
    conflict: String,
    onSuccess: () -> Unit
) {
    if (name.isBlank()) {
        viewModel.showError("Policy name is required")
        return
    }

    val req = SaveLeavePolicyRequest(
        name = name.trim(),
        description = description.trim().ifBlank { null },
        status = status,
        effectiveFrom = effectiveFrom.ifBlank { null },
        effectiveUntil = effectiveUntil?.ifBlank { null },
        eligibility = PolicyEligibilityDto(
            employmentStatuses = statuses,
            includeProbationers = includeProbationers,
            autoEvaluateOnProbationClearance = autoEvaluateProbation
        ),
        leaveTypes = bundle,
        assignment = PolicyAssignmentDto(
            mode = mode,
            priorityTier = priorityTier,
            conflictResolution = conflict
        )
    )

    if (editId.isNullOrBlank()) {
        viewModel.createLeavePolicy(req, onSuccess = { onSuccess() })
    } else {
        viewModel.updateLeavePolicy(editId, req, onSuccess = { onSuccess() })
    }
}