package com.cuso.tailor.view.home.hr.settings.leave_type

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateLeaveTypeScreen(
    leaveTypeIdToEdit: String? = null,
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    val isEditMode = !leaveTypeIdToEdit.isNullOrBlank()

    // ─────────────────────────────────────────────────────────────
    // SECTION 01: Basic Details & Classification States
    // ─────────────────────────────────────────────────────────────
    var leaveTypeName by remember { mutableStateOf("") }
    var shortCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var compensationType by remember { mutableStateOf("paid") } // paid, unpaid, partial
    var category by remember { mutableStateOf("Regular Leave") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Regular Leave", "Medical Leave", "Special Leave")
    val compensationOptions = listOf(
        Pair("paid", "Paid"),
        Pair("unpaid", "Unpaid"),
        Pair("partial", "Partial")
    )

    val paletteList = listOf(
        Pair("#E5484D", "Crimson"),
        Pair("#6366F1", "Indigo"),
        Pair("#7654B8", "Regal Violet"),
        Pair("#4F6A9A", "Slate Blue"),
        Pair("#2F9E6B", "Emerald"),
        Pair("#C98A3D", "Bronze")
    )
    var selectedColor by remember { mutableStateOf("#7654B8") }
    var effectiveFrom by remember { mutableStateOf("01-01-2027") }
    var effectiveUntil by remember { mutableStateOf("") }
    var isOpenEnded by remember { mutableStateOf(true) }

    // ─────────────────────────────────────────────────────────────
    // SECTION 02: Leave Allowance & Accrual Method States
    // ─────────────────────────────────────────────────────────────
    var allocationMethod by remember { mutableStateOf("fixed_annual") }
    var annualEntitlement by remember { mutableStateOf("18") }
    var leaveCycle by remember { mutableStateOf("calendar_year") }
    var isLeaveCycleExpanded by remember { mutableStateOf(false) }

    val cycleOptions = listOf(
        Pair("calendar_year", "Calendar Year (Jan - Dec)"),
        Pair("financial_year", "Financial Year (Apr - Mar)"),
        Pair("joining_anniversary", "Joining Anniversary")
    )

    var maxAccumulated by remember { mutableStateOf("30") }
    var prorateForMidYearJoiners by remember { mutableStateOf(true) }

    // ─────────────────────────────────────────────────────────────
    // SECTION 03: Employee Eligibility & Scope States
    // ─────────────────────────────────────────────────────────────
    var audienceScope by remember { mutableStateOf("all_employees") } // all_employees, selected_groups
    val selectedEmploymentTypes = remember { mutableStateListOf<String>() }
    var departmentScope by remember { mutableStateOf("all_dept") } // all_dept, selected_dept
    var isDepartmentScopeExpanded by remember { mutableStateOf(false) }

    val departmentScopeOptions = listOf(
        Pair("all_dept", "All Departments"),
        Pair("selected_dept", "Selected Departments")
    )

    var minServiceId by remember { mutableStateOf("none") }
    var isMinServiceExpanded by remember { mutableStateOf(false) }
    val minServiceOptions = listOf(
        Triple("none", "No minimum service", Pair(0, "months")),
        Triple("3_months", "3 Months Continuous Service", Pair(3, "months")),
        Triple("6_months", "6 Months Continuous Service", Pair(6, "months"))
    )

    val locations = remember { mutableStateListOf<String>() }
    var availableInProbation by remember { mutableStateOf(false) }

    // ─────────────────────────────────────────────────────────────
    // SECTION 04: Leave Usage & Request Constraints States
    // ─────────────────────────────────────────────────────────────
    var allowHalfDay by remember { mutableStateOf(true) }
    var allowHourly by remember { mutableStateOf(false) }
    var minLeaveDuration by remember { mutableStateOf("0.5") }
    var maxConsecutiveDays by remember { mutableStateOf("10") }
    var advanceNoticeId by remember { mutableStateOf("3") }
    var isAdvanceNoticeExpanded by remember { mutableStateOf(false) }

    val advanceNoticeOptions = listOf(
        Pair("0", "No advance notice"),
        Pair("3", "3 Days in advance"),
        Pair("7", "7 Days in advance")
    )

    var emergencySameDay by remember { mutableStateOf(true) }
    var allowNegativeBalance by remember { mutableStateOf(false) }
    var includeInterveningWeekends by remember { mutableStateOf(false) }
    var requireSupportingDocs by remember { mutableStateOf(false) }
    var docsAfterDays by remember { mutableStateOf("4") }

    // ─────────────────────────────────────────────────────────────
    // SECTION 05: Carry Forward & Encashment Rules States
    // ─────────────────────────────────────────────────────────────
    var enableCarryForward by remember { mutableStateOf(true) }
    var maxCarryForwardLimit by remember { mutableStateOf("10") }
    var expiryId by remember { mutableStateOf("q1") }
    var isExpiryExpanded by remember { mutableStateOf(false) }

    val expiryOptions = listOf(
        Triple("q1", "End of Q1 (31 March) of following year", 3),
        Triple("none", "No Expiration", 0)
    )

    var excessBalanceTreatment by remember { mutableStateOf("expire") } // expire, encash
    var encashmentRate by remember { mutableStateOf("basic_per_30") }
    var isEncashmentRateExpanded by remember { mutableStateOf(false) }

    val encashmentRateOptions = listOf(
        Pair("basic_per_30", "Basic Pay / 30 per day"),
        Pair("basic_per_26", "Basic Pay / 26 per day"),
        Pair("gross_per_30", "Gross Pay / 30 per day"),
        Pair("gross_per_26", "Gross Pay / 26 per day")
    )
    var encashmentCeiling by remember { mutableStateOf("8") }

    // ─────────────────────────────────────────────────────────────
    // SECTION 06: Approval Hierarchy & Governance States
    // ─────────────────────────────────────────────────────────────
    var requiresFormalApproval by remember { mutableStateOf(true) }
    var routingId by remember { mutableStateOf("two_tier") }
    var isRoutingExpanded by remember { mutableStateOf(false) }

    val routingOptions = listOf(
        Pair("two_tier", "Two-Tier: Reporting Manager -> Branch HR Admin"),
        Pair("single_tier", "Single-Tier: Direct Supervisor")
    )

    var postFactoApproval by remember { mutableStateOf(false) }
    var postFactoHours by remember { mutableStateOf("48") }
    var hrHeadSignOff by remember { mutableStateOf(false) }
    var hrSignOffDays by remember { mutableStateOf("5") }

    val isLoadingDetail by viewModel.isLoadingLeaveTypeDetail.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingLeaveType.collectAsStateWithLifecycle()

    // ─────────────────────────────────────────────────────────────
    // Prefill Form in Edit Mode
    // ─────────────────────────────────────────────────────────────
    LaunchedEffect(leaveTypeIdToEdit) {
        if (!leaveTypeIdToEdit.isNullOrBlank()) {
            viewModel.fetchLeaveTypeById(leaveTypeIdToEdit) { item ->
                leaveTypeName = item.leaveTypeName
                shortCode = item.shortCode
                description = item.description
                compensationType = item.compensationType.lowercase()
                category = item.category
                selectedColor = item.colorCode.ifBlank { "#7654B8" }
                effectiveFrom = parseIsoToDisplay(item.effectiveFrom)
                effectiveUntil = parseIsoToDisplay(item.effectiveUntil)
                isOpenEnded = item.isOpenEnded

                allocationMethod = item.allocationMethod
                annualEntitlement = if (item.daysPerYear > 0) item.daysPerYear.toString() else ""
                leaveCycle = item.leaveYearCycle
                maxAccumulated = if (item.maxAccumulatedBalance > 0) item.maxAccumulatedBalance.toString() else ""
                prorateForMidYearJoiners = item.prorateForMidYearJoiners

                // Eligibility
                audienceScope = item.eligibility.audienceScope
                selectedEmploymentTypes.clear()
                selectedEmploymentTypes.addAll(item.eligibility.employmentClassifications)
                departmentScope = if (item.eligibility.allDepartments) "all_dept" else "selected_dept"
                val matchedMin = minServiceOptions.firstOrNull { it.third.first == item.eligibility.minServiceRequirement.value }
                minServiceId = matchedMin?.first ?: "none"
                locations.clear()
                locations.addAll(item.eligibility.locations)
                availableInProbation = item.eligibility.availableDuringProbation

                // Usage rules
                allowHalfDay = item.usageRules.allowHalfDay
                allowHourly = item.usageRules.allowHourly
                minLeaveDuration = item.usageRules.minimumDuration.toString()
                maxConsecutiveDays = if (item.usageRules.maxConsecutiveDays > 0) item.usageRules.maxConsecutiveDays.toString() else ""
                advanceNoticeId = item.usageRules.advanceNoticeDays.toString()
                emergencySameDay = item.usageRules.allowEmergencyWaiver
                allowNegativeBalance = item.usageRules.allowNegativeBalance
                includeInterveningWeekends = item.usageRules.includeInterveningWeekends
                requireSupportingDocs = item.usageRules.requireSupportingDocuments
                docsAfterDays = if (item.usageRules.documentRequiredAfterDays > 0) item.usageRules.documentRequiredAfterDays.toString() else ""

                // Carry forward & encashment
                enableCarryForward = item.carryForward.enabled
                maxCarryForwardLimit = if (item.carryForward.maxDays > 0) item.carryForward.maxDays.toString() else ""
                expiryId = if (item.carryForward.expiryAfterMonths == 3) "q1" else "none"
                excessBalanceTreatment = item.carryForward.excessTreatment

                encashmentRate = item.encashment.rateCalculation
                encashmentCeiling = if (item.encashment.annualCeilingDays > 0) item.encashment.annualCeilingDays.toString() else ""

                // Approval
                requiresFormalApproval = item.approval.requiresApproval
                routingId = if (item.approval.chain.size > 1) "two_tier" else "single_tier"
                postFactoApproval = item.approval.postFactoApproval.enabled
                postFactoHours = item.approval.postFactoApproval.withinHours.toString()
                hrHeadSignOff = item.approval.hrHeadSignOff.enabled
                hrSignOffDays = item.approval.hrHeadSignOff.whenDurationExceedsDays.toString()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Reactive Readiness Checklist Checks
    // ─────────────────────────────────────────────────────────────
    val cleanCode = shortCode.trim().uppercase()
    val needsDays = allocationMethod in listOf("fixed_annual", "monthly_accrual")
    val daysValue = annualEntitlement.toDoubleOrNull() ?: 0.0

    val check1Passed = leaveTypeName.isNotBlank() && cleanCode.isNotBlank() && effectiveFrom.isNotBlank()
    val check2Passed = allocationMethod.isNotBlank() && leaveCycle.isNotBlank() && (!needsDays || daysValue > 0)
    val check3Passed = audienceScope == "all_employees" || selectedEmploymentTypes.isNotEmpty()
    val check4Passed = (minLeaveDuration.toDoubleOrNull() ?: 0.0) > 0.0 &&
            (!requireSupportingDocs || (docsAfterDays.toIntOrNull() ?: 0) > 0)
    val check5Passed = (!enableCarryForward || (maxCarryForwardLimit.toIntOrNull() ?: 0) > 0) &&
            (excessBalanceTreatment != "encash" || (encashmentCeiling.toIntOrNull() ?: 0) > 0)
    val check6Passed = !requiresFormalApproval || routingId.isNotBlank()

    val checks = listOf(
        Pair("Basic Details Defined", check1Passed),
        Pair("Allowance & Cycle Configured", check2Passed),
        Pair("Eligibility Rules Validated", check3Passed),
        Pair("Usage Constraints Checked", check4Passed),
        Pair("Carry Forward Simulation OK", check5Passed),
        Pair("Approval Route Mapped", check6Passed)
    )
    val passedCount = checks.count { it.second }
    val allPassed = passedCount == checks.size

    // ─────────────────────────────────────────────────────────────
    // Policy Simulation Calculation
    // ─────────────────────────────────────────────────────────────
    val simulationText = remember(enableCarryForward, maxCarryForwardLimit, excessBalanceTreatment, encashmentCeiling, expiryId) {
        val limit = maxCarryForwardLimit.toIntOrNull() ?: 0
        if (!enableCarryForward || limit <= 0) null
        else {
            val excess = 4
            val unused = limit + excess
            val treatment = if (excessBalanceTreatment == "encash") {
                val ceiling = encashmentCeiling.toIntOrNull() ?: excess
                "encash ${minOf(excess, ceiling)} of the remaining $excess days in payroll"
            } else {
                "expire the remaining $excess days"
            }
            val expiryText = if (expiryId == "q1") " (valid until 31 Mar of following year)" else ""
            "An employee with $unused unused days at year-end will carry forward $limit days$expiryText, and $treatment."
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Save / Publish Mutation Function
    // ─────────────────────────────────────────────────────────────
    fun saveLeaveType(status: String) {
        val minServ = minServiceOptions.firstOrNull { it.first == minServiceId }?.third ?: Pair(0, "months")
        val noticeDays = advanceNoticeId.toIntOrNull() ?: 0
        val expMonths = if (expiryId == "q1") 3 else 0
        val isEncash = enableCarryForward && excessBalanceTreatment == "encash"

        val chainList = if (routingId == "two_tier") {
            listOf(
                ApprovalChainNodeDto(level = 1, approverRole = "reporting_manager"),
                ApprovalChainNodeDto(level = 2, approverRole = "branch_hr")
            )
        } else {
            listOf(ApprovalChainNodeDto(level = 1, approverRole = "reporting_manager"))
        }

        val payload = LeaveTypeItemDto(
            id = leaveTypeIdToEdit.orEmpty(),
            leaveTypeName = leaveTypeName.trim(),
            shortCode = cleanCode,
            description = description.trim(),
            compensationType = compensationType,
            category = category,
            colorCode = selectedColor,
            effectiveFrom = parseDisplayToIso(effectiveFrom),
            effectiveUntil = if (isOpenEnded || effectiveUntil.isBlank()) null else parseDisplayToIso(effectiveUntil),
            isOpenEnded = isOpenEnded,
            status = status,
            allocationMethod = allocationMethod,
            daysPerYear = if (allocationMethod == "no_quota") 0 else (annualEntitlement.toIntOrNull() ?: 0),
            leaveYearCycle = leaveCycle,
            maxAccumulatedBalance = maxAccumulated.toIntOrNull() ?: 0,
            prorateForMidYearJoiners = prorateForMidYearJoiners,
            eligibility = LeaveEligibilityDto(
                audienceScope = audienceScope,
                employmentClassifications = if (audienceScope == "selected_groups") selectedEmploymentTypes.toList() else emptyList(),
                allDepartments = departmentScope == "all_dept",
                departments = emptyList(),
                locations = locations.toList(),
                availableDuringProbation = availableInProbation,
                minServiceRequirement = MinServiceRequirementDto(
                    value = minServ.first,
                    unit = minServ.second
                )
            ),
            usageRules = LeaveUsageRulesDto(
                allowHalfDay = allowHalfDay,
                allowHourly = allowHourly,
                minimumDuration = minLeaveDuration.toDoubleOrNull() ?: 0.5,
                maxConsecutiveDays = maxConsecutiveDays.toIntOrNull() ?: 0,
                advanceNoticeDays = noticeDays,
                allowEmergencyWaiver = emergencySameDay,
                allowNegativeBalance = allowNegativeBalance,
                includeInterveningWeekends = includeInterveningWeekends,
                requireSupportingDocuments = requireSupportingDocs,
                documentRequiredAfterDays = if (requireSupportingDocs) (docsAfterDays.toIntOrNull() ?: 0) else 0
            ),
            carryForward = LeaveCarryForwardDto(
                enabled = enableCarryForward,
                maxDays = if (enableCarryForward) (maxCarryForwardLimit.toIntOrNull() ?: 0) else 0,
                expiryAfterMonths = if (enableCarryForward) expMonths else 0,
                excessTreatment = excessBalanceTreatment
            ),
            encashment = LeaveEncashmentDto(
                enabled = isEncash,
                rateCalculation = encashmentRate,
                annualCeilingDays = if (isEncash) (encashmentCeiling.toIntOrNull() ?: 0) else 0
            ),
            approval = LeaveApprovalDto(
                requiresApproval = requiresFormalApproval,
                chain = if (requiresFormalApproval) chainList else emptyList(),
                postFactoApproval = PostFactoApprovalDto(
                    enabled = requiresFormalApproval && postFactoApproval,
                    withinHours = postFactoHours.toIntOrNull() ?: 48
                ),
                hrHeadSignOff = HrHeadSignOffDto(
                    enabled = requiresFormalApproval && hrHeadSignOff,
                    whenDurationExceedsDays = hrSignOffDays.toIntOrNull() ?: 5
                )
            )
        )

        if (isEditMode) {
            viewModel.updateLeaveType(
                id = leaveTypeIdToEdit,
                request = payload,
                onSuccess = { onSavedSuccess() }
            )
        } else {
            viewModel.createLeaveType(
                request = payload,
                onSuccess = { onSavedSuccess() }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Title Bar
        TitleBar(
            title = if (isEditMode) "Edit Leave Type" else "Create Leave Type",
            onClose = onClose
        )

        if (isLoadingDetail && isEditMode && leaveTypeName.isBlank()) {
            ListSkeleton()
            return
        }

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─────────────────────────────────────────────────────────────
            // SECTION 01: Basic Details & Classification
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "01", title = "Basic Details & Classification") {
                SectionBadgeHeader(
                    code = cleanCode.ifBlank { "PL" },
                    name = leaveTypeName.ifBlank { "Privilege Leave" },
                    colorHex = selectedColor
                )

                FormLabel(text = "LEAVE TYPE NAME", isRequired = true)
                FormTextField(
                    value = leaveTypeName,
                    onValueChange = { leaveTypeName = it },
                    placeholder = "e.g. Privilege Leave / Annual Leave"
                )

                FormLabel(text = "SHORT CODE", isRequired = true)
                FormTextField(
                    value = shortCode,
                    onValueChange = { shortCode = it.take(6).uppercase() },
                    placeholder = "e.g. PL"
                )

                FormLabel(text = "DESCRIPTION / POLICY CONTEXT", isRequired = false)
                FormTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Describe when and why this leave is granted..."
                )

                FormLabel(text = "COMPENSATION CLASSIFICATION", isRequired = true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(light_blue_border.copy(alpha = 0.35f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    compensationOptions.forEach { opt ->
                        val isSelected = compensationType == opt.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.fieldHeight * 0.85f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                                .background(if (isSelected) Primary else Color.Transparent)
                                .clickable { compensationType = opt.first },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.second,
                                color = if (isSelected) whiteBg else TextPrimary,
                                fontSize = tokens.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                FormDropdown(
                    label = "CATEGORY",
                    value = category,
                    expanded = isCategoryDropdownExpanded,
                    onExpandChange = { isCategoryDropdownExpanded = it },
                    options = categoryOptions,
                    onOptionSelected = { category = it },
                    isRequired = true
                )

                FormLabel(text = "DISPLAY TAG PALETTE COLOR", isRequired = false)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    paletteList.forEach { p ->
                        val c = remember(p.first) {
                            try { Color(p.first.toColorInt()) } catch (_: Exception) { Color(0xFF7654B8) }
                        }
                        val isSelected = selectedColor.equals(p.first, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(tokens.fieldHeight * 0.7f)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = p.first },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.75f))
                            }
                        }
                    }
                }

                FormLabel(text = "EFFECTIVE FROM", isRequired = true)
                DatePickerField(
                    value = effectiveFrom,
                    onDateSelected = { effectiveFrom = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormLabel(text = "EFFECTIVE UNTIL", isRequired = false)
                    Text(
                        text = if (isOpenEnded) "Set Expiry" else "Clear Expiry",
                        color = Primary,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isOpenEnded = !isOpenEnded
                            if (isOpenEnded) effectiveUntil = ""
                        }
                    )
                }
                if (isOpenEnded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(light_grey)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("Open Ended (No expiry)", color = TextSecondary, fontSize = tokens.bodySmall)
                    }
                } else {
                    DatePickerField(
                        value = effectiveUntil,
                        onDateSelected = { effectiveUntil = it }
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────
            // SECTION 02: Leave Allowance & Accrual Method
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "02", title = "Leave Allowance & Accrual Method") {
                Text(
                    text = "Configure the default entitlement quota and calculation basis for eligible operational staff.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                FormLabel(text = "ENTITLEMENT ALLOCATION METHOD", isRequired = true)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllocationTile(
                            title = "Fixed Annual",
                            icon = Icons.Default.CalendarMonth,
                            isSelected = allocationMethod == "fixed_annual",
                            modifier = Modifier.weight(1f)
                        ) { allocationMethod = "fixed_annual" }

                        AllocationTile(
                            title = "Monthly Accrual",
                            icon = Icons.Default.Refresh,
                            isSelected = allocationMethod == "monthly_accrual",
                            modifier = Modifier.weight(1f)
                        ) { allocationMethod = "monthly_accrual" }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllocationTile(
                            title = "Per-Event",
                            icon = Icons.Default.Person,
                            isSelected = allocationMethod == "per_event",
                            modifier = Modifier.weight(1f)
                        ) { allocationMethod = "per_event" }

                        AllocationTile(
                            title = "Earned / COFF",
                            icon = Icons.Default.Schedule,
                            isSelected = allocationMethod == "earned_coff",
                            modifier = Modifier.weight(1f)
                        ) { allocationMethod = "earned_coff" }
                    }
                    AllocationTile(
                        title = "No Quota",
                        icon = Icons.Default.Block,
                        isSelected = allocationMethod == "no_quota",
                        modifier = Modifier.fillMaxWidth()
                    ) { allocationMethod = "no_quota" }
                }

                if (allocationMethod != "no_quota") {
                    FormLabel(text = "DAYS PER YEAR", isRequired = needsDays)
                    FormTextField(
                        value = annualEntitlement,
                        onValueChange = { annualEntitlement = it.filter { ch -> ch.isDigit() } },
                        keyboardType = KeyboardType.Number,
                        placeholder = "e.g. 18",
                        trailingText = "days / yr"
                    )

                    FormDropdown(
                        label = "LEAVE YEAR CYCLE",
                        value = cycleOptions.firstOrNull { it.first == leaveCycle }?.second ?: "Calendar Year (Jan - Dec)",
                        expanded = isLeaveCycleExpanded,
                        onExpandChange = { isLeaveCycleExpanded = it },
                        options = cycleOptions.map { it.second },
                        onOptionSelected = { selected ->
                            leaveCycle = cycleOptions.firstOrNull { it.second == selected }?.first ?: "calendar_year"
                        }
                    )
                }

                FormLabel(text = "MAX ACCUMULATED BALANCE", isRequired = false)
                FormTextField(
                    value = maxAccumulated,
                    onValueChange = { maxAccumulated = it.filter { ch -> ch.isDigit() } },
                    keyboardType = KeyboardType.Number,
                    placeholder = "0 = no cap",
                    trailingText = "days max"
                )

                SwitchOptionRow(
                    title = "Prorate Allowance for Mid-Year Joiners",
                    subtitle = "Calculate proportional quota based on joining month in the active leave cycle.",
                    checked = prorateForMidYearJoiners,
                    onCheckedChange = { prorateForMidYearJoiners = it }
                )
            }

            // ─────────────────────────────────────────────────────────────
            // SECTION 03: Employee Eligibility & Scope
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "03", title = "Employee Eligibility & Scope") {
                Text(
                    text = "Specify default employee cohorts eligible for this leave type governed by HR Policies.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                FormLabel(text = "APPLICABLE AUDIENCE SCOPE", isRequired = true)
                ScopeSelectOption(
                    title = "All Active Company Employees",
                    isSelected = audienceScope == "all_employees",
                    onClick = { audienceScope = "all_employees" }
                )
                ScopeSelectOption(
                    title = "Selected Employee Groups & Categories",
                    isSelected = audienceScope == "selected_groups",
                    onClick = { audienceScope = "selected_groups" }
                )

                if (audienceScope == "selected_groups") {
                    FormLabel(text = "EMPLOYMENT CLASSIFICATIONS", isRequired = false)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedEmploymentTypes.forEach { tag ->
                            RemovableChipItem(text = tag) { selectedEmploymentTypes.remove(tag) }
                        }
                    }
                    Text(
                        text = "+ Add Employment Type",
                        fontSize = tokens.label,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(light_blue_border.copy(alpha = 0.35f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clickable {
                                if (!selectedEmploymentTypes.contains("Permanent")) selectedEmploymentTypes.add("Permanent")
                                else if (!selectedEmploymentTypes.contains("Contract")) selectedEmploymentTypes.add("Contract")
                            }
                    )
                }

                FormDropdown(
                    label = "DEPARTMENT SCOPE",
                    value = departmentScopeOptions.firstOrNull { it.first == departmentScope }?.second ?: "All Departments",
                    expanded = isDepartmentScopeExpanded,
                    onExpandChange = { isDepartmentScopeExpanded = it },
                    options = departmentScopeOptions.map { it.second },
                    onOptionSelected = { selected ->
                        departmentScope = departmentScopeOptions.firstOrNull { it.second == selected }?.first ?: "all_dept"
                    }
                )

                FormDropdown(
                    label = "MINIMUM SERVICE REQUIREMENT",
                    value = minServiceOptions.firstOrNull { it.first == minServiceId }?.second ?: "No minimum service",
                    expanded = isMinServiceExpanded,
                    onExpandChange = { isMinServiceExpanded = it },
                    options = minServiceOptions.map { it.second },
                    onOptionSelected = { selected ->
                        minServiceId = minServiceOptions.firstOrNull { it.second == selected }?.first ?: "none"
                    }
                )

                FormLabel(text = "LOCATION & WORKSHOP BRANCHES", isRequired = false)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    locations.forEach { loc ->
                        RemovableChipItem(text = loc) { locations.remove(loc) }
                    }
                }
                Text(
                    text = "+ Add Branch Location",
                    fontSize = tokens.label,
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(light_blue_border.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable {
                            if (!locations.contains("Mumbai HQ")) locations.add("Mumbai HQ")
                            else if (!locations.contains("New Delhi Flagship")) locations.add("New Delhi Flagship")
                        }
                )

                SwitchOptionRow(
                    title = "Available During Probationary Tenure",
                    subtitle = "Permit employees to claim earned quota during probation.",
                    checked = availableInProbation,
                    onCheckedChange = { availableInProbation = it }
                )
            }

            // ─────────────────────────────────────────────────────────────
            // SECTION 04: Leave Usage & Request Constraints
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "04", title = "Leave Usage & Request Constraints") {
                SwitchOptionRow(
                    title = "Allow Half-Day Requests",
                    subtitle = "Permits 0.5 day morning/afternoon leave.",
                    checked = allowHalfDay,
                    onCheckedChange = { allowHalfDay = it }
                )

                SwitchOptionRow(
                    title = "Allow Hourly Increments",
                    subtitle = if (allowHourly) "Permits leave requests in hours." else "Restricted: full and half-day blocks only.",
                    checked = allowHourly,
                    onCheckedChange = { allowHourly = it }
                )

                FormLabel(text = "MINIMUM DURATION", isRequired = false)
                FormTextField(
                    value = minLeaveDuration,
                    onValueChange = { minLeaveDuration = it },
                    placeholder = "0.5",
                    trailingText = "Day"
                )

                FormLabel(text = "MAX CONSECUTIVE DAYS", isRequired = false)
                FormTextField(
                    value = maxConsecutiveDays,
                    onValueChange = { maxConsecutiveDays = it.filter { ch -> ch.isDigit() } },
                    placeholder = "0 = no limit",
                    trailingText = "Working Days"
                )

                FormDropdown(
                    label = "ADVANCE NOTICE REQUIREMENT",
                    value = advanceNoticeOptions.firstOrNull { it.first == advanceNoticeId }?.second ?: "No advance notice",
                    expanded = isAdvanceNoticeExpanded,
                    onExpandChange = { isAdvanceNoticeExpanded = it },
                    options = advanceNoticeOptions.map { it.second },
                    onOptionSelected = { selected ->
                        advanceNoticeId = advanceNoticeOptions.firstOrNull { it.second == selected }?.first ?: "0"
                    }
                )

                SwitchOptionRow(
                    title = "Emergency / Same-Day",
                    subtitle = "Allow emergency waivers for sickness.",
                    checked = emergencySameDay,
                    onCheckedChange = { emergencySameDay = it }
                )

                SwitchOptionRow(
                    title = "Allow Negative Balance",
                    subtitle = if (allowNegativeBalance) "Employees can borrow against future quota." else "Cannot borrow unearned quotas.",
                    checked = allowNegativeBalance,
                    onCheckedChange = { allowNegativeBalance = it }
                )

                SwitchOptionRow(
                    title = "Include Intervening Weekends",
                    subtitle = if (includeInterveningWeekends) "Weekends between leave days count as leave." else "Sandwich leave rule disabled.",
                    checked = includeInterveningWeekends,
                    onCheckedChange = { includeInterveningWeekends = it }
                )

                SwitchOptionRow(
                    title = "Require Supporting Documents",
                    subtitle = "Mandatory document attachment for requests exceeding duration limit.",
                    checked = requireSupportingDocs,
                    onCheckedChange = { requireSupportingDocs = it }
                )

                if (requireSupportingDocs) {
                    FormLabel(text = "REQUIRED AFTER DAYS", isRequired = true)
                    FormTextField(
                        value = docsAfterDays,
                        onValueChange = { docsAfterDays = it.filter { ch -> ch.isDigit() } },
                        placeholder = "e.g. 4",
                        trailingText = "working days"
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────
            // SECTION 05: Carry Forward & Encashment Rules
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "05", title = "Carry Forward & Encashment Rules") {
                SwitchOptionRow(
                    title = "Enable Year-End Carry Forward",
                    subtitle = "Unused quotas roll into the subsequent calendar cycle under corporate limits.",
                    checked = enableCarryForward,
                    onCheckedChange = { enableCarryForward = it }
                )

                if (enableCarryForward) {
                    FormLabel(text = "MAXIMUM CARRY FORWARD LIMIT", isRequired = true)
                    FormTextField(
                        value = maxCarryForwardLimit,
                        onValueChange = { maxCarryForwardLimit = it.filter { ch -> ch.isDigit() } },
                        placeholder = "e.g. 10",
                        trailingText = "Days"
                    )

                    FormDropdown(
                        label = "CARRIED DAYS EXPIRATION",
                        value = expiryOptions.firstOrNull { it.first == expiryId }?.second ?: "No Expiration",
                        expanded = isExpiryExpanded,
                        onExpandChange = { isExpiryExpanded = it },
                        options = expiryOptions.map { it.second },
                        onOptionSelected = { selected ->
                            expiryId = expiryOptions.firstOrNull { it.second == selected }?.first ?: "none"
                        }
                    )

                    FormLabel(text = "EXCESS BALANCE TREATMENT", isRequired = false)
                    ScopeSelectOption(
                        title = "Expire excess days unconditionally",
                        isSelected = excessBalanceTreatment == "expire",
                        onClick = { excessBalanceTreatment = "expire" }
                    )
                    ScopeSelectOption(
                        title = "Allow Encashment of Excess in Payroll",
                        isSelected = excessBalanceTreatment == "encash",
                        onClick = { excessBalanceTreatment = "encash" }
                    )

                    if (excessBalanceTreatment == "encash") {
                        FormDropdown(
                            label = "ENCASHMENT RATE CALCULATION",
                            value = encashmentRateOptions.firstOrNull { it.first == encashmentRate }?.second ?: "Basic Pay / 30 per day",
                            expanded = isEncashmentRateExpanded,
                            onExpandChange = { isEncashmentRateExpanded = it },
                            options = encashmentRateOptions.map { it.second },
                            onOptionSelected = { selected ->
                                encashmentRate = encashmentRateOptions.firstOrNull { it.second == selected }?.first ?: "basic_per_30"
                            }
                        )

                        FormLabel(text = "ANNUAL ENCASHMENT CEILING", isRequired = true)
                        FormTextField(
                            value = encashmentCeiling,
                            onValueChange = { encashmentCeiling = it.filter { ch -> ch.isDigit() } },
                            placeholder = "e.g. 8",
                            trailingText = "days / year"
                        )
                    }

                    simulationText?.let { sim ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue_border.copy(alpha = 0.25f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Policy Simulation Example:", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = Primary)
                                    Text(sim, fontSize = tokens.caption, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // SECTION 06: Approval Hierarchy & Governance
            // ─────────────────────────────────────────────────────────────
            SectionCard(sectionNumber = "06", title = "Approval Hierarchy & Governance") {
                SwitchOptionRow(
                    title = "Requires Formal Approval Routing",
                    subtitle = "Requests must clear supervisory hierarchy prior to leave recording.",
                    checked = requiresFormalApproval,
                    onCheckedChange = { requiresFormalApproval = it }
                )

                if (requiresFormalApproval) {
                    FormDropdown(
                        label = "APPROVAL ROUTING CHAIN",
                        value = routingOptions.firstOrNull { it.first == routingId }?.second ?: "Two-Tier: Reporting Manager -> Branch HR Admin",
                        expanded = isRoutingExpanded,
                        onExpandChange = { isRoutingExpanded = it },
                        options = routingOptions.map { it.second },
                        onOptionSelected = { selected ->
                            routingId = routingOptions.firstOrNull { it.second == selected }?.first ?: "two_tier"
                        },
                        isRequired = true
                    )

                    SwitchOptionRow(
                        title = "Post-Facto Approval",
                        subtitle = if (postFactoApproval) "Allow submission within ${postFactoHours}h of return." else "Allow submission after return.",
                        checked = postFactoApproval,
                        onCheckedChange = { postFactoApproval = it }
                    )
                    if (postFactoApproval) {
                        FormLabel(text = "POST-FACTO SUBMISSION WINDOW", isRequired = false)
                        FormTextField(
                            value = postFactoHours,
                            onValueChange = { postFactoHours = it.filter { ch -> ch.isDigit() } },
                            placeholder = "48",
                            trailingText = "hours"
                        )
                    }

                    SwitchOptionRow(
                        title = "HR Head Sign-Off",
                        subtitle = if (hrHeadSignOff) "Mandatory when duration > $hrSignOffDays days." else "Mandatory for long-duration requests.",
                        checked = hrHeadSignOff,
                        onCheckedChange = { hrHeadSignOff = it }
                    )
                    if (hrHeadSignOff) {
                        FormLabel(text = "HR HEAD ESCALATION THRESHOLD", isRequired = false)
                        FormTextField(
                            value = hrSignOffDays,
                            onValueChange = { hrSignOffDays = it.filter { ch -> ch.isDigit() } },
                            placeholder = "5",
                            trailingText = "days"
                        )
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // Readiness Checklist Card
            // ─────────────────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CONFIGURATION READINESS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                        Text(
                            text = "$passedCount of ${checks.size} Complete",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = if (allPassed) greentext else yellowText
                        )
                    }

                    checks.forEach { (title, passed) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (passed) greentext else yellowText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(title, fontSize = tokens.caption, color = if (passed) TextPrimary else TextSecondary)
                        }
                    }

                    if (allPassed) {
                        Surface(
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                            color = greenBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ready for publication. Will be immediately available for assignment in HR Leave Policies.",
                                fontSize = tokens.caption,
                                color = greentext,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // Bottom Action Controls (Save Draft / Publish)
            // ─────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { saveLeaveType("draft") },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text("Save Draft", color = TextPrimary, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { saveLeaveType("active") },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isEditMode) "Update Leave Type" else "Preview & Publish",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Sub-Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    sectionNumber: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(light_blue_border.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SECTION $sectionNumber",
                    color = Primary,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = title_color
            )
            content()
        }
    }
}

@Composable
private fun SectionBadgeHeader(code: String, name: String, colorHex: String) {
    val tokens = LocalAppTokens.current
    val parsedColor = remember(colorHex) {
        try { Color(colorHex.toColorInt()) } catch (_: Exception) { Color(0xFF7654B8) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(light_blue_border.copy(alpha = 0.25f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(parsedColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                color = whiteBg,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            color = Primary,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AllocationTile(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(if (isSelected) Primary else light_blue_border.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) whiteBg else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) whiteBg else TextPrimary
            )
        }
    }
}

@Composable
private fun SwitchOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(Primary_background)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }
        MiniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ScopeSelectOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(
                1.dp,
                if (isSelected) Primary else sectionBorder,
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .background(if (isSelected) light_blue_border.copy(alpha = 0.2f) else whiteBg)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun RemovableChipItem(text: String, onRemove: () -> Unit) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(light_blue_border.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                fontSize = tokens.caption,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove classification",
                tint = Primary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

private fun parseIsoToDisplay(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val date = isoFormat.parse(isoString)
        if (date != null) displayFormat.format(date) else ""
    } catch (_: Exception) {
        ""
    }
}

private fun parseDisplayToIso(displayString: String): String? {
    if (displayString.isBlank()) return null
    return try {
        val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date = displayFormat.parse(displayString)
        if (date != null) isoFormat.format(date) else null
    } catch (_: Exception) {
        null
    }
}