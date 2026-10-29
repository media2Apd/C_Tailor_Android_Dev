@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.legal.legal_notice

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalNoticeItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.shareCsvFileSafely
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.LegalViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Case stages in ordered sequence matching React CASE_STAGES
private val CASE_STAGES = listOf(
    "Case_Filed" to "Case Filed",
    "Acknowledged" to "Acknowledged",
    "Under_Review" to "Under Review",
    "Notice_Sent" to "Notice Sent",
    "Response_Received" to "Response Received",
    "Mediation_Scheduled" to "Mediation Scheduled",
    "Resolution_Pending" to "Resolution Pending",
    "Case_Closed" to "Case Closed"
)

private val STAGE_LABEL_MAP = CASE_STAGES.toMap()

// Represents an action in the Log Action dropdown menu
private data class StageAction(
    val caseStatus: String,
    val label: String
)

@Composable
fun LegalNoticeDetailScreen(
    noticeId: String,
    viewModel: LegalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    val detail by viewModel.noticeDetail.collectAsState()
    val isLoading by viewModel.isLoadingNoticeDetail.collectAsState()
    val errorMessage by viewModel.noticeDetailError.collectAsState()
    val isUpdatingStatus by viewModel.isUpdatingNoticeStatus.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var actionMenuExpanded by remember { mutableStateOf(false) }
    var pendingStageAction by remember { mutableStateOf<StageAction?>(null) }
    var showUploadModal by remember { mutableStateOf(false) }

    LaunchedEffect(noticeId) {
        viewModel.fetchLegalNoticeDetail(noticeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalNoticeDetail()
        }
    }

    // Export CSV helper matching React handleExport
    fun exportToCsv(item: LegalNoticeItem) {
        try {
            val csvBuilder = StringBuilder()
            csvBuilder.append("Field,Value\n")
            csvBuilder.append("Case ID,\"${item.caseNumber}\"\n")
            csvBuilder.append("Title,\"${item.caseTitle}\"\n")
            csvBuilder.append("Case Type,\"${item.caseType}\"\n")
            csvBuilder.append("Party Type,\"${item.partyType}\"\n")
            csvBuilder.append("Primary Party,\"${item.primaryParty}\"\n")
            csvBuilder.append("Status,\"${item.status}\"\n")
            csvBuilder.append("Case Stage,\"${item.caseStatus ?: item.status}\"\n")
            csvBuilder.append("Risk Level,\"${item.riskLevel}\"\n")
            csvBuilder.append("Priority,\"${item.priority}\"\n")
            csvBuilder.append("Country,\"${item.displayCountry}\"\n")
            csvBuilder.append("Jurisdiction,\"${item.jurisdiction}\"\n")
            csvBuilder.append("Next Deadline,\"${item.nextDeadline}\"\n")
            csvBuilder.append("Opened Date,\"${item.createdAt}\"\n")
            csvBuilder.append("Case Description,\"${item.caseDescription.replace("\"", "\"\"")}\"\n")
            csvBuilder.append("Notes,\"${item.notes.replace("\"", "\"\"")}\"\n")

            val file = File(context.cacheDir, "${item.caseNumber.ifBlank { "legal-case" }}-details.csv")
            file.writeText(csvBuilder.toString())

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Case Data"))
            Toast.makeText(context, "Case data exported successfully", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Failed to export case data", Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoading) {
        ListSkeleton()
    } else if (!errorMessage.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBg)
                .padding(tokens.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = errorMessage ?: "Failed to load case detail", fontSize = tokens.bodySmall, color = redText)
        }
    } else if (detail != null) {
        val item = detail!!

        // Locking rule: Case is locked when status is Completed or Pending
        val isCompleted = item.status.equals("Completed", ignoreCase = true)
        val isLockedCase = isCompleted || item.status.equals("Pending", ignoreCase = true)

        // Stage index calculation matching React stageIndex(r.caseStatus)
        // If not started yet or null, currentStageIndex is -1 and nextStage becomes CASE_STAGES[0] ("Case_Filed")
        val currentStageIndex = CASE_STAGES.indexOfFirst { it.first.equals(item.caseStatus, ignoreCase = true) }
        val nextStage = if (currentStageIndex + 1 in CASE_STAGES.indices) CASE_STAGES[currentStageIndex + 1] else null

        // Build dynamic stage actions for the Log Action menu
        val stageActions = remember(item.status, item.caseStatus, currentStageIndex) {
            if (isLockedCase || currentStageIndex >= CASE_STAGES.lastIndex) {
                emptyList()
            } else {
                listOfNotNull(
                    // 1. Next sequential stage (e.g. Move to Case Filed, Move to Acknowledged, etc.)
                    nextStage?.let {
                        StageAction(
                            caseStatus = it.first,
                            label = "Move to ${it.second}"
                        )
                    },
                    // 2. Direct Close Case option (omitted if nextStage is already Case_Closed to prevent duplication)
                    if (nextStage?.first != "Case_Closed") {
                        StageAction(
                            caseStatus = "Case_Closed",
                            label = "Close Case"
                        )
                    } else null
                )
            }
        }

        // Secondary action buttons: Edit, Export, Download, Share
        val secondaryActions = listOf(
            DetailAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                enabled = !isLockedCase,
                onClick = { onNavigateToEdit(item.id) }
            ),
            DetailAction(
                label = "Export",
                icon = Icons.Default.FileDownload,
                onClick = { exportToCsv(item) }
            ),
            DetailAction(
                label = "Download",
                icon = Icons.Default.Download,
                onClick = { exportToCsv(item) }
            ),
            DetailAction(
                label = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    if (isSharing) return@DetailAction
                    isSharing = true

                    val csvData = "Record Number,Status\n${item.caseNumber},${item.status}"

                    shareCsvFileSafely(
                        context = context,
                        coroutineScope = coroutineScope,
                        fileName = item.caseNumber.ifEmpty { "record-details" },
                        csvContent = csvData,
                        onFinish = { isSharing = false }
                    )
                }
            )
        )

        // Header configuration: Log Action is visible only when stageActions are available
        val headerData = DetailHeaderData(
            icon = Icons.Default.Description,
            badges = listOf(
                DetailBadge(
                    text = item.status.ifBlank { "In Progress" },
                    textColor = if (isCompleted) greentext else Primary,
                    bgColor = if (isCompleted) greenBg else primary_light,
                    showDot = true
                )
            ),
            title = item.caseTitle.ifBlank { "Case Details" },
            metaItems = listOfNotNull(
                item.caseNumber.ifBlank { null },
                item.caseType.ifBlank { null },
                item.partyType.ifBlank { null },
                STAGE_LABEL_MAP[item.caseStatus]
            ),
            secondaryActions = secondaryActions,
            primaryAction = if (stageActions.isNotEmpty()) {
                DetailAction(
                    label = "Log Action",
                    onClick = { actionMenuExpanded = true }
                )
            } else null
        )

        val summaryStats = listOf(
            DetailStat(label = "STATUS", value = item.status, valueColor = Primary, showDot = true),
            DetailStat(label = "RISK", value = item.riskLevel, valueColor = if (item.riskLevel.equals("High", true)) redText else yellowText, showDot = true),
            DetailStat(label = "TYPE", value = item.caseType.ifBlank { "Dispute" }),
            DetailStat(label = "OPENED", value = formatNoticeIsoDate(item.createdAt)),
            DetailStat(label = "NEXT ACTION", value = formatNoticeIsoDate(item.nextDeadline), valueColor = Primary)
        )

        val tabs = listOf(
            TabItem("Overview"),
            TabItem("Parties & Reference"),
            TabItem("Notices & Documents"),
            TabItem("Orders & Deadlines"),
            TabItem("Case Activity")
        )

        Box(modifier = Modifier.fillMaxSize()) {
            DetailScreenTemplate(
                title = "Legal Notice & Dispute",
                onClose = onNavigateBack,
                header = headerData,
                summaryStats = summaryStats,
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            ) { activeIndex ->
                when (activeIndex) {
                    0 -> NoticeOverviewTab(item)
                    1 -> NoticePartiesTab(item)
                    2 -> NoticeDocumentsTab(
                        item = item,
                        onOpenUpload = { showUploadModal = true }
                    )
                    3 -> NoticeOrdersTab(item)
                    4 -> NoticeActivityTab(item)
                }
            }

            // Log Action dropdown menu
            if (stageActions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 175.dp, end = tokens.screenPadding)
                ) {
                    DropdownMenu(
                        expanded = actionMenuExpanded,
                        onDismissRequest = { actionMenuExpanded = false },
                        offset = DpOffset(0.dp, 4.dp),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        modifier = Modifier
                            .background(whiteBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
                    ) {
                        stageActions.forEach { action ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = action.label,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    actionMenuExpanded = false
                                    pendingStageAction = action
                                }
                            )
                        }
                    }
                }
            }
        }

        // Confirmation Modal Dialog matching Web UI and Payload
        pendingStageAction?.let { action ->
            val isClosingCase = action.caseStatus == "Case_Closed"
            val currentStageLabel = STAGE_LABEL_MAP[item.caseStatus] ?: "Not started"
            val targetStageLabel = STAGE_LABEL_MAP[action.caseStatus] ?: "Case Closed"
            val dialogTitle = action.label

            Dialog(
                onDismissRequest = {
                    if (!isUpdatingStatus) pendingStageAction = null
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = whiteBg,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Top Row: Icon + Title + Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (isClosingCase) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isClosingCase) Icons.Default.ErrorOutline else Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = if (isClosingCase) Color(0xFFDC2626) else Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = dialogTitle,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { pendingStageAction = null },
                                enabled = !isUpdatingStatus,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = close_color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Body Message
                        Text(
                            text = if (isClosingCase) {
                                "Move this case from $currentStageLabel to $targetStageLabel? A closed case is marked Completed and can't be edited."
                            } else {
                                "Move this case from $currentStageLabel to $targetStageLabel?"
                            },
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            lineHeight = tokens.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Action Buttons in Single Row (Cancel + Action)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { pendingStageAction = null },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                border = BorderStroke(1.dp, BorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = whiteBg,
                                    contentColor = TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp),
                                enabled = !isUpdatingStatus
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    viewModel.updateLegalNoticeStatus(
                                        noticeId = item.id,
                                        status = action.caseStatus,
                                        onSuccess = {
                                            pendingStageAction = null
                                            viewModel.fetchLegalNoticeDetail(item.id)
                                            Toast.makeText(context, "Case moved to $targetStageLabel", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            pendingStageAction = null
                                            Toast.makeText(context, "Failed to update case stage", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isClosingCase) Color(0xFFDC2626) else Color(0xFFE59800),
                                    contentColor = whiteBg
                                ),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp),
                                enabled = !isUpdatingStatus
                            ) {
                                Text(
                                    text = if (isUpdatingStatus) "Saving..." else dialogTitle,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upload Document Modal
        if (showUploadModal) {
            UploadDocumentModal(
                item = item,
                viewModel = viewModel,
                onDismiss = { showUploadModal = false }
            )
        }
    }
}

// ── Tab 0: Overview ──
@Composable
private fun NoticeOverviewTab(item: LegalNoticeItem) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text("Case Details", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Case ID", value = item.caseNumber.ifBlank { "-" }, modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Type", value = item.caseType.ifBlank { "-" }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Party", value = item.primaryParty.ifBlank { "-" }, modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Party Type", value = item.partyType.ifBlank { "-" }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Filed By", value = item.createdBy?.fullName ?: "Procurement", modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Date Filed", value = formatNoticeIsoDate(item.createdAt), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Legal Owner", value = item.legalOwner?.fullName ?: "Legal Counsel", modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Status", value = item.status, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Priority", value = item.priority, modifier = Modifier.weight(1f))
                    DetailStackedField(
                        label = "Linked Contract",
                        value = item.relatedContract?.contractName ?: item.relatedContract?.contractNumber ?: "-",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Jurisdiction", value = item.jurisdiction.ifBlank { item.displayCountry }, modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Country / Branch", value = "${item.displayCountry} / ${item.branchId?.name ?: "-"}", modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Risk Level", value = item.riskLevel, modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Next Deadline", value = formatNoticeIsoDate(item.nextDeadline), modifier = Modifier.weight(1f))
                }

                if (item.caseDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    HorizontalDivider(color = light_grey, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    Text("Case Description", fontSize = tokens.label, color = mutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.caseDescription, fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        }

        // Stepper: Case Progress
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text("Case Progress", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                val currentIdx = CASE_STAGES.indexOfFirst { it.first.equals(item.caseStatus, ignoreCase = true) }

                val steps = CASE_STAGES.mapIndexed { idx, stage ->
                    val state = when {
                        idx < currentIdx -> TimelineState.DONE
                        idx == currentIdx -> TimelineState.CURRENT
                        else -> TimelineState.PENDING
                    }
                    DetailStepItem(
                        title = stage.second,
                        subtitle = if (state == TimelineState.DONE) "Stage completed" else if (state == TimelineState.CURRENT) "Current active stage" else "Pending milestone",
                        state = state
                    )
                }
                DetailStepTimeline(steps = steps)
            }
        }
    }
}

// ── Tab 1: Parties & Reference ──
@Composable
private fun NoticePartiesTab(item: LegalNoticeItem) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text("Involved Parties", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                DetailInfoRow(label = "OPPOSING PARTY", value = item.primaryParty.ifBlank { "-" })
                DetailInfoRow(label = "PARTY TYPE", value = item.partyType.ifBlank { "-" })
                DetailInfoRow(label = "CASE OWNER", value = item.legalOwner?.fullName ?: "-")
                DetailInfoRow(label = "FILED BY", value = item.createdBy?.fullName ?: "-")
                DetailInfoRow(label = "COUNTRY / BRANCH", value = "${item.displayCountry} / ${item.branchId?.name ?: "-"}")
                DetailInfoRow(label = "JURISDICTION", value = item.jurisdiction.ifBlank { "-" }, showDivider = false)
            }
        }

        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text("Reference Records", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                DetailLinkRow(
                    icon = Icons.Default.Description,
                    title = "CONTRACT",
                    subtitle = item.relatedContract?.contractNumber ?: item.relatedContract?.contractName ?: "-",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                DetailLinkRow(
                    icon = Icons.Default.Description,
                    title = "INVOICE",
                    subtitle = item.relatedInvoice?.invoiceNumber ?: item.relatedInvoice?.id ?: "-",
                    onClick = {}
                )
                val orderDisplay = item.relatedOrder?.displayOrderNumber.orEmpty()
                if (orderDisplay.isNotBlank() && orderDisplay != "-") {
                    Spacer(modifier = Modifier.height(tokens.extraPadding))
                    DetailLinkRow(
                        icon = Icons.Default.Description,
                        title = "ORDER",
                        subtitle = orderDisplay,
                        onClick = {}
                    )
                }
            }
        }
    }
}

