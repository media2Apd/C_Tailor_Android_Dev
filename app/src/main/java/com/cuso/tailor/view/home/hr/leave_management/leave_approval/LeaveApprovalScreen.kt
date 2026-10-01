package com.cuso.tailor.view.home.hr.leave_management.leave_approval

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.LeaveMemberDto
import com.cuso.tailor.model.hr.LeaveRequestItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatLeaveDuration(startIso: String?, endIso: String?): String {
    if (startIso.isNullOrBlank()) return "—"
    return try {
        val startInstant = Instant.parse(startIso).atZone(ZoneId.systemDefault())
        val startFmt = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH).format(startInstant)
        if (endIso.isNullOrBlank()) return startFmt
        val endInstant = Instant.parse(endIso).atZone(ZoneId.systemDefault())
        val endFmt = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH).format(endInstant)
        "$startFmt - $endFmt"
    } catch (_: Exception) {
        val s = startIso.take(10)
        val e = endIso?.take(10).orEmpty()
        if (e.isNotBlank()) "$s - $e" else s
    }
}

// Resolves display name of the employee safely
fun resolveEmployeeName(member: LeaveMemberDto?): String {
    return when {
        member != null && (!member.firstName.isNullOrBlank() || !member.lastName.isNullOrBlank()) ->
            "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim()
        !member?.memberId.isNullOrBlank() -> member.memberId
        else -> "Employee"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LeaveApprovalScreen(
    onClose: () -> Unit = {},
    viewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchLeaveRequests()
    }
    val leaveRequests by viewModel.leaveRequests.collectAsState()
    val isLoading by viewModel.isLoadingLeaveRequests.collectAsState()
    val errorMessageState by viewModel.leaveRequestsError.collectAsState()

    // ── SEARCH FILTER STATE (Type explicitly declared) ──
    var searchQuery by remember { mutableStateOf("") }

    val filteredLeaveRequests: List<LeaveRequestItemDto> = remember(leaveRequests, searchQuery) {
        if (searchQuery.isBlank()) {
            leaveRequests
        } else {
            leaveRequests.filter { item: LeaveRequestItemDto ->
                val name = resolveEmployeeName(item.organizationMemberId)
                val leaveType = item.leaveTypeId?.name.orEmpty()
                val reason = item.reason.orEmpty()
                name.contains(searchQuery, ignoreCase = true) ||
                        leaveType.contains(searchQuery, ignoreCase = true) ||
                        reason.contains(searchQuery, ignoreCase = true)
            }
        }
    }


    // Dialog state holders
    var itemToApprove by remember { mutableStateOf<LeaveRequestItemDto?>(null) }
    var itemToReject by remember { mutableStateOf<LeaveRequestItemDto?>(null) }

    var successFeedback by remember { mutableStateOf<String?>(null) }
    var errorFeedback by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TitleBar(title = "Leave Approval", onClose = onClose)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Leave Request...",
                        isSearchBarAlone = true
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                // UI State Handler
                when {
                    isLoading && leaveRequests.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessageState != null && leaveRequests.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load leave requests",
                            message = errorMessageState ?: "Something went wrong. Please check your connection and try again.",
                            onRetry = { viewModel.fetchLeaveRequests() }
                        )
                    }

                    leaveRequests.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No pending leave requests found.",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                        ) {
                            items(
                                items = filteredLeaveRequests,
                                key = { it.id }
                            ) { item ->
                                val member = item.organizationMemberId
                                val employeeName = resolveEmployeeName(member)
                                val designation = member?.designation?.ifBlank { "Employee" } ?: "Employee"
                                val leaveTypeName = item.leaveTypeId?.name ?: "Leave"
                                val durationText = formatLeaveDuration(item.startDate, item.endDate)
                                val daysCount = item.totalDays?.toInt() ?: 1
                                val totalDaysText = if (daysCount <= 1) "$daysCount Day" else "$daysCount Days"

                                val (statusBg, statusText) = when (item.status.lowercase()) {
                                    "approved" -> greenBg to greentext
                                    "rejected" -> redBg to redText
                                    else -> yellowBg to yellowText
                                }

                                DataCard(
                                    item = item,
                                    title = employeeName,
                                    titleColor = title_color,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = designation,
                                    topBadgeText = leaveTypeName,
                                    topBadgeInline = true,
                                    topBadgeShowDot = false,
                                    topBadgeBgColor = primary_light,
                                    topBadgeTextColor = BluePrimary,
                                    content = {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "Duration",
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Normal,
                                                        color = headerGrey
                                                    )
                                                    Text(
                                                        text = durationText,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.Start,
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Total Days",
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Normal,
                                                            color = headerGrey
                                                        )
                                                        Text(
                                                            text = totalDaysText,
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                    }

                                                    StatusBadge(
                                                        text = item.status.replaceFirstChar { it.uppercase() },
                                                        bgColor = statusBg,
                                                        textColor = statusText,
                                                        showDot = false
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                                            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                                            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                                            // Action Buttons: Open dialogs on click
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { itemToReject = item },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(tokens.buttonHeight * 0.95f),
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                                    border = BorderStroke(1.dp, redText),
                                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                                                ) {
                                                    Text(
                                                        text = "Reject",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = redText
                                                    )
                                                }

                                                Button(
                                                    onClick = { itemToApprove = item },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(tokens.buttonHeight * 0.95f),
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                                ) {
                                                    Text(
                                                        text = "Approve",
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = whiteBg
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Approve Confirmation Dialog
        itemToApprove?.let { item ->
            ApproveLeaveDialog(
                employeeName = resolveEmployeeName(item.organizationMemberId),
                onDismiss = { itemToApprove = null },
                onConfirm = {
                    val targetId = item.id
                    itemToApprove = null
                    viewModel.updateLeaveStatus(
                        id = targetId,
                        status = "approved",
                        onSuccess = { msg -> successFeedback = msg },
                        onError = { err -> errorFeedback = err }
                    )
                }
            )
        }

        // Reject Confirmation Dialog with Note
        itemToReject?.let { item ->
            RejectLeaveDialog(
                employeeName = resolveEmployeeName(item.organizationMemberId),
                durationText = formatLeaveDuration(item.startDate, item.endDate),
                onDismiss = { itemToReject = null },
                onConfirm = { note ->
                    val targetId = item.id
                    itemToReject = null
                    viewModel.updateLeaveStatus(
                        id = targetId,
                        status = "rejected",
                        note = note,
                        onSuccess = { msg -> successFeedback = msg },
                        onError = { err -> errorFeedback = err }
                    )
                }
            )
        }

        // Dynamic Island Notifications
        DynamicIslandSuccess(
            message = successFeedback,
            onDismiss = { successFeedback = null }
        )

        DynamicIslandError(
            message = errorFeedback,
            onDismiss = { errorFeedback = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Confirmation Dialog: Approve Leave Request
// ─────────────────────────────────────────────────────────────
@Composable
private fun ApproveLeaveDialog(
    employeeName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(tokens.cardPadding)
            ) {
                Text(
                    text = "Approve leave request?",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                Text(
                    text = "Are you sure you want to approve the leave request from $employeeName?",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = textSubdued
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 0.6f))

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = "Approve",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Confirmation Dialog: Reject Leave Request with Optional Note
// ─────────────────────────────────────────────────────────────
@Composable
private fun RejectLeaveDialog(
    employeeName: String,
    durationText: String,
    onDismiss: () -> Unit,
    onConfirm: (note: String?) -> Unit
) {
    val tokens = LocalAppTokens.current
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(tokens.cardPadding)
            ) {
                Text(
                    text = "Reject leave request",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "$employeeName · $durationText",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )

                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "Note to employee (optional)",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = {
                        Text(
                            text = "Let them know why this request was rejected...",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = mutedText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sectionBorder,
                        unfocusedBorderColor = sectionBorder,
                        focusedContainerColor = whiteBg,
                        unfocusedContainerColor = whiteBg
                    ),
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 0.6f))

                    Button(
                        onClick = { onConfirm(noteText.trim().ifBlank { null }) },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = redText)
                    ) {
                        Text(
                            text = "Reject request",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    }
}