@file:Suppress("Unused_variable",
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "Unused_parameter", "VariableNeverRead", "SameParameterValue"
)

package com.cuso.tailor.view.home.services.service_order.production_orders

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.StaffDto
import com.cuso.tailor.model.service.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.viewmodel.SalesViewModel
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── UI Domain Models for the Overview Screen ──

data class ServiceGarmentMeasurement(
    val name: String,
    val value: String,
    val unit: String = "inch"
)
private data class StageColorPalette(
    val circle: Color,
    val bg: Color,
    val border: Color,
    val text: Color
)


data class ServiceGarmentStage(
    val stageNumber: String,
    val stageName: String,
    val description: String,
    val actualDuration: String = "—",
    val estimatedDuration: String = "10 mins",
    val finishedTime: String? = null,
    val status: String = "Completed",
    val assignedWorkerId: String? = null,
    val assignedStaffName: String?,
    val assignedStaffCode: String?,
    val assignedStaffRole: String? = null,
    val activeTasks: Int = 0,
    val workloadPercentage: Int = 0,
    val isCompleted: Boolean = true,
    val isAssigned: Boolean = false
)

data class ServiceGarmentDetail(
    val id: String,
    val jobCardCode: String,
    val type: String,
    val quantity: Int,
    val completedQuantity: Int,
    val price: Double,
    val priority: String,
    val trialRequired: Boolean,
    val complexity: String,
    val selectedModels: List<String>,
    val fabricSource: String,
    val pattern: String,
    val status: String,
    val currentStageName: String,
    val notes: String,
    val deadline: String = "—",
    val customerName: String = "—",
    val customerPhone: String = "—",
    val orderCode: String = "—",
    val totalEstimatedEffort: String = "1 hrs",
    val measurements: List<ServiceGarmentMeasurement> = emptyList(),
    val stagesList: List<ServiceGarmentStage> = emptyList(),
    val assignment: ServiceGarmentAssignment? = null
)

data class ServiceGarmentAssignment(
    val cuttingTailor: String? = null,
    val stitchingTailor: String? = null,
    val qualityInspector: String? = null,
    val primaryArtisan: String? = null,
    val trial: String? = null,
    val priority: String = "Medium",
    val startDate: String? = null,
    val completionDate: String? = null,
    val isAssigned: Boolean = false
)