// ── Tab 2: Notices & Documents ──
@Composable
private fun NoticeDocumentsTab(
    item: LegalNoticeItem,
    onOpenUpload: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val allDocs = item.caseDocuments + item.supportingEvidence

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Notices & Case Documents", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text("${allDocs.size} uploaded legal files", fontSize = tokens.caption, color = mutedText)
            }
            Button(
                onClick = onOpenUpload,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius)
            ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.8f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", fontSize = tokens.caption, color = whiteBg)
            }
        }

        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                if (allDocs.isEmpty()) {
                    Text("No documents uploaded for this case", fontSize = tokens.bodySmall, color = mutedText)
                } else {
                    allDocs.forEachIndexed { index, doc ->
                        val fileName = doc.publicId.substringAfterLast("/").ifBlank { doc.documentType }
                        DetailDocumentCard(
                            icon = Icons.Default.Description,
                            name = fileName,
                            uploadedText = formatNoticeIsoDate(doc.uploadedAt),
                            typeLabel = doc.documentType.replace("_", " "),
                            tags = listOf("Active"),
                            onReview = {
                                if (doc.documentUrl.isNotBlank()) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, doc.documentUrl.toUri())
                                    context.startActivity(browserIntent)
                                }
                            },
                            onView = {
                                if (doc.documentUrl.isNotBlank()) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, doc.documentUrl.toUri())
                                    context.startActivity(browserIntent)
                                }
                            }
                        )
                        if (index < allDocs.lastIndex) {
                            Spacer(modifier = Modifier.height(tokens.extraPadding))
                            HorizontalDivider(color = light_grey, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(tokens.extraPadding))
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 3: Orders & Deadlines ──
@Composable
private fun NoticeOrdersTab(item: LegalNoticeItem) {
    val tokens = LocalAppTokens.current
    val currentStage = STAGE_LABEL_MAP[item.caseStatus] ?: "In Progress"
    val daysUntilDeadline = calculateNoticeRemaining(item.nextDeadline)

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text("Case Orders & Deadlines", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NAME", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1.5f))
                    Text("DUE DATE", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1f))
                    Text("OWNER", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Next Action: $currentStage", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(daysUntilDeadline.first, fontSize = tokens.caption, color = if (daysUntilDeadline.second) redText else mutedText)
                    }
                    Text(formatNoticeIsoDate(item.nextDeadline), fontSize = tokens.caption, color = headerGrey, modifier = Modifier.weight(1f))
                    Text(item.legalOwner?.fullName ?: "Legal Team", fontSize = tokens.caption, color = headerGrey, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Tab 4: Activity ──
@Composable
private fun NoticeActivityTab(item: LegalNoticeItem) {
    val tokens = LocalAppTokens.current

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text("Case Activity", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
            Spacer(modifier = Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(modifier = Modifier.height(tokens.extraPadding))

            if (item.activity.isEmpty()) {
                Text("No activities logged for this case.", fontSize = tokens.bodySmall, color = mutedText)
            } else {
                val timelineItems = item.activity.map { act ->
                    val dateStr = formatNoticeIsoDate(act.performedAt)
                    DetailActivityItem(
                        date = dateStr,
                        time = "Log",
                        title = act.action.replace("_", " "),
                        subtitle = "${act.description}\nPerformed by: ${act.performedBy?.fullName ?: "Legal Team"}"
                    )
                }
                DetailActivityTimeline(items = timelineItems)
            }
        }
    }
}

// ── Document Upload Modal ──
@Composable
private fun UploadDocumentModal(
    item: LegalNoticeItem,
    viewModel: LegalViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current

    var selectedCategory by remember { mutableStateOf("Case_Document") }
    var selectedCategoryExpanded by remember { mutableStateOf(false) }
    var uploadedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uploadedUris = (uploadedUris + uris).take(5)
    }

    fun toTextPart(value: String): RequestBody {
        return value.trim().toRequestBody("text/plain".toMediaTypeOrNull())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Upload Document", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color, modifier = Modifier.size(tokens.iconSize))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                FormDropdown(
                    label = "Document Category",
                    value = if (selectedCategory == "Case_Document") "Case Document" else "Supporting Evidence",
                    expanded = selectedCategoryExpanded,
                    onExpandChange = { selectedCategoryExpanded = it },
                    options = listOf("Case Document", "Supporting Evidence"),
                    onOptionSelected = { chosen ->
                        selectedCategory = if (chosen == "Case Document") "Case_Document" else "Supporting_Evidence"
                    }
                )

                ImageUploadSection(
                    selectedImages = uploadedUris.map { it.toString() },
                    onBrowseClick = { filePicker.launch("*/*") },
                    onRemoveImage = { uriStr -> uploadedUris = uploadedUris.filter { it.toString() != uriStr } },
                    maxFiles = 5,
                    title = "Click to upload files",
                    subtitle = "PDF, DOCX, JPG up to 50 MB",
                    supportedFormatsText = "PDF, DOCX, JPG, PNG"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (uploadedUris.isEmpty()) {
                        Toast.makeText(context, "Please select at least one file", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUploading = true
                    val fileParts: List<MultipartBody.Part> = uploadedUris.mapNotNull { uri ->
                        val paramName = if (selectedCategory == "Case_Document") "caseDocuments" else "supportingEvidence"
                        uriToMultipartPart(context, uri, paramName)
                    }

                    val fieldsMap = mutableMapOf(
                        "caseTitle" to toTextPart(item.caseTitle)
                    )

                    val caseDocParts = if (selectedCategory == "Case_Document") fileParts else emptyList()
                    val supportingParts = if (selectedCategory == "Supporting_Evidence") fileParts else emptyList()

                    viewModel.updateLegalNotice(
                        noticeId = item.id,
                        fields = fieldsMap,
                        caseDocuments = caseDocParts,
                        supportingEvidence = supportingParts,
                        onSuccess = {
                            isUploading = false
                            onDismiss()
                            viewModel.fetchLegalNoticeDetail(item.id)
                            Toast.makeText(context, "Documents uploaded successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            isUploading = false
                            Toast.makeText(context, "Failed to upload document", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isUploading && uploadedUris.isNotEmpty()
            ) {
                Text(if (isUploading) "Uploading..." else "Upload", fontSize = tokens.bodySmall, color = whiteBg)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
            }
        }
    )
}

private fun formatNoticeIsoDate(isoDate: String): String {
    if (isoDate.isBlank()) return "-"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return isoDate.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun calculateNoticeRemaining(dateIso: String): Pair<String, Boolean> {
    if (dateIso.isBlank()) return Pair("-", false)
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val target = parser.parse(dateIso) ?: return Pair("-", false)
        val diffMs = target.time - System.currentTimeMillis()
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        if (diffDays >= 0) {
            Pair("(${diffDays}d remaining)", false)
        } else {
            Pair("(Ended ${-diffDays}d ago)", true)
        }
    } catch (_: Exception) {
        Pair("-", false)
    }
}