package com.cuso.mobile.view.home.logistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.inventory.AccordionSection
import com.cuso.mobile.view.home.reusablecomposables.SmoothBottomSheet
import com.cuso.mobile.view.home.reusablecomposables.SheetValue
import com.cuso.mobile.view.home.reusablecomposables.blurScrim
import com.cuso.mobile.view.home.sales.customer.OrderStatusStepper

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = Color(0xFF111827)
private val MutedColor = Color(0xFF9CA3AF)
private val InTransitBg = Color(0xFFEDE9FE)
private val InTransitText = Color(0xFF6D28D9)
private val SuccessColor = Color(0xFF16A34A)
private val StepInactiveBg = Color(0xFFF3F4F6)
private val StepInactiveText = Color(0xFF9CA3AF)

private enum class StepState { DONE, ACTIVE, PENDING }
private data class DeliveryStep(val label: String, val index: Int)

// ── Design tokens for sheets ──
private val RecommendedColor = Color(0xFF4F39F6)
private val SuccessGreen = Color(0xFF16A34A)
private val BusyOrange = Color(0xFFEA580C)
private val UnavailableRed = Color(0xFFEF4444)
private val SelectedBg = Color(0xFFEEF0FF)

private data class StaffMember(
    val name: String,
    val role: String,
    val activeDeliveries: Int,
    val successRate: Int,
    val availability: String,   // "Available" | "Busy" | "Unavailable"
    val isRecommended: Boolean = false,
    val nearestRouteMatch: Boolean = false
)

private val ActionButtonHeight = 40.dp
private val ActionButtonContentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)

