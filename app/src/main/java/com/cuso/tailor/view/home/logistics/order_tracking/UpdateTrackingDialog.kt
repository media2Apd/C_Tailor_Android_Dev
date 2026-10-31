package com.cuso.tailor.view.home.logistics.order_tracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.TrackingOverviewData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.viewmodel.TRACKING_STATUS_OPTIONS

/**
 * Returns mapped theme color for tracking status dots.
 */
private fun getStatusThemeColor(statusName: String): Color {
    return when (statusName) {
        "Shipped" -> chartBlue
        "In Transit" -> chartIndigo
        "Out for Delivery" -> chartAmber
        "Delivered" -> chartEmerald
        "Failed" -> redText
        else -> iconMuted
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateTrackingStatusModal(
    identifier: String,
    overview: TrackingOverviewData?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (status: String, location: String, note: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val initialStatus = remember(overview?.currentStatus) {
        val found = TRACKING_STATUS_OPTIONS.firstOrNull {
            it.id.equals(overview?.currentStatus, ignoreCase = true)
        }
        found?.id ?: "Created"
    }

    var selectedStatus by remember { mutableStateOf(initialStatus) }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    fun syncWithTimeline(statusKey: String) {
        val match = overview?.trackingTimeline?.firstOrNull {
            it.stage.equals(statusKey, ignoreCase = true) ||
                    (statusKey == "Created" && it.stage.equals("Shipped", ignoreCase = true))
        }
        if (match != null) {
            location = match.location.orEmpty()
            note = match.note.orEmpty()
        } else {
            location = overview?.shipmentInfo?.origin.orEmpty()
            note = ""
        }
    }

    LaunchedEffect(Unit) {
        syncWithTimeline(initialStatus)
    }

    // Status display name and list for FormDropdown
    val currentOptionName = remember(selectedStatus) {
        TRACKING_STATUS_OPTIONS.firstOrNull { it.id == selectedStatus }?.name ?: "Select status"
    }
    val statusNamesList = remember {
        TRACKING_STATUS_OPTIONS.map { it.name }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header & Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.cardPadding, vertical = tokens.cardPadding)
                ) {
                    // Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Update Tracking Status",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = identifier,
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = close_color
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 1. Status Dropdown (Reusable FormDropdown)
                    FormDropdown(
                        label = "Status",
                        value = currentOptionName,
                        expanded = expandedDropdown,
                        onExpandChange = { expandedDropdown = it },
                        options = statusNamesList,
                        onOptionSelected = { selectedName ->
                            val matched = TRACKING_STATUS_OPTIONS.firstOrNull { it.name == selectedName }
                            if (matched != null) {
                                selectedStatus = matched.id
                                syncWithTimeline(matched.id)
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Status selection dots (FlowRow prevents awkward word breaks like "Deliver\ned")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TRACKING_STATUS_OPTIONS.forEach { opt ->
                            val isSelected = selectedStatus == opt.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedStatus = opt.id
                                        syncWithTimeline(opt.id)
                                    }
                                    .padding(vertical = 3.dp, horizontal = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(getStatusThemeColor(opt.name), CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = opt.name,
                                    fontSize = tokens.caption,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else mutedText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 2. Location Input (Reusable FormTextField)
                    FormLabel(text = "Current Location")
                    FormTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "Primary Workshop Hub",
                        containerColor = light_grey
                    )

                    Spacer(Modifier.height(14.dp))

                    // 3. Notes Input (Reusable FormTextArea)
                    FormLabel(text = "Notes")
                    FormTextArea(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Add any specific details regarding this update...",
                        minLines = 3,
                        maxLines = 4,
                        focusedBorderColor = Primary
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = redText,
                            fontSize = tokens.caption
                        )
                    }
                }

                // Bottom Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = headerBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.cardPadding, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, sectionBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.height(tokens.buttonHeight)
                        ) {
                            Text("Cancel", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = { onSubmit(selectedStatus, location, note) },
                            enabled = !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(tokens.buttonHeight)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = whiteBg
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isSaving) "Updating..." else "Update Status",
                                fontSize = tokens.bodyMedium,
                                color = whiteBg,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}