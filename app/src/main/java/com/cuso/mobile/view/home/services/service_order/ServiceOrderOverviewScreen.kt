@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "Unused_parameter", "VariableNeverRead", "SameParameterValue"
)

package com.cuso.mobile.view.home.services.service_order

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.R
import com.cuso.mobile.model.sales.OrderOverviewData
import com.cuso.mobile.model.sales.OrderOverviewStageStep
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.light_blue
import com.cuso.mobile.ui.theme.light_blue_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.BackFabButton
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.home.inventory.procurement.orders.FormTextArea
import com.cuso.mobile.view.home.pdfgenerator.OrderReceiptPdfGenerator
import com.cuso.mobile.view.home.sales.sales_order.OrderReviewData
import com.cuso.mobile.view.home.sales.sales_order.toOrderReviewData
import com.cuso.mobile.viewmodel.AssignWorkersState
import com.cuso.mobile.viewmodel.ConvertToInvoiceState
import com.cuso.mobile.viewmodel.OrderOverviewState
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.ReceivePaymentState
import com.cuso.mobile.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

private val TabActive = Color(0xFF4F46E5)
private val TextPrimary = Color(0xFF111827)
private val mutedTextDark = Color(0xFF6B7280)
private val SectionBg = Color(0xFFF9FAFB)
private val BorderLight = title_border
private val StatusGreenBg = Color(0xFFDCFCE7)
private val StatusGreenText = Color(0xFF16A34A)
private val StatusOrangeBg = Color(0xFFFEF3C7)
private val StatusOrangeText = Color(0xFFD97706)
private val StatusGreyBg = Color(0xFFF3F4F6)
private val StatusGreyText = Color(0xFF6B7280)
private val PaidGreen = Color(0xFF16A34A)
private val BalanceRed = Color(0xFFEF4444)
private val ChipPurpleBg = Color(0xFFEDE9FE)
private val ChipPurpleText = Color(0xFF6D28D9)
private val AvatarBg = Color(0xFFEDE9FE)
private val AssignedBg = Color(0xFFE8F5E9)
private val AssignedText = Color(0xFF2E7D32)
private val UnassignedBg = Color(0xFFFFEBEE)
private val UnassignedText = Color(0xFFC62828)

data class ServiceGarmentDetail(
    val id: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val priority: String,
    val trialRequired: Boolean,
    val complexity: String,
    val selectedModels: List<String>,
    val fabricSource: String,
    val pattern: String,
    val measurements: List<Pair<String, String>>,
    val notes: String,
    val baseCost: Double,
    val additionalCharges: Double,
    val discount: Double,
    val assignment: ServiceGarmentAssignment? = null
) {
    val total: Double get() = baseCost + additionalCharges - discount
}

data class ServiceGarmentAssignment(
    val cuttingTailor: StaffDto? = null,
    val stitchingTailor: StaffDto? = null,
    val qualityInspector: StaffDto? = null,
    val trial: StaffDto? = null,
    val priority: String = "Low",
    val startDate: String? = null,
    val completionDate: String? = null,
    val isAssigned: Boolean = false
)

data class ServicePaymentRecord(
    val date: String,
    val amount: Double,
    val method: String,
    val refNo: String = "-"
)

