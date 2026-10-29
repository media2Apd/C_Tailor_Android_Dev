@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_employee

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalEmployeeItem
import com.cuso.tailor.model.legal.LegalRef
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// LEGAL STATUS CONFIG (mirrors React STATUS_LABEL / STATUS_ACTIONS)
// ─────────────────────────────────────────────────────────────

// Distance from the top of the screen to where the dropdown card appears.
// Increase or decrease this value to move the dropdown right under the "Legal Action" button.
private val LegalMenuTopOffset: Dp = 220.dp

// Colors used by the confirmation dialog (yellow = normal, red = danger)
private val LegalWarningYellow = yellowText
private val LegalDangerRed = redText

/**
 * One option shown inside the "Legal Action" dropdown.
 * [targetStatus] is the exact enum value sent to the backend, e.g. {"status":"InReview"}.
 */
data class LegalStatusAction(
    val label: String,
    val targetStatus: String,
    val isDanger: Boolean = false
)

/**
 * Converts any raw status text ("In Review", "inreview", "InActive"...)
 * into the exact backend enum value used by the web app.
 */
private fun canonicalLegalStatus(raw: String): String {
    return when (raw.trim().lowercase().replace(" ", "")) {
        "pending", "draft" -> "Pending"
        "inreview", "underreview" -> "InReview"
        "approved" -> "Approved"
        "active" -> "Active"
        "inactive" -> "InActive"
        else -> raw.trim()
    }
}

/**
 * Human readable label for a status (same as React STATUS_LABEL).
 */
fun legalStatusLabel(status: String): String {
    return when (canonicalLegalStatus(status)) {
        "Pending" -> "Pending"
        "InReview" -> "In Review"
        "Approved" -> "Approved"
        "Active" -> "Active"
        "InActive" -> "Inactive"
        else -> status
    }
}

/**
 * Next steps offered under "Legal Action" for the current status (same as React STATUS_ACTIONS).
 * Returns an empty list when no action is available, so the button is hidden.
 */
fun getLegalStatusActions(status: String): List<LegalStatusAction> {
    return when (canonicalLegalStatus(status)) {
        "Pending" -> listOf(
            LegalStatusAction(label = "Send for Review", targetStatus = "InReview")
        )
        "InReview" -> listOf(
            LegalStatusAction(label = "Approve", targetStatus = "Approved"),
            LegalStatusAction(label = "Send back to Pending", targetStatus = "Pending")
        )
        "Approved" -> listOf(
            LegalStatusAction(label = "Mark Active", targetStatus = "Active")
        )
        "Active" -> listOf(
            // Only InActive is treated as a danger action (red dialog), same as React
            LegalStatusAction(label = "Mark Inactive", targetStatus = "InActive", isDanger = true)
        )
        "InActive" -> listOf(
            LegalStatusAction(label = "Reactivate", targetStatus = "Active")
        )
        else -> emptyList()
    }
}

