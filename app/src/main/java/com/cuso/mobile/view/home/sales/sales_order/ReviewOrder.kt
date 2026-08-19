@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "Unused_parameter", "VariableNeverRead", "SameParameterValue"
)

package com.cuso.mobile.view.home.sales.sales_order

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.cuso.mobile.model.sales.OrderOverviewData
import com.cuso.mobile.model.sales.OrderOverviewStageStep
import com.cuso.mobile.model.sales.StaffDto
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.CirculerProgressIndicatorSmall
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.home.pdfgenerator.OrderReceiptPdfGenerator
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.viewmodel.AssignWorkersState
import com.cuso.mobile.viewmodel.ConvertToInvoiceState
import com.cuso.mobile.viewmodel.OrderOverviewState
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.ReceivePaymentState
import com.cuso.mobile.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.light_blue
import com.cuso.mobile.ui.theme.light_blue_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.BackFabButton
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.inventory.FormTextArea

// ─────────────────────────────────────────────────────────────────────────
// THEME COLORS
// ─────────────────────────────────────────────────────────────────────────
private val TabActive = Color(0xFF4F46E5)
private val TextPrimary = Color(0xFF111827)
private val mutedTextDark = Color(0xFF6B7280)
private val SectionBg = Color(0xFFF9FAFB)
private val BorderLight = Color(0xFFF0F0F0)
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

// ─────────────────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────────────────
data class GarmentDetail(
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
    val assignment: GarmentAssignment? = null
) {
    val total: Double get() = baseCost + additionalCharges - discount
}

data class GarmentAssignment(
    val cuttingTailor: StaffDto? = null,
    val stitchingTailor: StaffDto? = null,
    val qualityInspector: StaffDto? = null,
    val trial: StaffDto? = null,
    val priority: String = "Low",
    val startDate: String? = null,
    val completionDate: String? = null,
    val isAssigned: Boolean = false
)

data class PaymentRecord(
    val date: String,
    val amount: Double,
    val method: String,
    val refNo: String = "-"
)

data class PaymentInfo(
    val orderId: String,
    val status: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val history: List<PaymentRecord> = emptyList(),
    val additionalCharges: Double = 0.0,
    val discount: Double = 0.0
) {
    val remainingAmount: Double get() = totalAmount - paidAmount
    val paidTotal: Double get() = paidAmount
    val balancePending: Double get() = remainingAmount
    val completionPercent: Int
        get() = if (totalAmount <= 0.0) 0 else ((paidAmount / totalAmount) * 100).toInt().coerceIn(0, 100)
}

