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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.OrderViewUiState
import com.cuso.mobile.viewmodel.OrderViewViewModel
import com.cuso.mobile.viewmodel.StageUpdateState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Primary = Color(0xFF3B3BF9)
private val TextDark = Color(0xFF111827)

/**
 * Main Order Detail Screen providing a deep view into specific order progress,
 * garment stages, and historical activity.
 */
@Composable
fun OrderDetailScreen(
    orderId: String,
    onClose: () -> Unit,
    onEditOrder: () -> Unit = {}
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
        TitleBar("Order Management", onClose = onClose)
        HorizontalDivider(color = Color(0xFFF0F0F0))

        when (val state = uiState) {
            is OrderViewUiState.Loading, OrderViewUiState.Idle -> {
                ListSkeleton()
            }
            is OrderViewUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = Color.Red, fontSize = tokens.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.getOrdersView(orderId) }) {
                            Text("Retry", fontSize = tokens.bodyMedium)
                        }
                    }
                }
            }
            is OrderViewUiState.Success -> {
                OrderDetailContent(
                    orderId = orderId,
                    data = state.data,
                    onEditOrder = onEditOrder,
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

    var fabricExpanded by remember { mutableStateOf(false) }
    var measurementsExpanded by remember { mutableStateOf(false) }

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
        // Customer and Order Meta Info
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.customerId?.name ?: "—", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextDark)
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
                InfoColumn("Garment Type", order.branch?.name ?: "—")
                InfoColumn("Order Date", formatIso(order.orderDate ?: ""))
                InfoColumn("Delivery Date", formatIso(order.deliveryDate ?: ""))
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
                    Text("Edit Order", fontWeight = FontWeight.SemiBold, fontSize = tokens.bodySmall)
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

        // Garment Navigation chips
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

        // Adaptive Fabric Details Accordion
        AccordionSection(
            title = "Fabric & Material Details",
            icon = Icons.Default.Description,
            expanded = fabricExpanded,
            onHeaderClick = { fabricExpanded = !fabricExpanded }
        ) {
            currentItem?.let { item ->
                item.fabricDetails?.let { fabric ->
                    LabelValueRow("Fabric Type", fabric.fabricType.ifBlank { "—" })
                    LabelValueRow("Color", fabric.color.ifBlank { "—" })
                    LabelValueRow("Pattern", fabric.pattern.ifBlank { "—" })
                    LabelValueRow("Source", fabric.fabricSource.ifBlank { "—" })
                    LabelValueRow("Trial Required", if (item.trialRequired) "Yes" else "No")
                    LabelValueRow("Stitching Charge", "₹${item.stitchingCharge}")
                    LabelValueRow("Quantity", "${item.quantity}")
                }
            }
        }

        // Adaptive Measurements Accordion
        AccordionSection(
            title = "Measurements",
            icon = Icons.Default.Straighten,
            expanded = measurementsExpanded,
            onHeaderClick = { measurementsExpanded = !measurementsExpanded }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding * 0.75f)) {
                    val measurements = currentItem?.measurementSnapshot
                    if (!measurements.isNullOrEmpty()) {
                        measurements.forEach { (fieldName, measurementValues) ->
                            val value = measurementValues.value.joinToString(", ")
                            val unit = measurementValues.unit ?: ""
                            LabelValueRow(fieldName, if (value.isNotBlank()) "$value $unit" else "—")
                        }
                    } else {
                        Text("No specific measurement data recorded.", fontSize = tokens.bodySmall, color = mutedText)
                    }
                }
            }
        }

        // Garment Progress and Quick-Action Status Control
        Spacer(Modifier.height(24.dp))
        currentItem?.let { item ->
            val totalStages = currentStageGroup?.stages?.size ?: 0
            val completedStages = currentStageGroup?.stages?.count {
                normalizeStatus(stageStatusOverrides[it.id] ?: it.status) == "completed"
            } ?: 0

            Column(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.categoryName, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("$completedStages/$totalStages Stages Completed", fontSize = tokens.caption, color = Primary)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (totalStages > 0) completedStages.toFloat() / totalStages else 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                    color = Primary,
                    trackColor = Color(0xFFE5E7EB)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Production Stage Management Card
        currentStage?.let { stage ->
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
            val isUnlocked = stageIndex == unlockedStageIndex
            val committedStatus = stageStatusOverrides[stage.id] ?: stage.status
            val isCommittedCompleted = normalizeStatus(committedStatus) == "completed"
            val isUpdating = (stageUpdateState as? StageUpdateState.Loading)?.stageId == stage.id
            val buttonEnabled = isAssigned && isUnlocked && selectedStatus != committedStatus && !isUpdating

            Surface(
                modifier = Modifier.padding(tokens.screenPadding),
                shape = RoundedCornerShape(16.dp),
                color = whiteBg,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stage.stageName.replaceFirstChar { it.uppercase() }, fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold)
                            val workers = stage.assignedTo.joinToString { "${it.firstName} ${it.lastName}" }
                            Text(workers.ifBlank { "Unassigned" }, fontSize = tokens.bodySmall, color = if(isAssigned) Primary else Color.Red)
                        }
                        CustomStatusDropdown(
                            selectedStatus = selectedStatus,
                            expanded = statusExpanded,
                            enabled = isUnlocked && !isCommittedCompleted && !isUpdating,
                            onExpandChange = { statusExpanded = it },
                            onStatusSelected = { selectedStatus = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        BasicTextField(
                            value = stageNotes,
                            onValueChange = { if(isUnlocked) stageNotes = it },
                            enabled = isUnlocked,
                            textStyle = TextStyle(fontSize = tokens.bodySmall, color = TextDark),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (stageNotes.isEmpty()) Text("Add stage notes...", fontSize = tokens.bodySmall, color = mutedText)
                                inner()
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            overviewViewModel.updateStage(orderId, currentItem!!.id, stage.id, stage.stageName, normalizeStatus(selectedStatus))
                        },
                        enabled = buttonEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (buttonEnabled) Primary else Color(0xFFF3F4F6))
                    ) {
                        if (isUpdating) CirculerProgressIndicatorSmall()
                        else Text(if(isCommittedCompleted) "Completed" else "Update Status", fontSize = tokens.bodySmall)
                    }

                    Spacer(Modifier.height(12.dp))
                    // Step Navigation within stages
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { if(stageIndex > 0) stageIndex-- }, enabled = stageIndex > 0) {
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                        IconButton(onClick = { stageIndex++ }, enabled = stageIndex < (currentStageGroup?.stages?.size ?: 0) - 1) {
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }
        }

        // Activity and Logs Section
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Text("Activity & History", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (stageActivityCards.isEmpty() && trialCard == null) {
                Text("No activity logs available for this order.", fontSize = tokens.caption, color = mutedText)
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
private fun InfoColumn(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(label, fontSize = tokens.label, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextDark)
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
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextDark)
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
                Text(data.title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold)
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
    Box {
        Surface(
            modifier = Modifier.clickable(enabled = enabled) { onExpandChange(!expanded) },
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if(enabled) Primary.copy(0.3f) else Color(0xFFE5E7EB)),
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(selectedStatus.replaceFirstChar { it.uppercase() }, fontSize = tokens.caption, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
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
        color = if (isSelected) Primary else Color(0xFFF3F4F6)
    ) {
        Text(
            label,
            fontSize = tokens.caption,
            color = if (isSelected) Color.White else TextDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
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
        accentColor = Color(0xFFF59E0B),
        bgColor = Color(0xFFFEF3C7)
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
                accentColor = Color(0xFF22C55E),
                bgColor = Color(0xFFDBFCE7)
            )
        }
    }
}