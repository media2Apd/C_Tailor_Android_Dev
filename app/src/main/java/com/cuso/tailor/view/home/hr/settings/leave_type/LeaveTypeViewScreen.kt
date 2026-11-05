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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.LeaveTypeItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LeaveTypeViewScreen(
    leaveTypeId: String,
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Trigger API fetch on screen launch
    LaunchedEffect(leaveTypeId) {
        if (leaveTypeId.isNotBlank()) {
            viewModel.fetchLeaveTypeById(leaveTypeId)
        }
    }

    // Collect fetched detail from ViewModel with explicit type
    val leaveItem: LeaveTypeItemDto? by viewModel.selectedLeaveTypeDetail.collectAsStateWithLifecycle()
    val isLoading: Boolean by viewModel.isLoadingLeaveTypeDetail.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Activity History", "Linked Employees")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Header Bar
        TitleBar(
            title = leaveItem?.leaveTypeName?.ifBlank { "Leave Type Details" } ?: "Leave Type Details",
            onClose = {
                viewModel.clearSelectedLeaveTypeDetail()
                onClose()
            },
            trailingContent = {
                leaveItem?.let { currentItem ->
                    IconButton(onClick = { onEdit(currentItem.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Leave Type",
                            tint = Primary
                        )
                    }
                }
            }
        )

        // Breadcrumb Trail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("HR Settings", fontSize = tokens.caption, color = headerGrey)
            Text(">", fontSize = tokens.caption, color = headerGrey)
            Text("Leave Types", fontSize = tokens.caption, color = headerGrey)
            Text(">", fontSize = tokens.caption, color = headerGrey)
            Text(
                text = leaveItem?.leaveTypeName ?: "Details",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }

        Spacer(Modifier.height(8.dp))

        if (isLoading && leaveItem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return
        }

        val item: LeaveTypeItemDto? = leaveItem
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Leave type not found.", color = TextSecondary, fontSize = tokens.bodyMedium)
            }
            return
        }

        val parsedColor = remember(item.colorCode) {
            try {
                Color(item.colorCode.toColorInt())
            } catch (_: Exception) {
                Color(0xFF6366F1)
            }
        }

        val isPublished = item.status.equals("active", ignoreCase = true) || item.status.equals("published", ignoreCase = true)

        // Identity Card Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.fieldHeight * 1.1f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(parsedColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.shortCode.ifBlank { item.leaveTypeName.take(2).uppercase() },
                        color = parsedColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = tokens.bodyMedium
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.leaveTypeName,
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPublished) greenBg else yellowBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPublished) "• Published" else "• Draft",
                                color = if (isPublished) greentext else yellowText,
                                fontSize = tokens.label
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(light_blue_border.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.compensationType.replaceFirstChar { it.uppercase() },
                                color = Primary,
                                fontSize = tokens.label
                            )
                        }
                    }

                    if (item.description.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Navigation Tabs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = tokens.caption,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Primary else TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) Primary else Color.Transparent)
                    )
                }
            }
        }

        HorizontalDivider(color = sectionBorder, thickness = 1.dp)

        // Tab Content Switching
        when (selectedTab) {
            0 -> OverviewTabContent(item = item)
            1 -> ActivityHistoryTabContent(item = item)
            2 -> LinkedEmployeesTabContent(item = item)
        }
    }
}

