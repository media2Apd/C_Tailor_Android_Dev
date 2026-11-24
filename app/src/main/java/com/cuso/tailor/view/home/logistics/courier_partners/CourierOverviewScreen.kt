@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.courier_partners

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CourierActivityTimelineDto
import com.cuso.tailor.model.logistics.CourierPartnerDetailData
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun CourierPartnerOverviewScreen(
    courierId: String,
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    onEditCourierClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val detail by viewModel.selectedCourierPartnerDetail.collectAsState()
    val isLoading by viewModel.isLoadingCourierPartnerDetail.collectAsState()
    val errorMessage by viewModel.courierPartnerDetailError.collectAsState()
    val isToggling by viewModel.isTogglingCourierStatus.collectAsState()

    var showToggleConfirmDialog by remember { mutableStateOf(false) }

    // Fetch courier partner details from the API
    LaunchedEffect(courierId) {
        viewModel.fetchCourierPartnerDetail(courierId)
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Courier Partner Overview",
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
                        text = errorMessage ?: "Failed to load partner details",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = redText
                    )
                }
            }

            detail != null -> {
                val data = detail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.extraPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // Top Overview Card
                    item {
                        CourierTopHeaderCard(
                            data = data,
                            onEditClick = onEditCourierClick,
                            onToggleStatusClick = { showToggleConfirmDialog = true },
                            formatDateHelper = { iso -> viewModel.formatDeliveryDate(iso) }
                        )
                    }

                    // Reliability Score Card
                    item {
                        ReliabilityScoreCard(data = data)
                    }

                    // Basic Information Card
                    item {
                        BasicInformationCard(data = data)
                    }

                    // Serviceability Card
                    item {
                        ServiceabilityCard(
                            data = data,
                            onModifyZonesClick = onEditCourierClick
                        )
                    }

                    // Notes Card (shown only when notes exist in the API response)
                    if (data.notes.isNotBlank()) {
                        item {
                            NotesCard(notes = data.notes)
                        }
                    }

                    // Activity Timeline Card
                    item {
                        ActivityTimelineCard(
                            data = data,
                            formatDateTimeHelper = { iso ->
                                val datePart = viewModel.formatCourierDate(iso)
                                val timePart = viewModel.formatDateTimeToHuman(iso)
                                if (datePart == "--") "--" else "$datePart, $timePart"
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(tokens.screenPadding))
                    }
                }
            }
        }
    }

    // Confirmation dialog for enabling or disabling the courier partner
    if (showToggleConfirmDialog && detail != null) {
        val courier = detail!!
        EnableDisableCourierDialog(
            courierName = courier.courierName,
            currentStatus = courier.status,
            isSubmitting = isToggling,
            onDismiss = { showToggleConfirmDialog = false },
            onConfirm = { nextStatus ->
                viewModel.toggleCourierPartnerStatus(courierId, nextStatus) { message ->
                    DynamicIslandManager.showSuccess(message)
                    showToggleConfirmDialog = false
                }
            }
        )
    }
}

// =============================================================================
// SUB-SECTIONS & COMPOSABLE CARDS
// =============================================================================

@Composable
private fun CardSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
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

/**
 * Top Header Card displaying Courier Name, Status Badge, Created Date, and Action Buttons.
 */
@Composable
private fun CourierTopHeaderCard(
    data: CourierPartnerDetailData,
    onEditClick: () -> Unit,
    onToggleStatusClick: () -> Unit,
    formatDateHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current
    val isActive = data.status.equals("Active", ignoreCase = true)

    val formattedCreatedDate = remember(data.createdAt) {
        if (!data.createdAt.isNullOrBlank()) formatDateHelper(data.createdAt) else "--"
    }

    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.courierName.ifBlank { "--" },
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))

            // Status Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(if (isActive) greenBg else redBg)
                    .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.2f)
            ) {
                Text(
                    text = data.status.ifBlank { "--" },
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) greentext else redText
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))

        // Created Date Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(tokens.iconSize * 0.85f)
            )
            Text(
                text = "Created: $formattedCreatedDate",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.95f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = title_color,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                    Text(
                        text = "Edit Courier",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }

            Button(
                onClick = onToggleStatusClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.95f)
            ) {
                Text(
                    text = if (isActive) "Disable" else "Enable",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = whiteBg
                )
            }
        }
    }
}

