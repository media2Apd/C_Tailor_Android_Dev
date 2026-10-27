@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_documents

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
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
import com.cuso.tailor.model.legal.DocumentActivityDto
import com.cuso.tailor.model.legal.DocumentVersionDto
import com.cuso.tailor.model.legal.LegalDocumentDetailDto
import com.cuso.tailor.model.legal.RelatedRecordDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*

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

@Composable
fun LegalDocumentDetailScreen(
    documentId: String,
    viewModel: LegalViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEdit: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val documentDetail by viewModel.legalDocumentDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLegalDocumentDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingLegalDocumentStatus.collectAsStateWithLifecycle()
    val errorMessage by viewModel.legalDocumentDetailError.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCompliantDialog by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        if (documentId.isNotBlank()) {
            viewModel.fetchLegalDocumentDetail(documentId)
        }
    }

    if (isLoading && documentDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            ListSkeleton()
        }
        return
    }

    if (errorMessage != null && documentDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessage ?: "Failed to load document details",
                    fontSize = tokens.bodySmall,
                    color = redText
                )
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = { viewModel.fetchLegalDocumentDetail(documentId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(text = "Retry", color = whiteBg, fontSize = tokens.bodySmall)
                }
            }
        }
        return
    }

    val detail = documentDetail ?: return

    val tabs = remember {
        listOf(
            TabItem("Overview"),
            TabItem("File Versions"),
            TabItem("Linked Records"),
            TabItem("Access"),
            TabItem("History")
        )
    }

    val currentStatus = detail.status ?: "Draft"
    val isCompliantActive = currentStatus.equals("Active", ignoreCase = true) ||
            currentStatus.equals("Compliant", ignoreCase = true)

    val primaryAction = if (isCompliantActive) {
        DetailAction(
            label = "Compliant",
            icon = Icons.Default.Check,
            onClick = {} // Final stage: disabled
        )
    } else {
        DetailAction(
            label = "Mark Compliant",
            icon = Icons.Default.Check,
            onClick = { showCompliantDialog = true }
        )
    }

    val headerData = DetailHeaderData(
        icon = Icons.Default.Description,
        badges = emptyList(),
        title = detail.documentName ?: "Employee NDA – Chennai",
        metaItems = listOfNotNull(
            detail.documentNumber ?: "DOC-2026-00053",
            detail.documentType ?: "NDA",
            detail.confidentiality ?: "Confidential"
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
                    Toast.makeText(context, "Exporting document...", Toast.LENGTH_SHORT).show()
                }
            ),
            DetailAction(
                label = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    clipboardManager.setText(AnnotatedString("Legal Document: ${detail.documentName}"))
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        )
    )

    val summaryStats = listOf(
        DetailStat(
            label = "STATUS",
            value = detail.status ?: "In Progress",
            valueColor = if (isCompliantActive) greentext else orangeText,
            showDot = true
        ),
        DetailStat(
            label = "RISK PROFILE",
            value = detail.riskLevel ?: "Low Risk",
            valueColor = blueText,
            showDot = false
        ),
        DetailStat(
            label = "DOCUMENT VERSION",
            value = detail.version ?: "v1",
            valueColor = TextPrimary,
            showDot = false
        ),
        DetailStat(
            label = "EXPIRY DATE",
            value = formatDisplayDate(detail.expiryDate),
            valueColor = TextPrimary,
            showDot = false
        )
    )

    DetailScreenTemplate(
        title = "Legal Document",
        onClose = onClose,
        header = headerData,
        summaryStats = summaryStats,
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it }
    ) { currentTab ->
        when (currentTab) {
            0 -> DocumentOverviewTab(detail = detail)
            1 -> DocumentFileVersionsTab(versions = detail.versions.orEmpty())
            2 -> DocumentLinkedRecordsTab(related = detail.relatedRecords.orEmpty())
            3 -> DocumentAccessControlTab()
            4 -> DocumentHistoryTab(activities = detail.activities.orEmpty())
        }
    }

    // ── Mark Compliant Dialog ──
    if (showCompliantDialog) {
        DocumentMarkCompliantDialog(
            fromStatus = currentStatus,
            toStatus = "Active",
            isLoading = isUpdatingStatus,
            onDismiss = { showCompliantDialog = false },
            onConfirm = { comments ->
                viewModel.updateLegalDocumentStatus(
                    documentId = detail.id,
                    status = "Active",
                    comments = comments,
                    onSuccess = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
// Confirmation Dialog: Mark Compliant
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentMarkCompliantDialog(
    fromStatus: String,
    toStatus: String,
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
                // Header with yellow alert icon, title, and close button
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
                            text = "Mark Compliant",
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

                // Prompt description text
                Text(
                    text = "Change the status from $fromStatus to $toStatus?",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )

                Spacer(Modifier.height(14.dp))

                // Comments textarea
                FormTextArea(
                    value = comments,
                    onValueChange = { comments = it },
                    minLines = 4,
                    borderColor = sectionBorder,
                    focusedBorderColor = chartAmber,
                    placeholder = "Comments (optional)"
                )

                Spacer(Modifier.height(20.dp))

                // Action buttons: Cancel & Mark Compliant
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
                            containerColor = yellowText,
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
                                text = "Mark Compliant",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentOverviewTab(detail: LegalDocumentDetailDto) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "DOCUMENT INFORMATION",
        trailing = {
            Text(
                text = "Read-only view",
                fontSize = tokens.caption,
                color = mutedText
            )
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailStackedField(label = "Document ID", value = detail.documentNumber ?: "DOC-2026-00053", modifier = Modifier.weight(1f))
            DetailStackedField(label = "Type", value = detail.documentType ?: "NDA", modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailStackedField(label = "Department", value = detail.legalEntity ?: "HR Pool", modifier = Modifier.weight(1f))
            DetailStackedField(label = "Classification", value = detail.confidentiality ?: "Confidential", modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailStackedField(label = "Version", value = detail.version ?: "2.1", modifier = Modifier.weight(1f))
            DetailStackedField(label = "Created", value = formatDisplayDate(detail.createdAt), modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).padding(top = tokens.extraPadding)) {
                Text(text = "STATUS", fontSize = tokens.label, color = mutedText)
                Spacer(Modifier.height(4.dp))
                DetailPill(
                    badge = DetailBadge(
                        text = detail.status ?: "Active",
                        textColor = greentext,
                        bgColor = greenBg,
                        showDot = true
                    )
                )
            }
            DetailStackedField(label = "Last Modified", value = formatDisplayDate(detail.updatedAt), modifier = Modifier.weight(1f))
        }
    }

    Spacer(Modifier.height(tokens.extraPadding))

    DetailSectionCard(title = "TAGS") {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = tokens.extraPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tags = listOf(
                detail.confidentiality ?: "Confidential",
                detail.category ?: "Employment",
                detail.country ?: "Chennai Pool"
            )
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(activity_purple_bg)
                        .border(1.dp, light_blue_border, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "• $tag",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = blueText
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: File Versions Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentFileVersionsTab(versions: List<DocumentVersionDto>) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Version History",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "Upload Version", fontSize = tokens.bodySmall, color = whiteBg)
            }
        }

        if (versions.isEmpty()) {
            DetailCard {
                Box(modifier = Modifier.fillMaxWidth().padding(tokens.screenPadding * 2), contentAlignment = Alignment.Center) {
                    Text("No versions recorded.", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        } else {
            versions.forEach { ver ->
                DetailCard {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = ver.version ?: "v1",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                if (ver.isCurrentVersion == true) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(greenBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "current", fontSize = tokens.label, color = greentext)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                border = BorderStroke(1.dp, sectionBorder),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Preview", fontSize = tokens.caption, color = TextSecondary)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Uploaded: ${formatDisplayDate(ver.uploadedAt)}", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                            Text("By: ${ver.uploadedBy?.fullName ?: "HR Team"}", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                            Text("Size: 4.2 MB", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                            Text("Notes: ${ver.description ?: "Final version"}", fontSize = tokens.caption, color = mutedText, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Linked Records Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentLinkedRecordsTab(related: List<RelatedRecordDto>) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Linked Records", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                Box(
                    modifier = Modifier.size(20.dp).background(light_grey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${related.size}", fontSize = tokens.label, color = TextPrimary)
                }
            }

            TextButton(onClick = {}) {
                Text("+ Add Link", fontSize = tokens.bodySmall, color = Primary)
            }
        }

        related.forEach { record ->
            DetailCard {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (record.recordType == "Contract") purpleBg else greenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = record.recordType ?: "Contract",
                                fontSize = tokens.caption,
                                color = if (record.recordType == "Contract") purpleText else greentext
                            )
                        }
                        Text(text = record.recordId?.takeLast(8)?.uppercase() ?: "CTR-00412", fontSize = tokens.caption, color = mutedText)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(text = record.recordName ?: "Record Name", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text(text = "Master service & commercial agreement", fontSize = tokens.caption, color = headerGrey)

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = light_grey)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Record link active", fontSize = tokens.caption, color = mutedText)
                        Text(text = "View Record >", fontSize = tokens.bodySmall, color = Primary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 4: Access Tab
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentAccessControlTab() {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Document Access Control", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            TextButton(onClick = {}) {
                Text("+ Manage Access", fontSize = tokens.bodySmall, color = Primary)
            }
        }

        val accessRoles = listOf(
            Triple("Legal Team", "Read/Write", "L"),
            Triple("Department HR", "Read Only", "D"),
            Triple("Management", "View Only", "M")
        )

        accessRoles.forEach { (role, perm, initial) ->
            DetailCard {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).background(background_light_purple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = initial, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Primary)
                            }
                            Column {
                                Text(text = role, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(text = "User / Role", fontSize = tokens.caption, color = mutedText)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (perm == "Read/Write") greenBg else badgeGrey)
                                .border(1.dp, sectionBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = perm,
                                fontSize = tokens.caption,
                                color = if (perm == "Read/Write") greentext else TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = light_grey)
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Granted By: System Admin", fontSize = tokens.caption, color = headerGrey)
                        Text(text = "Access Date: 01 Jan 2026", fontSize = tokens.caption, color = headerGrey)
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
private fun DocumentHistoryTab(activities: List<DocumentActivityDto>) {
    val tokens = LocalAppTokens.current

    DetailSectionCard(
        title = "Document Activity",
        trailing = {
            Icon(Icons.Outlined.FilterList, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val history = activities.ifEmpty {
                listOf(
                    DocumentActivityDto(null, "New version 1.7 uploaded", "Revision includes updated IP clause", null, "12 Mar 2026"),
                    DocumentActivityDto(null, "Document reviewed", "Compliance check: Passed", null, "28 Feb 2026"),
                    DocumentActivityDto(null, "Document created", "Initial NDA template applied", null, "15 Jan 2026")
                )
            }

            history.forEachIndexed { index, act ->
                val isLast = index == history.lastIndex
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(12.dp).background(Primary, CircleShape)
                        )
                        if (!isLast) {
                            Box(
                                modifier = Modifier.width(1.5.dp).height(50.dp).background(sectionBorder)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = act.activityType ?: "Activity", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = act.description ?: "-", fontSize = tokens.caption, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        val author = act.performedBy?.fullName?.takeIf { it.isNotBlank() && it != "-" } ?: "Priya Sharma"
                        Text(text = "$author • ${act.performedAt ?: ""}", fontSize = tokens.caption, color = mutedText)
                    }
                }
            }
        }
    }
}