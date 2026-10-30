@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused", "UnusedExpression", "unusedVariable")

package com.cuso.tailor.view.home.logistics.courier_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.AssignedCourierInfoDto
import com.cuso.tailor.model.logistics.CourierCustomerInfoDto
import com.cuso.tailor.model.logistics.CourierDeliveryOverviewData
import com.cuso.tailor.model.logistics.CourierPreferredResolutionDto
import com.cuso.tailor.model.logistics.CourierProductDetailsDto
import com.cuso.tailor.model.logistics.CourierSalesOrderReferenceDto
import com.cuso.tailor.model.logistics.DeliveryProgressStepDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.LogisticsViewModel

private val AREA_ISSUES = listOf(
    "Sleeve", "Neck", "Waist", "Length", "Fit", "Damage", "Fabric Issue"
)

private val PIECE_COUNTS = listOf("Single Piece", "Multiple Pieces")

@Composable
fun CourierOverviewScreen(
    deliveryId: String,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val overview by viewModel.courierOverview.collectAsState()
    val isLoading by viewModel.isLoadingCourierOverview.collectAsState()
    val errorMessage by viewModel.courierOverviewError.collectAsState()
    val isMarkingCompleted by viewModel.isMarkingCourierCompleted.collectAsState()
    val courierPartners by viewModel.courierPartnersList.collectAsState()

    var showAssignModal by remember { mutableStateOf(false) }

    LaunchedEffect(deliveryId) {
        viewModel.fetchCourierOverview(deliveryId)
        viewModel.fetchCourierPartners()
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Courier Overview",
                onClose = onBack
            )
        }
    ) { paddingValues ->
        when {
            isLoading && overview == null -> {
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

            errorMessage != null && overview == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to load courier overview",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = redText
                    )
                }
            }

            overview != null -> {
                val data = overview!!
                val isDelivered = data.status.equals("Delivered", ignoreCase = true)
                val isReady = data.status.equals("Ready", ignoreCase = true)

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
                    // Header Overview Card
                    item {
                        CourierOverviewHeaderCard(
                            data = data,
                            onAssignClick = { showAssignModal = true },
                            onMarkCompletedClick = {
                                viewModel.markCourierDeliveryCompleted(deliveryId)
                            },
                            isCompleting = isMarkingCompleted,
                            isDelivered = isDelivered,
                            isReady = isReady
                        )
                    }

                    // Delivery Progress Stepper
                    item {
                        CourierDeliveryProgressCard(
                            currentStatus = data.status,
                            progressSteps = data.deliveryProgress
                        )
                    }

                    // Customer Information
                    item {
                        CourierCustomerInfoCard(customerInfo = data.customerInfo)
                    }

                    // Sales Order Reference
                    item {
                        CourierSalesOrderReferenceCard(
                            reference = data.salesOrderReference,
                            formatDateHelper = { isoString -> viewModel.formatDeliveryDate(isoString) }
                        )
                    }

                    // Product Details (Piece Count & Area Issues)
                    item {
                        CourierProductDetailsCard(productDetails = data.productDetails)
                    }

                    // Preferred Resolution
                    item {
                        CourierPreferredResolutionCard(
                            resolution = data.preferredResolution,
                            formatDateHelper = { isoString -> viewModel.formatDeliveryDate(isoString) }
                        )
                    }

                    // Courier Assignment
                    item {
                        CourierAssignmentCard(
                            assignedCourier = data.assignedCourier,
                            deliveryCharge = data.deliveryCharge
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Modal: Assign / Reassign Courier Dialog
    if (showAssignModal && overview != null) {
        val courierOptions = remember(courierPartners) {
            courierPartners.map { it.courierName }.distinct()
        }

        AssignCourierOverviewModalDialog(
            deliveryId = overview!!.id,
            orderId = overview!!.salesOrderReference?.orderId ?: "--",
            courierOptions = courierOptions,
            initialCourier = overview!!.assignedCourier?.courierName.orEmpty(),
            initialCharge = if (overview!!.deliveryCharge > 0) overview!!.deliveryCharge.toString() else "40",
            onDismiss = { showAssignModal = false },
            onConfirmAssign = { courier, charge, instructions ->
                viewModel.assignCourier(
                    deliveryId = deliveryId,
                    courierPartner = courier,
                    deliveryCharge = charge,
                    instructions = instructions,
                    onSuccess = {
                        showAssignModal = false
                        viewModel.fetchCourierOverview(deliveryId)
                    }
                )
            }
        )
    }
}

// =============================================================================
// SUB-SECTIONS & COMPOSABLE CARDS
// =============================================================================

@Composable
private fun SectionSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
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
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    val tokens = LocalAppTokens.current
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
}

/**
 * Top Summary card with IDs, Priority/Status pills, Reassign button & Mark Completed.
 */
@Composable
private fun CourierOverviewHeaderCard(
    data: CourierDeliveryOverviewData,
    onAssignClick: () -> Unit,
    onMarkCompletedClick: () -> Unit,
    isCompleting: Boolean,
    isDelivered: Boolean,
    isReady: Boolean
) {
    val tokens = LocalAppTokens.current

    val (statusBg, statusColor) = remember(data.status) {
        when (data.status.lowercase()) {
            "delivered" -> Pair(greenBg, greentext)
            "out for delivery", "in transit" -> Pair(primary_light, Primary)
            "pending clearance" -> Pair(light_grey, headerGrey)
            else -> Pair(light_grey, title_color)
        }
    }

    SectionSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = data.internalRef.split(" / ").firstOrNull() ?: data.internalRef,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = "/ ${data.deliveryId}",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = data.status,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                // Priority Pill
                if (!data.priority.isNullOrBlank()) {
                    val isHighPriority = data.priority.equals("High Priority", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isHighPriority) redBg else light_grey)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = data.priority,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = if (isHighPriority) redText else headerGrey
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Customer: ${data.customerInfo?.customerName ?: "—"}",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isDelivered) {
                OutlinedButton(
                    onClick = onAssignClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight * 0.95f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = if (isReady) "Assign Courier" else "Reassign Courier",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }

            Button(
                onClick = onMarkCompletedClick,
                enabled = !isDelivered && !isReady && !isCompleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = disabled
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(tokens.buttonHeight * 0.95f)
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(16.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Mark Completed",
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

/**
 * Stepper for 5 stages: Ready -> Assigned -> In Transit -> Out for Delivery -> Delivered.
 */
@Composable
private fun CourierDeliveryProgressCard(
    currentStatus: String,
    progressSteps: List<DeliveryProgressStepDto>
) {
    val tokens = LocalAppTokens.current

    val stepLabels = listOf("Ready", "Assigned", "In Transit", "Out for Delivery", "Delivered")
    val currentIndex = stepLabels.indexOfFirst { it.equals(currentStatus, ignoreCase = true) }

    SectionSurface {
        SectionHeader(title = "Delivery Progress", icon = Icons.Default.Route)

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEachIndexed { index, label ->
                val reached = index <= currentIndex
                val isCurrent = index == currentIndex

                val iconVector = when (label) {
                    "Ready" -> Icons.Default.Check
                    "Assigned" -> Icons.Default.PersonAdd
                    "In Transit" -> Icons.Default.LocalShipping
                    "Out for Delivery" -> Icons.Default.LocationOn
                    else -> Icons.Default.Check
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Left line
                        if (index > 0) {
                            val leftLineColor = if (index <= currentIndex) darkGreenBg else sectionBorder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(2.dp)
                                    .align(Alignment.CenterStart)
                                    .background(leftLineColor)
                            )
                        }

                        // Right line
                        if (index < stepLabels.lastIndex) {
                            val rightLineColor = if (index < currentIndex) darkGreenBg else sectionBorder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(2.dp)
                                    .align(Alignment.CenterEnd)
                                    .background(rightLineColor)
                            )
                        }

                        // Circle Icon
                        val circleBg = when {
                            isCurrent -> Primary
                            reached -> darkGreenBg
                            else -> light_grey
                        }
                        val iconTint = when {
                            reached || isCurrent -> whiteBg
                            else -> mutedText
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) primary_light else sectionBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = label,
                        fontSize = tokens.caption,
                        fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                        color = if (isCurrent) Primary else if (reached) title_color else headerGrey,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Customer Information Card.
 */
@Composable
private fun CourierCustomerInfoCard(customerInfo: CourierCustomerInfoDto?) {
    val tokens = LocalAppTokens.current
    SectionSurface {
        SectionHeader(title = "Customer Information", icon = Icons.Default.Person)

        Spacer(modifier = Modifier.height(14.dp))

        ReadOnlyDataField(label = "Customer Name", value = customerInfo?.customerName.orEmpty())
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Customer ID", value = customerInfo?.customerCode ?: customerInfo?.customerId.orEmpty())
            }
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Phone Number", value = customerInfo?.phone.orEmpty())
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ReadOnlyDataField(label = "Email Address", value = customerInfo?.email.orEmpty())
    }
}

/**
 * Sales Order Reference Card.
 */
@Composable
private fun CourierSalesOrderReferenceCard(
    reference: CourierSalesOrderReferenceDto?,
    formatDateHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current

    val formattedDeliveryDate = remember(reference?.deliveryDate) {
        if (!reference?.deliveryDate.isNullOrBlank()) formatDateHelper(reference.deliveryDate)
        else "—"
    }

    SectionSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Sales Order Reference", icon = Icons.Default.Description)

            // Trial Completed Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Trial Completed",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
                MiniSwitch(
                    checked = reference?.trialCompleted == true,
                    onCheckedChange = { null },
                    enabled = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        ReadOnlyDataField(label = "Order ID", value = reference?.orderId.orEmpty())
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Garment Name", value = reference?.garmentName.orEmpty())
            }
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Garment Type", value = reference?.garmentType.orEmpty())
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ReadOnlyDataField(label = "Delivery Date", value = formattedDeliveryDate)
    }
}

/**
 * Product Details Card showing Piece Count & Area Issue tags.
 */
@Composable
private fun CourierProductDetailsCard(productDetails: CourierProductDetailsDto?) {
    val tokens = LocalAppTokens.current

    val activeCount = productDetails?.pieceCount ?: "Single Piece"
    val activeIssues = productDetails?.areaIssues ?: emptyList()

    SectionSurface {
        SectionHeader(title = "Product Details", icon = Icons.Default.Description)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Garment Piece Count",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Single Piece / Multiple Pieces Toggle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PIECE_COUNTS.forEach { countOption ->
                val isSelected = countOption.equals(activeCount, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primary_light else whiteBg)
                        .border(
                            1.dp,
                            if (isSelected) Primary else BorderGray,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countOption,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Primary else title_color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Garment Area Issue",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Issue Badges Flow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AREA_ISSUES.take(4).forEach { issue ->
                val isSelected = activeIssues.contains(issue)
                IssueTag(label = issue, isSelected = isSelected)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AREA_ISSUES.drop(4).forEach { issue ->
                val isSelected = activeIssues.contains(issue)
                IssueTag(label = issue, isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun IssueTag(label: String, isSelected: Boolean) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) primary_light else whiteBg)
            .border(1.dp, if (isSelected) Primary else BorderGray, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Primary else headerGrey
        )
    }
}

/**
 * Preferred Resolution Card.
 */
@Composable
private fun CourierPreferredResolutionCard(
    resolution: CourierPreferredResolutionDto?,
    formatDateHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current

    val formattedServiceDate = remember(resolution?.preferredServiceDate) {
        if (!resolution?.preferredServiceDate.isNullOrBlank()) formatDateHelper(resolution.preferredServiceDate)
        else "—"
    }

    SectionSurface {
        SectionHeader(title = "Preferred Resolution", icon = Icons.Default.CalendarMonth)

        Spacer(modifier = Modifier.height(14.dp))

        ReadOnlyDataField(label = "Preferred Service Date", value = formattedServiceDate)
        Spacer(modifier = Modifier.height(10.dp))
        ReadOnlyDataField(label = "Resolution Notes", value = resolution?.resolutionNotes.orEmpty())
    }
}

/**
 * Courier Assignment Card.
 */
@Composable
private fun CourierAssignmentCard(
    assignedCourier: AssignedCourierInfoDto?,
    deliveryCharge: Double
) {
    SectionSurface {
        SectionHeader(title = "Courier Assignment", icon = Icons.Default.LocalShipping)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Courier", value = assignedCourier?.courierName.orEmpty())
            }
            Box(modifier = Modifier.weight(1f)) {
                ReadOnlyDataField(label = "Delivery Charge", value = if (deliveryCharge > 0) deliveryCharge.toString() else "0")
            }
        }
    }
}

/**
 * Uniform styled Read-Only field replica.
 */
@Composable
private fun ReadOnlyDataField(
    label: String,
    value: String
) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PanelBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { "—" },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Normal,
                color = title_color
            )
        }
    }
}

