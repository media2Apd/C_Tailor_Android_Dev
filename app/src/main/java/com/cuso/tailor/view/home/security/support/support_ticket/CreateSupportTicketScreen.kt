package com.cuso.tailor.view.home.security.support.support_ticket

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun CreateSupportTicketScreen(
    viewModel: SecurityViewModel,
    hrViewModel: HrViewModel,
    branchViewModel: BranchViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Dropdown states
    val branchUiState by branchViewModel.uiState.collectAsState()
    val branches = (branchUiState as? BranchUiState.Success)?.branches.orEmpty()
    val employees by hrViewModel.members.collectAsState()

    var issueType by remember { mutableStateOf("") }
    var issueTypeExpanded by remember { mutableStateOf(false) }

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var priority by remember { mutableStateOf("Medium") }
    var priorityExpanded by remember { mutableStateOf(false) }

    var affectedArea by remember { mutableStateOf("Web Application") }
    var affectedAreaExpanded by remember { mutableStateOf(false) }

    var affectedUserId by remember { mutableStateOf("") }
    var affectedUserExpanded by remember { mutableStateOf(false) }

    var branchId by remember { mutableStateOf("") }
    var branchExpanded by remember { mutableStateOf(false) }

    var contactPreference by remember { mutableStateOf("Email") }
    var contactEmail by remember { mutableStateOf("") }
    var affectedDevice by remember { mutableStateOf("") }

    val selectedImages = remember { mutableStateListOf<Uri>() }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri ->
            if (selectedImages.size < 5) selectedImages.add(uri)
        }
    }

    LaunchedEffect(Unit) {
        branchViewModel.loadBranches()
        hrViewModel.fetchMembers()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Create Support Ticket",
                onClose = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Card 1: Request Information ---
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Request Information", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                    Text("Tell us what problem you are experiencing.", fontSize = tokens.caption, color = TextSecondary)

                    FormDropdown(
                        label = "ISSUE TYPE",
                        isRequired = true,
                        value = issueType,
                        expanded = issueTypeExpanded,
                        onExpandChange = { issueTypeExpanded = it },
                        options = listOf("Login Issue", "Permission Issue", "Feature Request", "Data Discrepancy", "Other"),
                        onOptionSelected = { issueType = it }
                    )

                    FormLabel("SUBJECT", isRequired = true)
                    FormTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        placeholder = "Briefly describe the issue"
                    )

                    FormLabel("DESCRIPTION", isRequired = true)
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe the issue, what happened, and what you were trying to do."
                    )
                }
            }

            // --- Card 2: Impact & Priority ---
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Impact & Priority", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "PRIORITY",
                                value = priority,
                                expanded = priorityExpanded,
                                onExpandChange = { priorityExpanded = it },
                                options = listOf("Low", "Medium", "High", "Critical"),
                                onOptionSelected = { priority = it }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "AFFECTED AREA",
                                value = affectedArea,
                                expanded = affectedAreaExpanded,
                                onExpandChange = { affectedAreaExpanded = it },
                                options = listOf("Web Application", "Mobile Application", "API & Integrations", "Database"),
                                onOptionSelected = { affectedArea = it }
                            )
                        }
                    }

                    FormDropdown(
                        label = "AFFECTED USER",
                        value = employees.find { it._id == affectedUserId }?.let { "${it.firstName} ${it.lastName}".trim() } ?: "Search and select user",
                        expanded = affectedUserExpanded,
                        onExpandChange = { affectedUserExpanded = it },
                        options = employees.map { "${it.firstName} ${it.lastName}".trim() },
                        onOptionSelected = { name ->
                            affectedUserId = employees.find { "${it.firstName} ${it.lastName}".trim() == name }?._id.orEmpty()
                        }
                    )

                    FormDropdown(
                        label = "BRANCH / LOCATION",
                        value = branches.find { it.id == branchId }?.name ?: "Select branch or location",
                        expanded = branchExpanded,
                        onExpandChange = { branchExpanded = it },
                        options = branches.mapNotNull { it.name },
                        onOptionSelected = { name ->
                            branchId = branches.find { it.name == name }?.id.orEmpty()
                        }
                    )
                }
            }

            // --- Card 3: Contact Preference ---
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Contact Preference", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                    Text("How should we reach you for updates on this ticket?", fontSize = tokens.caption, color = TextSecondary)

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("Email", "Phone", "WhatsApp").forEach { method ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable { contactPreference = method }
                            ) {
                                RadioButton(selected = contactPreference == method, onClick = { contactPreference = method })
                                Text(method, fontSize = tokens.bodySmall)
                            }
                        }
                    }

                    FormLabel("EMAIL ADDRESS")
                    FormTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = "admin@sentinelerp.com"
                    )
                }
            }

            // --- Card 4: Supporting Information & Upload ---
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Supporting Information", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)

                    FormLabel("AFFECTED DEVICE (OPTIONAL)")
                    FormTextField(
                        value = affectedDevice,
                        onValueChange = { affectedDevice = it },
                        placeholder = "e.g. iPhone 13 Pro, Chrome on Windows 11"
                    )

                    ImageUploadSection(
                        selectedImages = selectedImages,
                        onBrowseClick = { filePicker.launch("image/*") },
                        onRemoveImage = { selectedImages.remove(it) },
                        maxFiles = 5,
                        subtitle = "Upload screenshots, error messages, logs or relevant documents."
                    )

                    // Warning Alert
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(redBg)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = redText, modifier = Modifier.size(18.dp))
                        Text(
                            "Security warning: Do not upload passwords, OTPs, API keys, access tokens or other sensitive credentials.",
                            fontSize = tokens.caption,
                            color = redText
                        )
                    }
                }
            }

            // Bottom Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) { Text("Cancel", color = TextPrimary) }

                Button(
                    onClick = {
                        val parts = selectedImages.mapNotNull { uriToMultipartPart(context, it, "attachments") }
                        viewModel.createSupportTicket(
                            issueType = issueType,
                            subject = subject,
                            description = description,
                            priority = priority,
                            affectedArea = affectedArea,
                            contactPreference = contactPreference,
                            affectedDevice = affectedDevice,
                            affectedUserId = affectedUserId.ifBlank { null },
                            branchId = branchId.ifBlank { null },
                            contactEmail = contactEmail.ifBlank { null },
                            attachmentParts = parts,
                            onSuccess = { onBack() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Create Ticket", color = whiteBg)
                }
            }
        }
    }
}