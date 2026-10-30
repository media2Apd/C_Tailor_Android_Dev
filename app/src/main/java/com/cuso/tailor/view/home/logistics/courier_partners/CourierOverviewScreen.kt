@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.courier_partners

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
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
    val context = LocalContext.current

    val detail by viewModel.selectedCourierPartnerDetail.collectAsState()
    val isLoading by viewModel.isLoadingCourierPartnerDetail.collectAsState()
    val errorMessage by viewModel.courierPartnerDetailError.collectAsState()
    val isToggling by viewModel.isTogglingCourierStatus.collectAsState()

    var showToggleConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courierId) {
        viewModel.fetchCourierPartnerDetail(courierId)
    }

    Scaffold(
        containerColor = Primary_background,
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
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Overview Card (Name, Status, Edit, Enable/Disable)
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
                        ServiceabilityCard(data = data)
                    }

                    // Activity Timeline Card
                    item {
                        ActivityTimelineCard(
                            data = data,
                            formatDateHelper = { iso -> viewModel.formatDateTimeToHuman(iso) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Modal Confirmation Dialog: Enable / Disable Courier matching React code
    if (showToggleConfirmDialog && detail != null) {
        val courier = detail!!
        EnableDisableCourierDialog(
            courierName = courier.courierName,
            currentStatus = courier.status,
            isSubmitting = isToggling,
            onDismiss = { showToggleConfirmDialog = false },
            onConfirm = { nextStatus ->
                viewModel.toggleCourierPartnerStatus(courierId, nextStatus) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

/**
 * Top Header Card with Courier Name, Status Badge, and Action Buttons.
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
        if (!data.createdAt.isNullOrBlank()) formatDateHelper(data.createdAt)
        else "12 Jan 2024"
    }

    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.courierName,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )

            // Status Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) greenBg else redBg)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isActive) "Active" else "Inactive",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) greentext else redText
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Created Date Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row: Edit Courier & Enable / Disable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onEditClick,
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
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 0.95f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Enable/Disable",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    }
}

/**
 * Reliability Score Card.
 */
@Composable
private fun ReliabilityScoreCard(data: CourierPartnerDetailData) {
    val tokens = LocalAppTokens.current

    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Reliability Score",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey
            )

            // Trend Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(greenBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = greentext,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "+2.4%",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = greentext
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Score Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Top tier delivery accuracy maintained over the last 30 days.",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = mutedText
        )
    }
}

/**
 * Basic Information Card with Status and Service Types.
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

        Spacer(modifier = Modifier.height(14.dp))

        // Courier Name and Current Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "Courier Name",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = data.courierName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "Current Status",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (data.status.equals("Active", ignoreCase = true)) greentext else redText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (data.status.equals("Active", ignoreCase = true)) "Operational" else "Inactive",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Service Types",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(10.dp))

        ServiceTypeCard(
            title = if (data.serviceTypes.isNotEmpty()) data.serviceTypes[0] + " Delivery" else "Express Delivery",
            subtitle = "24–48 Hour Turnaround",
            icon = Icons.Default.ElectricBolt
        )

        Spacer(modifier = Modifier.height(8.dp))

        ServiceTypeCard(
            title = if (data.serviceTypes.size > 1) data.serviceTypes[1] + " Delivery" else "Standard Delivery",
            subtitle = "3–5 Business Days",
            icon = Icons.Default.AccessTime
        )
    }
}

@Composable
private fun ServiceTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PanelBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
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

            Column {
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey
                )
            }
        }
    }
}

/**
 * Serviceability Card.
 */
@Composable
private fun ServiceabilityCard(data: CourierPartnerDetailData) {
    val tokens = LocalAppTokens.current

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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { /* Modify zones action */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Modify Zones",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PanelBg)
                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                        text = "All India Coverage",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Service available across all major states and Union Territories.",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Primary Hubs",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Normal,
            color = headerGrey
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HubChip(label = "110001 (Delhi)")
            HubChip(label = "400001 (Mumbai)")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HubChip(label = "560001 (Bengaluru)")
            HubChip(label = "600001 (Chennai)")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(light_blue)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "+ 42 more",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    }
}

@Composable
private fun HubChip(label: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PanelBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
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
 * Activity Timeline Card.
 */
@Composable
private fun ActivityTimelineCard(
    data: CourierPartnerDetailData,
    formatDateHelper: (String) -> String
) {
    val tokens = LocalAppTokens.current

    val lastUpdatedDate = remember(data.updatedAt) {
        if (!data.updatedAt.isNullOrBlank()) formatDateHelper(data.updatedAt)
        else "22 May 2024, 14:30 PM"
    }

    val createdDate = remember(data.createdAt) {
        if (!data.createdAt.isNullOrBlank()) formatDateHelper(data.createdAt)
        else "12 Jan 2024, 09:15 AM"
    }

    CardSurface {
        Text(
            text = "Activity Timeline",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(52.dp)
                            .background(sectionBorder)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Last Updated",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lastUpdatedDate,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profile parameters modified by Admin",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = mutedText
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(headerGrey)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Created Date",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = createdDate,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Onboarded into Logistics Management",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = mutedText
                    )
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG: ENABLE / DISABLE COURIER (Matching React modal exactly)
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
            border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon matching React's FileUser in bg-indigo-50
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(light_blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Enable/Disable Courier",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description matching React text: "Are you sure you want to change the status of {name} to {nextStatus}?"
                Text(
                    text = "Are you sure you want to change the status of $courierName to $nextStatus?",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = headerGrey,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons: Cancel and Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = light_grey,
                            contentColor = title_color
                        ),
                        shape = RoundedCornerShape(8.dp),
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
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
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

                Spacer(modifier = Modifier.height(18.dp))

                // System Audit Subtitle
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