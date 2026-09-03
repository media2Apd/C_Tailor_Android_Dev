@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "UNNECESSARY_SAFE_CALL"
)

package com.cuso.mobile.view.home.services.service_status.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.*
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.OrderViewUiState
import com.cuso.mobile.viewmodel.OrderViewViewModel
import com.cuso.mobile.viewmodel.StageUpdateState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServiceStatusDetailScreen(
    orderId: String,
    onClose: () -> Unit,
    onEditOrder: () -> Unit = {},
    onAssignAllStages: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val viewModel: OrderViewViewModel = hiltViewModel()
    val overviewViewModel: OrderOverviewViewModel = hiltViewModel()

    val uiState by viewModel.orderViewState.collectAsStateWithLifecycle()
    val stageUpdateState by overviewViewModel.stageUpdateState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.getOrdersView(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        ServiceStatusHeader(onClose = onClose, tokens = tokens)

        when (val state = uiState) {
            is OrderViewUiState.Loading, OrderViewUiState.Idle -> {
                ListSkeleton()
            }
            is OrderViewUiState.Error -> {
                AppErrorState(
                    title = "Failed to load dashboard",
                    message = "Something went wrong. Please check your connection and try again.",
                    onRetry = { viewModel.getOrdersView(orderId) }
                )
            }
            is OrderViewUiState.Success -> {
                ServiceStatusDetailContent(
                    orderId = orderId,
                    data = state.data,
                    onEditOrder = onEditOrder,
                    onAssignAllStages = onAssignAllStages,
                    overviewViewModel = overviewViewModel,
                    stageUpdateState = stageUpdateState,
                    onRefresh = { viewModel.getOrdersView(orderId) }
                )
            }
        }
    }
}

@Composable
fun OrderDetailScreen(
    orderId: String,
    onClose: () -> Unit,
    onEditOrder: () -> Unit = {},
    onAssignAllStages: (String) -> Unit = {}
) {
    ServiceStatusDetailScreen(
        orderId = orderId,
        onClose = onClose,
        onEditOrder = onEditOrder,
        onAssignAllStages = onAssignAllStages
    )
}

@Composable
private fun ServiceStatusHeader(
    onClose: () -> Unit,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Service Status",
            fontSize = tokens.h2,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary
            )
        }
    }
    HorizontalDivider(color = BorderGray, thickness = 1.dp)
}

