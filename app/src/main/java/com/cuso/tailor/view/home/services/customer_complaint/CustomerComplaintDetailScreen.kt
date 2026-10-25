@file:Suppress("unused", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.services.customer_complaint

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.ServicesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// Option definitions with id-to-label mapping
private val STATUS_OPTIONS = listOf(
    "Open" to "Open",
    "In_Investigation" to "In Investigation / In Progress",
    "Pending_Customer_Response" to "Pending Customer Response",
    "Resolution_Approved" to "Resolution Approved",
    "Resolved" to "Resolved",
    "Closed" to "Closed",
    "Rejected" to "Rejected"
)

private val CONTACT_CHANNEL_OPTIONS = listOf(
    "Phone_Call" to "Phone Call",
    "WhatsApp" to "WhatsApp",
    "In_Person_Showroom" to "In-Person Showroom Visit",
    "Email" to "Email",
    "SMS" to "SMS"
)

private val CUSTOMER_MOOD_OPTIONS = listOf(
    "Neutral_Cooperative" to "Neutral / Cooperative",
    "Angry_Frustrated" to "Angry / Frustrated",
    "Demanding_Escalation" to "Demanding Escalation",
    "Satisfied_Calm" to "Satisfied / Calm"
)

private val RESOLUTION_ACTION_OPTIONS = listOf(
    "Fabric_Replacement" to "Fabric Replacement",
    "Garment_Remake" to "Garment Remake",
    "Free_Rework_Alteration" to "Free Rework / Alteration",
    "Full_Refund" to "Full Financial Refund",
    "Partial_Refund" to "Partial Refund",
    "Store_Credit_Voucher" to "Store Credit Voucher",
    "Rejected_Customer_Fault" to "Reject Complaint (Customer Misuse)",
    "Apology_Settlement" to "Apology Settlement"
)

@Composable
fun ComplaintDetailViewScreen(
    complaintId: String,
    viewModel: ServicesViewModel,
    hrViewModel: HrViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(),
    onBack: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokens: AppDesignTokens = LocalAppTokens.current
    val detail by viewModel.complaintDetail.collectAsState()
    val isLoading by viewModel.isLoadingComplaintDetail.collectAsState()
    val errorMessage by viewModel.complaintDetailError.collectAsState()
    val isUploading by viewModel.isUploadingAttachment.collectAsState()
    val isUpdatingStatus by viewModel.isUpdatingComplaintStatus.collectAsState()
    val isAssigningStaff by viewModel.isAssigningStaff.collectAsState()
    val isSavingNote by viewModel.isSavingInternalNote.collectAsState()
    val isResolving by viewModel.isResolvingComplaint.collectAsState()

    // Fetch members from HR when screen opens
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers(page = 1, limit = 50, search = null, status = "Active")
    }
    val staffMembers by hrViewModel.members.collectAsState()

    // Dialog state controllers
    var showStatusDialog by remember { mutableStateOf(false) }
    var showAssignStaffDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showFollowUpDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(complaintId) {
        if (complaintId.isNotBlank()) {
            viewModel.fetchComplaintDetail(complaintId)
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val part = uriToMultipartPart(context, uri, "attachments")
            if (part != null) {
                viewModel.uploadComplaintAttachment(
                    complaintId = complaintId,
                    imagePart = part,
                    onSuccess = { Toast.makeText(context, "Attachment uploaded successfully", Toast.LENGTH_SHORT).show() },
                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                )
            } else {
                Toast.makeText(context, "Unable to read image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Resolve customer and ticket data
    val customerName = detail?.customerSnapshot?.name ?: detail?.customer?.fullName ?: "-"
    val customerPhone = detail?.customerSnapshot?.phone ?: detail?.customer?.mobileNumber ?: "—"
    val customerEmail = detail?.customerSnapshot?.email ?: detail?.customer?.email ?: "—"
    val customerAddress = detail?.customer?.billingAddress?.fullAddressDisplay ?: "-"

    val customerSince = formatYearOnly(detail?.customer?.createdAt)
    val ticketCode = detail?.complaintCode ?: "-"
    val title = detail?.complaintTitle ?: "-"
    val category = detail?.complaintCategory?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "-"
    val reportedDate = formatDisplayDate(detail?.createdAt)
    val invoiceCode = detail?.salesOrder?.orderCode ?: "-"
    val description = detail?.description ?: "-"

    val staffName = detail?.assignedTo?.fullName ?: "-"
    val staffRole = detail?.assignedTo?.role?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "-"
    val rawStatus = detail?.status ?: "Open"
    val status = rawStatus.replace("_", " ").replaceFirstChar { it.uppercase() }

    // Check if complaint is resolved, closed, or rejected -> hide actions if resolved
    val isResolvedOrClosed = rawStatus.equals("Resolved", ignoreCase = true) ||
            rawStatus.equals("Closed", ignoreCase = true) ||
            rawStatus.equals("Rejected", ignoreCase = true)

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = ticketCode,
                onClose = onBack,
                trailingContent = {
                    Surface(
                        modifier = Modifier
                            .size(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .clickable { },
                        color = whiteBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Download",
                                tint = title_color,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }

                    Button(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = tokens.extraPadding * 1.2f, vertical = 0.dp),
                        modifier = Modifier.height(tokens.fieldHeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize * 0.85f)
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
            )
        }
    ) { padding ->
        if (isLoading) {
            ListSkeleton()
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
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.1f)
            ) {
                // 1. TOP CARD: TITLE & TICKET STATUS
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            StatusBadge(
                                text = status,
                                textColor = if (rawStatus == "Resolved") greentext else Primary,
                                backgroundColor = if (rawStatus == "Resolved") greenBg else light_blue
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                        Text(
                            text = "Ticket Reference ID: $ticketCode",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // 2. STATS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
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

                // 3. CUSTOMER DETAILS CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { }
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
                                    modifier = Modifier.size(tokens.iconSize * 0.75f)
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.imageDp * 0.75f)
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
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Customer Since $customerSince",
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(light_blue)
                                .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .padding(tokens.extraPadding)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
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
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { }
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
                                    modifier = Modifier.size(tokens.iconSize * 0.75f)
                                )
                            }
                        }
                    }
                }

                // 4. COMPLAINT DESCRIPTION CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(light_blue)
                                .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
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
                                modifier = Modifier.padding(tokens.extraPadding * 1.1f)
                            )
                        }
                    }
                }

                // 5. EVIDENCE & ATTACHMENTS CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val attachments = detail?.attachments.orEmpty()
                            val boxSize = tokens.imageDp * 1.45f

                            if (attachments.isNotEmpty()) {
                                attachments.forEach { att ->
                                    val isVideo = att.url.endsWith(".mp4", ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(boxSize)
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .background(light_blue)
                                    ) {
                                        if (isVideo) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = headerGrey,
                                                    modifier = Modifier.size(tokens.iconSize * 1.4f)
                                                )
                                            }
                                        } else {
                                            AsyncImage(
                                                model = att.url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(statLogoBg.copy(alpha = 0.75f))
                                                .padding(vertical = 3.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = att.originalName ?: "ATTACHMENT",
                                                fontSize = tokens.label,
                                                color = whiteBg,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            // Upload Container
                            Box(
                                modifier = Modifier
                                    .size(boxSize)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(light_blue)
                                    .clickable(enabled = !isUploading) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(tokens.iconSize * 1.2f),
                                            color = Primary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Uploading...",
                                            fontSize = tokens.label,
                                            color = headerGrey,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircleOutline,
                                            contentDescription = "Upload",
                                            tint = headerGrey,
                                            modifier = Modifier.size(tokens.iconSize * 1.3f)
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
                }

                // 6. MANAGEMENT ACTIONS CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Primary)
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_thunder),
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.extraPadding * 1.2f)
                        ) {
                            ActionOptionItem(
                                icon = Icons.Outlined.Refresh,
                                label = "Update Status",
                                tokens = tokens,
                                onClick = { showStatusDialog = true }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.PersonAdd,
                                label = "Assign Staff",
                                tokens = tokens,
                                onClick = { showAssignStaffDialog = true }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                label = "Add Internal Note",
                                tokens = tokens,
                                onClick = { showNoteDialog = true }
                            )
                            ActionOptionItem(
                                icon = Icons.Outlined.NotificationsNone,
                                label = "Add Follow-up",
                                tokens = tokens,
                                onClick = { showFollowUpDialog = true }
                            )

                            // Disappear Approve Replacement & Close Complaint buttons if status is Resolved/Closed/Rejected
                            if (!isResolvedOrClosed) {
                                Spacer(Modifier.height(tokens.extraPadding))

                                // Approve Replacement Button
                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = greentext, disabledContainerColor = greenBg)
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

                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                                // Close Complaint Button
                                OutlinedButton(
                                    onClick = {
                                        val targetId = detail?.id?.ifBlank { null } ?: complaintId
                                        viewModel.updateComplaintStatus(
                                            complaintId = targetId,
                                            status = "Closed",
                                            onSuccess = { Toast.makeText(context, "Complaint closed", Toast.LENGTH_SHORT).show() },
                                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, redBg),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = redText,
                                        contentColor = whiteBg,
                                        disabledContainerColor = redBg
                                    )
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
                                        color = whiteBg
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. ASSIGNED PERSONNEL CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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
                                    .background(light_blue)
                                    .border(1.dp, light_blue_border, CircleShape),
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
                                    color = title_color,
                                    lineHeight = 13.sp
                                )
                                Text(
                                    text = staffRole,
                                    fontSize = tokens.caption,
                                    color = mutedText,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }

                // 8. ACTIVITY TIMELINE CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
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
                                color = Primary
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = BorderGray,
                            thickness = 1.dp
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
                            description = "Ticket generated via Customer Web Portal.",
                            meta = null,
                            isActive = false,
                            isLast = true,
                            tokens = tokens
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))
            }
        }
    }

    // ─────────────────────────────────────────────
    // MANAGEMENT ACTION DIALOGS
    // ─────────────────────────────────────────────

    // 1. Update Ticket Status Dialog
    if (showStatusDialog) {
        val targetComplaintId = detail?.id?.ifBlank { null } ?: complaintId

        UpdateTicketStatusDialog(
            currentStatusId = rawStatus,
            isLoading = isUpdatingStatus,
            tokens = tokens,
            onDismiss = { if (!isUpdatingStatus) showStatusDialog = false },
            onSave = { newStatusId ->
                viewModel.updateComplaintStatus(
                    complaintId = targetComplaintId,
                    status = newStatusId,
                    onSuccess = { successMessage ->
                        showStatusDialog = false
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // 2. Assign Operations Staff Dialog
    if (showAssignStaffDialog) {
        val targetComplaintId = detail?.id?.ifBlank { null } ?: complaintId

        AssignOperationsStaffDialog(
            currentStaffName = staffName,
            staffList = staffMembers,
            isLoading = isAssigningStaff,
            tokens = tokens,
            onDismiss = { if (!isAssigningStaff) showAssignStaffDialog = false },
            onAssign = { selectedMemberId ->
                viewModel.assignComplaintStaff(
                    complaintId = targetComplaintId,
                    memberId = selectedMemberId,
                    onSuccess = { successMsg ->
                        showAssignStaffDialog = false
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    },
                    onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // 3. Add Internal Observation Note Dialog
    if (showNoteDialog) {
        val targetComplaintId = detail?.id?.ifBlank { null } ?: complaintId

        AddInternalNoteDialog(
            isLoading = isSavingNote,
            tokens = tokens,
            onDismiss = { if (!isSavingNote) showNoteDialog = false },
            onSave = { enteredNote ->
                viewModel.addComplaintInternalNote(
                    complaintId = targetComplaintId,
                    note = enteredNote,
                    onSuccess = { successMsg ->
                        showNoteDialog = false
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    },
                    onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // 4. Log Customer Communication (Follow-up) Dialog
    if (showFollowUpDialog) {
        LogCustomerCommunicationDialog(
            tokens = tokens,
            onDismiss = { showFollowUpDialog = false },
            onSave = { channelId, summary, moodId ->
                showFollowUpDialog = false
                Toast.makeText(context, "Follow-up logged", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 5. Resolve Complaint / Approve Replacement Dialog
    if (showResolveDialog) {
        val targetComplaintId = detail?.id?.ifBlank { null } ?: complaintId

        ResolveComplaintDialog(
            isLoading = isResolving,
            tokens = tokens,
            onDismiss = { if (!isResolving) showResolveDialog = false },
            onResolve = { actionId, summary ->
                viewModel.resolveComplaint(
                    complaintId = targetComplaintId,
                    actionId = actionId,
                    summary = summary,
                    onSuccess = { successMsg ->
                        showResolveDialog = false
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    },
                    onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

// ─────────────────────────────────────────────
// DIALOG IMPLEMENTATIONS (REUSING FORM COMPONENTS)
// ─────────────────────────────────────────────

@Composable
private fun UpdateTicketStatusDialog(
    currentStatusId: String,
    isLoading: Boolean = false,
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onSave: (statusId: String) -> Unit
) {
    val statusMap = remember { STATUS_OPTIONS.associate { it.second to it.first } }
    val displayLabels = remember { STATUS_OPTIONS.map { it.second } }

    val initialLabel = remember(currentStatusId) {
        STATUS_OPTIONS.firstOrNull { it.first.equals(currentStatusId, ignoreCase = true) }?.second
            ?: displayLabels.first()
    }

    var selectedLabel by remember { mutableStateOf(initialLabel) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Ticket Status",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color, modifier = Modifier.size(tokens.iconSize))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f), color = BorderGray)

                FormDropdown(
                    value = selectedLabel,
                    expanded = expanded,
                    enabled = !isLoading,
                    onExpandChange = { expanded = it },
                    options = displayLabels,
                    onOptionSelected = {
                        selectedLabel = it
                        expanded = false
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(text = "Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            val statusId = statusMap[selectedLabel] ?: "Open"
                            onSave(statusId)
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(tokens.iconSize * 0.85f), color = whiteBg, strokeWidth = 2.dp)
                        } else {
                            Text(text = "Save", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignOperationsStaffDialog(
    currentStaffName: String,
    staffList: List<com.cuso.tailor.model.hr.MemberItem>,
    isLoading: Boolean = false,
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onAssign: (selectedMemberId: String) -> Unit
) {
    val staffMap = remember(staffList) {
        staffList.associate { member ->
            val name = "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim().ifBlank { member.userId?.email ?: "Staff" }
            name to member._id
        }
    }

    val displayOptions = remember(staffMap) {
        if (staffMap.isEmpty()) listOf("Loading staff...") else staffMap.keys.toList()
    }

    var selectedStaffName by remember(staffMap, currentStaffName) {
        mutableStateOf(
            displayOptions.firstOrNull { it.equals(currentStaffName, ignoreCase = true) }
                ?: displayOptions.firstOrNull()
                ?: "Select an option"
        )
    }

    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Operations Staff",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color, modifier = Modifier.size(tokens.iconSize))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f), color = BorderGray)

                FormDropdown(
                    value = selectedStaffName,
                    expanded = expanded,
                    enabled = !isLoading && staffList.isNotEmpty(),
                    onExpandChange = { expanded = it },
                    options = displayOptions,
                    onOptionSelected = { chosenName ->
                        selectedStaffName = chosenName
                        expanded = false
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(text = "Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            val memberId = staffMap[selectedStaffName]
                            if (!memberId.isNullOrBlank()) {
                                onAssign(memberId)
                            }
                        },
                        enabled = !isLoading && staffMap.containsKey(selectedStaffName),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(tokens.iconSize * 0.85f), color = whiteBg, strokeWidth = 2.dp)
                        } else {
                            Text(text = "Assign", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddInternalNoteDialog(
    isLoading: Boolean = false,
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Internal Observation Note",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color, modifier = Modifier.size(tokens.iconSize))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f), color = BorderGray)

                FormTextArea(
                    value = noteText,
                    onValueChange = { noteText = it },
                    enabled = !isLoading,
                    placeholder = "Private technician notes, lab inspection details...",
                    minLines = 4,
                    maxLines = 5,
                    borderColor = BorderGray,
                    focusedBorderColor = Primary
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(text = "Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = { onSave(noteText) },
                        enabled = !isLoading && noteText.isNotBlank(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(tokens.iconSize * 0.85f), color = whiteBg, strokeWidth = 2.dp)
                        } else {
                            Text(text = "Save Note", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCustomerCommunicationDialog(
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onSave: (channelId: String, summary: String, moodId: String) -> Unit
) {
    val channelMap = remember { CONTACT_CHANNEL_OPTIONS.associate { it.second to it.first } }
    val moodMap = remember { CUSTOMER_MOOD_OPTIONS.associate { it.second to it.first } }

    val channelLabels = remember { CONTACT_CHANNEL_OPTIONS.map { it.second } }
    val moodLabels = remember { CUSTOMER_MOOD_OPTIONS.map { it.second } }

    var selectedChannelLabel by remember { mutableStateOf(channelLabels.first()) }
    var selectedMoodLabel by remember { mutableStateOf(moodLabels.first()) }
    var summaryText by remember { mutableStateOf("") }

    var channelExpanded by remember { mutableStateOf(false) }
    var moodExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Customer Communication",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f), color = BorderGray)

                FormDropdown(
                    label = "Contact Channel",
                    isRequired = true,
                    value = selectedChannelLabel,
                    expanded = channelExpanded,
                    onExpandChange = { channelExpanded = it },
                    options = channelLabels,
                    onOptionSelected = {
                        selectedChannelLabel = it
                        channelExpanded = false
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormLabel(text = "Discussion Summary", isRequired = true)
                FormTextArea(
                    value = summaryText,
                    onValueChange = { summaryText = it },
                    placeholder = "What was discussed with customer...",
                    minLines = 3,
                    maxLines = 4,
                    borderColor = BorderGray,
                    focusedBorderColor = Primary
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormDropdown(
                    label = "Customer Reaction / Mood",
                    isRequired = false,
                    value = selectedMoodLabel,
                    expanded = moodExpanded,
                    onExpandChange = { moodExpanded = it },
                    options = moodLabels,
                    onOptionSelected = {
                        selectedMoodLabel = it
                        moodExpanded = false
                    }
                )

                Spacer(Modifier.height(tokens.screenPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(text = "Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }
                    Spacer(Modifier.width(tokens.extraPadding))
                    Button(
                        onClick = {
                            val channelId = channelMap[selectedChannelLabel] ?: "Phone_Call"
                            val moodId = moodMap[selectedMoodLabel] ?: "Neutral_Cooperative"
                            onSave(channelId, summaryText, moodId)
                        },
                        enabled = summaryText.isNotBlank(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled)
                    ) {
                        Text(text = "Save Follow-up", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. RESOLVE COMPLAINT DIALOG (APPROVE REPLACEMENT)
// ─────────────────────────────────────────────

@Composable
private fun ResolveComplaintDialog(
    isLoading: Boolean = false,
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onResolve: (actionId: String, summary: String) -> Unit
) {
    val actionMap = remember { RESOLUTION_ACTION_OPTIONS.associate { it.second to it.first } }
    val displayLabels = remember { RESOLUTION_ACTION_OPTIONS.map { it.second } }

    var selectedActionLabel by remember { mutableStateOf(displayLabels.first()) }
    var summaryText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                // Header with Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Approve Resolution / Replacement",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(tokens.iconSize * 1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = close_color, modifier = Modifier.size(tokens.iconSize))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = tokens.extraPadding * 0.8f), color = BorderGray)

                // Resolution Action Dropdown (Sends exact ID e.g. Garment_Remake)
                FormDropdown(
                    label = "Resolution Action",
                    isRequired = true,
                    value = selectedActionLabel,
                    expanded = expanded,
                    enabled = !isLoading,
                    onExpandChange = { expanded = it },
                    options = displayLabels,
                    onOptionSelected = { chosen ->
                        selectedActionLabel = chosen
                        expanded = false
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                // Resolution Summary Field
                FormLabel(text = "Resolution Summary", isRequired = true)
                FormTextArea(
                    value = summaryText,
                    onValueChange = { summaryText = it },
                    enabled = !isLoading,
                    placeholder = "Enter resolution summary / agreement notes...",
                    minLines = 3,
                    maxLines = 4,
                    borderColor = BorderGray,
                    focusedBorderColor = Primary
                )

                Spacer(Modifier.height(tokens.screenPadding))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(text = "Cancel", fontSize = tokens.bodySmall, color = title_color)
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            val actionId = actionMap[selectedActionLabel] ?: "Garment_Remake"
                            onResolve(actionId, summaryText)
                        },
                        enabled = !isLoading && summaryText.isNotBlank(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greentext,
                            disabledContainerColor = greenBg
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(tokens.iconSize * 0.85f), color = whiteBg, strokeWidth = 2.dp)
                        } else {
                            Text(text = "Approve & Resolve", fontSize = tokens.bodySmall, color = whiteBg)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// COMPONENT HELPERS & TIMELINE WITH CONCENTRIC CIRCLES
// ─────────────────────────────────────────────

@Composable
private fun ComplaintStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = tokens.extraPadding * 0.8f, vertical = tokens.extraPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize * 1.7f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(tokens.iconSize * 0.95f)
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            Text(
                text = label,
                fontSize = tokens.caption,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = tokens.bodySmall,
                color = title_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = tokens.extraPadding * 0.6f),
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

/**
 * Pixel-perfect Concentric Circle Timeline Event Item matching the design
 */
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isActive) primary_light else lightGray)
                    .border(
                        width = 1.5.dp,
                        color = if (isActive) Primary else mutedText,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Primary else mutedText)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(tokens.buttonHeight * 1.5f)
                        .background(BorderGray)
                )
            }
        }

        Spacer(Modifier.width(tokens.extraPadding * 0.8f))

        Column(modifier = Modifier.padding(bottom = tokens.extraPadding * 0.8f)) {
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