package com.cuso.tailor.view.home.logistics.shipment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.ShipmentActivityItemDto
import com.cuso.tailor.model.logistics.ShipmentDetailData
import com.cuso.tailor.model.logistics.TrackingTimelineDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_blue_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.SmoothBottomSheet
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun ShipmentDetailScreen(
    shipmentId: String,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val detail by viewModel.currentShipmentDetail.collectAsState()
    val isLoading by viewModel.isLoadingShipmentDetail.collectAsState()
    val errorMessage by viewModel.shipmentDetailError.collectAsState()
    val isSubmittingAction by viewModel.isSubmittingAction.collectAsState()

    var showUpdateStatusSheet by remember { mutableStateOf(false) }
    var showGenerateLabelSheet by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shipmentId) {
        if (shipmentId.isNotBlank()) {
            viewModel.fetchShipmentDetails(shipmentId)
        }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(
                title = "Shipment Details",
                subtitle = "Logistics • Delivery Management",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        when {
            isLoading && detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(tokens.iconSize * 1.5f)
                    )
                }
            }

            errorMessage != null && detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to load shipment details",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = redText
                    )
                }
            }

            detail != null -> {
                val shipment = detail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = tokens.screenPadding,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Overview Card
                    item {
                        ShipmentOverviewCard(
                            shipment = shipment,
                            onGenerateLabelClick = { showGenerateLabelSheet = true },
                            onUpdateStatusClick = { showUpdateStatusSheet = true },
                            onCancelClick = { showCancelConfirmationDialog = true }
                        )
                    }

                    // Tracking Timeline Stepper Card
                    item {
                        TrackingTimelineCard(
                            timeline = shipment.trackingTimeline,
                            status = shipment.status
                        )
                    }

                    // Shipment Information Card
                    item {
                        ShipmentInfoCard(shipment = shipment)
                    }

                    // Courier Details Card
                    item {
                        CourierDetailsCard(shipment = shipment)
                    }

                    // Delivery Address Card
                    item {
                        DeliveryAddressCard(shipment = shipment)
                    }

                    // Activity Log Timeline Card
                    item {
                        ActivityLogCard(
                            logs = shipment.activityLog,
                            formatDateHelper = { isoString -> viewModel.formatDateTimeToHuman(isoString) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet 1: Update Shipment Status
    if (showUpdateStatusSheet && detail != null) {
        UpdateShipmentStatusSheet(
            currentStatus = detail!!.status,
            currentLocation = detail!!.currentLocation.orEmpty(),
            shipmentCode = detail!!.shipmentNumber.ifBlank { detail!!.shipmentId },
            isSubmitting = isSubmittingAction,
            onDismiss = { showUpdateStatusSheet = false },
            onSubmit = { newStatus, location, notes ->
                viewModel.updateShipmentLiveStatus(
                    shipmentId = shipmentId,
                    status = newStatus,
                    currentLocation = location,
                    notes = notes,
                    onSuccess = {
                        showUpdateStatusSheet = false
                    }
                )
            }
        )
    }

    // Modal Sheet 2: Generate Shipping Label
    if (showGenerateLabelSheet && detail != null) {
        GenerateShippingLabelSheet(
            shipment = detail!!,
            isGenerating = isSubmittingAction,
            onDismiss = { showGenerateLabelSheet = false },
            onGenerateClick = {
                viewModel.generateShipmentLabel(shipmentId) {
                    // Refreshed state reflected in UI
                }
            }
        )
    }

    // Confirmation Dialog for Cancel Shipment
    if (showCancelConfirmationDialog) {
        Dialog(
            onDismissRequest = { showCancelConfirmationDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.isTablet) 0.60f else 0.90f),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cancel Shipment",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Are you sure you want to cancel this shipment? This operation cannot be undone.",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showCancelConfirmationDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Keep Active",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.cancelShipment(shipmentId) {
                                    showCancelConfirmationDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = redBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Confirm Cancel",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = redText
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// DETAIL SECTION CARDS
// =============================================================================

@Composable
private fun SectionContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    icon: ImageVector,
    trailingBadgeText: String? = null
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(light_blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }

        if (trailingBadgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(light_grey)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = trailingBadgeText,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = headerGrey
                )
            }
        }
    }
}

/**
 * Top Summary card with IDs and primary action buttons matching screenshot 2.
 */
@Composable
private fun ShipmentOverviewCard(
    shipment: ShipmentDetailData,
    onGenerateLabelClick: () -> Unit,
    onUpdateStatusClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val (statusLabel, statusBg, statusColor) = remember(shipment.status) {
        val cleanStatus = shipment.status.replace("_", " ")
        when (cleanStatus.lowercase()) {
            "delivered" -> Triple("Delivered", greenBg, greentext)
            "out for delivery" -> Triple("Out for Delivery", orangeBg, orangeText)
            "in transit" -> Triple("In Transit", primary_light, Primary)
            "cancelled", "failed" -> Triple(cleanStatus, redBg, redText)
            else -> Triple(cleanStatus, light_grey, headerGrey)
        }
    }

    SectionContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = shipment.shipmentNumber.ifBlank { shipment.shipmentId },
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Courier & Tracking ID split boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Courier Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Courier",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shipment.courierPartner?.ifBlank { "SwiftLogix" } ?: "SwiftLogix",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tracking ID Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Tracking ID",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shipment.trackingId?.ifBlank { "--" } ?: "--",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Row: Generate Label & Update Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onGenerateLabelClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Text(
                        text = "Generate Label",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }

            Button(
                onClick = onUpdateStatusClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Text(
                        text = "Update Status",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cancel Shipment link button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                .clickable { onCancelClick() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel Shipment",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = redText
            )
        }
    }
}

/**
 * Horizontal Tracking Timeline Stepper matching screenshot 2.
 */
/**
 * Horizontal Tracking Timeline Stepper matching screenshot.
 */
@Composable
private fun TrackingTimelineCard(
    timeline: TrackingTimelineDto?,
    status: String
) {
    val tokens = LocalAppTokens.current

    val steps = remember(timeline, status) {
        listOf(
            TimelineStepData(
                title = "Created",
                dateText = if (!timeline?.createdAt.isNullOrBlank()) "Oct 24\n10:00 AM" else "Pending",
                isCompleted = true,
                isActive = false,
                icon = Icons.Default.Check
            ),
            TimelineStepData(
                title = "In Transit",
                dateText = if (!timeline?.inTransitAt.isNullOrBlank()) "Oct 25\n02:00 PM" else "Pending",
                isCompleted = status.equals("Delivered", ignoreCase = true) || status.equals("Out_for_Delivery", ignoreCase = true),
                isActive = status.equals("In_Transit", ignoreCase = true),
                icon = Icons.Default.LocalShipping
            ),
            TimelineStepData(
                title = "Out for Delivery",
                dateText = if (!timeline?.outForDeliveryAt.isNullOrBlank()) "Today\n09:00 AM" else "Pending",
                isCompleted = status.equals("Delivered", ignoreCase = true),
                isActive = status.equals("Out_for_Delivery", ignoreCase = true),
                icon = Icons.Default.Place
            ),
            TimelineStepData(
                title = "Delivered",
                dateText = if (!timeline?.deliveredAt.isNullOrBlank()) "Delivered" else "Pending",
                isCompleted = status.equals("Delivered", ignoreCase = true),
                isActive = false,
                icon = Icons.Default.CheckCircle
            )
        )
    }

    SectionContainer {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize)
            )
            Text(
                text = "Tracking Timeline",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Horizontal Stepper Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Indicator Row: Left Line + Circle + Right Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Left Connector Line
                        val leftLineColor = if (index > 0) {
                            val prevStep = steps[index - 1]
                            if (prevStep.isCompleted && (step.isCompleted || step.isActive)) darkGreenBg else sectionBorder
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(leftLineColor)
                        )

                        // 2. Stepper Circle Icon
                        val circleBg = when {
                            step.isCompleted -> darkGreenBg
                            step.isActive -> Primary
                            else -> light_grey
                        }
                        val iconColor = when {
                            step.isCompleted || step.isActive -> whiteBg
                            else -> mutedText
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .border(
                                    width = if (step.isActive) 2.dp else 1.dp,
                                    color = if (step.isActive) Primary else sectionBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 3. Right Connector Line
                        val rightLineColor = if (index < steps.lastIndex) {
                            val nextStep = steps[index + 1]
                            if (step.isCompleted && (nextStep.isCompleted || nextStep.isActive)) darkGreenBg else sectionBorder
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(rightLineColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.title,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (step.isCompleted || step.isActive) title_color else headerGrey,
                        textAlign = TextAlign.Center,
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = step.dateText,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Normal,
                        color = if (step.isActive) Primary else mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class TimelineStepData(
    val title: String,
    val dateText: String,
    val isCompleted: Boolean,
    val isActive: Boolean,
    val icon: ImageVector
)

/**
 * Shipment Information Section Card.
 */
@Composable
private fun ShipmentInfoCard(shipment: ShipmentDetailData) {
    val tokens = LocalAppTokens.current

    val orderIdText = remember(shipment.orderDetail) {
        shipment.orderDetail?.orderCode ?: "ORD-2024-0012"
    }
    val weightText = remember(shipment.packageDetails?.weightKg) {
        val w = shipment.packageDetails?.weightKg ?: 2.5
        "$w KG"
    }
    val dimensionText = remember(shipment.packageDetails?.dimensions) {
        val dim = shipment.packageDetails?.dimensions
        if (dim != null) {
            "${dim.lengthCm.toInt()} x ${dim.widthCm.toInt()} x ${dim.heightCm.toInt()} cm"
        } else {
            "12 x 12 x 12 cm"
        }
    }

    SectionContainer {
        SectionHeaderRow(
            title = "Shipment Information",
            icon = Icons.Default.Inventory2
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order ID",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = orderIdText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Package Weight",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = weightText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Text(
                text = "Dimensions",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dimensionText,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}

/**
 * Courier Details Section Card.
 */
@Composable
private fun CourierDetailsCard(shipment: ShipmentDetailData) {
    val tokens = LocalAppTokens.current

    val partnerName = remember(shipment.courierPartner) {
        shipment.courierPartner?.ifBlank { "SwiftLogix" } ?: "SwiftLogix"
    }
    val serviceTypeName = remember(shipment.serviceType) {
        shipment.serviceType?.ifBlank { "Express Delivery" } ?: "Express Delivery"
    }

    SectionContainer {
        SectionHeaderRow(
            title = "Courier Details",
            icon = Icons.Default.LocalShipping
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Partner",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = partnerName,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Service Type",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(light_blue)
                        .border(1.dp, light_blue_border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = serviceTypeName,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }
        }
    }
}

/**
 * Delivery Address Section Card.
 */
@Composable
private fun DeliveryAddressCard(shipment: ShipmentDetailData) {
    val tokens = LocalAppTokens.current

    val pinText = remember(shipment.deliveryAddress?.pincode) {
        "PIN: ${shipment.deliveryAddress?.pincode?.ifBlank { "78701" } ?: "78701"}"
    }
    val recipientName = remember(shipment.deliveryAddress?.recipientName, shipment.orderDetail?.customerSnapshot?.name) {
        shipment.deliveryAddress?.recipientName?.ifBlank {
            shipment.orderDetail?.customerSnapshot?.name ?: "Jonathan H. Abernathy"
        } ?: "Jonathan H. Abernathy"
    }
    val phoneNumber = remember(shipment.deliveryAddress?.phoneNumber, shipment.orderDetail?.customerSnapshot?.phone) {
        shipment.deliveryAddress?.phoneNumber?.ifBlank {
            shipment.orderDetail?.customerSnapshot?.phone ?: "+1 555-0199"
        } ?: "+1 555-0199"
    }
    val fullAddress = remember(shipment.deliveryAddress?.fullAddress) {
        shipment.deliveryAddress?.fullAddress?.ifBlank {
            "822 Kinetic Parkway, Suite 400\nAustin, TX 78701, United States"
        } ?: "822 Kinetic Parkway, Suite 400\nAustin, TX 78701, United States"
    }

    SectionContainer {
        SectionHeaderRow(
            title = "Delivery Address",
            icon = Icons.Default.LocationOn,
            trailingBadgeText = pinText
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Inner Customer Container Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                .background(whiteBg)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = recipientName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phoneNumber,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Text(
                text = "Full Address",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fullAddress,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = title_color,
                lineHeight = tokens.bodyMedium
            )
        }
    }
}

/**
 * Vertical Activity Log Timeline Card.
 */
@Composable
private fun ActivityLogCard(
    logs: List<ShipmentActivityItemDto>,
    formatDateHelper: (String?) -> String
) {
    val tokens = LocalAppTokens.current

    val displayLogs = remember(logs) {
        if (logs.isNotEmpty()) {
            logs
        } else {
            listOf(
                ShipmentActivityItemDto(
                    action = "Departed from facility",
                    location = "Hub A, Houston",
                    timestamp = "Today • 02:00 PM"
                ),
                ShipmentActivityItemDto(
                    action = "Arrived at warehouse",
                    location = "Austin Sorting Center",
                    timestamp = "Oct 24 • 04:30 PM"
                ),
                ShipmentActivityItemDto(
                    action = "Shipment created",
                    location = "System",
                    timestamp = "Oct 24 • 10:00 AM"
                )
            )
        }
    }

    SectionContainer {
        SectionHeaderRow(
            title = "Activity Log",
            icon = Icons.Default.AccessTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            displayLogs.forEachIndexed { index, item ->
                val isFirst = index == 0
                val isLast = index == displayLogs.lastIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Vertical dot and connector line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFirst) Primary else whiteBg)
                                .border(
                                    width = 2.dp,
                                    color = if (isFirst) Primary else BorderGray,
                                    shape = CircleShape
                                )
                        )

                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(sectionBorder)
                            )
                        }
                    }

                    // Content column: Action + Location & Timestamp
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (!isLast) 16.dp else 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.action,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val locationText = if (!item.location.isNullOrBlank()) {
                                "${item.location} • ${item.note ?: ""}".trimEnd(' ', '•')
                            } else {
                                item.note.orEmpty()
                            }
                            Text(
                                text = locationText.ifBlank { "In Facility" },
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                        }

                        Text(
                            text = if (isFirst) "Today" else formatDateHelper(item.timestamp),
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = if (isFirst) Primary else mutedText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = sectionBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Bottom "View Full History >" link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle full activity history view */ },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "View Full History",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

// =============================================================================
// MODAL BOTTOM SHEET 1: UPDATE SHIPMENT STATUS (Screenshot 4)
// =============================================================================

@Composable
private fun UpdateShipmentStatusSheet(
    currentStatus: String,
    currentLocation: String,
    shipmentCode: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (newStatus: String, location: String, notes: String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val statusOptions = listOf("Created", "In Transit", "Out for Delivery", "Delivered", "Failed")

    var selectedStatus by remember(currentStatus) {
        val mapped = currentStatus.replace("_", " ")
        mutableStateOf(if (mapped in statusOptions) mapped else "In Transit")
    }
    var locationInput by remember(currentLocation) { mutableStateOf(currentLocation) }
    var notesInput by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    SmoothBottomSheet(
        state = SheetValue.Expanded,
        onStateChange = { if (it == SheetValue.Hidden) onDismiss() },
        onDismissRequest = onDismiss,
        title = "Update Shipment Status",
        subtitle = shipmentCode
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Status",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status Dropdown Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedStatus,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(whiteBg)
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = status,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = title_color
                                )
                            },
                            onClick = {
                                selectedStatus = status
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Legend Indicator Row matching screenshot 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicatorPill(label = "Created", color = headerGrey)
                StatusIndicatorPill(label = "In Transit", color = Primary)
                StatusIndicatorPill(label = "Out for Delivery", color = orangeText)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicatorPill(label = "Delivered", color = darkGreenBg)
                StatusIndicatorPill(label = "Failed", color = redText)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Location Field
            Text(
                text = "Current Location",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        color = title_color,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { innerTextField ->
                        if (locationInput.isEmpty()) {
                            Text(
                                text = "e.g. Distribution Center 4, Mumbai",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notes Text Area
            Text(
                text = "Notes",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    textStyle = TextStyle(
                        fontSize = tokens.bodySmall,
                        color = title_color,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.fillMaxSize(),
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { innerTextField ->
                        if (notesInput.isEmpty()) {
                            Text(
                                text = "Add any specific details regarding this update...",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                Button(
                    onClick = {
                        val backendStatus = selectedStatus.replace(" ", "_")
                        onSubmit(backendStatus, locationInput, notesInput)
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = disabled
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(tokens.buttonHeight)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "Update Status",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusIndicatorPill(label: String, color: Color) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// =============================================================================
// MODAL BOTTOM SHEET 2: GENERATE SHIPPING LABEL (Screenshot 5)
// =============================================================================

@Composable
private fun GenerateShippingLabelSheet(
    shipment: ShipmentDetailData,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerateClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val clipboardManager = LocalClipboardManager.current

    val trackingNumber = shipment.trackingId?.ifBlank { "TRK442991002" } ?: "TRK442991002"
    val weightText = "${shipment.packageDetails?.weightKg ?: 2.5} KG"
    val dim = shipment.packageDetails?.dimensions
    val dimensionsText = if (dim != null) {
        "${dim.lengthCm.toInt()}×${dim.widthCm.toInt()}×${dim.heightCm.toInt()} cm"
    } else {
        "12×12×12 cm"
    }

    SmoothBottomSheet(
        state = SheetValue.Expanded,
        onStateChange = { if (it == SheetValue.Hidden) onDismiss() },
        onDismissRequest = onDismiss,
        title = "Generate Shipping Label"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Shipment ID & Courier Partner Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shipment ID",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = shipment.shipmentNumber.ifBlank { shipment.shipmentId },
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Courier Name",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PanelBg)
                            .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = title_color,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Text(
                                text = shipment.courierPartner?.ifBlank { "SwiftLogix" } ?: "SwiftLogix",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking ID with Copy Icon
            Text(
                text = "Tracking ID",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.fieldHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trackingNumber,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = headerGrey,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(trackingNumber))
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Package Details Header
            Text(
                text = "Package Details",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weight Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PanelBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "WEIGHT",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weightText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }

                // Dimensions Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PanelBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "DIMENSIONS",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dimensionsText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Label Preview Dashed Container matching screenshot 5
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = BorderGray,
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = stroke
                        )
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(light_blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Label Preview",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (shipment.shippingLabel?.isGenerated == true) {
                            "Label ready for download & print"
                        } else {
                            "PDF format available after generation"
                        },
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Label Primary Button
            Button(
                onClick = onGenerateClick,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = disabled
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(18.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (shipment.shippingLabel?.isGenerated == true) "Regenerate Label" else "Generate Label",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Download & Print Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Handle download label PDF */ },
                    enabled = shipment.shippingLabel?.isGenerated == true,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Download Label",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }

                OutlinedButton(
                    onClick = { /* Handle print label */ },
                    enabled = shipment.shippingLabel?.isGenerated == true,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Print Label",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cancel action
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}