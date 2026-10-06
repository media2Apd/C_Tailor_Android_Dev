@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.hr.document_management.employee_document

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.DocumentFileDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun EmployeeDocumentUploadScreen(
    documentId: String? = null,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isEditMode = !documentId.isNullOrBlank()

    // Form inputs state
    var searchEmployeeText by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var isEmployeeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var titleText by remember { mutableStateOf("") }
    var issueDateText by remember { mutableStateOf("") }
    var expiryDateText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // Cloudinary remote images/documents from View-One API
    var existingCloudinaryFiles by remember { mutableStateOf<List<DocumentFileDto>>(emptyList()) }

    // Newly uploaded local document files state
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    // Collect states from ViewModel
    val categories by viewModel.documentCategories.collectAsState()
    val membersList by viewModel.members.collectAsState()
    val isSubmitting by viewModel.isSubmittingUpload.collectAsState()
    val documentDetail by viewModel.documentDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDocumentDetail.collectAsState()

    // Initial load: fetch dropdown data and fetch document details if in edit mode
    LaunchedEffect(documentId) {
        viewModel.fetchDocumentCategories()
        viewModel.fetchMembers(limit = 50)

        if (isEditMode) {
            viewModel.fetchEmployeeDocumentById(
                id = documentId,
                onError = { errorMessage ->
                    DynamicIslandManager.showError(errorMessage)
                }
            )
        }
    }

    // Clean up detail state when navigating back
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDocumentDetail()
        }
    }

    // Prefill form values once document view-one response is available
    LaunchedEffect(documentDetail) {
        documentDetail?.let { doc ->
            // Employee data
            val member = doc.organizationMemberId
            if (member != null) {
                selectedMemberId = member.id
                searchEmployeeText = member.fullName
                employeeId = member.employeeCode
                department = member.department.orEmpty()
            }

            // Category data
            val category = doc.documentCategoryId
            if (category != null) {
                selectedCategoryId = category.id
                selectedCategoryName = category.categoryName
            }

            // Document details
            titleText = doc.title
            issueDateText = formatIsoToDisplayDate(doc.issueDate)
            expiryDateText = formatIsoToDisplayDate(doc.expiryDate)
            notesText = doc.notes.orEmpty()

            // Cloudinary files from view-one API
            existingCloudinaryFiles = doc.files
        }
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = if (isEditMode) "Edit Document" else "Upload",
                onClose = onClose
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            val fileList = selectedFiles.mapNotNull { uri -> uriToFile(context, uri) }

                            if (isEditMode) {
                                viewModel.updateEmployeeDocument(
                                    documentId = documentId,
                                    memberId = selectedMemberId,
                                    categoryId = selectedCategoryId,
                                    title = titleText,
                                    issueDate = issueDateText,
                                    expiryDate = expiryDateText,
                                    notes = notesText,
                                    files = fileList,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Employee document updated successfully")
                                        onClose()
                                    },
                                    onError = { errorMessage ->
                                        DynamicIslandManager.showError(errorMessage)
                                    }
                                )
                            } else {
                                viewModel.uploadEmployeeDocument(
                                    memberId = selectedMemberId,
                                    categoryId = selectedCategoryId,
                                    title = titleText,
                                    issueDate = issueDateText,
                                    expiryDate = expiryDateText,
                                    notes = notesText,
                                    files = fileList,
                                    onSuccess = {
                                        DynamicIslandManager.showSuccess("Employee document created successfully")
                                        onClose()
                                    },
                                    onError = { errorMessage ->
                                        DynamicIslandManager.showError(errorMessage)
                                    }
                                )
                            }
                        },
                        enabled = !isSubmitting && !isLoadingDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        )
                    ) {
                        if (isSubmitting) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text(
                                text = if (isEditMode) "Finalize Update" else "Finalize Upload",
                                color = whiteBg,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (isEditMode) "Update Document" else "New Document Upload",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    modifier = Modifier.padding(horizontal = tokens.screenPadding)
                )

                Spacer(Modifier.height(12.dp))

                // ── Card 1: Employee Information ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding)) {
                        Text(
                            text = "Employee Information",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )

                        Spacer(Modifier.height(10.dp))

                        FormDropdown(
                            label = "Search Employees",
                            value = searchEmployeeText,
                            expanded = isEmployeeDropdownExpanded,
                            onExpandChange = { isEmployeeDropdownExpanded = it },
                            options = membersList.map { "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim() },
                            onOptionSelected = { selectedName ->
                                val member = membersList.firstOrNull {
                                    "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim() == selectedName
                                }
                                searchEmployeeText = selectedName
                                selectedMemberId = member?._id.orEmpty()
                                employeeId = member?.memberId.orEmpty()
                                department = member?.departmentId?.name.orEmpty()
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    FormLabel(text = "Employee Id")
                                    FormLabel(text = employeeId.ifBlank { "—" })
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    FormLabel(text = "Department")
                                    FormLabel(text = department.ifBlank { "—" })
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Card 2: Document Details ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding)) {
                        Text(
                            text = "Document Details",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )

                        Spacer(Modifier.height(10.dp))

                        FormDropdown(
                            label = "Document Category",
                            value = selectedCategoryName,
                            expanded = isCategoryDropdownExpanded,
                            onExpandChange = { isCategoryDropdownExpanded = it },
                            options = categories.map { it.categoryName },
                            onOptionSelected = { categoryName ->
                                val matched = categories.firstOrNull { it.categoryName == categoryName }
                                selectedCategoryId = matched?.id.orEmpty()
                                selectedCategoryName = categoryName
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        FormLabel(text = "Title")
                        FormTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            placeholder = "e.g. Passport Copy 2024",
                            containerColor = headerBg,
                            borderColor = sectionBorder
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(text = "Issue Date")
                                DatePickerField(
                                    value = issueDateText,
                                    onDateSelected = { issueDateText = it }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(text = "Expiry Date")
                                DatePickerField(
                                    value = expiryDateText,
                                    onDateSelected = { expiryDateText = it }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Card 3: Image / Document Upload Dropzone ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    ImageUploadSection(
                        selectedImages = selectedFiles,
                        onBrowseClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        onRemoveImage = { fileToRemove ->
                            selectedFiles = selectedFiles.filter { it != fileToRemove }
                        },
                        maxFiles = 5,
                        title = if (isEditMode) "Replace / Add Documents" else "Upload Document",
                        subtitle = "Click to upload or drag & drop",
                        supportedFormatsText = "PDF, JPG, or PNG (Max 10MB)",
                        browseText = "Browse files"
                    )
                }

                // ── Card 3.1: Cloudinary Pre-existing Images (Shown below dropzone in Edit Mode) ──
                if (existingCloudinaryFiles.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(modifier = Modifier.padding(tokens.cardPadding)) {
                            Text(
                                text = "Current Cloudinary Documents",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )

                            Spacer(Modifier.height(8.dp))

                            existingCloudinaryFiles.forEach { fileDto ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(headerBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cloudinary Image Preview or Document Icon
                                    if (fileDto.resourceType.equals("image", ignoreCase = true) ||
                                        fileDto.url.endsWith(".png", true) ||
                                        fileDto.url.endsWith(".jpg", true) ||
                                        fileDto.url.endsWith(".jpeg", true)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(fileDto.url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Cloudinary Preview",
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    // Cloudinary File details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileDto.publicId.substringAfterLast("/").ifBlank { "Cloudinary File" },
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = fileDto.resourceType.uppercase(),
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }

                                    // Option to remove the existing file
                                    IconButton(
                                        onClick = {
                                            existingCloudinaryFiles = existingCloudinaryFiles.filter { it != fileDto }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove existing file",
                                            tint = redText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Card 4: Notes ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding)) {
                        FormLabel(text = "Notes")
                        FormTextArea(
                            value = notesText,
                            onValueChange = { notesText = it },
                            placeholder = "Add any specific comments or reference numb...",
                            minLines = 3,
                            maxLines = 5,
                            borderColor = sectionBorder,
                            focusedBorderColor = Primary,
                            textColor = TextPrimary,
                            placeholderColor = mutedText
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Extracts real filename and extension from Uri and creates a temp file with valid extension.
 */
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver

        var fileName = "upload_document"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: "upload_document"
            }
        }

        val mimeType = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        if (!fileName.contains(".")) {
            fileName = "$fileName.$extension"
        }

        val tempFile = File(context.cacheDir, fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (_: Exception) {
        null
    }
}

/**
 * Formats UTC ISO date (e.g. 2026-10-05T00:00:00.000Z) to readable format (dd-MM-yyyy / yyyy-MM-dd)
 */
private fun formatIsoToDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(isoDate)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        if (date != null) outputFormat.format(date) else isoDate
    } catch (_: Exception) {
        isoDate.substringBefore("T")
    }
}