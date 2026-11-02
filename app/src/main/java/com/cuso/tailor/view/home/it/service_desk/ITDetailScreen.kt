@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.it.service_desk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.TicketDetailDto
import com.cuso.tailor.model.it.UpdateTicketStateRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItTicketDetailScreen(
    ticketId: String,
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val ticket by viewModel.ticketDetail.collectAsState()
    val isLoading by viewModel.isLoadingTicketDetail.collectAsState()

    var showAssignModal by remember { mutableStateOf(false) }
    var showPriorityModal by remember { mutableStateOf(false) }
    var showStatusModal by remember { mutableStateOf(false) }
    var showCloseModal by remember { mutableStateOf(false) }

    var replyText by remember { mutableStateOf("") }
    var isSendingReply by remember { mutableStateOf(false) }
    var isUpdatingState by remember { mutableStateOf(false) }

    LaunchedEffect(ticketId) {
        viewModel.fetchTicketDetail(ticketId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Ticket Details",
            onClose = onNavigateBack
        )

        if (isLoading && ticket == null) {
            ListSkeleton()
        } else if (ticket != null) {
            val item = ticket!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                // Top Identification Banner
                TopTicketCard(
                    ticket = item,
                    onAssignClick = { showAssignModal = true },
                    onStatusClick = { showStatusModal = true },
                    onPriorityClick = { showPriorityModal = true },
                    onCloseClick = { showCloseModal = true }
                )

                // Key Highlights Card
                SectionContainer(title = "KEY HIGHLIGHTS") {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                            HighlightBlock(modifier = Modifier.weight(1f), label = "Category", value = item.category)
                            HighlightBlock(modifier = Modifier.weight(1f), label = "Priority", value = item.priority, valueColor = getPriorityTextColor(item.priority))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                            HighlightBlock(modifier = Modifier.weight(1f), label = "Status", value = item.status)
                            HighlightBlock(modifier = Modifier.weight(1f), label = "Requester", value = item.requester?.name ?: "--")
                        }
                        HighlightBlock(modifier = Modifier.fillMaxWidth(), label = "Branch", value = item.branch?.name ?: "--")
                    }
                }

                // Ticket Information Card
                SectionContainer(title = "Ticket Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        InfoRow(label = "ID", value = item.ticketId)
                        InfoRow(label = "Subject", value = item.subject)
                        InfoRow(label = "Category", value = item.category)
                        InfoRow(label = "Priority", value = item.priority)
                        InfoRow(label = "Status", value = item.status)
                        InfoRow(label = "Requester", value = item.requester?.name ?: "--")
                        InfoRow(
                            label = "Assignee",
                            value = item.assignee?.let {
                                if (!it.assignedUserName.isNullOrBlank()) "${it.assignedUserName} (${it.team ?: "IT Support"})"
                                else it.team ?: "Unassigned"
                            } ?: "Unassigned"
                        )
                        InfoRow(label = "Branch", value = item.branch?.name ?: "--")
                    }
                }

                // Description Box
                SectionContainer(title = "Description") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                            .background(Primary_background)
                            .padding(tokens.screenPadding)
                    ) {
                        Text(
                            text = item.description,
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }
                }

                // Related Information (Device / Asset / Application)
                if (item.relatedEntity != null && (item.relatedEntity.deviceCode != null || item.relatedEntity.assetCode != null || item.relatedEntity.applicationName != null)) {
                    SectionContainer(title = "Related Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)) {
                            item.relatedEntity.deviceCode?.let {
                                InfoRow(label = "[View Device]", value = it, labelColor = Primary)
                            }
                            item.relatedEntity.assetCode?.let {
                                InfoRow(label = "[View Asset]", value = it, labelColor = Primary)
                            }
                            item.relatedEntity.applicationName?.let {
                                InfoRow(label = "[Application]", value = it)
                            }
                        }
                    }
                }

                // SLA Timeline Bar
                item.slaLive?.let { sla ->
                    SectionContainer(title = "SLA") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SlaStep(label = "Created", value = sla.createdTime ?: "--", color = mutedText)
                            SlaStep(label = "First Response", value = sla.firstResponseTime ?: "Pending", color = greentext)
                            SlaStep(label = "Current Time", value = sla.currentTime ?: "--", color = orangeText)
                            SlaStep(label = "Target", value = sla.targetTime ?: "--", color = TextPrimary)
                        }
                    }
                }

                // Conversation Thread
                SectionContainer(title = "Conversation") {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                        if (item.conversations.isEmpty()) {
                            Text("No conversation messages logged yet.", fontSize = tokens.bodySmall, color = mutedText)
                        } else {
                            item.conversations.forEach { conv ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                        .background(if (conv.isInternalNote) yellowBg else Primary_background)
                                        .border(1.dp, if (conv.isInternalNote) orangeText.copy(alpha = 0.3f) else sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                        .padding(tokens.screenPadding * 0.75f)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (conv.isInternalNote) {
                                                    Icon(Icons.Default.Lock, contentDescription = null, tint = orangeText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                                                    Spacer(Modifier.width(tokens.screenPadding / 4))
                                                }
                                                Text(
                                                    text = "${conv.senderName ?: "Support"} (${conv.senderRole ?: "Member"})",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (conv.isInternalNote) orangeText else title_color
                                                )
                                            }
                                            Text(
                                                text = conv.createdAt?.take(10) ?: "",
                                                fontSize = tokens.caption,
                                                color = mutedText
                                            )
                                        }
                                        Text(
                                            text = conv.message,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Message Input
                        FormTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = "Type your reply or internal note..."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                        ) {
                            Button(
                                onClick = {
                                    if (replyText.isBlank()) return@Button
                                    isSendingReply = true
                                    viewModel.postTicketConversation(
                                        ticketId = item.ticketId,
                                        message = replyText,
                                        isInternalNote = true,
                                        onSuccess = {
                                            isSendingReply = false
                                            replyText = ""
                                            DynamicIslandManager.showSuccess("Internal note added.")
                                        },
                                        onError = { err ->
                                            isSendingReply = false
                                            DynamicIslandManager.showError(err)
                                        }
                                    )
                                },
                                enabled = !isSendingReply,
                                colors = ButtonDefaults.buttonColors(containerColor = yellowBg),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, orangeText.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                            ) {
                                Text("Add Internal Note", fontSize = tokens.bodySmall, color = orangeText)
                            }

                            Button(
                                onClick = {
                                    if (replyText.isBlank()) return@Button
                                    isSendingReply = true
                                    viewModel.postTicketConversation(
                                        ticketId = item.ticketId,
                                        message = replyText,
                                        isInternalNote = false,
                                        onSuccess = {
                                            isSendingReply = false
                                            replyText = ""
                                            DynamicIslandManager.showSuccess("Reply sent.")
                                        },
                                        onError = { err ->
                                            isSendingReply = false
                                            DynamicIslandManager.showError(err)
                                        }
                                    )
                                },
                                enabled = !isSendingReply,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Send", fontSize = tokens.bodySmall, color = whiteBg)
                                    Spacer(Modifier.width(tokens.screenPadding / 4))
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize * 0.8f))
                                }
                            }
                        }
                    }
                }

                // History Audit Logs
                if (item.historyLogs.isNotEmpty()) {
                    SectionContainer(title = "History") {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.5f)) {
                            item.historyLogs.forEach { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(text = log.action, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text(text = "${log.performedBy} · ${log.details.orEmpty()}", fontSize = tokens.caption, color = mutedText)
                                    }
                                    Text(text = log.createdAt?.take(10) ?: "", fontSize = tokens.caption, color = mutedText)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))
            }
        }
    }

    // Modal 1: Assign Staff / Team
    if (showAssignModal && ticket != null) {
        AssignSupportModal(
            initialTeam = ticket!!.assignee?.team.orEmpty(),
            initialName = ticket!!.assignee?.assignedUserName.orEmpty(),
            isLoading = isUpdatingState,
            onDismiss = { if (!isUpdatingState) showAssignModal = false },
            onConfirm = { team, name ->
                isUpdatingState = true
                viewModel.updateTicketState(
                    ticketId = ticket!!.ticketId,
                    request = UpdateTicketStateRequest(assigneeTeam = team, assignedUserName = name),
                    onSuccess = {
                        isUpdatingState = false
                        showAssignModal = false
                        DynamicIslandManager.showSuccess("Assignee updated.")
                    },
                    onError = { err ->
                        isUpdatingState = false
                        DynamicIslandManager.showError(err)
                    }
                )
            }
        )
    }

    // Modal 2: Change Status
    if (showStatusModal && ticket != null) {
        ChangeStatusModal(
            currentStatus = ticket!!.status,
            isLoading = isUpdatingState,
            onDismiss = { if (!isUpdatingState) showStatusModal = false },
            onConfirm = { newStatus ->
                isUpdatingState = true
                viewModel.updateTicketState(
                    ticketId = ticket!!.ticketId,
                    request = UpdateTicketStateRequest(status = newStatus),
                    onSuccess = {
                        isUpdatingState = false
                        showStatusModal = false
                        DynamicIslandManager.showSuccess("Status updated.")
                    },
                    onError = { err ->
                        isUpdatingState = false
                        DynamicIslandManager.showError(err)
                    }
                )
            }
        )
    }

    // Modal 3: Change Priority & SLA
    if (showPriorityModal && ticket != null) {
        ChangePriorityModal(
            currentPriority = ticket!!.priority,
            isLoading = isUpdatingState,
            onDismiss = { if (!isUpdatingState) showPriorityModal = false },
            onConfirm = { newPriority ->
                isUpdatingState = true
                viewModel.updateTicketState(
                    ticketId = ticket!!.ticketId,
                    request = UpdateTicketStateRequest(priority = newPriority),
                    onSuccess = {
                        isUpdatingState = false
                        showPriorityModal = false
                        DynamicIslandManager.showSuccess("Priority updated.")
                    },
                    onError = { err ->
                        isUpdatingState = false
                        DynamicIslandManager.showError(err)
                    }
                )
            }
        )
    }

    // Modal 4: Close Ticket Confirmation
    if (showCloseModal && ticket != null) {
        CloseTicketModal(
            isLoading = isUpdatingState,
            onDismiss = { if (!isUpdatingState) showCloseModal = false },
            onConfirm = {
                isUpdatingState = true
                viewModel.updateTicketState(
                    ticketId = ticket!!.ticketId,
                    request = UpdateTicketStateRequest(status = "Closed"),
                    onSuccess = {
                        isUpdatingState = false
                        showCloseModal = false
                        DynamicIslandManager.showSuccess("Ticket closed.")
                    },
                    onError = { err ->
                        isUpdatingState = false
                        DynamicIslandManager.showError(err)
                    }
                )
            }
        )
    }
}