// =============================================================================
// MODAL DIALOG: ASSIGN COURIER (Reused on Reassign Click)
// =============================================================================

@Composable
private fun AssignCourierOverviewModalDialog(
    deliveryId: String,
    orderId: String,
    courierOptions: List<String>,
    initialCourier: String,
    initialCharge: String,
    onDismiss: () -> Unit,
    onConfirmAssign: (courier: String, charge: Double, instructions: String?) -> Unit
) {
    val tokens = LocalAppTokens.current

    var selectedCourier by remember { mutableStateOf(initialCourier) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var deliveryChargeInput by remember { mutableStateOf(initialCharge) }
    var instructionsInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (tokens.isTablet) 0.65f else 0.94f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Delivery",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Banner Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(light_blue)
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assigning for Delivery ",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Text(
                            text = deliveryId,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = " (Order #$orderId)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field 1: Select Courier
                FormLabel(text = "Select Courier", isRequired = true)
                FormDropdown(
                    value = selectedCourier.ifBlank { "Select courier" },
                    expanded = isDropdownExpanded,
                    onExpandChange = { isDropdownExpanded = it },
                    options = courierOptions.ifEmpty { listOf("Loading couriers...") },
                    onOptionSelected = { selectedCourier = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Delivery Charge
                FormLabel(text = "Delivery Charge", isRequired = false)
                FormTextField(
                    value = deliveryChargeInput,
                    onValueChange = { deliveryChargeInput = it },
                    placeholder = "40",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Instruction for Courier
                FormLabel(text = "Instruction for Courier", isRequired = false)
                FormTextArea(
                    value = instructionsInput,
                    onValueChange = { instructionsInput = it },
                    placeholder = "Add specific stitching or adjustment instructions...",
                    minLines = 4,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons: Cancel and Assign courier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f)
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
                            val charge = deliveryChargeInput.toDoubleOrNull() ?: 0.0
                            onConfirmAssign(selectedCourier, charge, instructionsInput)
                        },
                        enabled = selectedCourier.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(tokens.buttonHeight * 0.9f)
                    ) {
                        Text(
                            text = "Assign courier",
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