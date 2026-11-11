@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.security.support.support_ticket

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.SecurityViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri
import kotlin.math.ln
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SupportTicketDetailScreen(
    ticketId: String,
    viewModel: SecurityViewModel,
    hrViewModel: HrViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val ticket by viewModel.selectedSupportTicket.collectAsState()
    val isLoading by viewModel.isTicketLoading.collectAsState()
    val employees by hrViewModel.members.collectAsState()

    var showAssignModal by remember { mutableStateOf(false) }
    var showStatusModal by remember { mutableStateOf(false) }
    var showPriorityModal by remember { mutableStateOf(false) }
    var showResolveModal by remember { mutableStateOf(false) }

    var replyText by remember { mutableStateOf("") }
    val replyAttachments = remember { mutableStateListOf<Uri>() }
    var isSendingReply by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (replyAttachments.size < 5) replyAttachments.add(uri)
        }
    }

    LaunchedEffect(ticketId) {
        viewModel.fetchSupportTicketById(ticketId)
        hrViewModel.fetchMembers()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = ticket?.ticketNumber ?: "Support Ticket",
                subtitle = ticket?.subject,
                onClose = onBack
            )
        }
    ) { innerPadding ->
        if (isLoading || ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val t = ticket!!
            val isClosed = t.status.equals("Resolved", ignoreCase = true) ||
                    t.status.equals("Closed", ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // Section 1: Header Overview & Quick Actions
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = t.issueType,
                                bgColor = primary_light,
                                textColor = Primary
                            )

                            val (priorityBg, priorityText) = when (t.priority.lowercase()) {
                                "critical", "urgent", "high" -> redBg to redText
                                "medium" -> yellowBg to yellowText
                                else -> greenBg to greentext
                            }
                            StatusBadge(
                                text = "${t.priority} Priority",
                                bgColor = priorityBg,
                                textColor = priorityText
                            )

                            val (statusBg, statusText) = when (t.status.lowercase()) {
                                "resolved", "closed" -> greenBg to greentext
                                "open" -> primary_light to Primary
                                else -> yellowBg to yellowText
                            }
                            StatusBadge(
                                text = t.status,
                                bgColor = statusBg,
                                textColor = statusText
                            )

                            val slaStatus = t.sla?.status ?: if (t.sla?.isEscalated == true) "At Risk" else "Ok"
                            val (slaBg, slaText) = if (slaStatus.equals("Ok", ignoreCase = true)) {
                                greenBg to greentext
                            } else {
                                redBg to redText
                            }
                            StatusBadge(
                                text = "SLA: $slaStatus",
                                bgColor = slaBg,
                                textColor = slaText
                            )
                        }

                        // Management Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAssignModal = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                border = BorderStroke(1.dp, sectionBorder),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                            ) {
                                Text(
                                    text = "Assign",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { showStatusModal = true },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                border = BorderStroke(1.dp, sectionBorder),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                            ) {
                                Text(
                                    text = "Change Status",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { showPriorityModal = true },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                border = BorderStroke(1.dp, sectionBorder),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                            ) {
                                Text(
                                    text = "Change Priority",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // Section 2: SLA Information (Calculated from API)
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val slaStatus = t.sla?.status ?: if (t.sla?.isEscalated == true) "At Risk" else "Ok"
                        val (slaPillBg, slaPillText) = if (slaStatus.equals("Ok", ignoreCase = true)) {
                            greenBg to greentext
                        } else {
                            redBg to redText
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SLA INFORMATION",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            StatusBadge(
                                text = slaStatus,
                                bgColor = slaPillBg,
                                textColor = slaPillText
                            )
                        }

                        // Target and Actual Times calculated from API
                        val firstResponseTargetStr = formatDurationBetween(t.createdAt, t.sla?.firstResponseTarget)
                        val firstResponseActualStr = t.sla?.firstResponseActual?.let {
                            formatDurationBetween(t.createdAt, it)
                        } ?: "Pending"
                        val resolutionTargetStr = formatDurationBetween(t.createdAt, t.sla?.resolutionTarget)
                        val timeRemainingStr = if (isClosed) "Resolved" else t.sla?.timeRemaining ?: "—"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SlaMetricBox(
                                modifier = Modifier.weight(1f),
                                label = "FIRST RESPONSE TARGET",
                                value = firstResponseTargetStr,
                                valueColor = TextPrimary
                            )
                            SlaMetricBox(
                                modifier = Modifier.weight(1f),
                                label = "FIRST RESPONSE",
                                value = firstResponseActualStr,
                                valueColor = if (t.sla?.firstResponseActual != null) greentext else TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SlaMetricBox(
                                modifier = Modifier.weight(1f),
                                label = "RESOLUTION TARGET",
                                value = resolutionTargetStr,
                                valueColor = TextPrimary
                            )
                            SlaMetricBox(
                                modifier = Modifier.weight(1f),
                                label = "TIME REMAINING",
                                value = timeRemainingStr,
                                valueColor = if (isClosed) TextSecondary else redText
                            )
                        }

                        HorizontalDivider(color = grey_border, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESCALATION",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (t.sla?.isEscalated == true) "Escalated" else "Not Escalated",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (t.sla?.isEscalated == true) redText else greentext
                            )
                        }
                    }
                }

                // ==========================================
                // Section 3: Ticket Information Grid
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TICKET INFORMATION",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        DetailPropertyRow(label = "Ticket ID", value = t.ticketNumber)
                        DetailPropertyRow(label = "Issue Type", value = t.issueType)
                        DetailPropertyRow(label = "Priority", value = t.priority, valueColor = redText)
                        DetailPropertyRow(label = "Status", value = t.status, valueColor = Primary)
                        DetailPropertyRow(label = "Affected Area", value = t.affectedArea)
                        DetailPropertyRow(label = "Created By", value = formatMemberName(t.createdBy))
                        DetailPropertyRow(label = "Created Date", value = formatIsoTimestamp(t.createdAt))
                    }
                }

                // ==========================================
                // Section 4: Requester & Impact
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "REQUESTER & IMPACT",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        val requesterName = formatMemberName(t.affectedUserId).ifBlank { "Unspecified User" }

                        // User Summary Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                                .background(light_grey)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = requesterName.take(2).uppercase(),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }

                            Column {
                                Text(
                                    text = requesterName,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = t.contactEmail ?: "No email provided",
                                    fontSize = tokens.caption,
                                    color = TextSecondary
                                )
                            }
                        }

                        DetailPropertyRow(
                            label = "Branch / Location",
                            value = formatBranchName(t.branchId, employees)
                        )
                        DetailPropertyRow(
                            label = "Contact Preference",
                            value = t.contactPreference
                        )
                        DetailPropertyRow(
                            label = "Affected Device",
                            value = t.affectedDevice ?: "—"
                        )
                    }
                }

                // ==========================================
                // Section 5: Current Assignment
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ASSIGNMENT",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        DetailPropertyRow(
                            label = "Current Support Team",
                            value = t.assignedTeam ?: "Unassigned"
                        )

                        val assigneeName = formatMemberName(t.assignedTo)
                        DetailPropertyRow(
                            label = "Assigned Specialist",
                            value = if (assigneeName.isNotBlank() && assigneeName != "—") assigneeName else "Not assigned yet"
                        )

                        if (!t.assignmentNote.isNullOrBlank()) {
                            DetailPropertyRow(
                                label = "Assignment Note",
                                value = t.assignmentNote
                            )
                        }

                        Button(
                            onClick = { showAssignModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight * 0.9f)
                        ) {
                            Text(
                                text = "Reassign Ticket",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                }

                // ==========================================
                // Section 6: Issue Description
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ISSUE DESCRIPTION",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Text(
                            text = "Subject: ${t.subject}",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(light_grey)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = t.description,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                lineHeight = tokens.bodyMedium.times(1.4f)
                            )
                        }
                    }
                }

                // ==========================================
                // Section 7: Attachments List
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ATTACHMENTS (${t.attachments.size})",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        if (t.attachments.isEmpty()) {
                            Text(
                                text = "No attachments uploaded for this ticket.",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        } else {
                            t.attachments.forEach { file ->
                                AttachmentItemRow(
                                    filename = file.filename,
                                    fileSize = file.size,
                                    fileUrl = file.url,
                                    onOpenUrl = { url ->
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                                url.toUri()))
                                        } catch (_: Exception) { }
                                    }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // Section 8: Live Conversation Thread
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CONVERSATION (${t.conversation.size})",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        if (t.conversation.isEmpty()) {
                            Text(
                                text = "No messages yet. Start the conversation below.",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            t.conversation.forEach { msg ->
                                val isSupport = msg.senderType.equals("support", ignoreCase = true)
                                val senderDisplayName = formatMemberName(msg.senderId).ifBlank {
                                    if (isSupport) "Support Team" else "Requester"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isSupport) Arrangement.End else Arrangement.Start
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.85f),
                                        horizontalAlignment = if (isSupport) Alignment.End else Alignment.Start
                                    ) {
                                        Text(
                                            text = "$senderDisplayName • ${formatIsoTimestamp(msg.createdAt)}",
                                            fontSize = tokens.caption,
                                            color = headerGrey,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                                .background(if (isSupport) Primary else light_grey)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = msg.message,
                                                fontSize = tokens.bodySmall,
                                                color = if (isSupport) whiteBg else TextPrimary
                                            )
                                        }

                                        // Message file attachments if present
                                        if (msg.attachments.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier.padding(top = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                msg.attachments.forEach { file ->
                                                    AttachmentItemRow(
                                                        filename = file.filename,
                                                        fileSize = file.size,
                                                        fileUrl = file.url,
                                                        onOpenUrl = { url ->
                                                            try {
                                                                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                                                            } catch (_: Exception) { }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Attachments to send with the next reply
                        if (replyAttachments.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                replyAttachments.forEach { uri ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                            .background(light_grey)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = uri.lastPathSegment ?: "File",
                                                fontSize = tokens.caption,
                                                color = TextPrimary,
                                                maxLines = 1
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = TextSecondary,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { replyAttachments.remove(uri) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reply Form Text Area
                        FormTextArea(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = "Type your response here...",
                            minLines = 3,
                            maxLines = 5
                        )

                        // Attach Files and Send Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { filePicker.launch("*/*") },
                                enabled = !isSendingReply && replyAttachments.size < 5
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Attach Files",
                                    fontSize = tokens.bodySmall,
                                    color = Primary
                                )
                            }

                            Button(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        isSendingReply = true
                                        val parts = replyAttachments.mapNotNull {
                                            uriToMultipartPart(context, it, "attachments")
                                        }
                                        viewModel.replySupportTicket(t.id, replyText, parts) {
                                            replyText = ""
                                            replyAttachments.clear()
                                            isSendingReply = false
                                        }
                                    }
                                },
                                enabled = replyText.isNotBlank() && !isSendingReply,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                modifier = Modifier.height(tokens.buttonHeight * 0.9f)
                            ) {
                                Text(
                                    text = if (isSendingReply) "Sending..." else "Send Reply",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // Section 9: Activity Timeline
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ACTIVITY HISTORY",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        if (t.activityHistory.isEmpty()) {
                            Text(
                                text = "No recorded activity yet.",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        } else {
                            t.activityHistory.forEachIndexed { index, act ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primary_light),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = resolveActivityIcon(act.action),
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = act.action,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        val byText = act.performedByName ?: formatMemberName(act.performedBy)
                                        val metaLine = listOfNotNull(
                                            byText.takeIf { it.isNotBlank() }?.let { "by $it" },
                                            act.createdAt?.let { formatIsoTimestamp(it) }
                                        ).joinToString(" • ")

                                        if (metaLine.isNotBlank()) {
                                            Text(
                                                text = metaLine,
                                                fontSize = tokens.caption,
                                                color = headerGrey
                                            )
                                        }

                                        if (!act.note.isNullOrBlank()) {
                                            Text(
                                                text = act.note,
                                                fontSize = tokens.caption,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < t.activityHistory.lastIndex) {
                                    HorizontalDivider(
                                        color = grey_border,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(start = 38.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // Section 10: Ticket Resolution Status
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "RESOLUTION",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )

                        DetailPropertyRow(
                            label = "Current Status",
                            value = t.resolution?.status ?: if (isClosed) t.status else "Pending",
                            valueColor = if (isClosed) greentext else yellowText
                        )

                        if (isClosed) {
                            if (!t.resolution?.resolvedAt.isNullOrBlank()) {
                                DetailPropertyRow(
                                    label = "Resolved At",
                                    value = formatIsoTimestamp(t.resolution.resolvedAt)
                                )
                            }
                            if (!t.resolution?.resolutionNote.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                        .background(greenBg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = t.resolution.resolutionNote,
                                        fontSize = tokens.bodySmall,
                                        color = greentext
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { showResolveModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.buttonHeight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Resolve Ticket",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // Modal 1: Assign Support Ticket Modal
            // ==========================================
            if (showAssignModal) {
                var selectedTeam by remember { mutableStateOf(t.assignedTeam ?: "IT Support") }
                var selectedAssignee by remember { mutableStateOf(resolveMemberId(t.assignedTo) ?: "") }
                var assignNote by remember { mutableStateOf("") }
                var teamExpanded by remember { mutableStateOf(false) }
                var userExpanded by remember { mutableStateOf(false) }

                TicketActionModal(
                    title = "Assign Support Ticket",
                    onDismiss = { showAssignModal = false },
                    onConfirm = {
                        viewModel.assignSupportTicket(t.id, selectedTeam, selectedAssignee, assignNote) {
                            showAssignModal = false
                        }
                    },
                    confirmText = "Assign Ticket"
                ) {
                    FormDropdown(
                        label = "SUPPORT TEAM",
                        value = selectedTeam,
                        expanded = teamExpanded,
                        onExpandChange = { teamExpanded = it },
                        options = listOf("IT Support", "Security Support", "Billing Support"),
                        onOptionSelected = { selectedTeam = it }
                    )

                    FormDropdown(
                        label = "ASSIGNED TO",
                        value = employees.find { it._id == selectedAssignee }?.let { "${it.firstName} ${it.lastName}".trim() } ?: "Select employee",
                        expanded = userExpanded,
                        onExpandChange = { userExpanded = it },
                        options = employees.map { "${it.firstName} ${it.lastName}".trim() },
                        onOptionSelected = { name ->
                            selectedAssignee = employees.find { "${it.firstName} ${it.lastName}".trim() == name }?._id.orEmpty()
                        }
                    )

                    FormLabel("ASSIGNMENT NOTE")
                    FormTextArea(
                        value = assignNote,
                        onValueChange = { assignNote = it },
                        placeholder = "Add instructions or context regarding this assignment...",
                        minLines = 3
                    )
                }
            }

            // ==========================================
            // Modal 2: Change Ticket Status Modal
            // ==========================================
            if (showStatusModal) {
                var newStatus by remember { mutableStateOf(t.status) }
                var statusReason by remember { mutableStateOf("") }
                var statusExpanded by remember { mutableStateOf(false) }

                TicketActionModal(
                    title = "Change Ticket Status",
                    onDismiss = { showStatusModal = false },
                    onConfirm = {
                        viewModel.changeSupportTicketStatus(t.id, newStatus, statusReason) {
                            showStatusModal = false
                        }
                    },
                    confirmText = "Update Status"
                ) {
                    Text(
                        text = "Current Status: ${t.status}",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )

                    FormDropdown(
                        label = "NEW STATUS",
                        value = newStatus,
                        expanded = statusExpanded,
                        onExpandChange = { statusExpanded = it },
                        options = listOf("Open", "In Progress", "Waiting", "Resolved", "Closed"),
                        onOptionSelected = { newStatus = it }
                    )

                    FormLabel("REASON / NOTE")
                    FormTextArea(
                        value = statusReason,
                        onValueChange = { statusReason = it },
                        placeholder = "Specify the reason for updating the ticket status...",
                        minLines = 3
                    )
                }
            }

            // ==========================================
            // Modal 3: Change Ticket Priority Modal
            // ==========================================
            if (showPriorityModal) {
                var newPriority by remember { mutableStateOf(t.priority) }
                var priorityReason by remember { mutableStateOf("") }
                var priorityExpanded by remember { mutableStateOf(false) }

                TicketActionModal(
                    title = "Change Ticket Priority",
                    onDismiss = { showPriorityModal = false },
                    onConfirm = {
                        viewModel.changeSupportTicketPriority(t.id, newPriority, priorityReason) {
                            showPriorityModal = false
                        }
                    },
                    confirmText = "Save Priority"
                ) {
                    Text(
                        text = "Current Priority: ${t.priority}",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )

                    FormDropdown(
                        label = "NEW PRIORITY",
                        value = newPriority,
                        expanded = priorityExpanded,
                        onExpandChange = { priorityExpanded = it },
                        options = listOf("Low", "Medium", "High", "Critical"),
                        onOptionSelected = { newPriority = it }
                    )

                    FormLabel("REASON FOR CHANGE")
                    FormTextArea(
                        value = priorityReason,
                        onValueChange = { priorityReason = it },
                        placeholder = "Explain why this ticket's urgency level is modified...",
                        minLines = 3
                    )
                }
            }

            // ==========================================
            // Modal 4: Resolve Ticket Modal
            // ==========================================
            if (showResolveModal) {
                var note by remember { mutableStateOf("") }

                TicketActionModal(
                    title = "Resolve Ticket",
                    onDismiss = { showResolveModal = false },
                    onConfirm = {
                        viewModel.resolveSupportTicket(t.id, note.trim()) {
                            showResolveModal = false
                            onBack()
                        }
                    },
                    confirmText = "Confirm Resolve"
                ) {
                    Text(
                        text = "This marks the ticket as resolved and stops the SLA clock. Continue?",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )

                    FormLabel("RESOLUTION NOTE", isRequired = true)
                    FormTextArea(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Summarize how the issue was resolved...",
                        minLines = 3
                    )
                }
            }
        }
    }
}

// ==========================================
// Reusable UI Subcomponents
// ==========================================

@Composable
private fun DetailPropertyRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun SlaMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(light_grey)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                fontSize = tokens.caption,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun AttachmentItemRow(
    filename: String,
    fileSize: Long?,
    fileUrl: String?,
    onOpenUrl: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(light_grey)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Column {
                Text(
                    text = filename,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                if (fileSize != null && fileSize > 0) {
                    Text(
                        text = formatBytes(fileSize),
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
            }
        }

        if (!fileUrl.isNullOrBlank()) {
            IconButton(
                onClick = { onOpenUrl(fileUrl) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open",
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TicketActionModal(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Save",
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                HorizontalDivider(color = grey_border, thickness = 1.dp)

                content()

                HorizontalDivider(color = grey_border, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Helpers for Safe Parsing without Hardcoding
// ==========================================

private fun formatMemberName(raw: Any?): String {
    if (raw == null) return "—"
    if (raw is String) return raw
    return try {
        val str = raw.toString()
        if (str.contains("firstName")) {
            val fn = Regex("firstName=([^,)]+)").find(str)?.groupValues?.get(1)?.trim() ?: ""
            val ln = Regex("lastName=([^,)]+)").find(str)?.groupValues?.get(1)?.trim() ?: ""
            "$fn $ln".trim().ifBlank { "—" }
        } else {
            str
        }
    } catch (_: Exception) {
        raw.toString()
    }
}

private fun resolveMemberId(raw: Any?): String? {
    if (raw == null) return null
    if (raw is String) return raw
    return try {
        Regex("_id=([^,)]+)").find(raw.toString())?.groupValues?.get(1)?.trim()
    } catch (_: Exception) {
        null
    }
}

private fun formatBranchName(raw: Any?, employees: List<Any>): String {
    if (raw == null) return "—"
    if (raw is String) return raw
    return try {
        Regex("name=([^,)]+)").find(raw.toString())?.groupValues?.get(1)?.trim() ?: raw.toString()
    } catch (_: Exception) {
        raw.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDurationBetween(startIso: String?, endIso: String?): String {
    if (startIso.isNullOrBlank() || endIso.isNullOrBlank()) return "—"
    return try {
        val start = Instant.parse(startIso)
        val end = Instant.parse(endIso)
        val minutes = Duration.between(start, end).toMinutes()
        if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            if (remMinutes == 0L) "${hours}h" else "${hours}h ${remMinutes}m"
        }
    } catch (_: Exception) {
        "—"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatIsoTimestamp(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        isoString.take(16).replace('T', ' ')
    }
}

@SuppressLint("DefaultLocale")
private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / 1024.0.pow(exp.toDouble()), pre)
}

private fun resolveActivityIcon(action: String): ImageVector {
    val a = action.lowercase()
    return when {
        a.contains("create") -> Icons.Default.ConfirmationNumber
        a.contains("assign") -> Icons.Default.PersonAdd
        a.contains("priority") -> Icons.Default.Flag
        a.contains("status") || a.contains("resolved") -> Icons.Default.Refresh
        a.contains("reply") || a.contains("message") -> Icons.Default.ChatBubble
        else -> Icons.Default.Circle
    }
}