@Composable
private fun OverviewTabContent(item: LeaveTypeItemDto) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val parsedColor = remember(item.colorCode) {
        try { Color(item.colorCode.toColorInt()) } catch (_: Exception) { Color(0xFF6366F1) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Annual Entitlement Section
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ANNUAL ENTITLEMENT", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(yellowBg.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.allocationMethod.replace('_', ' ').replaceFirstChar { it.uppercase() },
                            color = Primary,
                            fontSize = tokens.label
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${item.daysPerYear} days",
                        fontSize = tokens.h1,
                        fontWeight = FontWeight.Bold,
                        color = parsedColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("/ calendar year", fontSize = tokens.caption, color = TextSecondary)
                }

                Text(
                    text = "Effective cycle: ${formatDisplayDate(item.effectiveFrom)} to ${if (item.isOpenEnded || item.effectiveUntil.isNullOrBlank()) "Open Ended" else formatDisplayDate(item.effectiveUntil)}.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                // Sub Allocation Details Box
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = Primary_background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("ALLOCATION METHOD", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    item.allocationMethod.replace('_', ' ').uppercase(),
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Column {
                                Text("LEAVE PERIOD", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    item.leaveYearCycle.replace('_', ' ').replaceFirstChar { it.uppercase() },
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("PRORATE NEW JOINERS", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    if (item.prorateForMidYearJoiners) "Yes" else "No",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Column {
                                Text("MAX ACCUMULATED", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    "${item.maxAccumulatedBalance} Days Max",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Basic Details Card
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Basic Details", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                DetailFieldRow("Short Code", item.shortCode)
                DetailFieldRow("Classification", item.compensationType.replaceFirstChar { it.uppercase() } + " Leave")
                DetailFieldRow("Category", item.category)
                DetailFieldRow("Display Theme Color", item.colorCode)
                DetailFieldRow(
                    "Effective Window",
                    "${formatDisplayDate(item.effectiveFrom)} - ${if (item.isOpenEnded || item.effectiveUntil.isNullOrBlank()) "Open Ended" else formatDisplayDate(item.effectiveUntil)}"
                )
                DetailFieldRow("Lifecycle State", item.status.replaceFirstChar { it.uppercase() } + " (${item.version})")

                Spacer(Modifier.height(4.dp))
                Text("Description", fontSize = tokens.caption, color = headerGrey)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary_background)
                        .padding(10.dp)
                ) {
                    Text(
                        text = item.description.ifBlank { "No description provided." },
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
            }
        }

        // Employee Eligibility Card
        val eligibility = item.eligibility
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Employee Eligibility", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                DetailFieldRow("Audience Scope", eligibility.audienceScope.replace('_', ' ').replaceFirstChar { it.uppercase() })

                Text("Employment Classifications", fontSize = tokens.caption, color = headerGrey)
                if (eligibility.employmentClassifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        eligibility.employmentClassifications.forEach { EligibilityTag(it) }
                    }
                } else {
                    Text("All Classifications Applicable", fontSize = tokens.bodySmall, color = TextPrimary)
                }

                DetailFieldRow(
                    "Applicable Departments",
                    if (eligibility.allDepartments || eligibility.departments.isEmpty()) "All Departments" else eligibility.departments.joinToString(", ")
                )
                DetailFieldRow(
                    "Branches & Locations",
                    if (eligibility.locations.isEmpty()) "All Active Branches" else eligibility.locations.joinToString(", ")
                )
                DetailFieldRow(
                    "Minimum Service",
                    "${eligibility.minServiceRequirement.value} ${eligibility.minServiceRequirement.unit.replaceFirstChar { it.uppercase() }}"
                )
                DetailFieldRow(
                    "Available During Probation",
                    if (eligibility.availableDuringProbation) "Yes" else "No (Subject to HR override)"
                )
            }
        }

        // Usage Rules & Constraints Card
        val rules = item.usageRules
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Usage Rules & Constraints", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)

                RuleToggleItem("Half-Day Requests Allowed", if (rules.allowHalfDay) "Permitted" else "Disabled", enabled = rules.allowHalfDay)
                RuleToggleItem("Hourly Leaves Allowed", if (rules.allowHourly) "Permitted" else "Disabled", enabled = rules.allowHourly)
                RuleToggleItem("Emergency / Same-Day Requests", if (rules.allowEmergencyWaiver) "Allowed (Auto-notifies HR)" else "Disabled", enabled = rules.allowEmergencyWaiver)
                RuleToggleItem("Negative Balance Borrowing", if (rules.allowNegativeBalance) "Allowed" else "Restricted (No overdraft)", enabled = rules.allowNegativeBalance)
                RuleToggleItem("Include Intervening Weekends", if (rules.includeInterveningWeekends) "Counted as leave" else "Excluded", enabled = rules.includeInterveningWeekends)
                RuleToggleItem("Require Supporting Documents", if (rules.requireSupportingDocuments) "Required after ${rules.documentRequiredAfterDays} days" else "Not required", enabled = rules.requireSupportingDocuments)

                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = Primary_background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("MIN DURATION", fontSize = tokens.label, color = headerGrey)
                                Text("${rules.minimumDuration} Day(s)", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Column {
                                Text("MAX CONSECUTIVE", fontSize = tokens.label, color = headerGrey)
                                Text("${rules.maxConsecutiveDays} Working Days", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("ADVANCE NOTICE", fontSize = tokens.label, color = headerGrey)
                                Text("${rules.advanceNoticeDays} Days in Advance", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Column {
                                Text("DOCUMENT REQUIREMENT", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    if (rules.requireSupportingDocuments) "> ${rules.documentRequiredAfterDays} days" else "None",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Carry Forward & Encashment Card
        val cf = item.carryForward
        val enc = item.encashment
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Carry Forward & Encashment", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("CARRY FORWARD", fontSize = tokens.label, color = headerGrey)
                        Text(
                            if (cf.enabled) "Enabled (Max ${cf.maxDays}d)" else "Disabled (Lapses)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = if (cf.enabled) greentext else redText
                        )
                    }
                    Column {
                        Text("EXCESS TREATMENT", fontSize = tokens.label, color = headerGrey)
                        Text(
                            cf.excessTreatment.replaceFirstChar { it.uppercase() },
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PAYROLL ENCASHMENT", fontSize = tokens.label, color = headerGrey)
                        Text(
                            if (enc.enabled) "Enabled (Ceiling: ${enc.annualCeilingDays}d)" else "Not Allowed",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text("RATE CALCULATION", fontSize = tokens.label, color = headerGrey)
                        Text(
                            enc.rateCalculation.replace('_', ' ').uppercase(),
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Approval Routing Card
        val app = item.approval
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Approval Hierarchy & Governance", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                DetailFieldRow("Requires Approval", if (app.requiresApproval) "Yes (Mandatory)" else "No (Auto-approved)")

                if (app.chain.isNotEmpty()) {
                    Text("Approval Routing Chain", fontSize = tokens.caption, color = headerGrey)
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = Primary_background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            app.chain.forEach { node ->
                                Text(
                                    text = "Tier ${node.level}: ${node.approverRole.replace('_', ' ').replaceFirstChar { it.uppercase() }}",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                DetailFieldRow(
                    "Post-Facto Submission",
                    if (app.postFactoApproval.enabled) "Within ${app.postFactoApproval.withinHours} hours" else "Disabled"
                )
                DetailFieldRow(
                    "HR Head Escalation",
                    if (app.hrHeadSignOff.enabled) "When duration exceeds ${app.hrHeadSignOff.whenDurationExceedsDays} days" else "Disabled"
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryTabContent(item: LeaveTypeItemDto) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Activity History", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
        Text("Track changes and actions performed on this leave type.", fontSize = tokens.caption, color = TextSecondary)

        // Metadata Card from API
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("LEAVE TYPE METADATA SUMMARY", fontSize = tokens.label, color = headerGrey, fontWeight = FontWeight.Bold)
                DetailFieldRow("Created At", formatDisplayDateTime(item.createdAt))
                DetailFieldRow("Last Updated At", formatDisplayDateTime(item.updatedAt))
                DetailFieldRow("Current Version", item.version)
            }
        }

        // Live API Event Log
        AuditTimelineItem(
            title = "Current Version Active",
            performer = "System Admin",
            date = formatDisplayDateTime(item.updatedAt),
            desc = "${item.leaveTypeName} configured with ${item.daysPerYear} days annual allocation.",
            statusBadge = item.status.replaceFirstChar { it.uppercase() }
        )

        AuditTimelineItem(
            title = "Leave Type Created",
            performer = "System Admin",
            date = formatDisplayDateTime(item.createdAt),
            desc = "Initial record established for code ${item.shortCode}."
        )
    }
}

@Composable
private fun LinkedEmployeesTabContent(item: LeaveTypeItemDto) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(tokens.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Audience: ${item.eligibility.audienceScope.replace('_', ' ').replaceFirstChar { it.uppercase() }}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            Text(
                text = "All matching employees automatically inherit this leave type.",
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Sub-components & Parsers
// ─────────────────────────────────────────────────────────────

@Composable
private fun DetailFieldRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = tokens.caption, color = headerGrey)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun EligibilityTag(text: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(light_blue_border.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Primary, fontSize = tokens.caption, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RuleToggleItem(label: String, detail: String, enabled: Boolean) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Primary_background)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(detail, fontSize = tokens.caption, color = if (enabled) greentext else TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (enabled) greentext else BorderGray)
        )
    }
}

@Composable
private fun AuditTimelineItem(
    title: String,
    performer: String,
    date: String,
    desc: String,
    statusBadge: String? = null
) {
    val tokens = LocalAppTokens.current
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(14.dp)
                .border(2.dp, Primary, CircleShape)
                .background(whiteBg)
        )
        Spacer(Modifier.width(10.dp))
        Card(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, sectionBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    statusBadge?.let {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(greenBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(it, color = greentext, fontSize = tokens.label)
                        }
                    }
                }
                Text("Performed by $performer", fontSize = tokens.caption, color = TextSecondary)
                Text(date, fontSize = tokens.label, color = headerGrey)
                Text(desc, fontSize = tokens.caption, color = TextPrimary)
            }
        }
    }
}

private fun formatDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatDisplayDateTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate
    } catch (_: Exception) {
        isoDate
    }
}