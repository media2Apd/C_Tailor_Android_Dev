@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.courier_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CourierManagementItemDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun AllCourierScreen(
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    onViewDeliveryDetail: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Observe data from LogisticsViewModel
    val deliveries by viewModel.filteredCourierDeliveries.collectAsState()
    val isLoading by viewModel.isLoadingCourierDeliveries.collectAsState()
    val searchQuery by viewModel.courierSearchQuery.collectAsState()
    val courierPartners by viewModel.courierPartnersList.collectAsState()
    val isAssigning by viewModel.isAssigningCourier.collectAsState()
    val assignError by viewModel.assignCourierError.collectAsState()

    // Dialog state
    var selectedDeliveryForAssign by remember { mutableStateOf<CourierManagementItemDto?>(null) }

    // Fetch initial data from /api/logistics/courier-management/list
    LaunchedEffect(Unit) {
        viewModel.fetchCourierManagementList()
        viewModel.fetchCourierPartners()
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(
                title = "All Courier",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onCourierSearchQueryChange(it) },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                height = 44.dp
            )

            when {
                isLoading && deliveries.isEmpty() -> {
                    ListSkeleton()
                }

                deliveries.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records found",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = deliveries,
                            key = { it.id.ifBlank { it.deliveryId } }
                        ) { item ->
                            CourierDeliveryCard(
                                item = item,
                                onAssignClick = { selectedDeliveryForAssign = item },
                                onViewDetail = { id -> onViewDeliveryDetail(id) },
                                formatDateHelper = { rawDate -> viewModel.formatDeliveryDate(rawDate) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Assign Delivery
    if (selectedDeliveryForAssign != null) {
        val courierOptions = remember(courierPartners) {
            courierPartners.map { it.courierName }.distinct()
        }

        AssignDeliveryModalDialog(
            deliveryItem = selectedDeliveryForAssign!!,
            courierOptions = courierOptions,
            isSubmitting = isAssigning,
            errorMessage = assignError,
            onDismiss = { selectedDeliveryForAssign = null },
            onConfirmAssign = { courier, charge, instructions ->
                val targetDeliveryId = selectedDeliveryForAssign!!.id.ifBlank { selectedDeliveryForAssign!!.id }
                viewModel.assignCourier(
                    deliveryId = targetDeliveryId,
                    courierPartner = courier,
                    deliveryCharge = charge,
                    instructions = instructions,
                    onSuccess = {
                        selectedDeliveryForAssign = null
                        viewModel.fetchCourierManagementList()
                    }
                )
            }
        )
    }
}

/**
 * Main Record Card representing each delivery in the courier list.
 */
@Composable
private fun CourierDeliveryCard(
    item: CourierManagementItemDto,
    onAssignClick: () -> Unit,
    onViewDetail: (String) -> Unit,
    formatDateHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current

    val validId = remember(item) {
        item.id.trim().ifBlank { item.deliveryId.trim() }
    }

    val customerDisplay = remember(item.deliveryId) {
        item.deliveryId.ifBlank { "—" }
    }

    val formattedDate = remember(item.deliveryDate) {
        if (item.deliveryDate.isNotBlank()) "Delivery Date: ${formatDateHelper(item.deliveryDate)}"
        else "Delivery Date: —"
    }

    val actions = remember(validId) {
        listOf(
            MenuAction(
                label = "View Details",
                onClick = { if (validId.isNotBlank()) onViewDetail(validId) }
            )
        )
    }

    DataCard(
        item = item,
        onClick = { if (validId.isNotBlank()) onViewDetail(validId) },
        actions = emptyList(),
        showDivider = false,
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: Customer, Order Pill, Status Pill & 3-dot Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Customer Name and Order ID Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customerDisplay,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        // Order Code Pill
                        if (item.orderId.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(light_blue)
                                    .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.orderId,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                            }
                        }
                    }

                    // Right: Status Pill and 3-dot Action Dropdown Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Status Pill (Ready)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(greenBg)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.status.ifBlank { "Ready" },
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = greentext
                            )
                        }

                        // 3-dot Menu positioned right next to the status badge
                        if (actions.isNotEmpty()) {
                            ActionDropdownMenu(
                                actions = actions,
                                icon = Icons.Default.MoreVert
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Delivery Date
                Text(
                    text = formattedDate,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Container: Delivery Type & Customer Reference Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delivery Type Column
                    Column {
                        Text(
                            text = "Delivery Type",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.deliveryType.ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    // Customer / Phone Column
                    Column {
                        Text(
                            text = "Customer",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.customer.ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Row: Right-aligned Assign button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onAssignClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f)
                    ) {
                        Text(
                            text = "Assign",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    )
}

/**
 * Assign Delivery Modal Dialog matching the screenshot.
 */
@Composable
private fun AssignDeliveryModalDialog(
    deliveryItem: CourierManagementItemDto,
    courierOptions: List<String>,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmAssign: (courier: String, charge: Double, instructions: String?) -> Unit
) {
    val tokens = LocalAppTokens.current

    var selectedCourier by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var deliveryChargeInput by remember { mutableStateOf("40") }
    var instructionsInput by remember { mutableStateOf("") }

    val deliveryCode = deliveryItem.deliveryId.ifBlank { "—" }
    val orderCode = deliveryItem.orderId.ifBlank { "—" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (tokens.isTablet) 0.65f else 0.94f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Delivery",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Banner Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(light_blue)
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assigning for Delivery ",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Text(
                            text = deliveryCode,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = " (Order #$orderCode)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message banner
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        fontSize = tokens.caption,
                        color = redText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Field 1: Select Courier
                FormLabel(text = "Select Courier", isRequired = true)
                FormDropdown(
                    value = selectedCourier.ifBlank { "Select courier" },
                    expanded = isDropdownExpanded,
                    onExpandChange = { isDropdownExpanded = it },
                    options = courierOptions.ifEmpty { listOf("Loading couriers...") },
                    onOptionSelected = { selectedCourier = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Delivery Charge
                FormLabel(text = "Delivery Charge", isRequired = false)
                FormTextField(
                    value = deliveryChargeInput,
                    onValueChange = { deliveryChargeInput = it },
                    placeholder = "40",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Instruction for Courier
                FormLabel(text = "Instruction for Courier", isRequired = false)
                FormTextArea(
                    value = instructionsInput,
                    onValueChange = { instructionsInput = it },
                    placeholder = "Add specific stitching or adjustment instructions...",
                    minLines = 4,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons: Cancel and Assign courier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    Button(
                        onClick = {
                            val charge = deliveryChargeInput.toDoubleOrNull() ?: 0.0
                            onConfirmAssign(selectedCourier, charge, instructionsInput)
                        },
                        enabled = selectedCourier.isNotBlank() && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Assign courier",
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