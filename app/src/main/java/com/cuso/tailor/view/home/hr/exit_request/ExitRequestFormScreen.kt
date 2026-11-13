package com.cuso.tailor.view.home.hr.exit_request

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.io.File
import java.io.FileOutputStream

private val exitTypeChoices = listOf(
    "Resignation" to "Resignation",
    "Retirement" to "Retirement",
    "Termination" to "Termination",
    "Contract End" to "Contract_End",
    "Layoff" to "Layoff",
    "Absconding" to "Absconding",
    "Death" to "Death",
    "Other" to "Other"
)

@Composable
fun ExitRequestFormScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val orgMembers by viewModel.orgMembers.collectAsState()
    val members by viewModel.members.collectAsState()
    val memberDetail by viewModel.memberDetail.collectAsState()
    val isSubmitting by viewModel.isSubmittingExitRequest.collectAsState()

    // Fetch employee lists when entering the screen
    LaunchedEffect(Unit) {
        viewModel.fetchOrgStructure()
        viewModel.fetchMembers(page = 1, limit = 100)
    }

    var selectedMemberId by remember { mutableStateOf("") }
    var selectedMemberName by remember { mutableStateOf("") }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    var branchId by remember { mutableStateOf("") }
    var departmentId by remember { mutableStateOf("") }
    var departmentName by remember { mutableStateOf("") }
    var designationId by remember { mutableStateOf("") }
    var designationName by remember { mutableStateOf("") }

    var selectedExitTypeLabel by remember { mutableStateOf("Resignation") }
    var selectedExitTypeValue by remember { mutableStateOf("Resignation") }
    var exitTypeExpanded by remember { mutableStateOf(false) }

    var requestedLastWorkingDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var noticePeriodDaysText by remember { mutableStateOf("90") }
    var attachedFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    var memberError by remember { mutableStateOf<String?>(null) }
    var exitTypeError by remember { mutableStateOf<String?>(null) }
    var reasonError by remember { mutableStateOf<String?>(null) }

    // Build unified dropdown options from orgMembers or fallback to members list
    val memberOptions = remember(orgMembers, members) {
        if (orgMembers.isNotEmpty()) {
            orgMembers.map { m ->
                val display = if (m.fullName.isNotBlank()) {
                    m.fullName
                } else {
                    listOfNotNull(m.firstName, m.lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Unnamed Employee" }
                }
                display to m.id
            }
        } else {
            members.map { m ->
                val display = listOfNotNull(m.firstName, m.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Unnamed Employee" }
                display to m._id
            }
        }
    }

    // Fetch full member detail when an employee is selected
    LaunchedEffect(selectedMemberId) {
        if (selectedMemberId.isNotBlank()) {
            viewModel.fetchMemberDetail(selectedMemberId)
        }
    }

    // Extract department and designation if available on memberDetail
    LaunchedEffect(memberDetail, selectedMemberId) {
        val detail = memberDetail
        if (detail != null && detail._id == selectedMemberId) {
            branchId = detail.branchId?._id.orEmpty()
            departmentId = detail.departmentId?._id.orEmpty()
            departmentName = detail.departmentId?.name.orEmpty()
            designationId = detail.designationId?._id.orEmpty()
            designationName = detail.designationId?.name.orEmpty()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val resolved = uris.mapNotNull { uri -> copyUriToTempFile(context, uri) }
            attachedFiles = attachedFiles + resolved
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Exit Form",
            onClose = onClose
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // Form heading
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.3f)) {
                Text(
                    text = "Employee Exit Request",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "Initiate employee departure & notice period process.",
                    fontSize = tokens.bodySmall,
                    color = headerGrey
                )
            }

            // Employee Name dropdown
            FormDropdown(
                label = "Employee Name",
                value = selectedMemberName,
                expanded = memberDropdownExpanded,
                onExpandChange = { memberDropdownExpanded = it },
                options = memberOptions.map { it.first },
                onOptionSelected = { chosenName ->
                    val matched = memberOptions.find { it.first == chosenName }
                    if (matched != null) {
                        selectedMemberName = matched.first
                        selectedMemberId = matched.second
                        memberError = null
                    }
                },
                isRequired = true,
                isError = memberError != null,
                errorMessage = memberError
            )

            // Department field
            Column {
                FormLabel(text = "Department")
                FormTextField(
                    value = departmentName,
                    onValueChange = { departmentName = it },
                    placeholder = "Department",
                    enabled = true
                )
            }

            // Designation field
            Column {
                FormLabel(text = "Designation")
                FormTextField(
                    value = designationName,
                    onValueChange = { designationName = it },
                    placeholder = "Designation",
                    enabled = true
                )
            }

            // Exit Type dropdown
            FormDropdown(
                label = "Exit Type",
                value = selectedExitTypeLabel,
                expanded = exitTypeExpanded,
                onExpandChange = { exitTypeExpanded = it },
                options = exitTypeChoices.map { it.first },
                onOptionSelected = { chosenLabel ->
                    val matched = exitTypeChoices.find { it.first == chosenLabel }
                    if (matched != null) {
                        selectedExitTypeLabel = matched.first
                        selectedExitTypeValue = matched.second
                        exitTypeError = null
                    }
                },
                isRequired = true,
                isError = exitTypeError != null,
                errorMessage = exitTypeError
            )

            // Requested Last Working Day
            Column {
                FormLabel(text = "Requested Last Working Day")
                DatePickerField(
                    value = requestedLastWorkingDate,
                    onDateSelected = { requestedLastWorkingDate = it }
                )
            }

            // Reason for Exit
            Column {
                FormLabel(text = "Reason for Exit", isRequired = true)
                FormTextArea(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (it.isNotBlank()) reasonError = null
                    },
                    placeholder = "Briefly describe the reason for departure...",
                    minLines = 3,
                    maxLines = 5,
                    borderColor = if (reasonError != null) redText else sectionBorder
                )
                if (reasonError != null) {
                    Text(
                        text = reasonError!!,
                        fontSize = tokens.label,
                        color = redText,
                        modifier = Modifier.padding(top = tokens.extraPadding * 0.3f)
                    )
                }
            }

            // Required Notice Period card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REQUIRED NOTICE PERIOD",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Box(modifier = Modifier.width(tokens.fieldHeight * 2.4f)) {
                            FormTextField(
                                value = noticePeriodDaysText,
                                onValueChange = { input ->
                                    noticePeriodDaysText = input.filter { it.isDigit() }
                                },
                                keyboardType = KeyboardType.Number,
                                trailingText = "Days",
                                borderColor = sectionBorder,
                                textColor = Primary
                            )
                        }
                        Text(
                            text = "${noticePeriodDaysText.ifBlank { "0" }} Days",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    Text(
                        text = "Standard notice period applies based on policy.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
            }

            // Supporting Documents card
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
                        text = "Supporting Documents",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    attachedFiles.forEachIndexed { index, file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(Primary_background)
                                .border(
                                    1.dp,
                                    grey_border,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(
                                    horizontal = tokens.extraPadding,
                                    vertical = tokens.extraPadding * 0.8f
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Text(
                                    text = file.name,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Remove file",
                                tint = redText,
                                modifier = Modifier
                                    .size(tokens.iconSize)
                                    .clickable {
                                        attachedFiles = attachedFiles.toMutableList().also {
                                            it.removeAt(index)
                                        }
                                    }
                            )
                        }
                    }

                    // Upload trigger box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight * 1.15f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .border(
                                1.dp,
                                sectionBorder,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                            )
                            .clickable {
                                filePickerLauncher.launch(
                                    arrayOf("application/pdf", "image/*", "application/msword")
                                )
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                        Text(
                            text = "Upload New PDF",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // Departure Policy Guideline card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                color = primary_light,
                border = BorderStroke(1.dp, light_blue_border)
            ) {
                Column(
                    modifier = Modifier.padding(tokens.cardPadding * 0.8f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                ) {
                    Text(
                        text = "Departure Policy Guideline",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = "Ensure all system handovers and equipment return logs are completed at least 7 days before your last working day.",
                        fontSize = tokens.caption,
                        color = Primary
                    )
                }
            }
        }

        // Fixed bottom action bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            border = BorderStroke(1.dp, grey_border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    border = BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        var valid = true
                        if (selectedMemberId.isBlank()) {
                            memberError = "Please select an employee"
                            valid = false
                        }
                        if (selectedExitTypeValue.isBlank()) {
                            exitTypeError = "Exit type is required"
                            valid = false
                        }
                        if (reason.isBlank()) {
                            reasonError = "Reason for exit is required"
                            valid = false
                        }

                        if (!valid) {
                            DynamicIslandManager.showError("Please fill in all required fields")
                            return@Button
                        }

                        viewModel.createExitRequest(
                            organizationMemberId = selectedMemberId,
                            branchId = branchId,
                            departmentId = departmentId,
                            designationId = designationId,
                            exitType = selectedExitTypeValue,
                            reason = reason,
                            requestedLastWorkingDate = requestedLastWorkingDate,
                            noticePeriodDays = noticePeriodDaysText.toIntOrNull() ?: 90,
                            status = "Submitted",
                            files = attachedFiles,
                            onSuccess = { msg ->
                                DynamicIslandManager.showSuccess(msg)
                                onSubmitSuccess()
                            },
                            onError = { err ->
                                DynamicIslandManager.showError(err)
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = whiteBg,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    } else {
                        Text(
                            text = "Submit Exit",
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

private fun copyUriToTempFile(context: Context, uri: Uri): File? {
    return try {
        val fileName = getFileNameFromUri(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (_: Exception) {
        null
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "document_${System.currentTimeMillis()}"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}