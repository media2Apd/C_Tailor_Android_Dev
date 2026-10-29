@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.order_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.SmoothBottomSheet
import com.cuso.tailor.viewmodel.LogisticsViewModel

data class StatusOptionItem(
    val id: String,
    val name: String,
    val dotColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateOrderStatusBottomSheet(
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    orderId: String,
    orderCode: String,
    currentStatus: String = "Created",
    lastUpdate: String? = null,
    viewModel: LogisticsViewModel,
    onBlurScrimChange: (Dp, Float) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current
    val isSubmitting by viewModel.isSubmittingAction.collectAsState()
    val apiError by viewModel.statusUpdateError.collectAsState()

    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }
    var notesText by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Matches STATUS_OPTIONS & DOTS from React
    val allStatusOptions = remember {
        listOf(
            StatusOptionItem("Created", "Created", headerGrey),
            StatusOptionItem("Allocated", "Allocated", chartBlue),
            StatusOptionItem("Picked", "Picked", yellowText),
            StatusOptionItem("Packed", "Packed", chartIndigo),
            StatusOptionItem("Ready for Shipment", "Ready for Shipment", purpleText),
            StatusOptionItem("Shipped", "Shipped", complete_button_bg),
            StatusOptionItem("In_Transit", "In Transit", BluePrimary),
            StatusOptionItem("Out_for_Delivery", "Out for Delivery", orangeText),
            StatusOptionItem("Delivered", "Delivered", greentext),
            StatusOptionItem("Cancelled", "Cancelled", redText)
        )
    }

    // Reset state whenever the bottom sheet opens
    LaunchedEffect(sheetState, currentStatus) {
        if (sheetState != SheetValue.Hidden) {
            selectedStatus = currentStatus.ifBlank { "Created" }
            notesText = ""
            viewModel.clearStatusUpdateError()
        }
    }

    val selectedOption = allStatusOptions.find { it.id.equals(selectedStatus, ignoreCase = true) }
        ?: allStatusOptions.first()

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.82f,
        expandedFraction = 0.94f,
        dragCloseEnabled = true,
        scrollableContent = true,
        sheetBackgroundColor = whiteBg,
        onBlurScrimChange = onBlurScrimChange,
        headerContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Update Order Status",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Order ID: #$orderCode",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dropdown Picker Field (SelectDropdown replica)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Order Status",
                    fontSize = tokens.bodySmall,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(headerBg)
                        .border(BorderStroke(1.dp, sectionBorder), RoundedCornerShape(tokens.cardCornerRadius))
                        .clickable { isDropdownExpanded = !isDropdownExpanded }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(selectedOption.dotColor)
                            )
                            Text(
                                text = selectedOption.name,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = headerGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(whiteBg)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    ) {
                        allStatusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(opt.dotColor)
                                        )
                                        Text(
                                            text = opt.name,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    selectedStatus = opt.id
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Selection Pills Row (excluding 'Cancelled' to match React logic)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allStatusOptions
                    .filter { it.id != "Cancelled" }
                    .forEach { opt ->
                        val isSelected = selectedStatus.equals(opt.id, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) primary_light else headerBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else sectionBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedStatus = opt.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(opt.dotColor)
                                )
                                Text(
                                    text = opt.name,
                                    fontSize = tokens.label,
                                    color = if (isSelected) Primary else TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
            }

            // Notes Text Input Container
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Notes",
                    fontSize = tokens.bodySmall,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(headerBg)
                        .border(BorderStroke(1.dp, sectionBorder), RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(12.dp)
                ) {
                    if (notesText.isEmpty()) {
                        Text(
                            text = "Add an optional note...",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                    BasicTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Inline Error Banner
            if (!apiError.isNullOrBlank()) {
                Text(
                    text = apiError ?: "Failed to update status",
                    fontSize = tokens.caption,
                    color = redText,
                    fontWeight = FontWeight.Medium
                )
            }

            // Update Status Button
            Button(
                onClick = {
                    viewModel.updateOrderStatus(
                        orderId = orderId,
                        newStatus = selectedStatus,
                        note = notesText,
                        onSuccess = {
                            onStateChange(SheetValue.Hidden)
                        }
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = whiteBg,
                    disabledContainerColor = disabled
                )
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = whiteBg,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Updating...",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "Update Status",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Cancel Button
            OutlinedButton(
                onClick = { onStateChange(SheetValue.Hidden) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, sectionBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = title_border, thickness = 0.6.dp)

            // Audit Trail / Timestamp Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = headerGrey,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = lastUpdate ?: "Status changes are recorded in the audit trail",
                    fontSize = tokens.label,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}