// ─────────────────────────────────────────────────────────────────────────
// SCREEN ROOT
// ─────────────────────────────────────────────────────────────────────────
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun OrderOverviewScreen(
    orderId: String,
    onClose: () -> Unit = {},
    onEditOrder: (OrderReviewData) -> Unit = {},
    onCreateNew: () -> Unit = {}
) {
    // Blur & Scrim States — Payment sheet (lifted to root, same pattern as assign sheet)
    var paymentSheetBlur by remember { mutableStateOf(0.dp) }
    var paymentSheetScrim by remember { mutableFloatStateOf(0f) }
    var paymentSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val isPaymentSheetOpen = paymentSheetState != SheetValue.Hidden

    fun closePaymentSheet() {
        paymentSheetState = SheetValue.Hidden
        paymentSheetBlur = 0.dp
        paymentSheetScrim = 0f
    }
    val viewModel: OrderOverviewViewModel = hiltViewModel(key = "order_overview_view_$orderId")
    val salesViewModel: SalesViewModel = hiltViewModel()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val state by viewModel.overviewState.collectAsStateWithLifecycle()
    val currentOrderData = (state as? OrderOverviewState.Success)?.data
    val assignState by viewModel.assignWorkersState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Root-level notification state — drives the DynamicIsland banners for
    // every success/error event on this screen (assign workers, payments,
    // validation errors, PDF actions, etc).
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

    // Payment success/error, same pattern as assignState above
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

    // Blur & Scrim States — Assign sheet (existing)
    var assignSheetBlur by remember { mutableStateOf(0.dp) }
    var assignSheetScrim by remember { mutableFloatStateOf(0f) }

    var selectedGarmentForSheet by remember { mutableStateOf<GarmentDetail?>(null) }
    var assignSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    val isAssignSheetOpen = assignSheetState != SheetValue.Hidden

    fun closeAssignSheet() {
        assignSheetState = SheetValue.Hidden
        assignSheetBlur = 0.dp
        assignSheetScrim = 0f
        selectedGarmentForSheet = null
    }

    // Combined blur/scrim so header + content blur when EITHER sheet is open
    val combinedSheetBlur = maxOf(assignSheetBlur, paymentSheetBlur)
    val combinedSheetScrim = maxOf(assignSheetScrim, paymentSheetScrim)
    val isAnySheetOpen = isAssignSheetOpen || isPaymentSheetOpen

    // Payment data needed at root level so ReceivePaymentSheet can render as a sibling
    // of the tab content (same pattern as AssignTailorsSheet using selectedGarmentForSheet)
    val paymentInfoForSheet = currentOrderData?.let { remember(it) { extractPayment(it) } }

    // Outer wrapper Box: keeps the notification banners above the Scaffold's
    // topBar (higher z-order) at the same TopCenter position, so they are
    // never clipped under the header.
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
                Column(modifier = Modifier.fillMaxSize()
                    .blurScrim(radius = combinedSheetBlur)) {

                    Column(modifier = Modifier.background(whiteBg)) {
                        // Get the index of the selected tab for horizontal indicator animation
                        val selectedIndex = tabs.indexOf(selectedTab)

                        // Animate the horizontal position of the indicator dash
                        val indicatorOffset by animateFloatAsState(
                            targetValue = selectedIndex.toFloat(),
                            label = "TabIndicatorOffset"
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            // Calculate the dynamic width of each tab based on screen size
                            val tabWidth = maxWidth / tabs.size

                            Row(modifier = Modifier.fillMaxWidth()) {
                                tabs.forEach { tab ->
                                    val isSelected = tab == selectedTab

                                    // Smooth transition for text color
                                    val animatedTextColor by animateColorAsState(
                                        targetValue = if (isSelected) TabActive else mutedText,
                                        label = "TabTextColor"
                                    )

                                    // Smooth transition for scale (makes the active tab larger)
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
                                            // FontWeight stays bold for active tab
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = animatedTextColor,
                                            modifier = Modifier
                                                .scale(animatedScale) // Apply the animated size increase
                                                .padding(bottom = 4.dp)
                                        )
                                        // Spacer to ensure consistent height between states
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }

                            // Single animated sliding indicator dash
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(tabWidth * 0.5f) // Set width to 50% of the tab area
                                    .height(3.dp)
                                    // Slide the dash horizontally based on the animated index
                                    .offset(x = (tabWidth * indicatorOffset) + (tabWidth * 0.25f))
                                    .background(
                                        color = TabActive,
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                        }
                        HorizontalDivider(color = BorderLight)
                    }

                    // ── CONTENT AREA — blur + bottom sheet CONFINED here only ──
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

                        // Tab content — this alone gets blurred
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
                                            Text("Failed to load order", color = Color.Red, fontWeight = FontWeight.Bold)
                                            Text(s.message, color = Color.Gray, fontSize = 13.sp)
                                            Spacer(Modifier.height(12.dp))
                                            Button(onClick = { viewModel.fetchSalesOverview(orderId) }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                                is OrderOverviewState.Success -> {
                                    val garments: List<GarmentDetail> = remember(s.data) { extractGarments(s.data) }
                                    val payment: PaymentInfo = remember(s.data) { extractPayment(s.data) }

                                    when (selectedTab) {
                                        "Overview" -> OverviewTab(s.data)
                                        "Garments" -> GarmentsTab(
                                            garments = garments,
                                            onGoToAssignments = { selectedTab = "Assignments" }
                                        )
                                        "Assignments" -> AssignmentsTab(
                                            garments = garments,
                                            onAssignClick = { garment ->
                                                selectedGarmentForSheet = garment
                                                assignSheetState = SheetValue.Collapsed
                                            }
                                        )
                                        "Payment" -> PaymentTab(
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

                        // ── FABs (moved inside content Box too, same behavior) ──
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isAnySheetOpen,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            StepNavigationFab(
                                showBack = true,
                                onBack = { currentOrderData?.let { onEditOrder(it.toOrderReviewData()) } },
                                backEnabled = currentOrderData != null,
                                backLabel = "Edit Order",
                                backWidthFraction = 0.45f,
                                trailingWidthFraction = 0.45f,
                                trailingAction = TrailingFabAction.Next(
                                    label = "Create New",
                                    onClick = onCreateNew
                                )
                            )
                        }

                        // ── THE HALF-PAGE BOTTOM SHEET — confined to content Box only ──
                    }
                }
                AssignTailorsSheet(
                    garment = selectedGarmentForSheet,
                    staffList = staffList,
                    orderId = orderId,
                    viewModel = viewModel,
                    assignState = assignState,
                    sheetState = assignSheetState,
                    onStateChange = { assignSheetState = it },
                    onBlurScrimChange = { r, sc -> assignSheetBlur = r; assignSheetScrim = sc },
                    topInset = 66.dp,   // 0 here = "top of content Box" = right below header
                    onDismiss = { closeAssignSheet() },
                    onError = { msg -> errorMessage = msg }
                )

                // ReceivePaymentSheet rendered at root, same level as AssignTailorsSheet
                if (paymentInfoForSheet != null) {
                    ReceivePaymentSheet(
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

        // Notification banners live in the outer Box, on top of the entire
        // Scaffold (including topBar), so they are never clipped.
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


// ─────────────────────────────────────────────────────────────────────────
// EXTRACT HELPERS
// ─────────────────────────────────────────────────────────────────────────
private fun isStageAssigned(stage: OrderOverviewStageStep): Boolean =
    stage.status.lowercase() != "pending" || stage.assignedQuantity > 0

private fun extractGarments(data: OrderOverviewData): List<GarmentDetail> {
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

        GarmentDetail(
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
            assignment = GarmentAssignment(
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
    "paid" -> "PAID"
    "unpaid" -> "UNPAID"
    "partially paid", "partial" -> "PARTIALLY PAID"
    else -> raw.uppercase()
}

private fun extractPayment(data: OrderOverviewData): PaymentInfo {
    val order = data.order
    val payments = data.payments

    return PaymentInfo(
        orderId = order.orderNumber,
        status = normalizePaymentStatus(order.paymentStatus),
        totalAmount = order.totalAmount,
        paidAmount = order.totalPaid,
        history = payments.mapNotNull { p ->
            p.amount?.let { amt ->
                PaymentRecord(
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
// OVERVIEW TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OverviewTab(data: OrderOverviewData) {
    val order = data.order
    val customer = order.customerId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SectionTitle("Customer Information")
        InfoRow("Name", customer?.name ?: "—")
        InfoRow("Phone", "+91 ${customer?.mobile?.takeLast(10)}")
        InfoRow("Gender", customer?.gender ?: "—")
        InfoRow("Address", listOfNotNull(customer?.address?.addressLine, customer?.address?.city).joinToString(", ").ifBlank { "—" })

        Spacer(Modifier.height(10.dp))
        SectionTitle("Order Information")
        InfoRow("Order Number", "#${order.orderNumber}")
        InfoRow("Order Date", order.orderDate?.let { formatOverviewDate(it) } ?: "—")
        InfoRow("Trial Date", order.trialDate?.let { formatOverviewDate(it) } ?: "—")
        InfoRow("Delivery Date", order.deliveryDate?.let { formatOverviewDate(it) } ?: "—")
        InfoRow("Branch", order.branch?.name ?: "Not Assigned")
        InfoRowWithBadge("Status", order.status)

        Spacer(Modifier.height(10.dp))
        SectionTitle("Payment Snapshot")
        InfoRow("Net Order Amount", "₹${formatOverviewNumber(order.totalAmount)}")
        InfoRow("Paid Amount", "₹${formatOverviewNumber(order.totalPaid)}", valueColor = PaidGreen)
        InfoRow("Balance Due", "₹${formatOverviewNumber(order.balanceAmount)}", valueColor = BalanceRed)

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("₹${formatOverviewNumber(order.totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TabActive)
        }

        Spacer(Modifier.height(90.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────
// GARMENTS TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun GarmentsTab(
    garments: List<GarmentDetail>,
    onGoToAssignments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        SectionBlock(
            title = "Garments List",
            subtitle = "View measurements, models, and fabric details for ${garments.size} garment(s)."
        )
        Text(
            "Go To Assignment Mode →",
            fontSize = 13.sp,
            color = TabActive,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 6.dp, bottom = 16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onGoToAssignments() }
        )
        garments.forEachIndexed { idx, g ->
            GarmentCard(g)
            if (idx != garments.lastIndex) Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun GarmentCard(garment: GarmentDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(garment.type.take(1), color = ChipPurpleText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(garment.type, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Qty: ${garment.quantity} · ₹${formatOverviewNumber(garment.price)}", fontSize = 12.sp, color = mutedText)
            }
            Tag("${garment.priority} Priority", StatusGreyBg, StatusGreyText)
            Spacer(Modifier.width(6.dp))
            Tag(if (garment.trialRequired) "Trial Required" else "Trial Not required", StatusGreyBg, StatusGreyText)
        }

        if (garment.selectedModels.isNotEmpty()) {
            SmallSectionHeader("SELECTED MODELS")
            Row(Modifier.padding(vertical = 10.dp)) {
                garment.selectedModels.forEach { model ->
                    Tag(model, ChipPurpleBg, ChipPurpleText)
                    Spacer(Modifier.width(8.dp))
                }
            }
            HorizontalDivider(color = BorderLight)
        }

        SmallSectionHeader("FABRIC DETAILS")
        InfoRow("Fabric Source", garment.fabricSource)
        InfoRow("Pattern", garment.pattern)

        SmallSectionHeader("CLIENT MEASUREMENTS", trailingIcon = Icons.Default.Edit)
        Row(Modifier.padding(vertical = 10.dp)) {
            garment.measurements.forEach { (label, value) ->
                Tag("$label $value", SectionBg, TextPrimary, bordered = true)
                Spacer(Modifier.width(8.dp))
            }
        }
        HorizontalDivider(color = BorderLight)

        if (garment.notes.isNotBlank()) {
            SmallSectionHeader("NOTES")
            Text(
                garment.notes,
                fontSize = 13.sp,
                color = mutedTextDark,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HorizontalDivider(color = BorderLight)
        }

        SmallSectionHeader("GARMENT COST")
        InfoRow("Base Cost", "₹${formatOverviewNumber(garment.baseCost)}")
        InfoRow("Additional Charges", "₹${formatOverviewNumber(garment.additionalCharges)}")
        InfoRow("Discount", "-₹${formatOverviewNumber(garment.discount)}", valueColor = PaidGreen)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("₹${formatOverviewNumber(garment.total)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TabActive)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ASSIGNMENTS TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun AssignmentsTab(
    garments: List<GarmentDetail>,
    onAssignClick: (GarmentDetail) -> Unit
) {
    val hasAnyWorkerAssigned = garments.any { g ->
        g.assignment?.cuttingTailor != null ||
                g.assignment?.stitchingTailor != null ||
                g.assignment?.qualityInspector != null
    }

    when {
        garments.isEmpty() -> {
            EmptyAssignmentsState()
        }
        !hasAnyWorkerAssigned -> {
            NoWorkersAssignedState(
                onAssignWorker = {
                    garments.firstOrNull()?.let { onAssignClick(it) }
                }
            )
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Garment Assignments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Assign cutting, stitching and QC workers to each garment",
                    fontSize = 13.sp,
                    color = mutedText
                )
                Spacer(Modifier.height(16.dp))

                garments.forEachIndexed { idx, garment ->
                    val hasCutting = garment.assignment?.cuttingTailor != null
                    val hasStitching = garment.assignment?.stitchingTailor != null
                    val hasQC = garment.assignment?.qualityInspector != null
                    val isFullyAssigned = hasCutting && hasStitching && hasQC

                    AssignmentCard(
                        garment = garment,
                        isFullyAssigned = isFullyAssigned,
                        onAssignClick = { onAssignClick(garment) }
                    )
                    if (idx != garments.lastIndex) Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun NoWorkersAssignedState(
    onAssignWorker: () -> Unit
) {
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
                painter = painterResource( R.drawable.ic_users),
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
            Icon(Icons.Default.Group, contentDescription = null, tint = ChipPurpleText, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No garments to assign", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Add garments to this order first.", fontSize = 13.sp, color = mutedText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AssignmentCard(
    garment: GarmentDetail,
    isFullyAssigned: Boolean,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(garment.type, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "Qty: ${garment.quantity} | ${if (garment.trialRequired) "Trial Required" else "Trial Not Required"}",
                    fontSize = 12.sp,
                    color = mutedText
                )
            }

            Spacer(Modifier.height(12.dp))

            AssignmentRow(
                label = "Cutting",
                staff = garment.assignment?.cuttingTailor,
                isAssigned = garment.assignment?.cuttingTailor != null
            )
            AssignmentRow(
                label = "Stitching",
                staff = garment.assignment?.stitchingTailor,
                isAssigned = garment.assignment?.stitchingTailor != null
            )
            AssignmentRow(
                label = "QC",
                staff = garment.assignment?.qualityInspector,
                isAssigned = garment.assignment?.qualityInspector != null
            )
            if (garment.trialRequired) {

                AssignmentRow(
                    label = "Trial",
                    staff = garment.assignment?.trial,
                    isAssigned = garment.assignment?.trial != null
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start: ${garment.assignment?.startDate ?: "Not set"}", fontSize = 13.sp, color = mutedTextDark)
                Text("Expected: ${garment.assignment?.completionDate ?: "Not set"}", fontSize = 13.sp, color = mutedTextDark)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAssignClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TabActive)
            ) {
                Text(
                    if (isFullyAssigned) "Reassign" else "Assign",
                    color = whiteBg,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AssignmentRow(
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
// ASSIGN TAILORS SHEET
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun AssignTailorsSheet(
    garment: GarmentDetail?,
    staffList: List<StaffDto>,
    orderId: String,
    viewModel: OrderOverviewViewModel,
    assignState: AssignWorkersState,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: Dp, scrim: Float) -> Unit = { _, _ -> },
    topInset: Dp = 66.dp,
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
    var selectedTrial by remember(garment.id) { mutableStateOf(garment.assignment?.trial) }

    var priority by remember(garment.id) { mutableStateOf(garment.assignment?.priority?.ifBlank { "Low" } ?: "Low") }
    var completionDate by remember(garment.id) {
        mutableStateOf(
            garment.assignment?.completionDate
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date().apply { time += 30L * 24 * 60 * 60 * 1000 })
        )
    }

    var cuttingExpanded by remember { mutableStateOf(false) }
    var stitchingExpanded by remember { mutableStateOf(false) }
    var qcExpanded by remember { mutableStateOf(false) }
    var trialExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    val isAssigning = assignState is AssignWorkersState.Loading

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.75f,   // 75% of the container height — safe across screen sizes
        topInset = topInset,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange
    ) {
        // Added Surface to ensure the sheet has a solid background and doesn't look "see-through"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = whiteBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {

                Text(
                    "ASSIGN TAILORS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Manage workers for ${garment.type}.",
                    fontSize = 12.sp,
                    color = mutedText,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SectionBg, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(garment.type.take(1), color = ChipPurpleText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(garment.type, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Qty: ${garment.quantity}", fontSize = 12.sp, color = mutedText)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "TRIAL: ${if (garment.trialRequired) "YES" else "NO"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = mutedTextDark
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = TabActive, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Production Assignment", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TabActive)
                }

                Spacer(Modifier.height(12.dp))

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

                if (garment.trialRequired) {
                    Spacer(Modifier.height(12.dp))

                    FormDropdown(
                        label = "Trial",
                        value = selectedTrial?.let { "${it.firstName} ${it.lastName}" }
                            ?: "Select an option",
                        expanded = trialExpanded,
                        onExpandChange = { trialExpanded = it },
                        options = staffNames,
                        onOptionSelected = { name ->
                            staffNameToStaff[name]?.let {
                                selectedTrial = it
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

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

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        enabled = !isAssigning
                    ) {
                        Text("Cancel", color = TextPrimary, fontWeight = FontWeight.Medium)
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
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TabActive),
                        enabled = !isAssigning
                    ) {
                        if (isAssigning) {
                            CirculerProgressIndicatorReuse()
                        } else {
                            Text("Assign Workers", color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PAYMENT TAB
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun PaymentTab(
    payment: PaymentInfo,
    context: Context,
    orderData: OrderOverviewData,
    viewModel: OrderOverviewViewModel,
    sheetState: SheetValue,
    onOpenSheet: () -> Unit,
    onConvertToInvoice: () -> Unit = {},
    onShowSuccess: (String) -> Unit = {},
    onShowError: (String) -> Unit = {}
) {
    val (statusBg, statusText) = when (payment.status) {
        "PAID" -> StatusGreenBg to StatusGreenText
        "PARTIALLY PAID" -> StatusOrangeBg to StatusOrangeText
        else -> StatusOrangeBg to StatusOrangeText
    }
    val pdfGenerator = remember { OrderReceiptPdfGenerator(context) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Tag(payment.status, statusBg, statusText)
        Spacer(Modifier.height(10.dp))
        Text("Order ID: ${payment.orderId}", fontSize = 12.sp, color = mutedText)
        Spacer(Modifier.height(6.dp))
        Text("₹${formatOverviewNumber(payment.totalAmount)}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Total Order Amount", fontSize = 12.sp, color = mutedText)

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Payment Completion", fontSize = 13.sp, color = mutedTextDark)
            Text("${payment.completionPercent}%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { payment.completionPercent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
            color = TabActive,
            trackColor = Color(0xFFE5E7EB)
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(PaidGreen))
                Spacer(Modifier.width(6.dp))
                Text("Paid: ₹${formatOverviewNumber(payment.paidAmount)}", fontSize = 12.sp, color = mutedTextDark)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(BalanceRed))
                Spacer(Modifier.width(6.dp))
                Text("Remaining: ₹${formatOverviewNumber(payment.remainingAmount)}", fontSize = 12.sp, color = mutedTextDark)
            }
        }

        if (payment.remainingAmount > 0.0) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpenSheet,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TabActive)
            ) {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Receive Payment", color = whiteBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        PaymentHistorySection(
            history = payment.history,
            onView = { _ -> },
            onPrint = { _ -> }
        )

        Spacer(Modifier.height(20.dp))
        Text("Documents & Actions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SectionBg, RoundedCornerShape(10.dp))
                .clickable { onConvertToInvoice() }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConvertToInvoiceButton(
                viewModel = viewModel,
                salesOrderId = orderData.order._id,
                isPaymentDone = orderData.order.balanceAmount == 0.0,
                invoiceAlreadyExists = !orderData.order.invoiceId.isNullOrBlank()
            )
        }
        Spacer(Modifier.height(4.dp))
        ActionRow(
            icon = Icons.Default.Print,
            label = "Print Order Receipt",
            onClick = { pdfGenerator.printReceiptViaWebView(receiptData) }
        )
        ActionRow(
            icon = Icons.Default.Download,
            label = "Download PDF",
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
        Spacer(Modifier.height(20.dp))
        Text("Summary", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        InfoRow("Additional Charges", "₹${formatOverviewNumber(payment.additionalCharges)}")
        InfoRow("Discount", "₹${formatOverviewNumber(payment.discount)}", valueColor = PaidGreen)
        InfoRow("Paid Total", "₹${formatOverviewNumber(payment.paidTotal)}")
        InfoRow("Balance Pending", "₹${formatOverviewNumber(payment.balancePending)}", valueColor = BalanceRed)
        Spacer(Modifier.height(24.dp))
    }
}
// ─────────────────────────────────────────────────────────────────────────
// RECEIVE PAYMENT SHEET
// ─────────────────────────────────────────────────────────────────────────

@Composable
private fun ReceivePaymentSheet(
    orderId: String,
    balanceDue: Double,
    viewModel: OrderOverviewViewModel,
    isSaving: Boolean,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurChange: (Dp) -> Unit,
    topInset: Dp = 66.dp,
    onDismiss: () -> Unit
) {
    // Local states for payment input fields
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

    // Sync amount if Full Amount is selected
    LaunchedEffect(isFullAmount) {
        if (isFullAmount) amountText = formatAmountPlain(balanceDue)
    }

    // Reset sheet fields when hidden
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
        collapsedFraction = 0.9f,
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
                // Header Label
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        "RECEIVE PAYMENT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Balance Summary Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_blue, RoundedCornerShape(10.dp))
                        .border(1.dp,light_blue_border, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balance Due", fontSize = 13.sp, color = TextPrimary)
                    Text("₹${formatOverviewNumber(balanceDue)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BalanceRed)
                }

                Spacer(Modifier.height(16.dp))

                // Payment Type Selection
                FormLabel("Payment Type")
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentTypeOption(
                        label = "Full Amount",
                        selected = isFullAmount,
                        modifier = Modifier.weight(1f),
                        onClick = { isFullAmount = true }
                    )
                    PaymentTypeOption(
                        label = "Partial Amount",
                        selected = !isFullAmount,
                        modifier = Modifier.weight(1f),
                        onClick = { isFullAmount = false }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Amount and Method row
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

                // Reference and Date row
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

                // Push buttons to the bottom
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(16.dp))


                /**
                 * BUTTON SECTION: Arranged to match the requested image
                 */
                Column(modifier = Modifier.fillMaxWidth()) {

                    // First Row: Cancel (Left) and Save Only (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reusing BackFabButton for 'Cancel' as per image (light border)
                        BackFabButton(
                            label = "Cancel",
                            onClick = onDismiss,
                            enabled = !isSaving,
                            showArrow = false,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )

                        // 'Save Only' Outlined Button with Blue/Primary border to match image
                        OutlinedButton(
                            onClick = {
                                submitPayment(viewModel, orderId, amountText, selectedMethod, referenceNo, notes, paymentDate)
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

                    // Second Row: Save & Print (Full Width Solid Primary)
                    Button(
                        onClick = {
                            submitPayment(viewModel, orderId, amountText, selectedMethod, referenceNo, notes, paymentDate)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
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
private fun PaymentTypeOption(
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
                color = if (selected) TabActive else Color(0xFFE5E7EB),
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

private fun submitPayment(
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

// ─────────────────────────────────────────────────────────────────────────
// SHARED UI HELPERS
// ─────────────────────────────────────────────────────────────────────────
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
fun ConvertToInvoiceButton(
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
private fun PaymentHistorySection(
    history: List<PaymentRecord>,
    onView: (PaymentRecord) -> Unit,
    onPrint: (PaymentRecord) -> Unit
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
                    PaymentHistoryRow(
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
private fun PaymentHistoryRow(
    record: PaymentRecord,
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