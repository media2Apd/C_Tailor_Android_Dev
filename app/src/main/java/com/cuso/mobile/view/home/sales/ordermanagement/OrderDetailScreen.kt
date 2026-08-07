
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.OrderViewData
import com.cuso.mobile.model.sales.OrderViewGarmentItem
import com.cuso.mobile.model.sales.OrderViewStageGroup
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.OrderViewUiState
import com.cuso.mobile.viewmodel.OrderViewViewModel
import com.cuso.mobile.viewmodel.StageUpdateState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Primary = Color(0xFF3B3BF9)
  private val TextDark = Color(0xFF111827)

@Composable
fun OrderDetailScreen(
    orderId: String,
    onClose: () -> Unit,
    onEditOrder: () -> Unit = {}
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TitleBar("Orders Management", onClose = onClose)

        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        when (val state = uiState) {
            is OrderViewUiState.Loading, OrderViewUiState.Idle -> {
                ListSkeleton()

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

private fun normalizeStatus(status: String) = status.trim().lowercase().replace(" ", "_")

@Composable
private fun OrderDetailContent(
    orderId: String,
    data: OrderViewData,
    onEditOrder: () -> Unit = {},
    overviewViewModel: OrderOverviewViewModel,
    stageUpdateState: StageUpdateState,
    onRefresh: () -> Unit
) {
    val order = data.order
    var garmentIndex by remember { mutableIntStateOf(0) }
    val currentItem = data.items.getOrNull(garmentIndex)
    val currentStageGroup = currentItem?.let { item -> data.stages.find { it.garmentItemId == item.id } }

    var stageIndex by remember(garmentIndex) { mutableIntStateOf(0) }
    val currentStage = currentStageGroup?.stages?.getOrNull(stageIndex)

    var fabricExpanded by remember { mutableStateOf(false) }
    var measurementsExpanded by remember { mutableStateOf(false) }

    // Tracks locally-applied status per stage (keyed by stage id) so a change in one
    // stage never leaks into another stage's dropdown/card state.
    val stageStatusOverrides = remember(garmentIndex) { mutableStateMapOf<String, String>() }

    // The index of the first stage (for the current garment) that isn't completed yet.
    val unlockedStageIndex by remember(currentStageGroup, stageStatusOverrides.toMap()) {
        derivedStateOf {
            val stages = currentStageGroup?.stages ?: emptyList()
            val firstIncomplete = stages.indexOfFirst { s ->
                normalizeStatus(stageStatusOverrides[s.id] ?: s.status) != "completed"
            }
            if (firstIncomplete == -1) stages.size - 1 else firstIncomplete
        }
    }

    // Order-level activity cards (all garments' completed stages, sorted latest-first)
    val stageActivityCards = remember(data.stages) {
        buildStageActivityCards(data.stages, data.items)
    }
    val trialCard = remember(order.trialDate) {
        buildTrialActivityCard(order.trialDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                    order.customerId?.name ?: "—",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
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

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoColumn("Garment",order.branch?.name?:"-")
            InfoColumn("Order Date", formatIso(order.orderDate ?: ""))
            InfoColumn("Delivery Date", formatIso(order.deliveryDate ?: ""))
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
                Text("Move Delivery", color = whiteBg, fontWeight = FontWeight.SemiBold)
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
            currentItem?.let { item ->
                item.fabricDetails?.let { fabric ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        LabelValueRow("Fabric Type", fabric.fabricType.ifBlank { "—" })
                        LabelValueRow("Color", fabric.color.ifBlank { "—" })
                        LabelValueRow("Pattern", fabric.pattern.ifBlank { "—" })
                        LabelValueRow("Source", fabric.fabricSource.ifBlank { "—" })
                        LabelValueRow("Priority", item.priority.ifBlank { "—" })
                        LabelValueRow("Trial Required", if (item.trialRequired) "Yes" else "No")
                        LabelValueRow("Stitching Charge", "₹${item.stitchingCharge}")
                        LabelValueRow("Quantity", "${item.quantity}")
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))

        // Measurements Section
        ExpandableRow("Measurements", measurementsExpanded) { measurementsExpanded = !measurementsExpanded }
        if (measurementsExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
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
                                Text(fieldName, fontSize = 14.sp, color = mutedText)
                                Text(
                                    if (value.isNotBlank()) "$value $unit" else "—",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chest", fontSize = 14.sp, color = mutedText)
                            Text("— in", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sleeve Length", fontSize = 14.sp, color = mutedText)
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
            val completedStages = currentStageGroup?.stages?.count { stage ->
                normalizeStatus(stageStatusOverrides[stage.id] ?: stage.status) == "completed"
            } ?: 0
            val inProgressStages = currentStageGroup?.stages?.count { stage ->
                normalizeStatus(stageStatusOverrides[stage.id] ?: stage.status) == "in_progress"
            } ?: 0

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
                    Text(item.categoryName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("Progress: $completedStages/$totalStages", fontSize = 13.sp, color = mutedText)
                }
                Spacer(Modifier.height(4.dp))

                val progress = if (totalStages > 0) completedStages.toFloat() / totalStages else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Primary,
                    trackColor = Color(0xFFE5E7EB)
                )

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pending: ${totalStages - completedStages}", fontSize = 12.sp, color = mutedText)
                    Text("In Progress: $inProgressStages", fontSize = 12.sp, color = Color(0xFFF59E0B))
                }

                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { /* TODO: mark garment complete */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Complete Garment", color = whiteBg, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        currentStage?.let { stage ->
            var statusExpanded by remember(stage.id) { mutableStateOf(false) }
            var selectedStatus by remember(stage.id) {
                mutableStateOf(stageStatusOverrides[stage.id] ?: stage.status)
            }
            var stageNotes by remember(stage.id) { mutableStateOf("") }

            LaunchedEffect(stageUpdateState) {
                if (stageUpdateState is StageUpdateState.Success && stageUpdateState.stageId == stage.id) {
                    stageStatusOverrides[stage.id] = selectedStatus
                    overviewViewModel.resetStageUpdateState()
                    onRefresh()
                } else if (stageUpdateState is StageUpdateState.Error && stageUpdateState.stageId == stage.id) {
                    overviewViewModel.resetStageUpdateState()
                }
            }

            val isAssigned = stage.assignedTo.isNotEmpty()
            val isUnlocked = stageIndex == unlockedStageIndex
            val isCardEnabled = isAssigned && isUnlocked

            val committedStatus = stageStatusOverrides[stage.id] ?: stage.status
            val hasChanges = selectedStatus != committedStatus
            val normalizedSelected = normalizeStatus(selectedStatus)

            val isCommittedCompleted = normalizeStatus(committedStatus) == "completed"

            val isUpdating = (stageUpdateState as? StageUpdateState.Loading)?.stageId == stage.id

            val cardBorderColor = when (normalizedSelected) {
                "in_progress" -> Color(0xFF3B82F6)
                "completed" -> Color(0xFF22C55E)
                else -> Color.Transparent
            }

                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    stage.stageName.replaceFirstChar { it.uppercase() },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                if (isAssigned) {
                                    Text(
                                        stage.assignedTo.joinToString(", ") { worker ->
                                            "${worker.firstName} ${worker.lastName}"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary
                                    )
                                } else {
                                    Text("Not assigned", fontSize = 13.sp, color = mutedText)
                                }
                                Text(
                                    "Qty: ${stage.completedQuantity} / ${stage.assignedQuantity}",
                                    fontSize = 12.sp,
                                    color = mutedText
                                )
                            }
                        }

                        CustomStatusDropdown(
                            selectedStatus = selectedStatus,
                            expanded = statusExpanded,
                            enabled = isCardEnabled && !isCommittedCompleted && !isUpdating,
                            onExpandChange = { if (isCardEnabled && !isCommittedCompleted && !isUpdating) statusExpanded = it },
                            onStatusSelected = { status -> selectedStatus = status }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = stageNotes,
                            onValueChange = { if (isCardEnabled) stageNotes = it },
                            enabled = isCardEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp, color = TextDark),
                            cursorBrush = SolidColor(Primary),
                            decorationBox = { inner ->
                                if (stageNotes.isEmpty()) Text("Add stage notes...", fontSize = 13.sp, color = mutedText)
                                inner()
                            }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    val buttonText = when {
                        !isAssigned -> "Not Assigned"
                        !isUnlocked -> "Locked"
                        isCommittedCompleted -> "Completed"
                        !hasChanges -> "No Changes"
                        else -> "Quick Update"
                    }
                    val buttonEnabled = isCardEnabled && hasChanges && !isUpdating

                    Button(
                        onClick = {
                            val garmentItemId = currentItem.id
                            overviewViewModel.updateStage(
                                orderId = orderId,
                                garmentItemId = garmentItemId,
                                stageId = stage.id,
                                stageName = stage.stageName,
                                status = normalizeStatus(selectedStatus)
                            )
                        },
                        enabled = buttonEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buttonEnabled) Primary else Color(0xFFF3F4F6),
                            disabledContainerColor = Color(0xFFF3F4F6),
                            disabledContentColor = mutedText,
                            contentColor = if (buttonEnabled) whiteBg else mutedText
                        ),
                    ) {
                        if (isUpdating) {
                            CirculerProgressIndicatorReuse()

                        } else {
                            Text(
                                buttonText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (buttonEnabled) whiteBg else mutedText
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

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
                            enabled = stageIndex < (currentStageGroup.stages.size ) - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (stageIndex < (currentStageGroup.stages.size ) - 1)
                                    TextDark
                                else
                                    Color(0xFFD1D5DB)
                            )
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

        val hasAnyActivity = stageActivityCards.isNotEmpty() ||
                trialCard != null ||
                !data.payments.isNullOrEmpty() ||
                data.delivery != null

        if (!hasAnyActivity) {
            Text("No activity yet", fontSize = 13.sp, color = mutedText)
        } else {
            // Trial alert — only shown if trialDate is present in API response
            trialCard?.let { card ->
                ActivityLogCard(card)
                Spacer(Modifier.height(8.dp))
            }

            // Stage completion cards — cutting, stitching, qc, trial (color coded, all garments)
            stageActivityCards.forEach { card ->
                ActivityLogCard(card)
                Spacer(Modifier.height(8.dp))
            }

            // Payments
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

            // Delivery
            data.delivery?.let {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delivery Status: ${it.status ?: "N/A"}", fontSize = 13.sp, color = TextDark)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ---------- Activity Log data + helpers ----------

private data class ActivityCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val bgColor: Color
)

@Composable
private fun ActivityLogCard(data: ActivityCardData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(data.bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, data.accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(data.icon, contentDescription = null, tint = data.accentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(data.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(data.subtitle, fontSize = 12.sp, color = mutedText)
        }
    }
}

// API 25-safe "today" string (yyyy-MM-dd) — avoids java.time.LocalDate which needs API 26+
private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date())
}

// Trial alert card — only built if trialDate is present (non-blank) in the API response.
private fun buildTrialActivityCard(trialDateIso: String?): ActivityCardData? {
    if (trialDateIso.isNullOrBlank()) return null

    val trialDatePart = trialDateIso.take(10) // yyyy-MM-dd
    val todayPart = getTodayDateString()      // yyyy-MM-dd

    return when {
        trialDatePart == todayPart -> ActivityCardData(
            title = "Trial Scheduled Today",
            subtitle = "Trial date: ${formatIso(trialDateIso)}",
            icon = Icons.Default.Warning,
            accentColor = Color(0xFFF59E0B),
            bgColor = Color(0xFFFEF3C7)
        )
        trialDatePart > todayPart -> ActivityCardData(
            title = "Upcoming Trial",
            subtitle = "Trial date: ${formatIso(trialDateIso)}",
            icon = Icons.Default.Event,
            accentColor = Color(0xFF3B82F6),
            bgColor = Color(0xFFDBEAFE)
        )
        else -> ActivityCardData(
            title = "Trial Date Passed",
            subtitle = "Trial date: ${formatIso(trialDateIso)}",
            icon = Icons.Default.EventBusy,
            accentColor = Color(0xFF9CA3AF),
            bgColor = Color(0xFFF3F4F6)
        )
    }
}

// Stage cards — one card per COMPLETED stage (cutting/stitching/qc/trial) across ALL garments,
// color coded by stage name, sorted latest-completed first.
private fun buildStageActivityCards(
    stageGroups: List<OrderViewStageGroup>,
    items: List<OrderViewGarmentItem>
): List<ActivityCardData> {
    val garmentNameById = items.associateBy({ it.id }, { it.categoryName })

    return stageGroups
        .flatMap { group ->
            val garmentName = garmentNameById[group.garmentItemId] ?: "Garment"
            group.stages
                .filter { normalizeStatus(it.status) == "completed" }
                .map { stage -> garmentName to stage }
        }
        .sortedByDescending { (_, stage) -> stage.completedAt ?: "" }
        .map { (garmentName, stage) ->
            val (accent, bg) = stageColors(stage.stageName)
            ActivityCardData(
                title = "${stage.stageName.replaceFirstChar { it.uppercase() }} Completed — $garmentName",
                subtitle = if (!stage.completedAt.isNullOrBlank())
                    "Completed on ${formatIso(stage.completedAt)}"
                else
                    "Completed",
                icon = Icons.Default.CheckCircle,
                accentColor = accent,
                bgColor = bg
            )
        }
}

private fun stageColors(stageName: String): Pair<Color, Color> {
    return when (stageName.trim().lowercase()) {
        "cutting" -> Color(0xFF3B82F6) to Color(0xFFDBEAFE)      // blue
        "stitching" -> Color(0xFF8B5CF6) to Color(0xFFEDE9FE)    // purple
        "qc" -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)           // amber
        "trial" -> Color(0xFF22C55E) to Color(0xFFDBFCE7)        // green
        else -> Color(0xFF6B7280) to Color(0xFFF3F4F6)           // gray fallback
    }
}

@Composable
private fun CustomStatusDropdown(
    selectedStatus: String,
    expanded: Boolean,
    enabled: Boolean = true,
    onExpandChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val statusOptions = listOf("Pending", "In Progress", "Completed")
    val displayText = selectedStatus.replaceFirstChar { it.uppercase() }
    val borderColor = if (enabled) Color(0xFFC7D2FE) else Color(0xFFE5E7EB)
    val textColor = if (enabled) TextDark else mutedText

    Box {
        Row(
            modifier = Modifier
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .background(whiteBg, RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { onExpandChange(!expanded) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(displayText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            containerColor = whiteBg,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(160.dp)
        ) {
            Text(
                "Select an option",
                fontSize = 12.sp,
                color = mutedText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            statusOptions.forEach { status ->
                val isSelected = status.equals(selectedStatus, ignoreCase = true) ||
                        status.replace(" ", "_").equals(selectedStatus, ignoreCase = true)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusSelected(status)
                            onExpandChange(false)
                        }
                        .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        status,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Primary else TextDark
                    )
                }
            }
        }
    }
}


@Composable
private fun GarmentChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Primary else whiteBg,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (isSelected) whiteBg else TextDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = mutedText)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = mutedText)
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
            tint = mutedText
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