@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.logistics.returns

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.ReturnItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.uriToMultipartPart
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private enum class ConfirmDialogAction {
    REJECT,
    APPROVE,
    SAVE
}

@Composable
fun ReturnRequestDetailsScreen(
    returnId: String,
    onNavigateBack: () -> Unit,
    onViewCustomerProfile: (String?) -> Unit = {},
    onViewOrderHistory: (String?) -> Unit = {},
    viewModel: LogisticsViewModel
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val returnDetail by viewModel.currentReturnDetail.collectAsState()
    val isLoading by viewModel.isLoadingReturnDetail.collectAsState()
    val isUpdating by viewModel.isUpdatingReturnStatus.collectAsState()
    val errorMessage by viewModel.returnDetailError.collectAsState()
    val updateStatusError by viewModel.updateReturnStatusError.collectAsState()

    // Form states
    var selectedWorkflowStatus by remember { mutableStateOf("Pending_Review") }
    var internalNotesText by remember { mutableStateOf("") }
    var confirmAction by remember { mutableStateOf<ConfirmDialogAction?>(null) }

    // State for newly picked local image URIs
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = (selectedImageUris + uris).distinct()

            val binaryParts = uris.mapNotNull { uri ->
                uriToMultipartPart(context, uri, "attachments")
            }

            if (binaryParts.isNotEmpty()) {
                DynamicIslandManager.showSuccess("Uploading image(s)...")
                viewModel.updateReturnStatus(
                    returnId = returnId,
                    newStatus = selectedWorkflowStatus,
                    internalNotes = internalNotesText,
                    attachmentParts = binaryParts,
                    onSuccess = { _ ->
                        DynamicIslandManager.showSuccess("Image uploaded successfully!")
                        selectedImageUris = emptyList() // clear local selection
                    }
                )
            }
        }
    }

    // Fetch details on enter
    LaunchedEffect(returnId) {
        if (returnId.isNotBlank()) {
            viewModel.fetchReturnDetail(returnId)
        }
    }

    // Synchronize initial data once loaded
    LaunchedEffect(returnDetail) {
        returnDetail?.let {
            selectedWorkflowStatus = it.status.ifBlank { "Pending_Review" }
            internalNotesText = it.internalNotes.orEmpty()
        }
    }

    // Emit backend update error to global DynamicIsland banner
    LaunchedEffect(updateStatusError) {
        updateStatusError?.let { error ->
            DynamicIslandManager.showError(error)
        }
    }

    // Emit initial load error to global DynamicIsland banner
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            if (returnDetail == null) {
                DynamicIslandManager.showError(error)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(title = "Return Details", onClose = onNavigateBack)
        },
        bottomBar = {
            if (returnDetail != null) {
                Surface(
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { confirmAction = ConfirmDialogAction.REJECT },
                                enabled = !isUpdating,
                                colors = ButtonDefaults.buttonColors(containerColor = redBg),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Text(
                                    text = "Reject Request",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = redText
                                )
                            }

                            OutlinedButton(
                                onClick = { confirmAction = ConfirmDialogAction.SAVE },
                                enabled = !isUpdating,
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Text(
                                    text = "Save Changes",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { confirmAction = ConfirmDialogAction.APPROVE },
                            enabled = !isUpdating,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Approve Request",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && returnDetail == null -> {
                    ListSkeleton()
                }
                errorMessage != null && returnDetail == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage ?: "Failed to load details", color = redText, fontSize = tokens.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.fetchReturnDetail(returnId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Retry", color = whiteBg)
                            }
                        }
                    }
                }
                returnDetail != null -> {
                    val item = returnDetail!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Overview Card
                        ReturnOverviewCard(item = item)

                        // 2. Customer Details Card
                        ReturnCustomerCard(
                            item = item,
                            onViewProfileClick = { onViewCustomerProfile(item.customer?.name) }
                        )

                        // 3. Original Order Details Card
                        ReturnOriginalOrderCard(
                            item = item,
                            onViewOrderHistoryClick = {
                                onViewOrderHistory(item.orderId?.id ?: item.orderCode)
                            }
                        )

                        // 4. Return Service Card
                        ReturnServiceCard(item = item)

                        // 5. Issue Description Card
                        ReturnIssueDescriptionCard(item = item)

                        // 6. Attachments Card (Remote files + newly picked local images)
                        ReturnAttachmentsCard(
                            item = item,
                            selectedImageUris = selectedImageUris,
                            onAddMoreClick = { galleryLauncher.launch("image/*") }
                        )

                        // 7. Internal Notes Card
                        ReturnInternalNotesCard(
                            notes = internalNotesText,
                            onNotesChange = { internalNotesText = it }
                        )

                        // 8. Decision Panel Card
                        ReturnDecisionPanelCard(
                            workflowStatus = selectedWorkflowStatus,
                            onWorkflowStatusChange = { selectedWorkflowStatus = it }
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Confirmation Modal
    if (confirmAction != null) {
        val action = confirmAction!!
        val title = when (action) {
            ConfirmDialogAction.REJECT -> "Reject Return Request?"
            ConfirmDialogAction.APPROVE -> "Approve Return Request?"
            ConfirmDialogAction.SAVE -> "Save Changes?"
        }
        val message = when (action) {
            ConfirmDialogAction.REJECT -> "Are you sure you want to reject this return request? This action will update the workflow status to Rejected."
            ConfirmDialogAction.APPROVE -> "Are you sure you want to approve this return request and move it to In Progress?"
            ConfirmDialogAction.SAVE -> "Are you sure you want to save the internal notes, selected workflow status, and newly added attachments?"
        }
        val confirmButtonText = when (action) {
            ConfirmDialogAction.REJECT -> "Reject Request"
            ConfirmDialogAction.APPROVE -> "Approve & Continue"
            ConfirmDialogAction.SAVE -> "Save Changes"
        }

        AlertDialog(
            onDismissRequest = { confirmAction = null },
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            containerColor = whiteBg,
            title = {
                Text(
                    text = title,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetStatus = when (action) {
                            ConfirmDialogAction.REJECT -> "Rejected"
                            ConfirmDialogAction.APPROVE -> "In_Progress"
                            ConfirmDialogAction.SAVE -> selectedWorkflowStatus
                        }

                        // Convert selected local image URIs into binary multipart parts
                        val binaryParts = selectedImageUris.mapNotNull { uri ->
                            uriToMultipartPart(context, uri, "attachments")
                        }

                        viewModel.updateReturnStatus(
                            returnId = returnId,
                            newStatus = targetStatus,
                            internalNotes = internalNotesText,
                            attachmentParts = binaryParts,
                            onSuccess = { msg ->
                                DynamicIslandManager.showSuccess(msg)
                                selectedImageUris = emptyList() // Clear local pick list on success
                                confirmAction = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action == ConfirmDialogAction.REJECT) redText else Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = confirmButtonText,
                        color = whiteBg,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary,
                        fontSize = tokens.bodySmall
                    )
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// SECTION CARDS (REUSABLE HELPERS)
// ---------------------------------------------------------------------------

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
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    iconBgColor: Color = primary_light,
    iconTint: Color = Primary,
    title: String,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Text(
                text = title,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
        trailingContent?.invoke()
    }
}

// 1. Overview Card
@Composable
private fun ReturnOverviewCard(item: ReturnItemDto) {
    val tokens = LocalAppTokens.current
    val statusBg = when (item.status) {
        "Pending_Review" -> orangeBg
        "In_Progress" -> primary_light
        "Completed" -> greenBg
        "Rejected" -> redBg
        else -> light_grey
    }
    val statusColor = when (item.status) {
        "Pending_Review" -> orangeText
        "In_Progress" -> Primary
        "Completed" -> greentext
        "Rejected" -> redText
        else -> TextSecondary
    }

    SectionContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(text = "Request Identifier", fontSize = tokens.label, color = mutedText)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "#${item.returnId}",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status.replace("_", " "),
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Return Type", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(text = item.returnType, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Request Date", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatHumanDate(item.createdAt),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(roseBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Priority", fontSize = tokens.label, color = redText)
                    Spacer(Modifier.height(2.dp))
                    Text(text = item.priority, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = redText)
                }
            }
        }
    }
}

// 2. Customer Details Card
@Composable
private fun ReturnCustomerCard(
    item: ReturnItemDto,
    onViewProfileClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val customer = item.customer
    val customerName = customer?.name ?: item.orderId?.customerSnapshot?.name ?: "Customer"
    val phone = customer?.phone ?: item.orderId?.customerSnapshot?.phone ?: "--"
    val email = customer?.email ?: item.orderId?.customerSnapshot?.email ?: "--"
    val address = customer?.address ?: item.orderId?.customerSnapshot?.shippingAddress?.street ?: "No address provided"

    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.Person,
            iconBgColor = primary_light,
            iconTint = Primary,
            title = "Customer Details",
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewProfileClick() }
                ) {
                    Text(text = "View Profile", fontSize = tokens.bodySmall, color = Primary, fontWeight = FontWeight.Medium)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                }
            }
        )

        Column {
            Text(text = "Customer Name", fontSize = tokens.label, color = mutedText)
            Spacer(Modifier.height(2.dp))
            Text(text = customerName, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Call, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Column {
                    Text(text = "Phone", fontSize = 10.sp, color = mutedText)
                    Text(text = phone, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Mail, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Column {
                    Text(text = "Email", fontSize = 10.sp, color = mutedText)
                    Text(text = email, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Column {
            Text(text = "Shipping Address", fontSize = tokens.label, color = mutedText)
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(10.dp)
            ) {
                Text(
                    text = address,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// 3. Original Order Details Card
@Composable
private fun ReturnOriginalOrderCard(
    item: ReturnItemDto,
    onViewOrderHistoryClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val order = item.orderId
    val orderCode = item.orderCode.ifBlank { order?.orderCode ?: "--" }
    val garmentItem = item.garmentSummary?.itemName ?: item.serviceCategory ?: "Custom Garment"

    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.ShoppingBag,
            iconBgColor = greenBg,
            iconTint = greentext,
            title = "Original Order Details",
            trailingContent = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(greenBg)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(text = order?.status ?: "Completed", fontSize = tokens.caption, color = greentext, fontWeight = FontWeight.Medium)
                }
            }
        )

        Column {
            Text(text = "Order Identifier", fontSize = tokens.label, color = mutedText)
            Text(text = "#$orderCode", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Column {
            Text(text = "Garment Item", fontSize = tokens.label, color = mutedText)
            Text(text = garmentItem, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(text = "Order Date", fontSize = tokens.label, color = mutedText)
                    Text(text = formatHumanDate(item.garmentSummary?.orderedDate ?: item.createdAt), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBgLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(text = "Delivery Date", fontSize = tokens.label, color = mutedText)
                    Text(text = formatHumanDate(item.garmentSummary?.deliveredDate ?: item.updatedAt), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }

        HorizontalDivider(color = sectionBorder, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewOrderHistoryClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "View Full Order History", fontSize = tokens.bodySmall, color = Primary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
        }
    }
}

// 4. Return Service Card
@Composable
private fun ReturnServiceCard(item: ReturnItemDto) {
    val tokens = LocalAppTokens.current
    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.Build,
            iconBgColor = primary_light,
            iconTint = Primary,
            title = "Return Service"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Service Category", fontSize = tokens.label, color = mutedText)
                Text(text = item.serviceCategory ?: "Suit Fitting & Adjustments", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Preferred Completion", fontSize = tokens.label, color = mutedText)
                Text(text = formatHumanDate(item.preferredCompletionDate ?: item.createdAt), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Priority Level", fontSize = tokens.label, color = mutedText)
                Text(text = item.priority, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Return Type", fontSize = tokens.label, color = mutedText)
                Text(text = item.returnType.replace("_", " "), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// 5. Issue Description Card
@Composable
private fun ReturnIssueDescriptionCard(item: ReturnItemDto) {
    val tokens = LocalAppTokens.current
    val issue = item.issueDescription?.ifBlank { null } ?: "No issue description provided."

    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.Description,
            iconBgColor = primary_light,
            iconTint = Primary,
            title = "Issue Description"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(cardBgLight)
                .padding(12.dp)
        ) {
            Text(
                text = issue,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }

        if (!item.preferredResolutionNotes.isNullOrBlank()) {
            Text(text = "Preferred Resolution Notes", fontSize = tokens.label, color = mutedText)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(light_blue)
                    .border(1.dp, light_blue_border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(text = item.preferredResolutionNotes, fontSize = tokens.bodySmall, color = TextPrimary)
            }
        }
    }
}

// 6. Attachments Card (Remote files + newly picked local images)
@Composable
private fun ReturnAttachmentsCard(
    item: ReturnItemDto,
    selectedImageUris: List<Uri>,
    onAddMoreClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val remoteAttachments = item.attachmentUrls
    val totalCount = remoteAttachments.size + selectedImageUris.size

    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.AttachFile,
            iconBgColor = primary_light,
            iconTint = Primary,
            title = "Attachments ($totalCount)",
            trailingContent = {
                if (totalCount > 0) {
                    Text(text = "Tap to expand", fontSize = tokens.caption, color = mutedText)
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Existing remote images
            remoteAttachments.forEachIndexed { index, url ->
                EvidenceThumbnail(
                    label = "Photo ${index + 1}",
                    imageModel = url
                )
            }

            // 2. Newly picked local images
            selectedImageUris.forEachIndexed { index, uri ->
                EvidenceThumbnail(
                    label = "New ${index + 1}",
                    imageModel = uri
                )
            }

            // 3. Add more dashed box (triggers gallery picker)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(8.dp))
                    .background(whiteBg)
                    .clickable { onAddMoreClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add More",
                        tint = mutedText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Add More", fontSize = tokens.label, color = mutedText)
                }
            }

            if (totalCount == 0) {
                Text(
                    text = "No evidence files uploaded",
                    fontSize = tokens.caption,
                    color = mutedText,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EvidenceThumbnail(label: String, imageModel: Any) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(light_grey)
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = whiteBg, fontWeight = FontWeight.Medium)
        }
    }
}

// 7. Internal Notes Card
@Composable
private fun ReturnInternalNotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.EditNote,
            iconBgColor = orangeBg,
            iconTint = orangeText,
            title = "Internal Notes"
        )

        FormTextArea(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = "Type internal processing notes here...",
            minLines = 3,
            maxLines = 5
        )
    }
}

// 8. Decision Panel Card
@Composable
private fun ReturnDecisionPanelCard(
    workflowStatus: String,
    onWorkflowStatusChange: (String) -> Unit
) {
    var isStatusMenuExpanded by remember { mutableStateOf(false) }

    val statusDisplayMap = remember {
        mapOf(
            "Pending_Review" to "Pending Review",
            "In_Progress" to "In Progress",
            "Completed" to "Completed",
            "Rejected" to "Rejected"
        )
    }

    val statusOptions = remember {
        listOf("Pending Review", "In Progress", "Completed", "Rejected")
    }

    SectionContainer {
        SectionHeader(
            icon = Icons.Outlined.CheckCircle,
            iconBgColor = primary_light,
            iconTint = Primary,
            title = "Decision Panel"
        )

        FormDropdown(
            label = "Update Workflow Status",
            value = statusDisplayMap[workflowStatus] ?: "Pending Review",
            expanded = isStatusMenuExpanded,
            onExpandChange = { isStatusMenuExpanded = it },
            options = statusOptions,
            onOptionSelected = { selectedLabel ->
                val backendKey = statusDisplayMap.entries.firstOrNull { it.value == selectedLabel }?.key ?: "Pending_Review"
                onWorkflowStatusChange(backendKey)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// DATE UTILITY
// ---------------------------------------------------------------------------
private fun formatHumanDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(rawDate)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: rawDate
    } catch (_: Exception) {
        rawDate
    }
}