data class ServicePaymentInfo(
    val orderId: String,
    val status: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val history: List<ServicePaymentRecord> = emptyList(),
    val additionalCharges: Double = 0.0,
    val discount: Double = 0.0
) {
    val remainingAmount: Double get() = totalAmount - paidAmount
    val paidTotal: Double get() = paidAmount
    val balancePending: Double get() = remainingAmount
    val completionPercent: Int
        get() = if (totalAmount <= 0.0) 0 else ((paidAmount / totalAmount) * 100).toInt().coerceIn(0, 100)
}
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ServiceOrderOverviewScreen(
    orderId: String,
    onClose: () -> Unit = {},
    onEditOrder: (OrderReviewData) -> Unit = {},
    onCreateNew: () -> Unit = {}
) {
    // Payment sheet states
    var paymentSheetBlur by remember { mutableStateOf(0.dp) }
    var paymentSheetScrim by remember { mutableFloatStateOf(0f) }
    var paymentSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val isPaymentSheetOpen = paymentSheetState != SheetValue.Hidden

    fun closePaymentSheet() {
        paymentSheetState = SheetValue.Hidden
        paymentSheetBlur = 0.dp
        paymentSheetScrim = 0f
    }

    val viewModel: OrderOverviewViewModel = hiltViewModel(key = "service_order_overview_$orderId")
    val salesViewModel: SalesViewModel = hiltViewModel()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val state by viewModel.overviewState.collectAsStateWithLifecycle()
    val currentOrderData = (state as? OrderOverviewState.Success)?.data
    val assignState by viewModel.assignWorkersState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        viewModel.fetchSalesOverview(orderId)
        salesViewModel.fetchStaff()
    }

    LaunchedEffect(assignState) {
        if (assignState is AssignWorkersState.Success) {
            successMessage = "Workers assigned successfully!"
            viewModel.fetchSalesOverview(orderId)
            viewModel.resetAssignWorkersState()
        } else if (assignState is AssignWorkersState.Error) {
            errorMessage = "Assignment failed: ${(assignState as AssignWorkersState.Error).message}"
            viewModel.resetAssignWorkersState()
        }
    }

    val receivePaymentState by viewModel.receivePaymentState.collectAsStateWithLifecycle()
    LaunchedEffect(receivePaymentState) {
        when (val rs = receivePaymentState) {
            is ReceivePaymentState.Success -> {
                successMessage = "Payment recorded successfully"
                closePaymentSheet()
                viewModel.fetchSalesOverview(orderId)
                viewModel.resetReceivePaymentState()
            }
            is ReceivePaymentState.Error -> {
                errorMessage = "Payment failed: ${rs.message}"
                viewModel.resetReceivePaymentState()
            }
            else -> Unit
        }
    }

    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Garments", "Assignments", "Payment")

    // ── Two-Step Assignment Sheet States ──
    var selectedGarmentForSheet by remember { mutableStateOf<ServiceGarmentDetail?>(null) }

    // Step 1 Sheet: Assignment Method Selection
    var assignMethodSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var assignMethodSheetBlur by remember { mutableStateOf(0.dp) }
    var assignMethodSheetScrim by remember { mutableFloatStateOf(0f) }

    // Step 2 Sheet: Production Tailors Dropdowns
    var assignTailorsSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var assignTailorsSheetBlur by remember { mutableStateOf(0.dp) }
    var assignTailorsSheetScrim by remember { mutableFloatStateOf(0f) }

    val isAssignMethodSheetOpen = assignMethodSheetState != SheetValue.Hidden
    val isAssignTailorsSheetOpen = assignTailorsSheetState != SheetValue.Hidden

    fun closeAllAssignSheets() {
        assignMethodSheetState = SheetValue.Hidden
        assignMethodSheetBlur = 0.dp
        assignMethodSheetScrim = 0f

        assignTailorsSheetState = SheetValue.Hidden
        assignTailorsSheetBlur = 0.dp
        assignTailorsSheetScrim = 0f

        selectedGarmentForSheet = null
    }

    val combinedSheetBlur = maxOf(assignMethodSheetBlur, assignTailorsSheetBlur, paymentSheetBlur)
    val combinedSheetScrim = maxOf(assignMethodSheetScrim, assignTailorsSheetScrim, paymentSheetScrim)
    val isAnySheetOpen = isAssignMethodSheetOpen || isAssignTailorsSheetOpen || isPaymentSheetOpen

    BackHandler(enabled = isAnySheetOpen) {
        when {
            isAssignTailorsSheetOpen -> {
                assignTailorsSheetState = SheetValue.Hidden
                assignMethodSheetState = SheetValue.Collapsed
            }
            isAssignMethodSheetOpen -> closeAllAssignSheets()
            isPaymentSheetOpen -> closePaymentSheet()
        }
    }

    val paymentInfoForSheet = currentOrderData?.let { remember(it) { extractServicePayment(it) } }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar("Order Details", onClose = onClose)
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
                    Column(modifier = Modifier.background(whiteBg)) {
                        val selectedIndex = tabs.indexOf(selectedTab)
                        val indicatorOffset by animateFloatAsState(
                            targetValue = selectedIndex.toFloat(),
                            label = "TabIndicatorOffset"
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val tabWidth = maxWidth / tabs.size

                            Row(modifier = Modifier.fillMaxWidth()) {
                                tabs.forEach { tab ->
                                    val isSelected = tab == selectedTab

                                    val animatedTextColor by animateColorAsState(
                                        targetValue = if (isSelected) TabActive else mutedText,
                                        label = "TabTextColor"
                                    )

                                    val animatedScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.15f else 1.0f,
                                        label = "TabTextScale"
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { selectedTab = tab }
                                            .padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = animatedTextColor,
                                            modifier = Modifier
                                                .scale(animatedScale)
                                                .padding(bottom = 4.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(tabWidth * 0.5f)
                                    .height(3.dp)
                                    .offset(x = (tabWidth * indicatorOffset) + (tabWidth * 0.25f))
                                    .background(
                                        color = TabActive,
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                        HorizontalDivider(color = BorderLight)
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blurScrim(combinedSheetBlur)
                                .graphicsLayer {
                                    alpha = 1f - (combinedSheetScrim * 0.2f)
                                }
                        ) {
                            when (val s = state) {
                                is OrderOverviewState.Loading, OrderOverviewState.Idle -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CirculerProgressIndicatorReuse()
                                    }
                                }
                                is OrderOverviewState.Error -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Failed to load service order", color = Color.Red, fontWeight = FontWeight.Bold)
                                            Text(s.message, color = Color.Gray, fontSize = 13.sp)
                                            Spacer(Modifier.height(12.dp))
                                            Button(onClick = { viewModel.fetchSalesOverview(orderId) }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                                is OrderOverviewState.Success -> {
                                    val garments: List<ServiceGarmentDetail> = remember(s.data) { extractServiceGarments(s.data) }
                                    val payment: ServicePaymentInfo = remember(s.data) { extractServicePayment(s.data) }

                                    when (selectedTab) {
                                        "Overview" -> ServiceOverviewTab(s.data)
                                        "Garments" -> ServiceGarmentsTab(
                                            garments = garments,
                                            onGoToAssignments = { selectedTab = "Assignments" }
                                        )
                                        "Assignments" -> ServiceAssignmentsTab(
                                            garments = garments,
                                            onAssignClick = { garment ->
                                                selectedGarmentForSheet = garment
                                                assignMethodSheetState = SheetValue.Collapsed
                                            }
                                        )
                                        "Payment" -> ServicePaymentTab(
                                            payment = payment,
                                            context = context,
                                            orderData = s.data,
                                            viewModel = viewModel,
                                            sheetState = paymentSheetState,
                                            onOpenSheet = { paymentSheetState = SheetValue.Collapsed },
                                            onShowSuccess = { msg -> successMessage = msg },
                                            onShowError = { msg -> errorMessage = msg }
                                        )
                                    }
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isAnySheetOpen,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            StepNavigationFab(
                                showBack = true,
                                showBackArrow = false,
                                showTrailingArrow = false,
                                onBack = { currentOrderData?.let { onEditOrder(it.toOrderReviewData()) } },
                                backEnabled = currentOrderData != null,
                                backLabel = "Edit Order",
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

                // ── STEP 1: Assignment Method Selection Sheet ──
                AssignMethodSelectionSheet(
                    garment = selectedGarmentForSheet,
                    sheetState = assignMethodSheetState,
                    onStateChange = { assignMethodSheetState = it },
                    onBlurScrimChange = { r, sc -> assignMethodSheetBlur = r; assignMethodSheetScrim = sc },
                    topInset = 66.dp,
                    onDismiss = { closeAllAssignSheets() },
                    onContinueWithIndividual = {
                        assignMethodSheetState = SheetValue.Hidden
                        assignTailorsSheetState = SheetValue.Collapsed
                    }
                )

                // ── STEP 2: Production Tailors Dropdowns Sheet ──
                AssignServiceTailorsSheet(
                    garment = selectedGarmentForSheet,
                    staffList = staffList,
                    orderId = orderId,
                    viewModel = viewModel,
                    assignState = assignState,
                    sheetState = assignTailorsSheetState,
                    onStateChange = { assignTailorsSheetState = it },
                    onBlurScrimChange = { r, sc -> assignTailorsSheetBlur = r; assignTailorsSheetScrim = sc },
                    topInset = 66.dp,
                    onDismiss = { closeAllAssignSheets() },
                    onError = { msg -> errorMessage = msg }
                )

                // ── Payment Sheet ──
                if (paymentInfoForSheet != null) {
                    ReceiveServicePaymentSheet(
                        orderId = currentOrderData.order._id,
                        balanceDue = paymentInfoForSheet.remainingAmount,
                        viewModel = viewModel,
                        isSaving = receivePaymentState is ReceivePaymentState.Loading,
                        sheetState = paymentSheetState,
                        onStateChange = { paymentSheetState = it },
                        onBlurChange = { paymentSheetBlur = it },
                        onDismiss = { closePaymentSheet() }
                    )
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

private fun isStageAssigned(stage: OrderOverviewStageStep): Boolean =
    stage.status.lowercase() != "pending" || stage.assignedQuantity > 0

private fun extractServiceGarments(data: OrderOverviewData): List<ServiceGarmentDetail> {
    val items = data.items
    val stageGroups = data.stages

    return items.map { item ->
        val stageGroup = stageGroups.firstOrNull { it.garmentItemId == item._id }
        val anyAssigned = stageGroup?.stages?.any { isStageAssigned(it) } == true

        val cuttingTailor = stageGroup?.stages
            ?.firstOrNull { it.stageName == "cutting" }
            ?.assignedTo?.firstOrNull()
        val stitchingTailor = stageGroup?.stages
            ?.firstOrNull { it.stageName == "stitching" }
            ?.assignedTo?.firstOrNull()
        val qualityInspector = stageGroup?.stages
            ?.firstOrNull { it.stageName == "qc" }
            ?.assignedTo?.firstOrNull()
        val trial = stageGroup?.stages
            ?.firstOrNull { it.stageName == "trial" }
            ?.assignedTo?.firstOrNull()

        val additionalChargesTotal = item.additionalCharges.sumOf { it.amount }

        ServiceGarmentDetail(
            id = item._id,
            type = item.categoryName,
            quantity = item.quantity,
            price = item.stitchingCharge + additionalChargesTotal,
            priority = item.priority,
            trialRequired = item.trialRequired,
            complexity = "—",
            selectedModels = emptyList(),
            fabricSource = item.fabricDetails?.fabricSource ?: "—",
            pattern = item.fabricDetails?.pattern ?: "—",
            measurements = emptyList(),
            notes = "",
            baseCost = item.stitchingCharge,
            additionalCharges = additionalChargesTotal,
            discount = 0.0,
            assignment = ServiceGarmentAssignment(
                cuttingTailor = cuttingTailor,
                stitchingTailor = stitchingTailor,
                qualityInspector = qualityInspector,
                trial = trial,
                priority = item.priority,
                isAssigned = anyAssigned
            )
        )
    }
}

private fun normalizePaymentStatus(raw: String): String = when (raw.lowercase().replace("_", " ").trim()) {
    "paid" -> "Paid"
    "unpaid" -> "Unpaid"
    "partially paid", "partial" -> "Partial"
    else -> raw.replaceFirstChar { it.uppercase() }
}

private fun extractServicePayment(data: OrderOverviewData): ServicePaymentInfo {
    val order = data.order
    val payments = data.payments

    return ServicePaymentInfo(
        orderId = order.orderNumber,
        status = normalizePaymentStatus(order.paymentStatus),
        totalAmount = order.totalAmount,
        paidAmount = order.totalPaid,
        history = payments.mapNotNull { p ->
            p.amount?.let { amt ->
                ServicePaymentRecord(
                    date = p.paymentDate?.let { formatOverviewDate(it) } ?: "—",
                    amount = amt,
                    method = p.method?.replaceFirstChar { it.uppercase() } ?: "Payment",
                    refNo = p.transactionId?.takeIf { it.isNotBlank() } ?: "-"
                )
            }
        },
        additionalCharges = order.summaryAdditionalCharges.sumOf { it.amount },
        discount = order.discount
    )
}

// ─────────────────────────────────────────────────────────────────────────
// OVERVIEW TAB (Updated to match design screenshot)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceOverviewTab(data: OrderOverviewData) {
    val order = data.order
    val payments = data.payments
    val customer = order.customerId
    val totalGarmentsCount = data.items.sumOf { it.quantity }

    val paymentStatusFormatted = normalizePaymentStatus(order.paymentStatus)
    val (statusBg, statusText) = when (paymentStatusFormatted.lowercase()) {
        "paid" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "partial" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "unpaid" -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
        else -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    }

    val displayNotes = payments.firstOrNull()?.notes?.takeIf { it.isNotBlank() }
        ?: "Customer prefers pastel colors"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(whiteBg)
    ) {
        // ── 1. Customer Information ──
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Customer Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(10.dp))
            OverviewFieldRow("Name", customer?.name ?: "—")
            OverviewFieldRow("Phone", customer?.mobile?.let { if (it.startsWith("+91")) it else "+91 $it" } ?: "—")
            OverviewFieldRow("Email", customer?.email ?: "—", showDivider = false)
        }

        OverviewSectionDivider()

        // ── 2. Order Information ──
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Order Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(10.dp))
            OverviewFieldRow("Order Date", order.orderDate?.let { formatOverviewFullDate(it) } ?: "—")
            OverviewFieldRow("Delivery Date", order.deliveryDate?.let { formatOverviewFullDate(it) } ?: "—")
            OverviewFieldRow("Garment Count", "$totalGarmentsCount items", showDivider = false)
        }

        OverviewSectionDivider()

        // ── 3. Payment Snapshot ──
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Payment Snapshot",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(10.dp))
            OverviewFieldRow("Net Order Amount", "₹${formatOverviewNumber(order.totalAmount)}")
            OverviewFieldRow("Paid Amount", "₹${formatOverviewNumber(order.totalPaid)}")
            OverviewFieldRow("Balance Due", "₹${formatOverviewNumber(order.balanceAmount)}", valueColor = Color(0xFFEF4444))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Status", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = paymentStatusFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusText
                    )
                }
            }
        }

        OverviewSectionDivider()

        // ── 4. Notes & Instructions ──
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Notes & Instructions",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(12.dp))

            Text("Notes", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(4.dp))
            Text(
                text = displayNotes,
                fontSize = 13.sp,
                color = Color(0xFF374151)
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Text("Special Instructions", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Please ensure fitting is perfect. Customer is particular about measurements.",
                fontSize = 13.sp,
                color = Color(0xFF374151),
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun OverviewFieldRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827),
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF9CA3AF))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = valueColor)
    }
    if (showDivider) {
        HorizontalDivider(color = Color(0xFFF8F9FA), thickness = 1.dp)
    }
}