@Composable
private fun TopTicketCard(
    ticket: TicketDetailDto,
    onAssignClick: () -> Unit,
    onStatusClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(tokens.screenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.ticketId,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                StatusBadge(
                    text = ticket.priority,
                    bgColor = getPriorityBgColor(ticket.priority),
                    textColor = getPriorityTextColor(ticket.priority)
                )
            }

            Text(
                text = ticket.subject,
                fontSize = tokens.bodySmall,
                color = mutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 3)
            ) {
                Button(
                    onClick = onAssignClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = whiteBg
                    ),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Text("Assign", fontSize = tokens.caption, color = title_color)
                }

                Button(
                    onClick = onStatusClick,
                    colors = ButtonDefaults.buttonColors(containerColor = whiteBg),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Text("Status", fontSize = tokens.caption, color = title_color)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 3)
            ) {
                Button(
                    onClick = onPriorityClick,
                    colors = ButtonDefaults.buttonColors(containerColor = whiteBg),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Text("Priority", fontSize = tokens.caption, color = title_color)
                }

                if (ticket.status != "Closed") {
                    Button(
                        onClick = onCloseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = redText),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                    ) {
                        Text("Close", fontSize = tokens.caption, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionContainer(title: String, content: @Composable () -> Unit) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .padding(tokens.screenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
            Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
            content()
        }
    }
}

@Composable
private fun HighlightBlock(modifier: Modifier, label: String, value: String, valueColor: Color = TextPrimary) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .background(Primary_background)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .padding(tokens.screenPadding * 0.75f)
    ) {
        Column {
            Text(label, fontSize = tokens.caption, color = mutedText)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = valueColor)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, labelColor: Color = mutedText) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = labelColor)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun SlaStep(label: String, value: String, color: Color) {
    val tokens = LocalAppTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(tokens.iconSize * 0.5f).clip(CircleShape).background(color))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = tokens.label, color = mutedText)
        Text(value, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

// --- Action Modals ---

@Composable
private fun AssignSupportModal(
    initialTeam: String,
    initialName: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (team: String, name: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var team by remember { mutableStateOf(initialTeam) }
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)) {
        Surface(shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f), color = whiteBg) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                Text("Assign Support Staff / Team", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                Column {
                    FormLabel(text = "Support Team")
                    FormTextField(value = team, onValueChange = { team = it }, placeholder = "e.g., Hardware Desk / Network Team")
                }

                Column {
                    FormLabel(text = "Assignee Agent Name")
                    FormTextField(value = name, onValueChange = { name = it }, placeholder = "e.g., Staff Name")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = whiteBg), border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder), modifier = Modifier.weight(1f)) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Button(
                        onClick = { onConfirm(team.trim(), name.trim()) },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Save Assignee", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeStatusModal(
    currentStatus: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var status by remember { mutableStateOf(currentStatus) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)) {
        Surface(shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f), color = whiteBg) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                Text("Change Status", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                FormDropdown(
                    label = "Status",
                    value = status,
                    expanded = expanded,
                    onExpandChange = { expanded = it },
                    options = listOf("Open", "In Progress", "Pending", "Resolved", "Closed"),
                    onOptionSelected = { status = it }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = whiteBg), border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder), modifier = Modifier.weight(1f)) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Button(
                        onClick = { onConfirm(status) },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Update Status", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangePriorityModal(
    currentPriority: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var priority by remember { mutableStateOf(currentPriority) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)) {
        Surface(shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f), color = whiteBg) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                Text("Change Priority & SLA", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                FormDropdown(
                    label = "Priority",
                    value = priority,
                    expanded = expanded,
                    onExpandChange = { expanded = it },
                    options = listOf("Critical", "High", "Medium", "Low"),
                    onOptionSelected = { priority = it }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = whiteBg), border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder), modifier = Modifier.weight(1f)) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Button(
                        onClick = { onConfirm(priority) },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Update Priority", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun CloseTicketModal(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)) {
        Surface(shape = RoundedCornerShape(tokens.cardCornerRadius * 1.2f), color = whiteBg) {
            Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.6f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Close Ticket", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedText, modifier = Modifier.size(tokens.iconSize).clickable { onDismiss() })
                }
                Text("Are you sure you want to close this ticket?", fontSize = tokens.bodySmall, color = TextSecondary)
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = whiteBg), border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder), modifier = Modifier.weight(1f)) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = redText, disabledContainerColor = disabled),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Close Ticket", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}