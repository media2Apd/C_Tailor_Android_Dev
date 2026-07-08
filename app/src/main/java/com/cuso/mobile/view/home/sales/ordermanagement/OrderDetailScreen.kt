package com.cuso.mobile.view.home.sales.ordermanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.OrderViewData
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.viewmodel.OrderViewUiState
import com.cuso.mobile.viewmodel.OrderViewViewModel

private val Primary = Color(0xFF3B3BF9)
private val TextMuted = Color(0xFF9CA3AF)
private val TextDark = Color(0xFF111827)

@Composable
fun OrderDetailScreen(
    orderId: String,
    onClose: () -> Unit,
    onEditOrder: () -> Unit = {}
) {
    val viewModel: OrderViewViewModel = hiltViewModel()
    val uiState by viewModel.orderViewState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.getOrdersView(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Orders Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Icon(
                Icons.Default.Close, contentDescription = "Close",
                tint = TextDark,
                modifier = Modifier.size(22.dp).clickable { onClose() }
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        when (val state = uiState) {
            is OrderViewUiState.Loading, OrderViewUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is OrderViewUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Color.Red, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.getOrdersView(orderId) }) { Text("Retry") }
                    }
                }
            }
            is OrderViewUiState.Success -> {
                OrderDetailContent(data = state.data, onEditOrder = onEditOrder)
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    data: OrderViewData,
    onEditOrder: () -> Unit = {}  // This will trigger the navigation

) {
    val order = data.order
    var garmentIndex by remember { mutableIntStateOf(0) }
    val currentItem = data.items.getOrNull(garmentIndex)
    val currentStageGroup = currentItem?.let { item -> data.stages.find { it.garmentItemId == item.id } }

    var stageIndex by remember(garmentIndex) { mutableIntStateOf(0) }
    val currentStage = currentStageGroup?.stages?.getOrNull(stageIndex)

    var fabricExpanded by remember { mutableStateOf(false) }
    var measurementsExpanded by remember { mutableStateOf(false) }
    var stageNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with Customer Name and Order Number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    order.customerId.name ?: "—",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    order.customerId.mobile ?: "—",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
            Box(
                modifier = Modifier
                    .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    order.orderNumber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Order Details Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoColumn("Order Status", order.status.replaceFirstChar { it.uppercase() })
            InfoColumn("Payment", order.paymentStatus.replaceFirstChar { it.uppercase() })
            InfoColumn("Total", "₹${order.totalAmount}")
            InfoColumn("Balance", "₹${order.balanceAmount}")
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoColumn("Wearer", order.wearerType ?: "—")
            InfoColumn("Source", order.source ?: "—")
            InfoColumn("Trial Date", formatIso(order.trialDate ?: ""))
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoColumn("Order Date", formatIso(order.orderDate ?: ""))
            InfoColumn("Delivery Date", formatIso(order.deliveryDate ?: ""))
            InfoColumn("Branch", order.branch?.name ?: "—")
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onEditOrder,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Text("Edit Order", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { /* TODO: move delivery flow */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Move Delivery", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Garment Selector
        if (data.items.size > 1) {
            Text("Garments (${data.items.size})", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.items.forEachIndexed { index, item ->
                    GarmentChip(
                        label = "${index + 1}. ${item.categoryName}",
                        isSelected = index == garmentIndex,
                        onClick = {
                            garmentIndex = index
                            stageIndex = 0
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Fabric Details Section
        ExpandableRow("Fabric & Material Details", fabricExpanded) { fabricExpanded = !fabricExpanded }
        if (fabricExpanded) {
            currentItem?.fabricDetails?.let { fabric ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    LabelValueRow("Fabric Type", fabric.fabricType.ifBlank { "—" })
                    LabelValueRow("Color", fabric.color.ifBlank { "—" })
                    LabelValueRow("Pattern", fabric.pattern.ifBlank { "—" })
                    LabelValueRow("Source", fabric.fabricSource.ifBlank { "—" })
                    LabelValueRow("Priority", currentItem.priority.ifBlank { "—" })
                    LabelValueRow("Trial Required", if (currentItem.trialRequired) "Yes" else "No")
                    LabelValueRow("Stitching Charge", "₹${currentItem.stitchingCharge}")
                    LabelValueRow("Quantity", "${currentItem.quantity}")
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        // Measurements Section - DISPLAYS ACTUAL DATA
        // Measurements Section - Shows in Card layout like the image
        ExpandableRow("Measurements", measurementsExpanded) { measurementsExpanded = !measurementsExpanded }
        if (measurementsExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val measurements = currentItem?.measurementSnapshot
                    if (!measurements.isNullOrEmpty()) {
                        measurements.forEach { (fieldName, measurementValues) ->
                            val value = measurementValues.value.joinToString(", ")
                            val unit = measurementValues.unit ?: ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    fieldName,
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                                Text(
                                    if (value.isNotBlank()) "$value $unit" else "—",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }
                    } else {
                        // Placeholder measurements
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chest", fontSize = 14.sp, color = TextMuted)
                            Text("— in", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sleeve Length", fontSize = 14.sp, color = TextMuted)
                            Text("— in", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        Spacer(Modifier.height(16.dp))

        // Garment Progress
        currentItem?.let { item ->
            val totalStages = currentStageGroup?.stages?.size ?: 0
            val completedStages = currentStageGroup?.stages?.count { stage -> stage.status == "completed" } ?: 0
            val inProgressStages = currentStageGroup?.stages?.count { stage -> stage.status == "in_progress" } ?: 0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.categoryName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        "Progress: $completedStages/$totalStages",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
                Spacer(Modifier.height(4.dp))

                // Progress Bar
                val progress = if (totalStages > 0) completedStages.toFloat() / totalStages else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Primary,
                    trackColor = Color(0xFFE5E7EB)
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Pending: ${totalStages - completedStages}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        "In Progress: $inProgressStages",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { /* TODO: mark garment complete */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Complete Garment", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stage Details
        // Stage Details - With Status Dropdown
        // Stage Details - With Custom Status Dropdown like the image
        currentStage?.let { stage ->
            var statusExpanded by remember { mutableStateOf(false) }
            var selectedStatus by remember(stage.status) { mutableStateOf(stage.status) }

            // Status options from API
            val statusOptions = currentStageGroup.stages.map { it.status }.distinct() ?: listOf("pending", "in_progress", "completed", "failed")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Stage Header with Status Dropdown
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Stage ${stageIndex + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stage.stageName.replaceFirstChar { it.uppercase() },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        // Custom Status Dropdown - Matches the image
                        CustomStatusDropdown(
                            selectedStatus = selectedStatus,
                            statusOptions = statusOptions,
                            expanded = statusExpanded,
                            onExpandChange = { statusExpanded = it },
                            onStatusSelected = {
                                selectedStatus = it
                                // TODO: Call API to update stage status
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Assigned Workers Status - Like the image showing "Not assigned" or assigned info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stage.assignedTo.isNotEmpty()) {
                            // Assigned state - show worker info with green dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Assigned · Qty: ${stage.completedQuantity}/${stage.assignedQuantity}",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        } else {
                            // Not assigned state - show grey dot like the image
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFD1D5DB))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Not assigned · Qty: 0 / 0",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Stage Notes Input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = stageNotes,
                            onValueChange = { stageNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp, color = TextDark),
                            cursorBrush = SolidColor(Primary),
                            decorationBox = { inner ->
                                if (stageNotes.isEmpty()) Text(
                                    "Add stage notes...",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                                inner()
                            }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Quick Update Button - Shows "Not Assigned" when no workers
                    val isAssigned = stage.assignedTo.isNotEmpty()
                    Button(
                        onClick = {
                            // TODO: Update stage with new status and notes
                        },
                        enabled = isAssigned,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAssigned) Primary else Color(0xFFF3F4F6),
                            disabledContainerColor = Color(0xFFF3F4F6),
                            disabledContentColor = TextMuted,  // This sets the text color when disabled
                            contentColor = if (isAssigned) Color.White else TextMuted

                        ),
                    ) {
                        Text(
                            if (isAssigned) "Quick Update" else "Not Assigned",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isAssigned) Color.White else TextMuted
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Stage Navigation
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(
                            onClick = { if (stageIndex > 0) stageIndex-- },
                            enabled = stageIndex > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = if (stageIndex > 0) TextDark else Color(0xFFD1D5DB)
                            )
                        }
                        IconButton(
                            onClick = {
                                val max = (currentStageGroup.stages.size) - 1
                                if (stageIndex < max) stageIndex++
                            },
                            enabled = stageIndex < (currentStageGroup.stages.size) - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (stageIndex < (currentStageGroup.stages.size) - 1)
                                    TextDark
                                else
                                    Color(0xFFD1D5DB)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
        Spacer(Modifier.height(12.dp))

        // Activity Section
        Text("Activity & Alerts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        Spacer(Modifier.height(8.dp))

        if (data.payments.isNullOrEmpty() && data.delivery == null) {
            Text("No activity yet", fontSize = 13.sp, color = TextMuted)
        } else {
            // Show payments if any
            data.payments?.forEach { payment ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "₹${payment.amount ?: 0} via ${payment.method ?: "N/A"} on ${formatIso(payment.date ?: "")}",
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }
            }
            // Show delivery info if any
            data.delivery?.let {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Delivery Status: ${it.status ?: "N/A"}",
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
@Composable
private fun CustomStatusDropdown(
    selectedStatus: String,
    statusOptions: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val displayText = selectedStatus.replaceFirstChar { it.uppercase() }

    Box {
        // Status chip that looks like the image
        Row(
            modifier = Modifier
                .background(
                    when (selectedStatus.lowercase()) {
                        "in_progress", "in progress" -> Color(0xFFFEF3C7)
                        "completed" -> Color(0xFFDBFCE7)
                        "pending" -> Color(0xFFF3F4F6)
                        else -> Color(0xFFF3F4F6)
                    },
                    RoundedCornerShape(20.dp)
                )
                .clickable { onExpandChange(!expanded) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Small colored dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when (selectedStatus.lowercase()) {
                            "in_progress", "in progress" -> Color(0xFF92400E)
                            "completed" -> Color(0xFF0AB83E)
                            "pending" -> Color(0xFF6B7280)
                            else -> Color(0xFF6B7280)
                        }
                    )
            )
            Text(
                displayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when (selectedStatus.lowercase()) {
                    "in_progress", "in progress" -> Color(0xFF92400E)
                    "completed" -> Color(0xFF0AB83E)
                    "pending" -> Color(0xFF6B7280)
                    else -> Color(0xFF6B7280)
                }
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = when (selectedStatus.lowercase()) {
                    "in_progress", "in progress" -> Color(0xFF92400E)
                    "completed" -> Color(0xFF0AB83E)
                    "pending" -> Color(0xFF6B7280)
                    else -> Color(0xFF6B7280)
                },
                modifier = Modifier.size(16.dp)
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(140.dp)
        ) {
            statusOptions.forEach { status ->
                val statusText = status.replaceFirstChar { it.uppercase() }
                val isSelected = status == selectedStatus

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusSelected(status)
                            onExpandChange(false)
                        }
                        .background(
                            if (isSelected) Color(0xFFF3F4F6) else Color.Transparent
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Colored dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                when (status.lowercase()) {
                                    "in_progress", "in progress" -> Color(0xFF92400E)
                                    "completed" -> Color(0xFF0AB83E)
                                    "pending" -> Color(0xFF6B7280)
                                    else -> Color(0xFF6B7280)
                                }
                            )
                    )
                    Text(
                        statusText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = TextDark
                    )
                    if (isSelected) {
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
// Helper Composables

@Composable
private fun GarmentChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Primary else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else TextDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ExpandableRow(title: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = TextMuted
        )
    }
}

@Composable
private fun StageStatusChip(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "in_progress", "in progress" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "completed" -> Color(0xFFDBFCE7) to Color(0xFF0AB83E)
        "pending" -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
        else -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = text
        )
    }
}

private fun formatIso(iso: String): String {
    if (iso.isBlank()) return "—"
    return try {
        val parts = iso.take(10).split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) {
        iso
    }
}