@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.hr.document_management.employee_document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.EmployeeDocumentDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun EmployeeDocumentListScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onNavigateToUpload: () -> Unit = {},
    onEditDocument: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val documents by viewModel.employeeDocuments.collectAsState()
    val isLoading by viewModel.isLoadingDocuments.collectAsState()

    // State for tracking which document is selected for deletion
    var documentToDelete by remember { mutableStateOf<EmployeeDocumentDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchEmployeeDocuments()
    }

    // Floating action button configuration for document upload
    val uploadFabConfig = remember {
        FabConfig(
            label = "Upload",
            icon = Icons.Default.Add,
            onClick = onNavigateToUpload
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = uploadFabConfig,
        fabVisible = !isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            // 1. Title Bar
            TitleBar(
                title = "Employee Document Upload",
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Search & Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.fetchEmployeeDocuments(query)
                },
                placeholder = "Search Programs, employee...",
                onFilterClick = { /* Handle filter sheet */ }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Document List using reusable DataCard
            if (isLoading) {
                ListSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(documents) { doc ->
                        EmployeeDocumentDataCard(
                            document = doc,
                            onViewClick = { /* Handle document view */ },
                            onEditClick = { selectedDoc -> onEditDocument(selectedDoc.id) },
                            onDeleteClick = { selectedDoc ->
                                // Trigger reusable DeleteModel dialog
                                documentToDelete = selectedDoc
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Reusable DeleteModel Dialog ──
    documentToDelete?.let { doc ->
        val documentTitle = doc.title.ifBlank { doc.organizationMemberId?.fullName.orEmpty() }

        DeleteModel(
            title = "Delete Employee Document",
            message = "Are you sure you want to delete '$documentTitle'?\nThis action cannot be undone.",
            onDismiss = {
                documentToDelete = null
            },
            onDelete = {
                val targetId = doc.id
                documentToDelete = null
                viewModel.deleteEmployeeDocument(
                    id = targetId,
                    onSuccess = {
                        DynamicIslandManager.showSuccess("Employee document deleted successfully")
                    },
                    onError = { errorMessage ->
                        DynamicIslandManager.showError(errorMessage)
                    }
                )
            }
        )
    }
}

@Composable
fun EmployeeDocumentDataCard(
    document: EmployeeDocumentDto,
    onViewClick: (EmployeeDocumentDto) -> Unit = {},
    onEditClick: (EmployeeDocumentDto) -> Unit = {},
    onDeleteClick: (EmployeeDocumentDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Extract dynamic values without fallback dummy text
    val employeeCode = document.organizationMemberId?.employeeCode.orEmpty()
    val employeeName = document.organizationMemberId?.fullName.orEmpty()
    val categoryName = document.documentCategoryId?.categoryName.orEmpty()
    val formattedExpiry = remember(document.expiryDate) {
        formatIsoDate(document.expiryDate).orEmpty()
    }
    val statusText = document.status.takeIf { it.isNotBlank() }

    // Map 3-dot dropdown actions
    val cardActions = remember(document) {
        listOf(
            MenuAction(
                label = "View",
                textColor = TextPrimary,
                tint = close_color,
                onClick = { onViewClick(document) }
            ),
            MenuAction(
                label = "Edit",
                textColor = TextPrimary,
                tint = close_color,
                onClick = { onEditClick(document) }
            ),
            MenuAction(
                label = "Delete",
                textColor = redText,
                tint = redText,
                onClick = { onDeleteClick(document) }
            )
        )
    }

    // Map row fields matching the design layout (Label on left, Value on right)
    val fields = remember(employeeName, categoryName, formattedExpiry) {
        listOf(
            DataCardField(
                label = "Employee Name",
                text = employeeName,
                labelColor = close_color,
                textColor = TextLog,
                valueFontWeight = FontWeight.Medium,
                asRow = true
            ),
            DataCardField(
                label = "Category",
                text = categoryName,
                labelColor = close_color,
                textColor = TextLog,
                valueFontWeight = FontWeight.Medium,
                asRow = true
            ),
            DataCardField(
                label = "Expiry Date",
                text = formattedExpiry,
                labelColor = close_color,
                textColor = TextLog,
                valueFontWeight = FontWeight.Medium,
                asRow = true
            )
        )
    }

    // Reusable DataCard integration
    DataCard(
        item = document,
        title = employeeCode,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        topBadgeText = statusText,
        topBadgeBgColor = greenBg,
        topBadgeTextColor = complete_button_bg,
        topBadgeShowDot = false,
        topBadgeInline = true,
        topBadgeCornerRadius = tokens.cardCornerRadius,
        showHeaderDivider = true,
        footerFields = fields,
        footerAsRows = true,
        actions = cardActions,
        onClick = { onViewClick(document) }
    )
}

/**
 * Converts ISO UTC date string (e.g., 2026-10-05T00:00:00.000Z) to readable format (05 Oct 2026).
 */
private fun formatIsoDate(isoDateString: String?): String? {
    if (isoDateString.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(isoDateString) ?: return null
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(parsedDate)
    } catch (_: Exception) {
        null
    }
}