@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.delivery_management.delivery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.DeliveryDetailData
import com.cuso.tailor.model.logistics.DeliveryStatusHistoryDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LogisticsViewModel

private val STATUS_FLOW = listOf(
    "Ready",
    "Assigned",
    "In Transit",
    "Out for Delivery",
    "Delivered"
)

private val COLLECTED_BY_OPTIONS = listOf(
    "Customer" to "Customer",
    "Authorized Representative" to "Representative",
    "Courier Agent" to "Courier_Agent"
)

private val VERIFICATION_OPTIONS = listOf(
    "OTP Verified ✓" to "OTP_Verified",
    "ID Proof Verified" to "ID_Proof_Verified",
    "Physical Signature" to "Physical_Signature",
    "Direct Handover" to "Direct_Handover"
)

private val PACKAGE_CONDITION_OPTIONS = listOf(
    "Good" to "Good",
    "Tampered" to "Tampered",
    "Damaged" to "Damaged"
)

@Composable
fun DeliveryDetailScreen(
    deliveryId: String,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val deliveryDetail by viewModel.deliveryDetail.collectAsState()
    val isLoading by viewModel.isLoadingDeliveryDetail.collectAsState()
    val errorMessage by viewModel.deliveryDetailError.collectAsState()
    val isMarkingCompleted by viewModel.isMarkingCompleted.collectAsState()
    val isUpdatingDeliveryStatus by viewModel.isUpdatingDeliveryStatus.collectAsState()

    var showAssignDialog by remember { mutableStateOf(false) }
    var showInStorePickupDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var nextStatusTarget by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    var isEditingResolution by remember { mutableStateOf(false) }
    var serviceDate by remember { mutableStateOf("") }
    var serviceNotes by remember { mutableStateOf("") }

    LaunchedEffect(deliveryId) {
        if (deliveryId.isNotBlank()) {
            viewModel.fetchDeliveryDetail(deliveryId)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "Delivery Overview",
                onClose = {
                    viewModel.clearDeliveryDetail()
                    onBack()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    ListSkeleton()
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load delivery details",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                }

                deliveryDetail != null -> {
                    val detail = deliveryDetail!!
                    val order = detail.order
                    val customer = order?.customerSnapshot

                    val garmentName = remember(detail) {
                        val items = order?.items.orEmpty()
                        items.joinToString(", ") { it.itemDescription.orEmpty() }
                            .ifBlank { "Garment Item" }
                    }

                    val garmentType = remember(detail) {
                        detail.garmentDetails?.pieceCount
                            ?: if ((order?.items?.size ?: 0) > 1) "Multiple Pieces" else "Single Piece"
                    }

                    val customerCode = remember(customer) {
                        order?.orderCode?.replace("ORD-", "CUS-") ?: "CUS-001"
                    }

                    val sortedHistory = remember(detail.statusHistory) {
                        detail.statusHistory.sortedByDescending { it.changedAt }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ── 1. Top Header Card (Status, Priority, Actions) ──
                        DeliveryHeaderCard(
                            detail = detail,
                            isMarkingCompleted = isMarkingCompleted,
                            onAssignClick = { showAssignDialog = true },
                            onPickupClick = { showInStorePickupDialog = true },
                            onNextStatusClick = {
                                val currentIndex = STATUS_FLOW.indexOf(detail.status)
                                if (currentIndex in 0 until STATUS_FLOW.lastIndex) {
                                    nextStatusTarget = STATUS_FLOW[currentIndex + 1]
                                    showStatusDialog = true
                                }
                            },
                            onMarkCompletedClick = {
                                viewModel.markDeliveryCompleted(detail.id) { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // ── 2. Delivery Progress Stepper ──
                        DeliveryProgressSection(currentStatus = detail.status)

                        // ── 3. Customer Information Section ──
                        CustomerInfoSection(
                            customerName = customer?.name ?: "—",
                            customerCode = customerCode,
                            phoneNumber = customer?.phone ?: "—",
                            emailAddress = customer?.email ?: "—"
                        )

                        // ── 4. Sales Order Reference Section ──
                        SalesOrderReferenceSection(
                            orderCode = order?.orderCode ?: "—",
                            trialCompleted = detail.trialCompleted,
                            garmentName = garmentName,
                            garmentType = garmentType,
                            deliveryDate = order?.dueDate?.let { viewModel.formatDeliveryDate(it) } ?: "—"
                        )

                        // ── 5. Assigned Staff Section ──
                        AssignedStaffSection(
                            staffName = detail.assignedStaff?.fullName ?: "Not Assigned",
                            staffRole = detail.assignedStaff?.status ?: "custom",
                            hasStaff = detail.assignedStaff != null,
                            activeJobs = 4,
                            successRate = "98%"
                        )

                        // ── 6. Activity Log & Notes Section ──
                        ActivityLogSection(
                            history = sortedHistory,
                            staffName = detail.assignedStaff?.fullName,
                            viewModel = viewModel
                        )

                        // ── 7. Preferred Resolution Section ──
                        PreferredResolutionSection(
                            isEditing = isEditingResolution,
                            serviceDate = serviceDate,
                            serviceNotes = serviceNotes,
                            onServiceDateChange = { serviceDate = it },
                            onServiceNotesChange = { serviceNotes = it },
                            onStartEdit = {
                                isEditingResolution = true
                                serviceDate = detail.updatedAt?.let { viewModel.formatDeliveryDate(it) } ?: ""
                                serviceNotes = ""
                            },
                            onSave = { isEditingResolution = false },
                            onCancel = { isEditingResolution = false }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Dialog 1: Assign / Reassign Staff Dialog ──
                    if (showAssignDialog) {
                        AssignDeliveryDialog(
                            hrViewModel = hrViewModel,
                            onDismiss = { showAssignDialog = false },
                            onConfirm = { member, targetDate, instructions ->
                                val dateParts = targetDate.split("-")
                                val apiDate = if (dateParts.size == 3) {
                                    "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
                                } else {
                                    targetDate
                                }

                                viewModel.assignDelivery(
                                    deliveryId = detail.id,
                                    staffId = member._id,
                                    staffInstructions = instructions,
                                    targetCompletionDate = apiDate,
                                    onSuccess = {
                                        showAssignDialog = false
                                        viewModel.fetchDeliveryDetail(detail.id)
                                    }
                                )
                            }
                        )
                    }

                    // ── Dialog 2: Confirm In-Store Pickup Dialog ──
                    if (showInStorePickupDialog) {
                        InStorePickupDialog(
                            detail = detail,
                            hrViewModel = hrViewModel,
                            onDismiss = { showInStorePickupDialog = false },
                            onConfirm = { staffName, collectedBy, verification, condition, notes ->
                                val orderId = detail.order?.id ?: detail.id
                                viewModel.confirmInStorePickup(
                                    orderId = orderId,
                                    handedOverByStaffName = staffName,
                                    collectedBy = collectedBy,
                                    verificationMethod = verification,
                                    packageCondition = condition,
                                    notes = notes,
                                    onSuccess = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        showInStorePickupDialog = false
                                        viewModel.fetchDeliveryDetail(detail.id)
                                    }
                                )
                            }
                        )
                    }

                    // ── Dialog 3: Change Status Confirmation Dialog with API Call ──
                    if (showStatusDialog && nextStatusTarget != null) {
                        StatusChangeDialog(
                            nextStatus = nextStatusTarget!!,
                            message = statusMessage,
                            isLoading = isUpdatingDeliveryStatus,
                            onMessageChange = { statusMessage = it },
                            onDismiss = {
                                if (!isUpdatingDeliveryStatus) {
                                    showStatusDialog = false
                                    statusMessage = ""
                                    nextStatusTarget = null
                                }
                            },
                            onConfirm = {
                                viewModel.updateDeliveryStatus(
                                    deliveryId = detail.id,
                                    newStatus = nextStatusTarget!!,
                                    message = statusMessage,
                                    onSuccess = { successMsg ->
                                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                        showStatusDialog = false
                                        statusMessage = ""
                                        nextStatusTarget = null
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top header card showing order code, delivery number, status, priority, and header actions.
 */
@Composable
private fun DeliveryHeaderCard(
    detail: DeliveryDetailData,
    isMarkingCompleted: Boolean,
    onAssignClick: () -> Unit,
    onPickupClick: () -> Unit,
    onNextStatusClick: () -> Unit,
    onMarkCompletedClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isDelivered = detail.status.equals("Delivered", ignoreCase = true)
    val isReady = detail.status.equals("Ready", ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${detail.order?.orderCode ?: "—"} / ${detail.deliveryNumber}",
                        fontSize = tokens.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Text(
                        text = "Customer: ${detail.order?.customerSnapshot?.name ?: "—"}",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDelivered) greenBg else primary_light)
                            .padding(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = detail.status,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = if (isDelivered) greentext else Primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(orangeBg)
                            .padding(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = detail.priority,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = orangeText
                        )
                    }
                }
            }

            if (!isDelivered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAssignClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight * 0.85f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (detail.assignedStaff != null) "Reassign Staff" else "Assign Staff",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onPickupClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight * 0.85f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "In-store Pickup",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!isReady) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNextStatusClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight * 0.85f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detail.status,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = mutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        Button(
                            onClick = onMarkCompletedClick,
                            enabled = !isMarkingCompleted,
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight * 0.85f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = whiteBg,
                                disabledContainerColor = disabled,
                                disabledContentColor = whiteBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            if (isMarkingCompleted) {
                                CircularProgressIndicator(
                                    color = whiteBg,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "Mark as Completed",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Status change confirmation dialog calling updateDeliveryStatus on confirm.
 */
@Composable
private fun StatusChangeDialog(
    nextStatus: String,
    message: String,
    isLoading: Boolean,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Change status to $nextStatus?",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                FormTextArea(
                    value = message,
                    onValueChange = onMessageChange,
                    placeholder = "Add note or comment...",
                    minLines = 3,
                    maxLines = 4,
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = headerGrey)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = whiteBg,
                            disabledContainerColor = disabled,
                            disabledContentColor = whiteBg
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text("Confirm", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wide In-Store Pickup Dialog.
 */
@Composable
private fun InStorePickupDialog(
    detail: DeliveryDetailData,
    hrViewModel: HrViewModel,
    onDismiss: () -> Unit,
    onConfirm: (staffName: String, collectedBy: String, verification: String, condition: String, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val members by hrViewModel.members.collectAsState()
    val isLoadingMembers by hrViewModel.isLoadingMembers.collectAsState()

    var handedOverStaff by remember { mutableStateOf("") }
    var staffExpanded by remember { mutableStateOf(false) }

    var collectedByLabel by remember { mutableStateOf("Customer") }
    var collectedByExpanded by remember { mutableStateOf(false) }

    var verificationLabel by remember { mutableStateOf("OTP Verified ✓") }
    var verificationExpanded by remember { mutableStateOf(false) }

    var packageConditionLabel by remember { mutableStateOf("Good") }
    var conditionExpanded by remember { mutableStateOf(false) }

    var pickupDate by remember { mutableStateOf("29-10-2026") }
    var notesText by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (members.isEmpty()) {
            hrViewModel.fetchMembers(page = 1, limit = 100)
        }
    }

    val staffOptions = remember(members) {
        members.mapNotNull { m ->
            "${m.firstName.orEmpty()} ${m.lastName.orEmpty()}".trim().ifBlank { null }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (tokens.isTablet) 0.65f else 0.94f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.cardPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confirm In-Store Pickup",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(tokens.iconSize * 1.3f)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = headerGrey)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelBg)
                        .padding(horizontal = tokens.cardPadding, vertical = 10.dp)
                ) {
                    Row {
                        Text(text = "Pickup for Order ", fontSize = tokens.bodySmall, color = headerGrey)
                        Text(
                            text = detail.order?.orderCode ?: detail.deliveryNumber,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = tokens.cardHeight * 6f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.cardPadding, vertical = tokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Customer", fontSize = tokens.label, color = headerGrey)
                            Text(
                                detail.order?.customerSnapshot?.name ?: "—",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                        Column {
                            Text("Pickup Location", fontSize = tokens.label, color = headerGrey)
                            Text(
                                "Nexus Boutique",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                        Column {
                            Text("Items", fontSize = tokens.label, color = headerGrey)
                            Text(
                                "${detail.order?.items?.size ?: 1} Items",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Handed Over By",
                                isRequired = true,
                                value = if (isLoadingMembers && staffOptions.isEmpty()) "Loading staff..." else handedOverStaff.ifBlank { "Select staff" },
                                expanded = staffExpanded && !isLoadingMembers && staffOptions.isNotEmpty(),
                                onExpandChange = { staffExpanded = it },
                                options = staffOptions,
                                onOptionSelected = {
                                    handedOverStaff = it
                                    staffExpanded = false
                                },
                                enabled = !isLoadingMembers && staffOptions.isNotEmpty()
                            )
                            Text("Staff member handing the order", fontSize = tokens.label, color = mutedText)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Collected By",
                                isRequired = true,
                                value = collectedByLabel,
                                expanded = collectedByExpanded,
                                onExpandChange = { collectedByExpanded = it },
                                options = COLLECTED_BY_OPTIONS.map { it.first },
                                onOptionSelected = {
                                    collectedByLabel = it
                                    collectedByExpanded = false
                                }
                            )
                        }
                    }

                    FormDropdown(
                        label = "Verification",
                        value = verificationLabel,
                        expanded = verificationExpanded,
                        onExpandChange = { verificationExpanded = it },
                        options = VERIFICATION_OPTIONS.map { it.first },
                        onOptionSelected = {
                            verificationLabel = it
                            verificationExpanded = false
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Pickup Date")
                            DatePickerField(
                                value = pickupDate,
                                onDateSelected = { pickupDate = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Package Condition",
                                value = packageConditionLabel,
                                expanded = conditionExpanded,
                                onExpandChange = { conditionExpanded = it },
                                options = PACKAGE_CONDITION_OPTIONS.map { it.first },
                                onOptionSelected = {
                                    packageConditionLabel = it
                                    conditionExpanded = false
                                }
                            )
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel(text = "Notes (Optional)")
                        FormTextArea(
                            value = notesText,
                            onValueChange = { notesText = it },
                            placeholder = "Add any pickup or handover notes...",
                            minLines = 3,
                            maxLines = 4
                        )
                    }

                    if (validationError != null) {
                        Text(text = validationError!!, fontSize = tokens.caption, color = redText)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = tokens.cardPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = headerGrey)
                    }

                    Spacer(modifier = Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            if (handedOverStaff.isBlank()) {
                                validationError = "Please select who handed over the order"
                                return@Button
                            }
                            validationError = null

                            val collectedByEnum = COLLECTED_BY_OPTIONS.find { it.first == collectedByLabel }?.second ?: "Customer"
                            val verificationEnum = VERIFICATION_OPTIONS.find { it.first == verificationLabel }?.second ?: "OTP_Verified"
                            val conditionEnum = PACKAGE_CONDITION_OPTIONS.find { it.first == packageConditionLabel }?.second ?: "Good"

                            onConfirm(
                                handedOverStaff,
                                collectedByEnum,
                                verificationEnum,
                                conditionEnum,
                                notesText.trim()
                            )
                        },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = whiteBg,
                            disabledContainerColor = disabled,
                            disabledContentColor = whiteBg
                        )
                    ) {
                        Text("Confirm Pickup", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                    }
                }
            }
        }
    }
}

/**
 * Delivery Progress stepper.
 */
@Composable
private fun DeliveryProgressSection(currentStatus: String) {
    val tokens = LocalAppTokens.current
    val currentStep = remember(currentStatus) {
        val idx = STATUS_FLOW.indexOf(currentStatus)
        if (idx >= 0) idx else 0
    }

    FormSectionContainer(title = "Delivery Progress", icon = Icons.Default.LocalShipping) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            STATUS_FLOW.forEachIndexed { index, stepName ->
                val isDone = index <= currentStep

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    when {
                                        index == 0 -> Color.Transparent
                                        index <= currentStep -> Primary
                                        else -> sectionBorder
                                    }
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Primary else whiteBg)
                                .border(1.5.dp, if (isDone) Primary else sectionBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, null, tint = whiteBg, modifier = Modifier.size(12.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    when {
                                        index == STATUS_FLOW.lastIndex -> Color.Transparent
                                        index < currentStep -> Primary
                                        else -> sectionBorder
                                    }
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepName,
                        fontSize = tokens.label,
                        fontWeight = if (index == currentStep) FontWeight.Medium else FontWeight.Normal,
                        color = if (index == currentStep) Primary else headerGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Customer Information section.
 */
@Composable
private fun CustomerInfoSection(
    customerName: String,
    customerCode: String,
    phoneNumber: String,
    emailAddress: String
) {
    FormSectionContainer(title = "Customer Information", icon = Icons.Default.Person) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Customer Name")
            FormTextField(value = customerName, onValueChange = {}, enabled = false)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FormLabel(text = "Customer ID")
                FormTextField(value = customerCode, onValueChange = {}, enabled = false)
            }

            Column(modifier = Modifier.weight(1f)) {
                FormLabel(text = "Phone Number")
                FormTextField(value = phoneNumber, onValueChange = {}, enabled = false)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Email Address")
            FormTextField(value = emailAddress, onValueChange = {}, enabled = false)
        }
    }
}

/**
 * Sales Order Reference section.
 */
@Composable
private fun SalesOrderReferenceSection(
    orderCode: String,
    trialCompleted: Boolean,
    garmentName: String,
    garmentType: String,
    deliveryDate: String
) {
    val tokens = LocalAppTokens.current

    FormSectionContainer(title = "Sales Order Reference", icon = Icons.Default.Description) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trial Completed",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            MiniSwitch(
                checked = trialCompleted,
                onCheckedChange = {},
                enabled = false
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Order ID")
            FormTextField(value = orderCode, onValueChange = {}, enabled = false)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FormLabel(text = "Garment Name")
                FormTextField(value = garmentName, onValueChange = {}, enabled = false)
            }

            Column(modifier = Modifier.weight(1f)) {
                FormLabel(text = "Garment Type")
                FormTextField(value = garmentType, onValueChange = {}, enabled = false)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Delivery Date")
            FormTextField(value = deliveryDate, onValueChange = {}, enabled = false)
        }
    }
}

/**
 * Assigned Staff section.
 */
@Composable
private fun AssignedStaffSection(
    staffName: String,
    staffRole: String,
    hasStaff: Boolean,
    activeJobs: Int,
    successRate: String
) {
    val tokens = LocalAppTokens.current

    FormSectionContainer(title = "Assigned Staff", icon = Icons.Default.Person) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(tokens.iconSize * 1.2f))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = staffName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                if (hasStaff) {
                    Text(
                        text = staffRole,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = Primary
                    )
                }
            }
        }

        if (hasStaff) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                        .background(PanelBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Active Jobs", fontSize = tokens.caption, fontWeight = FontWeight.Normal, color = headerGrey)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(activeJobs.toString(), fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                        .background(PanelBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Success Rate", fontSize = tokens.caption, fontWeight = FontWeight.Normal, color = headerGrey)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(successRate, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                    }
                }
            }

            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primary_light),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight * 0.85f)
            ) {
                Text("View Profile & Capacity", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = Primary)
            }
        }
    }
}

/**
 * Activity Log & Notes section.
 */
@Composable
private fun ActivityLogSection(
    history: List<DeliveryStatusHistoryDto>,
    staffName: String?,
    viewModel: LogisticsViewModel
) {
    val tokens = LocalAppTokens.current

    FormSectionContainer(title = "Activity Log & Notes", icon = Icons.Default.AccessTime) {
        if (history.isEmpty()) {
            Text(
                text = "No activity yet",
                fontSize = tokens.bodySmall,
                color = headerGrey,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            history.forEachIndexed { index, item ->
                val title = if (item.toStatus == "Assigned" && staffName != null) {
                    "Assigned to $staffName"
                } else {
                    "Status changed to ${item.toStatus ?: "—"}"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(y = 5.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) Primary else mutedText)
                        )
                        if (index != history.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(34.dp)
                                    .background(sectionBorder)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        if (!item.message.isNullOrBlank()) {
                            Text(
                                text = "“${item.message}”",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                        }
                        Text(
                            text = item.changedAt?.let { viewModel.formatDeliveryDate(it) } ?: "—",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = mutedText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preferred Resolution section.
 */
@Composable
private fun PreferredResolutionSection(
    isEditing: Boolean,
    serviceDate: String,
    serviceNotes: String,
    onServiceDateChange: (String) -> Unit,
    onServiceNotesChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val tokens = LocalAppTokens.current

    FormSectionContainer(
        title = "Preferred Resolution",
        icon = Icons.Default.CalendarToday,
        actionContent = {
            if (!isEditing) {
                IconButton(onClick = onStartEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = headerGrey, modifier = Modifier.size(16.dp))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onSave, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = darkGreenBg, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = redText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Preferred Service Date")
            FormTextField(
                value = if (isEditing) serviceDate else serviceDate.ifBlank { "Not Set" },
                onValueChange = onServiceDateChange,
                enabled = isEditing,
                placeholder = "mm/dd/yyyy"
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            FormLabel(text = "Resolution Notes")
            FormTextArea(
                value = if (isEditing) serviceNotes else serviceNotes.ifBlank { "No Notes" },
                onValueChange = onServiceNotesChange,
                enabled = isEditing,
                placeholder = "Additional details about desired resolution...",
                minLines = 3,
                maxLines = 4
            )
        }
    }
}

/**
 * Reusable section container.
 */
@Composable
private fun FormSectionContainer(
    title: String,
    icon: ImageVector,
    actionContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(icon, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                actionContent?.invoke()
            }

            HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)
            content()
        }
    }
}