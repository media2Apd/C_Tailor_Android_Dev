@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.logistics.order_tracking

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.model.logistics.TrackingTimelineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

/**
 * Returns matching icon for tracking stage.
 */
private fun getTimelineIcon(stage: String): ImageVector {
    val clean = stage.replace("_", " ").trim()
    return when {
        clean.contains("Shipped", ignoreCase = true) -> Icons.Default.Warehouse
        clean.contains("Transit", ignoreCase = true) -> Icons.Default.LocalShipping
        clean.contains("Delivery", ignoreCase = true) && !clean.contains("Delivered", ignoreCase = true) -> Icons.Default.Layers
        clean.contains("Delivered", ignoreCase = true) -> Icons.Default.Home
        else -> Icons.Default.Check
    }
}

@Composable
fun TrackingOverviewScreen(
    viewModel: LogisticsViewModel,
    trackingIdentifier: String,
    onViewShipment: (shipmentId: String) -> Unit = {},
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val trackingData by viewModel.trackingOverview.collectAsState()
    val isLoading by viewModel.isLoadingTrackingOverview.collectAsState()
    val isUpdating by viewModel.isUpdatingTrackingStatus.collectAsState()
    val updateError by viewModel.updateTrackingStatusError.collectAsState()
    val errorMessage by viewModel.trackingOverviewError.collectAsState()

    var showStatusModal by remember { mutableStateOf(false) }

    LaunchedEffect(trackingIdentifier) {
        if (trackingIdentifier.isNotBlank()) {
            viewModel.fetchTrackingOverview(trackingIdentifier)
        }
    }

    if (showStatusModal) {
        UpdateTrackingStatusModal(
            identifier = trackingData?.trackingId ?: trackingIdentifier,
            overview = trackingData,
            isSaving = isUpdating,
            errorMessage = updateError,
            onDismiss = { showStatusModal = false },
            onSubmit = { status, location, note ->
                viewModel.updateTrackingStatus(
                    identifier = trackingIdentifier,
                    status = status,
                    location = location,
                    note = note,
                    onSuccess = { successMsg ->
                        showStatusModal = false
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Tracking Overview", onClose = onClose)
                    HorizontalDivider(color = dividerColor)
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { innerPadding ->

        if (isLoading) {
            ListSkeleton()
            return@Scaffold
        }

        if (trackingData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Tracking details not found.",
                    color = mutedText,
                    fontSize = tokens.bodyMedium
                )
            }
            return@Scaffold
        }

        val data = trackingData!!
        val timeline = data.trackingTimeline
        val lastCompletedIndex = timeline.indexOfLast { it.isCompleted }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                // Header row: Order ID & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.orderCode ?: data.orderId ?: "—",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    val statusText = data.currentStatus?.replace("_", " ") ?: "Created"
                    val isDelivered = statusText.equals("Delivered", ignoreCase = true)
                    val isFailed = statusText.equals("Failed", ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(
                                when {
                                    isDelivered -> greenBg
                                    isFailed -> redBg
                                    else -> background_light_purple
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isDelivered -> greentext
                                isFailed -> redText
                                else -> BluePrimary
                            },
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.7f))

                // Info row
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoCell("Tracking ID", data.trackingId ?: "—", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Courier", data.courierPartner ?: "—", Modifier.weight(1f))
                    VerticalDividerLine()
                    InfoCell("Est. Delivery", viewModel.formatTrackingDate(data.estDeliveryDate), Modifier.weight(1f))
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.8f))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            data.shipmentId?.let { onViewShipment(it) }
                        },
                        enabled = !data.shipmentId.isNullOrBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = BorderStroke(1.2.dp, if (!data.shipmentId.isNullOrBlank()) Primary else sectionBorder)
                    ) {
                        Text("View Shipment", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { showStatusModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Update Status", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(tokens.screenPadding * 1.2f))
            HorizontalDivider(color = dividerColor)

            // Timeline Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.cardPadding * 0.35f)
            ) {
                Text(
                    text = "Tracking Timeline",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(tokens.screenPadding * 0.8f))

            // Timeline items
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                timeline.forEachIndexed { index, item ->
                    val isDone = item.isCompleted && (data.currentStatus == "Delivered" || index < lastCompletedIndex)
                    val isCurrent = index == lastCompletedIndex && data.currentStatus != "Delivered"

                    TimelineItemRow(
                        item = item,
                        isDone = isDone,
                        isCurrent = isCurrent,
                        isLast = index == timeline.lastIndex,
                        formattedTime = viewModel.formatDateTimeToHuman(item.timestamp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = dividerColor)

            // Shipment Information section
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding * 0.5f, vertical = tokens.cardPadding * 0.35f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Shipment Information",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Spacer(Modifier.height(tokens.screenPadding * 0.4f))
                ShipmentInfoRow("Origin", data.shipmentInfo?.origin ?: "—")
                HorizontalDivider(color = dividerColor)
                ShipmentInfoRow("Destination", data.shipmentInfo?.destination ?: "—")
                HorizontalDivider(color = dividerColor)
                ShipmentInfoRow("Package Weight", data.shipmentInfo?.packageWeightKg ?: "—")
            }

            Spacer(Modifier.height(tokens.screenPadding))
            HorizontalDivider(color = dividerColor)

            // Courier Details section
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding * 0.5f, vertical = tokens.cardPadding * 0.35f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Courier Details",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                Spacer(Modifier.height(tokens.screenPadding * 0.4f))
                CourierDetailRow("Courier", data.courierPartner ?: "—")
                HorizontalDivider(color = dividerColor)
                CourierDetailRow("Service Type", data.serviceType ?: "—")
                Spacer(Modifier.height(tokens.screenPadding * 1.6f))
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: TrackingTimelineItemDto,
    isDone: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    formattedTime: String
) {
    val tokens = LocalAppTokens.current

    val circleOuterSize = tokens.iconSize * 2.2f
    val circleInnerSize = tokens.iconSize * 1.8f

    val scale by animateFloatAsState(
        targetValue = if (isDone || isCurrent) 1f else 0.95f,
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(circleOuterSize)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = circleOuterSize / 2)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isDone) darkGreenBg else grey_border)
                )
            }

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(circleOuterSize),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(circleOuterSize)
                            .background(primary_light, CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(circleInnerSize)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> darkGreenBg
                                isCurrent -> Primary
                                else -> grey_border
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.Check else getTimelineIcon(item.stage),
                        contentDescription = null,
                        tint = if (isDone || isCurrent) whiteBg else iconMuted,
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .padding(top = 2.dp, bottom = if (isLast) 0.dp else 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.stage.replace("_", " "),
                    fontSize = tokens.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                    color = if (isCurrent) Primary else if (item.isCompleted) TextPrimary else mutedText
                )
                Text(
                    text = if (item.isCompleted) formattedTime else "Pending",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            item.location?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        fontSize = tokens.bodySmall,
                        color = if (isDone) greentext else mutedText
                    )
                }
            }

            item.note?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        fontSize = tokens.bodySmall,
                        color = if (item.isCompleted) TextSecondary else mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(dividerColor)
    )
}

@Composable
private fun ShipmentInfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = tokens.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun CourierDetailRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
fun TrackingOverviewScreenPreview() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppTokens provides getAdaptiveTokens(WindowWidthSizeClass.Compact)
        ) {
            Surface(color = whiteBg) {
                // Design preview container
            }
        }
    }
}