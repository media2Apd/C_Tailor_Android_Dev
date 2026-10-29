@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.expense_management.approval

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.ExpenseClaimAttachmentDto
import com.cuso.tailor.model.hr.ExpenseClaimDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExpenseClaimDetailScreen(
    claimId: String,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onWithdrawClaim: () -> Unit = {},
    onDownloadSlip: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val claimDetail by viewModel.expenseClaimDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaimDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingClaimStatus.collectAsStateWithLifecycle()

    var decisionNote by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(claimId) {
        if (claimId.isNotBlank()) {
            viewModel.fetchExpenseClaimDetail(claimId)
        }
    }

    fun handleAction(targetStatus: String) {
        if (decisionNote.isBlank()) {
            errorMessage = "Please enter your comments or reasons for decision"
            return
        }
        viewModel.updateExpenseClaimStatus(
            claimId = claimId,
            status = targetStatus,
            comment = decisionNote,
            onSuccess = { msg ->
                successMessage = msg
                decisionNote = ""
            },
            onError = { err ->
                errorMessage = err
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Expense Details",
                    onClose = {
                        viewModel.clearExpenseClaimDetail()
                        onClose()
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (claimDetail == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense details not found",
                        fontSize = tokens.bodyMedium,
                        color = mutedText
                    )
                }
            } else {
                val claim = claimDetail!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(PanelBg)
                        .verticalScroll(scrollState)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // ── 1. Top Identity Details Card ──
                    IdentityInfoCard(claim = claim)

                    // ── 2. Amount Breakdown Card ──
                    AmountBreakdownCard(claim = claim)

                    // ── 3. Attachments Card ──
                    AttachmentsCard(attachments = claim.attachments)

                    // ── 4. Approval History Card (Scrollable Table) ──
                    ApprovalHistoryCard(claim = claim)

                    // ── 5. Policy Notice Banner ──
                    PolicyNoticeCard(categoryName = claim.category?.name ?: "Expense")

                    // ── 6. Approval Actions Section ──
                    ApprovalActionsCard(
                        decisionNote = decisionNote,
                        onDecisionNoteChange = { decisionNote = it },
                        submittedByName = claim.employee?.fullName ?: "Employee",
                        isActionLoading = isUpdatingStatus,
                        onApprove = { handleAction("Approved") },
                        onReject = { handleAction("Rejected") },
                        onClarify = { handleAction("Clarification_Requested") },
                        onLog = { }
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun IdentityInfoCard(claim: ExpenseClaimDto) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.title.orEmpty().ifBlank { "Untitled" },
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Ref ID: ${claim.claimNumber.orEmpty().ifBlank { "EXP-${claim.id.takeLast(6).uppercase()}" }}",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }

                StatusBadge(
                    text = claim.status?.replace("_", " ") ?: "",
                    textColor = Primary,
                    bgColor = activity_purple_bg
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = grey_border, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SUBMISSION DATE", fontSize = tokens.label, color = headerGrey)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatHeaderDate(claim.submittedAt ?: claim.createdAt),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("CATEGORY", fontSize = tokens.label, color = headerGrey)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = claim.category?.name ?: "General",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DEPARTMENT", fontSize = tokens.label, color = headerGrey)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "—",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("SUBMITTED BY", fontSize = tokens.label, color = headerGrey)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = claim.employee?.fullName ?: "N/A",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            if (!claim.description.isNullOrBlank()) {
                Spacer(Modifier.height(tokens.extraPadding))
                Text(
                    text = claim.description,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AmountBreakdownCard(claim: ExpenseClaimDto) {
    val tokens = LocalAppTokens.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AMOUNT BREAKDOWN",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(activity_purple_bg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "INR (${currencySymbol()})",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal Amount", fontSize = tokens.bodySmall, color = TextSecondary)
                Text(currencyFormatter.format(claim.amount), fontSize = tokens.bodySmall, color = TextPrimary)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tax (${calculateTaxPercent(claim.amount, claim.taxAmount ?: 0.0)}%)", fontSize = tokens.bodySmall, color = TextSecondary)
                Text(currencyFormatter.format(claim.taxAmount ?: 0.0), fontSize = tokens.bodySmall, color = TextPrimary)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = grey_border, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL CLAIM", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text("✓ Inclusive of taxes", fontSize = tokens.label, color = greentext)
                }

                Text(
                    text = currencyFormatter.format(claim.totalAmount),
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun AttachmentsCard(attachments: List<ExpenseClaimAttachmentDto>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ATTACHMENTS",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(light_grey),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = attachments.size.toString(),
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                    }
                }

                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Download All",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            attachments.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                        .background(whiteBg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(light_grey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = file.fileName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${file.fileType ?: "PNG"} • ${formatFileSize(file.fileSize)}",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                        Text(
                            text = "Verified Upload",
                            fontSize = tokens.label,
                            color = greentext
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download",
                        tint = headerGrey,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ApprovalHistoryCard(claim: ExpenseClaimDto) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Approval History",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
            colors = CardDefaults.cardColors(containerColor = whiteBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Approver", fontSize = tokens.bodySmall, color = TextSecondary, modifier = Modifier.width(130.dp))
                    Text("Date", fontSize = tokens.bodySmall, color = TextSecondary, modifier = Modifier.width(110.dp))
                    Text("Status", fontSize = tokens.bodySmall, color = TextSecondary, modifier = Modifier.width(100.dp))
                    Text("Comments", fontSize = tokens.bodySmall, color = TextSecondary, modifier = Modifier.width(180.dp))
                }

                HorizontalDivider(color = grey_border, thickness = 1.dp)

                // Table Content Row
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = claim.employee?.fullName ?: "Vicky Sakthi",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.width(130.dp)
                    )
                    Text(
                        text = formatTimelineDate(claim.submittedAt ?: claim.createdAt),
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.width(110.dp)
                    )
                    Box(modifier = Modifier.width(100.dp)) {
                        StatusBadge(
                            text = "Submitted",
                            bgColor = greenBg,
                            textColor = greentext,
                            showDot = false,
                            cornerRadius = 6.dp
                        )
                    }
                    Text(
                        text = "Claim submitted for approval.",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.width(180.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyNoticeCard(categoryName: String) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        colors = CardDefaults.cardColors(containerColor = PanelBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Claims for $categoryName must comply with section 4.2 of the Corporate Expense Policy. For queries, contact HR Helpdesk.",
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ApprovalActionsCard(
    decisionNote: String,
    onDecisionNoteChange: (String) -> Unit,
    submittedByName: String,
    isActionLoading: Boolean = false,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClarify: () -> Unit,
    onLog: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text(
                text = "Approval Actions",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            FormLabel("Decision Note")
            FormTextArea(
                value = decisionNote,
                onValueChange = onDecisionNoteChange,
                placeholder = "Enter your comments or reasons for decision...",
                minLines = 3,
                maxLines = 4
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onApprove,
                enabled = !isActionLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Approve Claim",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReject,
                enabled = !isActionLoading,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Text(
                    text = "Reject & Notify",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(14.dp))
//            HorizontalDivider(color = grey_border, thickness = 1.dp)
//            Spacer(Modifier.height(14.dp))
//
//            Text("Quick Actions", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
//            Spacer(Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                // Clarify
//                Card(
//                    modifier = Modifier
//                        .weight(1f)
//                        .clickable(enabled = !isActionLoading) { onClarify() },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = CardDefaults.cardColors(containerColor = PanelBg)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 12.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(Icons.Outlined.Mail, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
//                        Spacer(Modifier.height(4.dp))
//                        Text("CLARIFY", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = TextPrimary)
//                    }
//                }
//
//                // Log
//                Card(
//                    modifier = Modifier
//                        .weight(1f)
//                        .clickable(enabled = !isActionLoading) { onLog() },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = CardDefaults.cardColors(containerColor = PanelBg)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 12.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(Icons.Outlined.History, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
//                        Spacer(Modifier.height(4.dp))
//                        Text("LOG", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = TextPrimary)
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(6.dp))
//            Text(
//                text = "Clarify sends your note to $submittedByName and pauses the claim.",
//                fontSize = tokens.caption,
//                color = headerGrey
//            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FORMATTING UTILITIES
// ─────────────────────────────────────────────────────────────

private fun calculateTaxPercent(amount: Double, tax: Double): Int {
    if (amount <= 0) return 0
    return ((tax / amount) * 100).toInt()
}

private fun formatHeaderDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val d = input.parse(isoDate)
        if (d != null) output.format(d) else isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatTimelineDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val d = input.parse(isoDate)
        if (d != null) output.format(d) else isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatFileSize(size: Long?): String {
    if (size == null || size <= 0) return "420 KB"
    return "${size / 1024} KB"
}

@Composable
fun ExpenseClaimDetailScreen(
    claimId: String,
    title: String = "Expense Details",
    showApprovalActions: Boolean = true,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onWithdrawClaim: () -> Unit = {},
    onDownloadSlip: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val claimDetail by viewModel.expenseClaimDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaimDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingClaimStatus.collectAsStateWithLifecycle()

    var decisionNote by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(claimId) {
        if (claimId.isNotBlank()) {
            viewModel.fetchExpenseClaimDetail(claimId)
        }
    }

    fun handleAction(targetStatus: String) {
        if (decisionNote.isBlank()) {
            errorMessage = "Please enter your comments or reasons for decision"
            return
        }
        viewModel.updateExpenseClaimStatus(
            claimId = claimId,
            status = targetStatus,
            comment = decisionNote,
            onSuccess = { msg ->
                successMessage = msg
                decisionNote = ""
            },
            onError = { err ->
                errorMessage = err
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = title,
                    onClose = {
                        viewModel.clearExpenseClaimDetail()
                        onClose()
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                ListSkeleton()
            } else if (claimDetail == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense details not found",
                        fontSize = tokens.bodyMedium,
                        color = mutedText
                    )
                }
            } else {
                val claim = claimDetail!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(PanelBg)
                        .verticalScroll(scrollState)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // Identity details card
                    IdentityInfoCard(claim = claim)

                    // Breakdown of amount
                    AmountBreakdownCard(claim = claim)

                    // Attachments list
                    AttachmentsCard(attachments = claim.attachments)

                    // Approval History log
                    ApprovalHistoryCard(claim = claim)

                    // Policy notice
                    PolicyNoticeCard(categoryName = claim.category?.name ?: "Expense")

                    // Render approval actions only when enabled
                    if (showApprovalActions) {
                        ApprovalActionsCard(
                            decisionNote = decisionNote,
                            onDecisionNoteChange = { decisionNote = it },
                            submittedByName = claim.employee?.fullName ?: "Employee",
                            isActionLoading = isUpdatingStatus,
                            onApprove = { handleAction("Approved") },
                            onReject = { handleAction("Rejected") },
                            onClarify = { handleAction("Clarification_Requested") },
                            onLog = { }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NewClaimDetailScreen(
    claimId: String,
    showApprovalActions: Boolean = true,
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onWithdrawClaim: () -> Unit = {},
    onDownloadSlip: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()

    val claimDetail by viewModel.expenseClaimDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingExpenseClaimDetail.collectAsStateWithLifecycle()
    val isUpdatingStatus by viewModel.isUpdatingClaimStatus.collectAsStateWithLifecycle()

    var decisionNote by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(claimId) {
        if (claimId.isNotBlank()) {
            viewModel.fetchExpenseClaimDetail(claimId)
        }
    }

    fun handleAction(targetStatus: String) {
        if (decisionNote.isBlank()) {
            errorMessage = "Please enter your comments or reasons for decision"
            return
        }
        viewModel.updateExpenseClaimStatus(
            claimId = claimId,
            status = targetStatus,
            comment = decisionNote,
            onSuccess = { msg ->
                successMessage = msg
                decisionNote = ""
            },
            onError = { err ->
                errorMessage = err
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "New Claim Details",
                    onClose = {
                        viewModel.clearExpenseClaimDetail()
                        onClose()
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                ListSkeleton()
            } else if (claimDetail == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense details not found",
                        fontSize = tokens.bodyMedium,
                        color = mutedText
                    )
                }
            } else {
                val claim = claimDetail!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(PanelBg)
                        .verticalScroll(scrollState)
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    IdentityInfoCard(claim = claim)
                    AmountBreakdownCard(claim = claim)
                    AttachmentsCard(attachments = claim.attachments)
                    ApprovalHistoryCard(claim = claim)
                    PolicyNoticeCard(categoryName = claim.category?.name ?: "Expense")

                    // Only show approval decision controls when permitted
                    if (showApprovalActions) {
                        ApprovalActionsCard(
                            decisionNote = decisionNote,
                            onDecisionNoteChange = { decisionNote = it },
                            submittedByName = claim.employee?.fullName ?: "Employee",
                            isActionLoading = isUpdatingStatus,
                            onApprove = { handleAction("Approved") },
                            onReject = { handleAction("Rejected") },
                            onClarify = { handleAction("Clarification_Requested") },
                            onLog = { }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}