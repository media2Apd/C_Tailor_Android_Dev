@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_compliance

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.ComplianceActivityDto
import com.cuso.tailor.model.legal.LegalComplianceDetailDto
import com.cuso.tailor.model.legal.LegalComplianceDocumentDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

// ── Date and Timeline Utilities ──

private fun parseDate(rawDate: String?): Date? {
    if (rawDate.isNullOrBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )
    for (format in formats) {
        try {
            val parser = SimpleDateFormat(format, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(rawDate)
            if (parsed != null) return parsed
        } catch (_: Exception) {}
    }
    return null
}

private fun formatDisplayDate(rawDate: String?): String {
    val date = parseDate(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

private fun getTimeAgo(rawDate: String?): String {
    val date = parseDate(rawDate) ?: return "-"
    val diff = System.currentTimeMillis() - date.time
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    return when {
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        else -> "Just now"
    }
}

private data class RequirementItem(
    val name: String,
    val type: String,
    val isCompleted: Boolean,
    val dueDate: String
)

private data class DeadlineMilestone(
    val title: String,
    val date: String,
    val owner: String,
    val isOverdue: Boolean
)

// ── Main Screen ──

@Composable
fun LegalComplianceDetailScreen(
    complianceId: String,
    viewModel: LegalViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEdit: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val complianceDetail by viewModel.complianceDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingComplianceDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingComplianceStatus.collectAsStateWithLifecycle()
    val errorMessage by viewModel.complianceDetailError.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog state
    var showApprovalDialog by remember { mutableStateOf(false) }
    var showCompliantDialog by remember { mutableStateOf(false) }

    LaunchedEffect(complianceId) {
        if (complianceId.isNotBlank()) {
            viewModel.fetchComplianceDetail(complianceId)
        }
    }

    if (isLoading && complianceDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            ListSkeleton()
        }
        return
    }

    if (errorMessage != null && complianceDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessage ?: "Failed to load compliance details",
                    fontSize = tokens.bodySmall,
                    color = redText
                )
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = { viewModel.fetchComplianceDetail(complianceId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(text = "Retry", color = whiteBg, fontSize = tokens.bodySmall)
                }
            }
        }
        return
    }

    val detail = complianceDetail ?: return

    val tabs = remember {
        listOf(
            TabItem("Overview"),
            TabItem("Requirements"),
            TabItem("Documents"),
            TabItem("Deadlines"),
            TabItem("History")
        )
    }

    val currentStatus = detail.status.orEmpty()
    val isDraft = currentStatus.equals("Draft", ignoreCase = true)
    val isPendingApproval = currentStatus.equals("Pending_Approval", ignoreCase = true) ||
            currentStatus.equals("Pending", ignoreCase = true)
    val isCompliantActive = currentStatus.equals("Active", ignoreCase = true) ||
            currentStatus.equals("Compliant", ignoreCase = true)

    // Stage 1: Submit for Approval -> Stage 2: Mark Compliant -> Stage 3: Compliant (Disabled)
    val primaryAction = when {
        isDraft -> DetailAction(
            label = "Submit for Approval",
            icon = Icons.AutoMirrored.Filled.Send,
            onClick = { showApprovalDialog = true }
        )
        isPendingApproval -> DetailAction(
            label = "Mark Compliant",
            icon = Icons.Default.Check,
            onClick = { showCompliantDialog = true }
        )
        isCompliantActive -> DetailAction(
            label = "Compliant",
            icon = Icons.Default.Check,
            onClick = {} // Disabled stage: final state reached, no further transitions
        )
        else -> null
    }

    val headerData = DetailHeaderData(
        icon = Icons.Default.Description,
        badges = emptyList(),
        title = detail.complianceName ?: "Employment Contract Registration",
        metaItems = listOfNotNull(
            detail.complianceCode ?: "CMP-2026-00311",
            detail.complianceCategory ?: "Labour",
            detail.jurisdiction ?: "India / Chennai"
        ),
        primaryAction = primaryAction,
        secondaryActions = listOf(
            DetailAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                onClick = { onEdit(detail.id) }
            ),
            DetailAction(
                label = "Download",
                icon = Icons.Default.FileDownload,
                onClick = {
                    Toast.makeText(context, "Exporting Compliance data...", Toast.LENGTH_SHORT).show()
                }
            ),
            DetailAction(
                label = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    clipboardManager.setText(AnnotatedString("Compliance: ${detail.complianceName} (${detail.complianceCode})"))
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        )
    )

    val summaryStats = listOf(
        DetailStat(
            label = "STATUS",
            value = detail.status?.replace("_", " ") ?: "Pending",
            valueColor = if (isPendingApproval) blueText else if (isCompliantActive) greentext else TextPrimary,
            showDot = true
        ),
        DetailStat(
            label = "RISK",
            value = detail.riskLevel ?: "High",
            valueColor = yellowText,
            showDot = true
        ),
        DetailStat(
            label = "DUE",
            value = formatDisplayDate(detail.dueDate),
            valueColor = TextPrimary,
            showDot = false
        ),
        DetailStat(
            label = "OWNER",
            value = detail.responsibleTeam ?: detail.complianceOwner?.fullName ?: "HR Team",
            valueColor = TextPrimary,
            showDot = false
        )
    )

    DetailScreenTemplate(
        title = "Compliance Management",
        onClose = onClose,
        header = headerData,
        summaryStats = summaryStats,
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it }
    ) { currentTab ->
        when (currentTab) {
            0 -> ComplianceOverviewTab(detail = detail)
            1 -> ComplianceRequirementsTab(detail = detail)
            2 -> ComplianceDocumentsTab(detail = detail, context = context)
            3 -> ComplianceDeadlinesTab(detail = detail)
            4 -> ComplianceHistoryTab(activities = detail.activities.orEmpty())
        }
    }

    // ── Dialog 1: Submit for Approval ──
    if (showApprovalDialog) {
        ComplianceStatusDialog(
            title = "Submit for Approval",
            fromStatus = "Draft",
            toStatus = "Pending",
            confirmButtonText = "Submit for Approval",
            confirmButtonColor = yellowText,
            isLoading = isUpdatingStatus,
            onDismiss = { showApprovalDialog = false },
            onConfirm = { comments ->
                viewModel.updateComplianceStatus(
                    complianceId = detail.id,
                    status = "Pending_Approval",
                    comments = comments,
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        showApprovalDialog = false
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // ── Dialog 2: Mark Compliant ──
    if (showCompliantDialog) {
        ComplianceStatusDialog(
            title = "Mark Compliant",
            fromStatus = "Pending",
            toStatus = "Active",
            confirmButtonText = "Mark Compliant",
            confirmButtonColor = yellowText,
            isLoading = isUpdatingStatus,
            onDismiss = { showCompliantDialog = false },
            onConfirm = { comments ->
                viewModel.updateComplianceStatus(
                    complianceId = detail.id,
                    status = "Active",
                    comments = comments,
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        showCompliantDialog = false
                    },
                    onError = { err ->
                        Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable Status Confirmation Dialog
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceStatusDialog(
    title: String,
    fromStatus: String,
    toStatus: String,
    confirmButtonText: String,
    confirmButtonColor: Color,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (comments: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var comments by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg
        ) {
            Column(modifier = Modifier.padding(tokens.cardPadding)) {
                // Header with yellow alert icon, title, and close action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = yellowText,
                            modifier = Modifier.size(tokens.iconSize * 1.2f)
                        )
                        Text(
                            text = title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = iconMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Status Transition Description
                Text(
                    text = "Change the status from $fromStatus to $toStatus?",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )

                Spacer(Modifier.height(14.dp))

                // Multi-line Comments Input Field
                FormTextArea(
                    value = comments,
                    onValueChange = { comments = it },
                    minLines = 4,
                    borderColor = sectionBorder,
                    focusedBorderColor = chartAmber,
                    placeholder = "Comments (optional)"
                )

                Spacer(Modifier.height(20.dp))

                // Bottom Buttons: Cancel and Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.height(tokens.buttonHeight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = whiteBg,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = { onConfirm(comments) },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor,
                            contentColor = whiteBg
                        ),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = confirmButtonText,
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
}

// ─────────────────────────────────────────────────────────────
// Tab 1: Overview Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceOverviewTab(detail: LegalComplianceDetailDto) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "Compliance Details",
        trailing = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = iconMuted,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    ) {
        DetailInfoRow(label = "Compliance ID", value = detail.complianceCode ?: "-")
        DetailInfoRow(label = "Category", value = detail.complianceCategory ?: "-")
        DetailInfoRow(label = "Jurisdiction", value = detail.jurisdiction ?: "-")
        DetailInfoRow(label = "Due Date", value = formatDisplayDate(detail.dueDate))
        DetailInfoRow(label = "Owner", value = detail.responsibleTeam ?: "HR Pool")
        DetailInfoRow(label = "Priority", value = detail.priority ?: "Medium")

        DetailBadgeRow(
            label = "Status",
            badge = DetailBadge(
                text = detail.status?.replace("_", " ") ?: "Pending",
                textColor = blueText,
                bgColor = activity_purple_bg,
                showDot = false
            )
        )

        DetailBadgeRow(
            label = "Risk",
            badge = DetailBadge(
                text = detail.riskLevel ?: "High",
                textColor = yellowText,
                bgColor = yellowBg,
                showDot = true
            ),
            showDivider = false
        )
    }

    Spacer(Modifier.height(tokens.extraPadding))

    DetailSectionCard(
        title = "Compliance Progress",
        trailing = {
            Text(
                text = "Step 3 of 4",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Overall Completion", fontSize = tokens.caption, color = mutedText)
                Text(text = "50%", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
            }

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(light_grey)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Primary)
                )
            }

            Spacer(Modifier.height(tokens.screenPadding))

            val steps = listOf(
                DetailStepItem(
                    title = "Documentation",
                    subtitle = "Completed on 12 Aug 2026",
                    state = TimelineState.DONE
                ),
                DetailStepItem(
                    title = "Verification",
                    subtitle = "Verified by Legal Counsel",
                    state = TimelineState.DONE
                ),
                DetailStepItem(
                    title = "Submission",
                    subtitle = "Awaiting registry confirmation",
                    state = TimelineState.CURRENT
                ),
                DetailStepItem(
                    title = "Approval",
                    subtitle = "Final regulatory certificate",
                    state = TimelineState.PENDING
                )
            )

            DetailStepTimeline(steps = steps)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: Requirements Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceRequirementsTab(detail: LegalComplianceDetailDto) {
    val tokens = LocalAppTokens.current

    val requirements = remember(detail) {
        listOf(
            RequirementItem("File Form 5B", detail.requirementType ?: "Filing", false, formatDisplayDate(detail.dueDate)),
            RequirementItem("Documented Statement", "Document", false, formatDisplayDate(detail.effectiveDate)),
            RequirementItem("Contract of Visa", "Payment", false, formatDisplayDate(detail.dueDate))
        )
    }

    DetailSectionCard(
        title = "Compliance Requirements",
        trailing = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(light_grey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${requirements.size}",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = "Sort",
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            requirements.forEach { req ->
                DetailCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.name,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(light_grey)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = req.type, fontSize = tokens.label, color = TextSecondary)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Due: ${req.dueDate}",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }
                        }

                        DetailPill(
                            badge = DetailBadge(
                                text = "Pending",
                                textColor = blueText,
                                bgColor = activity_purple_bg,
                                showDot = true
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Documents Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceDocumentsTab(detail: LegalComplianceDetailDto, context: Context) {
    val tokens = LocalAppTokens.current
    val docs: List<LegalComplianceDocumentDto> = detail.supportingDocuments ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contractual Documents",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Button(
                onClick = {
                    Toast.makeText(context, "Upload Document Clicked", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = "Upload", fontSize = tokens.bodySmall, color = whiteBg)
            }
        }

        if (docs.isEmpty()) {
            DetailCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding * 2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contractual documents attached.",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            }
        } else {
            docs.forEach { doc ->
                val documentUrl = doc.url
                val filename = doc.originalFilename ?: "Document.pdf"
                val uploadedDateText = formatDisplayDate(doc.uploadedAt)

                DetailCard {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .background(light_grey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = iconMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = filename,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(light_grey)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "Draft", fontSize = tokens.label, color = TextSecondary)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = uploadedDateText,
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (!documentUrl.isNullOrBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, documentUrl.toUri())
                                        context.startActivity(intent)
                                    }
                                },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, sectionBorder),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(text = "Preview", fontSize = tokens.caption, color = TextSecondary)
                            }

                            Spacer(Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    if (!documentUrl.isNullOrBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, documentUrl.toUri())
                                        context.startActivity(intent)
                                    }
                                },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, sectionBorder),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(text = "Download", fontSize = tokens.caption, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 4: Deadlines Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceDeadlinesTab(detail: LegalComplianceDetailDto) {
    val tokens = LocalAppTokens.current

    val deadlines = remember(detail) {
        listOf(
            DeadlineMilestone("Draft submission", formatDisplayDate(detail.effectiveDate), "Finance", true),
            DeadlineMilestone("Review & approve", formatDisplayDate(detail.renewalReviewDate), "CFO", true),
            DeadlineMilestone("Final filing", formatDisplayDate(detail.dueDate), "HR Pool", true)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Deadlines",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            TextButton(onClick = {}) {
                Text(
                    text = "Add Milestone +",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }

        deadlines.forEach { milestone ->
            DetailCard {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = milestone.title,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Date: ${milestone.date}",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }

                        DetailPill(
                            badge = DetailBadge(
                                text = "Overdue",
                                textColor = redText,
                                bgColor = roseBg,
                                showDot = true
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = light_grey)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Owner", fontSize = tokens.caption, color = mutedText)
                        Text(
                            text = milestone.owner,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 5: History Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComplianceHistoryTab(activities: List<ComplianceActivityDto>) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "Activity History",
        trailing = {
            Text(
                text = "${activities.size} updates",
                fontSize = tokens.caption,
                color = mutedText
            )
        }
    ) {
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No activity logs recorded.", fontSize = tokens.bodySmall, color = mutedText)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                activities.forEachIndexed { index, act ->
                    val isLast = index == activities.lastIndex
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(whiteBg, CircleShape)
                                    .border(2.dp, Primary, CircleShape)
                            )
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(48.dp)
                                        .background(sectionBorder)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = act.activityType ?: "Status changed",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = getTimeAgo(act.performedAt),
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = act.description ?: "Updated by Compliance Team",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }
                }
            }
        }
    }
}