
@file:Suppress("unused")
package com.cuso.tailor.view.home.services.customer_complaint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ComplaintDetailViewScreen(
    complaintId: String,
    viewModel: ServicesViewModel,
    onBack: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val detail by viewModel.complaintDetail.collectAsState()
    val isLoading by viewModel.isLoadingComplaintDetail.collectAsState()
    val errorMessage by viewModel.complaintDetailError.collectAsState()

    LaunchedEffect(complaintId) {
        if (complaintId.isNotBlank()) {
            viewModel.fetchComplaintDetail(complaintId)
        }
    }

    val customerName = detail?.customerSnapshot?.name
        ?: detail?.customer?.fullName
        ?: "Customer"
    val customerPhone = detail?.customerSnapshot?.phone
        ?: detail?.customer?.mobileNumber
        ?: "—"
    val customerEmail = detail?.customerSnapshot?.email
        ?: detail?.customer?.email
        ?: "—"
    val customerAddress = detail?.customer?.billingAddress?.fullAddressDisplay
        ?: "Kumily, Idukki District, Kerala"

    val customerSince = formatYearOnly(detail?.customer?.createdAt)
    val ticketCode = detail?.complaintCode ?: "COMP-00124"
    val title = detail?.complaintTitle ?: "Product Complaint"
    val category = detail?.complaintCategory?.replace("_", " ") ?: "Product Defect"
    val reportedDate = formatDisplayDate(detail?.createdAt)
    val invoiceCode = detail?.salesOrder?.orderCode ?: "INV-2026-1123"
    val description = detail?.description
        ?: "Customer reported product issue. Awaiting further inspection."

    val staffName = detail?.assignedTo?.fullName ?: "Ramesh Kumar"
    val staffRole = detail?.assignedTo?.role?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
        ?: "Senior Operations Executive"

    val status = detail?.status?.replace("_", " ") ?: "In Progress"

    Scaffold(
        containerColor = whiteBg,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.7f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(tokens.buttonHeight * 0.8f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = title_color,
                                    modifier = Modifier.size(tokens.iconSize * 1.1f)
                                )
                            }
                            Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                            Column {
                                Text(
                                    text = "Ticket Ref",
                                    fontSize = tokens.label,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = ticketCode,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { /* Download PDF or action */ },
                                modifier = Modifier
                                    .size(tokens.buttonHeight * 0.85f)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Download",
                                    tint = title_color,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }

                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))

                            Button(
                                onClick = onEditClick,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.4f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                                Text(
                                    text = "Edit",
                                    fontSize = tokens.bodySmall,
                                    color = whiteBg,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (!errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Failed to load",
                    color = redText,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // ─────────────────────────────────────────────
                // 1. TOP CARD: TITLE & TICKET REFERENCE
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = title,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(tokens.extraPadding))
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                color = light_blue
                            ) {
                                Text(
                                    text = status,
                                    fontSize = tokens.label,
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.6f, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                        Text(
                            text = "Ticket Reference ID: $ticketCode",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // ─────────────────────────────────────────────
                // 2. STATS ROW (3 SUMMARY CARDS)
                // ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)
                ) {
                    ComplaintStatCard(
                        icon = Icons.Outlined.Tag,
                        iconColor = chartViolet,
                        iconBg = purpleBg,
                        label = "Complaint ID",
                        value = ticketCode,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                    ComplaintStatCard(
                        icon = Icons.Outlined.Layers,
                        iconColor = orangeText,
                        iconBg = orangeBg,
                        label = "Category",
                        value = category,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                    ComplaintStatCard(
                        icon = Icons.Outlined.CalendarToday,
                        iconColor = greentext,
                        iconBg = greenBg,
                        label = "Reported Date",
                        value = reportedDate,
                        modifier = Modifier.weight(1f),
                        tokens = tokens
                    )
                }

                // ─────────────────────────────────────────────
                // 3. CUSTOMER DETAILS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                Text(
                                    text = "Customer Details",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { /* CRM Action */ }
                            ) {
                                Text(
                                    text = "View CRM Profile",
                                    fontSize = tokens.caption,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Customer Avatar & Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.imageDp * 0.8f)
                                    .clip(CircleShape)
                                    .background(lightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customerName.take(2).uppercase(),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }

                            Spacer(Modifier.width(tokens.extraPadding))

                            Column {
                                Text(
                                    text = customerName,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Text(
                                    text = "Customer Since $customerSince",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Customer Contact Details Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(light_blue)
                                .padding(tokens.extraPadding)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.7f)) {
                                CustomerContactRow(
                                    icon = Icons.Outlined.Phone,
                                    text = customerPhone,
                                    tokens = tokens
                                )
                                CustomerContactRow(
                                    icon = Icons.Outlined.Email,
                                    text = customerEmail,
                                    tokens = tokens
                                )
                                CustomerContactRow(
                                    icon = Icons.Outlined.LocationOn,
                                    text = customerAddress,
                                    tokens = tokens
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Associated Invoice row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Associated Invoice",
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { /* Navigate to Invoice */ }
                            ) {
                                Text(
                                    text = invoiceCode,
                                    fontSize = tokens.bodySmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 4. COMPLAINT DESCRIPTION CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                            Text(
                                text = "Complaint Description",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Left vertical accent bar with description text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Primary)
                            )
                            Text(
                                text = "\"$description\"",
                                fontSize = tokens.bodySmall,
                                color = title_color,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(tokens.extraPadding)
                            )
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 5. EVIDENCE & ATTACHMENTS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                            Text(
                                text = "Evidence & Attachments",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            val attachments = detail?.attachments.orEmpty()
                            if (attachments.isNotEmpty()) {
                                attachments.take(2).forEach { att ->
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.imageDp * 1.5f)
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    ) {
                                        AsyncImage(
                                            model = att.url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(blackTitle.copy(alpha = 0.5f))
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = att.originalName ?: "IMG_001.JPG",
                                                fontSize = tokens.label,
                                                color = whiteBg,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            // Upload Container
                            Box(
                                modifier = Modifier
                                    .size(tokens.imageDp * 1.5f)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(lightGray)
                                    .clickable { /* Upload action */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddCircleOutline,
                                        contentDescription = "Upload",
                                        tint = headerGrey,
                                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Upload",
                                        fontSize = tokens.label,
                                        color = headerGrey,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 6. MANAGEMENT ACTIONS CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        // Blue Header banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Primary)
                                .padding(tokens.extraPadding)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                Text(
                                    text = "Management Actions",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(tokens.screenPadding)) {
                            ActionOptionItem(
                                icon = Icons.Outlined.Refresh,
                                label = "Update Status",
                                tokens = tokens,
                                onClick = { }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.PersonAdd,
                                label = "Assign Staff",
                                tokens = tokens,
                                onClick = { }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                label = "Add Internal Note",
                                tokens = tokens,
                                onClick = { }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.NotificationsNone,
                                label = "Add Follow-up",
                                tokens = tokens,
                                onClick = { }
                            )

                            Spacer(Modifier.height(tokens.extraPadding))

                            // Approve Replacement Button
                            Button(
                                onClick = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = whiteBg,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                Text(
                                    text = "Approve Replacement",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }

                            Spacer(Modifier.height(tokens.extraPadding * 0.7f))

                            // Close Complaint Button
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = BorderStroke(1.dp, redBg),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = redText)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = redText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                Text(
                                    text = "Close Complaint",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = redText
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 7. ASSIGNED PERSONNEL CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Text(
                            text = "Assigned Personnel",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.buttonHeight * 0.95f)
                                    .clip(CircleShape)
                                    .background(light_blue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = staffName.take(2).uppercase(),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                            }

                            Spacer(Modifier.width(tokens.extraPadding))

                            Column {
                                Text(
                                    text = staffName,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Text(
                                    text = staffRole,
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────
                // 8. ACTIVITY TIMELINE CARD
                // ─────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                            Text(
                                text = "Activity Timeline",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )

                        TimelineEventItem(
                            date = reportedDate,
                            title = "Resolution Initiated",
                            description = "Quality Control team has been assigned to inspect the return shipment.",
                            meta = "Logged by $staffName",
                            isActive = true,
                            isLast = false,
                            tokens = tokens
                        )

                        TimelineEventItem(
                            date = reportedDate,
                            title = "Claim Approved",
                            description = "Initial claim verification complete. Authorized for physical inspection.",
                            meta = null,
                            isActive = true,
                            isLast = false,
                            tokens = tokens
                        )

                        TimelineEventItem(
                            date = reportedDate,
                            title = "Initial Review",
                            description = "Complaint reviewed by Customer Success team.",
                            meta = null,
                            isActive = false,
                            isLast = false,
                            tokens = tokens
                        )

                        TimelineEventItem(
                            date = reportedDate,
                            title = "Complaint Submitted",
                            description = "Customer logged complaint for bridal blouse dye bleed.",
                            meta = null,
                            isActive = false,
                            isLast = true,
                            tokens = tokens
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))
            }
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENT HELPERS
// ─────────────────────────────────────────────

@Composable
private fun ComplaintStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    iconBg: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = whiteBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding * 0.8f),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 1.6f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            Text(
                text = label,
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = title_color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CustomerContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tokens: AppDesignTokens
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = headerGrey,
            modifier = Modifier.size(tokens.iconSize * 0.9f)
        )
        Spacer(Modifier.width(tokens.extraPadding * 0.6f))
        Text(
            text = text,
            fontSize = tokens.bodySmall,
            color = title_color,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ActionOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = tokens.extraPadding * 0.7f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 1.6f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(light_blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                )
            }
            Spacer(Modifier.width(tokens.extraPadding))
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                color = title_color,
                fontWeight = FontWeight.Normal
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = mutedText,
            modifier = Modifier.size(tokens.iconSize)
        )
    }
}

@Composable
private fun TimelineEventItem(
    date: String,
    title: String,
    description: String,
    meta: String?,
    isActive: Boolean,
    isLast: Boolean,
    tokens: AppDesignTokens
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 0.7f)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isActive) Primary else BorderGray,
                        CircleShape
                    )
                    .background(whiteBg)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(tokens.buttonHeight * 1.3f)
                        .background(BorderGray)
                )
            }
        }

        Spacer(Modifier.width(tokens.extraPadding))

        Column(modifier = Modifier.padding(bottom = tokens.extraPadding)) {
            Text(
                text = date,
                fontSize = tokens.caption,
                color = if (isActive) Primary else mutedText,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = title_color,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Normal
            )
            if (!meta.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = meta,
                    fontSize = tokens.label,
                    color = mutedText,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private fun formatDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "20 Feb 2026"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: "20 Feb 2026"
    } catch (_: Exception) {
        "20 Feb 2026"
    }
}

private fun formatYearOnly(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "2023"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: "2023"
    } catch (_: Exception) {
        "2023"
    }
}