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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.OrderViewData
import com.cuso.mobile.model.sales.OrderViewGarmentItem
import com.cuso.mobile.model.sales.OrderViewStageGroup
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.PanelBg
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redtext
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowtext
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.OrderViewUiState
import com.cuso.mobile.viewmodel.OrderViewViewModel
import com.cuso.mobile.viewmodel.StageUpdateState

/**
 * Main Order Detail Screen providing a deep view into specific order progress,
 * garment stages, and historical activity.
 */
@Composable
fun OrderDetailScreen(
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
            .background(Color.Transparent)
    ) {
        // Simple header: back chevron + title (matches design)
        TitleBar("Order Management",onClose = onClose)
        HorizontalDivider(color = BorderGray)

        when (val state = uiState) {
            is OrderViewUiState.Loading, OrderViewUiState.Idle -> {
                ListSkeleton()
            }
            is OrderViewUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = redtext, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = redtext, fontSize = tokens.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getOrdersView(orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Retry", fontSize = tokens.bodyMedium, color = Color.White)
                        }
                    }
                }
            }
            is OrderViewUiState.Success -> {
                OrderDetailContent(
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
private fun OrderDetailContent(
    orderId: String,
    data: OrderViewData,
    onEditOrder: () -> Unit = {},
    onAssignAllStages: (String) -> Unit = {},
    overviewViewModel: OrderOverviewViewModel,
    stageUpdateState: StageUpdateState,
    onRefresh: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val order = data.order
    var garmentIndex by remember { mutableIntStateOf(0) }
    val currentItem = data.items.getOrNull(garmentIndex)
    val currentStageGroup = currentItem?.let { item -> data.stages.find { it.garmentItemId == item.id } }

    var stageIndex by remember(garmentIndex) { mutableIntStateOf(0) }
    val currentStage = currentStageGroup?.stages?.getOrNull(stageIndex)

    val stageStatusOverrides = remember(garmentIndex) { mutableStateMapOf<String, String>() }

    val unlockedStageIndex by remember(currentStageGroup, stageStatusOverrides.toMap()) {
        derivedStateOf {
            val stages = currentStageGroup?.stages ?: emptyList()
            val firstIncomplete = stages.indexOfFirst { s ->
                normalizeStatus(stageStatusOverrides[s.id] ?: s.status) != "completed"
            }
            if (firstIncomplete == -1) stages.size - 1 else firstIncomplete
        }
    }

    val stageActivityCards = remember(data.stages) { buildStageActivityCards(data.stages, data.items) }
    val trialCard = remember(order.trialDate) { buildTrialActivityCard(order.trialDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Customer and Order Meta Info ──────────────────────────
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.customerId?.name ?: "—", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
                Surface(
                    color = Primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        order.orderNumber,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Garment", order.branch?.name ?: "—")
                InfoColumn("Order Date", formatIso(order.orderDate ?: ""))
                InfoColumn("Delivery", formatIso(order.deliveryDate ?: ""))
            }

            Spacer(Modifier.height(20.dp))

            // Global Order Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onEditOrder,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                ) {
                    Text("Edit Order", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall)
                }

                Button(
                    onClick = { /* Delivery Flow Transition */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Move Delivery", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall)
                }
            }
        }

        // ── Customer Contact ──────────────────────────────────────
        SectionHeader("Customer Contact")
        Row(
            modifier = Modifier
                .padding(horizontal = tokens.screenPadding)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Call, contentDescription = null, tint = mutedText, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                order.customerId?.mobile ?: "—",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Garment Navigation chips (only if multiple items) ────
        if (data.items.size > 1) {
            Column(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                Text("Select Garment Item", fontSize = tokens.bodySmall, color = mutedText)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.items.forEachIndexed { index, item ->
                        GarmentChip(
                            label = "${index + 1}. ${item.categoryName}",
                            isSelected = index == garmentIndex,
                            onClick = { garmentIndex = index; stageIndex = 0 }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Garment header + stage count + Assign All Stages ─────
        currentItem?.let { item ->
            val totalStages = currentStageGroup?.stages?.size ?: 0
            val completedStages = currentStageGroup?.stages?.count {
                normalizeStatus(stageStatusOverrides[it.id] ?: it.status) == "completed"
            } ?: 0

            Column(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                Text(
                    "Garment ${garmentIndex + 1} — ${item.categoryName}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.categoryName, fontSize = tokens.bodySmall, color = mutedText)
                    Text("$completedStages/$totalStages Stages", fontSize = tokens.bodySmall, color = mutedText)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onAssignAllStages(item.id) },
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Complete All Stages", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Measurements (2-column grid) ──────────────────────────
        SectionHeader("Measurements")
        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
            val measurements = currentItem?.measurementSnapshot?.toList() ?: emptyList()
            if (measurements.isEmpty()) {
                Text("No specific measurement data recorded.", fontSize = tokens.bodySmall, color = mutedText)
            } else {
                measurements.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPair.forEach { (fieldName, measurementValues) ->
                            val value = measurementValues.value.joinToString(", ")
                            val unit = measurementValues.unit ?: ""
                            MeasurementCard(
                                label = fieldName,
                                value = if (value.isNotBlank()) "$value $unit" else "— in",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Fabric & Material (no accordion, always visible) ─────
        SectionHeader("Fabric & Material")
        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
            currentItem?.fabricDetails?.let { fabric ->
                LabelValueRow("Type", fabric.fabricType.ifBlank { "—" })
                LabelValueRow("Color", fabric.color.ifBlank { "—" })
                LabelValueRow("Pattern", fabric.pattern.ifBlank { "—" })
                LabelValueRow("Source", fabric.fabricSource.ifBlank { "—" })
                // NOTE: "Priority" isn't in OrderViewGarmentItem/fabricDetails yet.
                // Wire this to the real field once it's added to the model.
                LabelValueRow("Priority", "Low")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Production Stages ─────────────────────────────────────
        SectionHeader("Production Stages")
        currentStageGroup?.stages?.forEachIndexed { index, stage ->
            var statusExpanded by remember(stage.id) { mutableStateOf(false) }
            var selectedStatus by remember(stage.id) { mutableStateOf(stageStatusOverrides[stage.id] ?: stage.status) }
            var stageNotes by remember(stage.id) { mutableStateOf("") }

            LaunchedEffect(stageUpdateState) {
                if (stageUpdateState is StageUpdateState.Success && stageUpdateState.stageId == stage.id) {
                    stageStatusOverrides[stage.id] = selectedStatus
                    overviewViewModel.resetStageUpdateState()
                    onRefresh()
                }
            }

            val isAssigned = stage.assignedTo.isNotEmpty()
            val isUnlocked = index == unlockedStageIndex
            val committedStatus = stageStatusOverrides[stage.id] ?: stage.status
            val isCommittedCompleted = normalizeStatus(committedStatus) == "completed"
            val isUpdating = (stageUpdateState as? StageUpdateState.Loading)?.stageId == stage.id
            val buttonEnabled = isAssigned && isUnlocked && selectedStatus != committedStatus && !isUpdating

            StageCard(
                stageNumber = index + 1,
                stageName = stage.stageName,
                isUnlocked = isUnlocked,
                isAssigned = isAssigned,
                quantity = currentItem?.quantity ?: 0,
                selectedStatus = selectedStatus,
                statusExpanded = statusExpanded,
                onStatusExpandChange = { statusExpanded = it },
                onStatusSelected = { selectedStatus = it },
                isCommittedCompleted = isCommittedCompleted,
                stageNotes = stageNotes,
                onNotesChange = { if (isUnlocked) stageNotes = it },
                isUpdating = isUpdating,
                buttonEnabled = buttonEnabled,
                onButtonClick = {
                    overviewViewModel.updateStage(orderId, currentItem.id, stage.id, stage.stageName, normalizeStatus(selectedStatus))
                },
                tokens = tokens
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))

        // ── Activity & Alerts ─────────────────────────────────────
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text("Activity & Alerts", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            if (stageActivityCards.isEmpty() && trialCard == null) {
                Text("No activity yet", fontSize = tokens.caption, color = mutedText)
            } else {
                trialCard?.let { ActivityLogCard(it) }
                stageActivityCards.forEach { ActivityLogCard(it) }
            }
        }

        Spacer(Modifier.height(50.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Reusable UI Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    val tokens = LocalAppTokens.current
    Text(
        title,
        fontSize = tokens.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 8.dp)
    )
}

@Composable
private fun InfoColumn(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(label, fontSize = tokens.label, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun MeasurementCard(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = PanelBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = tokens.caption, color = mutedText)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun ActivityLogCard(data: ActivityCardData) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = data.bgColor
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(data.icon, null, tint = data.accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(data.title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(data.subtitle, fontSize = tokens.caption, color = mutedText)
            }
        }
    }
}

@Composable
private fun CustomStatusDropdown(
    selectedStatus: String,
    expanded: Boolean,
    enabled: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val (textColor, bgColor) = statusColors(selectedStatus)
    Box {
        Surface(
            modifier = Modifier.clickable(enabled = enabled) { onExpandChange(!expanded) },
            shape = RoundedCornerShape(8.dp),
            color = bgColor
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(selectedStatus.replaceFirstChar { it.uppercase() }, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = textColor)
                Icon(Icons.Default.ArrowDropDown, null, tint = textColor, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
            listOf("Pending", "In Progress", "Completed").forEach { status ->
                DropdownMenuItem(
                    text = { Text(status, fontSize = tokens.bodySmall) },
                    onClick = { onStatusSelected(status); onExpandChange(false) }
                )
            }
        }
    }
}

@Composable
private fun GarmentChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (isSelected) Primary else PanelBg
    ) {
        Text(
            label,
            fontSize = tokens.caption,
            color = if (isSelected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StageCard(
    stageNumber: Int,
    stageName: String,
    isUnlocked: Boolean,
    isAssigned: Boolean,
    quantity: Int,
    selectedStatus: String,
    statusExpanded: Boolean,
    onStatusExpandChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit,
    isCommittedCompleted: Boolean,
    stageNotes: String,
    onNotesChange: (String) -> Unit,
    isUpdating: Boolean,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
    tokens: com.cuso.mobile.adaptive_screen.AppDesignTokens
) {
    val stageLabelColor = if (isUnlocked) greentext else mutedText

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        shape = RoundedCornerShape(16.dp),
        color = whiteBg,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stage $stageNumber", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = stageLabelColor)
                    Text(stageName.replaceFirstChar { it.uppercase() }, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                CustomStatusDropdown(
                    selectedStatus = selectedStatus,
                    expanded = statusExpanded,
                    enabled = isUnlocked && !isCommittedCompleted && !isUpdating,
                    onExpandChange = onStatusExpandChange,
                    onStatusSelected = onStatusSelected
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isAssigned) greentext else mutedText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isAssigned) "Assigned" else "Not assigned",
                    fontSize = tokens.caption,
                    color = if (isAssigned) greentext else mutedText
                )
                Text(" · Qty: 0/$quantity", fontSize = tokens.caption, color = mutedText)
            }

            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, BorderGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
                BasicTextField(
                    value = stageNotes,
                    onValueChange = onNotesChange,
                    enabled = isUnlocked,
                    textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (stageNotes.isEmpty()) Text("Add stage notes...", fontSize = tokens.bodySmall, color = mutedText)
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))
            val buttonLabel = when {
                !isAssigned -> "Not Assigned"
                isCommittedCompleted -> "Completed"
                else -> "Quick Update"
            }
            Button(
                onClick = onButtonClick,
                enabled = buttonEnabled,
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonEnabled) Primary else disabled,
                    disabledContainerColor = lightGray
                )
            ) {
                if (isUpdating) CirculerProgressIndicatorSmall()
                else Text(buttonLabel, fontSize = tokens.bodySmall, color = if (buttonEnabled) Color.White else mutedText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Business Logic Helpers
// ─────────────────────────────────────────────────────────────

private data class ActivityCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val bgColor: Color
)

private fun normalizeStatus(status: String) = status.trim().lowercase().replace(" ", "_")

private fun statusColors(status: String): Pair<Color, Color> = when (normalizeStatus(status)) {
    "completed" -> greentext to greenBg
    "in_progress" -> Primary to Primary.copy(alpha = 0.12f)
    else -> yellowtext to yellowBg
}

private fun formatIso(iso: String): String {
    if (iso.isBlank()) return "—"
    return try {
        val parts = iso.take(10).split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else iso
    } catch (_: Exception) { iso }
}

private fun buildTrialActivityCard(trialDateIso: String?): ActivityCardData? {
    if (trialDateIso.isNullOrBlank()) return null
    return ActivityCardData(
        title = "Scheduled Trial",
        subtitle = "Expected on ${formatIso(trialDateIso)}",
        icon = Icons.Default.Event,
        accentColor = yellowtext,
        bgColor = yellowBg
    )
}

private fun buildStageActivityCards(
    stageGroups: List<OrderViewStageGroup>,
    items: List<OrderViewGarmentItem>
): List<ActivityCardData> {
    val nameMap = items.associateBy({ it.id }, { it.categoryName })
    return stageGroups.flatMap { group ->
        val garmentName = nameMap[group.garmentItemId] ?: "Garment"
        group.stages.filter { normalizeStatus(it.status) == "completed" }.map { stage ->
            ActivityCardData(
                title = "${stage.stageName.uppercase()} Completed",
                subtitle = "Item: $garmentName",
                icon = Icons.Default.CheckCircle,
                accentColor = greentext,
                bgColor = greenBg
            )
        }
    }
}