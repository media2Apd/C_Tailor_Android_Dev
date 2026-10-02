package com.cuso.tailor.view.home.hr.leave_management.apply_leave

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.CreateLeaveRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.prepareAttachmentPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApplyLeaveScreen(
    onClose: () -> Unit = {},
    viewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchMembers(limit = 100)
        viewModel.fetchLeaveTypes()
    }

    val members by viewModel.members.collectAsState()
    val leaveTypes by viewModel.leaveTypes.collectAsState()
    val isCreating by viewModel.isCreatingLeave.collectAsState()

    var selectedEmployeeName by remember { mutableStateOf("") }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var employeeDropdownOpen by remember { mutableStateOf(false) }

    var selectedLeaveTypeName by remember { mutableStateOf("") }
    var selectedLeaveTypeId by remember { mutableStateOf("") }
    var leaveTypeDropdownOpen by remember { mutableStateOf(false) }

    val dateUiFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) }
    val dateApiFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH) }

    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(5)) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var reasonText by remember { mutableStateOf("") }
    val uploadedDocuments = remember { mutableStateListOf<Uri>() }

    var successFeedback by remember { mutableStateOf<String?>(null) }
    var errorFeedback by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uploadedDocuments.addAll(uris)
    }

    val totalDays = remember(startDate, endDate) {
        if (!endDate.isBefore(startDate)) {
            (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt()
        } else 0
    }

    // Date Picker Dialog for Start Date
    if (showStartDatePicker) {
        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                startDate = LocalDate.of(year, month + 1, dayOfMonth)
                if (endDate.isBefore(startDate)) {
                    endDate = startDate
                }
                showStartDatePicker = false
            },
            startDate.year,
            startDate.monthValue - 1,
            startDate.dayOfMonth
        )
        picker.setOnDismissListener { showStartDatePicker = false }
        DisposableEffect(Unit) {
            picker.show()
            onDispose { picker.dismiss() }
        }
    }

    // Date Picker Dialog for End Date
    if (showEndDatePicker) {
        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = LocalDate.of(year, month + 1, dayOfMonth)
                if (!chosen.isBefore(startDate)) {
                    endDate = chosen
                }
                showEndDatePicker = false
            },
            endDate.year,
            endDate.monthValue - 1,
            endDate.dayOfMonth
        )
        picker.setOnDismissListener { showEndDatePicker = false }
        DisposableEffect(Unit) {
            picker.show()
            onDispose { picker.dismiss() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Apply Leave",
                    onClose = onClose
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding)
            ) {
                // Title and Subtitle
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Apply for Leave",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Complete the details below to submit request",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // Employee Selection
                FormDropdown(
                    label = "Employee Name",
                    value = selectedEmployeeName.ifBlank { "Select Employee" },
                    expanded = employeeDropdownOpen,
                    onExpandChange = { employeeDropdownOpen = it },
                    options = members.map { member ->
                        "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim().ifBlank { member.memberId.orEmpty() }
                    },
                    onOptionSelected = { chosenName ->
                        selectedEmployeeName = chosenName
                        val matched = members.find {
                            "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim().equals(chosenName, ignoreCase = true) ||
                                    it.memberId.equals(chosenName, ignoreCase = true)
                        }
                        selectedEmployeeId = matched?._id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // Leave Type Selection with Policy Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leave Type",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Text(
                        text = "View Policy",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = BluePrimary,
                        modifier = Modifier.clickable { }
                    )
                }
                Spacer(Modifier.height(4.dp))
                FormDropdown(
                    label = "",
                    value = selectedLeaveTypeName.ifBlank { "Select Leave Type" },
                    expanded = leaveTypeDropdownOpen,
                    onExpandChange = { leaveTypeDropdownOpen = it },
                    options = leaveTypes.map { it.name },
                    onOptionSelected = { chosenName ->
                        selectedLeaveTypeName = chosenName
                        selectedLeaveTypeId = leaveTypes.find { it.name.equals(chosenName, ignoreCase = true) }?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // Date Selectors: Start Date & End Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Date",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(whiteBg)
                                .clickable { showStartDatePicker = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = startDate.format(dateUiFormatter),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = headerGrey,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Date",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(whiteBg)
                                .clickable { showEndDatePicker = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = endDate.format(dateUiFormatter),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = headerGrey,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // Total Days Display Card
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total Days",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(activity_purple_bg)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (totalDays <= 1) "$totalDays Day" else "$totalDays Days",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = BluePrimary
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // Reason for Leave Text Area
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Reason for Leave",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(whiteBg)
                            .padding(12.dp)
                    ) {
                        if (reasonText.isBlank()) {
                            Text(
                                text = "Family trip & personal time off.",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = mutedText
                            )
                        }
                        BasicTextField(
                            value = reasonText,
                            onValueChange = { reasonText = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(Primary)
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // Attachment Document Upload Section
                Text(
                    text = "Attachment (Optional)",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Spacer(Modifier.height(4.dp))
                ImageUploadSection(
                    selectedImages = uploadedDocuments,
                    onBrowseClick = { filePickerLauncher.launch("*/*") },
                    onRemoveImage = { uploadedDocuments.remove(it) },
                    subtitle = "Add supporting documents if required by policy"
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                // Form Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
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

                    Button(
                        onClick = {
                            val attachmentParts = uploadedDocuments.mapNotNull { uri ->
                                prepareAttachmentPart(context, uri)
                            }

                            viewModel.createLeaveRequest(
                                organizationMemberId = selectedEmployeeId,
                                leaveTypeId = selectedLeaveTypeId,
                                startDate = startDate.format(dateApiFormatter),
                                endDate = endDate.format(dateApiFormatter),
                                totalDays = totalDays,
                                isHalfDay = false,
                                reason = reasonText.trim(),
                                attachmentParts = attachmentParts,
                                onSuccess = { msg ->
                                    successFeedback = msg
                                    coroutineScope.launch {
                                        delay(1200)
                                        onClose()
                                    }
                                },
                                onError = { err ->
                                    errorFeedback = err
                                }
                            )
                        },
                        enabled = !isCreating,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = if (isCreating) "Submitting..." else "Submit",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // Policy Reminder Footer Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = primary_light
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Policy Reminder",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = BluePrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Requests must be submitted 48 hours in advance.",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 2f))
            }
        }

        // Dynamic Island Notifications
        DynamicIslandSuccess(
            message = successFeedback,
            onDismiss = { successFeedback = null }
        )
    }
}