@Composable
private fun ServiceStatusDetailContent(
    orderId: String,
    data: OrderViewData,
    onEditOrder: () -> Unit,
    onAssignAllStages: (String) -> Unit,
    overviewViewModel: OrderOverviewViewModel,
    stageUpdateState: StageUpdateState,
    onRefresh: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val order = data.order

    val isPaymentPending = remember(order.paymentStatus, order.totalPaid, order.balanceAmount) {
        val status = order.paymentStatus.trim().lowercase()
        status == "unpaid" || status == "pending" || (order.totalPaid == 0.0 && order.totalAmount > 0.0)
    }

    var garmentIndex by remember { mutableIntStateOf(0) }
    val currentItem = data.items.getOrNull(garmentIndex)
    val currentStageGroup = currentItem?.let { item -> data.stages.find { it.garmentItemId == item.id } }

    val stageStatusOverrides = remember(garmentIndex) { mutableStateMapOf<String, String>() }
    val stageNotesMap = remember(garmentIndex) { mutableStateMapOf<String, String>() }

    val activeStageIndex by remember(currentStageGroup, stageStatusOverrides.toMap(), isPaymentPending) {
        derivedStateOf {
            if (isPaymentPending) return@derivedStateOf -1
            val stages = currentStageGroup?.stages ?: emptyList()
            val firstIncomplete = stages.indexOfFirst { s ->
                normalizeStatus(stageStatusOverrides[s.id] ?: s.status) != "completed"
            }
            if (firstIncomplete == -1) stages.size else firstIncomplete
        }
    }

    var isContactExpanded by remember { mutableStateOf(true) }
    var isMeasurementsExpanded by remember { mutableStateOf(true) }
    var isFabricExpanded by remember { mutableStateOf(true) }
    var isCustomizationsExpanded by remember { mutableStateOf(true) }
    var garmentDropdownExpanded by remember { mutableStateOf(false) }

    val completedStagesCount = currentStageGroup?.stages?.count {
        normalizeStatus(stageStatusOverrides[it.id] ?: it.status) == "completed"
    } ?: 0
    val totalStagesCount = currentStageGroup?.stages?.size ?: 0
    val isGarmentFullyCompleted = totalStagesCount > 0 && completedStagesCount == totalStagesCount

    val stageActivityCards = remember(data.stages) { buildStageActivityCards(data.stages, data.items) }
    val trialCard = remember(order.trialDate) { buildTrialActivityCard(order.trialDate) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.customerId.name ?: "—",
                            fontSize = tokens.bodyLarge,
                            color = TextPrimary
                        )
                        val priorityText = currentItem?.priority?.ifBlank { "HIGH PRIORITY" } ?: "HIGH PRIORITY"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = redBg
                        ) {
                            Text(
                                text = priorityText.uppercase(),
                                fontSize = tokens.caption,
                                color = redText,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = buildString {
                            append(order.orderNumber)
                            if (!currentItem?.categoryName.isNullOrBlank()) {
                                append("  •  ${currentItem?.categoryName}")
                            }
                        },
                        fontSize = tokens.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = BorderGray.copy(alpha = 0.6f))
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Order Date", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(formatDisplayDate(order.orderDate), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Delivery Date", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(formatDisplayDate(order.deliveryDate), fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEditOrder,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Edit Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Move Delivery", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Garment Item:", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.width(10.dp))

                        Box {
                            Surface(
                                modifier = Modifier.clickable {
                                    if (data.items.size > 1) garmentDropdownExpanded = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                color = whiteBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        currentItem?.categoryName ?: "Garment",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp), tint = mutedText)
                                }
                            }

                            DropdownMenu(
                                expanded = garmentDropdownExpanded,
                                onDismissRequest = { garmentDropdownExpanded = false }
                            ) {
                                data.items.forEachIndexed { idx, item ->
                                    DropdownMenuItem(
                                        text = { Text(item.categoryName, fontSize = tokens.bodySmall) },
                                        onClick = {
                                            garmentIndex = idx
                                            garmentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (isGarmentFullyCompleted) {
                        Surface(shape = RoundedCornerShape(20.dp), color = greenBg) {
                            Text(
                                "Completed",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = greentext,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AccordionSectionCard(
                icon = R.drawable.ic_person,
                title = "Customer Contact",
                expanded = isContactExpanded,
                onToggle = { isContactExpanded = !isContactExpanded },
                tokens = tokens
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Call, null, tint = mutedText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(order.customerId.mobile ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)
                    }

                    val address = order.customerId.address
                    val addressText = if (address != null && (address.addressLine.isNotBlank() || address.city.isNotBlank())) {
                        listOf(address.addressLine, address.city, address.pincode).filter { it.isNotBlank() }.joinToString(", ")
                    } else {
                        (order.customerId.name?.lowercase()?.replace(" ", ".") ?: "customer") + "@email.com"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, null, tint = mutedText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(addressText, fontSize = tokens.bodySmall, color = TextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AccordionSectionCard(
                icon = R.drawable.ic_sliders,
                title = "Measurements",
                expanded = isMeasurementsExpanded,
                onToggle = { isMeasurementsExpanded = !isMeasurementsExpanded },
                tokens = tokens
            ) {
                val measurements = currentItem?.measurementSnapshot?.toList() ?: emptyList()
                if (measurements.isEmpty()) {
                    Text("No specific measurement data recorded.", fontSize = tokens.bodySmall, color = mutedText)
                } else {
                    measurements.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            pair.forEach { (field, valueObj) ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(field.uppercase(), fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(2.dp))
                                    val formattedVal = valueObj.value.joinToString(", ")
                                    val unit = valueObj.unit ?: "in"
                                    Text(
                                        if (formattedVal.isNotBlank()) "$formattedVal $unit" else "— in",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AccordionSectionCard(
                icon = R.drawable.ic_tag,
                title = "Fabric & Material",
                expanded = isFabricExpanded,
                onToggle = { isFabricExpanded = !isFabricExpanded },
                tokens = tokens
            ) {
                currentItem?.fabricDetails?.let { fabric ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailKeyValueRow("Type", fabric.fabricType.ifBlank { "—" }, tokens = tokens)
                        DetailKeyValueRow("Color", fabric.color.ifBlank { "—" }, tokens = tokens)
                        DetailKeyValueRow("Code", fabric.pattern.ifBlank { "—" }, highlightValue = true, tokens = tokens)
                        if (fabric.fabricSource.isNotBlank()) {
                            DetailKeyValueRow("Source", fabric.fabricSource, tokens = tokens)
                        }
                    }
                } ?: Text("No fabric details available.", fontSize = tokens.bodySmall, color = mutedText)
            }

            Spacer(Modifier.height(12.dp))

            AccordionSectionCard(
                icon = R.drawable.ic_layer,
                title = "Customizations",
                expanded = isCustomizationsExpanded,
                onToggle = { isCustomizationsExpanded = !isCustomizationsExpanded },
                tokens = tokens
            ) {
                val customizationList = listOfNotNull(
                    order.wearerType?.let { "Wearer: $it" },
                    if (currentItem?.trialRequired == true) "Trial required before final delivery" else null,
                    if (currentItem?.priority?.isNotBlank() == true) "Priority Level: ${currentItem.priority}" else null,
                    "Gold embroidery on collar and cuffs",
                    "Velvet shawl with matching thread work",
                    "Custom buttons - antique brass",
                    "Inner lining - silk blend"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (item in customizationList.take(4)) {
                        BulletPointItem(item, tokens = tokens)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { currentItem?.let { onAssignAllStages(it.id) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = complete_button_bg)
            ) {
                Text("Complete Garment", color = whiteBg, fontWeight = FontWeight.Bold, fontSize = tokens.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Settings, null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Production Stages", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            if (isPaymentPending) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = redBg,
                    border = BorderStroke(1.dp, redText.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = redText, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Payment is pending. Please complete the payment first to proceed with garment production.",
                            fontSize = tokens.bodySmall,
                            color = redText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            currentStageGroup?.stages?.forEachIndexed { index, stage ->
                val currentStatus = stageStatusOverrides[stage.id] ?: stage.status
                val userEnteredNotes = stageNotesMap[stage.id] ?: ""

                LaunchedEffect(stageUpdateState) {
                    if (stageUpdateState is StageUpdateState.Success && stageUpdateState.stageId == stage.id) {
                        overviewViewModel.resetStageUpdateState()
                        onRefresh()
                    }
                }

                val isUpdating = (stageUpdateState as? StageUpdateState.Loading)?.stageId == stage.id
                val isCompleted = normalizeStatus(currentStatus) == "completed"
                val isAssigned = stage.assignedTo.isNotEmpty()
                val isCurrentActive = !isPaymentPending && index == activeStageIndex
                val isLocked = isPaymentPending || (activeStageIndex != -1 && index > activeStageIndex)

                val assignedWorker = stage.assignedTo.firstOrNull()
                val assignedInfo = if (assignedWorker != null) {
                    val role = assignedWorker.role ?: "Worker"
                    val name = listOfNotNull(assignedWorker.firstName, assignedWorker.lastName).joinToString(" ").trim()
                    if (name.isNotBlank()) "$role - $name" else role
                } else {
                    "Not assigned"
                }

                val defaultNote = when {
                    isPaymentPending -> "Please complete the payment to begin this production stage."
                    !isAssigned -> "Staff has not been assigned to this stage yet."
                    stage.stageName.lowercase() == "order confirmed" -> "Payment received. Fabric ordered from supplier."
                    stage.stageName.lowercase() == "cutting" -> "All pieces cut. Pattern matched perfectly. No fabric wastage."
                    stage.stageName.lowercase() == "stitching" -> "Base stitching completed. Starting embroidery work."
                    else -> "Stage work in progress."
                }

                val effectiveDueDate = formatDisplayDate(stage.completedAt ?: order.deliveryDate)

                SequentialStageCard(
                    stageName = stage.stageName,
                    assignedWorkerInfo = assignedInfo,
                    dueDate = effectiveDueDate,
                    selectedStatus = currentStatus,
                    userNotes = userEnteredNotes,
                    defaultNote = defaultNote,
                    onNotesChange = { stageNotesMap[stage.id] = it },
                    isCompleted = isCompleted,
                    isAssigned = isAssigned,
                    isPaymentPending = isPaymentPending,
                    isActive = isCurrentActive,
                    isLocked = isLocked,
                    isUpdating = isUpdating,
                    onStatusChange = { newStatus ->
                        stageStatusOverrides[stage.id] = newStatus
                        overviewViewModel.updateStage(
                            orderId,
                            currentItem?.id ?: "",
                            stage.id,
                            stage.stageName,
                            normalizeStatus(newStatus)
                        )
                    },
                    tokens = tokens
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Notifications, null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Activity & Alerts", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(12.dp))

            if (stageActivityCards.isEmpty() && trialCard == null) {
                Text("No activity yet", fontSize = tokens.caption, color = mutedText)
            } else {
                trialCard?.let { HighlightAlertCard(it, tokens = tokens) }
                for (activityCard in stageActivityCards) {
                    HighlightAlertCard(activityCard, tokens = tokens)
                }
            }

            Spacer(Modifier.height(110.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top-only shadow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.06f)
                                )
                            )
                        )
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary_background,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onRefresh() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = "Save Updates",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = whiteBg
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(yellowText)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Unsaved changes",
                                    fontSize = tokens.caption,
                                    color = yellowText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Generate Delivery Prep",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionSectionCard(
    icon: Int,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    tokens: AppDesignTokens,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(icon), contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = BorderGray.copy(alpha = 0.6f))
                    Spacer(Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailKeyValueRow(
    label: String,
    value: String,
    highlightValue: Boolean = false,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(
            value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (highlightValue) Primary else TextPrimary
        )
    }
}

@Composable
private fun BulletPointItem(text: String, tokens: AppDesignTokens) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Primary)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = tokens.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun SequentialStageCard(
    stageName: String,
    assignedWorkerInfo: String,
    dueDate: String,
    selectedStatus: String,
    userNotes: String,
    defaultNote: String,
    onNotesChange: (String) -> Unit,
    isCompleted: Boolean,
    isAssigned: Boolean,
    isPaymentPending: Boolean,
    isActive: Boolean,
    isLocked: Boolean,
    isUpdating: Boolean,
    onStatusChange: (String) -> Unit,
    tokens: AppDesignTokens
) {
    val normStatus = normalizeStatus(selectedStatus)
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var isNotesFocused by remember { mutableStateOf(false) }

    val (statusLabel, statusTextColor, cardBgColor, cardBorderColor, buttonBgColor, isActionable) = when {
        isCompleted -> StageThemeConfig(
            statusLabel = "COMPLETED",
            statusTextColor = greentext,
            cardBgColor = activity_green_bg,
            cardBorderColor = complete_button_bg,
            buttonBgColor = complete_button_bg,
            isActionable = false
        )
        isActive && normStatus == "in_progress" -> StageThemeConfig(
            statusLabel = "IN PROGRESS",
            statusTextColor = Primary,
            cardBgColor = activity_purple_bg,
            cardBorderColor = Primary,
            buttonBgColor = Primary,
            isActionable = true
        )
        isActive -> StageThemeConfig(
            statusLabel = "PENDING",
            statusTextColor = yellowText,
            cardBgColor = activity_orange_bg,
            cardBorderColor = BorderGray,
            buttonBgColor = Primary,
            isActionable = true
        )
        else -> StageThemeConfig(
            statusLabel = "PENDING",
            statusTextColor = mutedText,
            cardBgColor = whiteBg,
            cardBorderColor = BorderGray,
            buttonBgColor = disabled,
            isActionable = false
        )
    }

    val stageIcon = when (stageName.lowercase()) {
        "order confirmed" -> rememberVectorPainter(Icons.Outlined.CheckCircle)
        "cutting" -> rememberVectorPainter(Icons.Outlined.ContentCut)
        "stitching" -> painterResource(R.drawable.ic_ticket)
        else -> rememberVectorPainter(Icons.Outlined.AssignmentTurnedIn)
    }

    val canOpenDropdown = isActive && !isCompleted && !isLocked && !isUpdating && isAssigned && !isPaymentPending
    val canEditNotes = isActive && !isCompleted && !isLocked && !isPaymentPending
    val buttonEnabled = isActive && !isCompleted && !isLocked && !isUpdating && isAssigned && !isPaymentPending

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        border = BorderStroke(if (isCompleted || (isActive && normStatus == "in_progress")) 1.5.dp else 1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = stageIcon,
                        contentDescription = null,
                        tint = if (isLocked) mutedText else statusTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stageName.replaceFirstChar { it.uppercase() },
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) mutedText else TextPrimary
                    )
                }

                Box {
                    if (isCompleted) {
                        Text(
                            text = "COMPLETED",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = greentext
                        )
                    } else if (isActive && normStatus == "in_progress") {
                        Text(
                            text = "IN PROGRESS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.clickable(enabled = canOpenDropdown) {
                                statusDropdownExpanded = true
                            }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isLocked) modelGray else yellowBg,
                            modifier = Modifier.clickable(enabled = canOpenDropdown) {
                                statusDropdownExpanded = true
                            }
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) mutedText else yellowText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (canOpenDropdown) {
                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            listOf("In Progress", "Completed").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontSize = tokens.bodySmall) },
                                    onClick = {
                                        statusDropdownExpanded = false
                                        onStatusChange(option)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_person), null, tint = mutedText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when {
                        isPaymentPending -> "Payment pending"
                        isLocked -> "Pending previous stage"
                        else -> assignedWorkerInfo
                    },
                    fontSize = tokens.caption,
                    color = mutedText
                )
                if (dueDate.isNotBlank() && !isLocked && !isPaymentPending) {
                    Text("  •  Due: $dueDate", fontSize = tokens.caption, color = mutedText)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canEditNotes) { },
                shape = RoundedCornerShape(8.dp),
                color = whiteBg
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = userNotes,
                        onValueChange = onNotesChange,
                        enabled = canEditNotes,
                        textStyle = TextStyle(
                            fontSize = tokens.bodySmall,
                            color = if (isLocked) mutedText else TextLog
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isNotesFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            if (isNotesFocused) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (userNotes.isEmpty()) {
                                        Text(
                                            text = "Enter stage note...",
                                            fontSize = tokens.bodySmall,
                                            color = mutedText
                                        )
                                    }
                                    innerTextField()
                                }
                            } else {
                                val displayText = if (userNotes.isNotBlank()) {
                                    "\"${userNotes.trim()}\""
                                } else {
                                    "\"$defaultNote\""
                                }
                                Text(
                                    text = displayText,
                                    fontSize = tokens.bodySmall,
                                    color = if (isLocked) mutedText else TextLog
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val buttonLabel = when {
                isCompleted -> "Completed"
                isPaymentPending -> "Payment Required"
                !isAssigned -> "Not Assigned"
                isLocked -> "Quick Update"
                normStatus == "in_progress" -> "Complete Stage"
                else -> "Quick Update"
            }

            Button(
                onClick = {
                    if (buttonEnabled) {
                        val nextStatus = when (normStatus) {
                            "in_progress" -> "completed"
                            else -> "in_progress"
                        }
                        onStatusChange(nextStatus)
                    }
                },
                enabled = buttonEnabled,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBgColor,
                    disabledContainerColor = if (isCompleted) greenBg else disabled
                )
            ) {
                if (isUpdating) {
                    CirculerProgressIndicatorSmall()
                } else {
                    Text(
                        text = buttonLabel,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (buttonEnabled) whiteBg else mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightAlertCard(
    data: ActivityCardData,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 12.dp,
            bottomStart = 0.dp,
            bottomEnd = 12.dp
        ),
        color = data.bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.5.dp)
                    .background(data.accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.accentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 1.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = data.subtitle,
                        fontSize = tokens.caption,
                        color = data.subtitleColor
                    )
                }
            }
        }
    }
}

private data class StageThemeConfig(
    val statusLabel: String,
    val statusTextColor: Color,
    val cardBgColor: Color,
    val cardBorderColor: Color,
    val buttonBgColor: Color,
    val isActionable: Boolean
)

private data class ActivityCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val subtitleColor: Color,
    val bgColor: Color
)

private fun normalizeStatus(status: String): String {
    return status.trim().lowercase().replace(" ", "_").replace("-", "_")
}

private fun formatDisplayDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "—"
    val cleaned = dateStr.trim()

    val parsePatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "MM-dd-yyyy"
    )

    for (pattern in parsePatterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = sdf.parse(cleaned)
            if (parsedDate != null) {
                val outFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                return outFormatter.format(parsedDate)
            }
        } catch (_: Exception) { }
    }
    return cleaned
}

private fun buildTrialActivityCard(trialDateIso: String?): ActivityCardData? {
    if (trialDateIso.isNullOrBlank()) return null
    return ActivityCardData(
        title = "Trial stage approaching in 2 days. Ensure stitching is completed on time.",
        subtitle = "${formatDisplayDate(trialDateIso)}, 11:00 AM • Trial Ready",
        icon = Icons.Outlined.WarningAmber,
        accentColor = yellowText,
        subtitleColor = activity_brown,
        bgColor = activity_orange_bg
    )
}

private fun buildStageActivityCards(
    stageGroups: List<OrderViewStageGroup>,
    items: List<OrderViewGarmentItem>
): List<ActivityCardData> {
    val nameMap = items.associateBy({ it.id }, { it.categoryName })
    val list = mutableListOf<ActivityCardData>()

    stageGroups.forEach { group ->
        val garmentName = nameMap[group.garmentItemId] ?: "Garment"
        group.stages.filter { normalizeStatus(it.status) == "completed" }.forEach { stage ->
            val formattedDate = formatDisplayDate(stage.completedAt)
            list.add(
                ActivityCardData(
                    title = "${stage.stageName} stage completed ahead of schedule.",
                    subtitle = if (formattedDate != "—") "$formattedDate • ${stage.stageName}" else "Completed • ${stage.stageName}",
                    icon = Icons.Outlined.CheckCircle,
                    accentColor = greentext,
                    subtitleColor = activity_green,
                    bgColor = activity_green_bg
                )
            )
        }
    }

    list.add(
        ActivityCardData(
            title = "Customer called to confirm trial appointment for Feb 21st.",
            subtitle = "Feb 12, 2:30 PM",
            icon = Icons.Outlined.Info,
            accentColor = BluePrimary,
            subtitleColor = activity_purple,
            bgColor = activity_purple_bg
        )
    )

    return list
}