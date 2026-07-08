package com.cuso.mobile.view.home.sales.sales_order

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.*
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.sales.sales_order.pdfgenerator.OrderReceiptPdfGenerator
import com.cuso.mobile.viewmodel.AssignWorkersState
import com.cuso.mobile.viewmodel.OrderOverviewState
import com.cuso.mobile.viewmodel.OrderOverviewViewModel
import com.cuso.mobile.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────
// THEME
// ─────────────────────────────────────────────────────────────────────────
private val TabActive = Color(0xFF4F46E5)
private val TextPrimary = Color(0xFF111827)
private val TextMuted = Color(0xFF9CA3AF)
private val TextMutedDark = Color(0xFF6B7280)
private val SectionBg = Color(0xFFF9FAFB)
private val BorderLight = Color(0xFFF0F0F0)
private val StatusGreenBg = Color(0xFFDCFCE7)
private val StatusGreenText = Color(0xFF16A34A)
private val StatusRedBg = Color(0xFFFEE2E2)
private val StatusRedText = Color(0xFFDC2626)
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
    val priority: String = "Low",
    val startDate: String? = null,
    val completionDate: String? = null,
    val isAssigned: Boolean = false
)

data class PaymentRecord(
    val date: String,
    val amount: Double,
    val method: String
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
@Composable
fun OrderOverviewScreen(
    orderId: String,
    onClose: () -> Unit = {},
    onEditOrder: (OrderReviewData) -> Unit = {},   // CHANGED — was () -> Unit
    onCreateNew: () -> Unit = {}
) {
    val viewModel: OrderOverviewViewModel = hiltViewModel()
    val salesViewModel: SalesViewModel = hiltViewModel()
    val staffList by salesViewModel.staffList.collectAsStateWithLifecycle()
    val state by viewModel.overviewState.collectAsStateWithLifecycle()
    val currentOrderData = (state as? OrderOverviewState.Success)?.data   // NEW
    val assignState by viewModel.assignWorkersState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(orderId) {
        viewModel.fetchSalesOverview(orderId)
        salesViewModel.fetchStaff()
    }

    // Handle assignment success
    LaunchedEffect(assignState) {
        if (assignState is AssignWorkersState.Success) {
            Toast.makeText(context, "Workers assigned successfully!", Toast.LENGTH_SHORT).show()
            viewModel.fetchSalesOverview(orderId) // Refresh data
            viewModel.resetAssignWorkersState()
        } else if (assignState is AssignWorkersState.Error) {
            Toast.makeText(context, "Assignment failed: ${(assignState as AssignWorkersState.Error).message}", Toast.LENGTH_LONG).show()
            viewModel.resetAssignWorkersState()
        }
    }

    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Garments", "Assignments", "Payment")

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Order Details", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClose() }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                tab,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) TabActive else TextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .fillMaxWidth(0.6f)
                                    .background(if (isSelected) TabActive else Color.Transparent)
                            )
                        }
                    }
                }
                HorizontalDivider(color = BorderLight)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = BorderLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { currentOrderData?.let { onEditOrder(it.toOrderReviewData()) } },
                        enabled = currentOrderData != null,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) { Text("Edit Order", fontWeight = FontWeight.Medium) }
                    Button(
                        onClick = onCreateNew,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TabActive)
                    ) { Text("Create New", fontWeight = FontWeight.Medium, color = Color.White) }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is OrderOverviewState.Loading, OrderOverviewState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TabActive)
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
                            staffList = staffList,
                            orderId = orderId,
                            viewModel = viewModel,
                            assignState = assignState
                        )
                        "Payment" -> PaymentTab(
                            payment = payment,
                            context = context,
                            orderData = s.data
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// EXTRACT FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────
private fun isStageAssigned(stage: OrderOverviewStageStep): Boolean =
    stage.status.lowercase() != "pending" || stage.assignedQuantity > 0

private fun extractGarments(data: OrderOverviewData): List<GarmentDetail> {
    val items = data.items
    val stageGroups = data.stages

    return items.map { item ->
        val stageGroup = stageGroups.firstOrNull { it.garmentItemId == item._id }
        val anyAssigned = stageGroup?.stages?.any { isStageAssigned(it) } == true

        // ── Pull the assigned worker for each stage ──
        val cuttingTailor = stageGroup?.stages
            ?.firstOrNull { it.stageName == "cutting" }
            ?.assignedTo?.firstOrNull()
        val stitchingTailor = stageGroup?.stages
            ?.firstOrNull { it.stageName == "stitching" }
            ?.assignedTo?.firstOrNull()
        val qualityInspector = stageGroup?.stages
            ?.firstOrNull { it.stageName == "qc" }
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
                PaymentRecord(date = "—", amount = amt, method = "Payment")
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
        InfoRow("Name", customer.name)
        InfoRow("Phone", "+91 ${customer.mobile.takeLast(10)}")
        InfoRow("Gender", customer.gender ?: "—")
        InfoRow("Address", listOfNotNull(customer.address?.addressLine, customer.address?.city).joinToString(", ").ifBlank { "—" })

        Spacer(Modifier.height(10.dp))
        SectionTitle("Order Information")
        InfoRow("Order Number", "#${order.orderNumber}")
        InfoRow("Order Date", formatOverviewDate(order.orderDate))
        InfoRow("Trial Date", order.trialDate?.let { formatOverviewDate(it) } ?: "—")
        InfoRow("Delivery Date", order.deliveryDate?.let { formatOverviewDate(it) } ?: "—")
        InfoRow("Branch", order.branch.name)
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

        Spacer(Modifier.height(10.dp))
        SectionTitle("Styling Notes")
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
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onGoToAssignments() }
        )
        garments.forEachIndexed { idx, g ->
            GarmentCard(g)
            if (idx != garments.lastIndex) Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GarmentCard(garment: GarmentDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(bottom = 4.dp)
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
                Text(
                    "Qty: ${garment.quantity} · ₹${formatOverviewNumber(garment.price)}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
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
                color = TextMutedDark,
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
// ASSIGNMENTS TAB - UPDATED WITH CARD UI AS PER IMAGE
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun AssignmentsTab(
    garments: List<GarmentDetail>,
    staffList: List<StaffDto>,
    orderId: String,
    viewModel: OrderOverviewViewModel,
    assignState: AssignWorkersState
) {
    var selectedGarment by remember { mutableStateOf<GarmentDetail?>(null) }
    var showAssignSheet by remember { mutableStateOf(false) }

    // Does ANY garment have at least one worker assigned?
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
                    selectedGarment = garments.first()
                    showAssignSheet = true
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
                    color = TextMuted
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
                        onAssignClick = {
                            selectedGarment = garment
                            showAssignSheet = true
                        }
                    )
                    if (idx != garments.lastIndex) Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Assignment Sheet
    if (showAssignSheet && selectedGarment != null) {
        AssignTailorsSheet(
            garment = selectedGarment!!,
            staffList = staffList,
            orderId = orderId,
            viewModel = viewModel,
            assignState = assignState,
            onDismiss = {
                showAssignSheet = false
                selectedGarment = null
            }
        )
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
                imageVector = Icons.Default.Group,
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
            color = TextMuted,
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
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Assign Worker", color = Color.White, fontWeight = FontWeight.SemiBold)
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
        Text(
            "Add garments to this order first.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    garment.type,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Qty: ${garment.quantity} | ${if (garment.trialRequired) "Trial Required" else "Trial Not Required"}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(12.dp))

            // Assignment rows - Cutting, Stitching, QC
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

            Spacer(Modifier.height(12.dp))

            // Dates row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Start: ${garment.assignment?.startDate ?: "Not set"}",
                    fontSize = 13.sp,
                    color = TextMutedDark
                )
                Text(
                    "Expected: ${garment.assignment?.completionDate ?: "Not set"}",
                    fontSize = 13.sp,
                    color = TextMutedDark
                )
            }

            Spacer(Modifier.height(12.dp))

            // Assign/Reassign button
            Button(
                onClick = onAssignClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFullyAssigned) TabActive else TabActive
                )
            ) {
                Text(
                    if (isFullyAssigned) "Reassign" else "Assign",
                    color = Color.White,
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
            Text(
                "${staff.firstName} ${staff.lastName}",
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(AssignedBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Assigned",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AssignedText
                )
            }
        } else {
            Text(
                "Not Assigned",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(UnassignedBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Not Assigned",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UnassignedText
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ASSIGN TAILORS SHEET
// ─────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignTailorsSheet(
    garment: GarmentDetail,
    staffList: List<StaffDto>,
    orderId: String,
    viewModel: OrderOverviewViewModel,
    assignState: AssignWorkersState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val staffNameToStaff = remember(staffList) {
        staffList.associateBy { "${it.firstName} ${it.lastName}" }
    }
    val staffNames = remember(staffList) { staffNameToStaff.keys.toList() }

    var selectedCutting by remember { mutableStateOf<StaffDto?>(garment.assignment?.cuttingTailor) }
    var selectedStitching by remember { mutableStateOf<StaffDto?>(garment.assignment?.stitchingTailor) }
    var selectedQC by remember { mutableStateOf<StaffDto?>(garment.assignment?.qualityInspector) }
    var priority by remember { mutableStateOf(garment.assignment?.priority?.ifBlank { "Low" } ?: "Low") }
    var completionDate by remember {
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
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ── Header ──
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
                color = TextMuted,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // ── Garment info card ──
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
                    Text(
                        garment.type.take(1),
                        color = ChipPurpleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(garment.type, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Qty: ${garment.quantity}", fontSize = 12.sp, color = TextMuted)
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
                        color = TextMutedDark
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section header ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ContentCut,
                    contentDescription = null,
                    tint = TabActive,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Production Assignment",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TabActive
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Staff dropdowns (reusing FormDropdown) ──
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

            Spacer(Modifier.height(12.dp))

            // ── Priority + Completion Date row ──
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

            // ── Buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                            Toast.makeText(context, "Please select all three workers", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TabActive),
                    enabled = !isAssigning
                ) {
                    if (isAssigning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Assign Workers", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerDropdown(
    label: String,
    staffList: List<StaffDto>,
    selectedStaff: StaffDto?,
    onSelect: (StaffDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 12.sp, color = TextMutedDark, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedStaff?.let { "${it.firstName} ${it.lastName}" } ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select $label", color = TextMuted, fontSize = 13.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = TabActive
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                staffList.forEach { staff ->
                    DropdownMenuItem(
                        text = { Text("${staff.firstName} ${staff.lastName} - ${staff.memberId}") },
                        onClick = {
                            onSelect(staff)
                            expanded = false
                        }
                    )
                }
                if (staffList.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No staff available", color = TextMuted) },
                        onClick = { expanded = false }
                    )
                }
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
    onConvertToInvoice: () -> Unit = {}
) {
    val (statusBg, statusText) = when (payment.status) {
        "PAID" -> StatusGreenBg to StatusGreenText
        "PARTIALLY PAID" -> StatusOrangeBg to StatusOrangeText
        else -> StatusOrangeBg to StatusOrangeText
    }
    val pdfGenerator = remember {
        OrderReceiptPdfGenerator(context)
    }

    val receiptData = remember(orderData) {
        OrderReceiptPdfGenerator.OrderReceiptData(
            orderNumber = orderData.order.orderNumber,
            customerName = orderData.order.customerId.name,
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
        Text("Order ID: ${payment.orderId}", fontSize = 12.sp, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Text("₹${formatOverviewNumber(payment.totalAmount)}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Total Order Amount", fontSize = 12.sp, color = TextMuted)

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Payment Completion", fontSize = 13.sp, color = TextMutedDark)
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
                Text("Paid: ₹${formatOverviewNumber(payment.paidAmount)}", fontSize = 12.sp, color = TextMutedDark)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(BalanceRed))
                Spacer(Modifier.width(6.dp))
                Text("Remaining: ₹${formatOverviewNumber(payment.remainingAmount)}", fontSize = 12.sp, color = TextMutedDark)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Payment History", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))
        if (payment.history.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inbox, contentDescription = null, tint = TextMuted, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text("No payments recorded for this order yet.", fontSize = 13.sp, color = TextMuted)
            }
        } else {
            payment.history.forEach { record ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(record.method, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(record.date, fontSize = 11.sp, color = TextMuted)
                    }
                    Text("₹${formatOverviewNumber(record.amount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PaidGreen)
                }
                HorizontalDivider(color = BorderLight)
            }
        }

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
            Text("Convert to Invoice", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TabActive)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TabActive, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        ActionRow(
            icon = Icons.Default.Print,
            label = "Print Order Receipt",
            onClick = {
                pdfGenerator.printReceiptViaWebView(receiptData)
            }
        )
        ActionRow(
            icon = Icons.Default.Download,
            label = "Download PDF",
            onClick = {
                val pdf = pdfGenerator.generateReceiptPdf(receiptData)
                if (pdf != null) {
                    Toast.makeText(
                        context,
                        "PDF Saved:\n${pdf.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        pdf
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to create PDF",
                        Toast.LENGTH_SHORT
                    ).show()
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
// SHARED UI HELPERS
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
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
        Text(subtitle, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun SmallSectionHeader(title: String, trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
        trailingIcon?.let {
            Icon(it, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
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
        Text(label, fontSize = 13.sp, color = Color.Black)
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
        Text(label, fontSize = 13.sp, color = TextMuted)
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
            Icon(icon, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, fontSize = 13.sp, color = TextPrimary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
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