@Composable
private fun OverviewSectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF8F9FC))
    )
}

private fun formatOverviewFullDate(raw: String): String {
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val month = months.getOrNull(parts[1].toInt() - 1) ?: parts[1]
            "${parts[2].toInt()} $month ${parts[0]}"
        } else raw
    } catch (_: Exception) {
        raw
    }
}

// ─────────────────────────────────────────────────────────────────────────
// GARMENTS TAB
// ─────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────
// GARMENTS TAB (Updated to match design screenshot)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceGarmentsTab(
    garments: List<ServiceGarmentDetail>,
    onGoToAssignments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FC))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Top Header Card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, title_border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Garments List",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onGoToAssignments() }
                    ) {
                        Text(
                            text = "Go to garments",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TabActive
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TabActive,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "View and manage all garments in this order and assign them to tailors or teams.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
            }
        }

        // ── Garment Items Cards ──
        garments.forEach { garment ->
            ServiceGarmentCard(
                garment = garment,
                onClick = onGoToAssignments
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ServiceGarmentCard(
    garment: ServiceGarmentDetail,
    onClick: () -> Unit = {}
) {
    val isAssigned = garment.assignment?.isAssigned == true ||
            garment.assignment?.cuttingTailor != null ||
            garment.assignment?.stitchingTailor != null

    val (assignBg, assignText) = if (isAssigned) {
        Color(0xFFDCFCE7) to Color(0xFF16A34A)
    } else {
        Color(0xFFFFEBEE) to Color(0xFFE53935)
    }

    val modelTag = garment.selectedModels.firstOrNull()
        ?: garment.pattern.takeIf { it.isNotBlank() && it != "—" }
        ?: "Designer"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Top Section: Avatar, Title, Badges & Chevron ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Letter Avatar Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = garment.type.take(1).uppercase(),
                        color = TabActive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Title + Tags
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.type,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Assignment Status Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(assignBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAssigned) "Assigned" else "Not Assigned",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = assignText
                            )
                        }

                        // Model / Complexity Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = modelTag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            // ── Bottom Section: Quantity & Trial Status ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quantity",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${garment.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Trial Status",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (garment.trialRequired) "Required" else "Not Required",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (garment.trialRequired) TabActive else Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ASSIGNMENTS TAB (Updated to match Image 1)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServiceAssignmentsTab(
    garments: List<ServiceGarmentDetail>,
    onAssignClick: (ServiceGarmentDetail) -> Unit
) {
    if (garments.isEmpty()) {
        EmptyAssignmentsState()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F9FC))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