data class ServicePaymentInfo(
    val orderId: String,
    val status: String,
    val paymentMode: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double
) {
    val remainingAmount: Double get() = balanceAmount
    val completionPercent: Int
        get() = if (totalAmount <= 0.0) 0 else ((paidAmount / totalAmount) * 100).toInt().coerceIn(0, 100)
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ServiceOrderOverviewScreen(
    orderId: String,
    onClose: () -> Unit = {},
    onCreateNew: () -> Unit = {},
    // When set, the screen opens Assign Garment Production for this job card directly
    initialAssignJobCardId: String? = null,
    // Called after workers are routed successfully while in assignment mode
    onAssignmentRouted: () -> Unit = {},
    viewModel: ServicesViewModel = hiltViewModel(),
    salesViewModel: SalesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val orderDetail by viewModel.serviceOrderDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingOrderDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.orderDetailError.collectAsStateWithLifecycle()

    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val specialistWorkloadList by viewModel.specialistWorkloadList.collectAsStateWithLifecycle()

    // Fetch order details, staff, and specialist workload
    LaunchedEffect(orderId) {
        viewModel.fetchServiceOrderDetail(orderId)
        viewModel.fetchSpecialistWorkload()
        salesViewModel.fetchStaff()
    }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val jobCardDetailData by viewModel.jobCardDetail.collectAsStateWithLifecycle()
    val isLoadingJobCard by viewModel.isLoadingJobCardDetail.collectAsStateWithLifecycle()

    // State for viewing Work (Production Stages screen)
    var activeJobCardId by remember { mutableStateOf<String?>(null) }
    var activeAssignmentGarment by remember { mutableStateOf<ServiceGarmentDetail?>(null) }

    // State for viewing Assignment (Assign Garment Production screen)
    var activeAssignProductionGarment by remember { mutableStateOf<ServiceGarmentDetail?>(null) }

    // True when this screen was opened from Production Details in assignment mode
    val isAssignmentMode = initialAssignJobCardId != null
    // Becomes true once the target garment has been opened (or could not be found)
    var autoOpenedAssignment by remember { mutableStateOf(false) }
    var selectedGarmentForMeasurements by remember { mutableStateOf<ServiceGarmentDetail?>(null) }
    var measurementSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var measurementSheetBlur by remember { mutableStateOf(0.dp) }
    var measurementSheetScrim by remember { mutableFloatStateOf(0f) }

    fun closeMeasurementSheet() {
        measurementSheetState = SheetValue.Hidden
        measurementSheetBlur = 0.dp
        measurementSheetScrim = 0f
        selectedGarmentForMeasurements = null
    }

    var paymentSheetBlur by remember { mutableStateOf(0.dp) }
    var paymentSheetScrim by remember { mutableFloatStateOf(0f) }
    var paymentSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val isPaymentSheetOpen = paymentSheetState != SheetValue.Hidden

    fun closePaymentSheet() {
        paymentSheetState = SheetValue.Hidden
        paymentSheetBlur = 0.dp
        paymentSheetScrim = 0f
    }

    var selectedTab by remember { mutableStateOf("Overview") }
    // Replaced "Payment" tab with "Timeline"
    val tabs = listOf("Overview", "Garments", "Assignments", "Timeline")
    var selectedGarmentForSheet by remember { mutableStateOf<ServiceGarmentDetail?>(null) }

    var assignMethodSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var assignMethodSheetBlur by remember { mutableStateOf(0.dp) }
    var assignMethodSheetScrim by remember { mutableFloatStateOf(0f) }

    var assignTailorsSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var assignTailorsSheetBlur by remember { mutableStateOf(0.dp) }
    var assignTailorsSheetScrim by remember { mutableFloatStateOf(0f) }

    val isAssignMethodSheetOpen = assignMethodSheetState != SheetValue.Hidden
    val isAssignTailorsSheetOpen = assignTailorsSheetState != SheetValue.Hidden
    val isMeasurementSheetOpen = measurementSheetState != SheetValue.Hidden

    fun closeAllAssignSheets() {
        assignMethodSheetState = SheetValue.Hidden
        assignMethodSheetBlur = 0.dp
        assignMethodSheetScrim = 0f

        assignTailorsSheetState = SheetValue.Hidden
        assignTailorsSheetBlur = 0.dp
        assignTailorsSheetScrim = 0f

        selectedGarmentForSheet = null
    }

    val combinedSheetBlur = maxOf(assignMethodSheetBlur, assignTailorsSheetBlur, paymentSheetBlur, measurementSheetBlur)
    val combinedSheetScrim = maxOf(assignMethodSheetScrim, assignTailorsSheetScrim, paymentSheetScrim, measurementSheetScrim)
    val isAnySheetOpen = isAssignMethodSheetOpen || isAssignTailorsSheetOpen || isPaymentSheetOpen || isMeasurementSheetOpen

    // Handle back button navigation for both screens and sheets
    val isViewingWork = activeJobCardId != null && activeAssignmentGarment != null
    val isViewingAssignmentProduction = activeAssignProductionGarment != null

    BackHandler(enabled = isViewingWork || isViewingAssignmentProduction || isAnySheetOpen) {
        when {
            isViewingWork -> {
                activeJobCardId = null
                activeAssignmentGarment = null
                viewModel.clearJobCardDetail()
            }
            isViewingAssignmentProduction -> {
                // In assignment mode go back to Production Details instead of the overview tabs
                if (isAssignmentMode) onClose() else activeAssignProductionGarment = null
            }
            isMeasurementSheetOpen -> closeMeasurementSheet()
            isAssignTailorsSheetOpen -> {
                assignTailorsSheetState = SheetValue.Hidden
                assignMethodSheetState = SheetValue.Collapsed
            }
            isAssignMethodSheetOpen -> closeAllAssignSheets()
            isPaymentSheetOpen -> closePaymentSheet()
        }
    }

    val garments: List<ServiceGarmentDetail> = remember(orderDetail) {
        orderDetail?.jobCards?.map { card ->
            extractGarmentDetailFromJobCard(card, orderDetail?.orderHeader)
        } ?: emptyList()
    }

    val paymentInfo: ServicePaymentInfo? = remember(orderDetail) {
        orderDetail?.orderHeader?.let { extractPaymentInfo(it) }
    }

    // Opens Assign Garment Production for the requested job card as soon as garments are loaded
    LaunchedEffect(garments, isLoading, initialAssignJobCardId) {
        if (initialAssignJobCardId == null || autoOpenedAssignment) return@LaunchedEffect

        val match = garments.firstOrNull { it.id == initialAssignJobCardId }
        when {
            match != null -> {
                activeAssignProductionGarment = match
                autoOpenedAssignment = true
            }
            // Data finished loading but the garment was not found: fall back to the normal tabs
            !isLoading && garments.isNotEmpty() -> autoOpenedAssignment = true
            // Loading failed: fall back to the normal screen so the retry UI is visible
            !isLoading && errorMessage != null -> autoOpenedAssignment = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            // 1. Navigate to Production Stages screen when "View Work" is clicked
            isViewingWork -> {
                JobCardViewOneScreen(
                    garment = activeAssignmentGarment!!,
                    jobCardData = jobCardDetailData,
                    isLoading = isLoadingJobCard,
                    onBack = {
                        activeJobCardId = null
                        activeAssignmentGarment = null
                        viewModel.clearJobCardDetail()
                    },
                    onTrialRework = { selectedStageIds, notes ->
                        successMessage = "Rework routed for ${selectedStageIds.size} stage(s) successfully"
                    }
                )
            }

            // 2. Navigate to Assign Garment Production screen when "View Assignment" is clicked
            isViewingAssignmentProduction -> {
                val isAllocating by viewModel.isAllocatingWorkers.collectAsStateWithLifecycle()

                AssignGarmentProductionScreen(
                    garment = activeAssignProductionGarment!!,
                    specialistWorkloadList = specialistWorkloadList,
                    isAllocating = isAllocating,
                    onBack = {
                        // In assignment mode go back to Production Details
                        if (isAssignmentMode) onClose() else activeAssignProductionGarment = null
                    },
                    onRouteToWorkshop = { updatedStages ->
                        val targetGarment = activeAssignProductionGarment ?: return@AssignGarmentProductionScreen

                        // Map updated stages to the API assignment payload items
                        val assignments = updatedStages.mapNotNull { stage ->
                            val workerId = stage.assignedWorkerId ?: return@mapNotNull null
                            val estimatedMinutes = stage.estimatedDuration
                                .replace(Regex("[^0-9]"), "")
                                .toIntOrNull() ?: 0

                            AllocateWorkersStageItem(
                                stageId = stage.stageNumber, // Maps stage ID
                                workerId = workerId,
                                allocatedQuantity = targetGarment.quantity,
                                estimatedMinutes = estimatedMinutes
                            )
                        }

                        if (assignments.isEmpty()) {
                            localErrorMessage = "Please assign at least one worker before routing"
                            return@AssignGarmentProductionScreen
                        }

                        viewModel.allocateWorkersToWorkshop(
                            jobCardId = targetGarment.id,
                            assignments = assignments,
                            forceReassign = false,
                            replaceExisting = true,
                            onSuccess = { msg ->
                                successMessage = msg
                                viewModel.fetchServiceOrderDetail(orderId)
                                if (isAssignmentMode) {
                                    // Route done: return to Production Details (it refreshes itself on open)
                                    onAssignmentRouted()
                                } else {
                                    activeAssignProductionGarment = null
                                }
                            },
                            onError = { err ->
                                localErrorMessage = err
                            }
                        )
                    }
                )
            }

            // Waiting for the assignment screen to open directly (assignment mode only)
            isAssignmentMode && !autoOpenedAssignment -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CirculerProgressIndicatorReuse()
                }
            }

            // 3. Default Order Overview tabs screen
            else -> {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        TitleBar(
                            title = "Order Details",
                            onClose = onClose
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .blurScrim(radius = combinedSheetBlur)
                        ) {
                            Spacer(Modifier.padding(top = 10.dp))
                            AppUnderlineTabRow(
                                tabs = tabs,
                                selectedIndex = tabs.indexOf(selectedTab),
                                onTabSelected = { index -> selectedTab = tabs[index] },
                                isScrollable = true
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blurScrim(combinedSheetBlur)
                                        .graphicsLayer {
                                            alpha = 1f - (combinedSheetScrim * 0.2f)
                                        }
                                ) {
                                    when {
                                        isLoading -> {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                CirculerProgressIndicatorReuse()
                                            }
                                        }

                                        errorMessage != null && orderDetail == null -> {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "Failed to load service order",
                                                        color = redText,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = tokens.bodyMedium
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = errorMessage ?: "Unknown error",
                                                        color = mutedText,
                                                        fontSize = tokens.caption
                                                    )
                                                    Spacer(Modifier.height(tokens.extraPadding))
                                                    Button(
                                                        onClick = { viewModel.fetchServiceOrderDetail(orderId) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                                                        modifier = Modifier.height(tokens.buttonHeight)
                                                    ) {
                                                        Text(
                                                            text = "Retry",
                                                            color = whiteBg,
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        orderDetail != null -> {
                                            val data = orderDetail!!
                                            when (selectedTab) {
                                                "Overview" -> ServiceOverviewTab(data)
                                                "Garments" -> ServiceGarmentsTab(
                                                    garments = garments,
                                                    onGoToAssignments = { selectedTab = "Assignments" },
                                                    onViewMeasurements = { garment ->
                                                        selectedGarmentForMeasurements = garment
                                                        measurementSheetState = SheetValue.Collapsed
                                                    },
                                                    // "View Assignment" -> opens Assign Garment Production Screen
                                                    onViewAssignment = { garment ->
                                                        activeAssignProductionGarment = garment
                                                    },
                                                    // Assign Worker -> opens the Method Selection Sheet first
                                                    onAssignWorker = { garment ->
                                                        selectedGarmentForSheet = garment
                                                        assignMethodSheetState = SheetValue.Collapsed
                                                    },
                                                    // "View Work" -> opens Production Stages Screen
                                                    onViewWork = { garment ->
                                                        activeJobCardId = garment.id
                                                        activeAssignmentGarment = garment
                                                        viewModel.fetchJobCardDetail(garment.id)
                                                    }
                                                )
                                                "Assignments" -> ServiceAssignmentsTab(
                                                    garments = garments,
                                                    onAssignClick = { garment ->
                                                        selectedGarmentForSheet = garment
                                                        assignMethodSheetState = SheetValue.Collapsed
                                                    }
                                                )
                                                // Dynamic Order Timeline Tab
                                                "Timeline" -> ServiceTimelineTab(
                                                    orderData = data,
                                                    garments = garments
                                                )
                                            }
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 16.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = !isAnySheetOpen,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        StepNavigationFab(
                                            showBack = true,
                                            showBackArrow = false,
                                            showTrailingArrow = false,
                                            onBack = onClose,
                                            backEnabled = true,
                                            backLabel = "Back",
                                            backWidthFraction = 0.30f,
                                            trailingWidthFraction = 0.35f,
                                            trailingAction = TrailingFabAction.Next(
                                                label = "Create New",
                                                onClick = onCreateNew
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── Measurements Detail Sheet ──
                        if (selectedGarmentForMeasurements != null) {
                            GarmentMeasurementsSheet(
                                garment = selectedGarmentForMeasurements!!,
                                sheetState = measurementSheetState,
                                onStateChange = { measurementSheetState = it },
                                onBlurScrimChange = { r, sc -> measurementSheetBlur = r; measurementSheetScrim = sc },
                                onDismiss = { closeMeasurementSheet() }
                            )
                        }

                        // ── Assignment Method Selection Sheet (Individual and Team Selection) ──
                        AssignMethodSelectionSheet(
                            garment = selectedGarmentForSheet,
                            sheetState = assignMethodSheetState,
                            onStateChange = { assignMethodSheetState = it },
                            onBlurScrimChange = { r, sc -> assignMethodSheetBlur = r; assignMethodSheetScrim = sc },
                            topInset = 66.dp,
                            onDismiss = { closeAllAssignSheets() },
                            onMethodSelected = { _ ->
                                // Route directly to Assign Garment Production sequencer screen
                                activeAssignProductionGarment = selectedGarmentForSheet
                                closeAllAssignSheets()
                            }
                        )

                        // ── Production Tailors Dropdowns Sheet ──
                        AssignServiceTailorsSheet(
                            garment = selectedGarmentForSheet,
                            staffList = staffList,
                            orderId = orderId,
                            sheetState = assignTailorsSheetState,
                            onStateChange = { assignTailorsSheetState = it },
                            onBlurScrimChange = { r, sc -> assignTailorsSheetBlur = r; assignTailorsSheetScrim = sc },
                            topInset = 66.dp,
                            onDismiss = { closeAllAssignSheets() },
                            onSuccess = { msg ->
                                successMessage = msg
                                viewModel.fetchServiceOrderDetail(orderId)
                                closeAllAssignSheets()
                            },
                            onError = { msg -> localErrorMessage = msg }
                        )

                        // ── Receive Payment Sheet ──
                        if (paymentInfo != null) {
                            ReceiveServicePaymentSheet(
                                orderId = orderDetail?.orderHeader?.id ?: orderId,
                                balanceDue = paymentInfo.remainingAmount,
                                sheetState = paymentSheetState,
                                onStateChange = { paymentSheetState = it },
                                onBlurChange = { paymentSheetBlur = it },
                                onDismiss = { closePaymentSheet() },
                                onPaymentSuccess = {
                                    successMessage = "Payment recorded successfully"
                                    closePaymentSheet()
                                    viewModel.fetchServiceOrderDetail(orderId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun JobCardViewOneScreen(
    garment: ServiceGarmentDetail,
    jobCardData: JobCardViewOneData?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onTrialRework: (selectedStageIds: Set<String>, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    // State controlling the Rework Routing Dialog visibility
    var showReworkDialog by remember { mutableStateOf(false) }

    val orderCode = jobCardData?.orderHeader?.orderCode ?: garment.orderCode
    val jobCardCode = jobCardData?.jobCardCode ?: garment.jobCardCode
    val customerName = jobCardData?.customerSnapshot?.name ?: garment.customerName
    val customerPhone = jobCardData?.customerSnapshot?.phone ?: garment.customerPhone
    val deliveryDate = jobCardData?.orderHeader?.dueDate?.let { formatDisplayFullDate(it) } ?: garment.deadline
    val priority = jobCardData?.priority ?: garment.priority
    val status = jobCardData?.status ?: garment.status
    val totalQty = jobCardData?.totalQuantity ?: garment.quantity
    val completedQty = jobCardData?.completedQuantity ?: garment.completedQuantity
    val garmentTitle = jobCardData?.garment?.name ?: garment.type
    val currentStage = jobCardData?.currentStageName ?: garment.currentStageName
    val fabricNotes = jobCardData?.garment?.fabricNotes?.ifBlank { "No special instructions provided." }
        ?: garment.notes.ifBlank { "No special instructions provided." }
    val fabricSource = jobCardData?.garment?.fabricSource?.replace("_", " ") ?: garment.fabricSource

    val stages = if (jobCardData != null && jobCardData.stages.isNotEmpty()) {
        jobCardData.stages.mapIndexed { idx, st ->
            val worker = st.allocations.firstOrNull()?.workerId
            val name = worker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() } ?: ""
            val timeSpent = if ((st.timeSpentMinutes ?: 0) > 0) "Time spent: ${st.timeSpentMinutes} min" else null
            ServiceGarmentStage(
                stageNumber = "${idx + 1}",
                stageName = st.stageName ?: "",
                description = "",
                assignedStaffName = name,
                assignedStaffCode = timeSpent,
                finishedTime = st.completedAt?.let { formatDisplayTime(it) } ?: "",
                isCompleted = st.status.equals("Completed", true)
            )
        }
    } else {
        garment.stagesList
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Work Details", onClose = onBack
            )
        }
    ) { padding ->
        if (isLoading) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                // ── SCREENSHOT 1: Order Specs Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .background(yellowBg)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(text = priority, fontSize = tokens.caption, color = yellowText, fontWeight = FontWeight.Medium)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .background(primary_light)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(text = status, fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Text(text = customerName, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = customerPhone, fontSize = tokens.caption, color = mutedText)

                        Spacer(Modifier.height(tokens.screenPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Delivery", fontSize = tokens.caption, color = mutedText)
                                Spacer(Modifier.height(2.dp))
                                Text(deliveryDate, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Quantity", fontSize = tokens.caption, color = mutedText)
                                Spacer(Modifier.height(2.dp))
                                Text("$completedQty / $totalQty pcs", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                        }
                    }
                }

                // ── SCREENSHOT 1: Production Stages Stepper Card ──
                // ==================== AFTER (100% Dynamic, Screenshot Design & colors.kt Theme) ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        // Header Row: Title and Top-Right Current Stage Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Production Stages Progression",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Garment: $garmentTitle ($jobCardCode)",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }

                            // Current Stage Badge
                            if (currentStage.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(primary_light)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Current: $currentStage",
                                        fontSize = tokens.caption,
                                        color = Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.screenPadding))

                        // Dynamic Stages Timeline from API response
                        val stagesList = jobCardData?.stages ?: emptyList()

                        stagesList.forEachIndexed { index, stage ->
                            val isLast = index == stagesList.lastIndex

                            // Status colors dynamically resolved from colors.kt
                            // ==================== AFTER (Resolved using Data Class) ====================


// 2. Destructure cleanly with StageColorPalette
                            val (circleColor, badgeBgColor, badgeBorderColor, badgeTextColor) = when (stage.status?.lowercase()) {
                                "rework" -> StageColorPalette(
                                    circle = redText,
                                    bg = roseBg,
                                    border = redBg,
                                    text = redText
                                )
                                "completed" -> StageColorPalette(
                                    circle = complete_button_bg,
                                    bg = greenBg,
                                    border = darkGreenBg,
                                    text = greentext
                                )
                                "in_progress", "started" -> StageColorPalette(
                                    circle = chartBlue,
                                    bg = primary_light,
                                    border = light_blue_border,
                                    text = blueText
                                )
                                else -> StageColorPalette(
                                    circle = iconMuted,
                                    bg = light_grey,
                                    border = BorderGray,
                                    text = TextSecondary
                                )
                            }

                            // Worker assignment details from API allocations
                            val firstAllocation = stage.allocations.firstOrNull()
                            val workerName = firstAllocation?.workerId?.let { worker ->
                                listOfNotNull(worker.firstName, worker.lastName).joinToString(" ").trim()
                            }.takeIf { !it.isNullOrBlank() }

                            val timeSpent = stage.timeSpentMinutes ?: 0
                            val assignedText = buildString {
                                if (!workerName.isNullOrBlank()) {
                                    append("Assigned to: $workerName")
                                }
                                if (timeSpent > 0) {
                                    if (isNotEmpty()) append(" • ")
                                    append("Time Spent: $timeSpent mins")
                                }
                            }

                            // Formatted start time from startedAt
                            val formattedStartedAt = formatDisplayTimeOnly(stage.startedAt)

                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Stepper Indicator Column (Outer Circle + Inner Ring + Vertical Connector Line)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(circleColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, whiteBg, CircleShape)
                                        )
                                    }

                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(52.dp)
                                                .background(sectionBorder)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                // Stage Info: Name, Status Badge, Worker, Started Time
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = if (isLast) 0.dp else 14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = stage.stageName ?: "Stage",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = title_color
                                        )

                                        val statusLabel = stage.status
                                        if (!statusLabel.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badgeBgColor)
                                                    .border(1.dp, badgeBorderColor, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = statusLabel.replace("_", " "),
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.Medium,
                                                    color = badgeTextColor
                                                )
                                            }
                                        }
                                    }

                                    if (assignedText.isNotBlank()) {
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = assignedText,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = TextSecondary
                                        )
                                    }

                                    if (!formattedStartedAt.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Started: $formattedStartedAt",
                                            fontSize = tokens.label,
                                            color = iconMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── SCREENSHOT 1: Trial & Rework Routing Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Need alterations or client trial corrections?",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        OutlinedButton(
                            onClick = { showReworkDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            border = BorderStroke(1.dp, redText),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = redText, modifier = Modifier.size(tokens.iconSize))
                            Spacer(Modifier.width(6.dp))
                            Text(text = "Trial & Rework Routing", color = redText, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                        }

// Show the Trial & Rework Routing Modal Dialog
                        if (showReworkDialog) {
                            TrialReworkRoutingDialog(
                                stages = jobCardData?.stages ?: emptyList(),
                                onDismiss = { showReworkDialog = false },
                                onConfirm = { selectedStages, notes ->
                                    showReworkDialog = false
                                    onTrialRework(selectedStages, notes)
                                }
                            )
                        }
                    }
                }

                // ── SCREENSHOT 2: Current Active Bench Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Text(text = "CURRENT ACTIVE BENCH", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                        Spacer(Modifier.height(tokens.extraPadding))
                        Text(text = "Current Stage", fontSize = tokens.caption, color = mutedText)
                        Text(text = currentStage, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)

                        Spacer(Modifier.height(tokens.extraPadding))
                        Text(text = "Assigned Artisan", fontSize = tokens.caption, color = mutedText)
                        Text(text = garment.assignment?.primaryArtisan ?: "Vicky Sakthi", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)

                        Spacer(Modifier.height(tokens.extraPadding))
                        Text(text = "Cutting / Fabric Notes", fontSize = tokens.caption, color = mutedText)
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = Primary_background,
                            border = BorderStroke(1.dp, light_grey)
                        ) {
                            Text(
                                text = "\"$fabricNotes\"",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                modifier = Modifier.padding(tokens.extraPadding)
                            )
                        }
                    }
                }

                // ── SCREENSHOT 2 & 3: Activity Log Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Text(text = "ACTIVITY LOG", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                        Spacer(Modifier.height(tokens.screenPadding))

                        stages.forEach { stage ->
                            // Completion log item with green circle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(complete_button_bg)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${stage.stageName} stage completed",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = stage.finishedTime ?: "14 Oct, 07:40 PM",
                                fontSize = tokens.caption,
                                color = mutedText,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 8.dp)
                            )

                            // Allocation log item with purple circle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(purpleText)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Assigned to ${stage.assignedStaffName} (${stage.stageName})",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = stage.finishedTime ?: "14 Oct, 07:40 PM",
                                fontSize = tokens.caption,
                                color = mutedText,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 8.dp)
                            )
                        }
                    }
                }

                // ── SCREENSHOT 3: Materials & Fabric Issued Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "MATERIALS & FABRIC ISSUED", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = mutedText)
                            Icon(painter = painterResource(R.drawable.ic_inventory), contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = Primary_background,
                            border = BorderStroke(1.dp, light_grey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(tokens.extraPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "FABRIC SOURCE", fontSize = tokens.label, color = mutedText)
                                    Spacer(Modifier.height(2.dp))
                                    Text(text = fabricSource, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(lightGray)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(text = "In Hand", fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Text(
                            text = "No additional trims/materials requisitioned.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// Conversion Helpers from API DTOs
// ─────────────────────────────────────────────────────────────────────────

private fun extractGarmentDetailFromJobCard(
    card: ServiceJobCardDetail,
    header: ServiceOrderHeader?
): ServiceGarmentDetail {
    val cuttingStage = card.stages.firstOrNull { it.stageName.equals("Cutting", true) }
    val stitchingStage = card.stages.firstOrNull { it.stageName.equals("Stitching", true) }
    val qcStage = card.stages.firstOrNull { it.stageName.equals("Quality Check", true) || it.stageName.equals("QC", true) }
    val trialStage = card.stages.firstOrNull { it.stageName.equals("Trial", true) }

    val cuttingWorker = cuttingStage?.allocations?.firstOrNull()?.workerId
    val stitchingWorker = stitchingStage?.allocations?.firstOrNull()?.workerId
    val qcWorker = qcStage?.allocations?.firstOrNull()?.workerId
    val trialWorker = trialStage?.allocations?.firstOrNull()?.workerId

    val cuttingName = cuttingWorker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
    val stitchingName = stitchingWorker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
    val qcName = qcWorker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
    val trialName = trialWorker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }

    val anyAssigned = card.stages.any { it.allocations.isNotEmpty() }
    val primaryArtisan = stitchingName ?: cuttingName ?: qcName ?: trialName

    val measurementList: List<ServiceGarmentMeasurement> = card.garment?.measurements?.mapNotNull { item ->
        val label = item.fieldName ?: item.fieldId?.displayName ?: item.fieldId?.name ?: return@mapNotNull null
        val numericVal = item.entries?.firstOrNull()?.value ?: 0.0
        val formattedVal = if (numericVal % 1.0 == 0.0) numericVal.toInt().toString() else numericVal.toString()
        val unit = item.entries?.firstOrNull()?.subLabel ?: item.fieldId?.unit ?: "inch"
        ServiceGarmentMeasurement(name = label, value = formattedVal, unit = unit)
    } ?: emptyList()

    val mappedStages: List<ServiceGarmentStage> = if (card.stages.isNotEmpty()) {
        card.stages.mapIndexed { index, st ->
            val worker = st.allocations.firstOrNull()?.workerId
            val name = worker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
            val code = worker?.employeeCode ?: "EMP-${String.format(Locale.ENGLISH, "%03d", index + 1)}"
            val stageTitle = st.stageName ?: st.stageId?.name ?: ""
            val actualTimeStr = if ((st.timeSpentMinutes ?: 0) > 0) "${st.timeSpentMinutes} mins" else "—"
            val estTimeStr = if ((st.estimatedMinutes ?: 0) > 0) "${st.estimatedMinutes} mins" else ""
            val finishedStr = st.completedAt?.let { formatDisplayTime(it) } ?: ""

            ServiceGarmentStage(
                // Use the backend stage _id as stageNumber or stage reference ID
                stageNumber = st.id.ifBlank { st.stageId?.id.orEmpty() },
                stageName = stageTitle,
                description = "Execute ${stageTitle.lowercase()} operations and quality validation.",
                actualDuration = actualTimeStr,
                estimatedDuration = estTimeStr,
                finishedTime = finishedStr,
                status = st.status ?: "",
                assignedWorkerId = worker?.id,
                assignedStaffName = name,
                assignedStaffCode = code,
                isCompleted = st.status.equals("", true),
                isAssigned = st.allocations.isNotEmpty()
            )
        }
    } else {
        emptyList()
    }

    return ServiceGarmentDetail(
        id = card.id,
        jobCardCode = card.jobCardCode ?: card.serialCode ?: "—",
        type = card.garment?.name ?: card.garment?.category?.displayName ?: "",
        quantity = card.totalQuantity,
        completedQuantity = card.completedQuantity,
        price = 0.0,
        priority = card.priority ?: "",
        trialRequired = true,
        complexity = card.garment?.stitchingType ?: "",
        selectedModels = listOfNotNull(card.garment?.design?.name),
        fabricSource = card.garment?.fabricSource?.replace("_", " ") ?: "",
        pattern = card.garment?.design?.name ?: card.garment?.design?.code ?: "—",
        status = card.status ?: "",
        currentStageName = card.currentStageName ?: "",
        notes = card.garment?.fabricNotes ?: card.garment?.specialInstructions ?: "",
        deadline = header?.dueDate?.let { formatDisplayFullDate(it) } ?: "",
        customerName = header?.customer?.name ?: "",
        customerPhone = header?.customer?.phone ?: "—",
        orderCode = header?.orderCode ?: "",
        totalEstimatedEffort = "",
        measurements = measurementList,
        stagesList = mappedStages,
        assignment = ServiceGarmentAssignment(
            cuttingTailor = cuttingName,
            stitchingTailor = stitchingName,
            qualityInspector = qcName,
            primaryArtisan = primaryArtisan,
            trial = trialName,
            priority = card.priority ?: "Medium",
            startDate = card.stages.firstOrNull()?.startedAt?.let { formatShortDate(it) },
            completionDate = card.stages.lastOrNull()?.completedAt?.let { formatShortDate(it) },
            isAssigned = anyAssigned
        )
    )
}

private fun extractPaymentInfo(header: ServiceOrderHeader): ServicePaymentInfo {
    val p = header.payment
    val grandTotal = p?.grandTotal ?: 0.0
    val advancePaid = p?.advanceAmountPaid ?: 0.0
    val balance = p?.balanceAmount ?: (grandTotal - advancePaid)

    return ServicePaymentInfo(
        orderId = header.orderCode ?: "—",
        status = p?.paymentStatus ?: "Unpaid",
        paymentMode = p?.paymentMode ?: "Cash",
        totalAmount = grandTotal,
        paidAmount = advancePaid,
        balanceAmount = balance
    )
}

// ─────────────────────────────────────────────────────────────────────────
// 1. OVERVIEW TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceOverviewTab(data: ServiceOrderDetailData) {
    val tokens = LocalAppTokens.current
    val header = data.orderHeader
    val customer = header?.customer
    val payment = header?.payment
    val address = customer?.billingAddress

    val fullAddress = listOfNotNull(
        address?.flatNo,
        address?.street,
        address?.city,
        address?.subdivisionName,
        address?.countryName,
        address?.pincode
    ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "—" }

    val (statusBg, statusText) = when (payment?.paymentStatus?.lowercase()) {
        "paid" -> Pair(greenBg, greentext)
        "partial", "partially paid" -> Pair(yellowBg, yellowText)
        else -> Pair(redBg, redText)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(whiteBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)) {
            Text(
                text = "Customer Information",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(tokens.extraPadding))
            OverviewFieldRow("Name", customer?.name ?: "—")
            OverviewFieldRow("Phone", customer?.phone?.let { if (it.startsWith("+91")) it else "+91 $it" } ?: "—")
            OverviewFieldRow("Email", customer?.email ?: "—")
            OverviewFieldRow("Customer Code", customer?.customerCode ?: "—")
            OverviewFieldRow("Customer Level", customer?.customerLevel ?: "Regular")
            OverviewFieldRow("Billing Address", fullAddress, showDivider = false)
        }

        OverviewSectionDivider()

        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)) {
            Text(
                text = "Order Information",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(tokens.extraPadding))
            OverviewFieldRow("Order Code", header?.orderCode ?: "—")
            OverviewFieldRow("Order Type", header?.orderType?.replace("_", " ") ?: "Sales Order")
            OverviewFieldRow("Order Date", header?.orderDate?.let { formatDisplayFullDate(it) } ?: "—")
            OverviewFieldRow("Due Date", header?.dueDate?.let { formatDisplayFullDate(it) } ?: "—")
            OverviewFieldRow("Priority", header?.priority ?: "Medium")
            OverviewFieldRow("Job Cards Count", "${data.totalJobCardsCount} Cards", showDivider = false)
        }

        OverviewSectionDivider()

        Column(modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)) {
            Text(
                text = "Payment Snapshot",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(tokens.extraPadding))
            OverviewFieldRow("Net Order Amount", "₹${payment?.grandTotal?.toInt() ?: 0}")
            OverviewFieldRow("Paid Amount", "₹${payment?.advanceAmountPaid?.toInt() ?: 0}")
            OverviewFieldRow("Balance Due", "₹${payment?.balanceAmount?.toInt() ?: 0}", valueColor = redText)
            OverviewFieldRow("Payment Mode", payment?.paymentMode ?: "Cash")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Status", fontSize = tokens.bodySmall, color = mutedText)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(statusBg)
                        .padding(horizontal = tokens.extraPadding, vertical = 4.dp)
                ) {
                    Text(
                        text = payment?.paymentStatus ?: "Unpaid",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = statusText
                    )
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────
// 2. GARMENTS TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceGarmentsTab(
    garments: List<ServiceGarmentDetail>,
    onGoToAssignments: () -> Unit,
    onViewMeasurements: (ServiceGarmentDetail) -> Unit,
    onViewAssignment: (ServiceGarmentDetail) -> Unit,
    onViewWork: (ServiceGarmentDetail) -> Unit,
    onAssignWorker: (ServiceGarmentDetail) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Primary_background)
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, title_border)
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Job Cards & Garments",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onGoToAssignments() }
                    ) {
                        Text(
                            text = "Go to assignments",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Text(
                    text = "View and manage all job cards and garments for this order.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }

        garments.forEach { garment ->
            val isAssigned = garment.assignment?.isAssigned == true || !garment.assignment?.primaryArtisan.isNullOrBlank()

            if (isAssigned) {
                AssignedGarmentCard(
                    garment = garment,
                    onViewMeasurements = { onViewMeasurements(garment) },
                    onViewAssignment = { onViewAssignment(garment) },
                    onViewWork = { onViewWork(garment) }
                )
            } else {
                UnassignedGarmentCard(
                    garment = garment,
                    onViewMeasurements = { onViewMeasurements(garment) },
                    // Opens the assignment method sheet when clicking "Assign Worker"
                    onAssignWorker = { onAssignWorker(garment) }
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun AssignedGarmentCard(
    garment: ServiceGarmentDetail,
    onViewMeasurements: () -> Unit,
    onViewAssignment: () -> Unit,
    onViewWork: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isCompleted = garment.status.equals("Completed", true)
    val (statusBg, statusText) = if (isCompleted) greenBg to greentext else yellowBg to yellowText
    val artisanName = garment.assignment?.primaryArtisan ?: "Assigned Artisan"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = garment.type.take(1).uppercase(),
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = tokens.bodyMedium
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.type,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Quantity: ${garment.quantity} ",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                        Text(
                            text = "(${garment.jobCardCode})",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(statusBg)
                        .padding(horizontal = tokens.extraPadding, vertical = 0.dp)
                ) {
                    Text(
                        text = garment.status,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = statusText
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            OverviewFieldRow(
                label = "Trial Stage",
                value = if (garment.trialRequired) "Required" else "Not Required",
                valueColor = orangeText,
                showDivider = false
            )
            OverviewFieldRow(
                label = "Priority",
                value = garment.priority,
                valueColor = yellowText,
                showDivider = false
            )
            OverviewFieldRow(
                label = "Deadline",
                value = garment.deadline,
                valueColor = TextPrimary,
                showDivider = false
            )

            Spacer(Modifier.height(tokens.extraPadding))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = primary_light,
                border = BorderStroke(1.dp, light_blue_border)
            ) {
                Row(
                    modifier = Modifier.padding(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = artisanName.take(1).uppercase(),
                            color = whiteBg,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Column {
                        Text(
                            text = artisanName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Assigned Artisan",
                            fontSize = tokens.label,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onViewMeasurements() },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ruler),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "View Measurements",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                OutlinedButton(
                    onClick = onViewAssignment, // Navigates to Assign Garment Production
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, light_blue_border),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                ) {
                    Text(
                        text = "View Assignment",
                        color = Primary,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onViewWork, // Navigates to Production Stages (JobCardViewOneScreen)
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "View Work",
                        color = whiteBg,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun UnassignedGarmentCard(
    garment: ServiceGarmentDetail,
    onViewMeasurements: () -> Unit,
    onAssignWorker: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {

            // ── Header: Avatar, Name, Qty + Code, Not Assigned badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = garment.type.take(1).uppercase(),
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = tokens.bodyMedium
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.type,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Quantity: ${garment.quantity} ",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                        Text(
                            text = "(${garment.jobCardCode})",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(redBg)
                        .padding(horizontal = tokens.extraPadding, vertical = 0.dp)
                ) {
                    Text(
                        text = "Not Assigned",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = redText
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            // ── Info Rows ──
            OverviewFieldRow(
                label = "Trial Stage",
                value = if (garment.trialRequired) "Required" else "Not Required",
                valueColor = orangeText,
                showDivider = false
            )
            OverviewFieldRow(
                label = "Priority",
                value = garment.priority,
                valueColor = yellowText,
                showDivider = false
            )
            OverviewFieldRow(
                label = "Deadline",
                value = garment.deadline,
                valueColor = TextPrimary,
                showDivider = false
            )

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            // ── View Measurements (right aligned) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onViewMeasurements() },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ruler),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "View Measurements",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            Spacer(Modifier.height(tokens.extraPadding))

            // ── Assign Worker Button ──
            Button(
                onClick = onAssignWorker,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Assign Worker",
                    color = whiteBg,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// 3. ASSIGNMENTS TAB (Matches Provided Screenshot & Dynamic Progress Bar)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceAssignmentsTab(
    garments: List<ServiceGarmentDetail>,
    onAssignClick: (ServiceGarmentDetail) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Calculate dynamic assignment metrics
    val totalGarments = garments.size
    val assignedCount = garments.count { garment ->
        garment.assignment?.isAssigned == true || !garment.assignment?.primaryArtisan.isNullOrBlank()
    }
    val progressFraction = if (totalGarments > 0) assignedCount.toFloat() / totalGarments else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    if (garments.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No garments available to assign.", color = mutedText, fontSize = tokens.bodySmall)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Primary_background)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── TOP CARD: Assignment Progress ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: Title and Garment Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assignment Progress",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Text(
                            text = "$assignedCount/$totalGarments garments",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Progress Bar: Fills dynamically up to 100%
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = light_grey
                    )

                    Spacer(Modifier.height(14.dp))

                    // Progress Percentage Text
                    Text(
                        text = "$progressPercentage% assigned",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey
                    )
                }
            }

            // ── GARMENTS LIST: Individual Garment Assignment Cards ──
            garments.forEach { garment ->
                ServiceAssignmentCard(
                    garment = garment,
                    onAssignClick = { onAssignClick(garment) }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ServiceAssignmentCard(
    garment: ServiceGarmentDetail,
    onAssignClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Check assignment status and retrieve assigned artisan name
    val artisanName = garment.assignment?.primaryArtisan
        ?: garment.assignment?.cuttingTailor
        ?: garment.assignment?.stitchingTailor
    val isAssigned = garment.assignment?.isAssigned == true || !artisanName.isNullOrBlank()

    val complexityLabel = garment.complexity.ifBlank { garment.priority.ifBlank { "" } }

    Card(
        modifier = Modifier.fillMaxWidth(), // Only fillMaxWidth, no clickable action on the card container
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Garment Image Thumbnail + Title + Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Garment Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shopping_bag),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Garment Name and Job Card Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.type,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "${garment.jobCardCode} • Qty ${garment.quantity} • $complexityLabel",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Dynamic State Layout ──
            if (isAssigned) {
                // Dynamic Status String from API Response formatted to Title Case
                val apiStatusRaw = garment.status.ifBlank { garment.currentStageName }
                val formattedStatus = remember(apiStatusRaw) {
                    apiStatusRaw.replace("_", " ")
                        .trim()
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { word ->
                            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                        }
                        .ifBlank { "" }
                }

                // Dynamic Status Color Palette based on API status value
                val (statusBg, statusBorder, statusTextColor) = when (apiStatusRaw.lowercase()) {
                    "completed" -> Triple(greenBg, darkGreenBg, greentext)
                    "in_progress", "in progress", "started" -> Triple(yellowBg.copy(alpha = 0.25f), yellowText, yellowText)
                    "rework" -> Triple(roseBg, redBg, redText)
                    "assigned" -> Triple(primary_light, light_blue_border, Primary)
                    else -> Triple(yellowBg.copy(alpha = 0.25f), yellowText, yellowText)
                }

                // Dynamic Worker Name and Role directly from API assignment
                val resolvedArtisanName = garment.assignment?.primaryArtisan
                    ?: garment.stagesList.firstOrNull { !it.assignedStaffName.isNullOrBlank() }?.assignedStaffName
                    ?: ""

                val resolvedRole = garment.stagesList.firstOrNull { !it.assignedStaffRole.isNullOrBlank() }?.assignedStaffRole
                    ?: ""

                // ASSIGNED STATE: Shows assigned artisan and status badge (non-clickable)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artisan info: Name and Role directly from API
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = resolvedArtisanName,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = resolvedRole,
                            fontSize = tokens.caption,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Status Badge with dynamic text and palette from API
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg,
                        border = BorderStroke(1.dp, statusBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formattedStatus,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusTextColor
                            )
                        }
                    }
                }
            } else {
                // UNASSIGNED STATE: "Not Assigned" badge + Solid "Assign" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Not Assigned" badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = roseBg,
                        border = BorderStroke(1.dp, redBg)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not Assigned",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = redText
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Only clicking this "Assign" button triggers the bottom sheet
                    Button(
                        onClick = onAssignClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Assign",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentWorkflowRow(
    stageLabel: String,
    workerName: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(tokens.iconSize)
            )
        }

        Spacer(Modifier.width(tokens.extraPadding))

        Column {
            Text(
                text = stageLabel,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = workerName,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// 4. TIMELINE TAB (Dynamic Order Progression from API Response)
// ─────────────────────────────────────────────────────────────────────────

private data class OrderTimelineStep(
    val title: String,
    val description: String,
    val timestamp: String?,
    val isCompleted: Boolean
)

@Composable
private fun ServiceTimelineTab(
    orderData: ServiceOrderDetailData,
    garments: List<ServiceGarmentDetail>
) {
    val tokens = LocalAppTokens.current
    val header = orderData.orderHeader
    val customerName = header?.customer?.name ?: "Customer"
    val orderCode = header?.orderCode ?: "Order"

    // Dynamic metrics calculation
    val totalGarments = garments.size
    val assignedCount = garments.count { it.assignment?.isAssigned == true || !it.assignment?.primaryArtisan.isNullOrBlank() }
    val isAllAssigned = totalGarments > 0 && assignedCount == totalGarments
    val isPartiallyAssigned = assignedCount in 1 until totalGarments

    // Dynamic Production Completion status
    val completedGarmentsCount = garments.count { it.status.equals("Completed", ignoreCase = true) }
    val isProductionCompleted = totalGarments > 0 && completedGarmentsCount == totalGarments

    // Delivery status
    val isDelivered = header?.status?.equals("Delivered", ignoreCase = true) == true

    // Assignment status text
    val assignmentTitle = when {
        isAllAssigned -> "Garments Assigned"
        isPartiallyAssigned -> "Partial Assignment"
        else -> "Partial Assignment"
    }

    val assignmentDate = garments.firstOrNull { it.assignment?.isAssigned == true }?.assignment?.startDate
        ?: header?.orderDate?.let { formatTimelineDateTime(it) }

    // Build timeline steps dynamically
    val timelineSteps = remember(orderData, garments) {
        listOf(
            OrderTimelineStep(
                title = "Order Created",
                description = "$orderCode created for $customerName",
                timestamp = header?.orderDate?.let { formatTimelineDateTime(it) },
                isCompleted = true
            ),
            OrderTimelineStep(
                title = "Garments Added",
                description = "$totalGarments garments added to the order",
                timestamp = header?.orderDate?.let { formatTimelineDateTime(it) },
                isCompleted = totalGarments > 0
            ),
            OrderTimelineStep(
                title = assignmentTitle,
                description = "$assignedCount of $totalGarments garments assigned",
                timestamp = assignmentDate,
                isCompleted = isAllAssigned
            ),
            OrderTimelineStep(
                title = "Production Completion",
                description = if (isProductionCompleted) "Completed" else "Pending",
                timestamp = if (isProductionCompleted) "Completed" else null,
                isCompleted = isProductionCompleted
            ),
            OrderTimelineStep(
                title = "Delivery",
                description = if (isDelivered) "Delivered" else "Scheduled",
                timestamp = header?.dueDate?.let { formatDisplayFullDate(it) },
                isCompleted = isDelivered
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, title_border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Card Title
                Text(
                    text = "Order Timeline",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Spacer(Modifier.height(24.dp))

                // Timeline List
                timelineSteps.forEachIndexed { index, step ->
                    val isLast = index == timelineSteps.lastIndex

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Node: Circle Icon & Vertical Connector Line
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(36.dp)
                        ) {
                            if (step.isCompleted) {
                                // Completed State: Blue circle with check icon
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = whiteBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                // Pending State: Soft light-blue background circle with subtle border
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(primary_light),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BorderGray, CircleShape)
                                    )
                                }
                            }

                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(56.dp)
                                        .background(light_blue_border)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        // Right Node Content: Title, Description, Timestamp
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 0.dp else 24.dp)
                        ) {
                            Text(
                                text = step.title,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                text = step.description,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )

                            if (!step.timestamp.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = step.timestamp,
                                    fontSize = tokens.label,
                                    color = iconMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────
// Helper function to format timestamp as e.g. "09 Oct 2026, 10:00 AM"
// ─────────────────────────────────────────────────────────────────────────
private fun formatTimelineDateTime(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (_: Exception) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(raw) ?: return raw
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date)
        } catch (_: Exception) {
            raw
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// MEASUREMENTS BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun GarmentMeasurementsSheet(
    garment: ServiceGarmentDetail,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.88f,
        topInset = 16.dp,
        scrollableContent = false,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange,
        headerContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ruler),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Measurements — ${garment.type}",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = mutedText)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Atelier Production Unit",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "PRODUCTION JOB CARD",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                            Text(
                                text = garment.jobCardCode,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = garment.type,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                .background(primary_light)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Qty: ${garment.quantity} Piece(s)",
                                fontSize = tokens.label,
                                color = Primary
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Order ID: ${garment.orderCode} • Delivery: ${garment.deadline}",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = garment.customerName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = garment.customerPhone,
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stitching:", fontSize = tokens.label, color = mutedText)
                            Text(garment.complexity, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fabric Source:", fontSize = tokens.label, color = mutedText)
                            Text(garment.fabricSource, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Design / Pattern:", fontSize = tokens.label, color = mutedText)
                            Text(garment.pattern, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority:", fontSize = tokens.label, color = mutedText)
                            Text(garment.priority.uppercase(), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = redText)
                        }
                    }
                }
            }

            if (garment.notes.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = yellowBg,
                    border = BorderStroke(1.dp, yellowText)
                ) {
                    Text(
                        text = "Fabric Notes: ${garment.notes}",
                        fontSize = tokens.bodySmall,
                        color = yellowText,
                        modifier = Modifier.padding(tokens.extraPadding)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_ruler), contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "MEASUREMENTS LIST (${garment.measurements.size} PARAMETERS)",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            if (garment.measurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No measurements found",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            } else {
                val chunkedMeasurements = garment.measurements.chunked(2)
                chunkedMeasurements.forEach { rowPairs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        rowPairs.forEach { item ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = BorderStroke(1.dp, light_blue_border)
                            ) {
                                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                                    Text(
                                        text = item.name,
                                        fontSize = tokens.caption,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = item.value,
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = item.unit,
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                }
                            }
                        }
                        if (rowPairs.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = light_grey, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SignPlaceholder(title = "Cutting Master Sign:")
                SignPlaceholder(title = "Stitching Tailor Sign:")
                SignPlaceholder(title = "Quality Inspector Sign:")
            }

            Spacer(Modifier.height(tokens.screenPadding))
        }
    }
}

@Composable
private fun SignPlaceholder(title: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.width(90.dp)) {
        Text(text = title, fontSize = tokens.label, color = mutedText)
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = light_grey, thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PRODUCTION SEQUENCER SCREEN (Matches Desktop Design)
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun AssignGarmentProductionScreen(
    garment: ServiceGarmentDetail,
    specialistWorkloadList: List<SpecialistWorkloadDto>,
    isAllocating: Boolean = false,
    onBack: () -> Unit,
    onRouteToWorkshop: (List<ServiceGarmentStage>) -> Unit
) {
    val tokens = LocalAppTokens.current

    // State for tracking which stage is currently opened for staff reassignment
    var activeStageForReassign by remember { mutableStateOf<ServiceGarmentStage?>(null) }

    // Mutable list of stages to immediately reflect reassignment on the UI
    val currentStages = remember(garment.stagesList) { mutableStateListOf(*garment.stagesList.toTypedArray()) }

    val totalStages = currentStages.size
    val assignedCount = currentStages.count { it.isAssigned }
    val completionPercent = if (totalStages > 0) ((assignedCount.toFloat() / totalStages) * 100).toInt() else 100

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Assign Garment Production", onClose = onBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.padding(tokens.screenPadding)) {
                    Button(
                        onClick = { onRouteToWorkshop(currentStages) },
                        enabled = !isAllocating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = primary_light
                        )
                    ) {
                        if (isAllocating) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = whiteBg, modifier = Modifier.size(tokens.iconSize))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Assign & Route to Workshop",
                                color = whiteBg,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Top Garment Spec Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(yellowBg)
                                .border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shopping_bag),
                                contentDescription = null,
                                tint = orangeText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = garment.type,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )

                                Spacer(Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Primary_background)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = garment.orderCode,
                                        fontSize = tokens.label,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(redBg)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "• ${garment.complexity.ifBlank { "Heavy Complexity" }}",
                                        fontSize = tokens.label,
                                        color = redText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (garment.trialRequired) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(badgeGrey)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "• Trial Required",
                                            fontSize = tokens.label,
                                            color = textSubdued,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.screenPadding))
                    HorizontalDivider(color = light_grey, thickness = 1.dp)
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Customer", fontSize = tokens.bodySmall, color = mutedText)
                        Text(
                            text = "${garment.customerName} (CUS-8842)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Quantity", fontSize = tokens.bodySmall, color = mutedText)
                        Text(
                            text = "${garment.quantity} Piece",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Fabric", fontSize = tokens.bodySmall, color = mutedText)
                        Text(
                            text = garment.fabricSource.ifBlank { "Raw Silk & Organza" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(tokens.screenPadding))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Primary_background,
                        border = BorderStroke(1.dp, title_border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = tokens.extraPadding, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TARGET DELIVERY",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = garment.deadline,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.height(30.dp),
                                color = sectionBorder,
                                thickness = 1.dp
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EST. WORK EFFORT",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "15.0 Man-Hrs",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.height(30.dp),
                                color = sectionBorder,
                                thickness = 1.dp
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "BATCH ROUTE",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "BLOUSE-LUX",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }

            // Sequencer Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "Production Stages Sequencer",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(primary_light)
                            .padding(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "$assignedCount of $totalStages Assigned",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Stage Allocation Completion: $completionPercent%", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }

                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { completionPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = light_grey,
                    )
                }
            }

            // Desktop Stage Cards populated directly from API response
            currentStages.forEachIndexed { index, stage ->
                val matchedSpecialist = specialistWorkloadList.firstOrNull {
                    it.fullName?.equals(stage.assignedStaffName, ignoreCase = true) == true ||
                            it.id == stage.assignedWorkerId
                }

                DesktopStageCard(
                    stageIndex = index + 1, // Pass sequential numeric index (1, 2, 3...)
                    stage = stage,
                    specialist = matchedSpecialist,
                    onReassignClick = {
                        activeStageForReassign = stage
                    }
                )
            }

            // Bottom Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "Assignment Summary",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesktopSummaryBox(label = "STAGES TOTAL", value = "$totalStages", bgColor = Primary_background, textColor = TextPrimary, modifier = Modifier.weight(1f))
                        DesktopSummaryBox(label = "ASSIGNED", value = "$assignedCount", bgColor = greenBg, textColor = greentext, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesktopSummaryBox(label = "REMAINING", value = "${totalStages - assignedCount}", bgColor = redBg, textColor = redText, modifier = Modifier.weight(1f))
                        DesktopSummaryBox(label = "TOTAL TIME", value = garment.totalEstimatedEffort, bgColor = Primary_background, textColor = TextPrimary, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(tokens.screenPadding))

                    Text(
                        text = "STAGE CHECKLIST",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    currentStages.forEachIndexed { i, st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(purpleText)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${i + 1}. ${st.stageName}",
                                    fontSize = tokens.caption,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = st.assignedStaffName ?: "Unassigned",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = if (st.isAssigned) TextPrimary else redText
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }

        // Show the custom Assign Staff Modal Dialog when a stage is selected
        activeStageForReassign?.let { targetStage ->
            AssignStaffDialog(
                stage = targetStage,
                garmentQty = garment.quantity,
                specialistWorkloadList = specialistWorkloadList,
                onDismiss = { activeStageForReassign = null },
                onAssignConfirm = { selectedSpecialist, durationMinutes ->
                    // Update stage locally with the newly chosen specialist
                    val stageIndex = currentStages.indexOfFirst { it.stageNumber == targetStage.stageNumber }
                    if (stageIndex != -1) {
                        currentStages[stageIndex] = targetStage.copy(
                            assignedWorkerId = selectedSpecialist.id,
                            assignedStaffName = selectedSpecialist.fullName,
                            assignedStaffCode = selectedSpecialist.empCode,
                            assignedStaffRole = selectedSpecialist.roleName,
                            estimatedDuration = "$durationMinutes mins",
                            isAssigned = true
                        )
                    }
                    activeStageForReassign = null
                }
            )
        }
    }
}

@Composable
private fun DesktopStageCard(
    stageIndex: Int = 1, // Added stageIndex parameter to guarantee sequential step numbers
    stage: ServiceGarmentStage,
    specialist: SpecialistWorkloadDto?,
    onReassignClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val staffDisplayName = specialist?.fullName ?: stage.assignedStaffName
    val staffCode = specialist?.empCode ?: stage.assignedStaffCode ?: ""
    val roleTitle = specialist?.roleName ?: stage.assignedStaffRole ?: ""
    val activeTasks = specialist?.activeStagesCount ?: stage.activeTasks
    val workloadPercentage = specialist?.workloadPercentage ?: stage.workloadPercentage

    val isAssigned = stage.isAssigned && !staffDisplayName.isNullOrBlank()
    val isCompleted = stage.status.equals("Completed", ignoreCase = true)
    val isRework = stage.status.equals("Rework", ignoreCase = true)

    // Formats sequential stage index cleanly into two digits (e.g., "01", "02", "03")
    val formattedStageNumber = String.format(Locale.ENGLISH, "%02d", stageIndex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stage Number Badge + Stage Name + Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Box (e.g. "01", "02")
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAssigned) primary_light else roseBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedStageNumber,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAssigned) Primary else redText
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Stage Name
                Text(
                    text = stage.stageName.ifBlank { "Stage" },
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Spacer(Modifier.width(8.dp))

                // Status Badge: "! Pending Specialist" when unassigned, else dynamic status
                if (!isAssigned) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = roseBg,
                        border = BorderStroke(1.dp, redBg)
                    ) {
                        Text(
                            text = "! Pending Specialist",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = redText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    val (statusBadgeBg, statusBadgeBorder, statusBadgeText, statusLabel) = when {
                        isCompleted -> Quadruple(greenBg, darkGreenBg, greentext, "✓ Completed")
                        isRework -> Quadruple(roseBg, redBg, redText, "Rework")
                        else -> Quadruple(yellowBg, yellowText, yellowText, stage.status.ifBlank { "In Progress" })
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusBadgeBg,
                        border = BorderStroke(1.dp, statusBadgeBorder)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = statusBadgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Stage Description
            Text(
                text = stage.description.ifBlank { "Execute ${stage.stageName.lowercase()} operations and quality validation." },
                fontSize = tokens.caption,
                color = headerGrey
            )

            Spacer(Modifier.height(10.dp))

            // Slot duration line matching screenshot
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = iconMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "•  Slot: Flexible Duration / On Queue",
                    fontSize = tokens.caption,
                    color = iconMuted
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Specialist Area ──
            if (!isAssigned) {
                // UNASSIGNED: Matches screenshot exactly ("No specialist assigned yet" + "Assign Staff" button)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Primary_background,
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Round user avatar icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(light_grey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = iconMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Text Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No specialist assigned yet",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Requires specialist in ${stage.stageName}",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }

                        // "Assign Staff" Button opening dialog
                        Button(
                            onClick = onReassignClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Assign",
                                color = whiteBg,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // ASSIGNED: Specialist Card with Avatar, Name, Code, Role & "Change Staff" action
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = primary_light,
                    border = BorderStroke(1.dp, light_blue_border)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(whiteBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = staffDisplayName.take(1).uppercase(),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = staffDisplayName,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                    if (staffCode.isNotBlank()) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "($staffCode)",
                                            fontSize = tokens.label,
                                            color = iconMuted
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = roleTitle.ifBlank { "Specialist in ${stage.stageName}" },
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }

                            // "Change Staff" button
                            TextButton(onClick = onReassignClick) {
                                Text(
                                    text = "Change Staff",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper data holder for status badge styling
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun DesktopSummaryBox(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Text(text = label, fontSize = tokens.label, color = mutedText)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

@Composable
private fun TrialReworkRoutingDialog(
    stages: List<ServiceJobStage>,
    onDismiss: () -> Unit,
    onConfirm: (selectedStageIds: Set<String>, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    // Keep track of which stages are checked for rework
    val selectedStageIds = remember { mutableStateListOf<String>() }
    var reworkNotes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = Primary_background,
            border = BorderStroke(1.dp, title_border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(tokens.screenPadding)
            ) {
                // Dialog Header
                Text(
                    text = "Select Stages Requiring Rework",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Only selected stages will be re-routed. Unchecked stages will remain approved.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                Spacer(Modifier.height(tokens.screenPadding))

                // Scrollable List of Stages
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stages.forEach { stage ->
                        val isSelected = selectedStageIds.contains(stage.id)
                        val worker = stage.allocations.firstOrNull()?.workerId
                        val workerName = worker?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() } ?: "Unassigned"
                        val workType = stage.workType ?: stage.stageName ?: "Stage"

                        // Stage selection card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedStageIds.remove(stage.id)
                                    } else {
                                        selectedStageIds.add(stage.id)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = whiteBg,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) redText else light_blue_border
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedStageIds.add(stage.id)
                                        else selectedStageIds.remove(stage.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = redText,
                                        checkmarkColor = whiteBg,
                                        uncheckedColor = BorderGray
                                    )
                                )

                                Spacer(Modifier.width(8.dp))

                                // Stage details & worker info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stage.stageName ?: "Stage",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Primary_background)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = workType,
                                                fontSize = tokens.label,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Responsible Worker: $workerName",
                                        fontSize = tokens.caption,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Status indicator badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) redBg else light_grey)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) redText else light_grey,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) "Rework Selected" else "Keep As-Is",
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) redText else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    // Technical Rework Notes Card Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, title_border)
                    ) {
                        Column(modifier = Modifier.padding(tokens.extraPadding)) {
                            Text(
                                text = "Technical Rework Notes for Worker",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reworkNotes,
                                onValueChange = { reworkNotes = it },
                                placeholder = {
                                    Text(
                                        text = "E.g., Reduce waist by 1 inch, adjust bust contour darts, re-check sleeve cuff fitting...",
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = light_grey,
                                    focusedContainerColor = whiteBg,
                                    unfocusedContainerColor = whiteBg
                                ),
                                textStyle = TextStyle(
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))

                // Bottom Action Buttons: Cancel & Confirm & Route to Rework
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, light_blue_border)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = { onConfirm(selectedStageIds.toSet(), reworkNotes) },
                        modifier = Modifier
                            .weight(1.8f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = redText, disabledContainerColor = redBg),
                        enabled = selectedStageIds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Confirm & Rework",
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

// ─────────────────────────────────────────────────────────────────────────
// Bottom Sheets
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun AssignMethodSelectionSheet(
    garment: ServiceGarmentDetail?,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> },
    topInset: Dp = 5.dp,
    onDismiss: () -> Unit,
    onMethodSelected: (method: String) -> Unit
) {
    if (garment == null) return
    val tokens = LocalAppTokens.current

    // State controlling selection between Individual and Team
    var selectedMethod by remember { mutableStateOf("Individual") }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.65f,
        topInset = topInset,
        scrollableContent = false,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange,
        headerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ASSIGNMENT METHOD",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${garment.type} · ${garment.jobCardCode}",
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Option 1: Individual Tailors ──
            val isIndividual = selectedMethod == "Individual"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMethod = "Individual" },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isIndividual) primary_light else whiteBg),
                border = BorderStroke(if (isIndividual) 1.5.dp else 1.dp, if (isIndividual) Primary else title_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isIndividual,
                        onClick = { selectedMethod = "Individual" },
                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Individual Specialists",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Assign qualified specialists to each production stage (Cutting, Embroidery, Stitching, QC).",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }
            }

            // ── Option 2: Team Assignment ──
            val isTeam = selectedMethod == "Team"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMethod = "Team" },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isTeam) primary_light else whiteBg),
                border = BorderStroke(if (isTeam) 1.5.dp else 1.dp, if (isTeam) Primary else title_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isTeam,
                        onClick = { selectedMethod = "Team" },
                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Workshop Team / Line",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Assign an integrated workshop unit or floor team to execute stages in batch flow.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }
            }
        }

        // Continue Button
        Button(
            onClick = { onMethodSelected(selectedMethod) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
                .height(tokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(tokens.cardCornerRadius)
        ) {
            Text(
                text = "Continue to Production Sequencer",
                color = whiteBg,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AssignServiceTailorsSheet(
    garment: ServiceGarmentDetail?,
    staffList: List<StaffDto>,
    orderId: String,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> },
    topInset: Dp = 5.dp,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (garment == null) return
    val tokens = LocalAppTokens.current

    val staffNames = remember(staffList) { staffList.map { "${it.firstName} ${it.lastName}" } }
    var selectedCutting by remember { mutableStateOf("") }
    var selectedStitching by remember { mutableStateOf("") }
    var selectedQC by remember { mutableStateOf("") }

    var cuttingExpanded by remember { mutableStateOf(false) }
    var stitchingExpanded by remember { mutableStateOf(false) }
    var qcExpanded by remember { mutableStateOf(false) }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.65f,
        topInset = topInset,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shape = RoundedCornerShape(topStart = tokens.cardCornerRadius, topEnd = tokens.cardCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                Text(
                    text = "Assign Responsible Workers",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(tokens.screenPadding))

                FormDropdown(
                    label = "Cutting Tailor",
                    value = selectedCutting.ifEmpty { "Select an option" },
                    expanded = cuttingExpanded,
                    onExpandChange = { cuttingExpanded = it },
                    options = staffNames,
                    onOptionSelected = { selectedCutting = it }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormDropdown(
                    label = "Stitching Tailor",
                    value = selectedStitching.ifEmpty { "Select an option" },
                    expanded = stitchingExpanded,
                    onExpandChange = { stitchingExpanded = it },
                    options = staffNames,
                    onOptionSelected = { selectedStitching = it }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormDropdown(
                    label = "Quality Inspector",
                    value = selectedQC.ifEmpty { "Select an option" },
                    expanded = qcExpanded,
                    onExpandChange = { qcExpanded = it },
                    options = staffNames,
                    onOptionSelected = { selectedQC = it }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedCutting.isNotBlank() || selectedStitching.isNotBlank()) {
                                onSuccess("Workers assigned successfully")
                            } else {
                                onError("Please select at least one worker")
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = "Assign",
                            color = whiteBg,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiveServicePaymentSheet(
    orderId: String,
    balanceDue: Double,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurChange: (Dp) -> Unit,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var amountText by remember { mutableStateOf(balanceDue.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var methodExpanded by remember { mutableStateOf(false) }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.50f,
        topInset = 5.dp,
        onDismissRequest = onDismiss,
        onBlurScrimChange = { r, _ -> onBlurChange(r) }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shape = RoundedCornerShape(topStart = tokens.cardCornerRadius, topEnd = tokens.cardCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding)
            ) {
                Text(
                    text = "RECEIVE PAYMENT",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(tokens.screenPadding))

                FormLabel("Amount to Pay (₹)")
                Spacer(Modifier.height(6.dp))
                FormTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardType = KeyboardType.Number,
                    placeholder = "0.00"
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormDropdown(
                    label = "Payment Method",
                    value = selectedMethod,
                    expanded = methodExpanded,
                    onExpandChange = { methodExpanded = it },
                    options = listOf("Cash", "UPI / QR", "Card", "Bank Transfer"),
                    onOptionSelected = { selectedMethod = it }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Button(
                    onClick = onPaymentSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Confirm Payment",
                        color = whiteBg,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// Common Layout Elements
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewFieldRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    showDivider: Boolean = true
) {
    val tokens = LocalAppTokens.current

    // Truncates value text to maximum 20 characters with ellipsis
    val formattedValue = if (value.length > 20) {
        "${value.take(20)}..."
    } else {
        value
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = close_color
        )
        Text(
            text = formattedValue,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (showDivider) {
        HorizontalDivider(color = light_grey, thickness = 1.dp)
    }
}

@Composable
private fun AssignStaffDialog(
    stage: ServiceGarmentStage,
    garmentQty: Int,
    specialistWorkloadList: List<SpecialistWorkloadDto>,
    onDismiss: () -> Unit,
    onAssignConfirm: (selectedSpecialist: SpecialistWorkloadDto, durationMinutes: Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    var durationText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var filterUnderEightyWorkload by remember { mutableStateOf(false) }
    var selectedSpecialistId by remember {
        mutableStateOf(stage.assignedWorkerId ?: specialistWorkloadList.firstOrNull()?.id.orEmpty())
    }

    // Filter specialists list based on search and workload
    val filteredSpecialists = remember(searchQuery, filterUnderEightyWorkload, specialistWorkloadList) {
        specialistWorkloadList.filter { specialist ->
            val matchesSearch = searchQuery.isBlank() ||
                    specialist.fullName?.contains(searchQuery, ignoreCase = true) == true ||
                    specialist.empCode?.contains(searchQuery, ignoreCase = true) == true ||
                    specialist.roleName?.contains(searchQuery, ignoreCase = true) == true

            val matchesWorkload = !filterUnderEightyWorkload || specialist.workloadPercentage < 80
            matchesSearch && matchesWorkload
        }
    }

    val currentlySelectedStaff = specialistWorkloadList.firstOrNull { it.id == selectedSpecialistId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Primary_background,
            border = BorderStroke(1.dp, title_border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(tokens.screenPadding)
            ) {
                // ── Header: Icon, Stage Tag, Title, Subtitle, Close Button ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(yellowBg)
                            .border(1.dp, BorderGray, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = orangeText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(yellowBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "STAGE ",
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Bold,
                                    color = orangeText
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Assign Staff — ${stage.stageName}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Set duration for $garmentQty pcs and choose an artisan.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = light_grey, thickness = 1.dp)
                Spacer(Modifier.height(tokens.extraPadding))

                // ── Stage Estimated Duration Section ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Stage Estimated Duration (Minutes)",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Total time needed for all $garmentQty piece(s) in this stage.",
                    fontSize = tokens.caption,
                    color = close_color
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            value = durationText,
                            onValueChange = { durationText = it.filter { char -> char.isDigit() } }
                        )
                    }
                    Text(
                        text = "mins",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        softWrap = false,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(tokens.screenPadding))

                // ── Search Staff Bar ──
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search staff by name or code...",
                    isSearchBarAlone = true
                )

                Spacer(Modifier.height(8.dp))

                // ── Workload Filter Button ──
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (filterUnderEightyWorkload) primary_light else whiteBg,
                    border = BorderStroke(1.dp, if (filterUnderEightyWorkload) Primary else BorderGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filterUnderEightyWorkload = !filterUnderEightyWorkload }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Workload < 80%",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (filterUnderEightyWorkload) Primary else TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // ── Scrollable Staff Cards List from API ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredSpecialists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No specialists found",
                                fontSize = tokens.bodySmall,
                                color = mutedText
                            )
                        }
                    } else {
                        filteredSpecialists.forEach { specialist ->
                            val isSelected = specialist.id == selectedSpecialistId
                            val isAvailable = specialist.workloadPercentage < 80

                            // ==================== AFTER (Clean Multi-Line Layout as in 2nd Image) ====================
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = whiteBg,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Primary else BorderGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSpecialistId = specialist.id }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Radio Button
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedSpecialistId = specialist.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Primary,
                                            unselectedColor = mutedText
                                        )
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    // Avatar Initial in Yellow Box
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(yellowBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = specialist.fullName?.take(1)?.uppercase() ?: "A",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = tokens.bodyMedium,
                                            color = orangeText
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    // Specialist Details Column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)   // before: 4.dp
                                    ) {
                                        // Line 1: Name + Code
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = specialist.fullName ?: "",
                                                fontSize = tokens.bodySmall,
                                                lineHeight = tokens.bodySmall * 1.2f,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (!specialist.empCode.isNullOrBlank()) {
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "(${specialist.empCode})",
                                                    fontSize = tokens.label,
                                                    lineHeight = tokens.label * 1.2f,
                                                    color = mutedText,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Line 2: Available / Busy badge (own line, compact)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isAvailable) greenBg else yellowBg)
                                                .padding(horizontal = 8.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isAvailable) "Available" else "Busy",
                                                fontSize = tokens.label,
                                                lineHeight = tokens.label * 1.2f,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isAvailable) greentext else yellowText
                                            )
                                        }

                                        // Line 3: Role
                                        if (!specialist.roleName.isNullOrBlank()) {
                                            Text(
                                                text = specialist.roleName,
                                                fontSize = tokens.caption,
                                                lineHeight = tokens.caption * 1.2f,
                                                color = TextSecondary
                                            )
                                        }

                                        // Line 4: Workload
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Workload: ",
                                                fontSize = tokens.caption,
                                                lineHeight = tokens.caption * 1.2f,
                                                color = headerGrey
                                            )
                                            Text(
                                                text = "${specialist.workloadPercentage}%",
                                                fontSize = tokens.caption,
                                                lineHeight = tokens.caption * 1.2f,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }

                                        // Line 5: Active Stages
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Active Stages in Hand: ",
                                                fontSize = tokens.caption,
                                                lineHeight = tokens.caption * 1.2f,
                                                color = headerGrey
                                            )
                                            Text(
                                                text = "${specialist.activeStagesCount}",
                                                fontSize = tokens.caption,
                                                lineHeight = tokens.caption * 1.2f,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                // ── Selected Summary Footer ──
                Text(
                    text = "Selected: ${currentlySelectedStaff?.fullName ?: "None"} (Flexible Duration)",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(tokens.extraPadding))

                // ── Bottom Action Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = {
                            if (currentlySelectedStaff != null) {
                                onAssignConfirm(
                                    currentlySelectedStaff,
                                    durationText.toIntOrNull() ?: 90
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = currentlySelectedStaff != null
                    ) {
                        Text(
                            text = "Assign Selected Staff",
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

@Composable
private fun OverviewSectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Primary_background)
    )
}

private fun formatDisplayFullDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

private fun formatDisplayTime(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

private fun formatShortDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd MMM", Locale.ENGLISH).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

// ==================== NEW HELPER FUNCTION ====================
// Formats ISO UTC timestamp (e.g., "2026-10-14T14:10:00.000Z") to local 12-hour time (e.g., "07:40 PM")
private fun formatDisplayTimeOnly(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return null
        val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
        }
        formatter.format(date)
    } catch (_: Exception) {
        null
    }
}