@Composable
fun LegalEmployeeDetailScreen(
    recordId: String,
    viewModel: LegalViewModel,
    hrViewModel: HrViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val record by viewModel.employeeDetail.collectAsState()
    val isLoading by viewModel.isLoadingEmployeeDetail.collectAsState()
    val isUpdatingStatus by viewModel.isUpdatingEmployeeStatus.collectAsState()
    val errorMessage by viewModel.employeeDetailError.collectAsState()
    val members by hrViewModel.members.collectAsState()

    // Controls whether the "Legal Action" dropdown card is visible
    var showActionMenu by remember { mutableStateOf(false) }

    // Holds the action chosen from the dropdown while waiting for confirmation
    var pendingStatusAction by remember { mutableStateOf<LegalStatusAction?>(null) }

    LaunchedEffect(recordId) {
        viewModel.fetchLegalEmployeeDetail(recordId)
        hrViewModel.fetchMembers()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalEmployeeDetail()
        }
    }

    // 5 Standalone Tabs matching screenshots exactly
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            TabItem(label = "Overview"),
            TabItem(label = "Employment & Labour Terms"),
            TabItem(label = "Documents & Compliance"),
            TabItem(label = "Legal Issues"),
            TabItem(label = "History")
        )
    }

    when {
        isLoading -> {
            ListSkeleton()
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(whiteBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Record not found",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = redText
                )
            }
        }
        record != null -> {
            val item = record!!

            // Helper to resolve an ID or LegalRef to a real human name
            fun resolveMemberName(ref: LegalRef?, fallbackCode: String = ""): String {
                if (ref == null) return fallbackCode.ifEmpty { "—" }
                val targetId = ref.id.ifEmpty { ref.displayName }
                val found = members.find { it._id == targetId || it.memberId == fallbackCode }
                return when {
                    found != null -> "${found.firstName} ${found.lastName}".trim()
                    ref.displayName.isNotBlank() && !ref.displayName.matches(Regex("^[a-fA-F0-9]{24}$")) -> ref.displayName
                    fallbackCode.isNotBlank() -> fallbackCode
                    else -> ref.id.take(8).ifEmpty { "—" }
                }
            }

            val employeeName = resolveMemberName(item.employeeId, item.employeeCode)
            val branchName = item.branchId?.name?.takeIf { it.isNotBlank() } ?: item.legalEntity

            // Calls the status API with the exact enum value (payload: {"status": newStatus}).
            // The dialog stays open while saving and is closed by onDone (success or error).
            fun changeStatus(newStatus: String, onDone: () -> Unit) {
                if (isUpdatingStatus) return
                viewModel.updateLegalEmployeeStatus(
                    recordId = item.id,
                    status = newStatus,
                    onSuccess = { msg ->
                        Toast.makeText(
                            context,
                            msg.ifEmpty { "Status changed to ${legalStatusLabel(newStatus)}" },
                            Toast.LENGTH_SHORT
                        ).show()
                        onDone()
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                )
            }

            // Available status actions for the current status (same as React STATUS_ACTIONS)
            val statusActions = remember(item.status) {
                getLegalStatusActions(item.status)
            }

            // "Legal Action" button: shown only when the current status has actions.
            // Clicking it opens the dropdown card (rendered below the template).
            val primaryAction: DetailAction? = if (statusActions.isNotEmpty()) {
                DetailAction(
                    label = "Legal Action",
                    icon = Icons.Default.Add,
                    onClick = { showActionMenu = true }
                )
            } else null

            // Root box so the dropdown and dialog can be drawn on top of the screen
            Box(modifier = Modifier.fillMaxSize()) {

                DetailScreenTemplate(
                    title = "Employee & Labour Legal",
                    onClose = onNavigateBack,
                    header = DetailHeaderData(
                        title = employeeName,
                        badges = emptyList(),
                        icon = Icons.Default.Description,
                        metaItems = listOf(
                            item.legalRecordNumber,
                            item.labourDocumentType.ifEmpty { "Legal Counsel" },
                            branchName
                        ),
                        primaryAction = primaryAction,
                        secondaryActions = listOf(
                            DetailAction(
                                label = "Edit",
                                icon = Icons.Default.Edit,
                                onClick = { onNavigateToEdit(item.id) }
                            ),
                            DetailAction(
                                label = "Download",
                                icon = Icons.Default.FileDownload,
                                onClick = {
                                    Toast.makeText(context, "Exporting record...", Toast.LENGTH_SHORT).show()
                                }
                            ),
                            DetailAction(
                                label = "Share",
                                icon = Icons.Default.Share,
                                onClick = {
                                    clipboardManager.setText(
                                        AnnotatedString("Employee Legal: $employeeName (${item.legalRecordNumber})")
                                    )
                                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                    ),
                    summaryStats = listOf(
                        DetailStat(
                            label = "STATUS",
                            // Show the readable label (e.g. "In Review") like the web app
                            value = legalStatusLabel(item.status),
                            valueColor = when {
                                item.status.equals("Active", true) -> greentext
                                item.status.equals("Inactive", true) -> redText
                                else -> orangeText
                            },
                            showDot = true
                        ),
                        DetailStat(label = "RISK RATING", value = item.riskLevel),
                        DetailStat(
                            label = "CONTRACT EXPIRY",
                            value = formatDateOnly(item.endDate).ifEmpty { "12 Mar 2027" }
                        ),
                        DetailStat(label = "LEGAL ISSUES", value = "0 Pending")
                    ),
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it }
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> OverviewTabContent(item, employeeName)
                        1 -> TermsTabContent(item, employeeName)
                        2 -> DocumentsTabContent(item)
                        3 -> LegalIssuesTabContent()
                        4 -> HistoryTabContent(item)
                    }
                }

                // Step 1: floating dropdown card with the available status actions
                if (showActionMenu && statusActions.isNotEmpty()) {
                    LegalActionMenu(
                        actions = statusActions,
                        onDismiss = { showActionMenu = false },
                        onSelect = { action ->
                            showActionMenu = false
                            pendingStatusAction = action // opens the confirmation dialog
                        }
                    )
                }

                // Step 2: confirmation dialog shown after an action is chosen
                pendingStatusAction?.let { action ->
                    LegalStatusConfirmDialog(
                        action = action,
                        currentStatusLabel = legalStatusLabel(item.status),
                        isSaving = isUpdatingStatus,
                        onConfirm = {
                            changeStatus(action.targetStatus) { pendingStatusAction = null }
                        },
                        onDismiss = { pendingStatusAction = null }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LEGAL ACTION DROPDOWN (white floating card, like the web menu)
// ─────────────────────────────────────────────────────────────
@Composable
private fun LegalActionMenu(
    actions: List<LegalStatusAction>,
    onDismiss: () -> Unit,
    onSelect: (LegalStatusAction) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Transparent full-screen layer: tapping outside the card closes the menu
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = LegalMenuTopOffset, end = 16.dp)
                .width(208.dp),
            shape = RoundedCornerShape(8.dp),
            color = whiteBg,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        // Same text color as the web menu (gray-700)
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(action) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATUS CONFIRMATION DIALOG (same layout as React ConfirmModal)
// ─────────────────────────────────────────────────────────────
@Composable
private fun LegalStatusConfirmDialog(
    action: LegalStatusAction,
    currentStatusLabel: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Yellow for normal actions, red for the danger action (Mark Inactive)
    val accentColor = if (action.isDanger) LegalDangerRed else LegalWarningYellow

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = whiteBg
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header row: warning icon + title on the left, close (X) on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = action.label,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(enabled = !isSaving) { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message: "Change the status from <current> to <target>?" with highlighted values
                Text(
                    text = buildAnnotatedString {
                        append("Change the status from ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = TextPrimary)) {
                            append(currentStatusLabel)
                        }
                        append(" to ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = TextPrimary)) {
                            append(legalStatusLabel(action.targetStatus))
                        }
                        append("?")
                    },
                    fontSize = tokens.bodySmall,
                    color = Color(0xFF4B5563)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons: Cancel (outlined) and confirm (yellow / red), aligned to the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = accentColor.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else action.label,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 0: OVERVIEW (Employee Legal Profile & Compliance Summary)
// ─────────────────────────────────────────────────────────────
@Composable
private fun OverviewTabContent(record: LegalEmployeeItem, employeeName: String) {
    val tokens = LocalAppTokens.current

    val startDateSafe = formatDateOnly(record.startDate).ifEmpty { "12 May 2021" }
    val probationEndSafe = formatDateOnly(record.renewalDate).ifEmpty { "12 Nov 2021" }
    val contractEndSafe = formatDateOnly(record.endDate).ifEmpty { "N/A (Retirement 2051)" }
    val locationSafe = record.branchId?.name?.takeIf { it.isNotBlank() } ?: "India / Mumbai"

    DetailSectionCard(title = "Employee Legal Profile") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "EMPLOYEE ID", value = record.legalRecordNumber, modifier = Modifier.weight(1f))
            DetailStackedField(label = "NAME", value = employeeName, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        DetailStackedField(label = "LEGAL FRAMEWORK", value = "Indian Labour Law")
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "CONTRACT TYPE", value = record.employmentType.ifEmpty { "Permanent" }, modifier = Modifier.weight(1f))
            DetailStackedField(label = "DEPARTMENT", value = record.departmentId?.name?.ifEmpty { "Operations" } ?: "Operations", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "DATE OF JOINING", value = startDateSafe, modifier = Modifier.weight(1f))
            DetailStackedField(label = "PROBATION END", value = probationEndSafe, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "CONTRACT END", value = contractEndSafe, modifier = Modifier.weight(1f))
            DetailStackedField(label = "LOCATION", value = locationSafe, modifier = Modifier.weight(1f))
        }
    }

    Spacer(modifier = Modifier.height(tokens.screenPadding))

    DetailSectionCard(title = "Legal Compliance & Summary") {
        ComplianceRow(label = "Employment Contract", status = "Signed & Filed")
        ComplianceRow(label = "NDA Status", status = "Signed (Active)")
        ComplianceRow(label = "Code of Conduct", status = "Acknowledged (Annually)")
        ComplianceRow(label = "Labour Compliance", status = "100% Compliant", statusColor = greentext)
        ComplianceRow(label = "Active Legal Issues", status = "0 Issues", isPill = true)
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1: EMPLOYMENT & LABOUR TERMS
// ─────────────────────────────────────────────────────────────
@Composable
private fun TermsTabContent(record: LegalEmployeeItem, employeeName: String) {
    val tokens = LocalAppTokens.current
    val contractDateSafe = formatDateOnly(record.startDate).ifEmpty { "31 Jun 2032" }
    val nextEvalSafe = formatDateOnly(record.renewalDate).ifEmpty { "25 Mar 2027" }
    val contractIdSafe = record.contractId?.displayName?.takeIf { it.isNotBlank() } ?: "CON-99204"

    DetailSectionCard(title = "Employment & Labour Notes") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "Employee ID", value = record.legalRecordNumber, modifier = Modifier.weight(1f))
            DetailStackedField(label = "Contract Date", value = contractDateSafe, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "Full Name", value = employeeName, modifier = Modifier.weight(1f))
            DetailStackedField(label = "Contract ID", value = contractIdSafe, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "Next Evaluation Date", value = nextEvalSafe, modifier = Modifier.weight(1f))
            DetailStackedField(label = "Duration", value = "30 Months", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "Onboarding Status", value = "Introduced", modifier = Modifier.weight(1f))
            DetailStackedField(label = "Applicable Law", value = "India Labour Law", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(tokens.extraPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStackedField(label = "No. Contract Notes", value = "1", modifier = Modifier.weight(1f))
            DetailStackedField(label = "Classification", value = "Employment", modifier = Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 2: DOCUMENTS & COMPLIANCE
// ─────────────────────────────────────────────────────────────
@Composable
private fun DocumentsTabContent(record: LegalEmployeeItem) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "Employee Legal Documents",
        trailing = {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    ) {
        val allDocs = record.employmentContractDocuments + record.labourDocuments + record.supportingDocuments
        if (allDocs.isEmpty()) {
            // Default mock documents if API returned empty array
            DetailDocumentCard(
                icon = Icons.Default.Description,
                name = "Employment Contract.pdf",
                uploadedText = "Valid until expiry",
                typeLabel = "Contract",
                tags = listOf("Active"),
                onReview = {},
                onView = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailDocumentCard(
                icon = Icons.Default.Description,
                name = "NDA Agreement.pdf",
                uploadedText = "Confidential",
                typeLabel = "Agreement",
                tags = listOf("Active"),
                onReview = {},
                onView = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailDocumentCard(
                icon = Icons.Default.Description,
                name = "Code of Conduct.pdf",
                uploadedText = "Acknowledged",
                typeLabel = "Policy",
                tags = listOf("Active"),
                onReview = {},
                onView = {}
            )
        } else {
            allDocs.forEach { doc ->
                DetailDocumentCard(
                    icon = Icons.Default.Description,
                    name = doc.documentName,
                    uploadedText = "Valid until expiry",
                    typeLabel = "Contract",
                    tags = listOf("Active"),
                    onReview = {},
                    onView = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 3: LEGAL ISSUES
// ─────────────────────────────────────────────────────────────
@Composable
private fun LegalIssuesTabContent() {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "Legal Issues",
        trailing = {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Issue", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = tokens.extraPadding * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.imageDp)
                    .background(Primary_background, RoundedCornerShape(tokens.cardCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 1.5f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "All Clear",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No active legal issues to be displayed",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 4: HISTORY (Employee Legal Activity)
// ─────────────────────────────────────────────────────────────
@Composable
private fun HistoryTabContent(record: LegalEmployeeItem) {
//    val tokens = LocalAppTokens.current

    DetailSectionCard(title = "Employee Legal Activity") {
        if (record.activities.isEmpty()) {
            // Default sample items if backend activities list is empty
            DetailActivityTimeline(
                items = listOf(
                    DetailActivityItem(
                        title = "Annual contract & terms compliance",
                        subtitle = "By HR Legal",
                        date = "18 Jul 2026",
                        time = "2:45 PM"
                    ),
                    DetailActivityItem(
                        title = "Labour law enforcement reported",
                        subtitle = "By M Kumar (Legal)",
                        date = "02 Apr 2026",
                        time = "11:30 AM"
                    ),
                    DetailActivityItem(
                        title = "Employee legal record transfer",
                        subtitle = "From Branch Office",
                        date = "15 Jan 2026",
                        time = ""
                    )
                )
            )
        } else {
            DetailActivityTimeline(
                items = record.activities.map { act ->
                    val (dateFormatted, timeFormatted) = formatActivityDateTime(act.performedAt)
                    DetailActivityItem(
                        title = act.description,
                        subtitle = "By Legal System",
                        date = dateFormatted,
                        time = timeFormatted
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPLIANCE ROW HELPER
// ─────────────────────────────────────────────────────────────
@Composable
private fun ComplianceRow(
    label: String,
    status: String,
    statusColor: Color = TextPrimary,
    isPill: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = mutedText)
        if (isPill) {
            Text(
                text = status,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = greentext,
                modifier = Modifier
                    .background(greenBg, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else {
            Text(status, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = statusColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DATE FORMATTING UTILITIES
// ─────────────────────────────────────────────────────────────
private fun formatDateOnly(rawDate: String): String {
    if (rawDate.isBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(rawDate) ?: return rawDate.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        rawDate.take(10)
    }
}

private fun formatActivityDateTime(rawDate: String): Pair<String, String> {
    if (rawDate.isBlank()) return Pair("—", "")
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(rawDate) ?: return Pair(rawDate.take(10), "")
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
        val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(date)
        Pair(dateStr, timeStr)
    } catch (_: Exception) {
        Pair(rawDate.take(10), "")
    }
}