private fun NoWorkersAssignedState(onAssignWorker: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_users),
                contentDescription = null,
                tint = ChipPurpleText,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "No workers assigned",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Assign tailors and masters to begin tracking garment production.",
            fontSize = 13.sp,
            color = mutedText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onAssignWorker,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TabActive),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = whiteBg,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Assign Worker", color = whiteBg, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyAssignmentsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = ChipPurpleText,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No garments to assign",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Add garments to this order first.",
            fontSize = 13.sp,
            color = mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ServiceAssignmentCard(
    garment: ServiceGarmentDetail,
    onAssignClick: () -> Unit
) {
    val isAssigned = garment.assignment?.isAssigned == true ||
            garment.assignment?.cuttingTailor != null ||
            garment.assignment?.stitchingTailor != null ||
            garment.assignment?.qualityInspector != null

    val cuttingStaffName = garment.assignment?.cuttingTailor?.let { "${it.firstName} ${it.lastName}" } ?: "Marco Rossi"
    val stitchingStaffName = garment.assignment?.stitchingTailor?.let { "${it.firstName} ${it.lastName}" } ?: "Elena Chen"
    val qcStaffName = garment.assignment?.qualityInspector?.let { "${it.firstName} ${it.lastName}" } ?: "Arjun Kumar"

    val startDate = garment.assignment?.startDate ?: "Jan 03"
    val expectedDate = garment.assignment?.completionDate ?: "Mar 05"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, title_border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Top Header: Title, Subtitle, Assigned Status Pill ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garment.type,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Qty: ${garment.quantity} | ${if (garment.trialRequired) "Trial Required" else "Bulk Order"}",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Green Assigned Pill with Dot
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAssigned) Color(0xFFDCFCE7) else Color(0xFFFFEBEE))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isAssigned) Color(0xFF16A34A) else Color(0xFFEF4444))
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isAssigned) "Assigned" else "Not Assigned",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAssigned) Color(0xFF16A34A) else Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            // ── Production Workflow Steps ──
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 1. Cutting
                AssignmentWorkflowRow(
                    stageLabel = "CUTTING",
                    workerName = cuttingStaffName,
                    icon = Icons.Default.ContentCut,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706)
                )

                // 2. Stitching
                AssignmentWorkflowRow(
                    stageLabel = "STITCHING",
                    workerName = stitchingStaffName,
                    icon = Icons.Default.Brush,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7)
                )

                // 3. QC
                AssignmentWorkflowRow(
                    stageLabel = "QC",
                    workerName = qcStaffName,
                    icon = Icons.Default.Check,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA)
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            // ── Timeline Progress ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start $startDate", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Text("Expected $expectedDate", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { 0.45f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF2F27CE),
                trackColor = Color(0xFFF3F4F6)
            )

            Spacer(Modifier.height(18.dp))

            // ── Action Buttons: Details & Reassign ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAssignClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2F27CE)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2F27CE))
                ) {
                    Text("Details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Button(
                    onClick = onAssignClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE))
                ) {
                    Text(
                        text = if (isAssigned) "Reassign" else "Assign",
                        color = whiteBg,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentWorkflowRow(
    stageLabel: String,
    workerName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color
) {
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
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = stageLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF),
                letterSpacing = 0.4.sp
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = workerName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun ServiceAssignmentRow(
    label: String,
    staff: StaffDto?,
    isAssigned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(70.dp)
        )

        if (isAssigned && staff != null) {
            Text("${staff.firstName} ${staff.lastName}", fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(AssignedBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Assigned", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AssignedText)
            }
        } else {
            Text("Not Assigned", fontSize = 14.sp, color = mutedText, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(UnassignedBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Not Assigned", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = UnassignedText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// STEP 1: ASSIGN METHOD SELECTION SHEET
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun AssignMethodSelectionSheet(
    garment: ServiceGarmentDetail?,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> },
    topInset: Dp = 5.dp,
    onDismiss: () -> Unit,
    onContinueWithIndividual: () -> Unit
) {
    if (garment == null) return

    val complexity = garment.complexity.takeIf { it != "—" && it.isNotBlank() }
        ?: garment.selectedModels.firstOrNull()
        ?: "Designer"

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.60f,
        topInset = topInset,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // ── Header Title & Subtitle ──
                Text(
                    text = "ASSIGN GARMENT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${garment.type}  ·  ", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Text("Qty ${garment.quantity}  ·  ", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Text("$complexity  ·  ", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                    Text(
                        text = if (garment.trialRequired) "Trial Required" else "No Trial",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Section Title ──
                Text(
                    text = "Choose an assignment method",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Select how you would like to assign this garment.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(14.dp))

                // ── Option 1: Individual Tailor (Selected) ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = primary_light),
                    border = BorderStroke(2.dp, Primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6366F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter=painterResource(R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Individual Tailor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Assign the complete garment to one skilled tailor.",
                            fontSize = 13.sp,
                            color = mutedText
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Available in your plan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Option 2: Team / Work Center (Premium) ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Team / Work Center",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4B5563)
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Premium",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Route the garment through multiple specialists or a production team.",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Upgrade to Premium to unlock team...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            Text(
                                text = "View Plans →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2F27CE)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Footer Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, grey_border)
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onContinueWithIndividual,
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = "Continue with Individual Tailor",
                            color = whiteBg,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ASSIGN TAILORS BOTTOM SHEET (Updated to match Image 2)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun AssignServiceTailorsSheet(
    garment: ServiceGarmentDetail?,
    staffList: List<StaffDto>,
    orderId: String,
    viewModel: OrderOverviewViewModel,
    assignState: AssignWorkersState,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> },
    topInset: Dp = 5.dp,
    onDismiss: () -> Unit,
    onError: (String) -> Unit = {}
) {
    if (garment == null) return

    val staffNameToStaff = remember(staffList) {
        staffList.associateBy { "${it.firstName} ${it.lastName}" }
    }
    val staffNames = remember(staffList) { staffNameToStaff.keys.toList() }

    var selectedCutting by remember(garment.id) { mutableStateOf(garment.assignment?.cuttingTailor) }
    var selectedStitching by remember(garment.id) { mutableStateOf(garment.assignment?.stitchingTailor) }
    var selectedQC by remember(garment.id) { mutableStateOf(garment.assignment?.qualityInspector) }

    var priority by remember(garment.id) { mutableStateOf("Low") }
    var completionDate by remember(garment.id) {
        mutableStateOf(
            garment.assignment?.completionDate
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date().apply { time += 30L * 24 * 60 * 60 * 1000 })
        )
    }

    var cuttingExpanded by remember { mutableStateOf(false) }
    var stitchingExpanded by remember { mutableStateOf(false) }
    var qcExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    val isAssigning = assignState is AssignWorkersState.Loading

    val complexity = garment.complexity.takeIf { it != "—" && it.isNotBlank() }
        ?: garment.selectedModels.firstOrNull()
        ?: "Designer"

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
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFD1D5DB))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(14.dp))

                // Title
                Text(
                    text = "ASSIGN TAILORS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Assign responsible tailors for each production stage of this garment.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                // Garment Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, grey_border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "GARMENT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF9CA3AF),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${garment.type} #FF-002",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Quantity", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Spacer(Modifier.height(3.dp))
                                Text("${garment.quantity} Units", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            }
                            Column {
                                Text("Complexity", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Spacer(Modifier.height(3.dp))
                                Text(complexity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            }
                            Column {
                                Text("Trial Required", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (garment.trialRequired) "Yes" else "No",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (garment.trialRequired) Color(0xFF2563EB) else Color(0xFF111827)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Section Title: Production Assignment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = Color(0xFF2F27CE),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Production Assignment",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Dropdowns
                FormDropdown(
                    label = "Cutting Tailor",
                    value = selectedCutting?.let { "${it.firstName} ${it.lastName}" } ?: "Select an option",
                    expanded = cuttingExpanded,
                    onExpandChange = { cuttingExpanded = it },
                    options = staffNames,
                    onOptionSelected = { name -> staffNameToStaff[name]?.let { selectedCutting = it } }
                )

                Spacer(Modifier.height(12.dp))

                FormDropdown(
                    label = "Stitching Tailor",
                    value = selectedStitching?.let { "${it.firstName} ${it.lastName}" } ?: "Select an option",
                    expanded = stitchingExpanded,
                    onExpandChange = { stitchingExpanded = it },
                    options = staffNames,
                    onOptionSelected = { name -> staffNameToStaff[name]?.let { selectedStitching = it } }
                )

                Spacer(Modifier.height(12.dp))

                FormDropdown(
                    label = "Quality Inspector",
                    value = selectedQC?.let { "${it.firstName} ${it.lastName}" } ?: "Select an option",
                    expanded = qcExpanded,
                    onExpandChange = { qcExpanded = it },
                    options = staffNames,
                    onOptionSelected = { name -> staffNameToStaff[name]?.let { selectedQC = it } }
                )

                Spacer(Modifier.height(14.dp))

                // Priority & Completion Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        FormDropdown(
                            label = "Priority",
                            value = priority,
                            expanded = priorityExpanded,
                            onExpandChange = { priorityExpanded = it },
                            options = listOf("Low", "Medium", "High", "Urgent"),
                            onOptionSelected = { priority = it }
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        FormLabel("Completion Date")
                        DatePickerField(
                            value = completionDate,
                            onDateSelected = { completionDate = it }
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                // Footer Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, grey_border),
                        enabled = !isAssigning
                    ) {
                        Text("Cancel", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (selectedCutting != null && selectedStitching != null && selectedQC != null) {
                                viewModel.assignWorkersToGarment(
                                    orderId = orderId,
                                    garmentItemId = garment.id,
                                    quantity = garment.quantity,
                                    cuttingStaffId = selectedCutting!!.id,
                                    stitchingStaffId = selectedStitching!!.id,
                                    qcStaffId = selectedQC!!.id
                                )
                                onDismiss()
                            } else {
                                onError("Please select all three workers")
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
                        enabled = !isAssigning
                    ) {
                        if (isAssigning) {
                            CirculerProgressIndicatorReuse()
                        } else {
                            Text("Assign Tailors", color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PAYMENT TAB (Updated to match design screenshot)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ServicePaymentTab(
    payment: ServicePaymentInfo,
    context: Context,
    orderData: OrderOverviewData,
    viewModel: OrderOverviewViewModel,
    sheetState: SheetValue,
    onOpenSheet: () -> Unit,
    onConvertToInvoice: () -> Unit = {},
    onShowSuccess: (String) -> Unit = {},
    onShowError: (String) -> Unit = {}
) {
    val pdfGenerator = remember { OrderReceiptPdfGenerator(context) }
    val receivePaymentState by viewModel.receivePaymentState.collectAsStateWithLifecycle()
    val isSaving = receivePaymentState is ReceivePaymentState.Loading

    // Form States for "Record New Payment"
    var amountText by remember { mutableStateOf(formatAmountPlain(payment.remainingAmount)) }
    var selectedMethod by remember { mutableStateOf("UPI / QR Scan") }
    var methodExpanded by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    }
    var internalNotes by remember { mutableStateOf("") }

    val methodOptions = listOf("UPI / QR Scan", "Cash", "Card", "Bank Transfer", "Cheque")

    // Documents & Actions Collapsible State
    var documentsExpanded by remember { mutableStateOf(true) }

    val receiptData = remember(orderData) {
        OrderReceiptPdfGenerator.OrderReceiptData(
            orderNumber = orderData.order.orderNumber,
            customerName = orderData.order.customerId?.name ?: "—",
            items = orderData.items.map {
                OrderReceiptPdfGenerator.OrderItem(
                    quantity = it.quantity,
                    name = it.categoryName,
                    price = it.stitchingCharge,
                    additionalCharge = it.additionalCharges.sumOf { add -> add.amount }
                )
            },
            otherCharges = orderData.order.summaryAdditionalCharges.sumOf { it.amount },
            totalAmount = orderData.order.totalAmount,
            paidAmount = orderData.order.totalPaid,
            balanceAmount = orderData.order.balanceAmount,
            deliveryDate = orderData.order.deliveryDate
        )
    }

    val (statusBg, statusText) = when (payment.status.lowercase()) {
        "paid" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "partial", "partially paid" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        else -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
    }

    val statusLabel = when (payment.status.lowercase()) {
        "paid" -> "PAID"
        "partial", "partially paid" -> "PARTIAL PAYMENT"
        else -> "UNPAID"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FC))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Top Summary Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row: Status Badge & Order ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusText,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "Order ID: ${payment.orderId}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Big Amount & Label
                    Text(
                        text = "₹${formatOverviewNumber(payment.totalAmount)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "TOTAL ORDER AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    Spacer(Modifier.height(14.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment Completion", fontSize = 13.sp, color = Color(0xFF4B5563))
                        Text(
                            "${payment.completionPercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TabActive
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { payment.completionPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TabActive,
                        trackColor = grey_border
                    )

                    Spacer(Modifier.height(14.dp))

                    // Paid & Remaining Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Paid: ",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "₹${formatOverviewNumber(payment.paidAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Remaining: ",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "₹${formatOverviewNumber(payment.remainingAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    if (payment.remainingAmount > 0.0) {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onOpenSheet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_credit),
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Receive Payment",
                                color = whiteBg,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ── 2. Record New Payment Card (Inline Form) ──
            if (payment.remainingAmount > 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, title_border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = TabActive,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Record New Payment",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        Spacer(Modifier.height(14.dp))

                        // Amount Received (₹)
                        FormLabel("Amount Received (₹)")
                        Spacer(Modifier.height(6.dp))
                        FormTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = "₹0.00",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(14.dp))

                        // Payment Method
                        FormDropdown(
                            label = "Payment Method",
                            value = selectedMethod,
                            expanded = methodExpanded,
                            onExpandChange = { methodExpanded = it },
                            options = methodOptions,
                            onOptionSelected = { selectedMethod = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        // Transaction ID / Ref No.
                        FormLabel("Transaction ID / Ref No.")
                        Spacer(Modifier.height(6.dp))
                        FormTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            placeholder = "TXN883910482"
                        )

                        Spacer(Modifier.height(14.dp))

                        // Payment Date
                        FormLabel("Payment Date")
                        Spacer(Modifier.height(6.dp))
                        DatePickerField(
                            value = paymentDate,
                            onDateSelected = { paymentDate = it }
                        )

                        Spacer(Modifier.height(14.dp))

                        // Internal Notes
                        FormLabel("Internal Notes")
                        Spacer(Modifier.height(6.dp))
                        FormTextArea(
                            value = internalNotes,
                            onValueChange = { internalNotes = it },
                            placeholder = "Mention specific details if any...",
                            minLines = 3,
                            maxLines = 4
                        )

                        Spacer(Modifier.height(20.dp))

                        // Actions: Cancel & Confirm Payment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    amountText = formatAmountPlain(payment.remainingAmount)
                                    transactionId = ""
                                    internalNotes = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, grey_border),
                                enabled = !isSaving
                            ) {
                                Text(
                                    "Cancel",
                                    color = Color(0xFF374151),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = {
                                    submitServicePayment(
                                        viewModel = viewModel,
                                        orderId = orderData.order._id,
                                        amountText = amountText,
                                        method = selectedMethod,
                                        referenceNo = transactionId,
                                        notes = internalNotes,
                                        paymentDate = paymentDate,
                                        paymentType = if ((amountText.toDoubleOrNull() ?: 0.0) >= payment.remainingAmount) "full" else "partial"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE)),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CirculerProgressIndicatorSmall()
                                } else {
                                    Text(
                                        "Confirm Payment",
                                        color = whiteBg,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Documents & Actions Card (Accordion Style) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { documentsExpanded = !documentsExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Documents & Actions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Icon(
                            imageVector = if (documentsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF6B7280)
                        )
                    }

                    if (documentsExpanded) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                        // 1. Generate / View Invoice
                        DocumentActionItem(
                            icon = Icons.Default.Description,
                            iconTint = Color(0xFF2F27CE),
                            label = "Generate Invoice",
                            arrowColor = Color(0xFF2F27CE),
                            onClick = {
                                if (orderData.order.balanceAmount == 0.0) {
                                    viewModel.convertToInvoice(orderData.order._id)
                                } else {
                                    onShowError("Please complete full payment to generate invoice.")
                                }
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)

                        // 2. Print Receipt
                        DocumentActionItem(
                            icon = Icons.Default.Print,
                            iconTint = Color(0xFF4B5563),
                            label = "Print Receipt",
                            arrowColor = Color(0xFF9CA3AF),
                            onClick = { pdfGenerator.printReceiptViaWebView(receiptData) }
                        )

                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)

                        // 3. Download PDF Ledger
                        DocumentActionItem(
                            icon = Icons.Default.FileDownload,
                            iconTint = Color(0xFF4B5563),
                            label = "Download PDF Ledger",
                            arrowColor = Color(0xFF9CA3AF),
                            onClick = {
                                val pdf = pdfGenerator.generateReceiptPdf(receiptData)
                                if (pdf != null) {
                                    onShowSuccess("PDF saved: ${pdf.absolutePath}")
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdf)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    onShowError("Failed to create PDF")
                                }
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 1.dp)

                        // 4. Create Adjustment
                        DocumentActionItem(
                            icon = Icons.Default.AddCircleOutline,
                            iconTint = Color(0xFFEF4444),
                            label = "Create Adjustment",
                            labelColor = Color(0xFFEF4444),
                            arrowColor = Color(0xFFEF4444),
                            onClick = { /* Handle adjustment if needed */ }
                        )
                    }
                }
            }

            // ── 4. Bottom Advance & Balance Summary Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, title_border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Advance Paid", fontSize = 14.sp, color = Color(0xFF4B5563))
                        Text(
                            "₹${formatOverviewNumber(payment.paidAmount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Balance Pending", fontSize = 14.sp, color = Color(0xFF4B5563))
                        Text(
                            "₹${formatOverviewNumber(payment.remainingAmount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // ── 5. Sticky Bottom Action Button ──
        if (payment.remainingAmount > 0.0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onOpenSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F27CE))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_credit),
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Receive Payment",
                        color = whiteBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    labelColor: Color = Color(0xFF111827),
    arrowColor: Color = Color(0xFF9CA3AF),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = arrowColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ReceiveServicePaymentSheet(
    orderId: String,
    balanceDue: Double,
    viewModel: OrderOverviewViewModel,
    isSaving: Boolean,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurChange: (Dp) -> Unit,
    topInset: Dp = 5.dp,
    onDismiss: () -> Unit
) {
    var isFullAmount by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf(formatAmountPlain(balanceDue)) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var methodExpanded by remember { mutableStateOf(false) }
    var referenceNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    }

    val methodOptions = listOf("Cash", "Card", "UPI", "Bank Transfer")

    LaunchedEffect(isFullAmount) {
        if (isFullAmount) amountText = formatAmountPlain(balanceDue)
    }

    LaunchedEffect(sheetState) {
        if (sheetState == SheetValue.Hidden) {
            isFullAmount = true
            selectedMethod = "Cash"
            referenceNo = ""
            notes = ""
            paymentDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        }
    }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.55f,
        topInset = topInset,
        onDismissRequest = onDismiss,
        onBlurScrimChange = { r, _ -> onBlurChange(r) }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            color = whiteBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        "RECEIVE PAYMENT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_blue, RoundedCornerShape(10.dp))
                        .border(1.dp, light_blue_border, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balance Due", fontSize = 13.sp, color = TextPrimary)
                    Text("₹${formatOverviewNumber(balanceDue)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BalanceRed)
                }

                Spacer(Modifier.height(16.dp))

                FormLabel("Payment Type")
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ServicePaymentTypeOption(
                        label = "Full Amount",
                        selected = isFullAmount,
                        modifier = Modifier.weight(1f),
                        onClick = { isFullAmount = true }
                    )
                    ServicePaymentTypeOption(
                        label = "Partial Amount",
                        selected = !isFullAmount,
                        modifier = Modifier.weight(1f),
                        onClick = { isFullAmount = false }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        FormLabel("Amount (₹)")
                        Spacer(Modifier.height(6.dp))
                        FormTextField(
                            value = amountText,
                            onValueChange = { if (!isFullAmount) amountText = it },
                            keyboardType = KeyboardType.Number,
                            placeholder = "0.00",
                            enabled = !isFullAmount
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        FormLabel("Payment Method")
                        FormDropdown(
                            label = "",
                            value = selectedMethod,
                            expanded = methodExpanded,
                            onExpandChange = { methodExpanded = it },
                            options = methodOptions,
                            onOptionSelected = { selectedMethod = it }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        FormLabel("Reference No. (Opt)")
                        Spacer(Modifier.height(6.dp))
                        FormTextField(
                            value = referenceNo,
                            onValueChange = { referenceNo = it },
                            placeholder = "TXN123..."
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        FormLabel("Date")
                        Spacer(Modifier.height(6.dp))
                        DatePickerField(
                            value = paymentDate,
                            onDateSelected = { paymentDate = it }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                FormLabel("Notes")
                Spacer(Modifier.height(6.dp))
                FormTextArea(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Optional Notes...",
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BackFabButton(
                            label = "Cancel",
                            onClick = onDismiss,
                            enabled = !isSaving,
                            showArrow = false,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                submitServicePayment(viewModel, orderId, amountText, selectedMethod, referenceNo, notes, paymentDate)
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, TabActive),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TabActive)
                        ) {
                            if (isSaving) {
                                CirculerProgressIndicatorSmall()
                            } else {
                                Text("Save Only", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            submitServicePayment(viewModel, orderId, amountText, selectedMethod, referenceNo, notes, paymentDate)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isSaving) {
                            CirculerProgressIndicatorSmall()
                        } else {
                            Text("Save & Print", color = whiteBg, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ServicePaymentTypeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) TabActive else grey_border,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (selected) light_blue else whiteBg)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = if (selected) Primary else TextPrimary)
    }
}

private fun submitServicePayment(
    viewModel: OrderOverviewViewModel,
    orderId: String,
    amountText: String,
    method: String,
    referenceNo: String,
    notes: String,
    paymentDate: String,
    paymentType: String = "full"
) {
    val amount = amountText.trim().toDoubleOrNull() ?: return
    if (amount <= 0.0) return

    viewModel.receivePayment(
        orderId = orderId,
        amount = amount,
        method = method.lowercase().replace(" ", "_"),
        transactionId = referenceNo.trim(),
        notes = notes.trim(),
        paymentDate = ddMMyyyyToIso(paymentDate),
        paymentType = paymentType
    )
}

private fun ddMMyyyyToIso(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else date
    } catch (_: Exception) {
        date
    }
}

private fun formatAmountPlain(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = blackTitle,
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg)
            .padding(vertical = 10.dp)
    )
}

@Composable
private fun SectionBlock(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 12.sp, color = mutedText)
    }
}

@Composable
private fun SmallSectionHeader(title: String, trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = mutedText)
        trailingIcon?.let {
            Icon(it, contentDescription = null, tint = mutedText, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun Tag(text: String, bg: Color, textColor: Color, bordered: Boolean = false) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = blackTitle)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
    HorizontalDivider(color = Color(0xFFF5F5F5))
}

@Composable
private fun InfoRowWithBadge(label: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = mutedText)
        Box(
            modifier = Modifier
                .background(StatusGreenBg, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                status.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusGreenText
            )
        }
    }
    HorizontalDivider(color = Color(0xFFF5F5F5))
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = mutedTextDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, fontSize = 13.sp, color = TextPrimary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedText, modifier = Modifier.size(18.dp))
    }
    HorizontalDivider(color = BorderLight)
}

private fun formatOverviewDate(raw: String): String {
    return try {
        val datePart = raw.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val month = months.getOrNull(parts[1].toInt() - 1) ?: parts[1]
            "${parts[2]} $month ${parts[0]}"
        } else raw
    } catch (_: Exception) { raw }
}

private fun formatOverviewNumber(value: Double): String {
    val longVal = value.toLong()
    val s = longVal.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

@Composable
fun ServiceConvertToInvoiceButton(
    viewModel: OrderOverviewViewModel,
    salesOrderId: String,
    isPaymentDone: Boolean,
    invoiceAlreadyExists: Boolean,
    modifier: Modifier = Modifier
) {
    val convertState by viewModel.convertToInvoiceState.collectAsState()

    var invoiceGeneratedLocally by remember(salesOrderId) { mutableStateOf(false) }

    LaunchedEffect(convertState) {
        if (convertState is ConvertToInvoiceState.Success) {
            invoiceGeneratedLocally = true
        }
    }

    val invoiceExists = invoiceAlreadyExists || invoiceGeneratedLocally
    val isLoading = convertState is ConvertToInvoiceState.Loading
    val isEnabled = isPaymentDone && !invoiceExists && !isLoading

    val bgColor = when {
        invoiceExists -> Color(0xFFDCFCE7)
        !isPaymentDone -> Color(0xFFF3F4F6)
        else -> Color(0xFFEEF0FF)
    }
    val contentColor = when {
        invoiceExists -> Color(0xFF16A34A)
        !isPaymentDone -> Color(0xFF9CA3AF)
        else -> Color(0xFF4F46E5)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .clickable(
                    enabled = isEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { viewModel.convertToInvoice(salesOrderId) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isLoading -> "Converting..."
                    invoiceExists -> "Invoice Generated"
                    else -> "Convert to Invoice"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )

            when {
                isLoading -> CirculerProgressIndicatorSmall()
                invoiceExists -> Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                else -> Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePaymentHistorySection(
    history: List<ServicePaymentRecord>,
    onView: (ServicePaymentRecord) -> Unit,
    onPrint: (ServicePaymentRecord) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChipPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = ChipPurpleText,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Payment History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(Modifier.height(12.dp))

        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SectionBg, RoundedCornerShape(12.dp))
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inbox, contentDescription = null, tint = mutedText, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text("No payments recorded for this order yet.", fontSize = 13.sp, color = mutedText)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(whiteBg)
                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            ) {
                history.forEachIndexed { idx, record ->
                    ServicePaymentHistoryRow(
                        record = record,
                        onView = { onView(record) },
                        onPrint = { onPrint(record) }
                    )
                    if (idx != history.lastIndex) HorizontalDivider(color = BorderLight)
                }
            }
        }
    }
}

@Composable
private fun ServicePaymentHistoryRow(
    record: ServicePaymentRecord,
    onView: () -> Unit,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(record.method, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(SectionBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(record.date, fontSize = 10.sp, color = mutedTextDark)
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                if (record.refNo != "-") "Ref: ${record.refNo}" else "No reference number",
                fontSize = 11.sp,
                color = mutedText
            )
        }

        Text(
            "₹${formatOverviewNumber(record.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PaidGreen,
            modifier = Modifier.padding(end = 10.dp)
        )

        PaymentActionIcon(icon = Icons.Default.Visibility, onClick = onView)
        Spacer(Modifier.width(6.dp))
        PaymentActionIcon(icon = Icons.Default.Print, onClick = onPrint)
    }
}

@Composable
private fun PaymentActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SectionBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TabActive, modifier = Modifier.size(16.dp))
    }
}