// ── STEPPER COMPOSABLE ──
//@Composable
//fun OrderStatusStepper(
//    stepLabels: List<String>,
//    currentStep: Int,
//    modifier: Modifier = Modifier
//) {
//    Column(modifier = modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.Top
//        ) {
//            stepLabels.forEachIndexed { index, label ->
//                val done = index < currentStep
//                val active = index == currentStep
//
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(28.dp)
//                            .background(
//                                when {
//                                    done -> Color(0xFF22C55E)
//                                    else -> Color.White
//                                },
//                                CircleShape
//                            )
//                            .border(
//                                width = if (active) 2.dp else 1.5.dp,
//                                color = when {
//                                    done -> Color(0xFF22C55E)
//                                    active -> Color(0xFF3B3BF9)
//                                    else -> Color(0xFFE5E7EB)
//                                },
//                                shape = CircleShape
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        when {
//                            done -> Icon(
//                                Icons.Default.Check,
//                                contentDescription = null,
//                                tint = Color.White,
//                                modifier = Modifier.size(14.dp)
//                            )
//                            active -> Box(
//                                modifier = Modifier
//                                    .size(10.dp)
//                                    .background(Color(0xFF3B3BF9), CircleShape)
//                            )
//                            else -> Text(
//                                "${index + 1}",
//                                color = Color(0xFF9CA3AF),
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(6.dp))
//
//                    if (active) {
//                        Text(
//                            text = label,
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF3B3BF9),
//                            textAlign = TextAlign.Center,
//                            maxLines = 1,
//                            softWrap = false,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier.wrapContentWidth()
//                        )
//                    } else {
//                        Spacer(modifier = Modifier.height(18.dp))
//                    }
//                }
//
//                if (index != stepLabels.lastIndex) {
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(top = 13.dp)
//                    ) {
//                        Canvas(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(2.dp)
//                        ) {
//                            drawLine(
//                                color = if (index < currentStep) Color(0xFF22C55E) else Color(0xFFE5E7EB),
//                                start = Offset(0f, size.height / 2),
//                                end = Offset(size.width, size.height / 2),
//                                strokeWidth = size.height,
//                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    onDismiss: () -> Unit = {},
    onAssignStaffConfirm: (String) -> Unit = {},
    onUpdateStatusConfirm: (String) -> Unit = {},
    onMarkCompleted: () -> Unit = {}
) {
    val recipientName = "Raji"
    val orderCode = "001"
    var status by remember { mutableStateOf("In Transit") }
    val customer = "8778239060"
    val estDelivery = "25 Feb 2026"
    val deliveryLocation = "Chennai"

    // ── Stepper labels ──
    val stepLabels = listOf("Ready", "Assigned", "In Transit", "Out For Delivery", "Delivered")
    val currentStepIndex = 3 // In Transit

    var expandedSection by remember { mutableStateOf("Package Details") }

    var courierPartner by remember { mutableStateOf("Select Courier") }
    var courierExpanded by remember { mutableStateOf(false) }
    var serviceType by remember { mutableStateOf("Select Service") }
    var serviceExpanded by remember { mutableStateOf(false) }

    var weight by remember { mutableStateOf("0.0") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    val totalQuantity = "1"
    val subtotal = "₹299.00"
    val deliveryCharge = "₹12.50"
    val grandTotal = "₹311.50"

    // Bottom sheet states
    var assignStaffSheetState by remember {
        mutableStateOf(SheetValue.Hidden)
    }

    var updateStatusSheetState by remember {
        mutableStateOf(SheetValue.Hidden)
    }
    var selectedStaff by remember { mutableStateOf<String?>(null) }

    // Blur states
    var assignSheetBlur by remember { mutableStateOf(0.dp) }
    var updateStatusSheetBlur by remember { mutableStateOf(0.dp) }

    // Determine if any sheet is open
    val isAnySheetOpen = assignStaffSheetState != SheetValue.Hidden || updateStatusSheetState != SheetValue.Hidden
    val currentBlur = when {
        assignStaffSheetState != SheetValue.Hidden -> assignSheetBlur
        updateStatusSheetState != SheetValue.Hidden -> updateStatusSheetBlur
        else -> 0.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── HEADER - Always solid (NO BLUR) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Delivery management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TitleColor)
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = LabelColor,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
            HorizontalDivider(color = BorderColor)

            // ── Stepper ──
            OrderStatusStepper(
                stepLabels = stepLabels,
                currentStep = currentStepIndex,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = BorderColor)
        }

        // ── MAIN CONTENT with blur ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
                .padding(top = 160.dp) // Push content below header + stepper
                .blurScrim(
                    if (isAnySheetOpen) currentBlur else 0.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$recipientName / $orderCode", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitleColor)
                        Box(
                            modifier = Modifier
                                .background(InTransitBg, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(status, fontSize = 12.sp, color = InTransitText, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryColumn(label = "Customer", value = customer, modifier = Modifier.weight(1f))
                        SummaryColumn(label = "Est.Delivery", value = estDelivery, modifier = Modifier.weight(1f))
                        SummaryColumn(label = "Delivery Location", value = deliveryLocation, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            assignStaffSheetState = SheetValue.Expanded
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                        contentPadding = ActionButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ActionButtonHeight)
                    ) {
                        Text(
                            selectedStaff?.let { "Assigned: $it" } ?: "+ Assign Staff",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            updateStatusSheetState = SheetValue.Expanded
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ActionButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ActionButtonHeight)
                    ) {
                        Text(
                            "Update Status",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    AccordionSection(
                        icon = Icons.Filled.LocalShipping,
                        title = "Courier Selection",
                        expanded = expandedSection == "Courier Selection",
                        onHeaderClick = { expandedSection = if (expandedSection == "Courier Selection") "" else "Courier Selection" }
                    ) {
                        FormDropdown(
                            label = "Courier Partner",
                            value = courierPartner,
                            expanded = courierExpanded,
                            onExpandChange = { courierExpanded = it },
                            options = listOf("Delhivery", "Blue Dart", "DTDC", "India Post"),
                            onOptionSelected = { courierPartner = it }
                        )
                        Spacer(Modifier.height(16.dp))
                        FormDropdown(
                            label = "Service Type",
                            value = serviceType,
                            expanded = serviceExpanded,
                            onExpandChange = { serviceExpanded = it },
                            options = listOf("Standard", "Express", "Same Day"),
                            onOptionSelected = { serviceType = it }
                        )
                    }

                    AccordionSection(
                        icon = Icons.Filled.Inventory2,
                        title = "Package Details",
                        expanded = expandedSection == "Package Details",
                        onHeaderClick = { expandedSection = if (expandedSection == "Package Details") "" else "Package Details" }
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Weight (kg)")
                                FormTextField(value = weight, onValueChange = { weight = it }, placeholder = "0.0", keyboardType = KeyboardType.Number)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Length (cm)")
                                FormTextField(value = length, onValueChange = { length = it }, placeholder = "Optional", keyboardType = KeyboardType.Number)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Width (cm)")
                                FormTextField(value = width, onValueChange = { width = it }, placeholder = "Optional", keyboardType = KeyboardType.Number)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Height (cm)")
                                FormTextField(value = height, onValueChange = { height = it }, placeholder = "Optional", keyboardType = KeyboardType.Number)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Order Summary", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
                    Spacer(Modifier.height(12.dp))
                    SummaryRow("Total quantity", totalQuantity)
                    SummaryRow("Subtotal", subtotal)
                    SummaryRow("Delivery Charge", deliveryCharge)
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(Modifier.height(10.dp))
                    SummaryRow("Grand Total", grandTotal, isBold = true)
                    Spacer(Modifier.height(90.dp))
                }

                // ── Fixed bottom "Mark as Completed" button ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onMarkCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ActionButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ActionButtonHeight)
                    ) {
                        Text("Mark as Completed",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // ── Assign Staff Bottom Sheet ──
        SmoothBottomSheet(
            state = assignStaffSheetState,
            onStateChange = {
                assignStaffSheetState = it
            },
            peekHeight = 520.dp,
            onDismissRequest = {
                assignStaffSheetState = SheetValue.Hidden
            },
            onBlurScrimChange = { blur, _ ->
                assignSheetBlur = blur
            },
            sheetBackgroundColor = Color.White,
            maxScrimAlpha = 0.4f,
            maxBlurRadius = 14.dp,
            topInset = 160.dp
        ) {
            val staffList = remember {
                listOf(
                    StaffMember("Antonio Rossi", "Senior Delivery Executive", 4, 98, "Available", isRecommended = true, nearestRouteMatch = true),
                    StaffMember("Maria Santos", "Delivery Executive", 3, 95, "Available"),
                    StaffMember("Raj Patel", "Senior Delivery Executive", 12, 97, "Busy"),
                    StaffMember("Chen Wei", "Delivery Executive", 0, 0, "Unavailable")
                )
            }
            var searchQuery by remember { mutableStateOf("") }
            var tempSelectedStaff by remember { mutableStateOf(staffList.firstOrNull { it.isRecommended }?.name) }
            var instructionNote by remember { mutableStateOf("") }

            val filteredStaff = remember(searchQuery, staffList) {
                if (searchQuery.isBlank()) staffList
                else staffList.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
            ) {
                Text(
                    "STAFF ALLOCATION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MutedColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TitleColor),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Search Staff..", fontSize = 14.sp, color = MutedColor)
                                inner()
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = LabelColor, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    filteredStaff.forEach { staff ->
                        val isSelected = tempSelectedStaff == staff.name
                        val isDisabled = staff.availability == "Unavailable"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SelectedBg else Color.Transparent)
                                .clickable(enabled = !isDisabled) { tempSelectedStaff = staff.name }
                                .padding(vertical = 12.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (!isDisabled) tempSelectedStaff = staff.name },
                                enabled = !isDisabled,
                                colors = RadioButtonDefaults.colors(selectedColor = RecommendedColor),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        staff.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDisabled) MutedColor else TitleColor
                                    )
                                    if (staff.isRecommended) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("Recommended", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RecommendedColor)
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(staff.role, fontSize = 12.sp, color = MutedColor)
                                Spacer(Modifier.height(4.dp))

                                if (isDisabled) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Active Deliveries: —", fontSize = 12.sp, color = MutedColor)
                                        Spacer(Modifier.width(10.dp))
                                        Box(modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(UnavailableRed))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Unavailable", fontSize = 12.sp, color = UnavailableRed)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Active Deliveries: ${staff.activeDeliveries}", fontSize = 12.sp, color = LabelColor)
                                        Spacer(Modifier.width(10.dp))
                                        Text("${staff.successRate}% Success", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.width(10.dp))
                                        val (dotColor, statusText) = if (staff.availability == "Available")
                                            SuccessGreen to "Available" else BusyOrange to "Busy"
                                        Box(modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor))
                                        Spacer(Modifier.width(4.dp))
                                        Text(statusText, fontSize = 12.sp, color = dotColor)
                                    }
                                }

                                if (staff.nearestRouteMatch) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Nearest Route Match",
                                        fontSize = 11.sp,
                                        color = RecommendedColor,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(top = 8.dp))
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Instruction for Staff", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LabelColor)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = instructionNote,
                            onValueChange = { instructionNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TitleColor),
                            decorationBox = { inner ->
                                if (instructionNote.isEmpty()) Text("Add an optional note...", fontSize = 13.sp, color = MutedColor)
                                inner()
                            }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            assignStaffSheetState = SheetValue.Hidden
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(ActionButtonHeight),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            tempSelectedStaff?.let { staffName ->
                                selectedStaff = staffName
                                onAssignStaffConfirm(staffName)
                            }
                            assignStaffSheetState = SheetValue.Hidden
                        },
                        enabled = tempSelectedStaff != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(ActionButtonHeight),
                        contentPadding = ActionButtonContentPadding,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Assign Staff", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ── Update Status Bottom Sheet ──
        SmoothBottomSheet(
            state = updateStatusSheetState,
            onStateChange = {
                updateStatusSheetState = it
            },
            peekHeight = 420.dp,
            onDismissRequest = {
                updateStatusSheetState = SheetValue.Hidden
            },
            onBlurScrimChange = { blur, _ ->
                updateStatusSheetBlur = blur
            },
            sheetBackgroundColor = Color.White,
            maxScrimAlpha = 0.4f,
            maxBlurRadius = 14.dp,
            topInset = 160.dp
        ) {
            var tempStatus by remember { mutableStateOf(status) }
            var tempStatusExpanded by remember { mutableStateOf(false) }
            var statusNote by remember { mutableStateOf("") }

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)) {
                Text(
                    "UPDATE ORDER STATUS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                Text("$recipientName / $orderCode", fontSize = 13.sp, color = LabelColor)
                Spacer(Modifier.height(16.dp))

                Text("ORDER STATUS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(6.dp))
                FormDropdown(
                    value = tempStatus,
                    expanded = tempStatusExpanded,
                    onExpandChange = { tempStatusExpanded = it },
                    options = listOf("Ready", "Assigned", "In Transit", "Out For Delivery", "Delivered"),
                    onOptionSelected = { tempStatus = it }
                )
                Spacer(Modifier.height(16.dp))

                Text("NOTES", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedColor, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = statusNote,
                        onValueChange = { statusNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TitleColor),
                        decorationBox = { inner ->
                            if (statusNote.isEmpty()) Text("Add an optional note...", fontSize = 13.sp, color = MutedColor)
                            inner()
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        status = tempStatus
                        onUpdateStatusConfirm(tempStatus)
                        updateStatusSheetState = SheetValue.Hidden
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ActionButtonHeight),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ActionButtonContentPadding,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Update Status", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        updateStatusSheetState = SheetValue.Hidden
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ActionButtonHeight),
                    contentPadding = ActionButtonContentPadding,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TitleColor)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MutedColor, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Last updated 2m ago by J. Doe", fontSize = 11.sp, color = MutedColor)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── Reusable pieces ──

@Composable
private fun SummaryColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = MutedColor)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = if (isBold) TitleColor else MutedColor, fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            value,
            fontSize = if (isBold) 15.sp else 13.sp,
            color = TitleColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}