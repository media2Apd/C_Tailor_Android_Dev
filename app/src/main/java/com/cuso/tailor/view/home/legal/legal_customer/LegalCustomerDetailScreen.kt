@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_customer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.hr.displayName
import com.cuso.tailor.model.legal.LegalCustomerDocumentDto
import com.cuso.tailor.model.legal.LegalCustomerItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.home.legal.detail_template.*
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun parseIsoDate(rawDate: String?): Date? {
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
            val date = parser.parse(rawDate)
            if (date != null) return date
        } catch (_: Exception) {}
    }
    return null
}

private fun formatDisplayDate(rawDate: String?): String {
    val date = parseIsoDate(rawDate) ?: return "-"
    return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
}

// ── Action Dialog Configuration Data Model ──
private data class LegalActionDialogState(
    val title: String,
    val targetStatus: String,
    val confirmButtonText: String,
    val isDisputeAction: Boolean
)

@Composable
fun LegalCustomerDetailScreen(
    recordId: String,
    viewModel: LegalViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEditRecord: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Fetch members to resolve owner & performedBy names from IDs
    val memberList by hrViewModel.members.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers(page = 1, limit = 100)
    }

    LaunchedEffect(recordId) {
        if (recordId.isNotBlank()) {
            viewModel.fetchLegalCustomerDetail(recordId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLegalCustomerDetail()
        }
    }

    val customerDetail by viewModel.customerDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCustomerDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingCustomerStatus.collectAsStateWithLifecycle()
    val errorMessage by viewModel.customerDetailError.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            TabItem("Overview"),
            TabItem("Terms & Consent"),
            TabItem("Documents"),
            TabItem("Disputes"),
            TabItem("Activity")
        )
    }

    var isLegalActionMenuExpanded by remember { mutableStateOf(false) }
    var activeDialogState by remember { mutableStateOf<LegalActionDialogState?>(null) }

    if (isLoading && customerDetail == null) {
        ListSkeleton()
        return
    }

    val record = customerDetail
    if (record == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = errorMessage ?: "Record not found", fontSize = tokens.bodySmall, color = redText)
                Spacer(Modifier.height(tokens.extraPadding))
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                ) {
                    Text(text = "Go Back", fontSize = tokens.bodySmall, color = whiteBg)
                }
            }
        }
        return
    }

    val customerName = record.customerId?.name ?: record.customerCode ?: "Customer Legal Record"
    val statusText = record.status ?: "Pending"
    val isDisputed = !record.disputeReference.isNullOrBlank() || statusText.equals("Dispute", ignoreCase = true)
    val disputesCount = if (isDisputed) "1" else "0"

    // Resolve owner name using member list or fallback to record data
    val legalOwnerId = record.legalOwner?.id.orEmpty()
    val legalOwnerName = memberList.find { it._id == legalOwnerId }?.displayName()?.ifBlank { null }
        ?: record.legalOwner?.fullName?.takeIf { it != "-" }
        ?: "Sales"

    val headerData = DetailHeaderData(
        icon = Icons.Default.Description,
        badges = listOf(
            DetailBadge(
                text = statusText,
                textColor = if (statusText.equals("Active", true)) greentext else if (statusText.equals("Dispute", true)) redText else orangeText,
                bgColor = if (statusText.equals("Active", true)) greenBg else if (statusText.equals("Dispute", true)) redBg else orangeBg,
                showDot = true
            )
        ),
        title = customerName,
        metaItems = listOfNotNull(
            record.legalCustomerNumber ?: record.customerCode,
            record.legalMatterType,
            record.countryName
        ),
        secondaryActions = listOf(
            DetailAction(label = "Edit", icon = Icons.Default.Edit, onClick = { onEditRecord(record.id) }),
            DetailAction(label = "Download", icon = Icons.Default.Download, onClick = {
                val firstDoc = record.supportingDocuments?.firstOrNull()?.documentUrl
                if (!firstDoc.isNullOrBlank()) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, firstDoc.toUri()))
                } else {
                    Toast.makeText(context, "No documents available to download", Toast.LENGTH_SHORT).show()
                }
            }),
            DetailAction(label = "Share", icon = Icons.Default.Share, onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Legal Record: ${record.legalCustomerNumber}\nCustomer: $customerName")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Record"))
            })
        ),
        primaryAction = DetailAction(
            label = "" +
                    "" +
                    "Legal Action",
            icon = Icons.Default.Add,
            onClick = {
                isLegalActionMenuExpanded = true
            }
        )
    )

    val summaryStats = listOf(
        DetailStat(
            label = "Status",
            value = statusText,
            valueColor = if (statusText.equals("Active", true)) greentext else if (statusText.equals("Dispute", true)) redText else orangeText,
            showDot = true
        ),
        DetailStat(
            label = "Risk",
            value = record.riskLevel ?: "Low",
            valueColor = if (record.riskLevel.equals("High", true)) redText else Primary,
            showDot = true
        ),
        DetailStat(label = "Disputes", value = disputesCount),
        DetailStat(label = "Consent", value = record.consentType ?: "Signed", valueColor = greentext),
        DetailStat(label = "Owner", value = legalOwnerName)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        DetailScreenTemplate(
            title = "Customer Legal",
            onClose = onClose,
            header = headerData,
            summaryStats = summaryStats,
            tabs = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        ) { currentTab ->
            when (currentTab) {
                0 -> OverViewTab(record, disputesCount, legalOwnerName)
                1 -> TermsConsentTab(record)
                2 -> DocumentsTab(record)
                3 -> DisputesNotesTab(
                    record = record,
                    isDisputed = isDisputed,
                    onAddDispute = {
                        activeDialogState = LegalActionDialogState(
                            title = "Raise Dispute",
                            targetStatus = "Dispute",
                            confirmButtonText = "Raise Dispute",
                            isDisputeAction = true
                        )
                    }
                )
                4 -> HistoryTab(record, legalOwnerId, legalOwnerName, memberList)
            }
        }

        // ── "+ Legal Action" Dropdown Menu ──
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 180.dp, end = tokens.screenPadding)
        ) {
            DropdownMenu(
                expanded = isLegalActionMenuExpanded,
                onDismissRequest = { isLegalActionMenuExpanded = false },
                offset = DpOffset(0.dp, 0.dp),
                shape = RoundedCornerShape(10.dp),
                containerColor = whiteBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                if (statusText.equals("Dispute", ignoreCase = true)) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Resolve Dispute",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            isLegalActionMenuExpanded = false
                            activeDialogState = LegalActionDialogState(
                                title = "Resolve Dispute",
                                targetStatus = "Active",
                                confirmButtonText = "Resolve Dispute",
                                isDisputeAction = false
                            )
                        }
                    )
                } else {
                    if (!statusText.equals("Active", ignoreCase = true)) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Mark Active",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            },
                            onClick = {
                                isLegalActionMenuExpanded = false
                                activeDialogState = LegalActionDialogState(
                                    title = "Mark Active",
                                    targetStatus = "Active",
                                    confirmButtonText = "Mark Active",
                                    isDisputeAction = false
                                )
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Raise Dispute",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            isLegalActionMenuExpanded = false
                            activeDialogState = LegalActionDialogState(
                                title = "Raise Dispute",
                                targetStatus = "Dispute",
                                confirmButtonText = "Raise Dispute",
                                isDisputeAction = true
                            )
                        }
                    )
                }
            }
        }

        // ── Confirmation Status Dialog ──
        activeDialogState?.let { dialogConfig ->
            LegalStatusConfirmDialog(
                title = dialogConfig.title,
                currentStatus = statusText,
                targetStatus = dialogConfig.targetStatus,
                confirmButtonText = dialogConfig.confirmButtonText,
                isDisputeAction = dialogConfig.isDisputeAction,
                isLoading = isUpdatingStatus,
                onDismiss = { activeDialogState = null },
                onConfirm = {
                    viewModel.updateLegalCustomerStatus(
                        recordId = record.id,
                        status = dialogConfig.targetStatus,
                        onSuccess = {
                            activeDialogState = null
                            Toast.makeText(context, "Status updated to ${dialogConfig.targetStatus}", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err ?: "Failed to update status", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Confirmation Dialog Composable
// ─────────────────────────────────────────────────────────────

@Composable
private fun LegalStatusConfirmDialog(
    title: String,
    currentStatus: String,
    targetStatus: String,
    confirmButtonText: String,
    isDisputeAction: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val formattedCurrent = when (currentStatus.lowercase()) {
        "dispute" -> "Disputed"
        else -> currentStatus
    }
    val formattedTarget = when (targetStatus.lowercase()) {
        "dispute" -> "Disputed"
        else -> targetStatus
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = whiteBg,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isDisputeAction) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = redText,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = yellowText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable(enabled = !isLoading) { onDismiss() }
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Change the status from $formattedCurrent to $formattedTarget?",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = tokens.bodySmall.times(1.4f)
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDisputeAction) redText else yellowText,
                            disabledContainerColor = (if (isDisputeAction) redText else yellowText).copy(alpha = 0.5f)
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
// Tab 0: Overview
// ─────────────────────────────────────────────────────────────

@Composable
private fun OverViewTab(
    record: LegalCustomerItemDto,
    disputesCount: String,
    legalOwnerName: String
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Customer Legal Profile",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                DetailInfoRow(label = "Record ID", value = record.legalCustomerNumber ?: "-")
                DetailInfoRow(label = "Customer", value = record.customerId?.name ?: record.customerCode ?: "-")
                DetailInfoRow(label = "Customer Code", value = record.customerCode ?: record.customerId?.id ?: "-")
                DetailInfoRow(label = "Type", value = record.legalMatterType ?: "-")
                DetailInfoRow(label = "Country / Branch", value = listOfNotNull(record.countryName, record.branchId?.name).joinToString(" / ").ifBlank { "-" })
                DetailInfoRow(label = "Legal Owner", value = legalOwnerName)
                DetailBadgeRow(
                    label = "Status",
                    badge = DetailBadge(
                        text = record.status ?: "Pending",
                        textColor = if (record.status.equals("Active", true)) greentext else if (record.status.equals("Dispute", true)) redText else orangeText,
                        bgColor = if (record.status.equals("Active", true)) greenBg else if (record.status.equals("Dispute", true)) redBg else orangeBg
                    )
                )
                DetailBadgeRow(
                    label = "Risk Level",
                    badge = DetailBadge(
                        text = record.riskLevel ?: "Low",
                        textColor = Primary,
                        bgColor = primary_light
                    )
                )
                DetailInfoRow(label = "Priority", value = record.priority ?: "Low")
                DetailInfoRow(label = "Active Disputes", value = disputesCount)
            }
        }

        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Terms & Agreement Summary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                DetailInfoRow(label = "Agreement Type", value = record.termsType ?: "Standard")
                DetailBadgeRow(
                    label = "Consent Status",
                    badge = DetailBadge(
                        text = record.consentType ?: "Signed",
                        textColor = greentext,
                        bgColor = greenBg
                    )
                )
                DetailInfoRow(label = "Contract", value = record.contractId?.name ?: record.contractId?.id ?: "-")
                DetailInfoRow(label = "Order Reference", value = record.orderReference ?: "-")
                DetailInfoRow(label = "Invoice Reference", value = record.invoiceReference ?: "-")
                DetailInfoRow(label = "Effective Date", value = formatDisplayDate(record.effectiveDate))
                DetailInfoRow(label = "Expiry Date", value = formatDisplayDate(record.expiryDate))
                DetailInfoRow(label = "Last Review", value = formatDisplayDate(record.updatedAt))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 1: Terms & Consent
// ─────────────────────────────────────────────────────────────

@Composable
private fun TermsConsentTab(record: LegalCustomerItemDto) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val consentDocs = (record.consentDocuments ?: emptyList()).sortedByDescending { parseIsoDate(it.uploadedAt)?.time ?: 0L }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Agreement Terms",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Agreement Type", value = record.termsType ?: "-", modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Contract", value = record.contractId?.name ?: record.contractId?.id ?: "-", modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Consent Type", value = record.consentType ?: "-", modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Latest Update", value = formatDisplayDate(record.updatedAt), modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailStackedField(label = "Effective Date", value = formatDisplayDate(record.effectiveDate), modifier = Modifier.weight(1f))
                    DetailStackedField(label = "Expiry Date", value = formatDisplayDate(record.expiryDate), modifier = Modifier.weight(1f))
                }

                DetailStackedField(label = "Cancellation Terms", value = record.cancellationTerms ?: "-")
                DetailStackedField(label = "Refund Terms", value = record.refundTerms ?: "-")
            }
        }

        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Text(
                    text = "Consent Records",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                if (consentDocs.isEmpty()) {
                    Text(
                        text = "No consent documents uploaded yet.",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeGrey, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Consent Document", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(1.5f))
                        Text(text = "Status", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(text = "Date", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    Spacer(Modifier.height(6.dp))

                    consentDocs.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!doc.documentUrl.isNullOrBlank()) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, doc.documentUrl.toUri()))
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.documentName ?: "Consent Document",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.5f)
                            )

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                DetailPill(
                                    badge = DetailBadge(
                                        text = record.consentType ?: "Pending",
                                        textColor = greentext,
                                        bgColor = greenBg,
                                        showDot = false
                                    )
                                )
                            }

                            Text(
                                text = formatDisplayDate(doc.uploadedAt),
                                fontSize = tokens.caption,
                                color = headerGrey,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = light_grey, thickness = 1.dp)
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Click a document to view it. Upload more from the Documents tab.",
                        fontSize = tokens.label,
                        color = mutedText
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: Documents
// ─────────────────────────────────────────────────────────────

@Composable
private fun DocumentsTab(record: LegalCustomerItemDto) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val rows = remember(record) {
        val list = mutableListOf<Triple<String, String, LegalCustomerDocumentDto>>()
        record.supportingDocuments.orEmpty().forEach { list.add(Triple(it.documentName ?: "Supporting Document", "Supporting", it)) }
        record.consentDocuments.orEmpty().forEach { list.add(Triple(it.documentName ?: "Consent Document", "Consent", it)) }
        record.legalCorrespondence.orEmpty().forEach { list.add(Triple(it.documentName ?: "Legal Correspondence", "Correspondence", it)) }
        list
    }

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
        DetailCard {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Legal Documents",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )

                    Button(
                        onClick = {
                            Toast.makeText(context, "Upload from Edit screen", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "Upload", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)

                if (rows.isEmpty()) {
                    Text(
                        text = "No documents uploaded yet.",
                        fontSize = tokens.bodySmall,
                        color = mutedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeGrey, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Document", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(1.6f))
                        Text(text = "Type", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(0.9f))
                        Text(text = "Date", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(0.9f))
                        Text(text = "Actions", fontSize = tokens.label, color = headerGrey, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    Spacer(Modifier.height(4.dp))

                    rows.forEach { (docName, category, docDto) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = docName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.6f)
                            )

                            Text(
                                text = category,
                                fontSize = tokens.caption,
                                color = headerGrey,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(0.9f)
                            )

                            Text(
                                text = formatDisplayDate(docDto.uploadedAt),
                                fontSize = tokens.caption,
                                color = headerGrey,
                                modifier = Modifier.weight(0.9f)
                            )

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (!docDto.documentUrl.isNullOrBlank()) {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, docDto.documentUrl.toUri()))
                                            } else {
                                                Toast.makeText(context, "URL not available", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Preview",
                                        tint = headerGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (!docDto.documentUrl.isNullOrBlank()) {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, docDto.documentUrl.toUri()))
                                            } else {
                                                Toast.makeText(context, "URL not available", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = headerGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = light_grey, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Disputes & Legal Notices
// ─────────────────────────────────────────────────────────────

@Composable
private fun DisputesNotesTab(
    record: LegalCustomerItemDto,
    isDisputed: Boolean,
    onAddDispute: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val correspondence = record.legalCorrespondence.orEmpty()

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Disputes & Legal Notices",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                if (!isDisputed) {
                    Button(
                        onClick = onAddDispute,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "Add Dispute", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            if (!isDisputed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No active disputes or notices for this customer",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "This customer is currently in good standing. Any registered litigation, arbitration, or regulatory dispute notifications will populate here.",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                        .background(redBg.copy(alpha = 0.5f))
                        .border(1.dp, redText.copy(alpha = 0.25f), RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "DISPUTE REFERENCE", fontSize = tokens.label, color = headerGrey)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = record.disputeReference ?: "Not linked yet (add it from Edit)",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        DetailPill(
                            badge = DetailBadge(
                                text = record.status ?: "Dispute",
                                textColor = redText,
                                bgColor = whiteBg,
                                showDot = true
                            )
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Text(text = "LEGAL NOTES", fontSize = tokens.label, color = headerGrey)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = record.legalDescription?.ifBlank { "No notes added." } ?: "No notes added.",
                    fontSize = tokens.bodySmall,
                    color = TextPrimary
                )

                if (correspondence.isNotEmpty()) {
                    Spacer(Modifier.height(tokens.extraPadding))
                    Text(text = "LEGAL CORRESPONDENCE", fontSize = tokens.label, color = headerGrey)
                    Spacer(Modifier.height(6.dp))

                    correspondence.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!doc.documentUrl.isNullOrBlank()) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, doc.documentUrl.toUri()))
                                    }
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.documentName ?: "Correspondence file",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatDisplayDate(doc.uploadedAt),
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

// ─────────────────────────────────────────────────────────────
// Tab 4: History
// ─────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(
    record: LegalCustomerItemDto,
    legalOwnerId: String,
    legalOwnerName: String,
    memberList: List<MemberItem>
) {
    val tokens = LocalAppTokens.current

    val timelineItems = remember(record, memberList, legalOwnerName) {
        val list = record.activities.orEmpty().sortedByDescending { parseIsoDate(it.performedAt)?.time ?: 0L }
        if (list.isNotEmpty()) {
            list.map { activity ->
                // Check if performedBy ID matches member list or legalOwner ID
                val performerId = activity.performedBy?.id.orEmpty()
                val performerName = memberList.find { it._id == performerId }?.displayName()?.ifBlank { null }
                    ?: (if (performerId.isNotBlank() && performerId == legalOwnerId) legalOwnerName else null)
                    ?: activity.performedBy?.fullName?.takeIf { it != "-" }
                    ?: "System"

                DetailActivityItem(
                    date = formatDisplayDate(activity.performedAt),
                    time = "",
                    title = when (activity.activityType) {
                        "Created" -> "Record created"
                        "Updated" -> "Record updated"
                        "Status Changed" -> "Status changed"
                        "Deleted" -> "Record deleted"
                        else -> activity.activityType ?: "Activity"
                    },
                    subtitle = listOfNotNull(activity.description, performerName).joinToString(" • By: ")
                )
            }
        } else {
            listOf(
                DetailActivityItem(
                    date = formatDisplayDate(record.createdAt),
                    time = "",
                    title = "Record created",
                    subtitle = "Initial record created • By: $legalOwnerName"
                )
            )
        }
    }

    DetailCard {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Customer Legal Activity",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            DetailActivityTimeline(timelineItems)
        }
    }
}