/**
 * Reliability Score Card backed by API data.
 */
@Composable
private fun ReliabilityScoreCard(data: CourierPartnerDetailData) {
    val tokens = LocalAppTokens.current

    CardSurface {
        Text(
            text = "Reliability Score",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
        ) {
            Text(
                text = "${data.reliabilityScore}%",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            Icon(
                imageVector = Icons.Default.NorthEast,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(tokens.iconSize * 0.9f)
            )
        }
    }
}

/**
 * Basic Information Card displaying partner metadata and API service types.
 */
@Composable
private fun BasicInformationCard(data: CourierPartnerDetailData) {
    val tokens = LocalAppTokens.current

    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Basic Information",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(tokens.iconSize * 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

        // Courier Name and Current Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            InfoTile(
                label = "Courier Name",
                value = data.courierName.ifBlank { "--" },
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding)
            ) {
                Column {
                    Text(
                        text = "Current Status",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(tokens.extraPadding * 0.3f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (data.status.equals("Active", ignoreCase = true)) greentext else redText,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                        Text(
                            text = data.status.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }
        }

        // Integration Type and Shiprocket Courier ID from API
        if (!data.integrationType.isNullOrBlank() || data.shiprocketCourierId != null) {
            Spacer(modifier = Modifier.height(tokens.extraPadding))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                InfoTile(
                    label = "Integration Type",
                    value = data.integrationType?.ifBlank { "--" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                if (data.shiprocketCourierId != null) {
                    InfoTile(
                        label = "Shiprocket ID",
                        value = data.shiprocketCourierId.toString(),
                        modifier = Modifier.weight(1f)
                    )
                } else if (data.transportMode.isNotBlank()) {
                    InfoTile(
                        label = "Transport Mode",
                        value = data.transportMode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        Text(
            text = "Service Types",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding))

        if (data.serviceTypes.isEmpty()) {
            Text(
                text = "No service types configured",
                fontSize = tokens.caption,
                color = mutedText
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                data.serviceTypes.forEach { service ->
                    ServiceTypeCard(title = service)
                }
            }
        }
    }
}

@Composable
private fun InfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(PanelBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding)
    ) {
        Column {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.3f))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ServiceTypeCard(title: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .background(PanelBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .padding(tokens.extraPadding * 1.2f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 1.8f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .background(light_blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            Text(
                text = title,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
    }
}

/**
 * Serviceability Card backed completely by data.serviceability from the API.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceabilityCard(
    data: CourierPartnerDetailData,
    onModifyZonesClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val serviceability = data.serviceability
    val coverageType = serviceability?.coverageType?.uppercase().orEmpty()
    val coveredCountries = serviceability?.coveredCountries.orEmpty()
    val coveredPostalCodes = serviceability?.coveredPostalCodes.orEmpty()
    val primaryHubs = serviceability?.primaryHubs.orEmpty()

    val (coverageTitle, coverageDesc) = when (coverageType) {
        "GLOBAL" -> "Global Coverage" to "Delivers worldwide to the listed countries."
        "DOMESTIC" -> "All India Coverage" to "Delivers across all serviceable locations in India."
        "REGIONAL" -> "Regional Coverage" to "Delivers to the listed countries."
        "SELECTED_POSTAL_CODES" -> "Selected Pincodes" to "Delivers only to the listed postal codes."
        else -> (serviceability?.coverageType?.ifBlank { "--" } ?: "--") to ""
    }

    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Serviceability",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f),
                modifier = Modifier.clickable { onModifyZonesClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.75f)
                )
                Text(
                    text = "Modify Zones",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                .background(PanelBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                .padding(tokens.extraPadding * 1.2f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.8f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(light_blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Column {
                    Text(
                        text = coverageTitle,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    if (coverageDesc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.2f))
                        Text(
                            text = coverageDesc,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                    }
                }
            }
        }

        // Covered Countries from API
        if (coveredCountries.isNotEmpty() && coverageType != "SELECTED_POSTAL_CODES") {
            Spacer(modifier = Modifier.height(tokens.screenPadding))
            Text(
                text = "Covered Countries",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
            ) {
                coveredCountries.forEach { country ->
                    HubChip(label = country)
                }
            }
        }

        // Covered Postal Codes from API
        if (coveredPostalCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(tokens.screenPadding))
            Text(
                text = "Covered Postal Codes",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )
            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
            ) {
                coveredPostalCodes.forEach { postalCode ->
                    HubChip(label = postalCode)
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        Text(
            text = "Primary Hubs",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

        if (primaryHubs.isEmpty()) {
            Text(
                text = "No primary hubs configured",
                fontSize = tokens.caption,
                color = mutedText
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
            ) {
                primaryHubs.forEach { hub ->
                    HubChip(label = hub)
                }
            }
        }
    }
}

@Composable
private fun HubChip(label: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(PanelBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.5f)
    ) {
        Text(
            text = label,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = title_color
        )
    }
}

/**
 * Notes Card displaying internal remarks from API.
 */
@Composable
private fun NotesCard(notes: String) {
    val tokens = LocalAppTokens.current
    CardSurface {
        Text(
            text = "Notes",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Primary
        )
        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))
        Text(
            text = notes,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = title_color
        )
    }
}

/**
 * Activity Timeline Card driven by API activityTimeline or createdAt/updatedAt timestamps.
 */
@Composable
private fun ActivityTimelineCard(
    data: CourierPartnerDetailData,
    formatDateTimeHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current

    // Build timeline entries strictly from API fields
    val timelineEntries = remember(data) {
        if (data.activityTimeline.isNotEmpty()) {
            data.activityTimeline
        } else {
            buildList {
                if (!data.updatedAt.isNullOrBlank()) {
                    add(
                        CourierActivityTimelineDto(
                            title = "Last Updated",
                            description = "",
                            timestamp = data.updatedAt
                        )
                    )
                }
                if (!data.createdAt.isNullOrBlank()) {
                    add(
                        CourierActivityTimelineDto(
                            title = "Created",
                            description = "",
                            timestamp = data.createdAt
                        )
                    )
                }
            }
        }
    }

    CardSurface {
        Text(
            text = "Activity Timeline",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Primary
        )

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        if (timelineEntries.isEmpty()) {
            Text(
                text = "No activity recorded",
                fontSize = tokens.caption,
                color = mutedText
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                timelineEntries.forEachIndexed { index, entry ->
                    val isFirst = index == 0
                    val isLast = index == timelineEntries.lastIndex
                    val formattedTime = if (entry.timestamp.isNotBlank()) {
                        formatDateTimeHelper(entry.timestamp)
                    } else {
                        "--"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(tokens.iconSize * 0.9f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.iconSize * 0.65f)
                                    .clip(CircleShape)
                                    .background(if (isFirst) Primary else headerGrey)
                            )
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(tokens.buttonHeight * 1.1f)
                                        .background(sectionBorder)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 0.dp else tokens.extraPadding)
                        ) {
                            Text(
                                text = entry.title.ifBlank { "--" },
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = formattedTime,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            if (entry.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.2f))
                                Text(
                                    text = entry.description,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Normal,
                                    color = mutedText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG: ENABLE / DISABLE COURIER
// =============================================================================

@Composable
private fun EnableDisableCourierDialog(
    courierName: String,
    currentStatus: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nextStatus: String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val nextStatus = if (currentStatus.equals("Active", ignoreCase = true)) "Inactive" else "Active"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (tokens.isTablet) 0.55f else 0.88f),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.imageDp * 0.9f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.9f))
                        .background(light_blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize * 1.4f)
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                Text(
                    text = "Enable/Disable Courier",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                Text(
                    text = "Are you sure you want to change the status of $courierName to $nextStatus?",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(tokens.cardPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = light_grey,
                            contentColor = title_color
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
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
                        onClick = { onConfirm(nextStatus) },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = disabled
                        ),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Text(
                                text = "Confirm",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                Text(
                    text = "Action will be logged in system audit",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
            }
        }
    }
}