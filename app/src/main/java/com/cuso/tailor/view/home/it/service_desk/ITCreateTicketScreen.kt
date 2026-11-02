package com.cuso.tailor.view.home.it.service_desk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.CreateTicketRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItCreateTicketScreen(
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val members by viewModel.orgMembers.collectAsState()
    var selectedMemberText by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var requesterName by remember { mutableStateOf("") }
    var requesterDept by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hardware") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("High") }
    var priorityExpanded by remember { mutableStateOf(false) }

    var relatedDeviceCode by remember { mutableStateOf("") }
    var relatedAssetCode by remember { mutableStateOf("") }
    var applicationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUsersForAssignment()
    }

    LaunchedEffect(members) {
        if (members.isNotEmpty() && selectedMemberText.isEmpty()) {
            val first = members.first()
            selectedMemberText = first.dropdownLabel
            requesterName = first.fullName
            requesterDept = first.departmentId?.name.orEmpty()
            branchName = first.branchId?.name.orEmpty()
            selectedUserId = first.userId?.id ?: first.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Create Ticket",
            subtitle = "IT Support Request",
            onClose = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    FormDropdown(
                        label = "Requester",
                        value = selectedMemberText.ifEmpty { "Select Employee" },
                        expanded = memberDropdownExpanded,
                        onExpandChange = { memberDropdownExpanded = it },
                        options = members.map { it.dropdownLabel },
                        isRequired = true,
                        onOptionSelected = { chosen ->
                            selectedMemberText = chosen
                            val matched = members.find { it.dropdownLabel == chosen }
                            matched?.let {
                                requesterName = it.fullName
                                requesterDept = it.departmentId?.name.orEmpty()
                                branchName = it.branchId?.name.orEmpty()
                                selectedUserId = it.userId?.id ?: it.id
                            }
                        }
                    )

                    Column {
                        FormLabel(text = "Subject", isRequired = true)
                        FormTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            placeholder = "e.g., POS printer not connecting"
                        )
                    }

                    FormDropdown(
                        label = "Category",
                        value = category,
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = listOf("POS", "Hardware", "Network", "Access", "Software"),
                        isRequired = true,
                        onOptionSelected = { category = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Priority",
                                value = priority,
                                expanded = priorityExpanded,
                                onExpandChange = { priorityExpanded = it },
                                options = listOf("Critical", "High", "Medium", "Low"),
                                isRequired = true,
                                onOptionSelected = { priority = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Branch")
                            FormTextField(
                                value = branchName,
                                onValueChange = { branchName = it },
                                placeholder = "e.g., Chennai Branch"
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    Text(
                        text = "Related References (Optional)",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Column {
                        FormLabel(text = "Related Device Code")
                        FormTextField(
                            value = relatedDeviceCode,
                            onValueChange = { relatedDeviceCode = it },
                            placeholder = "e.g., DEV-001 or POS-04"
                        )
                    }

                    Column {
                        FormLabel(text = "Related Asset Code")
                        FormTextField(
                            value = relatedAssetCode,
                            onValueChange = { relatedAssetCode = it },
                            placeholder = "e.g., AST-1048"
                        )
                    }

                    Column {
                        FormLabel(text = "Application Name")
                        FormTextField(
                            value = applicationName,
                            onValueChange = { applicationName = it },
                            placeholder = "e.g., POS Billing Software"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)) {
                    FormLabel(text = "Description", isRequired = true)
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe the issue or request in detail..."
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .border(1.dp, sectionBorder)
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
            ) {
                Button(
                    onClick = onNavigateBack,
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = whiteBg),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                }

                Button(
                    onClick = {
                        if (subject.isBlank() || description.isBlank()) {
                            DynamicIslandManager.showError("Please fill in Subject and Description.")
                            return@Button
                        }
                        isSubmitting = true
                        val req = CreateTicketRequest(
                            requesterName = requesterName.ifBlank { "User" },
                            requesterDepartment = requesterDept.ifBlank { null },
                            userId = selectedUserId,
                            subject = subject.trim(),
                            category = category,
                            priority = priority,
                            branchName = branchName.ifBlank { null },
                            relatedDeviceCode = relatedDeviceCode.ifBlank { null },
                            relatedAssetCode = relatedAssetCode.ifBlank { null },
                            applicationName = applicationName.ifBlank { null },
                            description = description.trim()
                        )
                        viewModel.createTicket(
                            request = req,
                            onSuccess = { msg ->
                                isSubmitting = false
                                DynamicIslandManager.showSuccess(msg)
                                onNavigateBack()
                            },
                            onError = { err ->
                                isSubmitting = false
                                DynamicIslandManager.showError(err)
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(tokens.buttonHeight)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = whiteBg, strokeWidth = 2.dp, modifier = Modifier.size(tokens.iconSize))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Text("Create Ticket", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}