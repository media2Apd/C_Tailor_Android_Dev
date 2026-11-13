package com.cuso.tailor.view.home.hr.exit_request

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

private val defaultPendingTasks = listOf(
    "Knowledge Transfer (Team Leads)",
    "Hardware Asset Return (IT Dept)",
    "Offboarding Survey Submission",
    "Final Timesheet Approval"
)

@Composable
fun ManagerApprovalScreen(
    exitRequestId: String,
    viewModel: HrViewModel,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val exitRequest by viewModel.exitRequestDetail.collectAsState()
    val isLoading by viewModel.isLoadingExitRequestDetail.collectAsState()
    val isSubmitting by viewModel.isSubmittingExitDecision.collectAsState()

    var remarks by remember { mutableStateOf("") }
    // Track which button triggered the submission ("Approved" or "Rejected")
    var actionLoading by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exitRequestId) {
        viewModel.fetchExitRequestById(exitRequestId)
    }

    LaunchedEffect(isSubmitting) {
        if (!isSubmitting) {
            actionLoading = null
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearExitRequestDetail() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Manager Approval",
            onClose = onClose
        )

        when {
            isLoading && exitRequest == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            exitRequest == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Exit request not found",
                        fontSize = tokens.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            else -> {
                val request = exitRequest!!
                val memberName = request.organizationMemberId.extractMemberName()
                val designationName = request.designationId.extractEntityName("Employee")
                val departmentName = request.departmentId.extractEntityName("-")
                val branchName = request.branchId.extractEntityName("Headquarters")
                val joinDate = formatExitDate(request.organizationMemberId.extractDoj())
                val displayCode = request.exitCode?.takeIf { it.isNotBlank() }
                    ?: "EX-${request.id.takeLast(8).uppercase()}"

                // Match Manager_Review pending approval first, then fallback to any Manager_Review entry
                val activeApproval = request.approvals.find {
                    it.stage == "Manager_Review" && it.status.equals("Pending", ignoreCase = true)
                } ?: request.approvals.find {
                    it.stage == "Manager_Review"
                }

                // Check activeApproval status first so Approved/Rejected decisions hide the action buttons
                val isPending = if (activeApproval != null) {
                    activeApproval.status.equals("Pending", ignoreCase = true)
                } else {
                    request.status in listOf("Submitted", "Pending_Approval")
                }

                // Reflect the manager review decision in the top status badge when recorded
                val effectiveStatus = activeApproval?.status
                    ?.takeIf { !it.equals("Pending", ignoreCase = true) }
                    ?: request.status
                val statusColors = resolveExitStatusColors(effectiveStatus)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                ) {
                    // Header summary row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                        ) {
                            Text(
                                text = "Manager Approval",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            Text(
                                text = "ID: $displayCode",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                            Text(
                                text = "Review employee exit request, verify project transitions, and authorize the clearance workflow.",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }

                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                        StatusBadge(
                            text = effectiveStatus.replace("_", " "),
                            bgColor = statusColors.first,
                            textColor = statusColors.second
                        )
                    }

                    // Employee identity card using DataCard
                    DataCard(
                        item = request,
                        image = DataCardImage(
                            url = request.organizationMemberId.extractProfilePicture(),
                            vector = Icons.Default.Person,
                            shape = CircleShape,
                            backgroundColor = primary_light,
                            tint = Primary
                        ),
                        title = memberName,
                        subtitle = if (departmentName != "-") "$designationName • $departmentName" else designationName,
                        showHeaderDivider = true,
                        footerAsColumns = true,
                        footerFields = listOf(
                            DataCardField(
                                label = "Join Date",
                                text = joinDate,
                                asColumn = true
                            ),
                            DataCardField(
                                label = "Notice Period",
                                text = "${request.noticePeriodDays} Days",
                                asColumn = true
                            ),
                            DataCardField(
                                label = "Branch",
                                text = branchName,
                                asColumn = true
                            )
                        )
                    )

                    // Current Projects Transition / Clearance Progress card
                    val totalClearance = request.clearanceItems.size.coerceAtLeast(1)
                    val clearedCount = request.clearanceItems.count { it.status == "Cleared" }
                    val clearancePercent = (clearedCount * 100) / totalClearance

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Text(
                                text = "Current Projects Transition",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            if (request.pendingTasks.isNotEmpty()) {
                                request.pendingTasks.forEach { task ->
                                    TransitionProgressItem(
                                        label = task.title,
                                        percent = task.progress
                                    )
                                }
                            } else {
                                TransitionProgressItem(
                                    label = "Clearance Handover ($clearedCount of $totalClearance cleared)",
                                    percent = clearancePercent
                                )
                                TransitionProgressItem(
                                    label = "Notice Period Compliance (${request.noticePeriodDays} Days)",
                                    percent = if (request.noticePeriodWaived) 100 else 60
                                )
                            }
                        }
                    }

                    // Pending Tasks / Clearance Checklist card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Pending Tasks",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            val checklist = request.clearanceItems
                            when {
                                checklist.isNotEmpty() -> {
                                    checklist.forEach { clearance ->
                                        val isCleared = clearance.status.equals("Cleared", ignoreCase = true) ||
                                                clearance.status.equals("Completed", ignoreCase = true)
                                        ChecklistRow(
                                            label = "${clearance.title} (${clearance.department})",
                                            isChecked = isCleared
                                        )
                                    }
                                }

                                request.pendingTasks.isNotEmpty() -> {
                                    request.pendingTasks.take(4).forEach { task ->
                                        val isDone = task.status.equals("Completed", ignoreCase = true)
                                        ChecklistRow(
                                            label = task.title,
                                            isChecked = isDone
                                        )
                                    }
                                }

                                else -> {
                                    defaultPendingTasks.forEach { taskTitle ->
                                        ChecklistRow(
                                            label = taskTitle,
                                            isChecked = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reviewer Verdict & Comments card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Text(
                                text = "Reviewer Verdict",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            if (isPending) {
                                Column {
                                    FormLabel(text = "Manager Comments")
                                    FormTextArea(
                                        value = remarks,
                                        onValueChange = { remarks = it },
                                        placeholder = "Enter notes regarding project handoff or reconsideration reasons...",
                                        minLines = 3,
                                        maxLines = 5,
                                        borderColor = sectionBorder
                                    )
                                }

                                // Accept Exit button
                                Button(
                                    onClick = {
                                        actionLoading = "Approved"
                                        viewModel.submitExitApprovalDecision(
                                            exitRequestId = request.id,
                                            approvalId = activeApproval?.id,
                                            approverId = activeApproval?.approverId?.extractId(),
                                            decisionStatus = "Approved",
                                            comments = remarks.trim(),
                                            onSuccess = { msg ->
                                                actionLoading = null
                                                DynamicIslandManager.showSuccess(msg)
                                                viewModel.fetchExitRequestById(request.id)
                                            },
                                            onError = { err ->
                                                actionLoading = null
                                                DynamicIslandManager.showError(err)
                                            }
                                        )
                                    },
                                    enabled = !isSubmitting && actionLoading == null,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.buttonHeight)
                                ) {
                                    if (actionLoading == "Approved") {
                                        CircularProgressIndicator(
                                            color = whiteBg,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    } else {
                                        Text(
                                            text = "Accept Exit",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = whiteBg
                                        )
                                    }
                                }

                                // Request Reconsideration button (requires manager comments)
                                OutlinedButton(
                                    onClick = {
                                        if (remarks.isBlank()) {
                                            DynamicIslandManager.showError(
                                                "Please enter comments explaining the reconsideration request"
                                            )
                                            return@OutlinedButton
                                        }
                                        actionLoading = "Rejected"
                                        viewModel.submitExitApprovalDecision(
                                            exitRequestId = request.id,
                                            approvalId = activeApproval?.id,
                                            approverId = activeApproval?.approverId?.extractId(),
                                            decisionStatus = "Rejected",
                                            comments = remarks.trim(),
                                            onSuccess = { msg ->
                                                actionLoading = null
                                                DynamicIslandManager.showSuccess(msg)
                                                viewModel.fetchExitRequestById(request.id)
                                            },
                                            onError = { err ->
                                                actionLoading = null
                                                DynamicIslandManager.showError(err)
                                            }
                                        )
                                    },
                                    enabled = !isSubmitting && actionLoading == null,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.buttonHeight)
                                ) {
                                    if (actionLoading == "Rejected") {
                                        CircularProgressIndicator(
                                            color = Primary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    } else {
                                        Text(
                                            text = "Request Reconsideration",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                        .background(Primary_background)
                                        .padding(tokens.extraPadding),
                                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                                ) {
                                    Text(
                                        text = "Decision Recorded",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = headerGrey
                                    )
                                    Text(
                                        text = activeApproval?.status ?: request.status.replace("_", " "),
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                    val recordedNote = activeApproval?.comments ?: request.remarks
                                    if (!recordedNote.isNullOrBlank()) {
                                        Text(
                                            text = "\"$recordedNote\"",
                                            fontSize = tokens.caption,
                                            fontStyle = FontStyle.Italic,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Exit Timeline Progress card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Text(
                                text = "Exit Timeline",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )

                            val timelineSteps = buildTimelineSteps(request)
                            timelineSteps.forEach { (label, completed) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.extraPadding)
                                            .clip(CircleShape)
                                            .background(if (completed) Primary else headerGrey)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = tokens.bodySmall,
                                        color = if (completed) TextPrimary else headerGrey
                                    )
                                }
                            }
                        }
                    }

                    // Retention Insight card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        color = Primary
                    ) {
                        Column(
                            modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                        ) {
                            Text(
                                text = "RETENTION INSIGHT",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = primary_light
                            )

                            val deptLabel = departmentName.takeIf { it != "-" } ?: "team"
                            Text(
                                text = "$memberName's exit increases $deptLabel workload by approximately 14% across core initiatives.",
                                fontSize = tokens.bodySmall,
                                color = whiteBg
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        DynamicIslandManager.showSuccess(
                                            "Detailed retention breakdown available in Analytics"
                                        )
                                    }
                                    .padding(top = tokens.extraPadding * 0.2f)
                            ) {
                                Text(
                                    text = "View Impact Report",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = whiteBg
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.3f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    label: String,
    isChecked: Boolean
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
    ) {
        // Reuse AppCheckbox in read-only checklist mode
        AppCheckbox(
            checked = isChecked,
            onCheckedChange = null,
            size = tokens.iconSize,
            enabled = false
        )

        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = if (isChecked) TextPrimary else TextSecondary,
            textDecoration = if (isChecked) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@Composable
private fun TransitionProgressItem(
    label: String,
    percent: Int
) {
    val tokens = LocalAppTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                color = TextPrimary
            )
            Text(
                text = "$percent%",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
        DataCardProgressBar(
            progress = (percent.coerceIn(0, 100)) / 100f,
            progressColor = Primary,
            trackColor = light_grey
        )
    }
}

private fun buildTimelineSteps(request: ExitRequestDto): List<Pair<String, Boolean>> {
    val status = request.status
    val submittedDate = formatExitDate(request.createdAt)
    val managerApprovalStatus = request.approvals.find { it.stage == "Manager_Review" }?.status
    val managerDone = managerApprovalStatus in listOf("Approved", "Rejected") || status in listOf(
        "Approved", "Rejected", "Clearance_Pending", "Clearance_Completed", "Settlement_Pending", "Settled", "Completed"
    )
    val hrDone = status in listOf(
        "Clearance_Completed", "Settlement_Pending", "Settled", "Completed"
    )
    val settlementDone = status in listOf("Settled", "Completed")

    return listOf(
        "Request Submitted ($submittedDate)" to true,
        (if (managerDone) "Manager Review (Completed)" else "Manager Review (In Progress)") to true,
        (if (hrDone) "HR Review (Completed)" else "HR Review (Pending)") to hrDone,
        (if (settlementDone) "Final Settlement (Completed)" else "Final Settlement (Pending)") to settlementDone
    )
}